# 10 — Graphics, Animation and VFX

**Purpose:** Production specs for assets, animation and effects. **Status:** Proposed · **Last reviewed:** 2026-10-18
Look: [09](09_VISUAL_ART_DIRECTION.md). Pipeline and naming: [16](16_ASSET_PIPELINE_AND_FILE_CONVENTIONS.md). Documentation does not create assets; every row below is an asset to **create, license or import**.

## 1. Rendering approach (depends on EXP-01 in [13](13_TECH_STACK_AND_RENDERING_DECISION.md))
3D-modelled characters/props are authored in Blender, rendered from a fixed camera (8 directions) to sprite atlases; environments are layered painted tiles plus prop sprites. Runtime uses 2D draw calls on a Skia canvas. If EXP-01 fails, this doc is re-written for Option C (real-time 3D in Godot).

## 2. Asset list
| Category | Items (MVP) | Format | Source | Status |
|---|---|---|---|---|
| App icon | 1024×1024 master, adaptive layers | PNG/SVG | Create | Proposed |
| Splash | Logo + ember animation | PNG sequence/Lottie | Create | Proposed |
| Menu backgrounds | Barracks hub, title key art | WebP | Create | Proposed |
| Environments | 8 locations (see 09) | Tiled WebP + JSON | Create/License | Proposed |
| Player | 1 base, 3 body presets, 4 hair, outfits ×3 | Sprite atlas | Create | Proposed |
| Squad | 4 characters | Sprite atlas | Create | Proposed |
| Rival/Keth/Thorne | 3 | Sprite atlas | Create | Proposed |
| Enemies | 3 archetypes (Patroller, Marksman, Heavy) + civilians ×6 | Sprite atlas | Create | Proposed |
| Weapons/gadgets | 3 primary, 1 sidearm, 3 gadgets | Sprite + FX | Create | Proposed |
| Props | Cover, crates, trucks, boats, beacons | Atlas | Create/License | Proposed |
| Portraits | 10 chars × 4 expressions | WebP 512² | Create | Proposed |
| HUD/icons | ~80 icons | SVG→PNG | Create | Proposed |
| Mission art | 12 banners | WebP | Create | Proposed |
| Cutscenes | 5 animated-still sequences | WebP + JSON timeline | Create | Proposed |
| Store assets | Icon, feature graphic 1024×500, ≥ 4 phone screenshots | PNG | Create | Proposed |

## 3. Animation set (per humanoid)
| Clip | Frames/dir | FPS | Notes |
|---|---|---|---|
| idle | 8 | 8 | breath |
| walk | 8 | 12 | |
| sprint | 8 | 12 | |
| crouch-walk | 8 | 10 | |
| aim/fire | 4 | 12 | additive overlay |
| reload | 10 | 10 | |
| cover-enter/exit | 6 | 12 | |
| carry | 8 | 10 | |
| down/revive | 8 | 10 | |
| interact/repair | 8 | 10 | |
| climb | 8 | 10 | |
Directions: 8. Budget per character atlas (Medium): ≤ 2048×2048 px compressed; total in-scene sprites ≤ 24 MB VRAM equivalent (target).
Principles: strong anticipation on fire/reload, 2-frame hit-stop, readable silhouettes at 96 px.

## 4. Facial expression requirements
Portraits only (no runtime facial rig): neutral, determined, afraid, relieved (+ angry for Corin/Keth, warm for Fathom).

## 5. VFX catalogue
| FX | Frames | Particles budget | Notes |
|---|---|---|---|
| Muzzle flash | 3 | 1 | additive |
| Tracer | line | 1 | fade 80 ms |
| Impact (dust/spark/water) | 6 | ≤ 10 | per surface |
| Smoke | 12 | ≤ 20 | alpha sheet |
| Rain | layer | ≤ 80 | culled on Low |
| Fire/beacon | 8 loop | ≤ 25 | key warm light |
| Flood splash/ripples | 8 | ≤ 20 | |
| Dust motes/fog | layer | ≤ 30 | |
Limits per profile in [09](09_VISUAL_ART_DIRECTION.md) §9.

## 6. LOD / scaling
Sprite mipmaps ×1, ×0.75, ×0.5 for High/Med/Low; distant props use merged static layers; animation frame-skipping on Low (every 2nd).

## 7. Validation rules
Power-of-two atlas tiles where required; alpha premultiplied; no stray pixels (automated check); every asset has an owner and licence row; file size caps: sprite atlas ≤ 4 MB, background ≤ 1.5 MB.

## 8. Acceptance
| ID | Criterion |
|---|---|
| GFX-001 | All MVP assets listed in manifest with status |
| GFX-002 | Atlas memory within budget on Medium profile (measured, EXP-03) |
| GFX-003 | Animation transitions have no pops in review video |
