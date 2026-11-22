# IRON HORIZON: THE SENTINEL PROTOCOL
## Game Design, Systems Specification, and Vibe-Coding Implementation Plan

**Document type:** Master game design document + engineering implementation instructions  
**Reference game:** *Military Academy 3D* (Google Play package `com.slg.MilitaryAcademy3D`)  
**Target experience:** A polished, realistic-feeling, 3D military academy / action / squad-command simulation  
**Primary target:** Android-first; preserve the existing project's actual platform and technology choices  
**Project status:** Planning baseline. This document does not assume that any feature has already been implemented.

> **Important instruction to the coding agent:** Treat this file as the product specification, not as permission to rewrite the existing project. First inspect the repository, identify what already works, and extend it safely. Never replace a working system merely because a cleaner implementation is possible.

---

# 1. Product Vision

## 1.1 Working title

**IRON HORIZON: THE SENTINEL PROTOCOL**

The title is a working title and can be changed later. Use original branding, characters, uniforms, maps, dialogue, missions, logos, sounds, and UI. *Military Academy 3D* is a reference for broad gameplay ideas only; do not copy its assets, exact dialogue, character designs, level layouts, branding, or proprietary content.

## 1.2 One-sentence pitch

A new recruit enters a demanding military academy, earns trust through realistic training, joins a squad on increasingly complex operations, and eventually becomes a senior commander whose decisions shape the future of a fictional nation.

## 1.3 What the game should feel like

The game should combine:

- The accessibility and quick activity variety of a mobile simulation game.
- The visual quality and physical presence of a modern 3D action game.
- A connected campaign in which training skills are used later in real missions.
- A believable progression from recruit to command responsibility.
- Combat where aim, line of sight, cover, weapon characteristics, range, and hit location matter.
- Squad members who behave like teammates rather than decorative followers.
- Decisions that affect relationships, mission outcomes, resources, and later story scenes.
- Enough replayability that completing the story does not make the game feel finished forever.

## 1.4 Design pillars

| Pillar | Product requirement | What to avoid |
|---|---|---|
| Believable 3D world | Detailed environments, lighting, animation, sound, scale, weather, and responsive controls | Flat arenas, floating objects, visibly repeated props, stiff characters |
| Skill-based training | Training teaches mechanics that are tested in later missions | Mini-games that have no connection to the campaign |
| Fair combat | A shot only causes damage when the game registers a valid hit | Auto-damage from looking toward an enemy, shooting through walls, random damage |
| Meaningful progression | Ranks require service, skill, mission results, and leadership | Promotions based only on tapping or waiting |
| Squad and leadership | Teammates have roles, trust, useful AI, and command behaviours | Squad members that stand still or run into hazards |
| Consequential story | Choices affect people, resources, reputation, and later missions | Fake choices where every answer produces the same outcome |
| Replayability | Mission grades, optional goals, challenge variants, and a training sandbox | A campaign that has no reason to revisit it |
| Player respect | Clear saving, fair rewards, accessible settings, and no disruptive forced ads | Ads during active combat, lost progress, pay-to-win upgrades |

## 1.5 Target players and platform assumptions

- Primary audience: players who enjoy military training, third-person action, shooting ranges, progression, squad play, and short-to-medium missions.
- Primary platform target: Android phones and tablets because the reference title is distributed on Google Play.
- Secondary platform target: Windows/PC only if the existing project and input architecture support it without destabilizing mobile.
- Single-player campaign must be playable offline after required game content is installed.
- Online features, cloud sync, leaderboards, or multiplayer must be optional additions—not dependencies for the core campaign.
- Aim for a teen-appropriate presentation: restrained violence, no gore requirement, no glorification of harm to civilians, and clear distinction between training targets and protected people.

---

# 2. Reference-Game Analysis

## 2.1 Reference identity and observed features

The linked Google Play listing identifies *Military Academy 3D* as a single-player, offline, stylized simulation game. Its listing describes a recruit joining an academy, progressing through military ranks, training for combat, earning medals, participating in historic-war-themed and modern/futuristic scenarios, using drone support, and leading operations across varied environments.

The visible store screenshots show examples of:

- A welcome / recruit introduction at an outdoor military camp.
- Physical training and obstacle-course activities.
- A promotion / rank presentation.
- First-person pistol target practice.
- A squad-selection / unit composition screen.
- A senior-promotion scene with dialogue choices.

Third-party descriptions and store copy also mention varied training mini-games, weapon target practice, character choice/customisation, challenge activities, and a career path from a low rank toward senior command.

## 2.2 Opportunities to improve on the reference

The new game should keep the accessible academy-to-command fantasy but deliver a more complete and polished experience.

| Area | Reference-inspired baseline | Enhanced implementation target |
|---|---|---|
| Graphics | Stylised 3D academy and battle scenes | High-quality real-time 3D, coherent scale, PBR materials, modern lighting, detailed animation, weather, environmental storytelling |
| Training | Discrete training challenges | A connected academy with instructor feedback, performance records, skill unlocks, and later mission callbacks |
| Shooting | Target-practice activities | Reliable aim, recoil, weapon sway, spread, reload, hit zones, range falloff, cover obstruction, and clear hit feedback |
| Weapons | Different weapon activities and unlocks | A balanced fictional arsenal with role identity, customisation, handling trade-offs, and training requirements |
| Ranks | Career progression and promotions | A transparent rank ladder with XP, qualification, service record, leadership, medals, and promotion ceremonies |
| Story | Narrative events and military career | Original multi-act storyline, memorable squad, branching decisions, mission consequences, and character arcs |
| Squad | Squad selection and command fantasy | Persistent squadmates, specialist roles, trust, orders, revive behaviour, and understandable AI states |
| Vehicles | Military technology and vehicle-related content | Purpose-built vehicle missions with controls, camera, damage states, objectives, and performance budgets |
| Replayability | Levels and challenge activities | Mission replay, optional objectives, challenge modifiers, medal grades, practice modes, and endgame command operations |
| Performance | Mobile title | Device quality tiers, profiling, loading budgets, pooled effects, graceful fallbacks, and tested low/mid-range Android support |
| Monetisation | Store items and rewarded content may exist | No forced mid-mission ads, no paid combat advantage, transparent cosmetic-only purchases if monetisation is added |

Public user reviews of the reference title mention issues such as interruptions from ads, lag, bugs in mission progression, dull/repetitive sections, and limited replay value after completion. Treat these as design risks to avoid—not as proof that every player experiences them.

## 2.3 Research limitations

This analysis is based on the public store listing, its visible screenshots and descriptions, and public third-party summaries/reviews. It is not a source-code analysis or a complete hands-on audit of every level, weapon statistic, mission trigger, or hidden system in the original app. Do not invent claims about original internal mechanics. This document specifies a new, original implementation.

---

# 3. Original World, Setting, and Story

## 3.1 World setting

The story takes place in the fictional **Republic of Aster**, a coastal country with mountain regions, forest borders, industrial cities, rural communities, and important shipping routes.

The player joins the **Aster Defence Corps (ADC)**. The ADC is a fictional organisation. Its names, insignia, uniforms, bases, fictional equipment, and political institutions must be designed from scratch.

A fragile regional peace is threatened by the **Meridian Front**, a fictional armed network that exploits a political crisis, disrupted infrastructure, and misinformation. The conflict should not portray an entire real-world country, ethnicity, religion, or population as evil. The opposing force consists of fictional organisations and individuals with different motives and levels of responsibility.

## 3.2 Story themes

- Responsibility is more important than rank.
- Leadership means protecting people, not just winning engagements.
- A squad is built through trust and consistent decisions.
- Intelligence can be incomplete or misleading.
- Tactical success can still have strategic costs.
- The player can be ambitious and competent without becoming cruel.
- The best ending depends on a combination of mission performance, relationships, civilian protection, and command decisions.

## 3.3 Player character

The player creates or selects a recruit. The character's callsign is editable. Character appearance should be customisable without affecting combat hitboxes or competitive balance.

Suggested customisation:

- Body preset and face preset.
- Skin tone and hair styles using original, respectful options.
- Voice set and dialogue delivery style.
- Uniform colour variant and authorised cosmetic accessories.
- Callsign, name, and pronouns where supported by the dialogue system.

Do not lock story-critical skill or rank progression behind a cosmetic choice.

## 3.4 Main cast

| Character | Role | Personality | Story function |
|---|---|---|---|
| Commander Elara Voss | Academy commandant | Calm, exacting, fair | Establishes standards and evaluates leadership |
| Captain Niko Arden | Training instructor / later field mentor | Direct, dry humour, experienced | Teaches core systems and challenges reckless decisions |
| Juno Vale | Scout / reconnaissance specialist | Observant, independent | Provides route and intelligence options; trust changes with player choices |
| Malik Oran | Support specialist | Loyal, patient, practical | Helps squad cohesion and support tactics |
| Dr. Imani Sen | Field medic / humanitarian liaison | Empathetic, firm | Makes civilian safety and casualty response part of the story |
| Rhea Kade | Engineer / technical specialist | Curious, analytical | Handles field repairs, communications, and objective interactions |
| Director Soren Vale | Senior political decision-maker | Pragmatic, politically aware | Represents civilian oversight and high-level trade-offs |
| Mara Vey | Meridian Front field leader | Disciplined, strategic, personally conflicted | Recurring antagonist whose motives are gradually revealed |

Names are placeholders and should be finalised before voice recording. Keep the cast small enough that the player can remember each person and observe their development.

## 3.5 Narrative structure

Use a six-act main campaign. Each act should contain a mix of gameplay, character moments, and a clear progression beat. The proposed mission counts are planning targets, not a requirement to build all content in the first development pass.

### Act I — First Formation

**Location:** Aster Defence Academy, coastal training complex.  
**Purpose:** Introduce controls, movement, academy life, team relationships, and the rank system.

Mission sequence:

1. **Arrival:** Character creation, base introduction, first dialogue choice, and academy tour.
2. **The Standard:** Movement, sprint, crouch, stamina, and a basic fitness course.
3. **Obstacle Line:** Vaulting, crawling, rope climbing, balance, and timing challenges.
4. **Range Discipline:** Aim, fire, reload, target identification, and hit confirmation.
5. **Field Awareness:** Navigation, spotting, map reading, and identifying safe routes.
6. **First Aid Drill:** Stabilise a training casualty and choose the correct support action.
7. **Squad Evaluation:** Work with Juno, Malik, and Rhea in a timed team exercise.
8. **Final Assessment:** A combined test. The result affects the instructor's trust and the player's first commendation.

### Act II — First Deployment

**Location:** Forested border and nearby rural settlements.  
**Purpose:** Turn training into a controlled field operation.

Mission sequence:

1. **Quiet Arrival:** Reconnaissance and route selection.
2. **Lost Signal:** Restore a communications relay while avoiding unnecessary confrontation.
3. **Safe Passage:** Escort a non-combatant convoy through a dangerous area.
4. **The Checkpoint:** Identify a false report and decide how to handle a tense encounter.
5. **Extraction Window:** Coordinate the squad and withdraw before a weather front closes the route.

### Act III — City Under Pressure

**Location:** Dense industrial city, hospital district, rail depot, and damaged utilities.  
**Purpose:** Introduce complex navigation, civilians, cover, squad orders, and consequences.

Mission sequence:

1. **Grey Morning:** Move through a damaged district with restricted visibility.
2. **Open Route:** Clear a safe path without damaging protected infrastructure.
3. **Hospital Line:** Protect a medical evacuation route; civilian safety is a primary objective.
4. **The Switchyard:** Restore power to a critical transport system.
5. **Two Signals:** Choose which of two urgent objectives receives the squad's limited support. The other objective is not forgotten; its outcome appears later.

### Act IV — Broken Coast

**Location:** Harbour, islands, stormy shoreline, and a supply vessel.  
**Purpose:** Introduce water, vehicles, weather, and multi-stage operations.

Mission sequence:

1. **Harbour Watch:** Identify a suspicious vessel using observation and intelligence.
2. **Crossing:** Navigate a small transport craft through marked hazards.
3. **Supply Line:** Protect a relief shipment and keep the route open.
4. **Storm Signal:** Restore a weather-damaged navigation station.
5. **Last Light:** Coordinate an extraction while the environment changes dynamically.

### Act V — The Highland Network

**Location:** Mountain passes, a research station, and an abandoned communications facility.  
**Purpose:** Introduce difficult terrain, long sightlines, recon drones, electronic interference as a game mechanic, and leadership under uncertainty.

Mission sequence:

1. **Thin Air:** Traverse steep terrain while managing stamina and equipment weight.
2. **Blind Ridge:** Use reconnaissance tools to understand the area before advancing.
3. **Broken Network:** Repair or reroute a communications node.
4. **False Coordinates:** Detect misleading intelligence and decide whether to delay the operation.
5. **The Relay:** Complete a multi-stage objective with squad roles and limited resources.

### Act VI — Sentinel Protocol

**Location:** Aster's national command centre and a final multi-region operation.  
**Purpose:** Resolve story threads and test the player's command decisions.

Mission sequence:

1. **Command Review:** The service record, squad loyalty, and political choices are reviewed.
2. **Final Deployment:** The player assigns squad roles and selects an operation plan.
3. **The Turning Point:** Complete a mission with multiple valid approaches and consequences.
4. **Sentinel:** Final decision and outcome based on campaign flags, not one isolated dialogue choice.

## 3.6 Branching story and consequence system

Choices should be recorded as durable campaign flags. Avoid a huge branching tree that is expensive to author. Use a manageable number of meaningful state variables.

Suggested campaign variables:

- `squad_trust` — per-squadmate relationship score.
- `command_reputation` — how senior leadership views the player.
- `civilian_safety_record` — outcomes of civilian protection objectives.
- `mission_integrity` — how often the player completes objectives without avoidable damage.
- `intelligence_confidence` — quality of information gathered and verified.
- `resource_readiness` — supplies, vehicle condition, and support availability.
- `mentor_respect` — the mentor's evaluation of discipline and judgement.

Example consequences:

- A player who protects the hospital evacuation earns medical-team trust and unlocks a later support option.
- A player who rushes into an unverified route may complete an objective faster but lose resources or damage trust.
- A player who consistently listens to recon information receives better route options later.
- A player who prioritises squad safety may unlock a rescue scene during the final act.

Do not punish experimentation with irreversible failure for every minor mistake. Provide clear feedback and allow mission replay.

## 3.7 Endings

Create at least three campaign outcome families:

1. **The Guardian:** High civilian-safety record, strong squad trust, and reliable command decisions. The player becomes a respected senior leader.
2. **The Strategist:** Strong operational success and high command reputation, but some relationships are strained. The player is appointed to a major strategic role.
3. **The Cost of Victory:** Missions are completed, but avoidable losses and poor decisions reduce trust. The player must rebuild the organisation's confidence.

A fourth hidden variation can reward excellent performance across all major systems. Do not make a political presidency or a title promotion the only meaningful ending; the campaign should be about leadership and the people affected by decisions.

---

# 4. Core Gameplay Loop

## 4.1 Main loop

1. Return to academy / headquarters hub.
2. Speak with available characters and review new briefings.
3. Choose training, story mission, side activity, or replay.
4. Prepare loadout and squad roles.
5. Deploy into a 3D mission.
6. Complete primary and optional objectives.
7. Receive a debrief with performance breakdown.
8. Earn XP, commendations, currency, skill points, and story flags.
9. Unlock training, equipment, character scenes, and the next mission.
10. Save progress and return to the hub.

## 4.2 Hub layout

The academy should be a walkable or menu-driven 3D hub, depending on project scope and performance. Suggested interactive areas:

- Command building: campaign map, mission selection, briefings.
- Training grounds: obstacle course and fitness drills.
- Shooting range: target practice and weapon qualification.
- Equipment room: loadout, weapon skins, attachments, and gear cosmetics.
- Squad room: squadmate status, roles, dialogue, and team upgrades.
- Medical centre: injury / recovery and first-aid training.
- Vehicle yard: inspect and select unlocked vehicles.
- Awards hall: medals, rank history, and mission record.

A fully explorable base is a stretch feature. A fast, polished hub menu with a few high-quality 3D scenes is acceptable for the first playable version.

## 4.3 Mission structure

Every mission must have:

- Mission ID and title.
- Story act and location.
- Short briefing and objectives.
- Required rank / training qualification if applicable.
- Player spawn and squad spawn points.
- Objective triggers and clear completion states.
- Fail / retry / checkpoint conditions.
- Optional objectives.
- Civilian or protected-entity rules where applicable.
- Reward definition and story flags.
- Mission debrief data.

Mission flow should be data-driven. Do not hard-code every mission into one enormous controller script.

---

# 5. Training Academy Systems

Training is a core system, not disposable filler. Every academy activity should teach a skill that has a gameplay use later.

## 5.1 Physical training

Activities:

- Sprint and stamina test.
- Push-up / endurance timing challenge.
- Obstacle course with vault, crawl, climb, balance, and landing actions.
- Rope climb.
- Swim / water-safety test if the engine and animation scope support it.
- Weighted movement course as an optional advanced challenge.

Scoring:

- Completion time.
- Number of missed checkpoints.
- Stamina management.
- Correct interaction timing.
- Damage or penalty events.

Physical training should improve player familiarity and unlock cosmetic commendations. Avoid making a player's avatar dramatically stronger only because they repeated one mini-game hundreds of times.

## 5.2 Marksmanship training

Progression:

1. Stationary large targets.
2. Different target distances.
3. Timed target sequence.
4. Moving targets.
5. Target identification with protected / neutral targets mixed in.
6. Controlled accuracy under mild movement.
7. Advanced qualification with limited ammunition.

Scoring should reward precision and correct identification—not simply firing as fast as possible.

## 5.3 Navigation and observation

- Follow map markers and physical landmarks.
- Identify route hazards.
- Use binocular / observation mode if available.
- Mark a point of interest.
- Compare two pieces of intelligence.
- Find a safe path without unnecessary engagement.

## 5.4 First aid and support training

Keep the system accessible and non-graphic. It is a game mechanic, not a real medical-training substitute.

- Recognise a teammate's downed / injured state.
- Reach a safe interaction zone.
- Select a fictional support action.
- Complete a short timing or resource-management interaction.
- Call the medic specialist where available.

Do not present the game as real medical, military, or weapons training.

## 5.5 Leadership exercises

- Assign two squad roles.
- Choose a route based on incomplete information.
- Protect a mock civilian group.
- Decide whether to continue or wait for missing intelligence.
- Review the results and receive instructor feedback.

---

# 6. Rank, XP, Promotion, and Awards

## 6.1 Rank progression principles

A rank is a career milestone, not a simple level number. A promotion should require a combination of:

- Minimum service XP.
- Required mission completion.
- Relevant academy qualification.
- Minimum leadership or discipline score for officer ranks.
- A promotion review / briefing scene.

Do not promote the player solely because a counter reached a threshold. Always show what is required and what remains incomplete.

## 6.2 Proposed fictional career ladder

The exact terminology can be adjusted to the fictional organisation's worldbuilding. This ladder is a game progression, not a claim that all real armed forces use the same structure.

| Order | Rank / appointment | Unlock or promotion evidence |
|---:|---|---|
| 1 | Cadet | Start of campaign |
| 2 | Recruit | Complete arrival and safety orientation |
| 3 | Private | Pass basic physical and movement tests |
| 4 | Lance Corporal | Pass basic marksmanship and navigation |
| 5 | Corporal | Complete first squad evaluation |
| 6 | Sergeant | Complete first deployment with acceptable conduct |
| 7 | Staff Sergeant | Demonstrate squad coordination in two operations |
| 8 | Warrant Officer | Complete advanced specialist qualification |
| 9 | Second Lieutenant | Complete leadership assessment and command exercise |
| 10 | First Lieutenant | Successfully lead a multi-objective mission |
| 11 | Captain | Complete the urban campaign and command review |
| 12 | Major | Coordinate multiple squad roles and support assets |
| 13 | Lieutenant Colonel | Complete a regional operation and strategic briefing |
| 14 | Colonel | Earn a high command evaluation and campaign milestone |
| 15 | Brigadier | Lead a multi-region operation |
| 16 | Major General | Complete a major campaign review |
| 17 | Lieutenant General | Demonstrate consistent strategic leadership |
| 18 | General | Complete the main campaign with a qualifying record |
| 19 | Chief of Defence | Optional final appointment based on the overall campaign ending |

## 6.3 XP sources

Award XP for:

- Completing primary objectives.
- Completing optional objectives.
- Passing academy qualifications.
- Completing missions with strong performance.
- Protecting civilians and squadmates.
- Gathering and verifying useful intelligence.
- Using squad roles effectively.
- Discovering mission evidence and story items.
- Completing challenge variants.

Do not award full XP repeatedly for exploiting a single easy target or restarting the same short action.

## 6.4 Mission grade

Use a transparent grade such as S / A / B / C / D, with the grade based on multiple factors:

- Primary objectives completed.
- Optional objectives completed.
- Accuracy / target identification.
- Squad condition at extraction.
- Civilian safety, where applicable.
- Time bonus only when speed is part of the mission design.
- Penalties for avoidable mission violations.

Do not make a fast but careless playthrough automatically better than a careful successful one.

## 6.5 Medals and commendations

Suggested awards:

| Award | Earn condition |
|---|---|
| Academy Distinction | Complete all core academy qualifications |
| Precision Mark | Earn a high marksmanship grade without wasting the qualification allowance |
| Guardian Ribbon | Complete a mission with all protected-person objectives safe |
| Squad First | Extract with every squadmate in a healthy state |
| Field Engineer | Complete a repair / communications objective efficiently |
| Recon Merit | Discover and verify all key intelligence points in a mission |
| Storm Service | Complete a weather-affected operation with no lost objective |
| Command Excellence | Earn a high grade in a mission with multiple squad orders |
| Sentinel Medal | Complete the final campaign under the highest leadership conditions |

Awards must be based on recorded gameplay events. Never grant a medal from a button click alone.

---

# 7. Weapons, Equipment, and Customisation

## 7.1 Important design boundary

All weapon names and balance values below are fictional game data. The workbench is an abstract game customisation interface. Do not include real-world weapon construction, gunsmithing, modification, or operational instructions. Use licensed / original 3D models and keep all equipment descriptions appropriate for a fictional game.

## 7.2 Weapon categories

| Category | Game role | Strength | Trade-off |
|---|---|---|---|
| Service pistol | Backup / academy qualification | Fast to equip, compact | Lower capacity and range in game balance |
| Compact SMG | Close-range fictional combat role | Responsive handling | Less effective at longer game distances |
| General-purpose carbine | Main campaign weapon | Versatile handling and range | No extreme specialisation |
| Battle rifle | Controlled medium-range use | Higher per-shot game impact | More recoil and slower follow-up |
| Designated marksman rifle | Precision support | Stronger stability and zoom | Slower close-range handling |
| Precision rifle | Long-range challenge role | Accurate at long in-game distances | Slow handling and restricted ammunition |
| Shotgun | Close-range breaching-style game role | Strong close-range effect | Rapid effectiveness falloff with distance |
| Support machine gun | Squad support role | Sustained area coverage in game | Heavy movement penalty and longer setup |
| Utility launcher | Limited special-objective tool | Interacts with specific mission props | Strict ammo and mission restrictions |
| Non-lethal training tool | Target / qualification activity | Supports safe training scenarios | Not a general combat weapon |

## 7.3 Example fictional weapon roster

These names and numbers are placeholder balance values, not real-world specifications. They are deliberately abstract and should be tuned from playtests.

| Fictional name | Category | Base game damage | Handling | Intended identity |
|---|---|---:|---|---|
| V-9 Service | Pistol | 22 | Fast, moderate spread | Sidearm qualification |
| Kestrel-5 | Compact SMG | 15 | Fast movement, higher spread | Close-range mobility |
| AR-4 Sentinel | Carbine | 28 | Balanced | Default campaign rifle |
| BR-8 Northstar | Battle rifle | 38 | Stronger recoil, slower follow-up | Deliberate medium-range play |
| M7 Longview | Marksman rifle | 48 | Stable when aimed | Precision support |
| LR-12 Horizon | Precision rifle | 72 | Slow aim and recovery | Long-range target challenges |
| B-12 Breacher | Shotgun | 12 per pellet-equivalent game unit | Strong close-range spread | Short-range mission utility |
| H-6 Anchor | Support weapon | 18 | Heavy, slower aim | Squad support role |
| T-2 Marker | Non-lethal tool | 0 standard combat damage | Accurate tagging | Training and identification tasks |

Do not expose these values as if they are scientifically realistic. They are initial game-balance numbers only.

## 7.4 Weapon attributes

Every weapon should be defined by a data asset / configuration object with fields similar to:

- `weapon_id`
- `display_name`
- `category`
- `base_damage`
- `effective_range_min`
- `effective_range_max`
- `falloff_curve`
- `fire_rate_gameplay`
- `magazine_capacity_gameplay`
- `reload_duration`
- `hip_spread`
- `aim_spread`
- `recoil_profile`
- `weapon_sway`
- `aim_time`
- `movement_penalty`
- `camera_fov_when_aiming`
- `supported_fire_modes`
- `muzzle_effect_id`
- `sound_profile_id`
- `animation_set_id`
- `unlock_requirement`
- `allowed_attachments`

Keep gameplay data separate from UI display text and 3D model references.

## 7.5 Attachments and cosmetics

Possible attachment slots:

- Optic / sight.
- Muzzle cosmetic / sound-profile slot where appropriate to the fictional game.
- Grip / handling slot.
- Magazine appearance / capacity balance slot.
- Cosmetic finish.

Attachment rules:

- Attachments must have clear trade-offs. Avoid upgrades that are strictly better in every situation.
- Do not allow attachments that make an inaccurate player automatically hit targets.
- Stat changes must be shown before equipping.
- Cosmetics must not change hitboxes, collision, or competitive fairness.
- Unlocks should come from qualification, missions, rewards, and optional cosmetic purchases—not paid combat power.

## 7.6 Weapon workbench mini-game

Create a visually satisfying workbench sequence inspired by assembly / inspection activities, but keep it fully abstract and fictional.

- Display a pre-authored weapon model and clearly labelled fictional modules.
- Let players drag, rotate, and snap approved modules to highlighted sockets.
- Use a clear ghost preview and snap feedback.
- The activity is a puzzle / inspection challenge, not a real assembly guide.
- Do not describe actual firearm assembly steps or real-world operating procedures.
- Once completed, show a safe in-game inspection summary and unlock the cosmetic / qualification reward.

---

# 8. Aiming, Shooting, Hit Detection, and Damage Rules

**This section is a high-priority engineering requirement.** The combat system must be deterministic, debuggable, and fair. A visual crosshair touching an enemy on screen must not automatically reduce health. Damage is only applied after a valid hit is registered by the combat system.

## 8.1 Aim model

Support:

- Third-person over-the-shoulder aiming as the default campaign view.
- Optional first-person aiming for dedicated range challenges if the current camera architecture supports it.
- Aim sensitivity settings.
- Horizontal and vertical sensitivity.
- Aim assist slider for touch devices, with a clear definition of what it does.
- Gyroscope aiming as an optional mobile setting if supported.
- Recoil recovery and weapon sway.
- Crouch / standing accuracy differences.

Aim assist may help the player acquire a target, but it must never directly apply damage or turn an invalid shot into a hit through solid cover.

## 8.2 Required firing sequence

When the player presses Fire:

1. Check that the player is alive and allowed to fire.
2. Check that the current weapon exists, is equipped, and is not in a reload / cooldown / disabled state.
3. Check ammunition. If the magazine is empty, play the correct empty response; do not create a projectile or damage event.
4. Calculate the current aim direction using camera orientation plus the weapon's current spread / recoil offset.
5. Determine an aim point from the camera ray.
6. Cast a second validation ray from the weapon muzzle toward that aim point. This prevents a third-person camera from allowing shots through a nearby wall or obstacle.
7. The first valid collision along the muzzle ray determines what was hit.
8. If the collision is a valid damageable target and the target is not protected / friendly under current mission rules, calculate damage.
9. If the collision is a wall, cover object, or other obstruction, stop the shot there. Do not damage an enemy behind it unless a deliberately designed game penetration rule allows it.
10. Spawn visual tracer / impact / sound feedback at the actual hit point.
11. Record the shot result for hit markers, mission scoring, stats, and replay data.

No damage should be generated by proximity, camera overlap, button presses alone, or a generic animation event.

## 8.3 Hitbox and hit-zone system

Use a clear hitbox hierarchy attached to the animated character skeleton. Suggested zones:

| Hit zone | Example multiplier | Notes |
|---|---:|---|
| Head / helmet | 1.70 | Helmet protection can reduce damage; do not force instant elimination for every hit |
| Upper torso | 1.00 | Standard reference zone |
| Lower torso | 0.90 | Slightly reduced game multiplier |
| Arm | 0.65 | Lower impact; may affect a fictional aim / handling state if that system exists |
| Leg | 0.60 | Lower impact; may affect movement briefly if enabled |
| Protected / non-combatant | 0.00 damage | Protected entities are never valid combat targets |

Hit zones must move with the character animation and must not be oversized in a way that makes near misses count as hits. Use debug visualisation in development builds.

## 8.4 Damage calculation

Use a transparent and testable formula:

```text
finalDamage = baseDamage
            * rangeMultiplier
            * hitZoneMultiplier
            * armourMultiplier
            * missionDifficultyMultiplier
            * temporaryStatusMultiplier
```

Suggested initial balance values:

- `rangeMultiplier`: a data-driven curve that starts near `1.0` and declines after the weapon's designed effective range.
- `hitZoneMultiplier`: read from the hit-zone table above.
- `armourMultiplier`: e.g. `1.0` for unarmoured zones, `0.65` for a lightly protected area, or `0.45` for a protected plate area. These are fictional balance values.
- `missionDifficultyMultiplier`: tune incoming damage and enemy resilience carefully; do not silently change player weapon stats between difficulty modes.
- `temporaryStatusMultiplier`: normally `1.0`; use only for clearly communicated temporary game states.

Clamp damage to a valid non-negative range. Keep the formula in one combat service so the same rule applies to all relevant weapons.

## 8.5 Range and spread

- Use a data-driven range falloff curve per weapon.
- A shot outside the effective range may still register a hit but should use reduced damage if the weapon's configuration allows it.
- Hip-fire spread should be wider than aimed spread.
- Movement, sprinting, jumping, and heavy equipment may increase spread.
- Crouching / supported aiming may reduce spread.
- Recoil should be deterministic enough for players to learn, with controlled variation where appropriate.
- Never add random spread after a shot has already been validated as a hit. Spread must be applied before hit detection.

## 8.6 Cover and obstruction

- Use collision layers to distinguish shootable targets, world geometry, glass-like props, foliage, and non-interactive decoration.
- Solid cover blocks the shot.
- Decorative foliage may obscure visibility without necessarily blocking a shot; define this per asset, not by a global hack.
- Any special penetration behaviour must be explicitly configured for a specific fictional material and weapon. Default behaviour is that solid cover blocks damage.
- Show an impact decal / particle on the surface actually hit.
- Do not apply damage to an enemy hidden behind an obstructing object.

## 8.7 Health, downed state, and elimination

For a non-graphic, teen-appropriate presentation:

- Enemies have configurable health and optional armour state.
- At zero health, transition to a clear disabled / eliminated state with a suitable animation.
- Avoid gore and excessive injury detail.
- Squadmates may enter a downed state before mission failure, depending on mission rules.
- The medic may revive a squadmate within a limited time and resource budget if the mission supports it.
- Mission failure should be based on defined objectives (e.g. player incapacitated, protected objective lost, time expired), not random hidden rules.

## 8.8 Friendly fire and protected entities

Default campaign configuration:

- Friendly fire is disabled for standard casual campaign play, but the shot should still generate a clear warning / penalty when the player repeatedly aims or fires carelessly near teammates.
- Training qualification may end or reduce the score when a protected target is hit.
- Civilians, medics, surrendered / protected characters, and marked neutral entities must not receive combat damage.
- If an advanced difficulty option enables friendly fire, make this explicit before the mission and record it in the mission settings.

## 8.9 Reload, weapon switching, and ammunition

- Reload must be an actual state transition, not an instant ammo counter reset.
- Player cannot fire during reload unless the specific weapon and animation design explicitly supports interruption.
- Weapon switching should respect animation time and prevent duplicate fire events.
- Ammo reserves must be tracked separately from magazine contents.
- Ammo pickups must have defined amounts and cannot exceed configured capacity.
- Mission restarts must restore the correct checkpoint loadout.
- Avoid infinite-ammo behaviour unless a clearly labelled practice mode uses it.

## 8.10 Hit feedback

Provide clear, restrained feedback:

- Small hit marker only after confirmed hit.
- Distinct armour / blocked-hit feedback where applicable.
- Target reaction animation based on hit zone and state.
- Weapon impact effect at the correct collision point.
- Optional damage number setting for accessibility / casual mode.
- No hit marker when the shot hits a wall or misses.

## 8.11 Combat test cases

These are mandatory acceptance tests:

1. Crosshair is visually over an enemy but a wall blocks the muzzle ray: **no enemy damage**.
2. Clear line of sight and ray intersects an enemy torso hitbox: **torso damage is applied once**.
3. Ray misses the target by a small amount: **no damage**.
4. Magazine is empty: **no shot and no damage event**.
5. Weapon is reloading: **no shot unless the defined reload-cancel behaviour allows it**.
6. Hit intersects a protected entity: **zero combat damage and a mission-rule response**.
7. Hit intersects a teammate in default campaign mode: **no teammate health loss; warning / penalty follows mission settings**.
8. Hit intersects a distant target outside a configured falloff range: **hit may register but uses the configured lower damage**.
9. A single bullet collision event is processed twice due to animation / physics callback: **damage is still applied only once**.
10. Target is already disabled: **no additional score farming or duplicate reward**.
11. Target moves behind cover between camera aim and muzzle validation: **the obstruction blocks the shot**.
12. Restarting from checkpoint restores ammo, health, and weapon state consistently.

---

# 9. Player Movement and Character Controller

## 9.1 Movement states

- Idle.
- Walk.
- Jog.
- Sprint.
- Crouch.
- Aim-walk.
- Vault / mantle.
- Climb.
- Swim, only if included in the chosen vertical slice.
- Interact.
- Reload.
- Downed / incapacitated.

Use a state machine or a clear movement controller. Avoid many unrelated boolean flags that can create impossible combinations.

## 9.2 Movement feel

- Acceleration and deceleration should be smooth.
- Character rotation should respond naturally to movement and camera direction.
- Sprinting should consume stamina and recover when not sprinting.
- Heavy equipment should have a modest, understandable movement effect.
- Slopes, steps, and obstacles should not cause jitter or unintended launches.
- Foot placement should use animation IK where available.
- Use different movement animation blends for weapon-ready and unarmed states.

## 9.3 Mobile control layout

Default mobile layout:

- Left virtual joystick: movement.
- Right drag area: camera look.
- Fire button: right side, reachable by thumb.
- Aim button: near fire, with hold / toggle preference.
- Reload button.
- Crouch button.
- Sprint button.
- Interact / contextual action.
- Weapon switch / gadget button.
- Optional squad-command button.

Allow button repositioning / scaling or provide at least three layout presets. Add sensitivity sliders and separate camera / aim sensitivity. Respect device safe areas and different aspect ratios.

## 9.4 PC / controller support

If the existing project supports PC or controller input, use an input abstraction layer so the game rules do not depend on one device. Do not duplicate mission or combat logic for every input type.

---

# 10. Enemy AI and Squad AI

## 10.1 Enemy AI states

Use clear AI states:

- Patrol.
- Observe.
- Suspicious.
- Alerted.
- Seek cover.
- Reposition.
- Coordinate with nearby AI.
- Protect objective.
- Search last known location.
- Retreat / surrender where story-appropriate.
- Disabled.

AI should not have perfect knowledge of the player's position. Use perception checks, line-of-sight rules, hearing / noise events if supported, and a memory of last known player location.

## 10.2 Enemy AI principles

- Enemies should use cover and react to obvious threats.
- Enemies should have reaction delays appropriate to difficulty.
- Enemies should not shoot accurately through walls or beyond their perception range.
- Enemies should not all use the same decision at the same time.
- Difficulty should change decision quality, health, reaction time, and coordination in controlled ways—not create unfair invisible damage.
- Provide readable telegraphs when an enemy changes behaviour.

## 10.3 Squad roles

| Role | Gameplay contribution | Example command |
|---|---|---|
| Scout | Spots routes and points of interest | Mark / observe location |
| Medic | Supports downed teammates | Assist / regroup |
| Engineer | Interacts with repair and technical objectives | Repair / restore device |
| Support | Helps maintain team coverage in a mission | Hold position / support |
| Marksman | Covers a designated observation point in scripted missions | Observe / cover area |
| Team leader | Coordinates squad status and order availability | Regroup / move to marker |

## 10.4 Squad command system

Initial command set:

- Follow me.
- Move to marker.
- Hold position.
- Regroup.
- Observe area.
- Interact with assigned mission objective.
- Assist / revive.

Commands must be context-aware. If a squadmate cannot reach a location, show a clear status rather than silently dropping the order.

## 10.5 Squadmate progression

Squadmates can earn trust and role proficiency through story events. Do not turn them into disposable stat upgrades. Their dialogue and availability should reflect campaign outcomes.

---

# 11. Mission Objectives, Navigation, and Mission Logic

## 11.1 Objective types

- Reach a location.
- Interact with an object.
- Complete a training challenge.
- Identify / inspect a point of interest.
- Escort a protected group.
- Defend a location for a defined period.
- Restore a power / communications / transport system.
- Extract the squad.
- Complete a sequence of checkpoints.
- Make a command decision.

Avoid repeating only "eliminate every enemy" objectives. The campaign should reward observation, protection, navigation, repairs, and teamwork.

## 11.2 Objective state machine

Every objective must have explicit states such as:

`LOCKED -> AVAILABLE -> ACTIVE -> COMPLETED`

Alternative terminal states can include:

- `FAILED`
- `SKIPPED`
- `OPTIONAL_EXPIRED`

Only valid state transitions are allowed. An objective must not complete twice or grant duplicate rewards. Dependent objectives should unlock from explicit prerequisite conditions.

## 11.3 Mission completion

A mission is completed only when all required primary objectives have completed and the extraction / debrief trigger has been reached. Optional objectives affect grade and rewards but must not accidentally block completion unless the briefing explicitly says they are required.

## 11.4 Checkpoints

- Save at safe, intentional mission checkpoints.
- Never save halfway through a critical animation or while a mission state is inconsistent.
- On restart, restore player position, health, loadout, objective states, squad status, and relevant story flags from the same checkpoint snapshot.
- Support a clear restart-from-checkpoint and restart-mission option.

---

# 12. Vehicles, Recon Tools, and Support Assets

## 12.1 Vehicles

Potential vehicle categories:

- Utility transport vehicle.
- Protected squad carrier.
- Small patrol boat.
- Logistics / supply vehicle.
- Non-player aircraft used for cinematic support or extraction.

Initial vehicle gameplay should be limited to one well-made driveable vehicle and one boat or scripted transport sequence. Do not build six unfinished vehicle types before the core on-foot game works.

Vehicle requirements:

- Clear enter / exit interaction.
- Camera that does not clip through the vehicle.
- Consistent acceleration, steering, braking, and collision.
- Defined vehicle health / damage states.
- Mission boundaries and recovery behaviour.
- Mobile-friendly control mapping.
- Performance-appropriate collision and physics settings.

## 12.2 Recon drone

The recon drone is a tactical observation tool, not an always-on enemy locator.

Game functionality:

- Limited operating time / battery.
- Camera view with controlled field of view.
- Mark a visible point of interest.
- Show signal / interference quality in specific missions.
- Return to player or end the session safely.
- Optional upgrades affect scan convenience, not automatic combat success.

## 12.3 Support call-ins

Support can include extraction, supply drop, communications assistance, or a restricted cinematic support action. A strike-style support ability, if included, must be treated as an abstract fictional game ability with strict mission boundaries, clear confirmation, a cooldown / resource cost, and protected-area checks.

Rules:

- No support action may target a protected civilian area.
- Show the target area and friendly positions before confirmation.
- Cancel if the mission state changes and the target becomes unsafe.
- Never make support so powerful that it removes the need for the player to complete the mission.
- Record use in mission scoring and the story system where appropriate.

---

# 13. Environments and Level Design

## 13.1 Environment types

Build a small number of convincing, reusable environments before producing many maps:

1. Academy campus and obstacle course.
2. Shooting range.
3. Forest / rural border.
4. Industrial city block.
5. Harbour / coastline.
6. Mountain pass / research outpost.
7. Command centre interior.

## 13.2 Environment quality rules

- Realistic human-scale doors, furniture, fences, stairs, vehicles, and props.
- Terrain should have believable slopes and readable paths.
- Use modular building kits but vary silhouettes and details to avoid repeated-looking streets.
- Add distant background geometry to create a convincing horizon.
- Use environmental storytelling: signs, abandoned equipment, damaged infrastructure, mission traces, and civilian spaces.
- Avoid clutter that blocks navigation or causes unfair collision.
- Each level should have a clear visual landmark visible from its main route.

## 13.3 Weather and time of day

Add environmental variety in controlled stages:

- Clear daytime.
- Overcast / soft light.
- Rain with surface response and sound.
- Fog / mist with visibility tuning.
- Sunset / night mission with readable navigation lights.

Weather must not be only a visual filter. It can gently affect visibility, movement surfaces, ambient sound, and mission atmosphere, but never in a way that makes objectives impossible to read.

---

# 14. 3D Graphics and Animation Direction

## 14.1 Visual target

Aim for **grounded, cinematic realism that remains performant on Android**. High-quality art is not achieved by adding more polygons alone. Prioritise correct scale, lighting, material response, animation, composition, and a consistent art direction.

## 14.2 Character models

- Use consistent body proportions and realistic clothing fit.
- Use high-quality facial and hair materials within the target device budget.
- Use cloth folds and equipment straps where they are visible at gameplay distance.
- Provide LOD versions for distant characters.
- Avoid clipping between hands, weapons, sleeves, and equipment.
- Ensure all characters use correct rigs and animation retargeting.
- Keep hitboxes attached to the intended bones / transforms.

## 14.3 Character animation

Required animation groups:

- Idle and breathing.
- Walk / jog / sprint.
- Start / stop / turn.
- Crouch and crouch-walk.
- Aim and weapon-ready.
- Fire and recoil response.
- Reload and weapon switch.
- Vault / mantle / climb.
- Interact and inspect.
- Downed / help / revive.
- Dialogue gestures.
- Squad orders and acknowledgements.

Use animation blending and IK where available. Do not snap instantly between unrelated poses. Ensure animation events cannot accidentally fire weapons twice or duplicate gameplay actions.

## 14.4 Materials and lighting

Use PBR materials with consistent roughness / metallic response. Examples:

- Cloth: mostly rough, subtle weave at close range.
- Metal: appropriate reflectivity and wear.
- Painted surfaces: layered paint / scratches where visible.
- Glass: controlled transparency and reflections.
- Wet ground: environment-aware roughness changes.
- Skin: restrained subsurface appearance if the engine supports it efficiently.

Lighting rules:

- Use physically plausible key / fill / ambient relationships.
- Bake static lighting where appropriate for mobile.
- Use real-time shadows selectively for moving objects and characters.
- Avoid overly dark interiors where the player cannot see objectives.
- Avoid overdone bloom, chromatic aberration, sharpen filters, or excessive motion blur.

## 14.5 Post-processing

Use a restrained stack:

- Tone mapping / colour grading.
- Ambient occlusion where affordable.
- Anti-aliasing supported by the selected engine.
- Motion blur as an optional low-intensity setting.
- Depth of field for controlled cinematics only.
- Bloom only for bright sources and restrained intensity.

Never rely on post-processing to hide low-quality assets or poor lighting.

## 14.6 VFX

- Muzzle flash must be short and appropriately scaled.
- Tracers and impacts must correspond to actual shot direction and collision point.
- Dust, rain, debris, smoke, and sparks should use pooled effects where possible.
- VFX should not obscure the crosshair or critical objective UI.
- Use LOD / reduced particle counts on lower quality settings.

## 14.7 Camera and cinematic presentation

- Keep the gameplay camera stable and readable.
- Use gentle camera shake, not excessive movement that causes discomfort.
- Use authored camera shots for major story moments.
- Return control to the player cleanly after cinematics.
- Provide options to skip already-seen cinematics.
- Do not interrupt input or save state during camera transitions.

---

# 15. Audio and Music

## 15.1 Audio categories

- Environment ambience: wind, trees, rain, distant traffic, harbour, indoor room tone.
- Movement: footsteps by surface, cloth, gear, ladder / rope interactions.
- Weapons: distinct fictional audio profiles, reload / handling sounds, impact layers.
- UI: subtle confirmation, error, unlock, promotion, and mission-complete feedback.
- Dialogue: recorded or carefully generated placeholder voice during prototyping.
- Music: academy / preparation, tension, exploration, mission escalation, debrief, and ending themes.

## 15.2 Audio implementation rules

- Use audio mixers / buses or the equivalent in the current engine.
- Provide separate master, music, effects, and dialogue volume controls.
- Use distance attenuation and occlusion where affordable.
- Avoid playing multiple identical sounds at full volume simultaneously.
- Pool or limit frequent impact sounds.
- Subtitles must be available for important dialogue and radio lines.
- Important mission information must never be available only through audio.

---

# 16. UI / UX and Menus

## 16.1 Main menu

Suggested sections:

- Continue.
- New Campaign.
- Mission Select / Replay.
- Training.
- Loadout.
- Squad.
- Awards / Service Record.
- Settings.
- Credits.

Only show features that exist and work. Do not display fake buttons or inactive menu options.

## 16.2 In-mission HUD

Keep it minimal and readable:

- Objective / next action.
- Health / status.
- Ammunition and selected equipment.
- Small map / compass if needed.
- Squad status and current order.
- Contextual interaction prompt.
- Hit feedback only when confirmed.

Avoid covering the centre of the screen with unnecessary UI. Ensure UI scales properly across aspect ratios and display cut-outs.

## 16.3 Mission briefing

Show:

- Mission context.
- Primary objective.
- Optional objectives.
- Known risks and uncertain intelligence.
- Recommended role / qualification.
- Available squad members.
- Loadout selection.

The briefing should be skippable after the player has seen it once, unless it contains a critical unacknowledged choice.

## 16.4 Debrief screen

Show:

- Mission result.
- Objective completion list.
- Grade and reason for grade.
- XP and rank progress.
- Medals / commendations earned.
- Ammunition / resource use where meaningful.
- Squad condition and relationship changes.
- Civilian-safety result, where relevant.
- Story consequence summary without spoiling hidden information.
- Replay / continue / return to base actions.

## 16.5 Accessibility

- Text size options.
- Subtitles and background contrast.
- Colour-safe indicators; do not rely on colour alone.
- Aim sensitivity and inversion options.
- Aim assist options with clear labels.
- Left-handed control layout if feasible.
- Hold / toggle choices for aim and sprint.
- Reduced camera shake.
- Reduced motion option.
- Audio sliders and subtitle options.

---

# 17. Economy, Rewards, and Monetisation

## 17.1 In-game currencies

Keep the economy simple. Suggested currencies:

- **Service Credits:** earned through missions and used for approved equipment cosmetics / practical non-power unlocks.
- **Commendation Tokens:** earned from awards and used for special cosmetic rewards.

Avoid multiple confusing currencies. Never require a premium currency to continue the main story.

## 17.2 Reward rules

- Mission completion awards the defined reward once.
- Optional objectives provide a transparent bonus.
- Replaying a mission can award grade improvement and a limited repeat reward, but must not create an infinite currency exploit.
- Daily rewards are optional and must not punish players for missing a day.
- Offline rewards must be deterministic and must not rely on a network request to grant core campaign progress.

## 17.3 Advertising and purchases

Preferred model:

- No forced advertisements during active gameplay or a mission.
- Optional rewarded ad only from a clearly labelled menu, never required for campaign progression.
- Cosmetic-only purchases if a store is introduced.
- Clear purchase confirmation and restore-purchase support on platforms that require it.
- No pay-to-win weapon stats, rank skips, or paid exclusive mission advantages.
- If no monetisation system is already implemented, do not add one during the core gameplay build without an explicit product decision.

---

# 18. Replayability and Endgame

The campaign should remain useful after the final story mission.

Features:

- Replay any previously completed mission.
- Improve mission grades and complete missed optional objectives.
- Academy qualification records and personal bests.
- Challenge variants: time pressure, limited resources, low visibility, or squad-role constraints.
- Range practice with saved accuracy records.
- Weekly / rotating challenges only if online infrastructure exists; the offline game must remain complete without them.
- New Game Plus with optional difficulty and retained cosmetics / records, if supported by save architecture.
- Post-campaign command operations using existing environments and mechanics.

Do not make procedural content the only source of replayability. Carefully authored mission variations are better than repetitive random enemy waves.

---

# 19. Save System and Persistence

## 19.1 Data to save

- Campaign version / save schema version.
- Player identity and cosmetic selections.
- Current act / mission / checkpoint.
- Completed mission IDs and best grades.
- Objective history and campaign flags.
- XP, current rank, and promotion eligibility.
- Skills / qualifications.
- Unlocked weapons, cosmetics, and loadouts.
- Weapon attachment choices.
- Squadmate trust / availability / role progression.
- Medals and awards.
- Settings and control layout.

## 19.2 Save reliability

- Use atomic or otherwise corruption-resistant save writes.
- Include a schema version and migration path.
- Do not overwrite a valid save with an empty default state after a parse failure.
- Keep a backup / previous valid save where feasible.
- Save at safe milestones and after significant reward / progression events.
- Show a clear save failure message and do not pretend the save succeeded.
- Support reset campaign only after explicit confirmation.

## 19.3 Offline-first requirement

The core campaign, saved progression, training, and mission replay should work offline. Any cloud save or leaderboard should be an optional synchronisation layer, not a hard dependency.

---

# 20. Recommended Technical Architecture

## 20.1 First rule: discover the actual project before choosing technology

The existing repository is the source of truth. Before implementing anything, inspect:

- Root folders and project configuration.
- Game engine and engine version, if any.
- Existing scenes / levels.
- Existing player controller and camera.
- Existing input system.
- Existing combat / weapon scripts.
- Existing character rigs and animation controllers.
- Existing UI and navigation.
- Existing save / persistence code.
- Existing asset pipeline.
- Build scripts and platform targets.
- Tests, lint rules, type checking, and documentation.

Do not assume that the project uses Unity, Unreal, Godot, Three.js, Babylon.js, React, or any particular framework. Detect it from the actual files.

## 20.2 Engine guidance for a genuinely new project

If the repository is empty and a native 3D Android game is the clear target:

- **Preferred default:** Unity with URP for a mobile-first 3D game, because the project needs a mature mobile rendering path, animation, input, physics, asset import, and Android builds.
- **Alternative:** Unreal Engine if the target is primarily high-end PC / console and the team can support its larger content and performance requirements.
- **Alternative:** Godot if open-source tooling and a smaller project footprint are more important than advanced visual tooling; validate the required 3D quality and Android performance early.
- **Web-based existing project:** If the repository already contains a working Three.js / Babylon.js or other web 3D game, keep the existing engine and improve it incrementally. Do not convert it to a native engine just because this document mentions Unity.

Do not migrate the engine as part of a routine feature task. An engine migration requires a separate technical proposal, asset / save / input migration plan, and explicit approval.

## 20.3 Suggested logical modules

Use these boundaries regardless of engine naming:

- `GameBootstrap` — startup, configuration, dependency setup.
- `SceneFlow` — menu, hub, mission, debrief transitions.
- `PlayerController` — movement and player state.
- `CameraController` — camera modes and sensitivity.
- `InputRouter` — touch / keyboard / controller mapping.
- `WeaponController` — fire, reload, switch, aim state.
- `CombatResolver` — hit validation and damage formula.
- `Damageable` — health / armour / state interface.
- `ObjectiveSystem` — objective state machine.
- `MissionDirector` — mission setup, events, completion conditions.
- `AIController` — enemy perception and behaviour.
- `SquadController` — squad state and orders.
- `ProgressionService` — XP, ranks, medals, qualifications.
- `CampaignState` — story flags and relationships.
- `SaveService` — persistence, versioning, migration.
- `AudioService` — buses and event playback.
- `UIFlow` — menus, HUD, briefing, debrief.
- `QualitySettingsService` — graphics presets and runtime adjustment.
- `Telemetry / DebugService` — development-only diagnostics and event logs.

These are conceptual responsibilities. If equivalent modules already exist, extend them instead of creating duplicate systems with different names.

## 20.4 Data-driven content

Keep configurable content in data assets / ScriptableObjects / JSON / resources as suitable for the existing engine:

- Weapon definitions.
- Enemy archetypes.
- Squadmate profiles.
- Rank requirements.
- Medal conditions.
- Mission definitions.
- Objective definitions.
- Dialogue nodes.
- Reward tables.
- Quality presets.

Do not put all mission content and progression numbers into one hard-coded source file.

## 20.5 Example mission data shape

This is a conceptual schema. Adapt it to the project's language and data system; do not paste it blindly if the project has an established schema.

```json
{
  "missionId": "ACT02_M03_SAFE_PASSAGE",
  "title": "Safe Passage",
  "act": 2,
  "locationId": "FOREST_BORDER_01",
  "requiredRank": "PRIVATE",
  "briefingId": "BRIEF_SAFE_PASSAGE",
  "primaryObjectives": [
    { "id": "OBJ_REACH_ROUTE", "type": "reach", "required": true },
    { "id": "OBJ_ESCORT_GROUP", "type": "escort", "required": true },
    { "id": "OBJ_EXTRACT", "type": "extract", "required": true }
  ],
  "optionalObjectives": [
    { "id": "OPT_VERIFY_SIGNAL", "type": "inspect", "required": false }
  ],
  "protectedEntityRules": {
    "civilianDamageAllowed": false,
    "friendlyFireDefault": false
  },
  "rewards": {
    "xp": 250,
    "serviceCredits": 40
  },
  "campaignFlagsOnSuccess": ["ACT02_SAFE_PASSAGE_COMPLETE"]
}
```

## 20.6 Event-driven system

Use a clear event / message system for cross-module communication. Examples:

- `ShotFired`
- `HitConfirmed`
- `DamageApplied`
- `TargetDisabled`
- `ObjectiveStateChanged`
- `MissionCompleted`
- `MissionFailed`
- `RankRequirementMet`
- `PromotionGranted`
- `MedalUnlocked`
- `SquadmateTrustChanged`
- `CheckpointSaved`

Events should have a single owner and clear payload types. Prevent duplicate subscription and duplicate reward processing.

---

# 21. Performance and Build Targets

## 21.1 Performance goals

Set measurable budgets after the first representative scene is available. Initial targets:

- Low / mid Android devices: stable 30 FPS target.
- Supported higher-end devices: 45–60 FPS target where the scene budget allows.
- Avoid sustained overheating and large frame-time spikes.
- Keep UI responsive during loading and scene transitions.
- Use dynamic resolution or quality scaling only if the existing engine supports it cleanly.

These are target goals to validate on actual devices, not guaranteed results from the design document.

## 21.2 Optimisation priorities

- Level of detail for characters, weapons, vehicles, and environment props.
- Occlusion culling where useful.
- Texture compression and texture-size budgets.
- Light baking for static environments where appropriate.
- Limit real-time shadow casters.
- GPU instancing / batching where the engine supports it.
- Object pooling for frequent projectiles, impacts, UI popups, and VFX.
- Avoid unnecessary per-frame allocations.
- Avoid excessive physics queries and AI updates.
- Use distance-based AI update rates for far-away actors.
- Stream large environments and use loading screens / transitions where necessary.
- Profile actual scenes; do not optimise based only on assumptions.

## 21.3 Quality presets

| Setting | Low | Medium | High |
|---|---|---|---|
| Resolution scale | Reduced | Native / near-native | Native |
| Shadows | Minimal | Medium distance | Higher quality / distance |
| Textures | Compressed low-memory set | Standard | High where supported |
| Effects | Reduced particles | Standard | Full supported effects |
| Foliage | Lower density | Medium density | Higher density |
| Post-processing | Minimal | Selected effects | Full restrained stack |
| Crowd / AI count | Reduced | Standard | Higher only if profiled |

Always allow the player to lower quality manually. Auto-detection may recommend a preset but should not lock the player out of settings.

---

# 22. Security, Privacy, and Platform Quality

- Do not collect personal data that is not needed for a clear product feature.
- Never put secrets, private keys, or platform credentials into the client repository.
- Use platform-approved purchase and analytics mechanisms if added.
- Make analytics optional / privacy-compliant and avoid collecting sensitive user content.
- Validate save data before applying it to game state.
- Avoid trusting client-supplied rank / reward values if an online feature is added.
- Provide a privacy policy and required store disclosures before release.
- Use original or properly licensed assets, fonts, sounds, music, and voice content.
- Test installation, app pause / resume, device rotation where supported, low memory, and interrupted loading.

---

# 23. Development Roadmap: Build in Safe, Testable Phases

Do not attempt to build the entire campaign in one giant vibe-coding prompt. Implement a playable vertical slice first. Each phase must compile / run before the next phase begins.

## Phase 0 — Repository audit and safety baseline

Tasks:

1. Inspect the full project structure and current branch.
2. Read existing project instructions, README, agent instructions, and build commands.
3. Identify the engine, language, framework, target platform, and version.
4. Run the current project before changing anything.
5. Record existing working scenes, controls, menus, assets, and systems.
6. Identify incomplete features and current bugs.
7. Create a feature inventory with status: working / partial / broken / missing.
8. Identify the smallest safe integration point for each planned system.
9. Create a branch or checkpoint before large changes.
10. Record baseline build and performance results.

Deliverables:

- `PROJECT_AUDIT.md` or equivalent notes.
- Existing feature inventory.
- Build / run instructions.
- Known issues list.
- A proposed implementation order that reuses the existing architecture.

**Exit gate:** The current project still runs, and the agent can explain which systems already exist before implementing new ones.

## Phase 1 — Core playable prototype

Build only:

- One academy test scene.
- One player character.
- Movement, camera, and input.
- One target range.
- One fictional pistol and one fictional carbine.
- Basic target hit detection using the required camera + muzzle validation.
- Basic health / target state.
- Simple UI for ammo, health, and objective.
- One mission objective and one checkpoint.

**Exit gate:** A player can move, aim, fire, miss, hit a valid target, reload, complete the exercise, and restart without broken state.

## Phase 2 — Combat foundation

Build:

- Weapon data definitions.
- Damage resolver and hit zones.
- Range falloff and armour multipliers.
- Recoil, spread, aim state, reload, switch.
- Cover collision rules.
- Enemy health / disabled states.
- Friendly / protected entity rules.
- Development hitbox and ray debug visualisation.
- Combat unit tests / play-mode tests.

**Exit gate:** All combat test cases in Section 8.11 pass.

## Phase 3 — Academy vertical slice

Build:

- Academy hub entry.
- One physical obstacle challenge.
- One navigation challenge.
- One marksmanship qualification.
- Instructor dialogue and results.
- Training scores saved to profile.
- First rank progression event.

**Exit gate:** Academy activities teach mechanics used later and saved results survive app restart.

## Phase 4 — Mission framework and first field mission

Build:

- Data-driven mission definition.
- Briefing / loadout screen.
- One forest / rural environment.
- Primary and optional objectives.
- One squadmate with basic follow / hold behaviour.
- Extraction and debrief.
- Mission checkpoint and retry.

**Exit gate:** The mission can be completed, failed, restarted, replayed, and graded correctly.

## Phase 5 — Progression, ranks, and awards

Build:

- XP awards.
- Qualification prerequisites.
- Rank table.
- Promotion ceremony.
- Medals and service record.
- Reward de-duplication.
- Save migration.

**Exit gate:** Rank and rewards are consistent across replays, checkpoints, and app restarts.

## Phase 6 — Story and squad foundation

Build:

- Main cast data.
- Dialogue / choice system.
- Campaign flags.
- Squad trust values.
- At least two meaningful choices with later consequences.
- Two squad roles and basic command orders.

**Exit gate:** Story choices are saved and can be verified in a later mission or debrief.

## Phase 7 — Visual and audio quality pass

Build / improve:

- Character models and animation quality.
- Weapon model and first-person / third-person camera polish.
- PBR materials and lighting.
- Weather pass for one mission.
- Environment ambience and weapon audio.
- Cinematic briefing / promotion presentation.
- UI visual hierarchy.

**Exit gate:** The vertical slice looks and sounds coherent on a real target Android device at the agreed performance target.

## Phase 8 — Expand content carefully

Add in order:

1. Remaining academy exercises.
2. Remaining Act I missions.
3. One mission from Act II.
4. One additional squad role.
5. One vehicle activity.
6. One recon drone activity.
7. More acts and environments only after the previous content is stable.

**Exit gate:** New content reuses the same stable systems and does not introduce duplicate implementations.

## Phase 9 — Replayability and release readiness

Build:

- Mission replay.
- Challenge variants.
- Endgame operations.
- Accessibility options.
- Performance presets.
- Save reliability testing.
- Device compatibility and store compliance.
- Crash / softlock / progression test pass.

**Exit gate:** A new player can complete the campaign without a developer workaround, lost progress, or mission softlock.

---

# 24. Non-Negotiable Vibe-Coding / Code-Agent Rules

Paste these rules into the coding agent's project instructions. They override convenience-driven refactors.

## 24.1 Inspect before editing

- Read the existing files before changing them.
- Search for existing controllers, systems, components, scenes, assets, and data definitions before creating new ones.
- Understand how the current feature is connected to the rest of the game.
- Do not assume a feature is missing because its name is different.
- If an existing implementation is working, extend it.

## 24.2 Never replace existing working systems casually

- Do not delete or replace working player movement, camera, combat, input, UI, progression, save, or scene systems just to implement a new feature.
- Do not rewrite an entire file when a small targeted change is enough.
- Do not change public APIs, save schemas, event names, scene identifiers, or asset paths without checking all dependencies.
- Do not remove existing features to make the new feature easier.
- Do not replace real functionality with mock data, placeholder buttons, or static screenshots.
- Do not introduce a second system that competes with an existing one.

## 24.3 Plan every meaningful change

Before coding a non-trivial feature, state:

1. What already exists.
2. Which files / scenes / modules need changes.
3. What new files are truly required.
4. How backward compatibility will be preserved.
5. What tests will prove the change works.

Then implement the smallest coherent change.

## 24.4 Preserve project conventions

- Follow the existing naming, formatting, folder, architecture, and asset conventions.
- Use the existing input, state, event, save, and UI patterns where they are sound.
- Reuse existing packages and dependencies when suitable.
- Do not install a new dependency for a feature that can be implemented with existing project tools.
- Do not change engine or framework versions unless a documented blocker requires it and the user has approved the migration.

## 24.5 Make changes in small batches

- Implement one coherent system or feature at a time.
- Build / run after each meaningful batch.
- Fix errors before moving forward.
- Do not combine unrelated changes into one giant commit or rewrite.
- Keep working states recoverable using version control.

## 24.6 Protect data and saves

- Never reset the player's save data during development unless explicitly testing a migration and using a backup.
- Add versioned migrations for save-structure changes.
- Do not rename persistent IDs without a mapping / migration.
- Make reward and objective completion idempotent so retries do not duplicate rewards.
- Preserve existing user settings and control layouts.

## 24.7 No fake completion

- A button is not implemented until it performs the advertised action.
- A weapon is not implemented until it has valid data, handling, animation, hit detection, reload, UI, and tests appropriate to its scope.
- A rank is not implemented until requirements, promotion, persistence, and debrief display work.
- A mission is not complete until objectives, failure, retry, checkpoint, rewards, and save behaviour work.
- Do not mark TODOs as complete or hide a failing system behind placeholder UI.

## 24.8 Report truthfully

After each task, report:

- Files / scenes changed.
- Existing systems reused.
- New systems added.
- Build / test commands run and whether they passed.
- Known bugs / limitations.
- Features deliberately not changed.

Never claim to have tested on a physical Android device unless that test actually happened.

## 24.9 Required safe-change workflow

```text
1. Inspect repository and current implementation.
2. Reproduce / run the current project.
3. Identify the smallest integration point.
4. Describe the intended change and affected files.
5. Make a backup / use a feature branch for risky changes.
6. Implement the smallest safe change.
7. Run format / lint / type checks where configured.
8. Build or run the relevant scene.
9. Test the feature and nearby existing features.
10. Check save compatibility and performance impact.
11. Summarise results and remaining issues.
```

## 24.10 Stop conditions

Stop and ask for a product decision instead of guessing if:

- A requested feature requires replacing the current engine or core architecture.
- Existing save data cannot be safely migrated.
- Two existing systems conflict and there is no obvious owner.
- A feature would require a paid service, account, API key, or recurring cost.
- The repository is missing required assets and the agent would otherwise fabricate a final-quality replacement.
- A requested change would break already-working platform support.

---

# 25. Test Plan and Definition of Done

## 25.1 Core functional tests

- App launches from a clean install.
- Main menu navigation works.
- New campaign and continue behave correctly.
- Player movement and camera controls work on supported devices.
- Aim and fire respect weapon state and hit validation.
- Reload and weapon switching cannot create duplicate shots.
- Objectives complete only once.
- Mission retry resets the correct state.
- Rank and medal rewards are awarded once.
- Story flags persist after restart.
- Squad commands reach valid states and report failure when blocked.
- Mission debrief values match recorded events.
- Settings persist correctly.

## 25.2 Visual tests

- Character feet remain grounded.
- Hands correctly align with weapon grips.
- No major mesh clipping in core animations.
- Hitboxes follow intended body locations.
- Camera does not clip through the player or environment in common play spaces.
- UI remains visible across supported aspect ratios.
- Effects do not obscure objectives or hit feedback.
- Lighting and materials remain readable in interior and exterior scenes.

## 25.3 Performance tests

- Test a low-end supported Android device, a mid-range device, and a higher-end device.
- Measure frame time in the most demanding representative mission.
- Test long play sessions for memory growth and overheating.
- Test scene loading and return-to-hub transitions.
- Profile AI, animation, particles, shadows, and draw calls.
- Verify lower quality presets provide a measurable performance improvement.

## 25.4 Progression / save tests

- Quit during hub, mission, debrief, and scene transition.
- Resume from each supported save point.
- Simulate corrupted save data and verify fallback behaviour.
- Load a save created by the previous save schema and verify migration.
- Replay completed missions and verify rewards are not duplicated incorrectly.
- Verify player rank cannot become invalid from inconsistent XP / flag state.

## 25.5 Release definition of done

A feature is done only when:

- It is implemented in the actual game, not just represented in UI.
- It follows the existing architecture or has an approved architectural reason not to.
- It has been tested in the relevant scene / workflow.
- It does not break existing working features.
- It saves and restores state correctly where required.
- It meets the agreed performance budget.
- It has no known blocker or softlock in the intended path.
- Documentation and agent status report are updated.

---

# 26. First Playable Vertical Slice: Exact Scope

The first demonstrable version should be small but polished. Build this before expanding the full campaign.

## Included

- Main menu with New Campaign / Continue / Settings.
- Customisable recruit name and a small set of appearance presets.
- Academy hub scene.
- One physical obstacle challenge.
- One shooting-range mission.
- One fictional pistol and one fictional carbine.
- Proper aim, fire, hit validation, damage, reload, and scoring.
- One instructor character with short dialogue.
- One squadmate who can follow and hold position.
- One short field mission with two primary objectives and one optional objective.
- One rank promotion and one medal.
- Debrief with grade, rewards, and performance breakdown.
- Local save / continue.
- Low / medium / high quality setting.

## Excluded from first slice

- Full six-act campaign.
- Large open world.
- Online multiplayer.
- Real-money store.
- Complex vehicle roster.
- Large-scale destruction.
- Full procedural mission generation.
- Every weapon category.
- Full voice acting for the entire cast.

## Vertical-slice success criteria

A new player can launch the game, enter the academy, finish a training activity, qualify at the shooting range, deploy on a short field mission, complete a primary objective, receive a rank / reward update, quit, and continue from the saved state without losing progress.

---

# 27. Suggested Initial Content Values

These values are starting points for prototyping, not final balancing decisions.

| System | Initial target |
|---|---|
| First playable mission length | 5–8 minutes |
| Typical campaign mission length | 8–15 minutes |
| Academy activity length | 1–4 minutes |
| Squad size | Player + 2 or 3 AI squadmates |
| Core weapon set for vertical slice | 1 pistol + 1 carbine |
| First environment | Academy range / campus |
| Second environment | Small forest / rural route |
| Campaign acts | 6 |
| Planned main missions | Approximately 30–32, subject to production scope |
| Main campaign | Offline playable |
| Minimum performance target | Stable 30 FPS on agreed low / mid target device |
| Higher quality target | 45–60 FPS where device and scene budget allow |

---

# 28. Glossary

| Term | Meaning in this project |
|---|---|
| ADS | Aim-down-sights / aimed weapon state |
| Hitbox | Collision volume used to identify a valid hit zone |
| Muzzle ray | Final shot-validation ray originating at the weapon muzzle |
| Raycast | Physics query used to test what the shot intersects |
| PBR | Physically based rendering |
| LOD | Level of detail used to reduce complexity at distance |
| URP | Unity Universal Render Pipeline |
| Mission flag | Saved campaign state recording a meaningful event or choice |
| Checkpoint | Safe mission snapshot that can be restored on retry |
| Vertical slice | Small, polished sample proving the core game loop works |
| Idempotent reward | Reward logic that cannot grant the same completion reward repeatedly by accident |

---

# 29. Research Sources

The following sources were used to understand the reference game's public positioning and visible features. Store statistics and app versions can change; figures below are a research snapshot, not permanent facts.

1. **Google Play — Military Academy 3D**  
   https://play.google.com/store/apps/details?id=com.slg.MilitaryAcademy3D  
   Used for the public description, category, store screenshots, offline / single-player positioning, and visible ratings / download snapshot at the time of research.

2. **Apple App Store — Military Academy 3D**  
   https://apps.apple.com/in/app/military-academy-3d/id1669466518  
   Used as a secondary public description of the recruit-to-command progression, choices, missions, and public version-history notes.

3. **Malavida — Military Academy 3D overview**  
   https://www.malavida.com/en/soft/military-academy-3d/android/  
   Used only as a third-party summary of reported mini-game types and broad feature categories; not treated as authoritative for internal implementation details.

4. **BlueStacks — Military Academy 3D**  
   https://www.bluestacks.com/apps/simulation/military-academy-3d-on-pc.html  
   Used as an additional public description / visual reference.

5. **Skich — Military Academy 3D**  
   https://skich.app/games/military-academy-3d  
   Used as an additional catalogue reference and for publicly listed gameplay / stream references.

**Research snapshot date:** 31 October 2026.

---

# 30. Final Instruction to the Development Agent

Build **IRON HORIZON** as a coherent, original, high-quality 3D game—not as a collection of unrelated mini-games and not as a direct copy of another title.

The priority order is:

1. Preserve the existing project and establish a reliable build.
2. Make movement, camera, aiming, and firing feel correct.
3. Guarantee that damage only occurs after a valid hit is registered.
4. Build one excellent academy-to-field-mission vertical slice.
5. Add data-driven ranks, rewards, missions, and save logic.
6. Add the squad and story systems with real consequences.
7. Improve 3D models, materials, lighting, animation, sound, and performance.
8. Expand campaign content only after the systems are stable.
9. Add replayability and release polish.

**Never sacrifice working game logic for a visual shortcut. Never sacrifice the player's saved progress for a refactor. Never claim a feature is complete until it has been tested in the running game.**
