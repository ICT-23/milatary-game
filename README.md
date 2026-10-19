# OPERATION: EMBERWATCH 3D — Military Academy & Field Simulation

[![Platform](https://img.shields.io/badge/Platform-Android%2014%2B%20(API%2036)-green.svg)](https://developer.android.com)
[![Engine](https://img.shields.io/badge/3D%20Engine-Three.js%20WebGL-blue.svg)](https://threejs.org)
[![UI](https://img.shields.io/badge/UI-Jetpack%20Compose-blue.svg)](https://developer.android.com/jetpack/compose)
[![Database](https://img.shields.io/badge/Persistence-Room%20(SQLite)-orange.svg)](https://developer.android.com/training/data-storage/room)

An original, story-driven 3D military action, academy training, and squad progression simulation game set in the coastal republic of Asterra.

---

## 🎮 Game Features

- **Real 3D Animated Character Rig (`Spectre-7`)**:
  - Hierarchical 3D mesh with tactical combat helmet, amber glowing visor, plate carrier armor, and military assault carbine.
  - Procedural skeletal running and walking animations with counter-balanced strides and arm swings.
  - Realistic weapon recoil kickback, dynamic point-light muzzle flashes, and ejected brass casings.
  - Jump physics with airborne lift and landing shock absorption.
- **3D Dynamic Coastal Outpost**:
  - Command Outpost Bunker with high-gain satellite dishes.
  - Damaged **Relay Tower Alpha** with a real-time rotating telemetry array.
  - Auxiliary Generator Vault with overheating pressure pipes.
  - Civilian Fallout Shelter with fortified blast doors.
  - Concrete perimeter barricades, sandbag emplacements, and ocean water planes.
- **Combat AI & Shooting Gallery**:
  - Autonomous 3D reconnaissance drones with 4 spinning rotors, alert spotlights, and explosive particle debris on destruction.
  - Pop-up ballistic silhouette targets on the perimeter range that physically knock down upon bullet impact.
- **Tactical Touch Controls & Low-Occlusion HUD**:
  - Smooth 3D trailing third-person camera with 360-degree touch-orbit aiming.
  - Virtual analog thumb joystick for 360-degree strafing and navigation.
  - Quick-action buttons: **FIRE**, **JUMP**, **SCAN**, and contextual **INTERACT**.
  - Telemetry HUD with health bars, ammunition readouts (`30 / 120`), and active objectives.
- **Procedural 3D Audio & Haptics**:
  - Low-latency Web Audio Synthesizer: Gunshot cracks with sub-bass punch, drone rotor whines, metallic ricochet pings, alert sirens, and scanner pulses.
  - Tactile Android haptic feedback triggered via the native `AndroidBridge`.
- **Local Persistence & Mission Progression**:
  - Android Room (SQLite) database stores campaign records, 3-star ratings, and branching choices offline without requiring servers or external accounts.

---

## 🛠️ Project Architecture

```
/
├── app/
│   ├── src/main/
│   │   ├── assets/game/           # 3D Three.js WebGL Engine, Shader Assets & Audio
│   │   │   ├── index.html         # 3D Viewport & Tactical HUD Overlays
│   │   │   ├── game.js            # 3D Engine, Character Rig, AI Drones & Ballistics
│   │   │   └── three.min.js       # Bundled local Three.js (100% offline)
│   │   ├── java/com/example/
│   │   │   ├── MainActivity.kt    # Root Compose Activity & Navigation Router
│   │   │   ├── data/              # Room Entities, DAO, and Repository
│   │   │   ├── game/              # Simulation Engine, Math, and Mission Catalog
│   │   │   └── ui/                # Jetpack Compose Screens, HUD, and Theme
│   │   └── res/                   # Adaptive Icons, Strings, and Values
├── docs/                          # Game Design, Story Bible & Architecture Guides
├── build.gradle.kts
└── settings.gradle.kts
```

---

## 🚀 Building & Running

### Prerequisites
- Android Studio Ladybug / Meerkat (or newer)
- Android SDK 36 (Android 16)
- JDK 21

### Build APK
```bash
gradle assembleDebug
```
The compiled APK will be generated at:
`app/build/outputs/apk/debug/app-debug.apk`

---

## 📄 License & Attribution
Created for **Operation: Emberwatch** / Military Academy 3D Simulation.
Repository: [https://github.com/ICT-23/milatry-acadmia](https://github.com/ICT-23/milatry-acadmia)
