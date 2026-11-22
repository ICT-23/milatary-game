# VALOR ACADEMY — ART, AUDIO & UI BIBLE
### Visual style, asset specs, animation, VFX, sound, interface, accessibility, localization

> **Companion files:** Game Design, Implementation Plan, Content & Balance Data.
> **PROTECTION RULE:** If art, audio, UI or scenes already exist in the project (`EXISTS-WORKING`), **do not replace them**. Add new assets, create prefab variants, and keep existing names/GUIDs. Use this document to match new assets to the existing look or to fill missing parts.

---

## 1. VISUAL PILLARS

1. **Grounded realism** — believable materials: worn metal, wet mud, scratched wood, fabric with weave.
2. **Cinematic light** — golden-hour shafts, volumetric-looking fog, strong silhouettes, motivated light.
3. **Readable combat** — enemies and objectives must be readable at distance; clutter must never hide gameplay.
4. **Era identity** — each act has its own color grade, props and weapon silhouettes.
5. **Performance-aware beauty** — baked lighting, smart LODs, texture atlases; looks premium on a mid-range phone.

**Reference moods (describe, do not copy any specific game):** gritty war-film cinematography, desaturated trench palette, warm desert dusk, cold teal-blue modern night.

---

## 2. COLOR & LIGHTING PER ACT

| Act | Setting | Palette | Light | Post-process grade |
|---|---|---|---|---|
| 1 Boot Camp | Academy, grass, brick | Olive, tan, red brick, blue sky | Morning sun, soft shadows | Clean, mild contrast, slight warm |
| 2 Great War | Mud trenches, fog | Brown, grey, sickly green | Overcast, low sun, fog volume | Desaturated 25%, sepia tint, film grain |
| 3 Interwar | Old town, snow, docks | Warm stone, steel blue, amber lamps | Dusk, street lamps, snowfall | Mild teal/orange split |
| 4 World at War | Beach, hedgerow, ruins | Grey-green, ash, fire orange | Smoke-filled, explosions as light | Contrast +10%, slight bleach bypass |
| 5 Cold Shadows | Desert, refinery, arctic | Orange sand, night cyan, white ice | Harsh noon / blue night / blizzard | Strong day-night split, bloom on lights |
| 6 Drone Age | Megacity, carrier, tech base | Cool blue, neon cyan, white | Night city lights, HUD glow | Clean, high contrast, subtle chromatic aberration |

Lighting setup (mobile):
- 1 directional real-time light (sun/moon), baked GI lightmaps for static geometry, light probe groups for characters, reflection probes per area.
- Max 4 dynamic point/spot lights visible at once; use baked lights elsewhere. Muzzle flash uses a short light pulse (pooled, 0.05 s).
- Sky: procedural/HDRI per act; fog (linear/exp2) tuned per mission.

---

## 3. CHARACTER ART

### 3.1 Characters list
| Character | Variants | Notes |
|---|---|---|
| Player (customizable) | 2 body types × 8 faces × 6 hair × skin tones; 6 uniforms by era; camo skins | Face morph via blend shapes; voice options (4) |
| Brooks | Era variants (young, wounded, senior) | Scars progress by act |
| Lena | Cadet, field, officer | Sniper kit |
| Vance | Cadet, officer | Arrogant posture |
| Whitfield | Formal, field | Commanding silhouette |
| Kessler | Young officer, general, final | Distinct coat/cape silhouette per age |
| Enemies | 3–4 head/outfit variants per type per era | Color-coded accents so type reads at distance |
| Civilians | 6 variants | Never targetable |

### 3.2 Technical specs
| Asset | Triangles (LOD0 / LOD1 / LOD2) | Texture size (ASTC) | Notes |
|---|---|---|---|
| Hero player | 18k / 9k / 4k | 2048 (body+gear atlas) + 1024 (face) | 4 blend-shape sets, cloth-lite on coat |
| Major NPC | 15k / 7k / 3k | 2048 | Unique face |
| Standard enemy | 8k / 4k / 2k | 1024 atlas, shared across variants | GPU-instanced where possible |
| Boss | 25k / 12k / 6k | 2048 ×2 | Custom shader details |
| Civilian | 5k / 2.5k / 1.2k | 512–1024 | |
| First-person arms | 8k | 1024 | Separate rig |

Rig: humanoid (Unity Humanoid avatar), ~65 bones max; facial bones optional on heroes; IK for feet/hands/head look.

### 3.3 Equipment visibility
Helmet, vest, backpack are separate meshes/blend layers so gear tiers show visually. Rank insignia swap on the shoulder texture (20 rank badges).

---

## 4. WEAPON ART

- **Poly budget:** first-person weapon 15k–30k tris, world weapon 3k–6k tris.
- **Textures:** 2048 PBR (albedo, metallic/smoothness, normal, AO), shared trim sheets for screws/rails.
- **Separate parts:** magazine, bolt, slide, trigger, safety, optic mount, muzzle, stock — for reload/bolt animations and attachments.
- **Attachment sockets:** `Socket_Optic, Socket_Muzzle, Socket_Barrel, Socket_Under, Socket_Mag, Socket_Stock` + `Socket_Muzzle_FX` and `Socket_Eject` on every weapon.
- **Wear states:** clean (modern), worn (Cold War), rusted/wood (WW1). Camo skins use a mask texture to tint only specific regions.
- **Naming:** `WPN_<Class>_<Name>_LOD0`, textures `T_WPN_<Name>_<Map>`.
- **Legal:** use original designs; generic names; avoid brand logos and real markings.

---

## 5. ENVIRONMENT ART

### 5.1 Level kit approach
- Modular kits per act (walls, trench pieces, bunkers, ruins, props) with trim sheets and tileable materials (mud, brick, concrete, metal, wood, snow, sand).
- **Scatter system** for grass, rocks, debris, barbed wire; foliage with wind shader (vertex animation).
- **Terrain:** Unity Terrain with 4–6 splat layers per scene; heightmaps hand-tuned; max 1 km² per mission (usually far less).
- **Props density guideline:** every 5–8 m of walkable space has a gameplay-relevant prop (cover/landmark); decoration fills in around it.
- **Landmarks:** each mission has 1–2 tall visible landmarks to orient the player.
- **Gameplay readability rules:** cover objects have consistent shapes/height (low = 1.0 m, high = 2.0 m); destructible objects have a visible damage state texture; objective areas have subtle light/flag/sound cue.

### 5.2 Surfaces (for impact FX/audio/footsteps)
Concrete, Metal, Wood, Dirt, Mud, Grass, Sand, Snow, Water, Glass, Fabric, Flesh. Each material tagged via `SurfaceType` component or physic material → drives decals, particles, impact sound, footstep sound, penetration rule (Implementation Plan 5.3).

### 5.3 Texture & model import rules
- Models: FBX, scale 1 = 1 meter, forward Z, pivot at base, no negative scale, apply transforms.
- Textures: power-of-two, ASTC (Android), sRGB on color maps only, mipmaps on, streaming mipmaps for large scenes; atlas small props.
- Lightmap UV2 generated; static flags set correctly.
- Colliders: simplified mesh/box colliders (never full mesh colliders on dense props).

---

## 6. ANIMATION LIST

### 6.1 Player (third-person + first-person)
- Locomotion blend tree (idle, walk, jog, sprint, strafe 8-way), crouch & crouch-walk, prone (optional), turn-in-place.
- Jump/land, vault (low & high), climb ladder, slide-into-cover.
- Weapon sets per class (pistol, rifle, SMG, LMG, shotgun, sniper, launcher): idle, aim, fire additive, reload (tactical & empty), swap in/out, sprint pose, ADS walk.
- Melee: slash, stab, stealth takedown (front/back).
- Grenade throw (overhand/underhand), cook.
- Heal (medkit/bandage), revive ally, carry body, interact, radio call, salute (rank-up cutscene).
- Hit reactions (directional light/heavy), death (6 variants), ragdoll transition, downed crawl.
- Cinematic acting for story scenes.

### 6.2 Enemies
Patrol walk, alert look-around, run, combat strafe, cover enter/peek/blind-fire/exit, throw grenade, reload, melee rusher, sniper aim/idle, heavy slow-walk, medic heal, hit reactions, death variants, surrender (optional), flinch from explosion.

### 6.3 Quality rules
- Animation Rigging / IK: feet grounded on slopes, hands on weapon grip, head look at target.
- Procedural recoil layered on animation (not baked into clips).
- Root motion off for gameplay locomotion (controller-driven) except for takedowns/vaults.
- Animation events for: footstep, reload steps (mag out/in/chamber), shell eject, melee hit frame, throw release.
- Mocap-quality sources: Mixamo (retarget), Asset Store packs, or custom; always cleanup foot sliding.

---

## 7. VFX LIST

| Effect | Details | Budget |
|---|---|---|
| Muzzle flash | 2–3 sprite variants + short light; smaller w/ suppressor | ≤ 12 particles |
| Shell ejection | Physics-lite brass casing, sound on landing | pooled 30 |
| Tracers | Stretched line with fade, every 3rd round (or tracer ammo) | pooled 40 |
| Bullet impacts | Per surface: dust, sparks, splinters, mud splash, water; decals (pooled, max 60, fade) | ≤ 20 particles |
| Blood (toggle) | Small spray + decal; off in "Reduced violence" mode | ≤ 15 |
| Explosion | Fireball sprite sheet, smoke column, debris, shockwave distortion, camera shake, light flash | ≤ 120 particles |
| Smoke grenade | Volume-like layered billboards, blocks vision | ≤ 100 |
| Fire | Looping flame + embers + light flicker | ≤ 60 |
| Weather | Rain (particles + wet shader + lens drops), fog, snow, sandstorm | Scaled by preset |
| Drone strike | Targeting laser, missile trail, impact; swarm sparks | ≤ 150 |
| Hit marker / kill confirm | UI-only | — |
| Dust motes, breath fog (cold), heat haze (desert) | Cheap ambient layers | ≤ 30 |

Rules: all VFX **pooled**, LOD by distance, disable shadows on particles, use GPU instancing, mobile-friendly shaders (no heavy refraction on Low).

---

## 8. CAMERA

- **Third-person default:** shoulder offset right (0.6 m), distance 2.6 m, FOV 60; swap shoulder with button.
- **ADS:** camera moves closer and FOV by optic; sniper uses first-person scope view with render-texture-free zoom (FOV change + vignette overlay).
- **Cover camera:** lowers and offsets away from the wall; peek raises.
- **Collision:** spring-arm raycast to prevent clipping through walls.
- **Shake:** explosion, heavy fire, landing; strength scaled by settings (Accessibility: can disable).
- **Cinematics:** Cinemachine + Timeline; letterbox bars; skippable.
- **Kill cam / slow-mo:** boss final hit & last enemy kill (optional toggle), 0.6 s at 0.3× time scale.

---

## 9. AUDIO BIBLE

### 9.1 Pillars
Punchy, readable, directional. The player must **hear where danger is** and **feel the weapon**.

### 9.2 SFX list
| Category | Sounds |
|---|---|
| Weapons | Each gun: close fire, distant fire, indoor/outdoor tail, suppressed, dry-fire, reload steps (mag out/in/bolt), swap, empty click; unique per class |
| Impacts | Per surface (12) × bullet/explosion; ricochet whizz-by; flesh hit; headshot "tick" cue |
| Explosions | Small/med/large, debris rain, ringing tinnitus (when near, short) |
| Movement | Footsteps per surface (walk/run/crouch), gear rattle, vault/climb, body fall |
| Player | Breath, heartbeat low-health, hit grunts, heal, reload effort |
| Enemies | Callouts, hit/death, grenade warning, reload, radio |
| Vehicles | Jeep, tank, helicopter, boat, drone buzz |
| UI | Click, confirm, deny, rank-up fanfare, medal jingle, cash, craft anvil, upgrade |
| Ambience | Per act/mission: wind, distant artillery, birds, rain, city hum, ocean, radio static |

Variation: each repeating sound has 3–5 variants, random pitch ±5%, no machine-gun looping artifacts.

### 9.3 Music
| Act | Style | Layers |
|---|---|---|
| 1 | Orchestral march, hopeful | Calm / Training / Success |
| 2 | Low strings, war drums, somber | Tension / Combat / Loss |
| 3 | Noir jazz hints + strings | Stealth / Chase / Duel |
| 4 | Epic orchestral, brass | Approach / Combat / Victory |
| 5 | Ambient synth + percussion | Recon / Raid / Betrayal |
| 6 | Electronic orchestral hybrid | Hacking / Swarm / Final boss |
Dynamic music: 3–4 stems (ambient, tension, combat, boss); crossfade 2 s on state changes; fade out on cutscene; main theme motif reused across acts with variation.

### 9.4 Voice-over
- Languages: English first; subtitles in Hindi, Spanish, Portuguese, French, Arabic, Indonesian, etc. (see Localization).
- Voice lines: story dialogue, mission briefings, squad barks ("Reloading!", "Contact left!", "Grenade!"), enemy barks (era-appropriate), ARIA system voice.
- Radio filter effect for comms; distance-based muffling.
- VO file naming: `VO_<Character>_<Act>_<Scene>_<Line>`.

### 9.5 Mixing
| Bus | Level | Notes |
|---|---|---|
| Master | 0 dB | Limiter at −1 dB |
| SFX – Weapons | −6 dB | Priority 1 |
| SFX – Impacts/Env | −10 dB | |
| Voice | −5 dB | Ducks music −6 dB while speaking |
| Music | −14 dB | Ducks SFX ambience slightly in cinematics |
| Ambience | −18 dB | |
| UI | −8 dB | |
- Max 32 simultaneous voices (Low: 20); pooled AudioSources; distance rolloff logarithmic; occlusion via low-pass when blocked by walls; reverb zones indoors.
- Settings: master, music, SFX, voice sliders, mono option, subtitles on/off, "headphone" spatial mode.

---

## 10. UI / UX

### 10.1 Style
Military-tactical: dark translucent panels, stencil-style headings, thin line icons, brass/olive accent color, high contrast text. One consistent icon set (rank, medals, weapon classes, currencies, objectives).

### 10.2 Typography
- Headings: bold stencil/condensed sans (licensed free, e.g. open-license fonts).
- Body: clean readable sans, min 16 sp on phones; numbers use monospace for ammo/timers.
- Localization-safe: fallback fonts for Hindi, Arabic (RTL support), CJK if added.

### 10.3 HUD layout (landscape phone)
```
┌──────────────────────────────────────────────────────────────┐
│ [Minimap]  [Compass bar──────N──────]       [Objective list] │
│  health ▮▮▮▮▮▯ armor ▮▮▮                     [Pause]         │
│                                                              │
│                         ( + )  crosshair                     │
│                                                              │
│ [Move stick]                                 [Fire]  [ADS]   │
│ [Grenade] [Medkit]    ammo 24 / 90 [Weapon]  [Reload][Swap]  │
│ [Crouch]                                     [Jump][Cover]   │
└──────────────────────────────────────────────────────────────┘
```
- Buttons are movable/resizable in the Controls editor; opacity slider; left-hand mode; presets (Default, Compact, Big).
- Damage direction arcs, hit marker (white = hit, red = headshot, skull = kill), low-health vignette.
- Notifications top-center (objective updated, checkpoint saved); never cover the crosshair.
- Context prompts: "Hold to revive", "Plant charge".

### 10.4 Screen flow
```
Splash → Main Menu ─┬ Campaign → Act Map → Mission Briefing → Loadout → [GAME] → Results → (Rank-Up) → Act Map
                    ├ Academy (Training, Tournaments)
                    ├ Armory (Weapons, Gunsmith, Gear, Skins)
                    ├ Squad / Skill Tree / Medals
                    ├ Survival / Daily Op / Weekly / Boss Rush
                    ├ Shop (Crates, Remove Ads, Starter Pack)
                    └ Settings (Graphics, Controls, Audio, Accessibility, Language, Privacy)
```

### 10.5 Key screen specs
- **Results:** stars animate, XP bar fills, cash counter, medals earned, "Double reward (ad)" button, Next/Retry.
- **Rank-up:** 3D insignia reveal, new unlocks list, skill point notice.
- **Armory:** left list of weapons, center rotating 3D model, right stat bars (current vs preview), bottom slot buttons.
- **Gunsmith:** tap slot → pick part → shows cost and stat delta → Confirm.
- **Briefing:** 3D map table with markers, objectives, enemy intel, recommended loadout, difficulty selector.
- **Pause:** Resume, Settings, Retry (from checkpoint), Quit, shows objectives.

### 10.6 Feedback & polish
Button press animation, haptic ticks (toggle), sound on every interaction, loading screens with tips and lore, never a dead tap (disabled buttons show a reason on tap).

---

## 11. ACCESSIBILITY

- Subtitles with speaker names, size S/M/L, background box option.
- Color-blind modes (protanopia/deuteranopia/tritanopia): enemy outline colors, minimap markers, UI states.
- Enemy outline option (high-visibility), crosshair color/size/shape options.
- Aim assist levels, hold/toggle options for ADS/crouch, adjustable sensitivity & dead zones, gyro on/off.
- Reduce camera shake / motion blur / flash effects; photosensitivity warning on first launch.
- "Reduced violence" mode (no blood, softer death animations).
- Full control remap (gamepad), one-handed layout preset, haptics on/off, text-size scaling.
- Difficulty can be changed mid-campaign.

---

## 12. LOCALIZATION

- All strings in a table (Unity Localization package): key → text per locale; no hard-coded text.
- Initial languages: English, Hindi, Spanish, Portuguese (BR), French, German, Russian, Arabic (RTL), Indonesian, Turkish, Japanese, Korean, Simplified Chinese (phased).
- Text expansion: reserve 30–40% extra width; use auto-sizing + scroll.
- Numbers, dates, currency localized; units metric (with optional imperial).
- Voice: English at launch; localized subtitles; consider Hindi VO later.
- Localization QA: pseudo-locale test (accents, long strings), RTL layout test.

---

## 13. ASSET PIPELINE & TRACKING

### 13.1 Folder & naming
```
Art/
  Characters/ Weapons/ Environment/<Act>/ Props/ VFX/ Animations/ UI/ Audio/ (SFX, Music, VO)
Naming prefixes: SK_ (skeletal), SM_ (static mesh), T_ (texture), M_ (material), MI_ (material instance),
                 A_ (animation), AC_ (animator controller), FX_ (VFX), PF_ (prefab), UI_, SFX_, MUS_, VO_
```

### 13.2 Asset license log (`ASSET_LICENSES.md`)
For **every** external asset record: name, source URL, author, license (CC0, CC-BY, Asset Store EULA, etc.), date, commercial-use OK?, attribution required?. No asset is shipped without a recorded license. AI-generated assets: record the tool and its commercial terms.

### 13.3 Asset import checklist
- [ ] Scale/pivot correct, no negative scale
- [ ] Triangles within budget, LODs set
- [ ] Textures compressed, mipmaps, correct color space
- [ ] Materials use project shaders (URP Lit / custom mobile shader)
- [ ] Colliders simple; layer set; static flags set
- [ ] Tested on low-end preset
- [ ] License logged

### 13.4 Placeholder strategy (to keep building without final art)
Use free/grey-box placeholders with the **same names/pivots** as final assets, so swapping later never breaks references (prefab variants, same socket names).

---

## 14. AI-ASSISTED ART PROMPT IDEAS (concept art / textures)

Use for **concept and texture ideas only**; check licensing before shipping:
- "Gritty realistic WW1 trench in fog at dawn, mud, wooden planks, barbed wire, desaturated, cinematic lighting, game environment concept art."
- "Tileable PBR texture: worn olive-drab canvas fabric with stitching, 2048, seamless."
- "Rank insignia set, 20 designs, realistic embroidered patches, flat lay, transparent background."
- "Futuristic drone control HUD overlay, thin cyan lines, military tactical, 16:9, transparent."
- "Character concept: female sniper cadet, field uniform, 1940s style, realistic proportions, turnaround sheet."

---

## 15. ART QA CHECKLIST (before shipping an act)

- [ ] Consistent color grade per act
- [ ] No texture stretching/z-fighting/light leaks
- [ ] Enemies readable against every background (squint test)
- [ ] Cover heights consistent
- [ ] VFX within particle budgets, pooled
- [ ] Audio levels balanced, no clipping
- [ ] UI readable on 5" and 10" screens; safe areas (notches) respected
- [ ] Low preset still looks acceptable
- [ ] Localization strings fit

---

*End of Art, Audio & UI Bible.*
