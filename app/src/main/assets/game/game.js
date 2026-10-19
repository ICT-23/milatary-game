// OPERATION: EMBERWATCH 3D — Complete Real-time 3D Game Engine
// Built with Three.js (WebGL), 60 FPS Procedural Rig Animation, and Web Audio

(function() {
  'use strict';

  // --- AUDIO SYNTHESIZER (100% Procedural Web Audio) ---
  const audioCtx = new (window.AudioContext || window.webkitAudioContext)();
  function playSound(type) {
    if (!audioCtx) return;
    if (audioCtx.state === 'suspended') audioCtx.resume();

    const t = audioCtx.currentTime;
    if (type === 'gunshot') {
      // Punchy military carbine crack
      const osc = audioCtx.createOscillator();
      const gain = audioCtx.createGain();
      osc.type = 'sawtooth';
      osc.frequency.setValueAtTime(150, t);
      osc.frequency.exponentialRampToValueAtTime(30, t + 0.12);
      gain.gain.setValueAtTime(0.7, t);
      gain.gain.exponentialRampToValueAtTime(0.01, t + 0.14);
      osc.connect(gain);
      gain.connect(audioCtx.destination);
      osc.start(t);
      osc.stop(t + 0.14);

      // Noise blast
      const bufferSize = audioCtx.sampleRate * 0.08;
      const buffer = audioCtx.createBuffer(1, bufferSize, audioCtx.sampleRate);
      const data = buffer.getChannelData(0);
      for (let i = 0; i < bufferSize; i++) data[i] = Math.random() * 2 - 1;
      const noise = audioCtx.createBufferSource();
      noise.buffer = buffer;
      const nGain = audioCtx.createGain();
      nGain.gain.setValueAtTime(0.8, t);
      nGain.gain.exponentialRampToValueAtTime(0.01, t + 0.09);
      noise.connect(nGain);
      nGain.connect(audioCtx.destination);
      noise.start(t);
    } else if (type === 'hit') {
      const osc = audioCtx.createOscillator();
      const gain = audioCtx.createGain();
      osc.type = 'triangle';
      osc.frequency.setValueAtTime(400, t);
      osc.frequency.exponentialRampToValueAtTime(120, t + 0.08);
      gain.gain.setValueAtTime(0.4, t);
      gain.gain.exponentialRampToValueAtTime(0.01, t + 0.08);
      osc.connect(gain);
      gain.connect(audioCtx.destination);
      osc.start(t);
      osc.stop(t + 0.08);
    } else if (type === 'explosion') {
      const bufferSize = audioCtx.sampleRate * 0.35;
      const buffer = audioCtx.createBuffer(1, bufferSize, audioCtx.sampleRate);
      const data = buffer.getChannelData(0);
      for (let i = 0; i < bufferSize; i++) data[i] = Math.random() * 2 - 1;
      const noise = audioCtx.createBufferSource();
      noise.buffer = buffer;
      const nGain = audioCtx.createGain();
      nGain.gain.setValueAtTime(1.0, t);
      nGain.gain.exponentialRampToValueAtTime(0.01, t + 0.35);
      noise.connect(nGain);
      nGain.connect(audioCtx.destination);
      noise.start(t);
    } else if (type === 'scan') {
      const osc = audioCtx.createOscillator();
      const gain = audioCtx.createGain();
      osc.type = 'sine';
      osc.frequency.setValueAtTime(600, t);
      osc.frequency.exponentialRampToValueAtTime(1400, t + 0.25);
      gain.gain.setValueAtTime(0.3, t);
      gain.gain.exponentialRampToValueAtTime(0.01, t + 0.25);
      osc.connect(gain);
      gain.connect(audioCtx.destination);
      osc.start(t);
      osc.stop(t + 0.25);
    } else if (type === 'objective') {
      [523.25, 659.25, 783.99].forEach((freq, idx) => {
        const osc = audioCtx.createOscillator();
        const gain = audioCtx.createGain();
        osc.frequency.value = freq;
        gain.gain.setValueAtTime(0.25, t + idx * 0.08);
        gain.gain.exponentialRampToValueAtTime(0.01, t + idx * 0.08 + 0.25);
        osc.connect(gain);
        gain.connect(audioCtx.destination);
        osc.start(t + idx * 0.08);
        osc.stop(t + idx * 0.08 + 0.25);
      });
    }
  }

  // --- NATIVE ANDROID BRIDGE ---
  function notifyHaptic(ms) {
    if (window.AndroidBridge && window.AndroidBridge.triggerHaptic) {
      window.AndroidBridge.triggerHaptic(ms);
    }
  }
  function notifyMissionComplete(score, choice, timeSec) {
    if (window.AndroidBridge && window.AndroidBridge.onMissionComplete) {
      window.AndroidBridge.onMissionComplete(score, choice, timeSec);
    }
  }

  // --- THREE.JS SCENE SETUP ---
  const container = document.getElementById('game-container');
  const scene = new THREE.Scene();
  scene.background = new THREE.Color(0x20262C);
  scene.fog = new THREE.FogExp2(0x20262C, 0.014);

  const camera = new THREE.PerspectiveCamera(62, window.innerWidth / window.innerHeight, 0.1, 1000);
  const renderer = new THREE.WebGLRenderer({ antialias: true, powerPreference: 'high-performance' });
  renderer.setSize(window.innerWidth, window.innerHeight);
  renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2));
  renderer.shadowMap.enabled = true;
  renderer.shadowMap.type = THREE.PCFSoftShadowMap;
  container.appendChild(renderer.domElement);

  // --- LIGHTING ---
  const hemiLight = new THREE.HemisphereLight(0x81919E, 0x354A40, 0.7);
  scene.add(hemiLight);

  const dirLight = new THREE.DirectionalLight(0xFFE8D0, 1.1);
  dirLight.position.set(40, 60, 30);
  dirLight.castShadow = true;
  dirLight.shadow.mapSize.width = 1024;
  dirLight.shadow.mapSize.height = 1024;
  dirLight.shadow.camera.near = 0.5;
  dirLight.shadow.camera.far = 150;
  const d = 50;
  dirLight.shadow.camera.left = -d;
  dirLight.shadow.camera.right = d;
  dirLight.shadow.camera.top = d;
  dirLight.shadow.camera.bottom = -d;
  scene.add(dirLight);

  // Red alert beacon at relay tower
  const towerBeacon = new THREE.PointLight(0xC84C4C, 2.0, 35);
  towerBeacon.position.set(0, 16, 25);
  scene.add(towerBeacon);

  // --- MATERIALS ---
  const matSand = new THREE.MeshLambertMaterial({ color: 0xD6C29A });
  const matConcrete = new THREE.MeshLambertMaterial({ color: 0x4C5862 });
  const matGraphite = new THREE.MeshStandardMaterial({ color: 0x20262C, roughness: 0.6, metalness: 0.3 });
  const matOlive = new THREE.MeshStandardMaterial({ color: 0x667353, roughness: 0.7 });
  const matAmberGlow = new THREE.MeshBasicMaterial({ color: 0xE7A83E });
  const matRedGlow = new THREE.MeshBasicMaterial({ color: 0xC84C4C });
  const matCyanGlow = new THREE.MeshBasicMaterial({ color: 0x38D39F });
  const matMetal = new THREE.MeshStandardMaterial({ color: 0x81919E, metalness: 0.8, roughness: 0.3 });

  // --- TERRAIN & ENVIRONMENT ---
  // Ground
  const groundGeo = new THREE.PlaneGeometry(240, 240, 32, 32);
  const ground = new THREE.Mesh(groundGeo, matSand);
  ground.rotation.x = -Math.PI / 2;
  ground.receiveShadow = true;
  scene.add(ground);

  // Ocean Water Plane (West side)
  const oceanGeo = new THREE.PlaneGeometry(160, 240);
  const oceanMat = new THREE.MeshStandardMaterial({ color: 0x1B3542, roughness: 0.2, metalness: 0.7 });
  const ocean = new THREE.Mesh(oceanGeo, oceanMat);
  ocean.rotation.x = -Math.PI / 2;
  ocean.position.set(-110, -0.4, 0);
  scene.add(ocean);

  // Concrete Runway / Patrol Road
  const roadGeo = new THREE.PlaneGeometry(14, 180);
  const road = new THREE.Mesh(roadGeo, matConcrete);
  road.rotation.x = -Math.PI / 2;
  road.position.set(0, 0.05, 0);
  road.receiveShadow = true;
  scene.add(road);

  // Obstacles & Building Arrays
  const colliders = [];

  function addBuilding(x, z, w, h, d, colorMat) {
    const geo = new THREE.BoxGeometry(w, h, d);
    const mesh = new THREE.Mesh(geo, colorMat || matGraphite);
    mesh.position.set(x, h / 2, z);
    mesh.castShadow = true;
    mesh.receiveShadow = true;
    scene.add(mesh);
    colliders.push({ minX: x - w/2 - 1.2, maxX: x + w/2 + 1.2, minZ: z - d/2 - 1.2, maxZ: z + d/2 + 1.2 });
    return mesh;
  }

  // 1. Command Outpost Bunker
  addBuilding(-22, -30, 20, 8, 16, matOlive);

  // 2. Auxiliary Generator Vault
  const genBuilding = addBuilding(24, 12, 14, 6, 12, matGraphite);

  // 3. Civilian Fallout Shelter
  addBuilding(32, 45, 16, 7, 20, matOlive);

  // 4. Perimeter Sandbags & Concrete Barricades
  for (let i = -30; i <= 30; i += 12) {
    if (Math.abs(i) > 6) {
      addBuilding(i, -12, 8, 2.5, 3, matConcrete);
    }
  }

  // 5. Tall 3D Telemetry Relay Tower Alpha
  const towerGroup = new THREE.Group();
  towerGroup.position.set(0, 0, 25);
  // Base truss legs
  const trussGeo = new THREE.CylinderGeometry(0.5, 1.2, 16, 4);
  const truss = new THREE.Mesh(trussGeo, matMetal);
  truss.position.y = 8;
  truss.castShadow = true;
  towerGroup.add(truss);
  // Rotating Dish
  const dishGeo = new THREE.CylinderGeometry(3.5, 0.4, 1.2, 16, 1, true);
  const dishMesh = new THREE.Mesh(dishGeo, matMetal);
  dishMesh.position.y = 17;
  dishMesh.rotation.x = Math.PI / 4;
  dishMesh.castShadow = true;
  towerGroup.add(dishMesh);
  scene.add(towerGroup);
  colliders.push({ minX: -4, maxX: 4, minZ: 21, maxZ: 29 });

  // 6. Military Supply Crates
  for (let c = 0; c < 12; c++) {
    const cx = (c % 2 === 0 ? -12 : 14) + (Math.sin(c) * 6);
    const cz = -25 + c * 5;
    addBuilding(cx, cz, 3, 3, 3, matOlive);
  }

  // 7. Pop-up Ballistic Shooting Range Targets
  const targets = [];
  for (let i = 0; i < 5; i++) {
    const tGroup = new THREE.Group();
    const targetBoard = new THREE.Mesh(new THREE.BoxGeometry(1.6, 2.8, 0.2), matRedGlow);
    targetBoard.position.y = 1.4;
    tGroup.add(targetBoard);
    tGroup.position.set(-18 + i * 8, 0, -45);
    scene.add(tGroup);
    targets.push({ group: tGroup, hp: 1, active: true, basePos: tGroup.position.clone() });
  }

  // 8. Extraction LZ Falcon Pad
  const lzGeo = new THREE.RingGeometry(5.5, 6.2, 32);
  const lzMesh = new THREE.Mesh(lzGeo, matCyanGlow);
  lzMesh.rotation.x = -Math.PI / 2;
  lzMesh.position.set(-25, 0.08, 45);
  scene.add(lzMesh);

  // --- PROCEDURAL 3D ANIMATED PLAYER RIG (Spectre-7) ---
  const player = {
    group: new THREE.Group(),
    pos: new THREE.Vector3(0, 0, -20),
    rotY: 0,
    speed: 10.5,
    health: 100,
    ammo: 30,
    isGrounded: true,
    vy: 0,
    animTime: 0,
    // Rig Joints
    torso: null,
    head: null,
    leftLeg: null,
    rightLeg: null,
    leftArm: null,
    rightArm: null,
    rifle: null,
    muzzleLight: null
  };

  // Build Player Mesh Rig
  (function buildPlayerRig() {
    // Torso
    const torsoGeo = new THREE.BoxGeometry(1.1, 1.4, 0.7);
    player.torso = new THREE.Mesh(torsoGeo, matOlive);
    player.torso.position.y = 1.6;
    player.torso.castShadow = true;
    player.group.add(player.torso);

    // Head + Helmet + Glowing Visor
    const headGeo = new THREE.BoxGeometry(0.7, 0.7, 0.75);
    player.head = new THREE.Mesh(headGeo, matGraphite);
    player.head.position.set(0, 1.1, 0);
    player.head.castShadow = true;
    const visorGeo = new THREE.BoxGeometry(0.55, 0.2, 0.1);
    const visor = new THREE.Mesh(visorGeo, matAmberGlow);
    visor.position.set(0, 0.05, 0.38);
    player.head.add(visor);
    player.torso.add(player.head);

    // Left Leg
    const legGeo = new THREE.BoxGeometry(0.4, 1.2, 0.45);
    player.leftLeg = new THREE.Mesh(legGeo, matGraphite);
    player.leftLeg.position.set(-0.35, -1.0, 0);
    player.leftLeg.castShadow = true;
    player.torso.add(player.leftLeg);

    // Right Leg
    player.rightLeg = new THREE.Mesh(legGeo, matGraphite);
    player.rightLeg.position.set(0.35, -1.0, 0);
    player.rightLeg.castShadow = true;
    player.torso.add(player.rightLeg);

    // Right Arm + Carbine Weapon
    const armGeo = new THREE.BoxGeometry(0.32, 1.1, 0.32);
    player.rightArm = new THREE.Mesh(armGeo, matOlive);
    player.rightArm.position.set(0.75, 0.3, 0.1);
    player.rightArm.rotation.x = -Math.PI / 4;
    player.torso.add(player.rightArm);

    // Left Arm
    player.leftArm = new THREE.Mesh(armGeo, matOlive);
    player.leftArm.position.set(-0.75, 0.3, 0.1);
    player.leftArm.rotation.x = -Math.PI / 5;
    player.torso.add(player.leftArm);

    // Military Carbine Rifle
    const rifleGroup = new THREE.Group();
    const bodyGeo = new THREE.BoxGeometry(0.2, 0.35, 1.4);
    const bodyMesh = new THREE.Mesh(bodyGeo, matGraphite);
    rifleGroup.add(bodyMesh);
    const barrelGeo = new THREE.CylinderGeometry(0.06, 0.06, 0.7);
    const barrelMesh = new THREE.Mesh(barrelGeo, matMetal);
    barrelMesh.rotation.x = Math.PI / 2;
    barrelMesh.position.set(0, 0.08, 0.8);
    rifleGroup.add(barrelMesh);
    rifleGroup.position.set(-0.15, -0.4, 0.5);
    player.rightArm.add(rifleGroup);
    player.rifle = rifleGroup;

    // Muzzle Flash Light
    player.muzzleLight = new THREE.PointLight(0xFFE082, 0, 10);
    player.muzzleLight.position.set(0, 0, 1.2);
    rifleGroup.add(player.muzzleLight);

    player.group.position.copy(player.pos);
    scene.add(player.group);
  })();

  // --- HOSTILE 3D RECON DRONES (AI SKIRMISH) ---
  const drones = [];
  function spawnDrone(x, y, z) {
    const droneGroup = new THREE.Group();
    droneGroup.position.set(x, y, z);

    // Core chassis
    const bodyMesh = new THREE.Mesh(new THREE.BoxGeometry(1.6, 0.5, 1.6), matGraphite);
    bodyMesh.castShadow = true;
    droneGroup.add(bodyMesh);

    // Center glowing lens
    const lens = new THREE.Mesh(new THREE.SphereGeometry(0.3, 12, 12), matRedGlow);
    lens.position.set(0, -0.2, 0.6);
    droneGroup.add(lens);

    // 4 Rotors
    const rotors = [];
    const rotorOffsets = [[0.9, 0.3, 0.9], [-0.9, 0.3, 0.9], [0.9, 0.3, -0.9], [-0.9, 0.3, -0.9]];
    rotorOffsets.forEach(pos => {
      const arm = new THREE.Mesh(new THREE.CylinderGeometry(0.06, 0.06, 0.8), matMetal);
      arm.rotation.z = Math.PI / 2;
      arm.position.set(pos[0]/2, 0, pos[2]/2);
      droneGroup.add(arm);

      const rBlade = new THREE.Mesh(new THREE.BoxGeometry(0.9, 0.04, 0.15), matCyanGlow);
      rBlade.position.set(pos[0], pos[1], pos[2]);
      droneGroup.add(rBlade);
      rotors.push(rBlade);
    });

    scene.add(droneGroup);
    drones.push({
      group: droneGroup,
      rotors: rotors,
      hp: 3,
      basePos: new THREE.Vector3(x, y, z),
      time: Math.random() * 10
    });
  }

  // Spawn 3 Recon Drones around Relay Tower Alpha
  spawnDrone(-8, 5.5, 18);
  spawnDrone(10, 6.0, 28);
  spawnDrone(0, 7.0, 36);

  // --- 3D BULLETS & TRACERS ---
  const bullets = [];
  const bulletGeo = new THREE.CylinderGeometry(0.08, 0.08, 0.8, 6);
  const bulletMat = new THREE.MeshBasicMaterial({ color: 0xFFEA8A });

  function fireBullet() {
    if (player.ammo <= 0) {
      player.ammo = 30; // Quick reload
      playSound('hit');
      updateHUD();
      return;
    }
    player.ammo--;
    playSound('gunshot');
    notifyHaptic(40);

    // Gun kick animation & muzzle flash
    player.rightArm.rotation.x += 0.25;
    player.muzzleLight.intensity = 3.5;
    setTimeout(() => { player.muzzleLight.intensity = 0; }, 45);

    // Create 3D Tracer projectile
    const bullet = new THREE.Mesh(bulletGeo, bulletMat);
    bullet.rotation.x = Math.PI / 2;

    // Calculate forward trajectory based on character facing + slight spread
    const forward = new THREE.Vector3(0, 0, 1).applyAxisAngle(new THREE.Vector3(0, 1, 0), player.rotY);
    bullet.position.set(
      player.pos.x + forward.x * 1.5,
      player.pos.y + 1.4,
      player.pos.z + forward.z * 1.5
    );
    bullet.rotation.y = player.rotY;

    scene.add(bullet);
    bullets.push({ mesh: bullet, dir: forward, life: 0 });
    updateHUD();
  }

  // --- 3D PARTICLES (Sparks, Smoke, Dust) ---
  const particles = [];
  const pMatSparks = new THREE.MeshBasicMaterial({ color: 0xFFC107 });
  const pMatSmoke = new THREE.MeshBasicMaterial({ color: 0x81919E, transparent: true, opacity: 0.7 });
  const pMatGreenSmoke = new THREE.MeshBasicMaterial({ color: 0x38D39F, transparent: true, opacity: 0.8 });

  function spawnSparks(pos, count = 8) {
    for (let i = 0; i < count; i++) {
      const p = new THREE.Mesh(new THREE.BoxGeometry(0.12, 0.12, 0.12), pMatSparks);
      p.position.copy(pos);
      const vel = new THREE.Vector3(
        (Math.random() - 0.5) * 8,
        Math.random() * 6 + 2,
        (Math.random() - 0.5) * 8
      );
      scene.add(p);
      particles.push({ mesh: p, vel: vel, life: 0.35 });
    }
  }

  function spawnGreenExtractionSmoke(pos) {
    for (let i = 0; i < 20; i++) {
      const p = new THREE.Mesh(new THREE.SphereGeometry(0.6 + Math.random() * 0.4, 8, 8), pMatGreenSmoke);
      p.position.set(pos.x + (Math.random()-0.5)*2, pos.y, pos.z + (Math.random()-0.5)*2);
      const vel = new THREE.Vector3((Math.random()-0.5)*1.5, Math.random()*3 + 1, (Math.random()-0.5)*1.5);
      scene.add(p);
      particles.push({ mesh: p, vel: vel, life: 2.5 });
    }
  }

  // --- MISSION STATE & OBJECTIVE TRACKER ---
  const MISSION = {
    step: 0,
    choiceMade: null,
    score: 1200,
    startTime: Date.now()
  };

  const OBJECTIVES = [
    { title: "REPORT TO TOMAS VALE", detail: "Navigate across the perimeter to forward recon liaison.", targetPos: new THREE.Vector3(0, 0, -2) },
    { title: "NEUTRALIZE SURVEILLANCE DRONES", detail: "Destroy 3 rogue telemetry scout drones near Relay Tower Alpha.", targetPos: new THREE.Vector3(0, 0, 25) },
    { title: "SCAN DAMAGED RELAY ARRAY", detail: "Use Field Scanner to inspect sabotaged transmitter frequency.", targetPos: new THREE.Vector3(0, 0, 25) },
    { title: "RESOLVE GENERATOR CRISIS", detail: "Decide whether to vent coolant to protect civilians or capture cipher drive.", targetPos: new THREE.Vector3(24, 0, 12) },
    { title: "EXTRACT AT LZ FALCON", detail: "Reach coastal extraction pad and signal chopper extraction.", targetPos: new THREE.Vector3(-25, 0, 45) },
    { title: "MISSION COMPLETE", detail: "Standby for after-action telemetry report.", targetPos: null }
  ];

  function updateHUD() {
    const cur = OBJECTIVES[MISSION.step] || OBJECTIVES[0];
    document.getElementById('hud-obj-title').textContent = cur.title;
    document.getElementById('hud-obj-detail').textContent = cur.detail;
    document.getElementById('hud-health-bar').style.width = player.health + '%';
    document.getElementById('hud-ammo-display').textContent = player.ammo + ' / 120';
  }

  // Interact Prompt Trigger
  const btnInteract = document.getElementById('btn-interact');
  function checkInteractionRadius() {
    let showBtn = false;
    let label = '';
    let action = null;

    if (MISSION.step === 0 && player.pos.distanceTo(new THREE.Vector3(0, 0, -2)) < 5) {
      showBtn = true;
      label = "TALK [TOMAS VALE]";
      action = () => {
        showDialogue("TOMAS VALE (RECON)", "Spectre! The communications array got hit by sabotaged pulse frequencies. Watch out—three autonomous recon drones are guarding Relay Tower Alpha ahead. Neutralize them and scan the tower!");
        MISSION.step = 1;
        playSound('objective');
        notifyHaptic(60);
        updateHUD();
      };
    } else if (MISSION.step === 2 && player.pos.distanceTo(new THREE.Vector3(0, 0, 25)) < 7) {
      showBtn = true;
      label = "SCAN [RELAY TOWER]";
      action = () => {
        triggerScanner();
        showDialogue("CAPTAIN MARA VOSS", "Spectre, your telemetry scanner detected intentional sabotage on Tower Alpha! The auxiliary generator is overheating southeast of your position. Get over there immediately!");
        MISSION.step = 3;
        playSound('objective');
        updateHUD();
      };
    } else if (MISSION.step === 3 && player.pos.distanceTo(new THREE.Vector3(24, 0, 12)) < 7) {
      showBtn = true;
      label = "OVERRIDE [GENERATOR]";
      action = () => {
        document.getElementById('choice-modal').style.display = 'block';
        playSound('scan');
      };
    } else if (MISSION.step === 4 && player.pos.distanceTo(new THREE.Vector3(-25, 0, 45)) < 7) {
      showBtn = true;
      label = "SIGNAL [EXTRACTION LZ]";
      action = () => {
        spawnGreenExtractionSmoke(new THREE.Vector3(-25, 0.5, 45));
        playSound('objective');
        notifyHaptic(150);
        MISSION.step = 5;
        updateHUD();
        setTimeout(() => {
          document.getElementById('victory-modal').style.display = 'block';
          const elapsed = Math.round((Date.now() - MISSION.startTime) / 1000);
          notifyMissionComplete(MISSION.score, MISSION.choiceMade || "SAFEGUARD_CIVILIANS", elapsed);
        }, 1200);
      };
    }

    if (showBtn) {
      btnInteract.style.display = 'block';
      btnInteract.textContent = label;
      btnInteract.onclick = action;
    } else {
      btnInteract.style.display = 'none';
    }
  }

  function showDialogue(speaker, text) {
    document.getElementById('dialogue-speaker').textContent = speaker;
    document.getElementById('dialogue-text').textContent = text;
    document.getElementById('dialogue-modal').style.display = 'block';
    playSound('scan');
  }

  document.getElementById('dialogue-next-btn').onclick = function() {
    document.getElementById('dialogue-modal').style.display = 'none';
  };

  // Choice Selection
  document.getElementById('choice-civilians-btn').onclick = function() {
    MISSION.choiceMade = "SAFEGUARD_CIVILIANS";
    MISSION.score += 800;
    document.getElementById('choice-modal').style.display = 'none';
    showDialogue("IDRIS KANE (MEDIC)", "Bless you, Spectre! You diverted the superheated coolant away from the fallout shelter. All 40 families are safe. Now proceed to LZ Falcon for extraction!");
    MISSION.step = 4;
    playSound('objective');
    updateHUD();
  };

  document.getElementById('choice-intel-btn').onclick = function() {
    MISSION.choiceMade = "INTERCEPT_DATA";
    MISSION.score += 1000;
    document.getElementById('choice-modal').style.display = 'none';
    showDialogue("ROWAN KADE (RIVAL)", "Ruthless work, Spectre! You captured the saboteurs' encrypted frequency codes before they wiped the server. High command will commend this. Head to LZ Falcon!");
    MISSION.step = 4;
    playSound('objective');
    updateHUD();
  };

  document.getElementById('victory-continue-btn').onclick = function() {
    document.getElementById('victory-modal').style.display = 'none';
    const elapsed = Math.round((Date.now() - MISSION.startTime) / 1000);
    notifyMissionComplete(MISSION.score, MISSION.choiceMade || "SAFEGUARD_CIVILIANS", elapsed);
  };

  // --- TOUCH INPUT SYSTEM ---
  // Joystick for 3D Movement
  const joystickZone = document.getElementById('joystick-zone');
  const joystickKnob = document.getElementById('joystick-knob');
  let joystickInput = { x: 0, y: 0 };
  let joystickTouchId = null;

  joystickZone.addEventListener('touchstart', (e) => {
    e.preventDefault();
    const touch = e.changedTouches[0];
    joystickTouchId = touch.identifier;
    handleJoystickMove(touch);
  }, { passive: false });

  joystickZone.addEventListener('touchmove', (e) => {
    e.preventDefault();
    for (let i = 0; i < e.changedTouches.length; i++) {
      if (e.changedTouches[i].identifier === joystickTouchId) {
        handleJoystickMove(e.changedTouches[i]);
        break;
      }
    }
  }, { passive: false });

  function resetJoystick() {
    joystickTouchId = null;
    joystickInput = { x: 0, y: 0 };
    joystickKnob.style.transform = `translate(0px, 0px)`;
  }
  joystickZone.addEventListener('touchend', resetJoystick);
  joystickZone.addEventListener('touchcancel', resetJoystick);

  function handleJoystickMove(touch) {
    const rect = joystickZone.getBoundingClientRect();
    const centerX = rect.left + rect.width / 2;
    const centerY = rect.top + rect.height / 2;
    const maxDist = rect.width / 2 - 20;

    let dx = touch.clientX - centerX;
    let dy = touch.clientY - centerY;
    const dist = Math.sqrt(dx * dx + dy * dy);

    if (dist > maxDist) {
      dx = (dx / dist) * maxDist;
      dy = (dy / dist) * maxDist;
    }
    joystickKnob.style.transform = `translate(${dx}px, ${dy}px)`;
    joystickInput.x = dx / maxDist;
    joystickInput.y = dy / maxDist;
  }

  // Camera Orbit / Touch Aim Zone (Right screen)
  const aimZone = document.getElementById('aim-touch-zone');
  let aimTouchId = null;
  let lastAimX = 0;
  let lastAimY = 0;
  let cameraPitch = 0.25;

  aimZone.addEventListener('touchstart', (e) => {
    const touch = e.changedTouches[0];
    aimTouchId = touch.identifier;
    lastAimX = touch.clientX;
    lastAimY = touch.clientY;
  });

  aimZone.addEventListener('touchmove', (e) => {
    for (let i = 0; i < e.changedTouches.length; i++) {
      const touch = e.changedTouches[i];
      if (touch.identifier === aimTouchId) {
        const deltaX = touch.clientX - lastAimX;
        const deltaY = touch.clientY - lastAimY;
        player.rotY -= deltaX * 0.008;
        cameraPitch = Math.max(-0.2, Math.min(0.65, cameraPitch + deltaY * 0.005));
        lastAimX = touch.clientX;
        lastAimY = touch.clientY;
        break;
      }
    }
  });

  function resetAimTouch() { aimTouchId = null; }
  aimZone.addEventListener('touchend', resetAimTouch);
  aimZone.addEventListener('touchcancel', resetAimTouch);

  // Keyboard Fallback for Emulator / Desktop Testing
  const keys = {};
  window.addEventListener('keydown', (e) => {
    keys[e.key.toLowerCase()] = true;
    if (e.key === ' ' || e.key === 'Spacebar') {
      if (player.isGrounded) { player.vy = 8.5; player.isGrounded = false; }
    }
    if (e.key.toLowerCase() === 'e') fireBullet();
  });
  window.addEventListener('keyup', (e) => { keys[e.key.toLowerCase()] = false; });

  // Buttons
  document.getElementById('btn-fire').addEventListener('touchstart', (e) => {
    e.preventDefault();
    fireBullet();
  });
  document.getElementById('btn-jump').addEventListener('touchstart', (e) => {
    e.preventDefault();
    if (player.isGrounded) {
      player.vy = 8.5;
      player.isGrounded = false;
      notifyHaptic(30);
    }
  });

  function triggerScanner() {
    playSound('scan');
    notifyHaptic(70);
    const scanner = document.getElementById('scanner-overlay');
    scanner.classList.add('scanner-active');
    setTimeout(() => scanner.classList.remove('scanner-active'), 1200);

    // Expand 3D ring wave
    const scanRing = new THREE.Mesh(
      new THREE.RingGeometry(1, 1.8, 32),
      new THREE.MeshBasicMaterial({ color: 0x38D39F, side: THREE.DoubleSide })
    );
    scanRing.rotation.x = -Math.PI / 2;
    scanRing.position.set(player.pos.x, 0.4, player.pos.z);
    scene.add(scanRing);

    let scale = 1;
    const interval = setInterval(() => {
      scale += 1.8;
      scanRing.scale.set(scale, scale, scale);
      if (scale > 28) {
        clearInterval(interval);
        scene.remove(scanRing);
      }
    }, 20);
  }
  document.getElementById('btn-scan').addEventListener('touchstart', (e) => {
    e.preventDefault();
    triggerScanner();
  });

  // --- MAIN 60 FPS GAME LOOP ---
  let lastTime = performance.now();
  function animate() {
    requestAnimationFrame(animate);

    const now = performance.now();
    const dt = Math.min((now - lastTime) / 1000, 0.05);
    lastTime = now;

    // 1. Calculate Movement Vector
    let mx = joystickInput.x;
    let mz = joystickInput.y;

    if (keys['w'] || keys['arrowup']) mz = -1;
    if (keys['s'] || keys['arrowdown']) mz = 1;
    if (keys['a'] || keys['arrowleft']) mx = -1;
    if (keys['d'] || keys['arrowright']) mx = 1;

    const moveMag = Math.sqrt(mx * mx + mz * mz);
    const isMoving = moveMag > 0.1;

    if (isMoving) {
      // Move in player's facing direction
      const forward = new THREE.Vector3(0, 0, -mz).applyAxisAngle(new THREE.Vector3(0, 1, 0), player.rotY);
      const right = new THREE.Vector3(mx, 0, 0).applyAxisAngle(new THREE.Vector3(0, 1, 0), player.rotY);
      const moveDir = forward.add(right).normalize();

      const nextX = player.pos.x + moveDir.x * player.speed * dt;
      const nextZ = player.pos.z + moveDir.z * player.speed * dt;

      // Obstacle collision check
      let blocked = false;
      for (const col of colliders) {
        if (nextX >= col.minX && nextX <= col.maxX && nextZ >= col.minZ && nextZ <= col.maxZ) {
          blocked = true;
          break;
        }
      }
      if (!blocked) {
        player.pos.x = Math.max(-110, Math.min(110, nextX));
        player.pos.z = Math.max(-110, Math.min(110, nextZ));
      }

      player.animTime += dt * 14;
    } else {
      player.animTime += dt * 3; // Subtle breathing idle
    }

    // Jump Physics
    if (!player.isGrounded) {
      player.vy -= 22 * dt;
      player.pos.y += player.vy * dt;
      if (player.pos.y <= 0) {
        player.pos.y = 0;
        player.vy = 0;
        player.isGrounded = true;
      }
    }

    // 2. Procedural Rig Animations (Leg strides, arm swings, gun recoil)
    player.group.position.copy(player.pos);
    player.group.rotation.y = player.rotY;

    if (isMoving) {
      // Anti-phase leg swing
      player.leftLeg.rotation.x = Math.sin(player.animTime) * 0.75;
      player.rightLeg.rotation.x = -Math.sin(player.animTime) * 0.75;
      player.torso.position.y = 1.6 + Math.abs(Math.sin(player.animTime)) * 0.15;
    } else {
      player.leftLeg.rotation.x = 0;
      player.rightLeg.rotation.x = 0;
      player.torso.position.y = 1.6 + Math.sin(player.animTime) * 0.05;
    }
    // Return arms to firing position gradually
    player.rightArm.rotation.x += (-Math.PI / 4 - player.rightArm.rotation.x) * (dt * 12);

    // 3. Smooth Third-Person Camera Tracking
    const camDist = 7.5;
    const camHeight = 3.6 + cameraPitch * 3;
    const camOffset = new THREE.Vector3(0, camHeight, -camDist).applyAxisAngle(new THREE.Vector3(0, 1, 0), player.rotY);
    const targetCamPos = player.pos.clone().add(camOffset);
    camera.position.lerp(targetCamPos, dt * 10);

    const lookTarget = player.pos.clone().add(new THREE.Vector3(0, 2.0 - cameraPitch * 2, 0));
    camera.lookAt(lookTarget);

    // 4. Update Bullets & Hit Detection
    for (let i = bullets.length - 1; i >= 0; i--) {
      const b = bullets[i];
      b.mesh.position.addScaledVector(b.dir, 65 * dt);
      b.life += dt;

      // Check hit against Drones
      for (let d = drones.length - 1; d >= 0; d--) {
        const drone = drones[d];
        if (b.mesh.position.distanceTo(drone.group.position) < 1.6) {
          drone.hp--;
          playSound('hit');
          spawnSparks(b.mesh.position, 6);
          if (drone.hp <= 0) {
            playSound('explosion');
            notifyHaptic(100);
            spawnSparks(drone.group.position, 20);
            scene.remove(drone.group);
            drones.splice(d, 1);
            MISSION.score += 250;
            // Advance objective if all drones destroyed
            if (drones.length === 0 && MISSION.step === 1) {
              MISSION.step = 2;
              playSound('objective');
              showDialogue("TOMAS VALE", "Hostile drones cleared! Proceed to Relay Tower Alpha dish terminal and trigger your field scanner!");
              updateHUD();
            }
          }
          scene.remove(b.mesh);
          bullets.splice(i, 1);
          break;
        }
      }

      // Check hit against Shooting Range Targets
      if (b.mesh) {
        for (const tg of targets) {
          if (tg.active && b.mesh.position.distanceTo(tg.group.position) < 2.0) {
            tg.active = false;
            tg.group.rotation.x = -Math.PI / 2; // Knock down
            playSound('hit');
            spawnSparks(b.mesh.position, 10);
            MISSION.score += 150;
            scene.remove(b.mesh);
            bullets.splice(i, 1);
            break;
          }
        }
      }

      if (b.life > 1.2 && bullets[i]) {
        scene.remove(b.mesh);
        bullets.splice(i, 1);
      }
    }

    // 5. Update Drones (Hover animation + rotor spin)
    drones.forEach(d => {
      d.time += dt;
      d.group.position.y = d.basePos.y + Math.sin(d.time * 2.5) * 0.6;
      d.group.rotation.y += dt * 0.8;
      d.rotors.forEach(r => r.rotation.y += dt * 35);
    });

    // 6. Rotate Dish & Pulse Beacon on Tower Alpha
    dishMesh.rotation.y += dt * 0.6;
    towerBeacon.intensity = 1.0 + Math.sin(now * 0.005) * 1.0;

    // 7. Update Particles
    for (let pIdx = particles.length - 1; pIdx >= 0; pIdx--) {
      const p = particles[pIdx];
      p.mesh.position.addScaledVector(p.vel, dt);
      p.life -= dt;
      if (p.life <= 0) {
        scene.remove(p.mesh);
        particles.splice(pIdx, 1);
      }
    }

    // 8. Check Contextual Interaction Radius
    checkInteractionRadius();

    // Render WebGL frame
    renderer.render(scene, camera);
  }

  // Handle Window Resizing
  window.addEventListener('resize', () => {
    camera.aspect = window.innerWidth / window.innerHeight;
    camera.updateProjectionMatrix();
    renderer.setSize(window.innerWidth, window.innerHeight);
  });

  // Start Simulation
  updateHUD();
  animate();
})();
