# 06 — Acceptance Criteria and Build Order

## A. Shooting is honest

| # | Check | How to verify |
| --- | --- | --- |
| A1 | Aiming at empty air and firing deals no damage anywhere | Fire at the sky 5 times, confirm no kill credit and no score change |
| A2 | Aiming at a hostile's body and firing damages **that** hostile | Stand 12 m from a sentinel, centre it, fire once, confirm damage number `1` and a flinch |
| A3 | A head hit deals double and reads `HEAD` on the crosshair | Aim at the sensor, confirm `2` and the crit styling |
| A4 | Cover blocks shots | Put a container between the player and a walker, fire, confirm the impact lands on the wall and the walker is untouched |
| A5 | Rounds are consumed per trigger pull regardless of hit | Fire 3 misses, confirm 30 → 27 |
| A6 | No auto-aim | Turn 40° away from a hovering scout, fire, confirm the scout is untouched |

## B. It reads as 3D

| # | Check | How to verify |
| --- | --- | --- |
| B1 | Player and hostiles cast shadows on the ground | Screenshot mid-scene, shadows visible under the rigs |
| B2 | Sky is a gradient, not a flat colour | Screenshot looking up |
| B3 | Scout rotors spin, walker legs stride, sentinel head tracks | Watch each archetype for 5 s |
| B4 | Every shot produces muzzle flash + tracer | Screenshot within 60 ms of firing |
| B5 | A kill topples the rig instead of deleting it | Kill a walker, watch the fall |
| B6 | Camera looks along the aim direction: crosshair is where the shot goes | Aim at a distant beacon, confirm the reticle sits on it |

## C. Chapters exist and are visible

| # | Check | How to verify |
| --- | --- | --- |
| C1 | Opening the game without a `mission` parameter shows ten chapter cards | Load `/index.html` |
| C2 | Locked chapters are visible but cannot be started | Select CH09 on a fresh profile, expect a toast |
| C3 | Clearing a chapter unlocks the next and records stars | Clear CH01, return to the select screen |
| C4 | Every chapter boots, spawns the player on walkable ground and shows a boot card | Load each `?mission=` id |
| C5 | Android dossiers match the ten chapters | `MissionDefinition.ALL_MISSIONS.size == 10` |

## D. Systems

| # | Check |
| --- | --- |
| D1 | `node --check` clean on `game.js`, `levels.js`, `tools/*.js` |
| D2 | `node tools/level-audit.js` → `PROBLEMS: 0` for all ten chapters |
| D3 | `node tools/dom-audit.js` → `missing ids: none`, script order correct |
| D4 | Pause / dialogue / choice / death / blur clear all held input |
| D5 | No console error on any chapter boot |

## Build order used for this spec

1. **Spec** — this folder (vision → rules → 3D → campaign → UI → acceptance).
2. **Combat rewrite** — `player.aimPitch`, aim ray, per-part hitboxes, cover
   sampling, damage numbers, tracer, flinch, death tumble. Replaces the
   projectile `bullets` pipeline.
3. **Camera** — look-along-aim so the crosshair is truthful.
4. **Render pass** — tone mapping, shadow camera fit, gradient sky dome,
   contact shadows, emissive cores.
5. **Campaign** — chapters 8–10 in `levels.js`, mirrored in
   `MissionDefinition.kt`.
6. **Deployment screen** — chapter cards, locks, stars, `localStorage`.
7. **Audits** — extend `level-audit` (new chapters, new footprints) and
   `dom-audit` (deployment screen, damage layer, crosshair label).
8. **Browser verification** — the checks in A, B and C above, observed.

## Known constraints

- No JDK or Gradle wrapper jar in this environment: the APK cannot be compiled
  here. Kotlin changes are verified by brace/paren balance and by matching the
  existing dossier schema exactly.
- The web build and the Android asset bundle are the same files; anything added
  to `app/src/main/assets/game` ships to both.
