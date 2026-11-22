# VALOR ACADEMY — Rise of the Commander
### Enhanced 3D Military Game — Complete Design Document (for vibe-coding)

> **Working title:** Valor Academy: Rise of the Commander
> **Inspired by:** *Military Academy 3D* (Google Play, by Coco Play / TabTale, published by CrazyLabs)
> **Goal:** Same core fantasy (recruit → commander → Medal of Honor) but with **realistic 3D graphics, a deeper story, real gun customization, a proper rank system, and real replay value.**
> **How to use this file:** Put it in the root of your project folder. Tell your AI coding tool: *"Read Valor_Academy_Game_Design.md and build the game one milestone at a time."*

---

## 0. QUICK SUMMARY (read this first)

| Item | Detail |
|---|---|
| Genre | 3D military action / simulation, single player, offline |
| Camera | Third-person over-the-shoulder (main) + first-person scope/aim mode |
| Platform | Android first (phone), later iOS / PC |
| Style | Realistic PBR 3D (not cartoon), cinematic lighting |
| Core loop | Train → Mission → Earn XP/Money → Rank up → Unlock/Upgrade guns → Harder mission |
| Story | A recruit rises through two world-war-inspired eras and a modern drone era to become Commander and earn the Medal of Honor |
| Replay value | New Game+, daily ops, endless Survival, weekly challenges, rank mastery |
| Monetization | Optional rewarded ads only (no forced mid-level ads), one-time "Remove Ads + Starter Pack" |

---

## 1. WHAT THE ORIGINAL GAME DOES (research notes)

From the Google Play listing and player reviews:

**Features of the original**
- Player joins a military academy, trains, and rises through ranks to US Army commander (reviews say it goes "Private → President").
- Story is set against two World Wars; later gets modern tech such as **drone strikes**.
- Training challenges, combat scenarios, tournaments (target shooting), missions on different terrains (cities, seas).
- Medals and experience per mission; alliances/teamwork are mentioned in the description.
- Single player, offline, simulation/casual, stylized graphics, 12+ rating (moderate violence), 5M+ downloads, 4.4★.

**What players complain about (we will FIX these)**
| Problem in original | Our fix |
|---|---|
| Too many ads, even mid-mission | Rewarded ads only, never interrupt gameplay |
| Game lags often | Strict performance budget + LOD + quality presets (Section 12) |
| Bugs: a round freezes after killing 1 enemy; first tournament impossible in time | Proper wave/objective state machine + automated tests; tuned timers with difficulty scaling |
| Gameplay dull / repetitive | Varied mission types (Section 7) |
| No replay value after finishing | New Game+, Survival, Daily Ops, ranked leaderboards, challenge modifiers |
| Hard to aim at some targets | Aim assist, adjustable sensitivity, scope zoom, gyro aiming option |
| Stylized low-detail look | Realistic PBR graphics, cinematic lighting, animation, sound |

---

## 2. GAME PILLARS

1. **Earn your rank** — progression feels real; every rank changes what you can do.
2. **Weapons feel real** — recoil, sound, reload animations, attachments, ballistics-lite.
3. **Cinematic realism** — dust, smoke, weather, golden-hour lighting, camera shake.
4. **Story with heart** — squadmates, a rival, a mentor, real choices.
5. **Respect the player** — fair difficulty, no cheap ads, no bugs that block progress.

---

## 3. STORY

> All characters are fictional. Eras are inspired by real history but the story is original.

### 3.1 Main characters
| Character | Role | Notes |
|---|---|---|
| **Cadet Jack "Ace" Mercer** (player, name changeable) | Protagonist | Farm-town kid, joins the academy. Player picks face, skin tone, hair, voice. |
| **Drill Sergeant Walter "Iron" Brooks** | Mentor | Tough, fair, trains you. His fate is a major emotional beat. |
| **Cadet Lena Hartwell** | Squadmate / sniper | Calm, precise; best friend. |
| **Cadet Marcus Vance** | Rival | Arrogant, wealthy family. Starts as rival, becomes ally or enemy based on player choices. |
| **General Eleanor Whitfield** | Commanding officer | Strategist, gives high-level missions. |
| **"The Warden" (Col. Dmitri Kessler)** | Main antagonist | Ruthless enemy commander who appears in every era (different ages / allies). |
| **ARIA** | AI drone system (modern era) | Voice of the drone/tech interface. |

### 3.2 Story structure (6 Acts)

**Act 1 — Boot Camp (Rank: Recruit → Private)**
- Jack arrives at *Fort Valor Academy*. Meets Brooks, Lena, Vance.
- Tutorial missions: obstacle course, rifle range, grenade range, first-aid, radio.
- Ends with a graduation exercise where Vance cheats; player chooses to report him or stay silent (affects rivalry arc).

**Act 2 — The Great War era (Rank: Private → Sergeant)**
- Trench warfare, mud, fog, gas-alert sequences, bolt-action rifles, early machine guns.
- Squad is deployed to a front-line sector. Brooks is wounded saving the squad.
- First meeting with Kessler (young officer of the opposing force).
- Boss mission: breakthrough of a fortified trench line.

**Act 3 — Interwar & Rising Storm (Rank: Sergeant → Lieutenant)**
- Jack is selected for Officer Candidate School. Learns leadership, commands small units.
- Spy / stealth missions, escort missions, a train-heist chase.
- Vance decision pays off (ally helps / rival sabotages).

**Act 4 — World War era (Rank: Lieutenant → Major)**
- Beach landing, forest ambush, city street fighting, tank support, air raid defense.
- Lena makes a sacrifice play OR survives based on player's choices.
- Kessler returns as a seasoned general.
- Boss mission: assault on a fortress bunker; **Medal of Honor nomination** event.

**Act 5 — Cold Shadows (Rank: Major → Colonel)**
- Desert and snow missions, helicopters, night vision, thermal scopes.
- Betrayal: someone inside the command leaks plans. Player investigates (light detective / choice sequence).

**Act 6 — Drone Age & Final Stand (Rank: Colonel → General / Commander)**
- ARIA introduced; player calls drone strikes and manages a squad of soldiers + drones.
- Kessler's final plan: seize a drone network. Final mission = multi-stage infiltration + boss duel.
- **Ending:** Player receives the **Medal of Honor**. Three endings:
  1. **Soldier's Honor** (high loyalty): stays in command, becomes Chief of Staff.
  2. **Statesman** (high diplomacy): enters public life (optional "road to President" epilogue like the original).
  3. **Lone Wolf** (high risk choices): retires as a legend, secret mission after credits.

### 3.3 Story delivery
- Cinematic cutscenes (in-engine, 30–60 s each), voiced dialogue, subtitles.
- Mission briefings with a 3D map table.
- Letters from home between acts (emotional, short).
- Dialogue choices (2–3 options) affect: **Loyalty, Diplomacy, Aggression** meters.

---

## 4. RANK SYSTEM

XP is earned from missions, kills, headshots, objectives, bonus goals, and training.
Each rank has a **title, XP needed, unlock reward, and badge** (3D rank insignia shown on uniform shoulder).

| # | Rank | XP needed (cumulative) | Unlocks |
|---|---|---|---|
| 1 | Recruit | 0 | Basic rifle, boot-camp training |
| 2 | Private | 500 | Sidearm slot, grenades |
| 3 | Private First Class | 1,500 | First gun attachment slot (optic) |
| 4 | Corporal | 3,500 | Armor vest upgrade, SMG |
| 5 | Sergeant | 7,000 | Squad command (1 AI teammate) |
| 6 | Staff Sergeant | 12,000 | Second attachment slot (grip/barrel) |
| 7 | Sergeant First Class | 20,000 | Machine gun, medic kit |
| 8 | Master Sergeant | 32,000 | Sniper rifle, camo skins |
| 9 | Sergeant Major | 48,000 | Squad of 2 AI, radio support (smoke) |
| 10 | Second Lieutenant | 70,000 | Officer uniform, mission planning table |
| 11 | First Lieutenant | 100,000 | Artillery strike call-in |
| 12 | Captain | 140,000 | Vehicle driving missions |
| 13 | Major | 190,000 | Night vision / thermal gear |
| 14 | Lieutenant Colonel | 250,000 | Squad of 3 AI, advanced attachments |
| 15 | Colonel | 330,000 | **Drone strike** unlocked (ARIA) |
| 16 | Brigadier General | 430,000 | Multi-drone swarm |
| 17 | Major General | 550,000 | Elite gear, legendary skins |
| 18 | Lieutenant General | 700,000 | Command whole battle (RTS-lite mini-missions) |
| 19 | General (Commander) | 900,000 | Final campaign missions |
| 20 | **Medal of Honor Holder** | Complete story | New Game+, prestige mode |

**Prestige:** After finishing, player can "Re-enlist" (New Game+) with a Prestige star, keeps cosmetic unlocks, enemies are tougher.

### 4.1 Medals & ribbons
| Medal | How to earn |
|---|---|
| Marksman Badge | 50 headshots |
| Sharpshooter Badge | 200 headshots |
| Expert Badge | 1,000 headshots |
| Purple Heart | Survive a mission with <10% health |
| Bronze Star | Complete all bonus objectives in an Act |
| Silver Star | Complete all missions of an Act on Hard |
| Distinguished Service Cross | Beat a boss without taking damage |
| **Medal of Honor** | Story finale |

---

## 5. WEAPONS & GUN SYSTEM

> Weapon categories use real-world-inspired designs for realism. Names can be generic/original to avoid trademark problems (e.g., "M-Rifle Mk1" instead of a brand name).

### 5.1 Weapon categories & examples
| Category | Era | Example (original-style names) | Role |
|---|---|---|---|
| Pistol | All | Service Pistol 1911-style, Revolver, Modern 9mm | Backup, fast draw |
| Bolt-action rifle | WW1 | Bolt Rifle "Pioneer" | High damage, slow fire |
| Semi-auto rifle | WW2 | Battle Rifle "Liberty" | Balanced |
| Assault rifle | Cold War / Modern | AR "Vanguard", Carbine "Raptor" | All-rounder |
| SMG | WW2 / Modern | SMG "Hornet" | Close range, fast |
| LMG | WW1–Modern | Machine Gun "Anvil" | Suppression |
| Shotgun | All | Pump Shotgun "Breaker" | Very close range |
| Sniper | All | Sniper "Longwatch", Anti-material "Titan" | Long range, one-shot |
| Launcher | Modern | Rocket Launcher "Hammer" | Vehicles, bunkers |
| Grenades | All | Frag, Smoke, Flash, Gas (WW1) | Utility |
| Melee | All | Trench knife, combat knife | Silent kill |

### 5.2 Weapon stats (every gun has these)
- **Damage**, **Fire rate (RPM)**, **Range**, **Accuracy**, **Recoil (vertical/horizontal)**, **Reload time**, **Magazine size**, **Mobility (move speed while aiming)**, **Penetration** (through thin cover).
- Stats shown as bars in the Armory UI + numeric on tap.

### 5.3 "GUN MAKE" — Gunsmith / Workshop system (in-game crafting)
Players build and upgrade guns at the **Armory Workshop** (3D bench where the gun rotates and parts snap on).

**Steps**
1. **Pick a base weapon** (unlocked by rank).
2. **Blueprints:** Collect blueprint pieces from missions/bosses to unlock new gun variants.
3. **Craft parts:** Spend **Steel**, **Parts**, and **Cash** (resources found in missions) to craft attachments.
4. **Attach parts** to slots (slots unlock by rank):
   | Slot | Options | Effect |
   |---|---|---|
   | Optic | Iron sights, Red-dot, 4x scope, 8x scope, Thermal | Aim zoom, accuracy |
   | Muzzle | Suppressor, Compensator, Flash hider | Noise, recoil, flash |
   | Barrel | Short, Long, Heavy | Range vs mobility |
   | Underbarrel | Foregrip, Bipod, Grenade launcher | Recoil, stability |
   | Magazine | Extended, Fast-mag, Drum | Ammo, reload |
   | Stock | Light, Heavy, Adjustable | Handling, recoil |
   | Ammo type | Standard, Armor-piercing, Hollow-point, Tracer | Damage type |
5. **Upgrade levels:** Each gun Lv1–Lv10 (damage/handling bonus), costs scale up.
6. **Skins / camos:** Desert, Woodland, Arctic, Gold, Prestige skins (cosmetic only).
7. **Weapon mastery:** Kills with a gun → mastery XP → unlock special attachments/skins.

### 5.4 How guns are USED in gameplay
- **Aim:** Hold ADS button → camera zooms, sway reduces.
- **Fire modes:** Single / Burst / Auto (if gun supports).
- **Recoil pattern:** Each gun has a recoil curve; sustained fire climbs and drifts.
- **Bullet physics-lite:** Bullet travel time + drop on snipers; hitscan on close weapons for mobile performance.
- **Hit zones:** Head (x2.5), Torso (x1), Limbs (x0.7).
- **Cover system:** Press near wall/sandbags → take cover, peek, blind-fire.
- **Reload:** Realistic animation (tactical vs empty reload); can cancel.
- **Weapon swap:** Tap to swap primary/secondary/grenade.
- **Suppressors:** Reduce enemy alert radius in stealth missions.
- **Ammo:** Limited reserve, pick up from crates/fallen enemies.
- **Weapon jams (WW1 era only, optional):** Mud/dirt can jam old guns → clear with tap (adds era flavor).

### 5.5 Gear & equipment
| Item | Function |
|---|---|
| Helmet (tiers) | Reduces headshot damage |
| Body armor (tiers) | Absorbs damage, breaks after limit |
| Medkit / bandage | Heal |
| Binoculars | Mark enemies |
| Night vision / thermal | Dark missions |
| Radio | Call smoke, artillery, drone |
| Gas mask | WW1 gas sequences |
| Backpack | More ammo/grenade carry |

---

## 6. DRONES & SUPPORT (Colonel rank and above)

- **ARIA Recon Drone:** Marks enemies through walls for 15 s.
- **Strike Drone:** Player aims on tactical map; drone fires precision missile (cooldown 60 s).
- **Swarm Drones (Brig. General):** Multiple small hits in an area.
- **Defense Drone:** Hovers near squad, shoots incoming rockets.
- **Pilot mode:** Player can briefly control a drone camera (first-person FPV) for special missions.
- Limited battery/charges per mission to keep balance.

Other support: smoke, mortar, artillery, airstrike (era-dependent), medic call.

---

## 7. GAMEPLAY MODES & MISSION TYPES

### 7.1 Campaign (main story)
~60 missions over 6 Acts, 5–12 minutes each. Mission types to avoid repetition:
1. **Assault** — clear an enemy position.
2. **Defend** — hold a point against waves.
3. **Sniper overwatch** — protect allies from a rooftop.
4. **Stealth infiltration** — silent takedowns, avoid alarms.
5. **Escort / Convoy** — protect a VIP or vehicle.
6. **Rescue** — extract hostages / wounded.
7. **Vehicle** — drive jeep/tank/boat sequences.
8. **Rail shooter** — gunner seat on truck/boat/helicopter.
9. **Demolition** — plant charges, defuse.
10. **Boss fight** — enemy commander with unique mechanics.
11. **Drone command** — direct strikes from the map (late game).

### 7.2 Training Academy (hub)
Always available to practice and earn bonus XP:
- Shooting range (accuracy & speed drills)
- **Tournaments** (target shooting — **fair timer**, scaled to rank; first tournament is generous, no impossible timers)
- Obstacle course (timed)
- Tactics classroom (quiz minigame, gives intel bonus)
- Gym (stat boosts: stamina, steadiness, strength)

### 7.3 Replay modes
- **Survival / Endless waves** — leaderboards.
- **Daily Operation** — one special mission per day with fixed loadout.
- **Weekly Challenge** — modifiers (headshots only, one-hit-kill, low ammo).
- **New Game+ / Prestige** — keep skins, harder enemies, new dialogue.
- **Boss Rush** — all bosses back to back.
- **Mission Mastery** — 3 stars per mission (time, accuracy, no damage).

### 7.4 Enemy types
| Enemy | Behavior |
|---|---|
| Rifleman | Standard, takes cover |
| Rusher | Charges, weak |
| Sniper | Laser/glint warning, one-shot |
| Heavy gunner | Armored, slow, suppressing fire |
| Grenadier | Flushes you out of cover |
| Officer | Buffs nearby enemies (kill first) |
| Medic | Heals others |
| Vehicle/tank | Needs launcher or flanking |
| Drone (late) | Flying, needs anti-air |
| Boss | Unique phases (see below) |

**Enemy AI:** state machine — *Patrol → Alert → Combat → Flank → Retreat/Heal → Dead*. Uses cover points, hears gunshots, sees via vision cone.

### 7.5 Boss list (examples)
1. **Trench Butcher** (Act 2) — armored heavy with machine gun.
2. **Colonel Kessler (young)** — duel in a ruined chapel.
3. **Fortress Commander** (Act 4) — multi-wave bunker defense + rooftop sniper.
4. **Ghost Sniper** (Act 5) — cat-and-mouse in snowstorm.
5. **Kessler (final)** — drone swarm + arena fight.

---

## 8. CONTROLS (mobile)

| Control | Action |
|---|---|
| Left virtual stick | Move |
| Right-side drag | Look/aim |
| Fire button (right thumb) | Shoot (hold for auto) |
| ADS button | Aim down sights / scope |
| Reload, Grenade, Swap, Crouch, Jump/Vault, Melee | On-screen buttons (layout editable) |
| Cover | Auto-snap when near cover, tap to peek |
| Gyro aiming | Optional |
| Aim assist | Adjustable (Off / Low / High) |
| Controller support | Bluetooth gamepad |

Accessibility: left-hand mode, subtitle size, color-blind enemy outlines, vibration toggle.

---

## 9. ECONOMY & PROGRESSION

**Currencies**
- **Cash** — earned in missions; buys guns, repairs.
- **Steel / Parts** — crafting attachments.
- **Medals (premium-lite)** — rare; earned from achievements; can also be obtained with rewarded ads or one-time purchase. **No pay-to-win**: everything is also earnable by playing.

**Upgrade categories:** Weapons, Armor, Squad, Drones, Fitness (health/stamina), Skills.

**Skill tree (earn 1 point per rank):**
- *Marksman:* less recoil, faster ADS, headshot bonus.
- *Tactician:* squad commands, bigger radio range, cooldown reduction.
- *Survivor:* more health, faster healing, armor bonus.
- *Stealth:* quieter steps, faster takedowns.

**Monetization (fair):**
- Rewarded ads: 2× mission reward, free revive, free crate.
- One-time "Remove Ads + Starter Pack".
- Cosmetic skins (optional).
- **No ads during gameplay.**

---

## 10. WORLD, MAPS & ENVIRONMENTS

| Act | Environments |
|---|---|
| 1 | Fort Valor Academy: barracks, obstacle course, range, mess hall, parade ground |
| 2 | Muddy trenches, no-man's-land, ruined farm, foggy forest |
| 3 | European old town, train station, snowy mountain pass, docks |
| 4 | Beach landing, bocage hedgerows, bombed city, fortress bunker, airfield |
| 5 | Desert canyon, oil refinery, arctic base, night city |
| 6 | High-tech compound, rooftop skyline, ocean carrier, drone factory |

**Dynamic weather:** rain, fog, snow, sandstorm, day/night cycle.
**Destruction (lite):** breakable crates, wooden walls, sandbag damage, glass.

---

## 11. GRAPHICS — "MORE 3D & REALISTIC" PLAN

### 11.1 Art direction
- **Realistic, gritty, cinematic** — think "modern console feel on mobile".
- Color grading per era: sepia/mud (WW1), desaturated green/gray (WW2), warm desert (Cold War), cool blue-teal (modern).

### 11.2 Visual feature list
| Feature | Details |
|---|---|
| **PBR materials** | Albedo, normal, roughness, metallic, AO maps on all assets |
| **Characters** | 12k–20k tris hero, 6k–10k tris NPCs, realistic faces, uniform cloth sim (light) |
| **Weapons** | 15k–30k tris first-person model, detailed moving parts |
| **Lighting** | Baked GI + 1 real-time directional light; light probes; reflection probes |
| **Shadows** | Cascaded shadow maps (2 cascades on low devices) |
| **Post-processing** | Bloom, color grading, vignette, ambient occlusion, depth of field (scope), motion blur (optional) |
| **VFX** | Muzzle flash, shell ejection, tracers, impact sparks/dust/blood (toggle), explosions with smoke volume, embers |
| **Particles** | Mud splash, rain drops on lens, breath fog in snow |
| **Animation** | Mocap-style: locomotion blend trees, IK foot placement, procedural recoil, reload, vault, death ragdoll |
| **Camera** | Dynamic FOV, shake on explosions, slow-mo on boss kill / last kill |
| **Terrain** | Splat-mapped terrain, trees with wind, grass, props (scatter) |
| **Water** | Shader with reflections and foam (beach mission) |
| **Sky** | Procedural sky, volumetric-looking clouds, sun shafts |
| **UI** | Clean military HUD, minimap, ammo, compass, damage direction |

### 11.3 Quality presets
| Preset | Target device | Settings |
|---|---|---|
| Low | 3 GB RAM phones | 30 FPS, 720p render scale 0.7, no shadows on small props, no post-fx |
| Medium | 4–6 GB RAM | 45–60 FPS, 1080p scale 0.85, basic post-fx |
| High | 8 GB+ | 60 FPS, full shadows + post-fx |
| Ultra (PC/tablet) | Strong GPU | 4K assets, SSAO, soft shadows |

### 11.4 Audio
- Layered gun sounds (close/distant/echo by environment), shell casing drops, footsteps by surface.
- Dynamic music: calm → tension → combat → victory.
- Spatial 3D audio, voice lines (English + subtitles for Hindi/Spanish etc.).
- Radio chatter and ambient war sounds.

---

## 12. TECH PLAN (for vibe-coding)

### 12.1 Recommended engine
**Unity (URP — Universal Render Pipeline), C#** — best for mobile 3D, huge asset support, AI tools understand it well.
Alternatives: Godot 4 (free, lighter) or Unreal (best realism but heavy for phones).

### 12.2 Project structure (suggested)
```
Assets/
  Scripts/
    Core/        GameManager, SaveSystem, EventBus
    Player/      PlayerController, Aiming, Health, Cover
    Weapons/     WeaponBase, Gun, Attachment, Recoil, Ballistics
    Enemies/     EnemyAI, Perception, Boss
    Squad/       SquadAI, Commands
    Missions/    MissionManager, Objectives
    UI/          HUD, Menus, Armory, RankScreen
    Progression/ RankSystem, XP, SkillTree, Medals
    Drones/      DroneController, StrikeSystem
    Audio/       AudioManager
  Art/ (Models, Textures, Materials, VFX, Animations)
  Scenes/ (MainMenu, Academy, Act1_..., Act6_...)
  ScriptableObjects/ (WeaponData, RankData, MissionData, EnemyData)
```

### 12.3 Key systems (build order)
1. Player controller + camera + mobile controls
2. Weapon system (ScriptableObject-driven stats)
3. Enemy AI + health/damage
4. Mission/objective manager
5. HUD + menus
6. Save/Load (JSON + cloud optional)
7. Rank/XP + progression + economy
8. Gunsmith workshop UI
9. Squad AI
10. Cutscenes + dialogue
11. Drones & support
12. Polish: VFX, audio, optimization

### 12.4 Performance budget (mobile)
- Draw calls < 150, triangles on screen < 400k (Medium), textures compressed (ASTC), atlas materials.
- GPU instancing, occlusion culling, LOD groups (3 levels), object pooling for bullets/VFX.
- Target: 60 FPS Medium, 30 FPS Low; test on a low-end Android.

### 12.5 Data-driven design
All weapons, ranks, missions, and enemies defined as ScriptableObjects/JSON so balance can be changed without code edits.

Example weapon data:
```json
{
  "id": "ar_vanguard",
  "name": "AR Vanguard",
  "category": "AssaultRifle",
  "era": "ColdWar",
  "damage": 28,
  "rpm": 650,
  "range": 60,
  "accuracy": 0.78,
  "recoil": { "vertical": 1.2, "horizontal": 0.4 },
  "reloadTime": 2.1,
  "magSize": 30,
  "unlockRank": 6,
  "slots": ["optic", "muzzle", "barrel", "underbarrel", "magazine", "stock"]
}
```

### 12.6 Testing (to avoid the original's bugs)
- Mission state machine with unit tests (no "stuck after 1 kill" bugs).
- Auto-test: every mission must be completable (bot run).
- Timer difficulty scales by rank; first tournament always passable.
- Crash/log reporting (Firebase Crashlytics).

---

## 13. UI SCREENS

1. Splash → Main Menu (Continue, Campaign, Academy, Armory, Survival, Daily Op, Settings)
2. Character creator
3. Mission select (3D map with Act progress)
4. Briefing (map table + objectives + recommended loadout)
5. Loadout screen
6. In-game HUD
7. Pause (resume, settings, retry, quit)
8. Result screen (XP, cash, medals, stars, rank-up animation)
9. Armory / Workshop (gunsmith)
10. Skill tree
11. Rank & Medals gallery
12. Squad management
13. Settings (graphics preset, controls layout, sound, language, accessibility)

---

## 14. DEVELOPMENT MILESTONES (vibe-coding roadmap)

| Milestone | Deliverable | Done when |
|---|---|---|
| M1 | Prototype | Player moves, shoots one gun at one dummy |
| M2 | Combat core | Enemy AI, health, 3 guns, cover, HUD |
| M3 | Boot Camp (Act 1) | Tutorial missions, obstacle course, range, rank 1–3 |
| M4 | Progression | XP, ranks, save/load, shop, armory basics |
| M5 | Gunsmith | Attachments, crafting, upgrades, skins |
| M6 | Act 2 + 3 | Trench & interwar missions, bosses, squad AI |
| M7 | Act 4 + 5 | Big set pieces, vehicles, night vision |
| M8 | Act 6 | Drones, final boss, endings |
| M9 | Replay modes | Survival, Daily Op, New Game+ |
| M10 | Polish | Graphics pass, audio, optimization, localization |
| M11 | Beta test | Bug fixing, balancing, store page |
| M12 | Launch | Google Play release |

---

## 15. READY-TO-USE VIBE-CODING PROMPTS

Copy these one at a time into your AI coding tool (Claude Code etc.):

**Prompt 1 — Setup**
> Read Valor_Academy_Game_Design.md. Create a Unity URP mobile project structure exactly as in Section 12.2 and a GameManager with scene loading and a save system (JSON).

**Prompt 2 — Player**
> Build a third-person mobile player controller: left joystick move, right-side drag look, fire/ADS/reload/crouch/jump buttons, cover snap, smooth camera with shoulder swap and FOV change on ADS.

**Prompt 3 — Weapons**
> Create a ScriptableObject-based weapon system with the stats in Section 5.2, recoil patterns, reload animations hooks, fire modes, ammo, hit zones (head x2.5), muzzle flash/shell VFX, and object-pooled bullets. Add the AR Vanguard from 12.5.

**Prompt 4 — Enemy AI**
> Create enemy AI with states Patrol, Alert, Combat, Flank, Retreat, Dead using NavMesh, vision cone, hearing, cover points. Add Rifleman, Sniper, Heavy, Officer types from Section 7.4.

**Prompt 5 — Missions**
> Build a mission/objective manager (kill all, reach point, defend timer, escort, plant bomb) as a state machine with events, 3-star scoring, and a result screen. Add unit tests that prevent stuck states.

**Prompt 6 — Ranks**
> Implement the 20-rank system from Section 4 with XP curves, rank-up screen animation, unlock rewards, medals from 4.1, and a skill tree from Section 9.

**Prompt 7 — Gunsmith**
> Create the Armory Workshop: 3D rotating gun preview, attachment slots (Section 5.3), crafting costs, gun levels 1–10, camo skins, and live stat-bar comparison.

**Prompt 8 — Graphics**
> Set up URP realistic look: PBR materials, baked lighting with light probes, post-processing (bloom, color grading, AO, vignette), quality presets from Section 11.3, LOD groups, and dynamic weather (rain/fog).

**Prompt 9 — Story**
> Add a dialogue and cutscene system (Timeline), subtitles, branching choices that change Loyalty/Diplomacy/Aggression, and implement Act 1 story beats from Section 3.2.

**Prompt 10 — Drones**
> Implement ARIA drone support: recon, strike (tactical map targeting with cooldown), swarm, and FPV pilot mode, unlocked at Colonel rank.

**Prompt 11 — Optimization**
> Profile and optimize for low-end Android: pooling, GPU instancing, occlusion culling, ASTC textures, 30 FPS Low preset. Report draw calls and memory.

**Prompt 12 — Replay**
> Add Survival endless waves, Daily Operation (seeded by date), Weekly Challenge modifiers, and New Game+ Prestige.

---

## 16. ASSET SOURCES (to get realistic graphics fast)

- **Models/Characters:** Unity Asset Store, Sketchfab (check license), Mixamo (animations/rigs), Synty (stylized, not for realism), Quixel Megascans (free with Unreal; textures/scans), Poly Haven (free HDRIs/textures).
- **Weapons:** Asset Store military packs, or generate base meshes and refine in Blender.
- **Audio:** Freesound, Zapsplat, Sonniss GDC bundles (free).
- **VFX:** Unity VFX Graph/Particle packs.
- **AI-assisted:** Text-to-3D tools for props, AI texture generators for PBR maps (always check license).
- Use **original names/designs** — avoid copying the original game's art or real brand names.

---

## 17. LEGAL & RATING NOTES

- Original characters, original story, original names (no copying from Military Academy 3D assets).
- Real weapon designs inspire visuals but use generic names, or check licensing.
- Violence level: moderate, stylized hit effects, optional blood toggle; target rating **12+/T**.
- Privacy policy and consent for ads/analytics are required on Google Play.
- Medal of Honor is a real US award; the in-game use is respectful and depicted as an honor.

---

## 18. SUCCESS CHECKLIST

- [ ] Smooth 60 FPS on mid-range phone
- [ ] No forced ads during gameplay
- [ ] All missions completable (automated tests pass)
- [ ] Story, ranks, and weapons all feel connected
- [ ] 3D graphics clearly more realistic than the original
- [ ] Replay modes keep players after the story ends
- [ ] Store page with trailer, screenshots, localized text

---

*End of document. Start with Milestone M1 and Prompt 1.*
