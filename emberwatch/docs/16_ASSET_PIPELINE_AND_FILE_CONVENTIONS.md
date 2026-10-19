# 16 — Asset Pipeline and File Conventions

**Purpose:** Folder layout, naming, formats, ownership, validation. **Status:** Proposed · **Last reviewed:** 2026-10-18
Asset needs: [10](10_GRAPHICS_ANIMATION_AND_VFX.md), [11](11_AUDIO_AND_CINEMATIC_DIRECTION.md).

## 1. Folder structure
```
/assets
  /art
    /characters/<char_id>/       # atlases + json
    /environments/<env_id>/
    /props/
    /ui/icons/ , /ui/panels/
    /portraits/
    /cutscenes/<cs_id>/
    /vfx/
  /audio
    /music/ , /sfx/ , /voice/<lang>/
  /fonts/
  /store/                        # listing graphics (not bundled in app)
/sources                         # .blend, .psd, .aseprite — NOT shipped (git LFS)
/assets/manifest.json            # every asset: id, path, owner, licence, status
```

## 2. Naming
`<category>_<name>_<variant>_<index>.<ext>` all lowercase, underscores. Examples: `char_voss_walk_dir0_00.png`, `env_tower7_bg_far.webp`, `sfx_beacon_ignite_01.ogg`, `mus_summit_action_stem2.ogg`, `ui_icon_cover.svg`.
Mission/character IDs follow canon: `P-01`, `M-101`, `CH-02` (stored lowercase with underscore in file names, e.g. `m_101`, `ch_02`).

## 3. Formats and scale
| Type | Format | Notes |
|---|---|---|
| Sprites | PNG source → atlas WebP/ETC2 (`Needs Validation` on Skia support) | Atlas ≤ 2048² Medium |
| Backgrounds | WebP q80–90 | 1.5 MB cap |
| Icons | SVG source → PNG @1x/2x/3x | 48 dp base |
| Audio | OGG/AAC | per [11](11_AUDIO_AND_CINEMATIC_DIRECTION.md) |
| Fonts | TTF/OTF (licensed) | |
Scale convention: 1 world unit = 1 metre = 48 px at High profile reference; character height 1.75 m ≈ 96 px.

## 4. Ownership and licensing
Manifest row fields: `id, path, creator, licence, licenceUrl, purchaseRef, aiGenerated(y/n), reviewedBy, status`. **No asset enters the build without a licence row.** If AI tools generate imagery, record the tool, terms and human edits; avoid prompts that imitate specific third-party IP. Ban: assets ripped from other games, stores or marketing.

## 5. Validation (automated in CI)
| Check | Rule |
|---|---|
| Manifest completeness | Every file in /assets is listed |
| Size caps | Atlas ≤ 4 MB, bg ≤ 1.5 MB, voice clip ≤ 400 KB |
| Naming | Regex pass |
| Dimensions | Atlas power-of-two where required |
| Licence | Non-empty |
| Orphans | Unreferenced assets flagged |

## 6. Pipeline
Blender model → turntable render script (8 dirs) → pack atlas (script in /tools) → manifest update → CI validation → preview build.

## 7. Acceptance
| ID | Criterion |
|---|---|
| AST-001 | CI blocks unlicensed/unlisted assets |
| AST-002 | Build size and memory budgets reported per build |
