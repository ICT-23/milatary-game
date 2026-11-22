// Walkability map for one theme, used when authoring supply caches and
// ground hostiles. Usage:
//   node tools/free-scan.js platform -34 34 -20 34 2
// Prints a grid where '.' is walkable with the player radius applied.
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

const theme = process.argv[2] || 'platform';
const x0 = Number(process.argv[3] || -40);
const x1 = Number(process.argv[4] || 40);
const z0 = Number(process.argv[5] || -40);
const z1 = Number(process.argv[6] || 40);
const step = Number(process.argv[7] || 2);

const env = sandbox.EMBERWATCH_LEVELS.buildEnvironment(theme, makeNode());

function free(x, z) {
  for (const c of env.colliders) {
    if (x > c.minX - 0.7 && x < c.maxX + 0.7 && z > c.minZ - 0.7 && z < c.maxZ + 0.7) return false;
  }
  return true;
}

console.log('theme ' + theme + '  x ' + x0 + '..' + x1 + '  z ' + z0 + '..' + z1 + '  step ' + step);

for (let z = z0; z <= z1; z += step) {
  let row = '';
  for (let x = x0; x <= x1; x += step) row += free(x, z) ? '.' : '#';
  console.log(String(z).padStart(4, ' ') + ' ' + row);
}

console.log('\nlegend: . walkable   # blocked   first column is x=' + x0);
