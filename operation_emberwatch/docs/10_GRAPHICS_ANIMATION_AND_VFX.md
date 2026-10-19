# 10 — Graphics, Animation and VFX

**Purpose:** Specify visual asset types, runtime budgets and implementation checks.  
**Status:** Proposed / Needs Validation  
**Last reviewed:** 2026-10-19

## Asset list and delivery format

| Asset group | Examples | Preferred source format | Runtime format / notes |
|---|---|---|---|
| App icon | Emberwatch beacon mark | Vector master + layered source | Android adaptive icon assets produced by engine/build pipeline; check store dimensions current at release. |
| Splash/title art | Mara silhouette, storm coast, relay | Layered PSD/Krita/Blender scene | Compressed texture or render; fallback static image. |
| Environment | Academy yard, road, relay station, shelter | Blender source + textures | Engine scene/prefab and mesh assets; modular pieces snapped to documented grid. |
| Characters | Mara, Ivo, Jun, Or, Vale, Quill, Rafi, generic civilians/hostiles | Blender source, rig definition, texture sources | Engine-ready skinned meshes, materials, LOD chain. |
| Props | Relay console, breaker, gate, paper logs, toolkit, beacon lens | Blender source | Prefabs with simple colliders and interaction anchors. |
| Animation | locomotion, aim, reload, interact, help, gestures, expressions | Source DCC project | Animation clips and controller/state machine. |
| VFX | rain, drizzle, dust, sparks, beacon glow, impact puff | Source project/prefab | Scalable particle systems; low setting variants. |
| UI | HUD, icons, map markers, menus, portraits | SVG/vector source, design file | Atlas/font/vector assets with authored states. |
| Mission illustration | briefing cards, codex evidence | Vector/painted source | Compressed WebP/texture; target-readable crop. |
| Cinematic | relay activation, handover, ending | Engine scene + animation track | Real-time timeline preferred; pre-render only after package-size/performance review. |
| Store materials | screenshots, feature graphic, promo still | Layered design source | Export exact sizes per current Play Console listing requirements. |

## Naming convention

Use `CATEGORY_Subject_Variant_Quality_V##` where needed. Examples:

- `CHR_MaraVenn_Body_High_v01`
- `ENV_Greywater_RelayRoom_LayoutA_v02`
- `PRP_Emberwatch_Console_Interactable_v01`
- `ANM_Mara_Repair_Start_v01`
- `VFX_Relay_SignalGlow_Med_v01`
- `UI_ObjectiveMarker_Active_v01`
- `AUD_Music_MissionM05_Tension_v01`

Do not include spaces, local user names or temporary “final_final” labels in committed asset paths. Source asset and runtime import must be traceable to the same asset manifest ID.

## Unit, scale and pivot standards

- Use engine-standard metric units; establish a documented character height and human scale during graybox.
- Apply transforms before export; define forward axis, up axis and root orientation per engine export preset.
- Character pivots at feet/root, props at functional anchor or geometric center as required; interaction anchor is a separate named transform.
- Modular environment snap grid: start with 0.5 m and 1 m increments; non-grid pieces need an explicit reason.
- Collision geometry is simpler than visual geometry. Avoid complex triangle mesh colliders on moving objects.
- Texture naming must distinguish base color, normal, packed mask and emissive maps; compression preset chosen by actual map type.

## Model and texture import checks

1. Scale, orientation, normals and pivot match source specification.
2. No missing materials, invalid shader, unused meshes or broken UVs.
3. LOD transition is not visible as a distracting silhouette pop at expected camera distance.
4. Texture memory is measured per scene, including mipmaps, not just source PNG size.
5. Transparency is used only where needed; sort issues are tested on device.
6. Characters share skeleton/animation rigs where practical; unique rigs are justified in the asset manifest.
7. Addressable/asynchronous assets have explicit loading fallbacks and are not referenced by fragile implicit name strings.

## Animation inventory for MVP

### Player
Idle, walk/jog/sprint, start/stop, strafe, turn-in-place, aim idle, aim walk, fire, reload, hit, stumble/knockdown, interact tap, repair hold loop/start/end, help/revive, climb/vault if used, dialogue idle, relieved/concerned gesture.

### Companion
Follow locomotion, hold idle, assist gesture, interact at authored anchor, acknowledge order, hit reaction, safe recovery/teleport fade if fallback needed.

### Generic hostile
Patrol locomotion, notice, suspicion, engage, fire, seek cover, stagger, surrender/retreat/disabled, scripted exit. Keep number of clips deliberately small; do not add advanced tactical animation absent gameplay value.

### Civilian
Idle variants, walk, wait, follow guide, help interaction, relieved/concerned reaction. Avoid expensive crowd behavior in MVP.

## VFX hierarchy

- **Tier 0 required:** objective beacon pulse, hit/impact cue, repair sparks, low-health cue, mission-complete transition.
- **Tier 1 atmosphere:** rain, puddle ripple, dust, light fog, distant wind debris.
- **Tier 2 optional:** extra debris, secondary reflections, decorative sparks, dense fog cards.

Tier 0 stays readable at every quality level. Tier 2 is disabled first when thermals or GPU time exceed target. VFX cannot obscure objectives, enemy silhouettes, subtitles or touch controls.

## Cinematic implementation

Prefer real-time engine cinematics for camera reliability and modest storage footprint. Each shot has a maximum duration target and exit point; skip is enabled after first viewing except where a minimal transition prevents state corruption. Keep playable control returned within roughly 30 seconds for intro, 10 seconds for mid-mission transitions and 45 seconds for the ending. Measure timings; these are design targets. During cutscenes, mission clocks and threats pause. Dialogue subtitles are generated from the same line data as voice events.

## Performance profiling gates

At graybox: record draw calls, triangles, texture memory and frame-time per representative scene. At vertical slice: profile rain, character skinning, shadows, transparent effects and post-processing on low/mid/high device representatives. At release: profile a 10-minute thermal run, repeated scene changes, memory after returning to menus and low-memory recovery. Do not state that budgets are achieved until the test report is recorded.

## Related documents

[Visual art direction](09_VISUAL_ART_DIRECTION.md) · [Audio/cinematics](11_AUDIO_AND_CINEMATIC_DIRECTION.md) · [Performance plan](17_PERFORMANCE_AND_DEVICE_COMPATIBILITY.md) · [Asset pipeline](16_ASSET_PIPELINE_AND_FILE_CONVENTIONS.md)
