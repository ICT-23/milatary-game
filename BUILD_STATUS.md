# BUILD STATUS

**Engine / stack:** Kotlin 2.2.10 + Jetpack Compose (the Unity plan translated to this stack)
**Last updated:** 2026-10-31
**Document of record:** `docs/Valor_Academy_Implementation_Plan.md` (Sections 2, 3) and `docs/Valor_Academy_Launch_and_LiveOps.md` (Sections 1-13)

Every row below was filled from an inventory of this repository, not from the
plan's assumptions. A row is `EXISTS-WORKING` only when the file beside it runs
today; anything still missing is marked so.

## Systems

| System | Status | Files | Notes |
|---|---|---|---|
| Player controller | EXISTS-PARTIAL | `ui/EmberwatchViewModel.kt`, `MainActivity.kt` | Tap-driven Compose campaign loop; no third-person 3D controller yet |
| Weapon system | EXISTS-WORKING | `balance/Weapons.kt`, `plan/FiringPipeline.kt` | Shipped weapon catalogue plus the eight ordered fire checks |
| Damage system | EXISTS-WORKING | `balance/BattleMath.kt`, `plan/DamageSystem.kt` | Single damage entry point; zones, falloff, armour, helmet all delegated |
| Enemy AI | EXISTS-PARTIAL | `plan/EnemyAiLogic.kt` | Eight-state logic machine; no NavMesh agent or scene perception yet |
| Mission manager | EXISTS-WORKING | `game/MissionDefinition.kt`, `plan/MissionFsm.kt` | Ten-state machine, objectives, watchdog, checkpoints, single grant |
| Rank/XP | EXISTS-WORKING | `balance/Ranks.kt`, `plan/ProgressionService.kt` | Full rank ladder, multi-rank-up loop, XP only through `ProgressionService` |
| Economy | EXISTS-WORKING | `balance/Economy.kt`, `plan/EconomyService.kt` | Atomic spend with rollback and duplicate-tap guard |
| Gunsmith | EXISTS-WORKING | `balance/GunsmithProgression.kt`, `plan/GunsmithLogic.kt` | Levels 1-10 with no skipping; preview never touches live stats |
| Save/Load | EXISTS-PARTIAL | `plan/SaveSystem.kt`, `data/EmberwatchDatabase.kt` | Atomic JSON save logic proven in memory; platform file binding still to do |
| HUD/Menus | EXISTS-WORKING | `ux/HudSpec.kt`, `plan/UiRules.kt`, `ui/screens` | HUD anchor layout, truth-only readout, queued popups, editable layout |
| Squad AI | EXISTS-PARTIAL | `plan/SquadStealth.kt` | Five commands, downed timer and squad cap; no ally pathing yet |
| Drones | EXISTS-PARTIAL | `gdd/Support.kt`, `plan/DroneLogic.kt` | Rank gate, charges, cooldowns, strike delay, FPV battery; no flight model |
| Story/Dialogue | EXISTS-WORKING | `gdd/Story.kt`, `plan/StoryFlow.kt` | Beats once, confirm-only choices, three reachable endings |
| Audio | EXISTS-PARTIAL | `game/AudioFeedback.kt` | Event cues only; no mixer, layering or ad ducking |
| VFX | MISSING | - | No particle or decal layer exists in this stack yet |
| Launch & live-ops | EXISTS-WORKING | `launch/*.kt`, `ui/screens/LaunchOpsScreen.kt` | Environment, build, telemetry, money, consent, FTUE, listing, QA, rollout and risk as shipping tables; live ad serving and analytics upload still have no backend |

Counts: 9 EXISTS-WORKING, 6 EXISTS-PARTIAL, 0 EXISTS-BROKEN, 1 MISSING.

## PROTECTED

These files may be extended, never overwritten.

- `balance/BattleMath.kt` - the only source of zone multipliers, falloff, armour and helmet numbers
- `balance/Ranks.kt` - the rank table; XP and rank labels read from here
- `balance/Economy.kt` - currency names and reward tables; never re-typed elsewhere
- `ux/HudSpec.kt` - the HUD anchors; the plan's UI rules read this layout
- `game/MissionDefinition.kt` - shipped mission content; new content is additive only
- `MainActivity.kt` - the navigation shell; new screens are added as branches, never rewrites

## Open Questions

1. Helmet tier 3: the plan prints `max(1, 2.5 - 0.5*tier) = 1.0`, `balance.ArmorMath` prints 1.2 - the shipped table wins (Section 2.4), and `HelmetAdapter.DIVERGENT_TIERS` records the divergence.
2. Aim assist: the plan prints Off/Low/High, the design bible lists four settings - the plan's three ship.
3. Unity physics layers have no Android equivalent; the `PhysicsLayer` enum is the mapping.
4. No Gradle wrapper or `local.properties` in the project, so this build is verified statically.
5. Drones: the charge count per type is not printed in either document.
6. Section 1.4 asks for the five spec files in the repository root; they currently live in `docs/` and are listed by name in `RepositorySpec.ROOT_DOCUMENTS`. `BUILD_STATUS.md`, `CHANGELOG.md` and `ASSET_LICENSES.md` are at the root.
7. Git LFS is not installed here, so `RepositorySpec.LFS_PATTERNS` records the twelve patterns as data; no `.gitattributes` filter ships until `git-lfs` is available, otherwise commits would break.
8. The document fixes the ad caps and the KPI bands but not the crate odds table, the reward amounts or the daily login payouts - the build ships `crate_odds=standard=1.0`, `daily_op_seed_list=auto` and `daily_login_day_N` reward ids so no balance number is invented.
9. `AnalyticsTracker` is the only door events leave through, but it holds them in memory: the remote sink, the crash reporter and the ad network are outside this repository.
10. Section 12 prints phase durations that sum to 32-50 weeks against the "~8-11 months" total; both are recorded as printed, and the gate asserts the labels rather than the sum.

## Next Task

1. Bind `SaveStore` to a real Android file implementation and keep the same atomic sequence.
2. Surface the plan gate in the debug panel so a device run reports the same case list.
3. Wire `AnalyticsTracker` and `RemoteConfig` to the chosen backend, keeping the six `RemoteConfig.GROUPS` and the shipped defaults as the fallback when the network is down.
4. Add the first-launch age gate + consent dialog screens that write into `ConsentGate`, so the Settings privacy entry reads a recorded answer instead of an undecided one.
