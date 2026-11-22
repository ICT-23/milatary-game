// Loads levels.js with a stubbed THREE and checks that every spawn, objective,
// supply cache and ground hostile can be reached inside its own radius.
const fs = require('fs');
const path = require('path');
const vm = require('vm');

const ROOT = path.resolve(__dirname, '..');
const LEVELS = path.join(ROOT, 'app/src/main/assets/game/levels.js');

function vec(x, y, z) {
  return {
    x: x || 0, y: y || 0, z: z || 0,
    set(a, b, c) { this.x = a; this.y = b; this.z = c; return this; },
    copy(v) { this.x = v.x; this.y = v.y; this.z = v.z; return this; },
    clone() { return vec(this.x, this.y, this.z); },
    distanceTo(v) { return Math.hypot(this.x - v.x, this.y - v.y, this.z - v.z); },
    addScaledVector() { return this; },
    applyAxisAngle() { return this; },
    normalize() { return this; },
    length() { return Math.hypot(this.x, this.y, this.z); },
    lengthSq() { return this.x * this.x + this.y * this.y + this.z * this.z; }
  };
}

function makeNode() {
  return {
    position: vec(), rotation: vec(), scale: vec(),
    material: { color: 0, opacity: 1, dispose() {} },
    geometry: { dispose() {} },
    children: [], userData: {}, visible: true,
    castShadow: false, receiveShadow: false,
    shadow: { mapSize: { width: 1024, height: 1024, set() {} }, camera: {}, bias: 0 },
    add(child) { this.children.push(child); return this; },
    remove() {}, lookAt() {}, rotateX() {}, rotateY() {}, rotateZ() {}
  };
}

const THREE = new Proxy({}, {
  get(target, prop) {
    if (typeof prop !== 'string') return undefined;
    if (/^[A-Z0-9_]+$/.test(prop)) return 1;
    return function Stub() { return makeNode(); };
  }
});

const sandbox = { THREE, console, Math, Date, JSON, Object, Array, Number, String, Boolean, isNaN, parseInt, parseFloat };
sandbox.window = sandbox;
sandbox.globalThis = sandbox;
vm.createContext(sandbox);
vm.runInContext(fs.readFileSync(LEVELS, 'utf8'), sandbox);

const LIB = sandbox.EMBERWATCH_LEVELS;
if (!LIB) throw new Error('levels.js did not publish EMBERWATCH_LEVELS');

const PLAYER_RADIUS = 0.6;
let problems = 0;

function pointFree(env, x, z, radius) {
  const pad = radius === undefined ? PLAYER_RADIUS : radius;
  const b = env.bounds;
  if (x < b.minX + pad || x > b.maxX - pad) return false;
  if (z < b.minZ + pad || z > b.maxZ - pad) return false;
  for (const c of env.colliders) {
    if (x > c.minX - pad && x < c.maxX + pad &&
        z > c.minZ - pad && z < c.maxZ + pad) return false;
  }
  return true;
}

// Mirrors the engine: the player only has to get inside `radius` of the point.
function nearestFree(env, x, z, radius) {
  if (pointFree(env, x, z)) return { x, z, dist: 0 };
  for (let r = 0.5; r <= radius + 0.001; r += 0.5) {
    for (let a = 0; a < 24; a++) {
      const angle = (a / 24) * Math.PI * 2;
      const px = x + Math.cos(angle) * r;
      const pz = z + Math.sin(angle) * r;
      if (pointFree(env, px, pz)) return { x: +px.toFixed(1), z: +pz.toFixed(1), dist: r };
    }
  }
  return null;
}

const ids = Object.keys(LIB.MISSIONS);
console.log('missions: ' + ids.length);

for (const id of ids) {
  const mission = LIB.MISSIONS[id];
  const env = LIB.buildEnvironment(mission.theme, makeNode());
  const lines = [];

  const spawn = nearestFree(env, mission.spawn[0], mission.spawn[2], 0);
  if (!spawn) { problems += 1; lines.push('  SPAWN UNREACHABLE'); }

  mission.objectives.forEach((obj, index) => {
    const radius = obj.radius || (obj.kind === 'destroy' || obj.kind === 'defend' ? 0 : 6);
    const tag = 'objective ' + (index + 1) + ' [' + obj.kind + ']';
    if (obj.kind === 'destroy' || obj.kind === 'defend') {
      // Position is a marker only; the objective is driven by squad counts.
      if (!pointFree(env, obj.pos[0], obj.pos[2]) && index === 0) {
        lines.push('  note: ' + tag + ' marker sits on geometry (harmless)');
      }
      return;
    }
    const hit = nearestFree(env, obj.pos[0], obj.pos[2], radius * 0.85);
    if (!hit) {
      problems += 1;
      lines.push('  UNREACHABLE ' + tag + ' radius=' + radius + ' @ ' + obj.pos[0] + ',' + obj.pos[2]);
    } else if (hit.dist > 0) {
      lines.push('  offset ' + tag + ' reachable at ' + hit.x + ',' + hit.z + ' (' + hit.dist + 'm)');
    }
    if (obj.auto && hit && hit.dist > radius * 0.6) {
      problems += 1;
      lines.push('  AUTO OBJECTIVE TOO TIGHT ' + tag + ' needs ' + hit.dist + 'm of ' + radius);
    }
  });

  (mission.supplies || []).forEach((sup, index) => {
    const hit = nearestFree(env, sup.x, sup.z, 1.4);
    if (!hit) {
      problems += 1;
      lines.push('  UNREACHABLE supply ' + (index + 1) + ' [' + sup.type + '] @ ' + sup.x + ',' + sup.z);
    } else if (hit.dist > 0) {
      lines.push('  offset supply ' + (index + 1) + ' best at ' + hit.x + ',' + hit.z + ' (' + hit.dist + 'm)');
    }
  });

  // Ground hostiles stand on the deck, so they need a genuinely free footprint.
  // Bosses arrive through an objective's `spawn` list instead of `drones`.
  const GROUND_KINDS = ['sentinel', 'walker', 'boss'];
  const groundHostiles = [];
  (mission.drones || []).forEach((d) => {
    if (GROUND_KINDS.includes(d.kind)) groundHostiles.push({ def: d, where: 'placed' });
  });
  (mission.objectives || []).forEach((obj) => {
    (obj.spawn || []).forEach((d) => {
      if (GROUND_KINDS.includes(d.kind)) groundHostiles.push({ def: d, where: 'objective spawn' });
    });
    if (obj.kind !== 'defend') return;
    (obj.waves || []).forEach((wave, waveIndex) => {
      wave.forEach((d) => {
        if (GROUND_KINDS.includes(d.kind)) {
          groundHostiles.push({ def: d, where: 'wave ' + (waveIndex + 1) });
        }
      });
    });
  });

  const groundCounts = {};
  groundHostiles.forEach(({ def, where }) => {
    groundCounts[def.kind] = (groundCounts[def.kind] || 0) + 1;
    const footprint = def.kind === 'boss' ? 1.7 : (def.kind === 'walker' ? 1.3 : 1.2);
    if (!pointFree(env, def.x, def.z, footprint) && !nearestFree(env, def.x, def.z, footprint)) {
      problems += 1;
      lines.push('  BLOCKED ' + def.kind + ' (' + where + ') @ ' + def.x + ',' + def.z);
    } else if (!pointFree(env, def.x, def.z, footprint)) {
      problems += 1;
      lines.push('  BLOCKED ' + def.kind + ' (' + where + ') footprint @ ' + def.x + ',' + def.z);
    }
  });

  console.log('\n' + id + '  ch' + mission.chapter + '  theme=' + mission.theme +
    '  colliders=' + env.colliders.length + '  supplies=' + (mission.supplies || []).length +
    '  ground=' + groundHostiles.length +
    (groundHostiles.length ? ' (' + Object.keys(groundCounts).map((k) => k + ':' + groundCounts[k]).join(', ') + ')' : ''));
  lines.forEach((line) => console.log(line));
}

for (const id of ids) {
  const m = LIB.MISSIONS[id];
  if (typeof m.parSeconds !== 'number') { problems += 1; console.log('MISSING parSeconds ' + id); }
  if (!m.extraction) { problems += 1; console.log('MISSING extraction ' + id); }
  if (m.objectives.some((o) => o.kind === 'choice') && !m.choice) {
    problems += 1;
    console.log('CHOICE OBJECTIVE WITHOUT choice BLOCK ' + id);
  }
  if (!m.supplies || m.supplies.length === 0) {
    problems += 1;
    console.log('MISSING supplies ' + id);
  }
  (m.supplies || []).forEach((s) => {
    if (s.type !== 'ammo' && s.type !== 'medkit') {
      problems += 1;
      console.log('BAD supply type ' + s.type + ' in ' + id);
    }
  });
}

console.log('\nPROBLEMS: ' + problems);
process.exit(problems === 0 ? 0 : 1);
