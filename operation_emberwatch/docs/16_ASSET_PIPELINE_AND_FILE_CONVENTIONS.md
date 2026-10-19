# 16 — Asset Pipeline and File Conventions

**Purpose:** Make asset production traceable, safe to import and legally auditable.  
**Status:** Proposed  
**Last reviewed:** 2026-10-19

## Repository layout

The following is a proposed layout for the eventual game project. It is not currently generated as code or an engine project.

```text
Assets/                    # or engine-equivalent resource folder
  Art/
    Characters/Source/
    Characters/Runtime/
    Environments/Source/
    Environments/Runtime/
    Props/Source/
    Props/Runtime/
    UI/Source/
    UI/Runtime/
  Animation/
    Source/
    Runtime/
  Audio/
    Music/Source/
    Music/Runtime/
    SFX/Source/
    SFX/Runtime/
    Dialogue/Source/
    Dialogue/Runtime/
  VFX/
  Scenes/
    Boot/
    Menus/
    Missions/M01...M05/
  PrefabsOrPackedScenes/
  ScriptableContent/
    Missions/
    Characters/
    Dialogue/
    Rewards/
  Localization/
  ResourcesOrAddressables/  # only if engine system requires it
  Tests/
Docs/                      # canonical design documents in repo root docs/ for this package
```

Source art may live in a separate controlled repository if files are large, but the manifest and source URI/hash must remain in the project. Use Git LFS or an approved asset versioning tool only after its storage limits and restore process are documented.

## Naming and stable IDs

- File path uses lowercase folder structure and stable, descriptive asset names; asset IDs are separate from display names.
- Character IDs: `CHR_MARA_VENN`, `CHR_IVO_SENN`, `CHR_JUN_ARAS`, `CHR_CAPTAIN_LEENA_OR`, `CHR_SOREN_VALE`, `CHR_NADI_QUILL`, `CHR_RAFI_DARO`.
- Mission IDs: `M01_THE_ASSESSMENT`, `M02_SIGNAL_IN_THE_RAIN`, `M03_THE_MISSING_MINUTE`, `M04_A_ROAD_THROUGH_GREYWATER`, `M05_EMBERWATCH`.
- Choice IDs: `CHOICE_01_SHELTER`, `CHOICE_02_RECORD`, `CHOICE_03_HANDOVER`.
- Localization keys are namespaced, e.g. `mission.m02.objective.restore_relay`, `choice.m03.share_record.option_a`.
- IDs must not include localized display text. Renames require a migration/alias if saved in player data.

## Asset manifest fields

Every shipped asset must be listed in a CSV/JSON manifest with:

- `assetId`, `sourcePath`, `runtimePath`, `category`, `owner`, `createdOrLicensedBy`, `licenseName`, `licenseEvidencePath`, `attributionText`, `allowedPlatforms`, `modificationAllowed`, `sourceHash`, `importSettingsProfile`, `memoryClass`, `usedByMissionIds`, `status`, `lastValidated`.

Use `status` values `CONCEPT`, `IN_PROGRESS`, `REVIEW`, `APPROVED`, `DEPRECATED`. An asset with unclear rights cannot be marked APPROVED for shipping.

## Production pipeline

1. **Brief:** include gameplay purpose, silhouette/readability, target device class, scale, expected use sites and acceptance checks.
2. **Blockout:** graybox interaction bounds and camera framing in engine before high-quality art.
3. **Create or source:** build, commission, license or generate concept reference with documented rights.
4. **Review:** art lead checks originality, consistency, scale, rig/UVs, texture density and license.
5. **Import:** apply shared presets, create LOD/collider variants and attach stable asset ID.
6. **In-engine validation:** test at real camera distance, under rain/night profile, in low quality and on a physical device.
7. **Approve and freeze:** manifest has evidence; avoid silent changes after a content-lock milestone.
8. **Deprecate:** mark old asset, remove unreferenced runtime files only after dependency search and clean build.

## Asset generation and third-party licenses

Do not download or extract the reference game's models, textures, sounds, screenshots or UI. Do not ship AI-generated imagery with visible text/logos unless those are reviewed and legally permitted. Store tool terms/version and any source inputs needed for provenance. Every font, music track, sample, model, animation and icon needs a license check. A paid asset marketplace license is not automatically transferable to collaborators or all platforms; record the exact license conditions.

## Build validation

Before merge, run editor import/validation and a clean Android build. Catch: missing textures, broken references, duplicate stable IDs, invalid localization keys, unsupported shaders, missing audio clips, unreferenced large assets, wrong compression profile, unauthorized file additions and case-sensitive path mismatches. The runtime must not depend on files stored only on one developer's desktop or AI tool session.

## Compression and storage rules

- Keep source and runtime textures separate.
- Use platform-appropriate texture/audio compression; measure visual quality and decode overhead on Android.
- Use mipmaps for 3D textures where appropriate; don't apply them indiscriminately to UI assets.
- Stream large music/cinematic tracks only when the engine's behavior and memory profile are verified.
- Remove source reference images from public builds unless licensed for distribution.
- Avoid duplicate copies of a model with identical content; share a source and generate required LODs/variants.

## Localization workflow

All player-visible string content is keyed. Export current strings with key, English source, context, character length target, notes and status. Review dialogue choices and mission objectives for meaning preservation. Do not place essential objective wording only in textures or baked art. Include pseudo-localization and 35% expansion checks before release; real translation scope is a separate budget decision.

## Definition of asset-ready

Asset is ready only when: source file and rights evidence exist; stable ID assigned; import settings approved; runtime variant imported; target-device screenshot or video reviewed; LOD/material cost known; used mission and dependencies listed; no open critical issue remains.

## Related documents

[Art direction](09_VISUAL_ART_DIRECTION.md) · [Graphics/VFX](10_GRAPHICS_ANIMATION_AND_VFX.md) · [Audio](11_AUDIO_AND_CINEMATIC_DIRECTION.md) · [Privacy/IP](19_PRIVACY_SECURITY_AND_COMPLIANCE.md)
