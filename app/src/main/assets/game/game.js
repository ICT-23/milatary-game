// OPERATION: EMBERWATCH 3D — Real-time tactical engine
// Three.js (WebGL) renderer, third-person operative rig, hostile drone AI,
// magazine-fed carbine, mission objective state machine and native bridge.
// Chapter data and environments live in levels.js.

(function () {
  'use strict';

  // ------------------------------------------------------------ CONFIG / BRIDGE
  var bridge = window.AndroidBridge || null;

  function readConfig() {
    var defaults = {
      missionId: 'M1_SIGNAL_IN_DUST',
      sensitivity: 1,
      sfxVolume: 0.8,
      haptics: true,
      highContrast: false,
      quality: 'MEDIUM',
      resolutionScale: 1,
      shadowDistance: 34,
      textureTier: 1,
      particleBudget: 0.8,
      foliageDensity: 1,
      postProcessingTier: 1,
      aiBudget: 1,
      masterVolume: 0.9,
      dialogueVolume: 0.85,
      subtitles: true
    };
    if (bridge && typeof bridge.getPlatformConfig === 'function') {
      try {
        var raw = bridge.getPlatformConfig();
        if (raw) {
          var parsed = JSON.parse(raw);
          defaults.missionId = parsed.missionId || defaults.missionId;
          defaults.sensitivity = typeof parsed.sensitivity === 'number' ? parsed.sensitivity : defaults.sensitivity;
          defaults.sfxVolume = typeof parsed.sfxVolume === 'number' ? parsed.sfxVolume : defaults.sfxVolume;
          defaults.haptics = parsed.haptics !== false;
          defaults.highContrast = parsed.highContrast === true;
          // Section 21.3: the host already resolved the preset, so the engine only reads values.
          defaults.quality = parsed.quality === 'LOW' || parsed.quality === 'HIGH' ? parsed.quality : defaults.quality;
          defaults.resolutionScale = typeof parsed.resolutionScale === 'number' ? parsed.resolutionScale : defaults.resolutionScale;
          defaults.shadowDistance = typeof parsed.shadowDistance === 'number' ? parsed.shadowDistance : defaults.shadowDistance;
          defaults.textureTier = typeof parsed.textureTier === 'number' ? parsed.textureTier : defaults.textureTier;
          defaults.particleBudget = typeof parsed.particleBudget === 'number' ? parsed.particleBudget : defaults.particleBudget;
          defaults.foliageDensity = typeof parsed.foliageDensity === 'number' ? parsed.foliageDensity : defaults.foliageDensity;
          defaults.postProcessingTier = typeof parsed.postProcessingTier === 'number' ? parsed.postProcessingTier : defaults.postProcessingTier;
          defaults.aiBudget = typeof parsed.aiBudget === 'number' ? parsed.aiBudget : defaults.aiBudget;
          defaults.masterVolume = typeof parsed.masterVolume === 'number' ? parsed.masterVolume : defaults.masterVolume;
          defaults.dialogueVolume = typeof parsed.dialogueVolume === 'number' ? parsed.dialogueVolume : defaults.dialogueVolume;
          defaults.subtitles = parsed.subtitles !== false;
        }
      } catch (err) { /* fall through to defaults */ }
    } else {
      var params = new URLSearchParams(window.location.search);
      if (params.get('mission')) defaults.missionId = params.get('mission');
      if (params.get('quality')) defaults.quality = params.get('quality').toUpperCase();
    }
    return defaults;
  }

  var CONFIG = readConfig();
  var LIB = window.EMBERWATCH_LEVELS;
  var LEVEL = (LIB && LIB.MISSIONS[CONFIG.missionId]) || (LIB && LIB.MISSIONS.M1_SIGNAL_IN_DUST);

  // Section 21.3: the preset decides anti-aliasing and how much of the effect budget
  // is spent. Nothing is locked away; a lower preset simply draws less.
  var QUALITY = {
    LOW: { antialias: false, resolutionScale: CONFIG.resolutionScale, particleScale: CONFIG.particleBudget, shadowMap: false },
    MEDIUM: { antialias: true, resolutionScale: CONFIG.resolutionScale, particleScale: CONFIG.particleBudget, shadowMap: true },
    HIGH: { antialias: true, resolutionScale: CONFIG.resolutionScale, particleScale: CONFIG.particleBudget, shadowMap: true }
  }[CONFIG.quality] || { antialias: true, resolutionScale: 1, particleScale: 0.8, shadowMap: true };

  function haptic(ms) {
    if (!CONFIG.haptics) return;
    if (bridge && typeof bridge.triggerHaptic === 'function') {
      try { bridge.triggerHaptic(ms); } catch (err) {}
    }
  }

  function notifyMissionComplete(score, choice, timeSec, healthPct) {
    if (bridge && typeof bridge.onMissionComplete === 'function') {
      try {
        bridge.onMissionComplete(Math.round(score), String(choice || ''), Math.round(timeSec), Math.round(healthPct));
        return true;
      } catch (err) {}
    }
    return false;
  }

  function notifyAbort() {
    if (bridge && typeof bridge.onAbortMission === 'function') {
      try { bridge.onAbortMission(); return true; } catch (err) {}
    }
    return false;
  }

  // ----------------------------------------------------------------- DOM LOOKUP
  function el(id) { return document.getElementById(id); }

  var ui = {
    container: el('game-container'),
    dialogue: el('dialogue-modal'),
    dialogueSpeaker: el('dialogue-speaker'),
    dialogueText: el('dialogue-text'),
    dialogueNext: el('dialogue-next-btn'),
    choice: el('choice-modal'),
    choiceTitle: el('choice-title'),
    choiceDesc: el('choice-desc'),
    choiceOptions: el('choice-options'),
    victory: el('victory-modal'),
    victorySub: el('victory-sub'),
    victoryStars: el('victory-stars'),
    victoryTime: el('victory-time'),
    victoryScore: el('victory-score'),
    victoryCopy: el('victory-copy'),
    victoryContinue: el('victory-continue-btn'),
    defeat: el('defeat-modal'),
    defeatRetry: el('defeat-retry-btn'),
    defeatAbort: el('defeat-abort-btn'),
    pause: el('pause-modal'),
    pauseSub: el('pause-sub'),
    pauseResume: el('pause-resume-btn'),
    pauseRestart: el('pause-restart-btn'),
    pauseChapt: el('pause-chapter-btn'),
    pauseAbort: el('pause-abort-btn'),
    objTitle: el('hud-obj-title'),
    objDetail: el('hud-obj-detail'),
    objProgress: el('hud-obj-progress'),
    sector: el('hud-sector'),
    healthBar: el('hud-health-bar'),
    healthText: el('hud-health-text'),
    ammo: el('hud-ammo-display'),
    ammoWarning: el('ammo-warning'),
    toast: el('pickup-toast'),
    bossBar: el('boss-bar'),
    bossFill: el('boss-bar-fill'),
    bossName: el('boss-name'),
    score: el('hud-score'),
    timer: el('hud-timer'),
    compassArrow: el('compass-arrow'),
    compassDistance: el('compass-distance'),
    crosshair: document.querySelector('.crosshair'),
    crosshairLabel: el('crosshair-label'),
    hitmarker: el('hitmarker'),
    damageFlash: el('damage-flash'),
    lowHealth: el('low-health'),
    scanner: el('scanner-overlay'),
    reloadPanel: el('reload-panel'),
    reloadBar: el('reload-bar-fill'),
    reloadLabel: el('reload-label'),
    interact: el('btn-interact'),
    fire: el('btn-fire'),
    jump: el('btn-jump'),
    scan: el('btn-scan'),
    reload: el('btn-reload'),
    pauseBtn: el('btn-pause'),
    joystickZone: el('joystick-zone'),
    joystickKnob: el('joystick-knob'),
    aimZone: el('aim-touch-zone'),
    bootChapter: el('boot-chapter'),
    bootTitle: el('boot-title'),
    bootSector: el('boot-sector')
  };

  if (CONFIG.highContrast) document.body.classList.add('high-contrast');

  // -------------------------------------------------------------- AUDIO SYNTHESIS
  var audioCtx = null;
  var masterGain = null;

  function audioOut() {
    if (!audioCtx) {
      var Ctx = window.AudioContext || window.webkitAudioContext;
      if (!Ctx) return null;
      try { audioCtx = new Ctx(); } catch (err) { return null; }
    }
    if (audioCtx.state === 'suspended') { audioCtx.resume(); }
    if (!masterGain) {
      masterGain = audioCtx.createGain();
      // Section 15.2: effects sit under the master bus so the player's ceiling is honoured.
      masterGain.gain.value = Math.max(0, Math.min(1, CONFIG.sfxVolume * CONFIG.masterVolume));
      masterGain.connect(audioCtx.destination);
    }
    return masterGain;
  }

  function tone(type, fromHz, toHz, duration, level, delay) {
    var out = audioOut();
    if (!out) return;
    var ctx = audioCtx;
    var start = ctx.currentTime + (delay || 0);
    var osc = ctx.createOscillator();
    var gain = ctx.createGain();
    osc.type = type;
    osc.frequency.setValueAtTime(fromHz, start);
    osc.frequency.exponentialRampToValueAtTime(Math.max(20, toHz), start + duration);
    gain.gain.setValueAtTime(level, start);
    gain.gain.exponentialRampToValueAtTime(0.001, start + duration);
    osc.connect(gain);
    gain.connect(out);
    osc.start(start);
    osc.stop(start + duration + 0.02);
  }

  function noiseBurst(duration, level, lowpassHz) {
    var out = audioOut();
    if (!out) return;
    var ctx = audioCtx;
    var start = ctx.currentTime;
    var frames = Math.floor(ctx.sampleRate * duration);
    var buffer = ctx.createBuffer(1, frames, ctx.sampleRate);
    var data = buffer.getChannelData(0);
    for (var i = 0; i < frames; i++) data[i] = Math.random() * 2 - 1;
    var source = ctx.createBufferSource();
    source.buffer = buffer;
    var gain = ctx.createGain();
    gain.gain.setValueAtTime(level, start);
    gain.gain.exponentialRampToValueAtTime(0.001, start + duration);
    if (lowpassHz) {
      var filter = ctx.createBiquadFilter();
      filter.type = 'lowpass';
      filter.frequency.value = lowpassHz;
      source.connect(filter);
      filter.connect(gain);
    } else {
      source.connect(gain);
    }
    gain.connect(out);
    source.start(start);
  }

  function playSound(name) {
    switch (name) {
      case 'gunshot':
        tone('sawtooth', 150, 32, 0.12, 0.5);
        noiseBurst(0.09, 0.55);
        break;
      case 'hit':
        tone('triangle', 420, 130, 0.08, 0.32);
        break;
      case 'explosion':
        noiseBurst(0.38, 0.7, 1800);
        tone('sawtooth', 90, 26, 0.32, 0.4);
        break;
      case 'droneShot':
        tone('square', 780, 220, 0.12, 0.18);
        break;
      case 'hurt':
        tone('sawtooth', 220, 70, 0.22, 0.32);
        break;
      case 'reload':
        tone('square', 180, 120, 0.06, 0.22);
        tone('square', 130, 90, 0.07, 0.2, 0.9);
        break;
      case 'empty':
        tone('square', 90, 70, 0.05, 0.18);
        break;
      case 'scan':
        tone('sine', 620, 1500, 0.24, 0.24);
        break;
      case 'objective':
        tone('sine', 523, 523, 0.16, 0.22);
        tone('sine', 659, 659, 0.16, 0.22, 0.09);
        tone('sine', 784, 784, 0.26, 0.22, 0.18);
        break;
      case 'resupply':
        tone('sine', 720, 1080, 0.1, 0.2);
        tone('sine', 1080, 1440, 0.14, 0.16, 0.08);
        break;
      case 'alert':
        tone('square', 520, 880, 0.2, 0.16);
        break;
      case 'victory':
        tone('sine', 523, 523, 0.22, 0.25);
        tone('sine', 659, 659, 0.22, 0.25, 0.12);
        tone('sine', 784, 784, 0.22, 0.25, 0.24);
        tone('sine', 1046, 1046, 0.4, 0.25, 0.36);
        break;
      case 'defeat':
        tone('sawtooth', 300, 60, 0.9, 0.3);
        break;
      default:
        break;
    }
  }

  // ------------------------------------------------------------------ RENDERING
  var scene = new THREE.Scene();
  var camera = new THREE.PerspectiveCamera(62, window.innerWidth / window.innerHeight, 0.1, 1000);
  var renderer = new THREE.WebGLRenderer({ antialias: QUALITY.antialias, powerPreference: 'high-performance' });
  renderer.setSize(window.innerWidth, window.innerHeight);
  renderer.setPixelRatio(Math.min(window.devicePixelRatio || 1, 2) * QUALITY.resolutionScale);
  renderer.shadowMap.enabled = QUALITY.shadowMap;
  renderer.shadowMap.type = THREE.PCFSoftShadowMap;
  renderer.outputEncoding = THREE.sRGBEncoding;
  renderer.toneMapping = THREE.ACESFilmicToneMapping;
  renderer.toneMappingExposure = 1.05;
  ui.container.appendChild(renderer.domElement);

  // Soft contact shadow so rigs sit on the ground instead of floating over it.
  var contactShadowGeo = new THREE.CircleGeometry(1, 18);
  function addContactShadow(group, radius, y) {
    var mesh = new THREE.Mesh(contactShadowGeo, new THREE.MeshBasicMaterial({
      color: 0x0A0C0E, transparent: true, opacity: 0.28, depthWrite: false
    }));
    mesh.rotation.x = -Math.PI / 2;
    mesh.scale.setScalar(radius);
    mesh.position.y = y === undefined ? 0.05 : y;
    mesh.renderOrder = -1;
    group.add(mesh);
    return mesh;
  }

  var ENV = LIB.buildEnvironment(LEVEL.theme, scene);
  var colliders = ENV.colliders;
  var bounds = ENV.bounds;
  var M = ENV.materials;

  // ------------------------------------------------------------- WAYPOINT BEACON
  var waypoint = { group: new THREE.Group(), beam: null, ring: null, active: false };
  (function buildWaypoint() {
    var beam = new THREE.Mesh(
      new THREE.CylinderGeometry(0.5, 0.5, 16, 10, 1, true),
      new THREE.MeshBasicMaterial({ color: 0x38D39F, transparent: true, opacity: 0.16, side: THREE.DoubleSide })
    );
    beam.position.y = 8;
    waypoint.beam = beam;
    waypoint.group.add(beam);

    var ring = new THREE.Mesh(
      new THREE.RingGeometry(1.7, 2.2, 30),
      new THREE.MeshBasicMaterial({ color: 0x38D39F, side: THREE.DoubleSide, transparent: true, opacity: 0.85 })
    );
    ring.rotation.x = -Math.PI / 2;
    ring.position.y = 0.12;
    waypoint.ring = ring;
    waypoint.group.add(ring);

    waypoint.group.visible = false;
    scene.add(waypoint.group);
  })();

  function setWaypoint(pos) {
    if (!pos) {
      waypoint.active = false;
      waypoint.group.visible = false;
      return;
    }
    waypoint.group.position.set(pos.x, 0, pos.z);
    waypoint.active = true;
    waypoint.group.visible = true;
  }

  // --------------------------------------------------------------- PLAYER RIG
  var player = {
    group: new THREE.Group(),
    pos: new THREE.Vector3(LEVEL.spawn[0], LEVEL.spawn[1], LEVEL.spawn[2]),
    rotY: 0,
    speed: 10.5 * Math.max(0.6, Math.min(1.4, CONFIG.sensitivity)),
    maxHealth: 100,
    health: 100,
    magSize: 30,
    magAmmo: 30,
    reserveAmmo: LEVEL.reserveAmmo || 120,
    isReloading: false,
    reloadTimer: 0,
    reloadDuration: 1.7,
    isGrounded: true,
    vy: 0,
    animTime: 0,
    alive: true,
    lastDamageAt: -999,
    lastShotAt: -999,
    recoil: 0,
    // Barrel elevation in radians. Positive is up; drives both the reticle and
    // the camera, so the crosshair is always where the round goes.
    aimPitch: 0.02,
    torso: null,
    head: null,
    leftLeg: null,
    rightLeg: null,
    leftArm: null,
    rightArm: null,
    rifle: null,
    muzzleLight: null
  };

  (function buildPlayerRig() {
    var matGraphite = new THREE.MeshStandardMaterial({ color: 0x20262C, roughness: 0.6, metalness: 0.3 });
    var matArmor = new THREE.MeshStandardMaterial({ color: 0x667353, roughness: 0.72 });
    var matVisor = new THREE.MeshBasicMaterial({ color: 0xE7A83E });
    var matSteel = new THREE.MeshStandardMaterial({ color: 0x81919E, metalness: 0.8, roughness: 0.3 });

    player.torso = new THREE.Mesh(new THREE.BoxGeometry(1.1, 1.4, 0.7), matArmor);
    player.torso.position.y = 1.6;
    player.torso.castShadow = true;
    player.group.add(player.torso);

    // Plate carrier
    var plate = new THREE.Mesh(new THREE.BoxGeometry(1.16, 0.9, 0.78), matGraphite);
    plate.position.set(0, 0.05, 0);
    player.torso.add(plate);

    player.head = new THREE.Mesh(new THREE.BoxGeometry(0.7, 0.7, 0.75), matGraphite);
    player.head.position.set(0, 1.1, 0);
    player.head.castShadow = true;
    var visor = new THREE.Mesh(new THREE.BoxGeometry(0.55, 0.2, 0.1), matVisor);
    visor.position.set(0, 0.05, 0.38);
    player.head.add(visor);
    var antenna = new THREE.Mesh(new THREE.CylinderGeometry(0.03, 0.03, 0.4), matSteel);
    antenna.position.set(0.28, 0.42, -0.1);
    player.head.add(antenna);
    player.torso.add(player.head);

    var legGeo = new THREE.BoxGeometry(0.4, 1.2, 0.45);
    player.leftLeg = new THREE.Mesh(legGeo, matGraphite);
    player.leftLeg.position.set(-0.35, -1.0, 0);
    player.leftLeg.castShadow = true;
    player.torso.add(player.leftLeg);

    player.rightLeg = new THREE.Mesh(legGeo, matGraphite);
    player.rightLeg.position.set(0.35, -1.0, 0);
    player.rightLeg.castShadow = true;
    player.torso.add(player.rightLeg);

    var armGeo = new THREE.BoxGeometry(0.32, 1.1, 0.32);
    player.rightArm = new THREE.Mesh(armGeo, matArmor);
    player.rightArm.position.set(0.75, 0.3, 0.1);
    player.rightArm.rotation.x = -Math.PI / 4;
    player.torso.add(player.rightArm);

    player.leftArm = new THREE.Mesh(armGeo, matArmor);
    player.leftArm.position.set(-0.75, 0.3, 0.1);
    player.leftArm.rotation.x = -Math.PI / 5;
    player.torso.add(player.leftArm);

    var rifle = new THREE.Group();
    var rifleBody = new THREE.Mesh(new THREE.BoxGeometry(0.2, 0.35, 1.4), matGraphite);
    rifle.add(rifleBody);
    var barrel = new THREE.Mesh(new THREE.CylinderGeometry(0.06, 0.06, 0.7), matSteel);
    barrel.rotation.x = Math.PI / 2;
    barrel.position.set(0, 0.08, 0.8);
    rifle.add(barrel);
    var magazine = new THREE.Mesh(new THREE.BoxGeometry(0.16, 0.42, 0.22), matSteel);
    magazine.position.set(0, -0.28, 0.15);
    rifle.add(magazine);
    rifle.position.set(-0.15, -0.4, 0.5);
    player.rightArm.add(rifle);
    player.rifle = rifle;

    player.muzzleLight = new THREE.PointLight(0xFFE082, 0, 12);
    player.muzzleLight.position.set(0, 0, 1.25);
    rifle.add(player.muzzleLight);

    player.group.position.copy(player.pos);
    scene.add(player.group);
    addContactShadow(player.group, 0.62, 0.03);
  })();

  // ------------------------------------------------------------------- HOSTILES
  // Three archetypes share the same targeting and damage pipeline:
  //   scout    hovering quad-rotor that strafes and takes pot shots (default)
  //   sentinel static ground turret with a tracking head and heavy bursts
  //   walker   slow bipedal heavy that closes to short range and hits hard
  var drones = [];
  var rangeTargets = [];

  var HOSTILE_KINDS = {
    scout: {
      hp: 3, patrol: 5, alertRadius: 30, fireRate: 2.6, damage: 9, score: 250,
      speed: 3.4, boltSpeed: 26, aimError: 0.05, hover: true, aimOffset: 0, hitRadius: 1.9
    },
    sentinel: {
      hp: 8, patrol: 0, alertRadius: 30, fireRate: 1.6, damage: 10, score: 400,
      speed: 0, boltSpeed: 34, aimError: 0.02, hover: false, aimOffset: 2.6, hitRadius: 2.2
    },
    walker: {
      hp: 14, patrol: 4, alertRadius: 40, fireRate: 3.4, damage: 16, score: 600,
      speed: 2.1, boltSpeed: 22, aimError: 0.035, hover: false, aimOffset: 2.4, hitRadius: 2.8
    },
    // Chapter 7 finale. Deliberately a ground frame so it can be cornered in the
    // deck geometry instead of hovering out of reach.
    boss: {
      hp: 60, patrol: 0, alertRadius: 70, fireRate: 1.15, damage: 20, score: 3000,
      speed: 1.7, boltSpeed: 24, aimError: 0.03, hover: false, aimOffset: 3.4, hitRadius: 3.6
    }
  };

  // Health fractions that trigger a boss summon, consumed in order.
  var BOSS_SUMMONS = [0.66, 0.33];

  // Stacked hit spheres, offsets relative to the hostile's centre of mass and
  // ordered head-first so a head hit wins over the body sphere behind it.
  HOSTILE_KINDS.scout.hit = [
    { part: 'head', dy: 0.5, r: 0.55, mult: 2 },
    { part: 'body', dy: 0, r: 1.0, mult: 1 }
  ];
  HOSTILE_KINDS.sentinel.hit = [
    { part: 'head', dy: 1.0, r: 0.55, mult: 2 },
    { part: 'body', dy: 0, r: 1.15, mult: 1 },
    { part: 'legs', dy: -1.5, r: 0.7, mult: 0.5 }
  ];
  HOSTILE_KINDS.walker.hit = [
    { part: 'head', dy: 1.2, r: 0.62, mult: 2 },
    { part: 'body', dy: 0, r: 1.5, mult: 1 },
    { part: 'legs', dy: -1.4, r: 0.9, mult: 0.5 }
  ];
  HOSTILE_KINDS.boss.hit = [
    { part: 'head', dy: 1.6, r: 1.0, mult: 2 },
    { part: 'body', dy: 0, r: 2.7, mult: 1 },
    { part: 'legs', dy: -2.2, r: 1.4, mult: 0.5 }
  ];

  // Shortest-path angle damping so a turret head does not spin the long way round.
  function turnToward(current, target, step) {
    var delta = ((target - current + Math.PI) % (Math.PI * 2) + Math.PI * 2) % (Math.PI * 2) - Math.PI;
    return current + Math.max(-step, Math.min(step, delta));
  }

  // Centre of mass used for aiming and hit detection, so tall ground frames are
  // not shot in the feet.
  function hostilePoint(drone) {
    return new THREE.Vector3(
      drone.group.position.x,
      drone.group.position.y + drone.aimOffset,
      drone.group.position.z
    );
  }

  // -------------------------------------------------------------------- AIM RAY
  // One direction, one ray, and the truth about what it touches. Everything the
  // player sees (crosshair state) and everything the shot does (damage) is
  // derived from this single function, so the reticle can never lie.
  var aimVec = new THREE.Vector3();
  var aimOrigin = new THREE.Vector3();

  function aimDirection(out) {
    var pitch = player.aimPitch;
    var flat = Math.cos(pitch);
    var v = out || new THREE.Vector3();
    v.set(Math.sin(player.rotY) * flat, Math.sin(pitch), Math.cos(player.rotY) * flat);
    return v.normalize();
  }

  function chestPoint(out) {
    aimOrigin.set(player.pos.x, player.pos.y + 1.55, player.pos.z);
    return out ? out.copy(aimOrigin) : aimOrigin;
  }

  // Nearest positive ray/sphere intersection distance, or -1 when missed.
  function raySphere(origin, dir, cx, cy, cz, radius) {
    var ox = origin.x - cx;
    var oy = origin.y - cy;
    var oz = origin.z - cz;
    var b = ox * dir.x + oy * dir.y + oz * dir.z;
    var c = ox * ox + oy * oy + oz * oz - radius * radius;
    if (c > 0 && b > 0) return -1;
    var disc = b * b - c;
    if (disc < 0) return -1;
    var t = -b - Math.sqrt(disc);
    return t < 0 ? 0 : t;
  }

  // Samples the level colliders along the shot so cover is worth using. Stops at
  // the first solid hit and returns the distance to it.
  function coverDistance(origin, dir, limit) {
    var step = 1.2;
    var t = step;
    while (t < limit) {
      var x = origin.x + dir.x * t;
      var y = origin.y + dir.y * t;
      var z = origin.z + dir.z * t;
      if (y <= 0 || blockedAt(x, z, 0.05)) return t;
      t += step;
    }
    return limit;
  }

  // Resolves the ray against every alive hostile and returns the nearest part hit.
  function resolveAimRay(origin, dir, maxDist) {
    var best = null;
    for (var i = 0; i < drones.length; i++) {
      var drone = drones[i];
      if (!drone.alive) continue;
      var cx = drone.group.position.x;
      var cy = drone.group.position.y + drone.aimOffset;
      var cz = drone.group.position.z;
      var boxes = drone.hit || HOSTILE_KINDS[drone.kind].hit;
      for (var b = 0; b < boxes.length; b++) {
        var box = boxes[b];
        var t = raySphere(origin, dir, cx, cy + box.dy, cz, box.r);
        if (t < 0 || t > maxDist) continue;
        if (!best || t < best.distance) {
          best = {
            drone: drone,
            part: box.part,
            mult: box.mult,
            distance: t,
            point: new THREE.Vector3(
              origin.x + dir.x * t,
              origin.y + dir.y * t,
              origin.z + dir.z * t
            )
          };
        }
      }
    }
    return best;
  }

  function buildScoutRig() {
    var group = new THREE.Group();

    var body = new THREE.Mesh(new THREE.BoxGeometry(1.6, 0.5, 1.6), M.dark);
    body.castShadow = true;
    group.add(body);

    var lens = new THREE.Mesh(new THREE.SphereGeometry(0.3, 12, 12), M.red);
    lens.position.set(0, -0.2, 0.6);
    group.add(lens);

    var rotors = [];
    var offsets = [[0.9, 0.3, 0.9], [-0.9, 0.3, 0.9], [0.9, 0.3, -0.9], [-0.9, 0.3, -0.9]];
    for (var i = 0; i < offsets.length; i++) {
      var arm = new THREE.Mesh(new THREE.CylinderGeometry(0.06, 0.06, 0.8), M.metal);
      arm.rotation.z = Math.PI / 2;
      arm.position.set(offsets[i][0] / 2, 0, offsets[i][2] / 2);
      group.add(arm);

      var blade = new THREE.Mesh(new THREE.BoxGeometry(0.9, 0.04, 0.15), M.cyan);
      blade.position.set(offsets[i][0], offsets[i][1], offsets[i][2]);
      group.add(blade);
      rotors.push(blade);
    }

    return { group: group, rotors: rotors, head: null, legs: null };
  }

  function buildSentinelRig() {
    var group = new THREE.Group();

    var base = new THREE.Mesh(new THREE.CylinderGeometry(1.1, 1.35, 0.5, 8), M.dark);
    base.position.y = 0.25;
    base.castShadow = true;
    base.receiveShadow = true;
    group.add(base);

    var column = new THREE.Mesh(new THREE.CylinderGeometry(0.42, 0.52, 2.1, 8), M.metal);
    column.position.y = 1.5;
    column.castShadow = true;
    group.add(column);

    var head = new THREE.Group();
    head.position.y = 2.75;
    var housing = new THREE.Mesh(new THREE.BoxGeometry(1.5, 0.85, 1.4), M.dark);
    housing.castShadow = true;
    head.add(housing);
    var barrel = new THREE.Mesh(new THREE.CylinderGeometry(0.14, 0.14, 1.1), M.metal);
    barrel.rotation.x = Math.PI / 2;
    barrel.position.set(0, 0, 0.85);
    head.add(barrel);
    var eye = new THREE.Mesh(new THREE.SphereGeometry(0.2, 10, 10), M.red);
    eye.position.set(0, 0.12, 0.72);
    head.add(eye);
    group.add(head);

    return { group: group, rotors: [], head: head, legs: null };
  }

  function buildWalkerRig() {
    var group = new THREE.Group();

    var torso = new THREE.Mesh(new THREE.BoxGeometry(2.0, 1.7, 1.6), M.dark);
    torso.position.y = 2.6;
    torso.castShadow = true;
    group.add(torso);

    var plate = new THREE.Mesh(new THREE.BoxGeometry(2.1, 0.5, 1.7), M.olive);
    plate.position.y = 0.15;
    torso.add(plate);

    var eye = new THREE.Mesh(new THREE.BoxGeometry(0.7, 0.22, 0.14), M.red);
    eye.position.set(0, 0.25, 0.82);
    torso.add(eye);

    var cannon = new THREE.Mesh(new THREE.CylinderGeometry(0.16, 0.16, 1.3), M.metal);
    cannon.rotation.x = Math.PI / 2;
    cannon.position.set(0.72, -0.1, 0.95);
    torso.add(cannon);

    var legGeo = new THREE.BoxGeometry(0.5, 1.9, 0.55);
    var legs = [];
    for (var side = -1; side <= 1; side += 2) {
      var leg = new THREE.Mesh(legGeo, M.metal);
      leg.position.set(side * 0.6, 1.05, 0);
      leg.castShadow = true;
      group.add(leg);
      legs.push(leg);
    }

    return { group: group, rotors: [], head: null, legs: legs };
  }

  function buildBossRig() {
    var group = new THREE.Group();

    // Command frame: a walker chassis scaled up with a reactor core and two
    // shoulder pods, so it reads as the architect's machine at a glance.
    var torso = new THREE.Mesh(new THREE.BoxGeometry(3.4, 2.6, 2.4), M.dark);
    torso.position.y = 4.2;
    torso.castShadow = true;
    group.add(torso);

    var core = new THREE.Mesh(new THREE.SphereGeometry(0.62, 16, 16), M.amber);
    core.position.set(0, 4.3, 1.15);
    group.add(core);

    var crest = new THREE.Mesh(new THREE.BoxGeometry(2.2, 0.35, 0.5), M.olive);
    crest.position.set(0, 5.55, 0.5);
    group.add(crest);

    var visor = new THREE.Mesh(new THREE.BoxGeometry(1.5, 0.28, 0.16), M.red);
    visor.position.set(0, 5.0, 1.22);
    group.add(visor);

    for (var side = -1; side <= 1; side += 2) {
      var pod = new THREE.Mesh(new THREE.BoxGeometry(1.1, 1.0, 1.6), M.metal);
      pod.position.set(side * 2.2, 4.5, 0.2);
      pod.castShadow = true;
      group.add(pod);

      var barrel = new THREE.Mesh(new THREE.CylinderGeometry(0.2, 0.2, 1.7), M.metal);
      barrel.rotation.x = Math.PI / 2;
      barrel.position.set(side * 2.2, 4.5, 1.3);
      group.add(barrel);
    }

    var legs = [];
    for (var legSide = -1; legSide <= 1; legSide += 2) {
      var thigh = new THREE.Mesh(new THREE.BoxGeometry(0.85, 2.1, 0.9), M.metal);
      thigh.position.set(legSide * 1.05, 2.35, 0);
      thigh.castShadow = true;
      group.add(thigh);
      legs.push(thigh);

      var shin = new THREE.Mesh(new THREE.BoxGeometry(1.05, 1.5, 1.15), M.dark);
      shin.position.set(legSide * 1.05, 0.75, 0);
      shin.castShadow = true;
      group.add(shin);
    }

    return { group: group, rotors: [], head: null, legs: legs };
  }

  function spawnDrone(def, squad) {
    var kind = HOSTILE_KINDS[def.kind] ? def.kind : 'scout';
    var tuning = HOSTILE_KINDS[kind];
    // Wave spawns are a little tougher and much more alert than their sentries.
    var wave = !!def.wave;
    var rig = kind === 'sentinel' ? buildSentinelRig()
      : kind === 'walker' ? buildWalkerRig()
      : kind === 'boss' ? buildBossRig()
      : buildScoutRig();

    var baseY = tuning.hover ? def.y : 0;
    rig.group.position.set(def.x, baseY, def.z);
    scene.add(rig.group);
    if (!tuning.hover) {
      addContactShadow(rig.group, kind === 'boss' ? 2.6 : (kind === 'walker' ? 1.5 : 1.0), 0.06);
    }

    var maxHp = def.hp || (wave && kind === 'scout' ? 4 : tuning.hp);
    drones.push({
      kind: kind,
      group: rig.group,
      rotors: rig.rotors,
      head: rig.head,
      legs: rig.legs,
      hp: maxHp,
      maxHp: maxHp,
      home: new THREE.Vector3(def.x, baseY, def.z),
      baseY: baseY,
      patrol: def.patrol === undefined ? (wave ? 3 : tuning.patrol) : def.patrol,
      alertRadius: def.alertRadius || (wave ? 42 : tuning.alertRadius),
      fireRate: def.fireRate || (wave ? 2.4 : tuning.fireRate),
      damage: def.damage || tuning.damage,
      score: def.score || tuning.score,
      speed: def.speed || tuning.speed,
      boltSpeed: tuning.boltSpeed,
      aimError: tuning.aimError,
      aimOffset: tuning.aimOffset,
      hitRadius: tuning.hitRadius,
      hitFlash: 0,
      walkPhase: 0,
      isBoss: kind === 'boss',
      label: def.label || (kind === 'boss' ? 'ARCHITECT COMMAND FRAME' : ''),
      summonIndex: 0,
      squad: squad || def.squad || 'generic',
      time: Math.random() * 10,
      fireCooldown: 1.4 + Math.random() * 1.6,
      state: 'patrol',
      alive: true
    });
  }

  for (var di = 0; di < (LEVEL.drones || []).length; di++) {
    spawnDrone(LEVEL.drones[di], LEVEL.drones[di].squad);
  }

  var levelTargets = (ENV.extras && ENV.extras.targets) || [];
  for (var ti = 0; ti < levelTargets.length; ti++) {
    rangeTargets.push({ group: levelTargets[ti], active: true });
  }

  // -------------------------------------------------------------- PROJECTILES
  // Player fire is hitscan (see fireShot); only hostile bolts travel.
  var enemyBolts = [];
  var enemyBoltGeo = new THREE.SphereGeometry(0.22, 8, 8);
  var enemyBoltMat = new THREE.MeshBasicMaterial({ color: 0xFF5A4A });

  // ------------------------------------------------------------------ PARTICLES
  var particles = [];
  var sparkMat = new THREE.MeshBasicMaterial({ color: 0xFFC107 });
  var smokeMat = new THREE.MeshBasicMaterial({ color: 0x81919E, transparent: true, opacity: 0.7 });
  var extractMat = new THREE.MeshBasicMaterial({ color: 0x38D39F, transparent: true, opacity: 0.8 });

  function spawnSparks(position, count) {
    var total = Math.max(1, Math.round((count || 8) * QUALITY.particleScale));
    for (var i = 0; i < total; i++) {
      var spark = new THREE.Mesh(new THREE.BoxGeometry(0.12, 0.12, 0.12), sparkMat);
      spark.position.copy(position);
      var vel = new THREE.Vector3(
        (Math.random() - 0.5) * 8,
        Math.random() * 6 + 2,
        (Math.random() - 0.5) * 8
      );
      scene.add(spark);
      particles.push({ mesh: spark, vel: vel, life: 0.4 });
    }
  }

  function spawnExtractionSmoke(position) {
    for (var i = 0; i < 22; i++) {
      var puff = new THREE.Mesh(new THREE.SphereGeometry(0.6 + Math.random() * 0.4, 8, 8), extractMat);
      puff.position.set(
        position.x + (Math.random() - 0.5) * 2,
        position.y,
        position.z + (Math.random() - 0.5) * 2
      );
      var vel = new THREE.Vector3((Math.random() - 0.5) * 1.5, Math.random() * 3 + 1, (Math.random() - 0.5) * 1.5);
      scene.add(puff);
      particles.push({ mesh: puff, vel: vel, life: 2.4 });
    }
  }

  // ------------------------------------------------------------- SUPPLY CACHES
  // Field resupply caches: ammo crates top the reserve pool back up, medkits
  // restore vitality. Both are one-shot and worth points, so sweeping a route
  // pays for the detour. Positions come from LEVEL.supplies.
  var supplies = [];
  var supplyCrateMat = new THREE.MeshStandardMaterial({ color: 0x2A3138, roughness: 0.72, metalness: 0.35 });

  (function buildSupplies() {
    var defs = LEVEL.supplies || [];
    for (var i = 0; i < defs.length; i++) {
      var def = defs[i];
      var tint = def.type === 'ammo' ? 0xE7A83E : 0x38D39F;
      var group = new THREE.Group();
      group.position.set(def.x, 0, def.z);

      var crate = new THREE.Mesh(new THREE.BoxGeometry(1.3, 0.9, 1.3), supplyCrateMat);
      crate.position.y = 0.55;
      crate.castShadow = true;
      crate.receiveShadow = true;
      group.add(crate);

      var band = new THREE.Mesh(
        new THREE.BoxGeometry(1.4, 0.16, 1.4),
        new THREE.MeshBasicMaterial({ color: tint })
      );
      band.position.y = 0.62;
      group.add(band);

      var ring = new THREE.Mesh(
        new THREE.RingGeometry(1.25, 1.55, 28),
        new THREE.MeshBasicMaterial({ color: tint, side: THREE.DoubleSide, transparent: true, opacity: 0.5 })
      );
      ring.rotation.x = -Math.PI / 2;
      ring.position.y = 0.06;
      group.add(ring);

      scene.add(group);
      supplies.push({ group: group, ring: ring, type: def.type, taken: false, time: i * 1.3 });
    }
  })();

  function collectSupply(cache) {
    cache.taken = true;
    cache.group.visible = false;
    mission.score += 100;

    if (cache.type === 'ammo') {
      var cap = (LEVEL.reserveAmmo || 120) * 2;
      var added = Math.min(cap - player.reserveAmmo, player.magSize * 2);
      player.reserveAmmo += Math.max(0, added);
      if (player.isReloading) {
        player.isReloading = false;
        player.reloadTimer = 0;
        updateReloadUI();
      }
      updateAmmoUI();
      caption(added > 0 ? '+' + added + ' ROUNDS' : 'AMMUNITION CACHE EMPTY', '#E7A83E');
    } else {
      var healed = Math.min(player.maxHealth - player.health, 40);
      player.health = Math.min(player.maxHealth, player.health + 40);
      updateHealthUI();
      caption(healed >= 1 ? '+' + Math.round(healed) + ' VITALITY' : 'VITALS ALREADY NOMINAL', '#38D39F');
    }

    spawnSparks(cache.group.position, 10);
    playSound('resupply');
    haptic(45);
  }

  function updateSupplies(dt) {
    if (!player.alive || mission.over) return;
    for (var i = 0; i < supplies.length; i++) {
      var cache = supplies[i];
      if (cache.taken) continue;
      cache.time += dt;
      cache.group.position.y = Math.sin(cache.time * 2.2) * 0.12;
      cache.ring.rotation.z += dt * 1.4;
      cache.ring.material.opacity = 0.4 + (Math.sin(cache.time * 3) + 1) * 0.16;
      var dx = player.pos.x - cache.group.position.x;
      var dz = player.pos.z - cache.group.position.z;
      if (dx * dx + dz * dz < 3.6) collectSupply(cache);
    }
  }

  // ------------------------------------------------------------- MISSION STATE
  var mission = {
    index: 0,
    score: 800,
    elapsed: 0,
    paused: false,
    over: false,
    choiceMade: null,
    killed: {},
    wave: 0,
    waveCooldown: 0,
    dialogueOpen: false,
    choiceOpen: false,
    interactKey: '',
    lockKey: '',
    bossShown: false,
    finalScore: 0,
    finalElapsed: 0,
    finalHealth: 100,
    finalStars: 0
  };

  function uiBlocking() {
    return mission.dialogueOpen || mission.choiceOpen || mission.paused || mission.over;
  }

  function currentObjective() {
    return LEVEL.objectives[mission.index] || null;
  }

  function objVector(obj) {
    return new THREE.Vector3(obj.pos[0], obj.pos[1], obj.pos[2]);
  }

  function countAlive(squad) {
    var alive = 0;
    for (var i = 0; i < drones.length; i++) {
      if (drones[i].alive && drones[i].squad === squad) alive += 1;
    }
    return alive;
  }

  function pad2(value) {
    return value < 10 ? '0' + value : '' + value;
  }

  function pad4(value) {
    var text = '' + value;
    while (text.length < 4) text = '0' + text;
    return text;
  }

  function formatTime(seconds) {
    var whole = Math.max(0, Math.floor(seconds));
    return pad2(Math.floor(whole / 60)) + ':' + pad2(whole % 60);
  }

  function nowSeconds() {
    return performance.now() / 1000;
  }

  function blockedAt(x, z, radius) {
    if (x < bounds.minX + radius || x > bounds.maxX - radius) return true;
    if (z < bounds.minZ + radius || z > bounds.maxZ - radius) return true;
    for (var i = 0; i < colliders.length; i++) {
      var c = colliders[i];
      if (x > c.minX - radius && x < c.maxX + radius && z > c.minZ - radius && z < c.maxZ + radius) {
        return true;
      }
    }
    return false;
  }

  // ------------------------------------------------------------------ HUD SYNC
  function setText(node, text) {
    if (node && node.textContent !== text) node.textContent = text;
  }

  function updateAmmoUI() {
    setText(ui.ammo, player.magAmmo + ' / ' + player.reserveAmmo);
    if (player.magAmmo === 0) ui.ammo.classList.add('empty');
    else ui.ammo.classList.remove('empty');
    var showWarning = player.magAmmo === 0 && !player.isReloading && player.alive && !mission.over;
    ui.ammoWarning.style.display = showWarning ? 'block' : 'none';
  }

  function updateHealthUI() {
    var pct = Math.max(0, Math.round(player.health));
    ui.healthBar.style.width = pct + '%';
    if (pct <= 30) ui.healthBar.classList.add('low');
    else ui.healthBar.classList.remove('low');
    setText(ui.healthText, pct + '% VITALITY');
    if (pct <= 30 && player.alive && !mission.over) ui.lowHealth.classList.add('active');
    else ui.lowHealth.classList.remove('active');
  }

  function objectiveProgressText() {
    var obj = currentObjective();
    if (!obj) return 'ALL OBJECTIVES COMPLETE';
    if (obj.kind === 'destroy') {
      var killed = Math.min(obj.required, mission.killed[obj.squad] || 0);
      // Boss objectives read as a hull percentage rather than a kill count.
      var bossObjective = false;
      for (var s = 0; obj.spawn && s < obj.spawn.length; s++) {
        if (obj.spawn[s].kind === 'boss') bossObjective = true;
      }
      if (bossObjective) {
        var frame = null;
        for (var b = 0; b < drones.length; b++) {
          if (drones[b].isBoss && drones[b].alive) { frame = drones[b]; break; }
        }
        if (!frame) return killed >= obj.required ? 'ARCHITECT DOWN' : 'TARGET LOST';
        return 'COMMAND FRAME ' + Math.round((frame.hp / frame.maxHp) * 100) + '%';
      }
      return 'HOSTILES ' + killed + ' / ' + obj.required;
    }
    if (obj.kind === 'defend') {
      if (mission.waveCooldown > 0) return 'WAVE ' + Math.min(obj.waves.length, mission.wave + 2) + ' INBOUND';
      return 'WAVE ' + (mission.wave + 1) + ' / ' + obj.waves.length;
    }
    return 'OBJECTIVE ' + (mission.index + 1) + ' / ' + LEVEL.objectives.length;
  }

  function updateHUD() {
    var obj = currentObjective();
    if (obj) {
      setText(ui.objTitle, obj.title);
      setText(ui.objDetail, obj.detail || '');
    }
    setText(ui.objProgress, objectiveProgressText());
    setText(ui.sector, 'CH' + pad2(LEVEL.chapter) + ' // ' + LEVEL.sector);
    setText(ui.score, 'SCORE ' + pad4(mission.score));
    setText(ui.timer, formatTime(mission.elapsed));
    updateAmmoUI();
    updateHealthUI();
  }

  function updateReloadUI() {
    if (player.isReloading) {
      ui.reloadPanel.classList.add('active');
      var pct = Math.min(100, (player.reloadTimer / player.reloadDuration) * 100);
      ui.reloadBar.style.width = pct + '%';
    } else {
      ui.reloadPanel.classList.remove('active');
    }
  }

  function showHitmarker(kill) {
    ui.hitmarker.classList.remove('show');
    void ui.hitmarker.offsetWidth;
    if (kill) ui.hitmarker.classList.add('kill');
    else ui.hitmarker.classList.remove('kill');
    ui.hitmarker.classList.add('show');
  }

  function flashDamage() {
    ui.damageFlash.style.opacity = '0.95';
    setTimeout(function () { ui.damageFlash.style.opacity = '0'; }, 130);
  }

  // Inline confirmation for pickups and other silent state changes.
  var toastTimer = null;
  function showToast(text, color) {
    if (!ui.toast) return;
    ui.toast.textContent = text;
    ui.toast.style.color = color || '#F0E9DB';
    ui.toast.style.borderColor = color || '#35423C';
    ui.toast.classList.add('show');
    if (toastTimer) clearTimeout(toastTimer);
    toastTimer = setTimeout(function () { ui.toast.classList.remove('show'); }, 1500);
  }

  // Section 15.2: purely informational audio-cue captions respect the subtitle
  // preference, while warnings that carry tactical information are always shown.
  function caption(text, color) {
    if (!CONFIG.subtitles) return;
    showToast(text, color);
  }

  // Boss health strip. Only visible while a command frame is alive, and it is
  // driven by the same 200 ms HUD tick as the rest of the readouts.
  function updateBossBar() {
    if (!ui.bossBar) return;
    var boss = null;
    for (var i = 0; i < drones.length; i++) {
      if (drones[i].isBoss && drones[i].alive) { boss = drones[i]; break; }
    }
    if (!boss) {
      if (mission.bossShown) {
        mission.bossShown = false;
        ui.bossBar.classList.remove('show');
      }
      return;
    }
    if (!mission.bossShown) {
      mission.bossShown = true;
      ui.bossBar.classList.add('show');
    }
    if (ui.bossName) ui.bossName.textContent = boss.label || 'ARCHITECT COMMAND FRAME';
    if (ui.bossFill) {
      var pct = Math.max(0, boss.hp / boss.maxHp) * 100;
      ui.bossFill.style.width = pct.toFixed(1) + '%';
    }
  }

  // ------------------------------------------------------------------- DIALOGUE
  function showDialogue(dialogue) {
    if (!dialogue) return;
    ui.dialogueSpeaker.textContent = dialogue.speaker;
    ui.dialogueText.textContent = dialogue.text;
    ui.dialogue.classList.add('show');
    mission.dialogueOpen = true;
    clearHeldInput();
    playSound('scan');
  }

  function closeDialogue() {
    ui.dialogue.classList.remove('show');
    mission.dialogueOpen = false;
    clearHeldInput();
  }

  // ---------------------------------------------------------- OBJECTIVE CONTROL
  function activateObjective() {
    var obj = currentObjective();
    if (!obj) {
      setWaypoint(null);
      return;
    }
    setWaypoint(objVector(obj));
    if (obj.auto && obj.dialogue) {
      // Delivered on arrival instead of on activation.
    }
    if (obj.kind === 'defend') {
      mission.wave = 0;
      mission.waveCooldown = 0;
      spawnWave(obj, 0);
      playSound('alert');
      haptic(70);
    }
    // Objectives can bring their own reinforcements, which is how the chapter 7
    // command frame arrives instead of standing on the deck from the start.
    if (obj.spawn) {
      for (var si = 0; si < obj.spawn.length; si++) {
        spawnDrone(obj.spawn[si], obj.squad);
      }
      playSound('alert');
      haptic(70);
    }
    mission.interactKey = '__refresh__';
    updateHUD();
  }

  function advanceObjective() {
    mission.index += 1;
    if (mission.index >= LEVEL.objectives.length) {
      triggerExtraction();
      return;
    }
    playSound('objective');
    activateObjective();
  }

  function spawnWave(obj, index) {
    var wave = obj.waves[index] || [];
    for (var i = 0; i < wave.length; i++) {
      var def = wave[i];
      spawnDrone({
        kind: def.kind,
        wave: true,
        x: def.x,
        y: def.y,
        z: def.z,
        hp: def.hp,
        patrol: def.patrol,
        alertRadius: def.alertRadius,
        damage: def.damage,
        fireRate: def.fireRate
      }, obj.squad);
    }
  }

  function updateWaves(dt) {
    var obj = currentObjective();
    if (!obj || obj.kind !== 'defend') return;
    if (mission.waveCooldown > 0) {
      mission.waveCooldown -= dt;
      if (mission.waveCooldown <= 0) {
        mission.wave += 1;
        spawnWave(obj, mission.wave);
        playSound('alert');
        haptic(60);
        updateHUD();
      }
    }
  }

  function checkObjectiveProgress() {
    var obj = currentObjective();
    if (!obj || mission.over) return;

    if (obj.kind === 'destroy') {
      if ((mission.killed[obj.squad] || 0) >= obj.required) {
        advanceObjective();
      }
    } else if (obj.kind === 'defend') {
      if (mission.waveCooldown > 0) return;
      if (countAlive(obj.squad) > 0) return;
      if (mission.wave < obj.waves.length - 1) {
        mission.waveCooldown = 3.2;
        playSound('alert');
        updateHUD();
      } else {
        advanceObjective();
      }
    } else if (obj.auto) {
      if (player.pos.distanceTo(objVector(obj)) <= (obj.radius || 6)) {
        if (obj.dialogue) showDialogue(obj.dialogue);
        advanceObjective();
      }
    }
  }

  // ----------------------------------------------------------------- INTERACTION
  function handleInteract(obj) {
    if (uiBlocking() || mission.over || !obj) return;
    playSound('scan');
    haptic(45);

    if (obj.kind === 'choice') {
      openChoice();
      return;
    }
    if (obj.kind === 'scan') triggerScanner();
    if (obj.dialogue) showDialogue(obj.dialogue);
    if (obj.kind === 'reach') {
      triggerExtraction();
      return;
    }
    advanceObjective();
  }

  function updateInteraction() {
    var obj = currentObjective();
    var show = false;
    if (!uiBlocking() && obj && !obj.auto && obj.kind !== 'destroy' && obj.kind !== 'defend') {
      if (player.pos.distanceTo(objVector(obj)) <= (obj.radius || 6)) show = true;
    }

    var key = show ? obj.label + '|' + mission.index : '';
    if (key !== mission.interactKey) {
      mission.interactKey = key;
      if (show) {
        ui.interact.style.display = 'block';
        ui.interact.textContent = obj.label || 'INTERACT';
        ui.interact.onclick = function () { handleInteract(obj); };
      } else {
        ui.interact.style.display = 'none';
        ui.interact.onclick = null;
      }
    }

    // The crosshair is driven by the same ray the shot uses, so what it reports
    // is exactly what a trigger pull would hit — cover included.
    var state = '';
    if (!uiBlocking() && player.alive) {
      var aimRay = aimDirection(aimVec);
      var rayFrom = chestPoint(new THREE.Vector3());
      var aimHit = resolveAimRay(rayFrom, aimRay, 110);
      if (aimHit) {
        var wallAt = coverDistance(rayFrom, aimRay, aimHit.distance);
        if (wallAt >= aimHit.distance - 0.4) {
          state = aimHit.part === 'head' ? 'head' : (aimHit.part === 'legs' ? 'legs' : 'body');
        }
      }
    }
    if (mission.lockKey !== state) {
      mission.lockKey = state;
      if (ui.crosshair) {
        ui.crosshair.classList.toggle('locked', state === 'body' || state === 'head' || state === 'legs');
        ui.crosshair.classList.toggle('crit', state === 'head');
        ui.crosshair.classList.remove('legs');
        if (state === 'legs') ui.crosshair.classList.add('legs');
      }
      if (ui.crosshairLabel) {
        ui.crosshairLabel.textContent = state === 'head' ? 'HEAD'
          : state === 'body' ? 'BODY'
          : state === 'legs' ? 'LEGS' : '';
      }
    }
  }

  // --------------------------------------------------------------------- CHOICE
  function openChoice() {
    if (!LEVEL.choice || mission.choiceOpen) return;
    mission.choiceOpen = true;
    ui.choiceTitle.textContent = LEVEL.choice.title;
    ui.choiceDesc.textContent = LEVEL.choice.description;
    ui.choiceOptions.textContent = '';
    for (var i = 0; i < LEVEL.choice.options.length; i++) {
      (function (opt) {
        var card = document.createElement('div');
        card.className = 'choice-card ' + (opt.tone || '');
        var tag = document.createElement('div');
        tag.className = 'choice-tag';
        tag.style.color = opt.tone === 'intel' ? '#E7A83E' : '#667353';
        tag.textContent = 'PROTOCOL ' + (opt.tone === 'intel' ? 'BRAVO' : 'ALPHA');
        var label = document.createElement('div');
        label.className = 'choice-label';
        label.textContent = opt.label;
        var summary = document.createElement('div');
        summary.className = 'choice-summary';
        summary.textContent = opt.summary;
        card.appendChild(tag);
        card.appendChild(label);
        card.appendChild(summary);
        card.onclick = function () { selectChoice(opt); };
        ui.choiceOptions.appendChild(card);
      })(LEVEL.choice.options[i]);
    }
    ui.choice.classList.add('show');
    clearHeldInput();
    playSound('alert');
    haptic(80);
  }

  function selectChoice(opt) {
    mission.choiceOpen = false;
    ui.choice.classList.remove('show');
    mission.choiceMade = opt.id;
    mission.score += opt.scoreBonus || 0;
    haptic(90);
    if (opt.dialogue) showDialogue(opt.dialogue);
    advanceObjective();
  }

  // ------------------------------------------------------------------- COMBAT
  // Shooting is hitscan: see fireShot() and resolveAimRay(). There is no target
  // acquisition step, so a round can only damage what the aim ray touches.

  function tryFire(now) {
    if (!player.alive || mission.over || mission.paused) return;
    if (player.isReloading) return;
    if (now - player.lastShotAt < 0.115) return;

    if (player.magAmmo <= 0) {
      playSound('empty');
      startReload();
      return;
    }

    player.lastShotAt = now;
    player.magAmmo -= 1;
    fireShot();
    updateAmmoUI();
  }

  // --------------------------------------------------------------- SHOT EFFECTS
  var tracers = [];
  var wrecks = [];
  var damageLayer = el('damage-layer');
  var tracerGeo = new THREE.BufferGeometry();

  function spawnTracer(from, to) {
    var points = tracerGeo.clone();
    points.setFromPoints([from.clone(), to.clone()]);
    var line = new THREE.Line(points, new THREE.LineBasicMaterial({
      color: 0xFFE082, transparent: true, opacity: 0.85
    }));
    scene.add(line);
    tracers.push({ line: line, life: 0 });
  }

  function updateTracers(dt) {
    for (var i = tracers.length - 1; i >= 0; i--) {
      var t = tracers[i];
      t.life += dt;
      t.line.material.opacity = Math.max(0, 0.85 - t.life * 14);
      if (t.life > 0.06) {
        scene.remove(t.line);
        t.line.geometry.dispose();
        t.line.material.dispose();
        tracers.splice(i, 1);
      }
    }
  }

  // Floating hit numbers are DOM so they stay crisp at any device pixel ratio.
  function spawnDamageNumber(point, amount, part) {
    if (!damageLayer) return;
    var projected = point.clone().project(camera);
    if (projected.z > 1) return;
    var node = document.createElement('div');
    node.className = 'damage-number' + (part === 'head' ? ' crit' : '');
    node.textContent = (part === 'head' ? 'HEAD ' : '') + amount;
    node.style.left = ((projected.x * 0.5 + 0.5) * window.innerWidth).toFixed(0) + 'px';
    node.style.top = ((-projected.y * 0.5 + 0.5) * window.innerHeight).toFixed(0) + 'px';
    damageLayer.appendChild(node);
    setTimeout(function () {
      if (node.parentNode) node.parentNode.removeChild(node);
    }, 760);
  }

  // Deaths tumble instead of blinking out, then get cleaned up.
  function updateWrecks(dt) {
    for (var i = wrecks.length - 1; i >= 0; i--) {
      var w = wrecks[i];
      w.life += dt;
      w.vy -= 13 * dt;
      w.group.position.y = Math.max(-2.6, w.group.position.y + w.vy * dt);
      w.group.rotation.x += w.spin * dt;
      w.group.rotation.z += w.spin * 0.45 * dt;
      if (w.life > 1.2) {
        scene.remove(w.group);
        wrecks.splice(i, 1);
      }
    }
  }

  // Hit flinch: a short scale punch on the body that was actually hit.
  function updateHostileFeedback(dt) {
    for (var i = 0; i < drones.length; i++) {
      var drone = drones[i];
      if (!drone.alive) continue;
      if (drone.hitFlash > 0) {
        drone.hitFlash = Math.max(0, drone.hitFlash - dt);
        drone.group.scale.setScalar(1 + drone.hitFlash * 0.75);
      } else if (drone.group.scale.x !== 1) {
        drone.group.scale.setScalar(1);
      }
    }
  }

  // Training boards on the firing range are shootable too.
  function resolveTargetRay(origin, dir, maxDist) {
    for (var i = 0; i < rangeTargets.length; i++) {
      var target = rangeTargets[i];
      if (!target.active) continue;
      var pos = target.group.position;
      var t = raySphere(origin, dir, pos.x, pos.y + 1.4, pos.z, 1.1);
      if (t >= 0 && t <= maxDist) {
        return {
          target: target,
          distance: t,
          point: new THREE.Vector3(
            origin.x + dir.x * t,
            origin.y + dir.y * t,
            origin.z + dir.z * t
          )
        };
      }
    }
    return null;
  }

  function fireShot() {
    playSound('gunshot');
    haptic(35);

    player.recoil = 0.3;
    player.rightArm.rotation.x += 0.2;
    player.muzzleLight.intensity = 3.6;
    setTimeout(function () { player.muzzleLight.intensity = 0; }, 45);

    // The shot is exactly the aim ray: no assist, no bending, no free damage.
    var dir = aimDirection(aimVec);
    var origin = chestPoint(new THREE.Vector3());
    var hit = resolveAimRay(origin, dir, 110);
    var board = resolveTargetRay(origin, dir, hit ? hit.distance : 60);
    var wall = coverDistance(origin, dir, hit ? hit.distance : (board ? board.distance : 60));
    var impact;

    if (board && wall >= board.distance - 0.4) {
      impact = board.point;
      board.target.active = false;
      board.target.group.rotation.x = -Math.PI / 2;
      spawnSparks(impact, 8);
      playSound('hit');
      spawnDamageNumber(impact, 150, 'board');
      mission.score += 150;
      updateHUD();
    } else if (hit && wall >= hit.distance - 0.4) {
      impact = hit.point;
      damageDrone(hit.drone, hit.point, 1, hit.part);
    } else {
      // Nothing worth hitting, or a wall got there first: the round lands on it.
      var reach = hit ? wall : Math.min(wall, 60);
      impact = new THREE.Vector3(
        origin.x + dir.x * reach,
        Math.max(0.06, origin.y + dir.y * reach),
        origin.z + dir.z * reach
      );
      spawnSparks(impact, 4);
    }

    var muzzle = new THREE.Vector3();
    player.rifle.getWorldPosition(muzzle);
    spawnTracer(muzzle, impact);
  }

  function startReload() {
    if (player.isReloading || player.reserveAmmo <= 0) return;
    if (player.magAmmo >= player.magSize) return;
    player.isReloading = true;
    player.reloadTimer = 0;
    playSound('reload');
    haptic(25);
    updateReloadUI();
    updateAmmoUI();
  }

  function updateReloadState(dt) {
    if (!player.isReloading) return;
    player.reloadTimer += dt;
    updateReloadUI();
    if (player.reloadTimer < player.reloadDuration) return;

    var missing = player.magSize - player.magAmmo;
    var take = Math.min(missing, player.reserveAmmo);
    player.magAmmo += take;
    player.reserveAmmo -= take;
    player.isReloading = false;
    updateReloadUI();
    updateAmmoUI();
  }

  function damageDrone(drone, point, amount, part) {
    var dealt = Math.max(1, Math.round((amount || 1) * (part === 'legs' ? 0.5 : part === 'head' ? 2 : 1)));
    drone.hp -= dealt;
    drone.hitFlash = 0.12;
    spawnSparks(point, part === 'head' ? 10 : 5);
    showHitmarker(false);
    playSound('hit');
    spawnDamageNumber(point, dealt, part);
    if (drone.hp > 0) return false;

    // Ground frames are tall: blow the wreck up at chest height, not at the feet.
    var wreck = new THREE.Vector3(
      drone.group.position.x,
      drone.group.position.y + drone.aimOffset,
      drone.group.position.z
    );
    drone.alive = false;
    // Let the rig fall instead of deleting it, so a kill is legible.
    wrecks.push({
      group: drone.group,
      vy: 0,
      spin: (Math.random() - 0.5) * 2.6,
      life: 0
    });
    spawnSparks(wreck, drone.isBoss ? 60 : (drone.kind === 'walker' ? 34 : 20));
    playSound('explosion');
    haptic(drone.kind === 'scout' ? 90 : 140);
    showHitmarker(true);
    if (drone.isBoss) {
      shake = Math.max(shake, 1.5);
      showToast('ARCHITECT DOWN', '#38D39F');
    }
    mission.score += drone.score;
    mission.killed[drone.squad] = (mission.killed[drone.squad] || 0) + 1;
    mission.damageDealt = (mission.damageDealt || 0) + dealt;
    updateHUD();
    return true;
  }

  function fireEnemyBolt(drone, lateral) {
    var origin = hostilePoint(drone);
    var targetPos = new THREE.Vector3(player.pos.x, player.pos.y + 1.3, player.pos.z);
    if (lateral) {
      // Spread a volley sideways along the aim plane instead of stacking bolts.
      var toTarget = targetPos.clone().sub(origin);
      var flat = Math.sqrt(toTarget.x * toTarget.x + toTarget.z * toTarget.z) || 1;
      targetPos.x += (-toTarget.z / flat) * lateral;
      targetPos.z += (toTarget.x / flat) * lateral;
    }
    var dir = targetPos.clone().sub(origin).normalize();
    dir.x += (Math.random() - 0.5) * drone.aimError;
    dir.y += (Math.random() - 0.5) * drone.aimError * 0.6;
    dir.z += (Math.random() - 0.5) * drone.aimError;
    dir.normalize();

    var mesh = new THREE.Mesh(enemyBoltGeo, enemyBoltMat);
    mesh.position.copy(origin);
    if (drone.kind === 'sentinel') mesh.scale.setScalar(1.6);
    if (drone.isBoss) mesh.scale.setScalar(1.3);
    scene.add(mesh);
    enemyBolts.push({
      mesh: mesh,
      dir: dir,
      life: 0,
      damage: drone.damage,
      speed: drone.boltSpeed
    });
    playSound('droneShot');
  }

  // Three-bolt volley: the command frame suppresses a corridor instead of
  // trading single shots, so standing still while it is facing you is fatal.
  function bossVolley(drone) {
    fireEnemyBolt(drone, -2.6);
    fireEnemyBolt(drone, 0);
    fireEnemyBolt(drone, 2.6);
  }

  // Each threshold spawns a two-drone escort flight. Escorts use their own squad
  // so the "apprehend the architect" counter only tracks the boss itself.
  function checkBossSummons(drone) {
    while (drone.summonIndex < BOSS_SUMMONS.length &&
           drone.hp / drone.maxHp <= BOSS_SUMMONS[drone.summonIndex]) {
      drone.summonIndex += 1;
      var base = drone.group.position;
      for (var i = 0; i < 2; i++) {
        var angle = Math.random() * Math.PI * 2;
        spawnDrone({
          x: base.x + Math.cos(angle) * 3.4,
          y: 6,
          z: base.z + Math.sin(angle) * 3.4,
          hp: 4,
          squad: 'escort',
          alertRadius: 55,
          damage: 9
        }, 'escort');
      }
      playSound('alert');
      haptic(120);
      showToast('ARCHITECT DEPLOYS ESCORTS', '#C84C4C');
    }
  }

  function damagePlayer(amount) {
    if (!player.alive || mission.over) return;
    player.health = Math.max(0, player.health - amount);
    player.lastDamageAt = nowSeconds();
    flashDamage();
    shake = Math.max(shake, 0.6);
    playSound('hurt');
    haptic(55);
    updateHealthUI();
    if (player.health <= 0) playerDown();
  }

  function playerDown() {
    player.alive = false;
    mission.over = true;
    clearHeldInput();
    playSound('defeat');
    haptic(320);
    ui.defeat.classList.add('show');
    updateHealthUI();
  }

  // ----------------------------------------------------------- EXTRACTION / END
  function triggerExtraction() {
    if (mission.over) return;
    mission.over = true;
    clearHeldInput();

    var ex = LEVEL.extraction || { pos: LEVEL.objectives[LEVEL.objectives.length - 1].pos, smoke: true };
    var pos = new THREE.Vector3(ex.pos[0], ex.pos[1], ex.pos[2]);
    if (ex.smoke) spawnExtractionSmoke(pos);
    setWaypoint(pos);
    playSound('victory');
    haptic(160);
    updateHUD();
    setTimeout(showVictory, 1400);
  }

  function showVictory() {
    var elapsed = Math.round(mission.elapsed);
    var healthPct = Math.max(0, Math.round(player.health));
    var stars = 1;
    if (healthPct >= 60) stars += 1;
    if (elapsed <= LEVEL.parSeconds) stars += 1;

    // The browser build keeps its own campaign progress; the native host owns
    // progression inside the app, so nothing is written there.
    if (!bridge) recordChapter(LEVEL.id, stars);

    var timeBonus = Math.max(0, LEVEL.parSeconds - elapsed) * 5;
    mission.score += timeBonus;
    mission.finalScore = Math.round(mission.score);
    mission.finalElapsed = elapsed;
    mission.finalHealth = healthPct;
    mission.finalStars = stars;

    var filled = '';
    for (var i = 0; i < 3; i++) filled += i < stars ? '\u2605' : '\u2606';
    ui.victoryStars.textContent = filled;
    ui.victoryTime.textContent = formatTime(elapsed);
    ui.victoryScore.textContent = '' + mission.finalScore;
    ui.victorySub.textContent = 'CHAPTER ' + pad2(LEVEL.chapter) + ' // ' + LEVEL.title;

    var copy = 'Objective secure, Spectre. ';
    if (mission.choiceMade && LEVEL.choice) {
      for (var c = 0; c < LEVEL.choice.options.length; c++) {
        if (LEVEL.choice.options[c].id === mission.choiceMade) {
          copy += 'Command logged your decision: ' + LEVEL.choice.options[c].label + '. ';
          break;
        }
      }
    } else {
      copy += 'All objectives complete. ';
    }
    copy += stars === 3 ? 'Clean run - no critical injuries and inside the operation window.'
      : stars === 2 ? 'Solid run. One performance criterion was missed.'
      : 'Mission complete, but the after-action board will have questions.';
    ui.victoryCopy.textContent = copy;
    ui.victory.classList.add('show');
    updateHUD();
  }

  var victoryFallback = false;

  function finishVictory() {
    var sent = notifyMissionComplete(mission.finalScore, mission.choiceMade, mission.finalElapsed, mission.finalHealth);
    if (sent) return;
    if (victoryFallback) {
      window.location.reload();
      return;
    }
    victoryFallback = true;
    ui.victoryCopy.textContent = 'No headquarters uplink is attached to this build, so the after-action report stays on the device. Restart the chapter to run it again.';
    ui.victoryContinue.textContent = 'RESTART CHAPTER';
  }

  function abortToHQ() {
    if (!notifyAbort()) window.location.reload();
  }

  function togglePause() {
    if (mission.over || deployOpen) return;
    mission.paused = !mission.paused;
    if (mission.paused) clearHeldInput();
    ui.pause.classList.toggle('show', mission.paused);
    playSound('scan');
  }

  // Native host hooks. Pause is idempotent so repeated back presses cannot
  // toggle the simulation back on, resume only clears an active pause.
  window.emberwatchPause = function () {
    if (mission.over || mission.paused) return;
    mission.paused = true;
    clearHeldInput();
    ui.pause.classList.add('show');
    playSound('scan');
  };

  // Called by the native host when the WebView finishes loading so a chapter
  // never starts paused after a configuration change.
  window.emberwatchResume = function () {
    if (mission.over || !mission.paused) return;
    mission.paused = false;
    ui.pause.classList.remove('show');
  };

  // -------------------------------------------------------------------- INPUT
  var touchCapable = 'ontouchstart' in window;
  var joystickInput = { x: 0, y: 0 };
  var joystickTouchId = null;
  var joystickMouse = false;
  var aimTouchId = null;
  var aimMouse = false;
  var lastAimX = 0;
  var lastAimY = 0;
  var firing = false;
  var keys = {};
  var shake = 0;
  var lastScanAt = -999;

  function handleJoystickPoint(clientX, clientY) {
    var rect = ui.joystickZone.getBoundingClientRect();
    var centerX = rect.left + rect.width / 2;
    var centerY = rect.top + rect.height / 2;
    var maxDist = rect.width / 2 - 18;
    var dx = clientX - centerX;
    var dy = clientY - centerY;
    var dist = Math.sqrt(dx * dx + dy * dy);
    if (dist > maxDist) {
      dx = (dx / dist) * maxDist;
      dy = (dy / dist) * maxDist;
    }
    ui.joystickKnob.style.transform = 'translate(' + dx + 'px,' + dy + 'px)';
    joystickInput.x = dx / maxDist;
    joystickInput.y = dy / maxDist;
  }

  function resetJoystick() {
    joystickTouchId = null;
    joystickMouse = false;
    joystickInput.x = 0;
    joystickInput.y = 0;
    ui.joystickKnob.style.transform = 'translate(0px,0px)';
  }

  // A dialogue, choice or pause screen can take over mid-gesture, so the release
  // event for a held trigger or a joystick drag never arrives. Every latched
  // input is dropped here, and callers use it whenever the sim stops accepting
  // input so a chapter never resumes running away and firing on its own.
  function clearHeldInput() {
    firing = false;
    aimMouse = false;
    aimTouchId = null;
    joystickMouse = false;
    keys = {};
    resetJoystick();
  }

  ui.joystickZone.addEventListener('touchstart', function (event) {
    if (uiBlocking()) return;
    event.preventDefault();
    var touch = event.changedTouches[0];
    joystickTouchId = touch.identifier;
    audioOut();
    handleJoystickPoint(touch.clientX, touch.clientY);
  }, { passive: false });

  ui.joystickZone.addEventListener('touchmove', function (event) {
    event.preventDefault();
    for (var i = 0; i < event.changedTouches.length; i++) {
      if (event.changedTouches[i].identifier === joystickTouchId) {
        handleJoystickPoint(event.changedTouches[i].clientX, event.changedTouches[i].clientY);
        break;
      }
    }
  }, { passive: false });

  ui.joystickZone.addEventListener('touchend', resetJoystick);
  ui.joystickZone.addEventListener('touchcancel', resetJoystick);

  ui.joystickZone.addEventListener('mousedown', function (event) {
    // Touch devices synthesize mouse events right after touchstart; ignore them.
    if (touchCapable || uiBlocking()) return;
    event.preventDefault();
    joystickMouse = true;
    handleJoystickPoint(event.clientX, event.clientY);
  });
  window.addEventListener('mousemove', function (event) {
    if (touchCapable || !joystickMouse) return;
    handleJoystickPoint(event.clientX, event.clientY);
  });
  window.addEventListener('mouseup', function () {
    if (joystickMouse) resetJoystick();
  });

  function applyAimDelta(dx, dy) {
    var sens = Math.max(0.5, Math.min(1.6, CONFIG.sensitivity));
    player.rotY -= dx * 0.008 * sens;
    player.aimPitch = Math.max(-0.48, Math.min(0.5, player.aimPitch - dy * 0.0024 * sens));
  }

  ui.aimZone.addEventListener('touchstart', function (event) {
    if (uiBlocking()) return;
    var touch = event.changedTouches[0];
    aimTouchId = touch.identifier;
    lastAimX = touch.clientX;
    lastAimY = touch.clientY;
    audioOut();
  });

  ui.aimZone.addEventListener('touchmove', function (event) {
    for (var i = 0; i < event.changedTouches.length; i++) {
      var touch = event.changedTouches[i];
      if (touch.identifier === aimTouchId) {
        applyAimDelta(touch.clientX - lastAimX, touch.clientY - lastAimY);
        lastAimX = touch.clientX;
        lastAimY = touch.clientY;
        break;
      }
    }
  });

  ui.aimZone.addEventListener('touchend', function () { aimTouchId = null; });
  ui.aimZone.addEventListener('touchcancel', function () { aimTouchId = null; });

  ui.aimZone.addEventListener('mousedown', function (event) {
    if (touchCapable || uiBlocking()) return;
    event.preventDefault();
    aimMouse = true;
    lastAimX = event.clientX;
    lastAimY = event.clientY;
  });
  window.addEventListener('mousemove', function (event) {
    if (touchCapable || !aimMouse) return;
    applyAimDelta(event.clientX - lastAimX, event.clientY - lastAimY);
    lastAimX = event.clientX;
    lastAimY = event.clientY;
  });
  window.addEventListener('mouseup', function () { aimMouse = false; });

  window.addEventListener('keydown', function (event) {
    var key = event.key.toLowerCase();
    // Modals and the pause screen only honour the pause toggle; holding a
    // movement key through a dialogue must not leak into the sim afterwards.
    if (uiBlocking() && key !== 'escape' && key !== 'p') return;
    keys[key] = true;
    if (key === ' ' || event.key === 'Spacebar') {
      event.preventDefault();
      jump();
    } else if (key === 'r') {
      startReload();
    } else if (key === 'e') {
      tryFire(nowSeconds());
    } else if (key === 'escape' || key === 'p') {
      togglePause();
    } else if (key === 'q') {
      player.rotY += 0.5;
    }
  });
  window.addEventListener('keyup', function (event) {
    keys[event.key.toLowerCase()] = false;
  });

  function jump() {
    if (!player.alive || mission.over || mission.paused) return;
    if (player.isGrounded) {
      player.vy = 8.5;
      player.isGrounded = false;
      haptic(25);
    }
  }

  function triggerScanner() {
    if (mission.over) return;
    var now = nowSeconds();
    if (now - lastScanAt < 1.1) return;
    lastScanAt = now;

    playSound('scan');
    haptic(60);
    ui.scanner.classList.add('scanner-active');
    setTimeout(function () { ui.scanner.classList.remove('scanner-active'); }, 1100);

    var ring = new THREE.Mesh(
      new THREE.RingGeometry(1, 1.8, 32),
      new THREE.MeshBasicMaterial({ color: 0x38D39F, side: THREE.DoubleSide, transparent: true, opacity: 0.85 })
    );
    ring.rotation.x = -Math.PI / 2;
    ring.position.set(player.pos.x, 0.4, player.pos.z);
    scene.add(ring);

    var scale = 1;
    var interval = setInterval(function () {
      scale += 1.7;
      ring.scale.set(scale, scale, scale);
      if (scale > 26) {
        clearInterval(interval);
        scene.remove(ring);
        ring.geometry.dispose();
        ring.material.dispose();
      }
    }, 24);
  }

  var lastPressAt = -999;

  function bindPress(node, handler) {
    node.addEventListener('touchstart', function (event) {
      event.preventDefault();
      lastPressAt = performance.now();
      audioOut();
      handler();
    }, { passive: false });
    node.addEventListener('click', function () {
      // Suppress the synthetic click that follows a handled touchstart.
      if (performance.now() - lastPressAt < 500) return;
      handler();
    });
  }

  ui.fire.addEventListener('touchstart', function (event) {
    if (uiBlocking()) return;
    event.preventDefault();
    audioOut();
    firing = true;
  }, { passive: false });
  ui.fire.addEventListener('touchend', function (event) {
    event.preventDefault();
    firing = false;
  }, { passive: false });
  ui.fire.addEventListener('touchcancel', function () { firing = false; });
  ui.fire.addEventListener('mousedown', function (event) {
    if (uiBlocking()) return;
    event.preventDefault();
    firing = true;
  });
  window.addEventListener('mouseup', function () { firing = false; });
  ui.fire.addEventListener('touchmove', function (event) { event.preventDefault(); }, { passive: false });

  bindPress(ui.jump, jump);
  bindPress(ui.scan, triggerScanner);
  bindPress(ui.reload, startReload);
  bindPress(ui.pauseBtn, togglePause);
  bindPress(ui.pauseResume, togglePause);
  bindPress(ui.pauseRestart, function () { window.location.reload(); });
  bindPress(ui.pauseAbort, abortToHQ);
  bindPress(ui.defeatRetry, function () { window.location.reload(); });
  bindPress(ui.defeatAbort, abortToHQ);
  bindPress(ui.victoryContinue, finishVictory);
  bindPress(ui.dialogueNext, closeDialogue);

  // Chapter select only exists in the browser build; the native host owns
  // campaign navigation, so the button is not offered inside the app.
  if (ui.pauseChapt) {
    if (bridge) ui.pauseChapt.style.display = 'none';
    else bindPress(ui.pauseChapt, function () { openDeployScreen(true); });
  }

  document.addEventListener('visibilitychange', function () {
    if (document.hidden && !mission.paused && !mission.over) togglePause();
  });

  // Losing focus mid-gesture leaves the same latched inputs behind as a modal
  // does, so drop them here too even when the round stays unpaused.
  window.addEventListener('blur', clearHeldInput);

  // ------------------------------------------------------------- FRAME UPDATES
  function updatePlayer(dt) {
    var mx = joystickInput.x;
    var mz = joystickInput.y;
    if (keys['w'] || keys['arrowup']) mz = -1;
    if (keys['s'] || keys['arrowdown']) mz = 1;
    if (keys['a'] || keys['arrowleft']) mx = -1;
    if (keys['d'] || keys['arrowright']) mx = 1;

    var mag = Math.sqrt(mx * mx + mz * mz);
    var moving = mag > 0.12;

    if (moving) {
      var normX = mx / mag;
      var normZ = mz / mag;
      var forwardX = Math.sin(player.rotY);
      var forwardZ = Math.cos(player.rotY);
      // The rig is authored facing local +Z and the camera sits at local -Z, so
      // the screen-right vector is forward x up, i.e. (-cos, 0, +sin). Using the
      // raw local +X here mirrors strafing: A would walk right on screen.
      var rightX = -Math.cos(player.rotY);
      var rightZ = Math.sin(player.rotY);

      var dirX = forwardX * (-normZ) + rightX * normX;
      var dirZ = forwardZ * (-normZ) + rightZ * normX;
      var length = Math.sqrt(dirX * dirX + dirZ * dirZ) || 1;
      dirX /= length;
      dirZ /= length;

      var step = player.speed * Math.min(1, mag) * dt;
      var nextX = player.pos.x + dirX * step;
      var nextZ = player.pos.z + dirZ * step;

      if (!blockedAt(nextX, nextZ, 0.55)) {
        player.pos.x = nextX;
        player.pos.z = nextZ;
      } else if (!blockedAt(nextX, player.pos.z, 0.55)) {
        player.pos.x = nextX;
      } else if (!blockedAt(player.pos.x, nextZ, 0.55)) {
        player.pos.z = nextZ;
      }
      player.animTime += dt * 13;
    } else {
      player.animTime += dt * 3.5;
    }

    if (!player.isGrounded) {
      player.vy -= 24 * dt;
      player.pos.y += player.vy * dt;
      if (player.pos.y <= 0) {
        player.pos.y = 0;
        player.vy = 0;
        player.isGrounded = true;
      }
    }

    player.group.position.copy(player.pos);
    player.group.rotation.y = player.rotY;

    // The carbine follows the barrel elevation so aiming up reads on screen.
    if (player.rifle) {
      player.rifle.rotation.x = -player.aimPitch * 0.85 + player.recoil * 0.6;
    }
    if (player.rightArm) {
      player.rightArm.rotation.x = (-Math.PI / 4 - player.aimPitch * 0.7) + player.recoil;
    }
    player.recoil *= 0.82;

    if (moving) {
      player.leftLeg.rotation.x = Math.sin(player.animTime) * 0.75;
      player.rightLeg.rotation.x = -Math.sin(player.animTime) * 0.75;
      player.leftArm.rotation.x = -Math.PI / 5 + Math.sin(player.animTime) * 0.32;
      player.torso.position.y = 1.6 + Math.abs(Math.sin(player.animTime)) * 0.14;
    } else {
      player.leftLeg.rotation.x *= 0.86;
      player.rightLeg.rotation.x *= 0.86;
      player.leftArm.rotation.x += (-Math.PI / 5 - player.leftArm.rotation.x) * Math.min(1, dt * 8);
      player.torso.position.y = 1.6 + Math.sin(player.animTime) * 0.05;
    }

    var now = nowSeconds();
    if (player.alive && !mission.over && now - player.lastDamageAt > 8 && player.health < player.maxHealth) {
      player.health = Math.min(player.maxHealth, player.health + 3.5 * dt);
    }
  }

  function updateDrones(dt) {
    for (var i = 0; i < drones.length; i++) {
      var drone = drones[i];
      if (!drone.alive) continue;

      drone.time += dt;

      var dx = player.pos.x - drone.group.position.x;
      var dz = player.pos.z - drone.group.position.z;
      var dist = Math.sqrt(dx * dx + dz * dz) || 0.001;
      var engaged = player.alive && !mission.over && dist < drone.alertRadius;

      if (engaged && drone.state !== 'engage') {
        drone.state = 'engage';
        if (dist < 30) playSound('alert');
      } else if (!engaged) {
        drone.state = 'patrol';
      }

      if (drone.kind === 'sentinel') {
        // Emplacements hold the line: the head tracks the player and the gun
        // cycles faster than a scout, but nothing moves.
        if (drone.head) {
          var yaw = Math.atan2(dx, dz);
          drone.head.rotation.y = turnToward(drone.head.rotation.y, engaged ? yaw : 0, dt * 3.5);
        }
        if (engaged) {
          drone.fireCooldown -= dt;
          if (drone.fireCooldown <= 0) {
            drone.fireCooldown = drone.fireRate + Math.random() * 0.4;
            fireEnemyBolt(drone);
          }
        }
        continue;
      }

      if (drone.kind === 'boss') {
        // Command frame: keeps mid range, volleys in threes and calls escorts as
        // its plating fails. It never flees, so the last minute is a gunfight.
        var bossStep = 0;
        if (engaged && dist > 17) bossStep = drone.speed;
        else if (engaged && dist < 10) bossStep = -drone.speed * 0.6;
        if (bossStep !== 0) {
          var bnx = (dx / dist) * bossStep * dt;
          var bnz = (dz / dist) * bossStep * dt;
          var bx = drone.group.position.x + bnx;
          var bz = drone.group.position.z + bnz;
          if (!blockedAt(bx, bz, 1.7)) {
            drone.group.position.x = bx;
            drone.group.position.z = bz;
            drone.walkPhase += dt * 2.4;
          } else if (!blockedAt(bx, drone.group.position.z, 1.7)) {
            drone.group.position.x = bx;
          } else if (!blockedAt(drone.group.position.x, bz, 1.7)) {
            drone.group.position.z = bz;
          }
        }
        drone.group.rotation.y = Math.atan2(dx, dz);
        if (drone.legs) {
          var bossSwing = Math.sin(drone.walkPhase) * 0.28;
          drone.legs[0].rotation.x = bossSwing;
          drone.legs[1].rotation.x = -bossSwing;
        }
        checkBossSummons(drone);
        if (engaged && dist < 55) {
          drone.fireCooldown -= dt;
          if (drone.fireCooldown <= 0) {
            drone.fireCooldown = drone.fireRate + Math.random() * 0.35;
            bossVolley(drone);
          }
        }
        continue;
      }

      if (drone.kind === 'walker') {
        // Ground frame: lumbers forward until it is close enough to shell the
        // player, legs pumping so the gait reads at distance.
        if (engaged && dist > 8) {
          var wnx = dx / dist;
          var wnz = dz / dist;
          var stepX = drone.group.position.x + wnx * drone.speed * dt;
          var stepZ = drone.group.position.z + wnz * drone.speed * dt;
          if (!blockedAt(stepX, stepZ, 1.1)) {
            drone.group.position.x = stepX;
            drone.group.position.z = stepZ;
            drone.walkPhase += dt * 4.6;
          } else if (!blockedAt(stepX, drone.group.position.z, 1.1)) {
            drone.group.position.x = stepX;
            drone.walkPhase += dt * 4.6;
          } else if (!blockedAt(drone.group.position.x, stepZ, 1.1)) {
            drone.group.position.z = stepZ;
            drone.walkPhase += dt * 4.6;
          }
        }
        drone.group.rotation.y = Math.atan2(dx, dz);
        if (drone.legs) {
          var swing = Math.sin(drone.walkPhase) * 0.42;
          drone.legs[0].rotation.x = swing;
          drone.legs[1].rotation.x = -swing;
        }
        if (engaged && dist < 30) {
          drone.fireCooldown -= dt;
          if (drone.fireCooldown <= 0) {
            drone.fireCooldown = drone.fireRate + Math.random() * 0.6;
            fireEnemyBolt(drone);
          }
        }
        continue;
      }

      drone.group.position.y = drone.baseY + Math.sin(drone.time * 2.5) * 0.6;
      for (var r = 0; r < drone.rotors.length; r++) {
        drone.rotors[r].rotation.y += dt * 32;
      }

      if (engaged) {
        var nx = dx / dist;
        var nz = dz / dist;
        var approach = dist > 14 ? drone.speed : (dist < 8 ? -drone.speed * 0.65 : 0);
        var strafe = Math.sin(drone.time * 1.6) * 1.4;
        var moveX = (nx * approach + (-nz) * strafe) * dt;
        var moveZ = (nz * approach + nx * strafe) * dt;

        var nextX = drone.group.position.x + moveX;
        var nextZ = drone.group.position.z + moveZ;
        if (!blockedAt(nextX, nextZ, 1.0)) {
          drone.group.position.x = nextX;
          drone.group.position.z = nextZ;
        } else if (!blockedAt(nextX, drone.group.position.z, 1.0)) {
          drone.group.position.x = nextX;
        } else if (!blockedAt(drone.group.position.x, nextZ, 1.0)) {
          drone.group.position.z = nextZ;
        }

        drone.group.lookAt(player.pos.x, drone.group.position.y, player.pos.z);

        drone.fireCooldown -= dt;
        if (drone.fireCooldown <= 0 && dist < 34) {
          drone.fireCooldown = drone.fireRate + Math.random() * 0.7;
          fireEnemyBolt(drone);
        }
      } else {
        var angle = drone.time * 0.45 + drone.home.x * 0.2;
        var homeX = drone.home.x + Math.cos(angle) * drone.patrol;
        var homeZ = drone.home.z + Math.sin(angle) * drone.patrol;
        var homeDx = homeX - drone.group.position.x;
        var homeDz = homeZ - drone.group.position.z;
        var homeDist = Math.sqrt(homeDx * homeDx + homeDz * homeDz) || 0.001;
        var patrolX = drone.group.position.x + (homeDx / homeDist) * 2.4 * dt;
        var patrolZ = drone.group.position.z + (homeDz / homeDist) * 2.4 * dt;
        if (!blockedAt(patrolX, patrolZ, 0.9)) {
          drone.group.position.x = patrolX;
          drone.group.position.z = patrolZ;
        }
        drone.group.rotation.y += dt * 0.8;
      }
    }
  }

  function updateEnemyBolts(dt) {
    var chest = new THREE.Vector3();
    for (var i = enemyBolts.length - 1; i >= 0; i--) {
      var bolt = enemyBolts[i];
      bolt.mesh.position.addScaledVector(bolt.dir, (bolt.speed || 26) * dt);
      bolt.life += dt;
      chest.set(player.pos.x, player.pos.y + 1.3, player.pos.z);

      if (player.alive && !mission.over && bolt.mesh.position.distanceTo(chest) < 1.7) {
        damagePlayer(bolt.damage);
        scene.remove(bolt.mesh);
        enemyBolts.splice(i, 1);
        continue;
      }
      if (bolt.life > 3.2) {
        scene.remove(bolt.mesh);
        enemyBolts.splice(i, 1);
      }
    }
  }

  function updateParticles(dt) {
    for (var i = particles.length - 1; i >= 0; i--) {
      var particle = particles[i];
      particle.mesh.position.addScaledVector(particle.vel, dt);
      particle.life -= dt;
      if (particle.life <= 0) {
        scene.remove(particle.mesh);
        particles.splice(i, 1);
      }
    }
  }

  function updateWaypoint(dt) {
    if (!waypoint.active) return;
    waypoint.ring.rotation.z += dt * 1.6;
    waypoint.beam.material.opacity = 0.1 + (Math.sin(performance.now() * 0.004) + 1) * 0.06;
  }

  function updateEnvironment(dt) {
    var now = performance.now();
    for (var i = 0; i < ENV.rotators.length; i++) {
      var rotator = ENV.rotators[i];
      rotator.mesh.rotation[rotator.axis] += rotator.speed * dt;
    }
    for (var b = 0; b < ENV.beacons.length; b++) {
      var light = ENV.beacons[b];
      if (light.userData.baseIntensity === undefined) light.userData.baseIntensity = light.intensity;
      light.intensity = light.userData.baseIntensity * (0.72 + 0.28 * Math.sin(now * 0.003 + b * 1.7));
    }
  }

  function updateCompass() {
    if (!waypoint.active) return;
    var dx = waypoint.group.position.x - player.pos.x;
    var dz = waypoint.group.position.z - player.pos.z;
    var distance = Math.sqrt(dx * dx + dz * dz);
    var relative = Math.atan2(dx, dz) - player.rotY;
    ui.compassArrow.style.transform = 'rotate(' + relative + 'rad)';
    setText(ui.compassDistance, Math.round(distance) + 'm');
  }

  // Third-person boom that looks ALONG the aim direction, so the reticle drawn
  // at screen centre is where the shot actually goes.
  var camDesired = new THREE.Vector3();
  var camLook = new THREE.Vector3();
  var camFlat = new THREE.Vector3();

  function updateCamera(dt) {
    var chestY = player.pos.y + 2.05;
    var aim = aimDirection(aimVec);
    camFlat.set(aim.x, 0, aim.z);
    if (camFlat.lengthSq() < 0.0004) camFlat.set(Math.sin(player.rotY), 0, Math.cos(player.rotY));
    camFlat.normalize();

    camDesired.set(
      player.pos.x - camFlat.x * 8.6,
      chestY + 1.15 + player.aimPitch * 2.6,
      player.pos.z - camFlat.z * 8.6
    );
    camera.position.lerp(camDesired, Math.min(1, dt * 9));

    camLook.set(player.pos.x + aim.x * 60, chestY + aim.y * 60, player.pos.z + aim.z * 60);
    camera.lookAt(camLook);

    if (shake > 0) {
      camera.position.x += (Math.random() - 0.5) * shake;
      camera.position.y += (Math.random() - 0.5) * shake * 0.7;
      camera.position.z += (Math.random() - 0.5) * shake * 0.5;
      shake = Math.max(0, shake - dt * 2.2);
    }
  }

  // ----------------------------------------------------------------- MAIN LOOP
  var lastFrameTime = performance.now();
  var lastHudSync = 0;

  function frame() {
    requestAnimationFrame(frame);

    var nowMs = performance.now();
    var dt = Math.min((nowMs - lastFrameTime) / 1000, 0.05);
    lastFrameTime = nowMs;

    if (!uiBlocking()) {
      mission.elapsed += dt;
      updatePlayer(dt);
      updateDrones(dt);
      updateHostileFeedback(dt);
      updateTracers(dt);
      updateWrecks(dt);
      updateEnemyBolts(dt);
      updateParticles(dt);
      updateSupplies(dt);
      updateWaves(dt);
      checkObjectiveProgress();
      updateReloadState(dt);
      if (firing) tryFire(nowMs / 1000);
    }

    updateWaypoint(dt);
    updateEnvironment(dt);
    updateInteraction();
    updateCompass();
    updateCamera(dt);

    if (nowMs - lastHudSync > 200) {
      lastHudSync = nowMs;
      updateHUD();
      updateBossBar();
    }

    renderer.render(scene, camera);
  }

  window.addEventListener('resize', function () {
    camera.aspect = window.innerWidth / window.innerHeight;
    camera.updateProjectionMatrix();
    renderer.setSize(window.innerWidth, window.innerHeight);
  });

  // ------------------------------------------------- DEPLOYMENT / CHAPTER SELECT
  // The native host always supplies a mission id, so this screen only exists in
  // the browser build. Progress lives in localStorage keyed by mission id.
  var PROGRESS_KEY = 'emberwatch.progress.v1';
  var deployScreen = el('deploy-screen');
  var deployList = el('deploy-list');
  var deployProgress = el('deploy-progress');
  var deployStart = el('deploy-start-btn');
  var deployUnlock = el('deploy-unlock-btn');
  var deployResume = el('deploy-resume-btn');
  var deployHint = el('deploy-hint');
  var chapterOrder = [];
  var selectedChapterId = null;
  var deployOpen = false;
  var progress = loadProgress();

  function hasMissionParam() {
    try {
      return !!new URLSearchParams(window.location.search).get('mission');
    } catch (err) {
      return false;
    }
  }

  function loadProgress() {
    try {
      var raw = window.localStorage.getItem(PROGRESS_KEY);
      var parsed = raw ? JSON.parse(raw) : {};
      return parsed && typeof parsed === 'object' ? parsed : {};
    } catch (err) {
      return {};
    }
  }

  function saveProgress() {
    try {
      window.localStorage.setItem(PROGRESS_KEY, JSON.stringify(progress));
    } catch (err) { /* private mode: progress just will not persist */ }
  }

  function recordChapter(id, stars) {
    var best = Math.max(progress[id] || 0, stars);
    if (best === progress[id]) return;
    progress[id] = best;
    saveProgress();
  }

  function chapterList() {
    var list = [];
    for (var key in LIB.MISSIONS) {
      if (Object.prototype.hasOwnProperty.call(LIB.MISSIONS, key)) list.push(LIB.MISSIONS[key]);
    }
    list.sort(function (a, b) { return a.chapter - b.chapter; });
    return list;
  }

  function isChapterUnlocked(index) {
    if (index === 0) return true;
    var prev = chapterOrder[index - 1];
    return !!progress[prev.id];
  }

  function starGlyphs(stars) {
    var text = '';
    for (var i = 0; i < 3; i++) text += i < stars ? '\u2605' : '\u2606';
    return text;
  }

  function renderDeploy() {
    if (!deployList) return;
    chapterOrder = chapterList();
    deployList.textContent = '';

    var cleared = 0;
    for (var c = 0; c < chapterOrder.length; c++) {
      if (progress[chapterOrder[c].id]) cleared += 1;
    }
    if (deployProgress) deployProgress.textContent = cleared + ' / ' + chapterOrder.length + ' CLEARED';

    if (!selectedChapterId) {
      for (var s = 0; s < chapterOrder.length; s++) {
        if (isChapterUnlocked(s)) selectedChapterId = chapterOrder[s].id;
      }
    }

    for (var i = 0; i < chapterOrder.length; i++) {
      (function (missionData, index) {
        var unlocked = isChapterUnlocked(index);
        var stars = progress[missionData.id] || 0;
        var card = document.createElement('div');
        card.className = 'chapter-card' + (unlocked ? '' : ' locked') +
          (missionData.id === selectedChapterId ? ' selected' : '');
        card.setAttribute('data-mission', missionData.id);

        var num = document.createElement('div');
        num.className = 'chapter-num';
        num.textContent = 'CH' + pad2(missionData.chapter);

        var body = document.createElement('div');
        var title = document.createElement('div');
        title.className = 'chapter-title';
        title.textContent = missionData.title;
        var meta = document.createElement('div');
        meta.className = 'chapter-meta';
        var hostileCount = (missionData.drones || []).length;
        meta.textContent = String(missionData.theme || '').toUpperCase() + ' // ' +
          hostileCount + ' HOSTILES // PAR ' + formatTime(missionData.parSeconds || 0);
        body.appendChild(title);
        body.appendChild(meta);

        var state = document.createElement('div');
        state.className = 'chapter-state';
        if (!unlocked) {
          state.className += ' locked-label';
          state.textContent = 'LOCKED';
        } else if (stars > 0) {
          state.className += ' cleared';
          var glyphs = document.createElement('div');
          glyphs.className = 'chapter-stars';
          glyphs.textContent = starGlyphs(stars);
          var label = document.createElement('div');
          label.textContent = 'CLEARED';
          state.appendChild(glyphs);
          state.appendChild(label);
        } else {
          state.className += ' open-label';
          state.textContent = 'READY';
        }

        card.appendChild(num);
        card.appendChild(body);
        card.appendChild(state);
        card.addEventListener('click', function () {
          if (!unlocked) {
            if (deployHint) {
              deployHint.textContent = 'CH' + pad2(missionData.chapter) +
                ' is locked. Clear CH' + pad2(index) + ' (' + chapterOrder[index - 1].title + ') first.';
            }
            return;
          }
          selectedChapterId = missionData.id;
          renderDeploy();
        });
        card.addEventListener('dblclick', function () {
          if (unlocked) launchChapter(missionData.id);
        });
        deployList.appendChild(card);
      })(chapterOrder[i], i);
    }

    if (deployStart) {
      deployStart.textContent = 'BEGIN ' + (selectedChapterId
        ? 'CH' + pad2(LIB.MISSIONS[selectedChapterId].chapter)
        : 'CHAPTER');
    }
  }

  function launchChapter(id) {
    window.location.href = 'index.html?mission=' + encodeURIComponent(id);
  }

  function openDeployScreen(fromPause) {
    if (!deployScreen) return;
    mission.paused = true;
    ui.pause.classList.remove('show');
    clearHeldInput();
    deployOpen = true;
    if (deployResume) deployResume.hidden = !fromPause;
    if (deployHint) {
      deployHint.textContent = fromPause
        ? 'Choosing a chapter abandons this run and redeploys from the start.'
        : 'Select a chapter, then deploy. Progress is stored on this device.';
    }
    renderDeploy();
    deployScreen.classList.add('show');
  }

  function closeDeployScreen() {
    if (!deployScreen) return;
    deployOpen = false;
    deployScreen.classList.remove('show');
  }

  function resumeFromDeploy() {
    closeDeployScreen();
    clearHeldInput();
    mission.paused = false;
  }

  if (deployStart) {
    deployStart.addEventListener('click', function () {
      if (selectedChapterId) launchChapter(selectedChapterId);
    });
  }

  if (deployResume) {
    deployResume.addEventListener('click', resumeFromDeploy);
  }

  if (deployUnlock) {
    deployUnlock.addEventListener('click', function () {
      for (var i = 0; i < chapterOrder.length; i++) {
        if (!progress[chapterOrder[i].id]) progress[chapterOrder[i].id] = 1;
      }
      saveProgress();
      if (deployHint) deployHint.textContent = 'All chapters unlocked for review. Progress was recorded locally.';
      renderDeploy();
    });
  }

  // -------------------------------------------------------------------- BOOT
  ui.bootChapter.textContent = 'CHAPTER ' + pad2(LEVEL.chapter);
  ui.bootTitle.textContent = LEVEL.title;
  ui.bootSector.textContent = LEVEL.sector;
  ui.pauseSub.textContent = 'CHAPTER ' + pad2(LEVEL.chapter) + ' // ' + LEVEL.title;
  ui.victorySub.textContent = 'CHAPTER ' + pad2(LEVEL.chapter) + ' // AFTER-ACTION';

  camera.position.set(player.pos.x, 5.6, player.pos.z - 8.2);
  activateObjective();
  updateHUD();

  // No mission parameter and no native host means the player came in through
  // the campaign terminal: hold the sim and let them pick a chapter.
  if (!bridge && !hasMissionParam()) openDeployScreen(false);

  setTimeout(function () {
    if (LEVEL.startDialogue && !mission.over && !deployOpen) showDialogue(LEVEL.startDialogue);
  }, 2500);

  frame();
})();
