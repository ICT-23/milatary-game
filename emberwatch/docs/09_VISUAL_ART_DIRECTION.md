# 09 — Visual Art Direction (Art Bible)

**Purpose:** Authoritative look and feel. **Status:** Proposed · **Last reviewed:** 2026-10-18
Production details: [10](10_GRAPHICS_ANIMATION_AND_VFX.md), [16](16_ASSET_PIPELINE_AND_FILE_CONVENTIONS.md). UI tokens: [08](08_UI_UX_AND_SCREEN_SPECIFICATIONS.md).

## 1. Visual identity
**"Lantern-lit realism, hand-painted warmth."** Grounded military gear and weathered architecture, softened by painterly textures and a recurring warm light motif (the ember) against cool mountain blues. Each scene has one dominant warm light so the player's eye finds objectives.

## 2. Originality boundaries
No copying silhouettes, uniforms, emblems, UI or environments from any existing game. Reference only broad real-world visual material (mountain valleys, hydro infrastructure, field hospitals, stone fortresses) from licensed or self-shot sources. Keep a **Reference Log** (source, licence, use) — see [24](24_REFERENCE_ANALYSIS_AND_ORIGINALITY.md).

## 3. Colour palette
| Role | Hex |
|---|---|
| Ember (accent) | #E8742C |
| Slate green (CDC) | #2F4A3F |
| Night navy | #0F1B24 |
| Ash grey | #8A9199 |
| Bone | #E9E4D8 |
| Amber (objective) | #F2B33D |
| Alert red | #D1423B |
| Safe green | #5FAE6B |
| Water teal | #3B8FA6 |
| Compact rust | #8C4A2F |
| Mist blue | #A9C2CF |
Rules: objectives always amber+shape; hostile alerts red+shape; friendly markers teal/green+shape.

## 4. Environments
| Location | Mood | Key features | Palette lean |
|---|---|---|---|
| Ash Road | Urgent, wet | Mud, rising water, tilted bus | Teal/grey |
| Halvorn Ridge | Disciplined, cold | Stone barracks, lantern posts, parade ground | Slate/ember |
| Ridge forest (night) | Quiet tension | Moon shafts, fog | Navy/mist |
| Harrow Dam Road | Open, exposed | Concrete spillway, convoy | Bone/grey |
| Tower 7 | Haunted, sacred | Charred beacon, keeper's room | Ember on dark |
| Flooded quarter | Chaotic | Floating debris, narrow streets | Teal/rust |
| Dam depot | Industrial | Pipes, ledgers, fluorescent | Cold bone |
| Summit hub | Epic | Wind, chain of beacons far below | Ember vs navy |

## 5. Characters, uniforms, props
- CDC uniform: slate green, ember-orange armband per Lantern; ranks via shoulder bars (1–6 notches).
- Compact: mismatched gear, rust-coloured sashes.
- Silhouette test: each of the 10 cast members must be recognisable in solid black at 64 px height.
- Weapons: grounded, slightly stylised; no real-brand markings.
- Materials: painted albedo with subtle roughness; no photoreal PBR requirement.

## 6. Lighting and atmosphere
One key light + one warm accent; baked ambient shading in sprites; dynamic lights limited to flares and beacons (max 2 dynamic lights per scene on Low/Med). Fog/mist as layered scrolling alpha sheets, not volumetrics. Weather: Clear, Rain, Fog; Day/Dusk/Night via palette grade overlay + lighting sprites.

## 7. Cinematic composition
Letterbox 2.39:1 for cutscenes; rule-of-thirds; characters framed with their warm light edge; slow parallax; no more than 45 s each.

## 8. Mobile readability
Characters ≥ 96 px tall at reference 1080p height; outlines 2 px darker value; avoid values within 10% of background; important props have rim light. HUD never over centre 60%.

## 9. Quality profiles (targets; see [17](17_PERFORMANCE_AND_DEVICE_COMPATIBILITY.md))
| Profile | Sprite res | Particles | Dynamic lights | Parallax layers | FPS target |
|---|---|---|---|---|---|
| Low | 0.5× atlas | ≤ 60 | 0 | 2 | 30 |
| Medium | 0.75× | ≤ 150 | 1 | 3 | 30–60 |
| High | 1.0× | ≤ 300 | 2 | 4 | 60 |
Same scene composition across profiles; only density/resolution changes so gameplay stays identical.

## 10. UI-to-world consistency
Panels use slate with bone type, hairline ember rules; lantern icon motif; no neon gradients.

## 11. Anti-patterns
Generic gradient backgrounds, lens-flare overload, cluttered HUD, inconsistent proportions between characters, purely decorative UI chrome.

## 12. Acceptance
| ID | Criterion |
|---|---|
| ART-001 | Every environment has a one-page style frame approved before production |
| ART-002 | Silhouette test passes for all cast |
| ART-003 | Contrast check passes at 3 quality profiles |
