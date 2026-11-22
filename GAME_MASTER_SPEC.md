# GAME MASTER SPEC — VANGUARD ACADEMY 3D

Applying Rules: [SEARCH_FIRST | NON_DESTRUCTIVE | STRICT_LOGIC | CORE]

Source of record: `docs/Military Game Design Plan.md`. This file is the bootstrap
header that document's "Operational Synthesis" requires; it summarises the
binding rules and maps them onto this repository. Where this summary and the
source disagree, the source wins.

---

## 0. Response Header (mandatory)

Every response from an agent working in this repository starts with:

```
Applying Rules: [SEARCH_FIRST | NON_DESTRUCTIVE | STRICT_LOGIC | <ACTIVE_MODULE>]
```

`<ACTIVE_MODULE>` is one of `CORE`, `PROGRESSION`, `GUNSMITH`, `COMBAT`,
`NARRATIVE`, `META`, `UI`.

## 1. Golden rules

1. **Never replace or delete working code.** Existing modules, state machines,
   UI components and weapon logic are extended, never rewritten.
2. **Search first.** Grep and semantic search before writing anything.
3. **Extend, do not mutate.** New behaviour arrives as new types, new data, or a
   new EventBus subscriber. Smallest possible diff.
4. **Protect the module registry.** `MODULE_REGISTRY.json` is checked before an
   edit. A module at `LOCKED_VERIFIED` may not have its public API signature or
   core logic changed without explicit human override.
5. **File size and hygiene cap.** Every source file stays under 300 lines. No
   dead functions, no unused imports.
6. **No hallucinated APIs.** Only code that has actually been read is relied on.

## 2. Six-step execution sequence

| Step | Gate |
| --- | --- |
| 1 SCAN | read the target module, its neighbours, `MODULE_REGISTRY.json` |
| 2 PLAN | state exact paths created/extended and how existing logic survives |
| 3 TEST CONTRACT | contract cases defined before the implementation |
| 4 DOMAIN | pure logic returning `GameResult<T>` / `GameError` |
| 5 VIEW | meshes, cameras, HUD subscribed through the EventBus |
| 6 VERIFY | type check, lint, contract audit, runtime logs |

Business and ballistic rules never live inside a button handler or an animation
callback.

## 3. Topology → packages

The specification's `src/<Module>` layout maps one-to-one onto Kotlin packages:

| Spec | Package |
| --- | --- |
| `src/Core/` | `com.example.game.vanguard.core` |
| `src/Progression/` | `com.example.game.vanguard.progression` |
| `src/Gunsmith/` | `com.example.game.vanguard.gunsmith` |
| `src/Combat/` | `com.example.game.vanguard.combat` |
| `src/Narrative/` | `com.example.game.vanguard.narrative` |
| `src/Meta/` | `com.example.game.vanguard.meta` |
| `src/UI/` | `com.example.game.ui` + `com.example.game.vanguard` (audit surface) |

Pre-existing packages (`com.example.game.combat`, `.progression`, `.academy`,
`.data`, `.ui`) stay untouched. Vanguard reuses their geometry primitives
(`Vec3`, `BallisticBody`, `BallisticsEngine.raySphere`) instead of duplicating
them.

## 4. Module contracts

- **Core** — `EventBus` (sync, typed, unsubscribable), `GameResult<T>` with a
  stable `GameError.code`, categories for UI branching. `GameClock` runs a scaled
  and an unscaled timeline; `DependencyContainer` resolves once and freezes;
  `ModuleRegistry` mirrors the JSON registry and answers `mayEdit`.
- **Combat gate 1** — `FireControl`: safety position → chambered round → bolt
  locked → not reloading → muzzle clear → `now >= nextAllowedFireTimestamp`
  with `interval = 60.0 / TruePhysicsRPM`. The HUD reads the same interval.
- **Combat gate 2** — ADS camera is aligned *to* the optic socket; FOV never
  moves the sight line.
- **Combat gate 3** — dual vector: the sight vector must land inside the aiming
  cone *and* the muzzle→impact segment must be free. Otherwise 0 damage.
- **Combat gate 4** — four hitboxes: skull 3.5x (bypasses plate), torso 1.0x
  (plate stops under-rated rounds), pelvis 1.1x (bypasses plate, collapses
  mobility), limbs 0.45x (arm sway 0.25 / leg slow 0.35). Entry and exit decals.
- **Combat gate 5** — Isosceles / Weaver / Harries, Standing / Crouched / Prone,
  accumulating spring recoil, darkness penalty only inside the Harries beam.
- **Combat gate 6** — capsule clearance puts the weapon in compressed ready and
  inhibits fire; prone transitions are mutually exclusive with melee, lean and
  sprint.
- **Gunsmith** — seven sockets; six calibres; 57/62 cm barrels at a 16 mm
  profile with an M14 thread; BRX1 45° reversible linear bolt (780 rpm) beside
  the 660 rpm rotating bolt; triggers 900/1200/1500 g; SFP 5-25 (exact at 25x,
  doubles at 12.5x) and FFP 5-25 (constant MRAD); parallax 10 m–infinity;
  thread protector / FTE brake / suppressor (flash hidden, smoke alpha capped,
  first-shot pop); LOP 365 mm + 12.7 mm spacers; Kick-Off I/Plus/Mega to 70%;
  barrel swaps must hold the bedding under 3.0 SUB MOA or be refused.
- **Progression** — nine monotonic ranks `E1_PVT..CIV_PRES`, append-only ledger,
  regression rejected, chapter loaders may never reset to `E1_PVT`, 3.0 s
  promotion watchdog on unscaled time, `RankChanged` → uniform mesh binder that
  preserves face, hair and personal gear.
- **Narrative** — three-way dilemmas only, unique ids, no dominating option,
  `TacticalDoctrine` / `UnitMorale` / `PoliticalCapital` in `[-100, +100]`.
- **Meta** — production progress is `now - startUtc`; presidential GDP split
  across Civil Infrastructure / Defense R&D / Naval-Ground Fleets totalling
  100; meta camera pinch chain; no interstitial, store prompt or battle pass
  while `CombatState == ACTIVE_MISSION`.
- **Meta systems** — `BaseIdleManager` caps offline accrual at 12 h;
  `BattleMapController` folds zone control (HOSTILE/CONTESTED/FRIENDLY/SECURED)
  and names the next objective deterministically; `VehicleCustomizationBay`
  fits armour, powertrain, paint and weapon station or fails with a typed code.
- **System support** — `AcousticReverbSystem` (radius, tail, decibels, first-shot
  pop), `SkillTreeService` (prerequisites, max rank, linear branch bonus),
  `VoiceOverSync` (single owner per instant, 0.85–1.15 playback, 0.5 s skip
  lock), `StoryArcManager` (five acts gated by chapter prerequisites and rank).
- **UI** — `HudController`, `PauseSensitivityPanel` and `CheckpointAdBoundary`
  read domain values only. The HUD never accepts an RPM argument; it prints
  `MechanicalState.hudRpm`.

## 5. Module registry workflow

`MODULE_REGISTRY.json` holds the six keys from the specification. Legal values:

- `PENDING_IMPLEMENTATION` — not built.
- `IMPLEMENTED_PENDING_AUDIT_RUN` — built and covered by contract cases, but the
  Step 6 verification has not been executed in this environment.
- `LOCKED_VERIFIED` — type check, lint and the contract audit have been run and
  passed. Only then may a module be treated as untouchable.

Promotion to `LOCKED_VERIFIED` requires running the audit in
`com.example.game.vanguard.VanguardContractAudit` and seeing every case pass.

## 6. Verification

The contract gate is executable inside the app:

```kotlin
val audit = VanguardContractAudit.audit()
audit.allPassed          // every case
audit.failures           // ContractCase(group, index, title, expectation, detail)
audit.byGroup()          // CORE / COMBAT / GUNSMITH / CAREER / NARRATIVE / META / SYSTEMS
VanguardContractAudit.GROUP_ORDER  // fixed display order for the audit screen
```

Each case is pure, deterministic domain code, so the same result is produced on
every launch and every device. Tests are defined before the implementation and
never edited to accommodate a broken one.
