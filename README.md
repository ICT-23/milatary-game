# OPERATION: EMBERWATCH 3D — Military Academy & Field Simulation

[![Platform](https://img.shields.io/badge/Platform-Android%2014%2B%20(API%2036)-green.svg)](https://developer.android.com)
[![Engine](https://img.shields.io/badge/3D%20Engine-Three.js%20r128%20WebGL-blue.svg)](https://threejs.org)
[![UI](https://img.shields.io/badge/UI-Jetpack%20Compose-blue.svg)](https://developer.android.com/jetpack/compose)
[![Database](https://img.shields.io/badge/Persistence-Room%20(SQLite)-orange.svg)](https://developer.android.com/training/data-storage/room)

An original, story-driven 3D military action, academy training, and squad progression simulation set in the coastal republic of Asterra.

Seven chapters are playable end to end. Every chapter is data-driven: the objective chain, hostiles, branching decisions and the procedural environment are defined in `levels.js`, while `game.js` runs combat, AI and the mission state machine.

---

## 🎮 Game Features

- **Seven playable chapters** — Coastal relay outpost, subterranean cable vaults, tidal salt flats, highway evacuation, mining district, fortress defence and the offshore transmission platform. Each chapter has its own sky, fog, palette, geometry and objective chain.
- **Real 3D operative rig (`Spectre-7`)**:
  - Hierarchical mesh with combat helmet, amber visor, plate carrier and assault carbine.
  - Procedural running/walking animation, weapon recoil, muzzle flashes and ejected brass.
  - Jump physics with airborne lift and landing absorption.
- **Combat that resolves**:
  - Aim-assisted hit-scan carbine — the crosshair turns red when a target is inside the lock cone.
  - Magazine-fed ammo (`30` in the mag, chapter-specific reserve) with a real reload cycle, reload bar and ammo warnings.
  - Three hostile archetypes share one targeting pipeline — patrolling `scout` quad-rotors, fixed `sentinel` turrets with a tracking head, and `walker` bipeds that close to short range. Each has its own rig, hit radius, bolt speed, aim spread, damage and score.
  - Hostiles patrol, acquire, strafe, fire tracer bolts and deal damage. Player health regenerates only after 8 seconds out of contact.
  - **Campaign finale**: chapter 7 ends on a boss encounter — the architect's 60-hit-point command frame, three-bolt volleys and two escort flights — with its own HUD health strip and hull-percentage objective tracking.
  - Death, retry and abort flows are handled inside the engine.
- **Threat briefings**: every chapter's operational dossier lists the exact enemy composition (scouts, sentinels, walkers, the boss and wave contents) so players know what the next deck throws at them before deploying.
- **Campaign completion banner**: clearing chapter 7 swaps the "next operation" line for a full-width ember banner in the after-action report.
- **Field resupply**: every chapter seeds 4–5 one-shot supply caches on the route. Amber crates refill the reserve pool (two magazines, capped at twice the chapter's starting reserve); green medkits restore up to 40 vitality. Each cache scores 100 points, flashes a HUD confirmation and cancels an in-progress reload.
- **Objective machine**: `talk`, `destroy`, `scan`, `interact`, `choice`, `defend` (multi-wave) and `reach`, plus `auto` waypoints that complete on arrival. Every objective drives the compass, the waypoint beam and the HUD.
- **Branching decisions**: chapters 1, 5 and 7 each end on a choice card (safeguard civilians / intercept data, detain or cover Rowan Kade, broadcast the truth or destroy the array). The chosen id is stored by Room and shown in the after-action report.
- **Living squad relationships**: each decision shifts trust with Voss, respect with Idris, affinity with Leena, sync with Tomas and rivalry with Kade (plus squad morale). The roster screen reads those columns from the profile and renders a live standing meter for every operative.
- **Tactical touch controls & low-occlusion HUD**:
  - Third-person trailing camera with touch-orbit aiming and an analog thumb joystick.
  - `FIRE`, `JUMP`, `SCAN`, `RELOAD`, `INTERACT` and a pause button.
  - Timer, score, health, ammo, compass bearing, objective progress and a desktop keyboard fallback (`WASD`, mouse aim, `Space`, `R`, `E`, `Esc`).
- **Procedural 3D audio & haptics** — Web Audio synthesizer for gunshots, rotor whine, impacts, alarms and extraction, plus Android haptics through the native `AndroidBridge`.
- **Campaign progression** — star ratings, best score/time per chapter, sequential unlocks, XP rewards and a five-step rank ladder, all persisted offline in Room.

---

## ⭐ Rating & Progression Rules

| Result | Condition |
| --- | --- |
| 1 star | Chapter completed |
| 2 stars | Completed with 60%+ health remaining |
| 3 stars | Additionally finished under the chapter's par time |

`MissionRating` in `app/src/main/java/com/example/game/MissionDefinition.kt` is the authority — the engine's own stars are recomputed natively so stored results cannot drift.

Ranks: Junior Cadet → Cadet Operative (250 XP) → Field Operative (1,000 XP) → Senior Operative (2,000 XP) → Emberwatch Vanguard (3,000 XP). First clears pay the full chapter reward; replays pay a quarter.

### Supply cache data

Each chapter declares its caches in `levels.js`:

```js
supplies: [
  { type: 'ammo', x: -6, z: -20 },
  { type: 'medkit', x: 8, z: 12 }
]
```

`ammo` adds two magazines to the reserve (capped at `2 × reserveAmmo`), `medkit` restores up to 40 vitality, and both award 100 points. The coordinates are written into walkable space — the level audit checks every spawn, objective and cache against the generated colliders, so no pickup can spawn inside geometry.

### Hostile archetypes

Entries in a chapter's `drones` array (and inside a `defend` objective's `waves`) accept an optional `kind`:

```js
drones: [
  { x: 8, y: 6, z: 14, hp: 5, squad: 'elite' },                 // scout, the default
  { kind: 'sentinel', x: -22, z: -2, squad: 'guard' },          // placed turret
  { kind: 'walker', x: 14, z: 2, squad: 'guard' }               // heavy biped
]
```

| Kind | Hit points | Behaviour | Damage / cadence | Score |
| --- | --- | --- | --- | --- |
| `scout` | 3 (4 in waves) | Hovers, strafes, retreats inside 8 m | 9 per bolt, ~2.6 s | 250 |
| `sentinel` | 8 | Static; head tracks the player, 24–30 m engagement | 10 per bolt, ~1.6 s, 34 m/s bolts | 400 |
| `walker` | 14 | Walks straight at the player, stops at 8 m, no strafe | 16 per bolt, ~3.4 s | 600 |
| `boss` | 60 | Holds 10–17 m, three-bolt volleys, summons escorts at 66% / 33% hull | 20 per bolt, ~1.15 s | 3,000 |

The chapter 7 command frame is not in the `drones` array — it arrives with its objective, so it only appears once the player has reached the console:

```js
{
  kind: 'destroy', title: 'APPREHEND THE ARCHITECT',
  pos: [12, 0, -14], squad: 'architect', required: 1,
  spawn: [{ kind: 'boss', x: 12, z: -14, label: 'THE ARCHITECT // COMMAND FRAME' }]
}
```

Any objective with a `spawn` array raises its reinforcements the moment it becomes current. While a boss is alive the HUD shows a dedicated health strip (`#boss-bar`) and its objective reads out hull percentage instead of a kill count; the escorts it deploys are tagged `escort` so they never contaminate the boss counter.

Ground kinds (`sentinel`, `walker`, `boss`) ignore `y` and stand on the deck, so their coordinates must sit in free space. `tools/level-audit.js` checks a 1.2–1.7 m footprint for every placed ground hostile, objective spawn and wave, and ground hostiles carry their own `guard` squad so `destroy` and `defend` counters stay exact.

---

## 🛠️ Project Architecture

```
/
├── app/
│   └── src/main/
│       ├── assets/game/
│       │   ├── index.html         # Viewport, combat HUD, dialogue/choice/victory/pause overlays
│       │   ├── levels.js          # 7 chapter definitions + procedural environment builders
│       │   ├── game.js            # Renderer, player rig, drone AI, ballistic combat, mission machine
│       │   └── three.min.js       # Bundled Three.js r128 (100% offline)
│       ├── java/com/example/
│       │   ├── MainActivity.kt    # Root Compose activity + screen router + back handling
│       │   ├── data/              # Room entities, DAO, repository, progression rules
│       │   ├── game/              # Mission catalog, rating rules, 2.5D engine, audio
│       │   └── ui/                # Screens (title, ops board, briefing, 3D view, debrief, roster, settings), HUD, theme
│       └── res/                   # Adaptive icons, strings, values
├── docs/                          # Game design, story bible and architecture guides
├── emberwatch/docs/               # Documentation mirror
├── operation-emberwatch/          # Separate React Native/Expo prototype (mission 01 only)
├── build.gradle.kts
└── settings.gradle.kts
```

### Native ↔ engine bridge

`Game3DView` loads `file:///android_asset/game/index.html?mission=<CHAPTER_ID>` and exposes `AndroidBridge`:

| Direction | Call | Purpose |
| --- | --- | --- |
| JS → Kotlin | `getPlatformConfig()` | Returns JSON with mission id, sensitivity, SFX volume, haptics and high-contrast flags |
| JS → Kotlin | `onMissionComplete(score, choice, timeSec, healthPct)` | Final result; Kotlin recomputes stars and persists the run |
| JS → Kotlin | `onAbortMission()` | Abort from the pause or defeat dialog |
| JS → Kotlin | `triggerHaptic(ms)` | Vibration request |
| Kotlin → JS | `window.emberwatchPause()` / `window.emberwatchResume()` | Hardware back opens the engine pause menu |

---

## 🕹️ Controls

| Action | Touch | Keyboard / mouse |
| --- | --- | --- |
| Move | Left virtual joystick | `W` `A` `S` `D` |
| Aim / orbit camera | Drag the right side of the screen | Hold left mouse button and drag |
| Fire | Hold `FIRE` | Hold `E` (or click `FIRE`) |
| Reload | `RELOAD` | `R` |
| Jump | `JUMP` | `Space` |
| Scan | `SCAN` | — |
| Interact | Contextual `INTERACT` button | `E` when the button shows |
| Pause | Pause button | `Esc` / `P` |

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

### Notes
- The 3D engine is fully offline — `three.min.js` is bundled, no CDN or network permission is required.
- `levels.js` can be opened directly in `index.html` for desktop testing: `index.html?mission=M6_EMBER_PROTOCOL` runs that chapter without the Android bridge.

### Data validation (no Android build required)
```bash
node --check app/src/main/assets/game/game.js
node --check app/src/main/assets/game/levels.js
node tools/level-audit.js   # spawns, objectives, caches and ground hostiles vs. the generated colliders
node tools/dom-audit.js     # every element id game.js queries exists in index.html
node tools/free-scan.js platform -34 34 -34 34 6   # walkability grid for authoring new positions
```

---

## 📄 License & Attribution
Created for **Operation: Emberwatch** / Military Academy 3D Simulation.
Repository: [https://github.com/ICT-23/milatry-acadmia](https://github.com/ICT-23/milatry-acadmia)
