# 🎖️ MILITARY ACADEMY 3D: TACTICAL EVOLUTION
**Master Game Design Document (GDD), System Logic Specification & Vibe Coding Implementation Plan**

---

## ⚠️ 0. CRITICAL AI "VIBE CODING" DIRECTIVES (READ FIRST)
> **INSTRUCTION FOR AI CODING ASSISTANTS (Cursor, Windsurf, Copilot, Claude, etc.):**
> You MUST adhere to the following strict rules during every prompt and implementation phase:
> 1. **NON-DESTRUCTIVE UPDATES ONLY:** Before generating or modifying any code, scan the existing workspace/file structure. **DO NOT** overwrite, delete, or replace existing working systems, components, scenes, or variables unless explicitly instructed to refactor them.
> 2. **EXTEND, DON'T REPLACE:** If a feature (e.g., player controller, basic shooting, UI canvas, or state manager) is already built, hook into its existing interfaces or extend its classes/functions. Preserve all custom user configurations.
> 3. **STRICT COMBAT & PHYSICS VALIDATION:** Never use "fake" or timer-based auto-damage. Damage MUST only occur when a physical 3D Raycast or Projectile Collider actively intersects an entity's hitbox while the weapon is aligned and aimed at that target.
> 4. **MODULAR ARCHITECTURE:** Keep state management, 3D rendering, weaponsmithing, and AI behavior decoupled so individual modules can be upgraded iteratively without breaking the core game loop.

---

## 1. EXECUTIVE SUMMARY & ENHANCED VISION
Inspired by *Military Academy 3D* (`com.slg.MilitaryAcademy3D`), **Military Academy 3D: Tactical Evolution** transforms the casual mobile mini-game formula into a **high-fidelity, realistic 3D tactical military simulation and RPG**. 

Instead of disconnected, repetitive tap mini-games, the player experiences a continuous, story-driven military career in a fully traversable 3D Academy Base and deployed warzones, featuring Physically Based Rendering (PBR), true ballistics, modular gunsmithing, cover-based combat, and tactical drone warfare.

---

## 2. STORYLINE & CAMPAIGN STRUCTURE ("OPERATION IRON VANGUARD")

### Narrative Arc
You begin as a raw civilian enlistee arriving at **Fort Valor**, an elite joint-forces military academy formed after the rise of **"The Chimera Syndicate"**—a rogue private military corporation seizing global strategic zones. Your choices in dialogue, training scores, and combat casualties directly affect squad morale and story branches.

*   **Act I: The Crucible (Bootcamp & Basic Training)**
    *   *Setting:* Fort Valor Training Grounds (Obstacle courses, firing ranges, kill-houses, barracks).
    *   *Plot:* Surviving strict Drill Sergeant Vance, bonding with your 4-person rookie squad, learning physical movement, weapon assembly, and basic marksmanship.Uncovering a Syndicate informant inside the academy.
*   **Act II: Baptism by Fire (Frontline Deployment)**
    *   *Setting:* Bombarded Eastern European urban streets & forward operating bases (FOBs).
    *   *Plot:* First live combat deployment. Defending supply lines, executing tactical breaches, and rescuing pinned allied platoons using cover mechanics and squad commands.
*   **Act III: Modern Warfare & Drone Command (Special Operations)**
    *   *Setting:* Arid mountain strongholds, naval oil rigs, and night-vision stealth compounds.
    *   *Plot:* Inducted into the Special Tactical Reconnaissance Unit. Utilizing custom-crafted suppressed weapons, thermal optics, and real-time UAV Drone Strikes to dismantle Syndicate leadership.
*   **Act IV: The Medal of Honor (Endgame Climax)**
    *   *Setting:* Chimera Syndicate’s fortified island citadel.
    *   *Plot:* Full-scale combined-arms assault. Leading an entire battalion, making high-stakes command decisions (e.g., sacrificing tactical advantage to save trapped comrades), culminating in a final showdown.

---

## 3. MILITARY RANK, MEDALS & PROGRESSION SYSTEM

Progression is governed by **XP (Experience Points)**, **Commendation Ribbons**, and **Tactical Reputation**.

| Rank Tier | Rank Title | Unlock Requirements | Unlocked Privileges & Gameplay Features |
| :--- | :--- | :--- | :--- |
| **Tier 1** | **Recruit (RCT)** | Starting Rank ($0\text{ XP}$) | Basic Obstacle Course, Workbench Level 1, Iron-Sight Pistol. |
| **Tier 2** | **Private (PVT)** | $500\text{ XP}$ + Pass Basic Rifle Qual | Assault Rifle Frame, Live-Fire Killhouse, Basic Cover Mechanics. |
| **Tier 3** | **Corporal (CPL)** | $1,500\text{ XP}$ + 1st Combat Mission | Gunsmith Optics & Barrels, 1 AI Wingman Command. |
| **Tier 4** | **Sergeant (SGT)** | $3,500\text{ XP}$ + Squad Leader Trial | Full 4-Man Squad Commands (Suppress, Flank, Hold), Sniper Frame. |
| **Tier 5** | **Lieutenant (LT)** | $7,000\text{ XP}$ + Officer Academy Exam | Tactical Recon Drone & Mortar Strike Call-ins, Advanced Ballistics. |
| **Tier 6** | **Captain (CPT)** | $12,000\text{ XP}$ + Act II Completion | Heavy Weapons, Thermal/NVG Scopes, Custom Ammo Crafting. |
| **Tier 7** | **Major (MAJ)** | $20,000\text{ XP}$ + Zero-Casualty Op | AC-130 / Guided Missile Drone Support, Elite Armor Plates. |
| **Tier 8** | **Commander / General** | $35,000\text{ XP}$ + Campaign Victory | Medal of Honor Ceremony, Sandbox War-Room Mode, All Blueprints. |

### Medals & Honors System
*   **Sharpshooter Ribbon:** Complete a mission with $\ge 85\%$ accuracy and $\ge 5$ headshots.
*   **Purple Heart:** Survive a mission after dropping below $15\%$ health and applying field medical trauma kits.
*   **Tactical Genius Medal:** Eliminate an enemy outpost using stealth or drone strikes without triggering an alarm.
*   **Medal of Honor:** Awarded for saving all squadmates during high-risk narrative choice events in Act III & IV.

---

## 4. WEAPON MANUFACTURING (3D GUNSMITH) & BALLISTICS

Unlike the original game's simple tap-to-trace gun crafting, this system features a **Realistic 3D Modular Gunsmith Workbench**.

### 4.1. Weapon Crafting & Assembly Pipeline
1.  **Blueprint Acquisition:** Earned via rank-ups, mission objectives, or salvaging enemy weapons.
2.  **Material Forging & CNC Milling Mini-Game:**
    *   Players use **Weapon Alloy**, **Polymer**, and **High-Grade Steel** at the Academy Armory.
    *   Interactive 3D assembly: Snap together the **Lower Receiver**, **Upper Receiver**, **Bolt Carrier Group**, **Barrel**, **Handguard**, **Stock**, **Magazine**, and **Optic**.
3.  **6-Slot Attachment Customization:**
    *   **Muzzle:** Flash Hider, Compensator (reduces vertical recoil), Suppressor (stealth, slight muzzle velocity penalty).
    *   **Barrel:** Short CQB Barrel (fast ADS speed), Heavy Long Barrel (increased range & damage).
    *   **Optic:** Iron Sights, Red Dot ($1.5\times$), ACOG ($4\times$), Variable Sniper Scope ($8\times\text{--}12\times$).
    *   **Underbarrel:** Angled Grip (aim stability), Vertical Foregrip (recoil control), Laser Sight (hip-fire accuracy).
    *   **Magazine:** Standard ($30\text{ rnd}$), Fast Mag, Extended Drum ($60\text{ rnd}$, reduces movement speed).
    *   **Ammunition Type:** Standard FMJ, Armor Piercing (ignores $50\%$ armor), Hollow Point ($+25\%$ unarmored limb/chest damage).

### 4.2. Weapon Stat Formulas
Every attachment dynamically recalculates the weapon's core stats:
$$\text{Effective Recoil} = \text{Base Recoil} \times (1 - \text{Grip Modifier} - \text{Stock Modifier} - \text{Muzzle Modifier})$$
$$\text{ADS Speed (ms)} = \text{Base ADS} + \sum (\text{Attachment Weight Penalties})$$

---

## 5. CORE GAMEPLAY MECHANICS & STRICT COMBAT LOGIC

> **CRITICAL LOGIC RULE:** An entity (Player or AI) can **ONLY** deal or receive damage when all physical aiming, line-of-sight, and collision conditions are satisfied.

### 5.1. Aiming, Raycasting & Hit Detection Logic
1.  **Aim Alignment Check:**
    *   Every weapon has a `MuzzleTransform` (position and forward vector $\vec{F}_{\text{muzzle}}$) and camera reticle alignment.
    *   When in **ADS (Aim Down Sights)**, the weapon's forward vector aligns with the camera center ray, modified by procedural weapon sway and recoil kick.
    *   When **Hip-Firing**, the shot vector is randomized within a dynamic cone angle $\theta_{\text{spread}}$ around $\vec{F}_{\text{muzzle}}$.
2.  **3D Raycast / Ballistic Projectile Verification:**
    *   Pressing the Fire trigger spawns a physical raycast (or high-velocity stepped ballistic projectile with gravity drop $\vec{g} = -9.81\text{ m/s}^2$) originating strictly from `MuzzleTransform.position` along the calculated shot vector.
    *   **Obstruction Check:** If the ray intersects an environment collider (wall, sandbag, vehicle) before reaching the enemy collider, the shot is blocked (unless wall penetration material thresholds are met). **Zero damage is applied to the enemy behind solid cover.**
3.  **Hitbox & Damage Calculation:**
    *   Characters have segmented 3D Physics Colliders: `Head`, `Thorax (Chest)`, `Stomach`, and `Limbs (Arms/Legs)`.
    *   Damage is ONLY applied when the projectile/raycast physically hits one of these colliders:
        $$D_{\text{final}} = D_{\text{base}} \times M_{\text{hitbox}} \times F_{\text{dropoff}}(d) \times (1 - A_{\text{reduction}})$$
    *   **Hitbox Multipliers ($M_{\text{hitbox}}$):**
        *   `Head`: $2.5\times$ (Instant kill for unhelmeted targets or sniper rifles)
        *   `Thorax`: $1.2\times$
        *   `Stomach`: $1.0\times$
        *   `Limbs`: $0.75\times$ (Applies movement speed or aim-sway penalty to hit target)
    *   **Distance Drop-off ($F_{\text{dropoff}}(d)$):** Linearly scales from $1.0$ at effective range $d_{\text{min}}$ down to $0.4$ at maximum range $d_{\text{max}}$.

### 5.2. Cover System & Enemy AI Logic
*   **Contextual 3D Cover:** Player and AI snap to low cover (crouch) or high cover (standing corner peek). While hidden behind cover, the character's hitbox is physically behind the 3D obstacle mesh. Peeking or aiming (ADS) physically moves the upper torso and head out of cover, exposing those specific hitboxes to incoming fire.
*   **Enemy AI Finite State Machine (FSM):**
    *   `PATROL` $\rightarrow$ `ALERT` (hears unsuppressed gunfire or sees player in vision cone) $\rightarrow$ `SEEK_COVER` $\rightarrow$ `ENGAGE` (peeks out, aims weapon toward player position, fires with realistic angular inaccuracy) $\rightarrow$ `FLANK / SUPPRESS`.
    *   AI **must also obey the aiming rule**: An AI soldier cannot damage the player while facing away, reloading, or suppressed behind cover. Their muzzle must point within a valid angle of the player and pass a clear 3D raycast to a visible player hitbox.

### 5.3. Drone Strike & Tactical Support System
*   Unlocked at **Lieutenant (Tier 5)**.
*   Switches camera to an overhead thermal/optical 3D UAV feed with FLIR shader post-processing.
*   Player paints targets or guides a laser-guided munition; calculates blast radius $R_{\text{blast}}$ using `Physics.OverlapSphere` and line-of-sight checks so solid roofs/bunkers shield units inside if the munition is anti-personnel.

---

## 6. REALISTIC 3D GRAPHICS & AUDIO SPECIFICATION

To elevate the visuals far beyond the mobile casual original:
*   **Rendering Pipeline:** Physically Based Rendering (PBR) using Metallic-Roughness workflows (Three.js WebGL2/WebGPU for Web, or Unity URP/HDRP / Unreal Engine 5).
*   **Lighting & Atmosphere:**
    *   Real-time directional sunlight with cascading shadow maps (CSM).
    *   Screen-Space Ambient Occlusion (SSAO), Bloom for muzzle flashes/explosions, and Depth of Field (DoF) when aiming down sights (ADS) or inspecting weapons on the workbench.
*   **Procedural Animations & IK (Inverse Kinematics):**
    *   Two-bone IK for hands gripping different foregrips and rifles.
    *   Procedural recoil springs (kickback, camera shake, muzzle rise) and breathing sway.
    *   Physical ragdoll transitions or directional hit-reaction blends upon taking damage.
*   **VFX & SFX:**
    *   Material-specific bullet impact decals and particle emitters (sparks on metal, dust on concrete, dirt plumes on terrain, blood/hit confirmation markers).
    *   Positional 3D spatial audio with supersonic bullet crack/whiz-by effects and indoor reverb zones.

---

## 7. COMPREHENSIVE VIBE CODING IMPLEMENTATION PLAN

Use this phased roadmap to build the game step-by-step with your AI coding assistant. **Always remind the AI to check existing files before executing a phase.**

### Phase 1: Foundation, Preservation Audit & 3D Scene Architecture
*   **Goal:** Inspect the existing codebase, preserve any existing setup, and establish the core 3D rendering loop, lighting, and modular state manager.
*   **Deliverables:**
    *   `GameStateManager` (tracks Rank, XP, Inventory, Story Progress, Save/Load via LocalStorage/JSON).
    *   3D Environment loader for **Fort Valor Academy Hub** (Barracks, Armory, Firing Range, Tactical Briefing Room) with PBR lighting and collision meshes.
    *   Non-destructive integration layer that wraps existing scripts without deleting them.

### Phase 2: 3D Character Controller, Camera & Cover Mechanics
*   **Goal:** Implement smooth 3D movement (Third-Person Tactical Over-the-Shoulder transitioning to First-Person ADS).
*   **Deliverables:**
    *   WASD / Virtual Joystick movement, Sprint, Crouch, Prone, and Obstacle Vaulting.
    *   Dynamic Camera Controller (`Idle`, `Combat Shoulder`, `ADS Zoom`).
    *   Ray-detected Cover System (detects low/high walls, locks movement along cover plane, exposes character mesh only when peeking/aiming).

### Phase 3: Strict Ballistics, Aiming & Segmented Hitbox System
*   **Goal:** Build the authentic combat engine where damage is strictly tied to physical aiming and raycast/projectile collision.
*   **Deliverables:**
    *   `WeaponController` with `MuzzleTransform` origin, ammo counters, reload state machine, and procedural recoil/sway.
    *   `BallisticsEngine` performing 3D Raycasts/Projectiles from the muzzle vector, checking obstacle penetration, and detecting `HitboxComponent` (`Head`, `Thorax`, `Stomach`, `Limbs`).
    *   Visual debug toggle (`debugRay = true/false`) to verify bullet trajectories and hit points during development.

### Phase 4: 3D Modular Gunsmith & Weapon Crafting Workbench
*   **Goal:** Create the interactive 3D weapon assembly and customization station.
*   **Deliverables:**
    *   3D Workbench camera view with orbit/zoom controls.
    *   Interactive part-snapping mechanic (Receiver, Barrel, Optic, Muzzle, Grip, Magazine).
    *   Real-time stat recalculation UI (Damage, Fire Rate, Recoil Control, ADS Speed, Mobility) that directly feeds parameters into `WeaponController`.

### Phase 5: Enemy AI, Squad Commands & Training Missions
*   **Goal:** Populate the Academy and Combat Zones with intelligent 3D agents.
*   **Deliverables:**
    *   Academy Drill Sergeant NPC, Obstacle Course timers, and Pop-up Target Firing Range with accuracy scoring.
    *   Enemy Tactical AI with vision cones, cover-seeking behavior, and physical muzzle-aim validation before firing.
    *   Squad Command Wheel (`Follow`, `Hold Position`, `Suppress Target`, `Flank`).

### Phase 6: Rank Progression, Story Campaign & UAV Drone Strikes
*   **Goal:** Connect all systems into the 4-Act Story Campaign with ranks, medals, and tactical support.
*   **Deliverables:**
    *   Rank Promotion ceremonies, Medal evaluation logic at Mission End screens, and Branching Dialogue UI.
    *   UAV Recon & Guided Drone Strike overhead thermal camera mode with physics blast calculation.
    *   Post-processing polish, audio spatialization, and performance optimization.

---

## 8. READY-TO-USE PROMPTS FOR YOUR AI CODING ASSISTANT

Copy and paste these prompts sequentially into your vibe coding tool:

### Prompt 1: Codebase Audit & Safe Core Setup
> *"Read `PROJECT_SPEC_AND_PLAN.md` completely. First, audit the current project directory and list all existing files, components, and logic. DO NOT delete or overwrite any existing working features. Build or extend the core 3D scene setup, PBR lighting, and `GameStateManager` (Rank, XP, Credits, Inventory) from Phase 1 so it integrates cleanly with what is already built."*

### Prompt 2: Player Controller, Cover & ADS Camera
> *"Following `PROJECT_SPEC_AND_PLAN.md` Phase 2 and Rule 0 (Non-Destructive Updates), implement or extend the 3D Player Controller. Include smooth movement, crouching, contextual cover snapping behind 3D obstacles, and a dynamic camera that transitions from Over-The-Shoulder to Aim-Down-Sights (ADS) without breaking any existing UI or scene objects."*

### Prompt 3: Strict Aim-Based Ballistics & Hitboxes
> *"Following `PROJECT_SPEC_AND_PLAN.md` Section 5.1 and Phase 3, implement the `WeaponController` and `BallisticsEngine`. CRITICAL LOGIC: Damage must NEVER be applied automatically or without aiming. Every shot must fire a 3D raycast or physical projectile directly from the weapon's `MuzzleTransform` along its aimed vector, check for solid cover obstructions first, and only apply damage if it intersects a valid target's segmented 3D hitbox (`Head`, `Thorax`, `Stomach`, `Limbs`). Preserve all existing working scripts."*

### Prompt 4: 3D Modular Gunsmith Workbench
> *"Following `PROJECT_SPEC_AND_PLAN.md` Section 4 and Phase 4, add the 3D Gunsmith Workbench module. Allow the player to craft weapons from blueprints and attach 3D modular parts (Optics, Barrels, Muzzles, Grips, Magazines) that dynamically modify the weapon's recoil, accuracy, damage drop-off, and ADS speed in the existing `WeaponController`."*

### Prompt 5: Tactical AI, Ranks, Story Missions & Drone Strikes
> *"Following `PROJECT_SPEC_AND_PLAN.md` Phases 5 & 6, implement the Enemy AI FSM (which must also physically aim its weapon and pass a clear 3D raycast check to damage the player), the Military Rank & Medal progression system, the Academy Training Courses, and the overhead thermal UAV Drone Strike system. Ensure all previously built systems remain intact."*