# 09 — Visual Art Direction

**Purpose:** Establish an original, producible visual identity.  
**Status:** Proposed  
**Last reviewed:** 2026-10-19

## Creative statement

**“A warning light in weathered stone.”** The visual world blends storm-worn coastal architecture, practical rescue equipment, damp slate materials and small amber/coral signals of human action. It should feel cinematic in composition but designed for instant mobile readability. Avoid glossy sci-fi armor, excessive gradients, anonymous AI-looking detail, real-world insignia and generic grey warzone imagery.

## Art principles

1. **Readable silhouette:** player, allies, civilians and hostiles differ by outline, posture and accessories before color is considered.
2. **Human-scale evidence:** paper tags, repair marks, hand-written shelter boards, patched rain gear and maintenance panels tell the story.
3. **Storm as atmosphere, not obstruction:** mist and rain frame silhouettes but never obscure critical navigation or targets.
4. **Signal light with purpose:** amber light marks emergency infrastructure, active objectives or safe wayfinding; it is not decorative trim everywhere.
5. **Reuse with variation:** one modular coastal kit creates several layouts through orientation, material wear, lighting and prop placement.
6. **No direct imitation:** reference screenshots are used only to discuss clarity, contrast and camera framing. No traced composition, copied asset, logo, exact colorway, character or UI layout.

## Palette tokens

| Token | Hex | Usage | Constraint |
|---|---|---|---|
| Deep Ink | `#171E22` | UI ink, darkest cloth | Avoid crushing detail on low-brightness displays. |
| Wet Slate | `#33434A` | Stone, metal and uniform base | Variation comes from roughness, not black noise. |
| Storm Blue | `#536B78` | Distant environment and cool shadows | Keep characters separable from backdrop. |
| Sea Glass | `#8BA7A0` | Safe-wayfinding and weathered paint | Never sole signal for success. |
| Paper Ivory | `#E9E2D0` | Paper, UI text, rain canvas | Preserve highlight detail. |
| Signal Amber | `#E6A34B` | Relay lamps, key props, objective accent | Use sparingly; objective lamps remain readable. |
| Rescue Coral | `#D96B55` | Danger state, restricted barrier labels | Pair with icon/shape. |
| Moss Green | `#53664C` | Vegetation, field textile | No camouflage that hides ally silhouette. |
| Oxide | `#7A5140` | Rust, aged fixtures | Use as material history. |
| Night Navy | `#202B39` | Night background | Retain separation between hostiles and environment. |

Target rough material groups: matte fabric, rough wet stone, painted scratched metal, translucent relay lens, rubberized grip, rain-glossed ground. Do not add micro-scratches to every surface; large value grouping matters more at phone scale.

## Environment direction

### Meridian Academy
- Geometry: simple long blocks, visible painted lane markings, a central instruction board, training props and a decommissioned flood-control building.
- Composition: strong horizontal routes and distant hills. Place tutorial prompts against low-detail surfaces.
- Lighting: neutral morning sky, clear key direction, soft shadows; avoid deep dark corners in onboarding.
- Props: reusable cones, rope barriers, safety markers, modular gate, practice targets and shelter signage.

### Greywater Reach
- Geometry: irregular seawalls, narrow service paths, cliff-side cable trays, short ladders and one strong landmark per space.
- Materials: dark basalt-like stone, worn pale concrete, painted metal, damp wood and salt-streaked glass.
- Weather: rain streaks and wind audio are scalable; low quality can reduce particle density but preserve the same visibility and gameplay cues.
- Lighting: overcast cool fill; amber relay lens contrasts at key beats. Rescue zones use warmer practical light without overdone bloom.

### North Relay Station
- Three clear layers: approach path, maintenance shell, beacon roof. Avoid maze-like layout. The station must read as emergency infrastructure, not a weapon facility.
- Relay interface uses large physical switches, labeled cable channels and a simple mechanical indicator. Exact electrical functionality is fictional and not a real engineering tutorial.
- Use paper maintenance cards to support the mystery, with no small text required for objective completion.

## Character and uniform design

- Veyra's uniform language is original: slate field cloth, restrained sea-glass panels and plain modular straps. No real flags, ranks, unit patches or copied uniforms.
- Mara's amber pack tab is her primary gameplay silhouette marker. Companion Jun uses a pale sand bag; Ivo uses a blue-grey shoulder marker.
- Hostile GMF characters use weatherproof workwear and mismatched protective layers—no skull motifs or stereotyped cultural markers.
- Civilians carry practical umbrellas, bags or shelter ID cards; their silhouettes should not mimic combatants.
- Face detail is concentrated around eyes, brow and mouth; hair and accessories must remain recognizable at low LOD.

## Lighting and atmosphere

- Use one primary directional/sky lighting setup per exterior and baked or carefully limited lights indoors where possible.
- Treat volumetric fog, transparent rain, screen-space effects and multiple dynamic shadows as expensive toggles.
- Keep objective surfaces within high local contrast. Keep subtitles on a separate UI plane with controlled backdrop opacity.
- Night mode uses value separation and practical lamps; do not force players to raise screen brightness to see doors or controls.
- Weather cannot change the gameplay collision model unexpectedly.

## Animation and VFX tone

Animations are weighty but compact: decisive start/stop, limited aim sway, clear recoil, readable stagger and purposeful tool use. Avoid exaggerated ragdoll comedy unless it is part of a consistent tone (not planned). Impact effects are brief dust/spark/water sprays; no screen-filling blood, persistent gore or oversized explosions. Screen shake defaults low and can be reduced.

## UI-world integration

UI action amber can echo the relay lamp, but interface panels use solid color and clear typography rather than faux military HUD textures. Objective markers are subtle and contextual. Use world-space icon only when the target is far enough away to justify it; do not stack large waypoint, interact label, objective panel and subtitle on the same pixel region.

## Asset and quality budgets (initial targets)

These are **prototype budgets**, not verified platform guarantees. The technical art lead may adjust them after device profiling, recording the evidence in the decision log.

| Asset / setting | Initial target | Notes |
|---|---|---|
| Player mesh | 25k–45k triangles high LOD | Validate silhouette, deformation and mobile skinning. |
| Main NPC mesh | 18k–35k triangles high LOD | Reuse base rigs/materials where possible. |
| Small background civilian | 4k–12k triangles | May use simpler facial features. |
| Typical material slots per character | 3–5 | Avoid many unique draw calls. |
| Texture maps | 1K common; 2K only hero/close-up when memory permits | Prefer packed masks and sensible mipmaps. |
| Main environment texture | 512–2K by projected importance | Texture atlases for modular prop families. |
| Visible dynamic lights | 1–3 meaningful moving lights in a combat cell | Actual cost depends on render pipeline/device. |
| Active high-detail characters | 4–7 including player and NPCs | Screen and GPU budget must determine final cap. |
| Rain/particle density | High profile baseline; medium/low presets reduce count | Preserve sightlines and cue timing. |
| Shadows | One main directional shadow; local shadows selectively | Low preset can disable nonessential dynamic shadows. |

## Originality and asset rights

Every produced/commissioned asset must have recorded author/vendor, license, permitted platforms, modification rights, attribution requirement and proof of purchase/permission. Generative tools may assist concepts, but final character consistency, model topology, typography and licensing must be reviewed. Do not upload sensitive proprietary material into tools without appropriate rights and data review.

## Related documents

[Graphics/animation/VFX](10_GRAPHICS_ANIMATION_AND_VFX.md) · [UI tokens](08_UI_UX_AND_SCREEN_SPECIFICATIONS.md) · [Asset pipeline](16_ASSET_PIPELINE_AND_FILE_CONVENTIONS.md) · [Originality](24_REFERENCE_ANALYSIS_AND_ORIGINALITY.md)
