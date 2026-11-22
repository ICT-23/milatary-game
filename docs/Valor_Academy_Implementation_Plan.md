# VALOR ACADEMY — IMPLEMENTATION PLAN
### Build rules, gameplay logic, phased tasks and tests (for AI / vibe-coding)

> **Companion file:** `Valor_Academy_Game_Design.md` (WHAT to build).
> **This file:** HOW to build it, in what order, and the **logic rules that must never be broken**.
> **Read both files fully before writing any code.**

---

## TABLE OF CONTENTS
1. Golden Rules (read first)
2. Existing-Work Protection Protocol (do NOT replace what is built)
3. Project Status Tracking (BUILD_STATUS.md)
4. Architecture & Conventions
5. CORE GAMEPLAY LOGIC (the rules that make the game "make sense")
   - 5.1 Aiming
   - 5.2 Firing pipeline (can I shoot?)
   - 5.3 Hit detection (damage ONLY on a real hit)
   - 5.4 Damage calculation
   - 5.5 Health, death and kill credit
   - 5.6 Ammo, reload, weapon swap
   - 5.7 Attachments & stat modifiers
   - 5.8 Cover system
   - 5.9 Grenades & explosions
   - 5.10 Enemy AI logic
   - 5.11 Squad AI logic
   - 5.12 Stealth logic
   - 5.13 Drones & support logic
   - 5.14 Mission state machine
   - 5.15 Tournament / timed challenges
   - 5.16 XP, ranks, unlocks
   - 5.17 Economy & shop
   - 5.18 Gunsmith / crafting
   - 5.19 Save system
   - 5.20 Story & choices
   - 5.21 UI/HUD rules
   - 5.22 Ads & monetization rules
6. Phased Implementation Plan (milestones, files, acceptance criteria)
7. Testing Plan
8. Performance Plan
9. Edge Cases & Known-Bug Prevention
10. Debug Tools
11. Prompt Templates for Each Session
12. Definition of Done

---

## 1. GOLDEN RULES (READ FIRST)

These apply to **every** task, in **every** session.

1. **Never replace working code.** If a file, class, prefab, scene or asset already exists and works, **extend it** (add a new method, a new component, a new file). Do not rewrite it from scratch, rename it, move it, or delete it. See Section 2.
2. **Inspect before you build.** Before creating anything, search the project to see if it already exists (by name and by purpose).
3. **One system per task.** Do not touch unrelated systems in the same task.
4. **Logic first, visuals second.** A feature is "done" only when the logic rules in Section 5 pass their tests, not when it merely looks right.
5. **Single source of truth.** Damage is applied in ONE place (`DamageSystem`). XP in ONE place (`ProgressionService`). Currency in ONE place (`EconomyService`). Never duplicate this logic in other scripts.
6. **Data-driven.** Numbers (damage, RPM, XP, prices) live in ScriptableObjects/JSON, never hard-coded inside gameplay scripts.
7. **Everything testable.** Core logic (damage, XP, economy, mission states, save) must be pure C# classes that can be unit-tested without the Unity scene.
8. **No silent failures.** Every rejected action (can't shoot, can't buy, invalid hit) logs a reason in debug builds.
9. **Keep the game always runnable.** At the end of every task the project must open, compile with zero errors, and the Main Menu → Game → Result flow must work.
10. **Document as you go.** Update `BUILD_STATUS.md` and `CHANGELOG.md` at the end of each task (Section 3).
11. **If unsure, ask or choose the safest additive option** — never the destructive one.

---

## 2. EXISTING-WORK PROTECTION PROTOCOL

Use this protocol **every time** before writing code. The user may have already built parts of the game; they must not be lost or overwritten.

### 2.1 Step-by-step before each task
1. **Read `BUILD_STATUS.md`** (if it does not exist, create it using Section 3 after step 2).
2. **Inventory scan:** list all folders/files under `Assets/Scripts`, `Assets/Scenes`, `Assets/Prefabs`, `Assets/ScriptableObjects`. Note what exists.
3. **Match each requested feature to the inventory**, and mark it:
   - `EXISTS-WORKING` → do **not** rebuild. Only integrate with it or extend it.
   - `EXISTS-PARTIAL` → complete the missing parts **inside the existing file/class** without changing its existing public methods, field names, or serialized fields.
   - `EXISTS-BROKEN` → fix the bug with the **smallest possible change**; explain the change in the CHANGELOG.
   - `MISSING` → create new files.
4. **Write a short plan** (what will be added/changed, which files) **before** editing.
5. **Make a backup note:** for every existing file you modify, record its path in CHANGELOG ("Modified: path — reason").

### 2.2 Non-destructive coding rules
| Do | Don't |
|---|---|
| Add a new method/class/component | Delete or rewrite an existing method that works |
| Add new optional fields with default values | Rename or remove existing serialized fields (this breaks scenes/prefabs/saves) |
| Use `partial` classes, interfaces, events, or composition to extend | Change public method signatures other code already uses |
| Add new ScriptableObject assets | Overwrite or reset existing ScriptableObject values the user tuned |
| Add a new scene or additive scene | Replace or empty an existing scene |
| Add new save fields with a version bump + migration | Change the save format without migration (players lose progress) |
| Add new prefab variants | Modify the original prefab in a way that changes existing behavior |
| Deprecate with `[Obsolete]` and keep working | Delete "unused-looking" code without checking references |

### 2.3 Protected assets (never overwrite without explicit permission)
- Any file listed under `PROTECTED` in `BUILD_STATUS.md`
- User-made art, audio, animations, scenes, prefabs
- Tuned balance data (ScriptableObjects / JSON)
- Player save files and save schema
- Project settings (Player Settings, URP asset, quality settings) — only add, never reset

### 2.4 Conflict resolution
If the new design conflicts with existing code:
1. Keep the existing behavior working.
2. Add an **adapter** or **wrapper** that bridges old and new.
3. Record the conflict and the chosen solution in `BUILD_STATUS.md → Open Questions`.
4. Only replace old code if the user explicitly says so, and then keep the old version in a `_Deprecated/` folder.

### 2.5 Regression guard
- Before finishing a task: run all existing tests + manual smoke test (menu → mission → result).
- If any previously working feature now fails, **revert your change** and use a safer approach.

---

## 3. PROJECT STATUS TRACKING

Create and maintain these two files in the project root.

### 3.1 `BUILD_STATUS.md` template
```markdown
# BUILD STATUS (update at end of every task)
Last updated: <date>   Engine: Unity <version>, URP

## Systems
| System | Status | Files | Notes |
|---|---|---|---|
| Player controller | EXISTS-WORKING / PARTIAL / BROKEN / MISSING | ... | ... |
| Weapon system | ... | ... | ... |
| Damage system | ... | ... | ... |
| Enemy AI | ... | ... | ... |
| Mission manager | ... | ... | ... |
| Rank/XP | ... | ... | ... |
| Economy | ... | ... | ... |
| Gunsmith | ... | ... | ... |
| Save/Load | ... | ... | ... |
| HUD/Menus | ... | ... | ... |
| Squad AI | ... | ... | ... |
| Drones | ... | ... | ... |
| Story/Dialogue | ... | ... | ... |
| Audio | ... | ... | ... |
| VFX | ... | ... | ... |

## PROTECTED (do not overwrite)
- <path> — <why>

## Open Questions / Conflicts
- ...

## Next Task
- ...
```

### 3.2 `CHANGELOG.md` template
```markdown
## <date> — <task name>
Added:    <new files>
Modified: <existing files> — <what and why, minimal>
Removed:  (should be empty — see rule 1)
Tests:    <what was run, pass/fail>
```

---

## 4. ARCHITECTURE & CONVENTIONS

### 4.1 Layers & tags (physics)
| Layer | Used for |
|---|---|
| `Player` | Player body hitboxes |
| `Enemy` | Enemy body hitboxes |
| `Ally` | Squad hitboxes |
| `Environment` | Walls, ground, props (block bullets) |
| `Cover` | Cover objects (block bullets, enable cover snap) |
| `Glass` / `Destructible` | Penetrable / breakable |
| `Projectile` | Bullets/rockets |
| `Trigger` | Objective zones |

A **bullet raycast mask** includes: `Enemy`, `Ally`, `Player`, `Environment`, `Cover`, `Glass`, `Destructible`. It excludes the shooter itself.

### 4.2 Core interfaces (all gameplay uses these)
```csharp
public interface IDamageable {
    Team Team { get; }
    bool IsAlive { get; }
    void ReceiveDamage(DamageInfo info);   // ONLY DamageSystem calls this
}
public struct DamageInfo {
    public float Amount;
    public HitZone Zone;
    public DamageType Type;      // Bullet, Explosion, Melee, Drone, Fall
    public GameObject Source;    // shooter
    public WeaponData Weapon;    // can be null for explosions
    public Vector3 HitPoint, HitNormal;
}
public enum Team { Player, Ally, Enemy, Neutral }
public enum HitZone { Head, Torso, Limb }
```

### 4.3 Event bus (decoupling)
Systems talk through events, not direct references:
`OnShotFired`, `OnHit`, `OnDamageDealt`, `OnEntityKilled`, `OnReloadStarted/Finished`, `OnWeaponSwapped`, `OnObjectiveCompleted`, `OnMissionStateChanged`, `OnXPGained`, `OnRankUp`, `OnCurrencyChanged`, `OnPlayerDied`.

### 4.4 Folder & naming
Follow the structure in `Valor_Academy_Game_Design.md` Section 12.2. Classes PascalCase, private fields `_camelCase`, ScriptableObjects suffixed `Data` (e.g. `WeaponData`, `RankData`, `MissionData`).

### 4.5 Frame & update rules
- Use `Update` for input, `FixedUpdate` for physics, `LateUpdate` for camera.
- No `Find*`/`GetComponent` in `Update` — cache references.
- Pool bullets, VFX, decals, audio sources.

---

## 5. CORE GAMEPLAY LOGIC

> These are the **mandatory behavior rules**. Each has: *Rule → Logic → Edge cases → Test.*
> Implement them exactly. If a rule seems to conflict with existing code, follow Section 2.4.

---

### 5.1 AIMING

**Rule:** Aiming only changes *where the shot goes*. It never causes damage by itself.

**Logic**
1. **Aim direction** = camera forward through the screen center (crosshair). The crosshair position and the real bullet path must match (convergence — see 5.3).
2. **ADS (aim down sights):**
   - Hold ADS → FOV lerps to weapon's `adsFov` over `adsTime`.
   - Spread multiplier drops to `adsSpreadMult` (e.g. 0.4); sway reduced.
   - Move speed multiplied by `adsMoveMult`.
   - Sprinting is cancelled when ADS starts.
   - ADS is cancelled by: reload start, sprint, weapon swap, death, vault.
3. **Sway:** small idle sway (Perlin noise), reduced when crouched/prone, increased when moving. Sniper scope has breath hold (limited stamina).
4. **Aim assist (optional, mobile):**
   - Only **nudges the aim direction slightly toward a target already near the crosshair** (angle < `assistAngle`) and only if that target is **visible** (line of sight clear).
   - Aim assist **never adds damage, never auto-fires, never hits through walls, never locks onto dead or off-screen targets.**
   - Strength: Off / Low / High (settings). Disabled for snipers at full zoom unless user enables.
5. **Target outlining / crosshair feedback:** crosshair turns red when raycast from the camera hits an enemy hitbox within range — **visual only**.

**Edge cases:** aiming at the sky (no hit), aiming at a teammate (crosshair turns green, firing still allowed but see friendly fire 5.3), aiming through glass.

**Test:** With aim assist ON and an enemy behind a wall inside the assist angle → aim must NOT move toward them.

---

### 5.2 FIRING PIPELINE (can the player shoot?)

**Rule:** A shot is only fired if **every** check passes. Otherwise nothing happens (no ammo used, no damage).

**Checks (in order)**
```
1. Owner IsAlive
2. Weapon is equipped & not currently being swapped (swap time elapsed)
3. Not reloading (unless shell-reload weapon like shotgun & interrupt allowed)
4. Not sprinting / vaulting / climbing / in cutscene / game paused
5. Fire-rate cooldown finished (now >= nextFireTime)
6. Fire mode allows (Single: needs fresh press; Burst: not mid-burst; Auto: held)
7. Ammo in magazine > 0   → if 0: play dry-click, auto-start reload if setting ON
8. Weapon not jammed (WW1 era only)
```

**On success**
```
- magazine -= 1 (burst/auto: per bullet; shotgun: 1 shell = N pellets)
- nextFireTime = now + 60/RPM
- Compute spread + recoil (5.3)
- Spawn shot (hitscan or projectile per weapon type)
- Fire events: muzzle flash, sound, shell eject, OnShotFired
- Alert nearby enemies (noise radius; suppressor reduces it)
```

**Edge cases:** pressing fire and reload on the same frame (reload wins only if mag is empty; otherwise fire wins), swapping at 1 bullet left, game paused mid-burst (resume correctly), dying mid-burst (stop).

**Test:** Empty mag + fire → zero damage, zero ammo change, dry-click once.

---

### 5.3 HIT DETECTION — **DAMAGE ONLY WHEN A SHOT REALLY HITS A TARGET**

**Rule (most important):** Damage is dealt **only** if the bullet path physically intersects a valid, alive, hostile hitbox. Pointing at an enemy, having them in the crosshair, or having them on screen is **not** a hit.

**Hitscan weapons (pistol, rifle, SMG, shotgun pellets)**
```
origin    = camera position (player) / muzzle or eyes (AI)
dir       = aimDirection rotated by (spread + recoil offset)
maxRange  = weapon.range
hit = Physics.Raycast(origin, dir, out info, maxRange, bulletMask)
```
**Convergence fix:** For third-person, raycast from camera center first to find the aim point; then fire the actual bullet **from the muzzle toward that aim point**. If the muzzle→aimPoint ray is blocked by cover earlier (e.g. gun barrel pokes into a wall), that blocking object takes the hit (prevents shooting through a wall corner).

**Projectile weapons (sniper, launcher, drone missile)**
- Bullet has speed, gravity drop, lifetime; moved with sub-step raycasts between frames (**no tunneling**).
- Damage applied on first valid intersection.

**Processing a hit**
```
if (!hit)                              → no damage (play tracer to max range)
else if hit.collider has IDamageable:
      target = hit.collider's IDamageable
      if (!target.IsAlive)             → ignore (no damage, bullet passes or stops per rule)
      if (target == shooter)           → ignore
      if (IsFriendly(shooter, target)) → apply friendly-fire rule (below)
      else                             → build DamageInfo → DamageSystem.Apply()
else (environment):
      spawn impact VFX/decal by surface type
      if material is penetrable (thin wood/glass) AND weapon.penetration > material.thickness:
            continue ray with reduced damage (damage *= penetrationFalloff)
      else stop.
```

**Rules**
| Case | Result |
|---|---|
| Shot hits wall before enemy | Wall takes hit; enemy takes **no** damage |
| Shot hits cover object | Blocked (unless penetrable) |
| Shot misses everything | No damage, tracer continues to range |
| Shot hits dead body | No damage; (optional) ragdoll push impulse only |
| Shot hits enemy beyond weapon range | No damage |
| One bullet hits at most **one** target per ray (unless penetration) | Prevent double damage |
| Shotgun | Each pellet is an independent ray; damage per pellet |
| Friendly fire | Default **OFF** for AI→AI; player→ally: 0 damage + warning UI (Hard mode: ally takes damage and mission can fail if ally dies) |
| Shooter's own hitbox | Always excluded from raycast mask |
| Damage numbers/hitmarker | Shown **only** when `DamageSystem.Apply` actually reduced health |

**Spread & recoil (affects WHERE the bullet goes)**
- `spread` = `baseSpread` × stance × movement × ADS × sustained-fire factor, then random point in cone.
- `recoil` = per-shot kick from weapon's recoil curve (vertical + horizontal), camera rises, recovers over `recoilRecovery` when not firing.
- Both are applied to **direction before the raycast** so misses are natural.

**Edge cases:** shooting at point-blank into a wall (muzzle inside collider), shooting while enemy dies the same frame (second bullet must find `IsAlive == false`), hitting two overlapping hitboxes (take nearest hit), high-latency frame spikes (use time-based sub-steps).

**Tests**
- Enemy behind wall, player fires at them → enemy health unchanged.
- Enemy in open, crosshair on head, fire → health decreases by headshot amount.
- Player fires and misses by 2 m → no damage, no hitmarker.
- Shotgun at 3 m: N pellets hit; at max range: few/none.
- Ally in front of enemy: ray hits ally first → enemy takes no damage.

---

### 5.4 DAMAGE CALCULATION (`DamageSystem` — single entry point)

```
final = weapon.damage
final *= zoneMultiplier[zone]            // Head 2.5, Torso 1.0, Limb 0.7
final *= rangeFalloff(distance)          // 1.0 until falloffStart, linear to minDamageMult at maxRange
final *= ammoTypeMultiplier              // AP, hollow-point, etc.
final *= difficultyMultiplier            // enemy damage vs player on Easy/Normal/Hard
final *= (1 - targetDamageReduction)     // skills / perks
// Armor
if (target.armor > 0 && zone != Head or helmet absent):
      absorbed = min(target.armor, final * armorAbsorbRatio)   // e.g. 0.6
      armor   -= absorbed * (1 - weapon.armorPierce)
      final   -= absorbed * (1 - weapon.armorPierce)
// Helmet reduces headshot multiplier: head mult = max(1, 2.5 - helmetTier*0.5)
final = max(final, 0)
target.health -= final
```

**Rules**
- Clamp: health never below 0, never above max.
- `ReceiveDamage` ignored if target is dead, **invulnerable** (spawn protection 2 s, cutscene, tutorial dummy flag), or the mission is not in `Playing` state.
- Player damage taken ≥ 1 per hit (no 0-damage loops unless friendly fire off).
- Explosion damage uses its own falloff (5.9).
- Fall damage only above threshold height.
- Damage events broadcast `OnDamageDealt(source, target, amount, zone)` once per application.

**Test:** Unit test table of (weapon, zone, distance, armor) → expected damage; headshot must always be ≥ torso for the same shot.

---

### 5.5 HEALTH, DEATH & KILL CREDIT

**Health model**
- `Health` component: `current`, `max`, regen rules.
- Player: **no auto-regen during combat**; slow regen after 5 s without damage up to 30% (Normal), medkits heal fully; Hard: no regen.
- Low health (< 25%): vignette, heartbeat sound, desaturation.

**Death flow (runs exactly once)**
```
if (health <= 0 && !isDead):
    isDead = true
    disable colliders' damage reception, AI, weapon, input
    play death animation / ragdoll (impulse from last hit direction)
    fire OnEntityKilled(victim, killer, weapon, zone)   // exactly once
    drop loot (ammo/cash) for enemies
```
- **Kill credit:** the killer is the `Source` of the last damaging hit. If the source died or is gone, no XP.
- **XP/cash/medal-progress** granted **only** on `OnEntityKilled` with a valid killer on Player team, and **only once per victim** (guard with `isDead` flag).
- Headshot kill → `headshotCount++` for medals.
- Player death → Fail screen: Revive (rewarded ad or medkit token), Retry from last checkpoint, Quit. Revive restores 50% health + 2 s invulnerability, does **not** reset enemies.

**Edge cases:** two bullets kill in the same frame (one kill event only), explosion + bullet simultaneous, enemy dies while reloading/shooting (cancel actions), kill after the mission ended (no XP).

**Test:** Spam damage on an enemy at 1 HP in the same frame → exactly one `OnEntityKilled`, XP granted once.

---

### 5.6 AMMO, RELOAD & WEAPON SWAP

**Ammo model:** `magazine` (loaded) + `reserve` (carried). Total never negative; reserve capped by backpack tier.

**Reload**
```
start: allowed if magazine < magSize AND reserve > 0 AND not already reloading
time:  weapon.reloadTime (tactical, mag not empty) or weapon.emptyReloadTime (mag empty)
finish: need = magSize - magazine; take = min(need, reserve)
        magazine += take; reserve -= take      // ammo is MOVED, never created
cancel: swap weapon / sprint / die / vault → reload aborted, no ammo moved
Shell-by-shell (shotgun/bolt): +1 per shell, can be interrupted by fire
```
- Ammo only transferred at **reload completion** (not at start).
- Pickups: ammo crates add to `reserve` up to cap; excess is wasted and shown as "AMMO FULL".
- Infinite ammo only in Training Range / debug.

**Weapon swap**
- Swap time per weapon; cannot fire during swap; reload state is cancelled; ADS cancelled.
- Weapon remembers its `magazine` & `reserve` (state stored per weapon instance, not global).
- Quick-swap between last two weapons.

**Test:** Reload with 10 reserve, mag 25/30 → mag 30, reserve 5. Cancel reload at 50% → values unchanged.

---

### 5.7 ATTACHMENTS & STAT MODIFIERS

- Each attachment = a set of **modifiers**: `{stat, op(Add|Mul), value}`.
- Final stat = `(base + sum(Add)) * product(Mul)`, **clamped** to min/max defined in `WeaponData`.
- Recompute stats **from base every time** (never accumulate on previous results) → prevents stat drift/duplication.
- One attachment per slot; incompatible pairs blocked (e.g. two optics).
- Suppressor: noise radius ×0.3, damage ×0.95, hides muzzle flash; not available on weapons flagged `noSuppressor`.
- Optic changes `adsFov`, `adsSpreadMult`, reticle sprite.
- Ammo type changes damage/pierce/trail.
- Attachments persist per weapon (saved).

**Test:** Equip then unequip an attachment 100× → stats equal base.

---

### 5.8 COVER SYSTEM

- A **cover point** is an object on layer `Cover` with a defined height (low/high) and lean points.
- **Snap condition:** player within 1.2 m of cover AND facing it (angle < 70°) AND presses Cover/auto-snap setting.
- In cover: movement restricted along the cover edge; crouch if low cover; **peek** = move aim up/out; **blind fire** = shoot with 2× spread, 0.6× damage multiplier modifier on accuracy (not on damage if hits).
- Bullets still obey 5.3: cover **physically blocks** shots; enemies behind cover can be hit only when their exposed hitbox (head/upper body when peeking) is on the ray.
- Leaving cover: moving away or pressing Cover again.
- Destructible cover has HP; when destroyed, player is exposed (auto-exit cover).
- Enemy AI uses the same cover points (claim system: one occupant per slot).

**Test:** Enemy in low cover, player aims at feet-level torso behind cover → blocked; aims at head above cover → hit.

---

### 5.9 GRENADES & EXPLOSIONS

```
Throw: arc preview (line), cook time (fuse 3 s), bounce off surfaces.
Explode: OverlapSphere(position, radius)
For each IDamageable in sphere (unique per entity — not per collider!):
    if no line-of-sight (Raycast from blast center to target's chest blocked by Environment) → damage ×0.25 (or 0 if fully shielded)
    damage = maxDamage * (1 - dist/radius)^1.5        // falloff
    apply via DamageSystem (Type = Explosion), Source = thrower
Apply physics impulse to rigidbodies, camera shake scaled by distance.
```
- **Each entity takes damage once per explosion** (dedupe by entity, not by hitbox).
- Friendly fire follows 5.3 rules; thrower takes self-damage at reduced rate on Hard.
- Smoke grenade: blocks AI vision (not bullets); flash: blinds enemies in view cone for X s; gas (WW1): damage over time unless gas mask equipped.
- Launcher rockets: direct hit = full damage, plus splash.

**Test:** Enemy behind thick wall at 2 m → takes reduced/no damage; two hitboxes of one enemy inside radius → damaged once.

---

### 5.10 ENEMY AI LOGIC

**States:** `Idle/Patrol → Suspicious → Alert → Combat → Flank → TakeCover → Retreat/Heal → Dead`

**Perception**
- **Vision:** cone (e.g. 90°, range 40 m, farther in daylight, shorter in fog/night). Target is *seen* only if inside cone **AND** a line-of-sight raycast to the target's head/chest is not blocked. Smoke blocks vision.
- **Hearing:** gunshots within `noiseRadius` → move to investigate (Suspicious). Suppressed shots have smaller radius.
- **Memory:** keeps last known position for 8 s after losing sight; searches there.
- **Alert propagation:** an enemy that sees the player alerts allies within 20 m (radio delay 1–2 s).

**Combat behavior**
- Pick cover (not already claimed, protects from player's direction, within range), move, shoot in bursts, peek/duck cycles.
- **Enemy attacks obey the same hit rules as the player (5.3):** AI fires a shot with an **accuracy-based spread** (accuracy grows with time-in-sight, decreases with player movement/distance, cover). **AI shots that miss deal NO damage.** There is no "auto-hit" or "damage if in sight".
- First-shot delay (reaction time) = 0.3–0.8 s (by difficulty) so the player is never instantly shot.
- Fire in bursts with pauses; ammo and reloads simulated (AI reloads → vulnerable window).
- If health < 30% and has medic → retreat; if squad lost > 50% → fall back (morale).
- **Flank:** if player has been in cover > N seconds, 1–2 enemies path to a flank point.
- Types: Rifleman, Rusher (no cover, melee), Sniper (laser/glint warns 1.0 s before the shot, one hit shot), Heavy (armor; slower; suppresses), Grenadier (throws when player in same cover > 4 s), Officer (buff aura; kill priority), Medic, Vehicle, Drone, Boss.
- Use NavMesh; stuck detection → re-path or teleport-to-nearest-valid after 5 s out of sight.

**Performance:** max 6–8 active combat AIs; others in cheap states. Stagger AI updates (10 Hz for far).

**Test:** Player stands still in cover out of sight → enemies never deal damage; player in sight but enemy spread misses → no damage; enemy first shot after ≥ reaction time.

---

### 5.11 SQUAD AI LOGIC

- Squad members follow the player (formation), take cover, engage visible enemies, obey commands: **Follow, Hold, Attack target, Cover me, Regroup**.
- Squad members use the same weapon/hit/damage systems (5.3–5.5), same ammo rules (they have limited ammo, resupply from crates).
- Downed state: ally goes "Down" at 0 HP for 20 s; player (or medic ally) revives by holding interact. Bleed-out → dead (permanent for mission; story-critical allies cannot die except at scripted moments).
- Allies **never block the player's shots** unless physically in the line (and then friendly-fire rule applies).
- Max squad size by rank (Design doc Section 4).

---

### 5.12 STEALTH LOGIC

- Enemy detection meter 0–100: fills when player inside vision (faster when close, standing, moving, lit; slower when crouched/dark/behind foliage); empties when out of sight.
- At 100 → Alert (combat). 40–99 → Suspicious (investigate).
- Silent takedown: from behind within 1.5 m, enemy not Alert, melee/suppressed weapon; plays animation; body can be hidden; bodies found by enemies raise alert.
- Alarm: reaching an alarm panel triggers reinforcements — only once per mission.
- Stealth bonus objective: "no alarm".

---

### 5.13 DRONES & SUPPORT

- Unlocked at Colonel (rank 15). Stored as abilities with **charges** and **cooldowns** (data-driven).
- **Recon drone:** marks enemies in a radius for 15 s (outline through walls, **visual only**, no damage); marks removed on death.
- **Strike drone:** player selects a point on the tactical map/aim point → delay 2 s → missile; damage via explosion rules (5.9), Source = player, **cannot be used if no charge/cooldown**, cannot target inside a "no-strike" zone (civilians/allies in radius → warning).
- **Swarm:** N small hits in an area over 3 s; each hit uses explosion rule with small radius.
- **FPV pilot:** player camera switches to drone, body becomes vulnerable (enemies may attack); battery timer; leaving mode restores state exactly.
- Artillery/airstrike/smoke: call via radio → delay → effect; one active call at a time per type.

---

### 5.14 MISSION STATE MACHINE (prevents "stuck after killing 1 enemy")

**States:** `Loading → Briefing → Intro/Cutscene → Playing → (Paused) → Completing → Success | Failed → Results → Exit`

**Rules**
- Only valid transitions allowed (enforced in code; invalid → log error).
- **Objectives** are independent objects with their own state: `Locked → Active → Completed/Failed`. Types: KillAll, KillCount, ReachZone, DefendTimer, Escort, Plant/Defuse, Collect, SurviveWaves, DroneStrike, Stealth.
- Mission completes when **all required objectives** are Completed; optional ones only give bonus stars.
- Mission fails when: player dead (and no revive), protected NPC/asset dies, timer ends (if timed objective), alarm triggered on a stealth-required objective.
- **Wave/Kill-all logic:** count living enemies via a registry (`EnemyRegistry`) updated by `OnEntityKilled` **and** by safe cleanup (despawn). Not by counting scene objects. If an enemy falls out of the map (below kill-plane) → counted as dead (`kill-plane` triggers `Die` with no XP) so it can't block progress.
- **Watchdog:** if no objective progress and no active enemies/targets for 60 s → show hint arrow; at 120 s with no valid targets → auto-complete the stuck objective (log a warning).
- **Checkpoints:** saved at objective completion; Retry restores to last checkpoint (player health, ammo, objectives, enemy states summarised).
- **Results:** stars computed from data (time, accuracy = hits/shots, damage taken, bonus objectives); XP and cash granted **once** (guard flag), then saved.

**Test:** Automated bot completes every mission; kill enemies in random order including falling off map; mission always ends.

---

### 5.15 TOURNAMENT / TIMED CHALLENGES

- Timer start only **after** the 3-2-1 countdown finishes.
- Time limit = `baseTime * difficultyScale[rank]`; **first tournament must be passable by a new player** (playtest target: 80% pass rate).
- Targets: each has a hit counter; score = hits × multiplier (headshot/center bonus); targets cannot spawn off-screen or behind obstructions.
- Pass thresholds: Bronze/Silver/Gold; failing allows immediate retry; never blocks story progression (tournaments are optional side content giving bonus rewards).
- Pause stops the timer.

---

### 5.16 XP, RANKS & UNLOCKS (`ProgressionService`)

**XP sources** (data-driven table)
| Source | XP |
|---|---|
| Enemy kill (by type) | 10–100 |
| Headshot bonus | +5 |
| Objective complete | by mission data |
| Mission complete | by mission data × star multiplier |
| Bonus objective | +50–200 |
| Training drill pass | by drill |
| First-time mission bonus | +25% |

**Rules**
- XP added only via `ProgressionService.AddXP(amount, reason)`; never negative; logged with reason.
- On XP change: `while (xp >= nextRankXP) rankUp()` (handles multiple ranks at once); each rank-up fires `OnRankUp(newRank)` once, grants skill point + unlock rewards, queues the Rank-Up screen (shown at Results, not mid-combat).
- Unlock gating uses `player.rank >= item.unlockRank` checked in **one place** (`UnlockService.IsUnlocked`); locked items show their required rank in UI.
- Rank is derived from XP (single source of truth); stored XP is saved, rank is recomputed on load.
- XP not granted: friendly kills, kills after mission end, kills from debug/god-mode, repeated farming of the same respawned enemy (respawn-farm protection: no XP from respawns).
- Prestige (New Game+) resets XP/rank progress but keeps cosmetics/mastery; increments prestige counter.

**Test:** Add 10,000 XP at once from rank 1 → ranks up through correct ranks, rewards granted once each.

---

### 5.17 ECONOMY & SHOP (`EconomyService`)

- Currencies: `Cash`, `Steel`, `Parts`, `Medals`.
- All changes through `Spend(currency, amount, reason)` / `Earn(...)`.
- `Spend` is **atomic**: checks balance ≥ cost, then deducts, then grants the item, then saves; if any step fails → rollback, no charge.
- No negative balances; no int overflow (clamp at max).
- Purchases blocked if rank locked, already owned (non-consumable), or inventory full.
- Rewarded-ad rewards granted only on the ad **completion callback**; if the ad fails → nothing granted, message shown.
- Prices & drop rates in data tables (no hard-coded).
- Duplicate-purchase guard: ignore taps while a transaction is processing.

**Test:** Spend with insufficient funds → nothing changes; double-tap buy → one purchase only.

---

### 5.18 GUNSMITH / CRAFTING

- Craft flow: select base weapon → select slot → choose part → **cost check** → confirm → deduct resources → part added to inventory → equip.
- Blueprint pieces: collect N pieces → unlock blueprint → can craft that weapon/part.
- Weapon upgrade levels 1–10: each level adds fixed % to damage/handling; cost table; max level caps; cannot skip levels.
- Live stat-bar comparison shows current vs new (using 5.7 recompute) — **preview does not change real stats until confirmed**.
- Skins are cosmetic only; **no stat changes**.
- Equipped attachments saved per weapon; selling/dismantling returns 50% resources (not equipped parts).

---

### 5.19 SAVE SYSTEM

- Save file JSON with `saveVersion`, `lastSaved`, player profile, rank/XP, currencies, unlocked items, weapon states (level, attachments), mission progress/stars, settings, story choices, medals.
- **Atomic write:** write to `save.tmp` → verify (re-read & parse) → replace `save.json` → keep `save.bak` (previous good).
- On load: if `save.json` invalid → try `save.bak` → else new profile (never crash).
- **Migration:** if `saveVersion < current`, run sequential migrations (v1→v2→...). Never delete unknown fields.
- Autosave: on mission complete, purchase, rank-up, settings change, app pause/quit.
- Mid-mission checkpoint saved separately (`checkpoint.json`), cleared on mission end.
- Optional cloud save (later).

**Test:** Corrupt save file → game loads backup; old-version save loads and migrates without data loss.

---

### 5.20 STORY & CHOICES

- Story state: `currentAct`, `completedBeats`, `choices{}`, `meters{loyalty, diplomacy, aggression}`.
- Beats trigger from mission completion; each beat plays **once** (flag saved).
- Choices only recorded when the player confirms; timed choices default to the neutral option.
- Ending chosen at final mission from meters + key flags (Design doc Section 3.2); all endings reachable (test with meter presets).
- Skip allowed for cutscenes after first view; dialogue history log.
- Critical allies (Brooks, Lena, Vance) can only be killed/saved via the scripted choice logic, not by random stray bullets (`StoryCritical` flag blocks lethal damage outside scripted beats).

---

### 5.21 UI / HUD RULES

- HUD shows only truth from game state: health, armor, ammo (mag/reserve), weapon name, fire mode, grenade count, objectives, minimap, compass, damage direction indicator (only on real damage taken), hit marker (only on real damage dealt; different for headshot/kill).
- Buttons are disabled (greyed) when the action isn't allowed (e.g. reload when full/no reserve).
- No UI blocks the center of the screen; layout editable and saved.
- Rank-up, medal, and level-end popups queue and show at safe moments (never in combat).
- Settings apply immediately and persist.
- All text through a localization table (no hard-coded strings).

---

### 5.22 ADS & MONETIZATION RULES

- **No ad ever plays during gameplay or cutscenes.**
- Allowed ad points: (a) player taps "Watch ad for 2× reward" on the Results screen; (b) player taps "Revive"; (c) player taps "Free crate"; optional interstitial only **between** missions, no more than once per 3 missions and never right after a Fail.
- If no ad is available → hide/disable the button (no dead buttons).
- "Remove Ads" purchase disables interstitials permanently (saved + restored on reinstall).
- No pay-to-win: everything purchasable has an in-game earning path.

---

## 6. PHASED IMPLEMENTATION PLAN

> For each milestone: **Inventory first (Section 2)**, implement, run acceptance tests, update `BUILD_STATUS.md` + `CHANGELOG.md`.
> If a listed item already exists (`EXISTS-WORKING`), skip it and just integrate.

### Phase 0 — Project audit & setup (always first)
**Tasks**
- Scan the project; fill `BUILD_STATUS.md` with what exists.
- Create missing folders (Section 4.4), layers/tags (4.1), assembly definitions for tests.
- Create core interfaces and EventBus (4.2–4.3) **if missing**.
- Set up version control (`.gitignore` for Unity), commit baseline.
**Acceptance:** project compiles; status file accurate; nothing existing modified except additive setup.

### Phase 1 — Player & camera (M1)
**Files (new unless exist):** `PlayerController`, `PlayerInput` (mobile + gamepad), `ThirdPersonCamera`, `PlayerAnimator`.
**Tasks:** move/sprint/crouch/jump/vault, camera follow with collision, ADS FOV change, touch UI (joystick + look drag).
**Logic to include:** 5.1 (ADS rules), 5.2 checks 1,4.
**Acceptance:** smooth 60 FPS movement, no camera clipping, controls remappable.

### Phase 2 — Weapons & Damage Core (M2) ⭐ most critical
**Files:** `WeaponData` (SO), `Weapon`, `WeaponController`, `RecoilSystem`, `SpreadSystem`, `Projectile`, `HitDetection`, `DamageSystem`, `Health`, `HitboxZone`, `DamageInfo`, `AmmoSystem`.
**Tasks**
1. WeaponData with all stats (Design 5.2).
2. Firing pipeline checks (5.2).
3. Hitscan + projectile hit detection with muzzle/camera convergence (5.3).
4. Hitbox zones (head/torso/limb) on characters.
5. DamageSystem formula (5.4), health/death (5.5).
6. Ammo/reload/swap (5.6).
7. Test dummies and a shooting range scene.
**Acceptance (all must pass):**
- [ ] Damage only on real hit; wall blocks; miss = 0 damage
- [ ] Headshot > torso > limb
- [ ] Empty mag does nothing; reload math correct; cancel safe
- [ ] Dead target can't be damaged; exactly one kill event
- [ ] Unit tests for damage table and reload pass

### Phase 3 — Enemy AI (M2b)
**Files:** `EnemyAI`, `EnemyStateMachine`, `Perception`, `CoverManager`, `EnemyData` (SO), `EnemyRegistry`.
**Tasks:** states and perception (5.10), cover claiming, accuracy-based shooting using the SAME `HitDetection`, alert propagation, types: Rifleman, Rusher, Sniper, Heavy.
**Acceptance:** enemies can't see through walls; AI misses sometimes; reaction delay works; no AI stuck; 8 active AIs ≥ 45 FPS on mid device.

### Phase 4 — Mission system & HUD (M2c)
**Files:** `MissionManager`, `MissionStateMachine`, `Objective` classes, `MissionData` (SO), `ResultScreen`, `HUD`, `PauseMenu`, `CheckpointSystem`.
**Tasks:** state machine (5.14), objectives, stars, results, watchdog, HUD (5.21).
**Acceptance:** bot completes test missions; no stuck states; XP/cash granted once.

### Phase 5 — Boot Camp / Act 1 (M3)
**Tasks:** Academy hub scene, obstacle course, shooting range drills, tournament (5.15), tutorial prompts, cover system (5.8), grenades (5.9), first story beats (5.20), 6–8 missions.
**Acceptance:** a new player can finish Act 1 without help; first tournament passable; tutorial skippable.

### Phase 6 — Progression & economy (M4)
**Files:** `ProgressionService`, `RankData` (20 ranks), `UnlockService`, `EconomyService`, `SkillTree`, `MedalService`, `SaveSystem`.
**Tasks:** 5.16, 5.17, 5.19; rank-up UI, medals, skill points.
**Acceptance:** rank math tests; atomic purchase tests; corrupt-save recovery; migration test.

### Phase 7 — Gunsmith (M5)
**Files:** `AttachmentData`, `WeaponStatCalculator`, `GunsmithUI`, `CraftingService`, `BlueprintService`.
**Tasks:** 5.7 and 5.18; 3D rotating preview; stat-bars; camo skins.
**Acceptance:** stat recompute never drifts; crafting costs correct; preview doesn't change live stats.

### Phase 8 — Acts 2–3 (M6)
**Tasks:** WW1 trench environment (mud, fog), bolt-action/jam mechanic, gas grenades + gas mask, squad AI (5.11), stealth (5.12), escort/train missions, boss 1 & 2, officer rank content.
**Acceptance:** 15–20 missions playable; bosses beatable on all difficulties.

### Phase 9 — Acts 4–5 (M7)
**Tasks:** beach landing, city fights, vehicles (jeep/tank gunner), night vision/thermal, snow/desert, sniper boss, betrayal story sequence.
**Acceptance:** vehicle controls stable; night vision performance OK.

### Phase 10 — Act 6 & Drones (M8)
**Tasks:** ARIA, drones (5.13), tactical map, final boss, three endings (5.20), medal ceremony cutscene.
**Acceptance:** all endings reachable; drone rules enforced (charges, cooldown, no-strike).

### Phase 11 — Replay modes (M9)
**Tasks:** Survival waves, Daily Operation (seed = date), Weekly Challenge modifiers, New Game+/Prestige, Boss Rush, leaderboards (local first).
**Acceptance:** deterministic daily seed; prestige doesn't break save; respawn-farm protection works.

### Phase 12 — Graphics & audio polish (M10)
**Tasks:** PBR materials, lighting bake, post-processing, VFX, weather, animations, audio layering, quality presets (Design 11).
**Acceptance:** Visual target met on High; Low preset stable 30 FPS.

### Phase 13 — Optimization & QA (M11)
**Tasks:** Section 8 budget, profiling, memory leak checks, crash reporting, localization, accessibility, full regression run.
**Acceptance:** all tests green; 2-hour soak test with no crash.

### Phase 14 — Release prep (M12)
**Tasks:** Play Store assets, privacy policy, data safety form, age rating, ads consent, release build (AAB), signing.
**Acceptance:** internal testing track build passes pre-launch report.

---

## 7. TESTING PLAN

### 7.1 Unit tests (EditMode) — pure logic
| Area | Tests |
|---|---|
| DamageSystem | zone mult, falloff, armor, helmet, invulnerable, dead target, clamp |
| Ammo | reload math, cancel, pickup caps, swap keeps state |
| Attachments | modifier order, recompute from base, clamp, incompatibility |
| Progression | multi-rank-up, no negative XP, rewards once, prestige |
| Economy | atomic spend, rollback, double-tap guard |
| Save | write/read, corruption fallback, migration chain |
| Mission FSM | valid/invalid transitions, objective completion, fail conditions, watchdog |
| Enemy registry | counts correct after kill/despawn/fall |

### 7.2 PlayMode tests (scene-based)
- Range scene: fire at dummies through wall / in open / at ally / at dead body.
- Explosion through wall; same entity double-hit dedupe.
- Enemy sees player only with LOS; AI miss rate within target band.
- Mission complete bot; random-order kills.
- Full flow: menu → mission → result → rank-up → shop → new mission.

### 7.3 Manual QA checklist
- [ ] Touch controls feel good on small & large phones
- [ ] Aim assist Off/Low/High behave per 5.1
- [ ] Hitmarker only on real damage
- [ ] Friendly fire behaves per difficulty
- [ ] No forced ads in gameplay
- [ ] Save survives force-close during a purchase
- [ ] Rotation/interruptions (call, notification) pause properly
- [ ] Low-battery & low-memory devices

### 7.4 Regression from original game's known bugs
| Original bug | Our test |
|---|---|
| Round freezes after killing 1 enemy | Kill-all objective with enemies falling off map / dying simultaneously |
| First tournament impossible | Tournament pass-rate playtest ≥ 80%, timer scaling test |
| Hard to focus on targets | Aim assist tests, reticle visibility on all backgrounds |
| Frequent lag | Profiler budget test on low-end device |
| Ads during gameplay | Automated check: no ad call in `Playing` state |

---

## 8. PERFORMANCE PLAN

| Budget | Low | Medium | High |
|---|---|---|---|
| FPS | 30 | 60 | 60 |
| Draw calls | < 100 | < 150 | < 250 |
| Triangles in view | < 250k | < 400k | < 800k |
| Active AI in combat | 4 | 6 | 8 |
| Texture memory | < 400 MB | < 700 MB | < 1.2 GB |
| Shadows | off / 1 cascade | 2 cascades | 3–4 cascades |
| Render scale | 0.7 | 0.85 | 1.0 |

Techniques: pooling, GPU instancing, static batching, occlusion culling, LOD groups, ASTC textures, baked lighting, AI tick rate scaling, audio source limits, avoid GC allocations in `Update` (no LINQ/string concat), async scene loading.

---

## 9. EDGE CASES & KNOWN-BUG PREVENTION

| Situation | Required behavior |
|---|---|
| Two bullets kill same frame | One kill event, one XP |
| Enemy falls through map | Kill-plane kills it, objective unblocks |
| Player pauses mid-reload | Reload timer pauses |
| App backgrounded mid-mission | Auto-pause; resume exact state |
| Reward ad closes early | No reward |
| Save during death | Save consistent state (no half-dead) |
| Rank-up mid-combat | Popup queued to Results |
| Shooting while swapped weapon animation | No shot |
| Muzzle inside wall | Wall takes the hit |
| Ragdoll blocks door/path | Ragdoll collision layer excludes player nav |
| Last enemy stuck unreachable | Watchdog hint → auto-complete |
| Explosion hits multi-collider enemy | Damage once |
| Healing at full HP | Item not consumed |
| Ammo pickup at full reserve | Not consumed |
| Language/font missing glyphs | Fallback font |
| Controller + touch both connected | Last-used input wins |

---

## 10. DEBUG TOOLS (editor/dev builds only, stripped from release)

- On-screen debug panel: FPS, draw calls, active AI, current mission state, objective list.
- Cheats: god mode (marks XP as "debug", **doesn't grant XP**), infinite ammo, skip objective, add XP/currency, set rank, spawn enemy, teleport to checkpoint, slow-motion.
- Gizmos: bullet rays (hit/miss colors), AI vision cones, hearing radius, cover points, nav paths, hitbox zones.
- Log categories: `[Fire] [Hit] [Damage] [Kill] [AI] [Mission] [XP] [Economy] [Save]` — each rejection logs its reason.
- Debug builds only: **`#if UNITY_EDITOR || DEVELOPMENT_BUILD`**.

---

## 11. PROMPT TEMPLATES FOR EACH SESSION

### 11.1 Universal prefix (paste before EVERY task)
> Read `Valor_Academy_Game_Design.md`, `Valor_Academy_Implementation_Plan.md`, `BUILD_STATUS.md` and `CHANGELOG.md`.
> **Do not replace or rewrite any existing working file, class, scene, prefab, or data asset.** First scan the project and tell me what already exists for this task. Only add new files or make minimal additive changes to existing ones. Keep existing public APIs and serialized fields unchanged. Follow the logic rules in Section 5 exactly. When finished, run the tests, update `BUILD_STATUS.md` and `CHANGELOG.md`, and summarize what you added/changed.

### 11.2 Task prompts

**Weapons & damage**
> Implement Phase 2 (weapons & damage core). Check what exists first. Follow 5.2, 5.3, 5.4, 5.5, 5.6 exactly: damage only on a real raycast/projectile hit; walls and cover block shots; misses do nothing; one kill event per death. Build a shooting-range test scene and unit tests listed in 7.1.

**Enemy AI**
> Implement Phase 3. Enemies must only see the player with clear line of sight, use the same hit detection and damage system as the player (their missed shots do no damage), use cover, and respect reaction delay. Follow 5.10.

**Missions**
> Implement Phase 4 mission state machine per 5.14, including kill-plane handling, watchdog, checkpoints and single-grant rewards. Add a bot test that completes every mission.

**Progression & economy & save**
> Implement Phase 6 per 5.16, 5.17, 5.19 with unit tests (multi-rank-up, atomic purchase, corrupt-save recovery, migration).

**Gunsmith**
> Implement Phase 7 per 5.7 and 5.18. Stats must always be recomputed from base; preview must not alter real stats.

**Bug-fix template**
> Bug: <describe>. Find the root cause first. Make the smallest possible fix without changing unrelated systems or public APIs. Add a regression test. Log it in CHANGELOG.

**Review template**
> Review the last change against Sections 1, 2 and 5 of the implementation plan. List any rule violations, any replaced/deleted existing code, and any duplicated logic (damage/XP/currency) and fix them.

---

## 12. DEFINITION OF DONE

A task is done only when **all** are true:
- [ ] Existing working features were not replaced or broken (Section 2)
- [ ] Logic rules in Section 5 for this feature are implemented and tested
- [ ] Damage / XP / currency go through their single services only
- [ ] No hard-coded balance numbers (data-driven)
- [ ] Zero compile errors/warnings that matter; unit + PlayMode tests pass
- [ ] Main Menu → Mission → Result flow still works
- [ ] Performance still within budget (Section 8)
- [ ] `BUILD_STATUS.md` and `CHANGELOG.md` updated
- [ ] Debug-only code is excluded from release builds

---

*End of Implementation Plan. Start with Phase 0 (audit) and use the Universal Prefix in 11.1 for every task.*
