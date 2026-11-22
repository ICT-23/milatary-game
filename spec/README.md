# OPERATION: EMBERWATCH — Game Specification

This folder is the single source of truth for the game. Every rule in here is
something the engine is expected to *do*, not something it hopes to do. If the
game and this folder disagree, the game is wrong.

## Reading order

| File | What it pins down |
| --- | --- |
| `01_VISION.md` | The fantasy, the pillars, the feel we are chasing |
| `02_RULES_AND_COMBAT.md` | Core loop, aiming, firing, damage, ammo, score, AI rules |
| `03_3D_ANIMATION_AND_CAMERA.md` | Render budget, camera rig, every animated element, VFX |
| `04_CAMPAIGN_CHAPTERS.md` | All ten chapters, objectives, hostiles, pacing |
| `05_UI_HUD_AND_CHAPTER_SELECT.md` | HUD contract, deployment screen, unlocks |
| `06_ACCEPTANCE_AND_ROADMAP.md` | Pass/fail checks and the build order |

## How the spec maps to code

| Spec area | Implemented in |
| --- | --- |
| Chapter data, themes, environments | `app/src/main/assets/game/levels.js` |
| Rules, combat, AI, camera, HUD logic | `app/src/main/assets/game/game.js` |
| HUD markup, overlays, CSS | `app/src/main/assets/game/index.html` |
| Native dossiers, unlocks, stars | `app/src/main/java/com/example/game/MissionDefinition.kt` |
| Automated contract checks | `tools/level-audit.js`, `tools/dom-audit.js`, `tools/kotlin-audit.js`, `tools/free-scan.js` |
| Local browser harness | `node tools/serve.js` → `http://localhost:5173` |

## Definition of done

A change is only "done" when:

1. `node --check` passes on every touched `.js` file.
2. `node tools/level-audit.js` prints `PROBLEMS: 0`.
3. `node tools/dom-audit.js` prints `missing ids: none` with every CSS check `ok`.
4. `node tools/kotlin-audit.js` prints no missing dossiers and no missing choice
   briefs — the Android campaign layer must list every chapter in `levels.js`.
5. The behaviour in `06_ACCEPTANCE_AND_ROADMAP.md` was observed in a browser,
   not assumed from reading the code.
