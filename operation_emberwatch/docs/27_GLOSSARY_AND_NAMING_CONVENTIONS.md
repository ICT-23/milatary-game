# 27 — Glossary and Naming Conventions

**Purpose:** Prevent inconsistent terminology across writing, code and tests.  
**Status:** Proposed  
**Last reviewed:** 2026-10-19

## Canonical terms

| Term | Definition / usage |
|---|---|
| **Veyra** | Fictional maritime republic where the game takes place. Never present it as a real nation. |
| **The Long Rain** | Severe storm that preceded the game's opening and exposed gaps in the warning network. |
| **Emberwatch** | Veyra's civil-warning relay network; not a weapon system. Capitalize as the proper name. |
| **Meridian Defence Academy** | Fictional academy where Mara trains. British spelling “Defence” is canonical. |
| **Meridian Defence Corps (MDC)** | Fictional uniformed corps supporting relief corridors and restoration. |
| **Veyra Civil Response Service (VCRS)** | Civilian service maintaining warning infrastructure and evacuation operations. |
| **Greywater Mutual Front (GMF)** | Loose coastal coalition with mixed motives; do not label every member a villain. |
| **Relay Committee** | Cross-agency group responsible for Emberwatch upgrade decisions. |
| **Mara Venn** | Playable protagonist; callsign/short addressing: Venn. |
| **Ivo Senn** | Rival cadet who becomes a collaborator. |
| **Captain Leena Or** | Academy field instructor. Refer to as Captain Or in dialogue unless intimacy justifies Leena. |
| **Jun Aras** | Medic trainee and active companion. |
| **Nadi Quill** | Civil response relay technician. |
| **Soren Vale** | Former engineer and senior GMF leader. |
| **Rafi Daro** | Civilian shelter coordinator. |
| **Mission** | Self-contained playable activity identified by stable M01–M05 IDs. |
| **Objective** | Authored task state inside a mission. Distinguish primary and optional objectives. |
| **Checkpoint** | Safe state from which a failed/terminated mission can resume. |
| **Vertical slice** | One polished representative mission proving the intended quality/technology direction. It is not the full campaign. |
| **MVP campaign** | Five-mission self-contained story target, conditional on vertical-slice gate. |
| **Story flag** | Stable saved key that records a choice or narrative fact; not a broad moral score. |
| **Codex** | In-game archive of discovered characters, places and evidence. |
| **Field utility** | Fictional mission-specific aid such as a flare or patch; not a paid currency. |
| **High/medium/low quality** | Rendering presets; they must not change gameplay rules. |
| **AAB** | Android App Bundle publication artifact used by Google Play under current policy. |
| **Target API** | Android API level an application declares it targets; separate from minimum supported Android OS. |

## Naming conventions

### Documents

`NN_UPPER_SNAKE_CASE.md` for numbered design files, stable links retained once published.

### IDs

- Mission: `M01_THE_ASSESSMENT` … `M05_EMBERWATCH`.
- Character: `CHR_MARA_VENN`, `CHR_IVO_SENN`, `CHR_CAPTAIN_LEENA_OR`, `CHR_JUN_ARAS`, `CHR_NADI_QUILL`, `CHR_SOREN_VALE`, `CHR_RAFI_DARO`.
- Choice: `CHOICE_01_SHELTER`, `CHOICE_02_RECORD`, `CHOICE_03_HANDOVER`.
- Checkpoint: `CP_M02_RELAY_ENTRY` pattern.
- Objective: `OBJ_M02_REACH_RELAY` for primary and `OPT_M02_RECOVER_KIT` for optional.
- Test: `QA-AREA-NNN`; task: `OW-NNN`; risk: `R-NN`; decision: `D-NNN`.
- UI screen: `UI-NN`; cinematic: `CIN-NN`.

IDs are identifiers, not display copy. Never derive them from translated titles at runtime. Preserve old IDs or migrate saved values when content is renamed.

## Writing conventions

- Use “player” for user-controlled human and “Mara” when discussing narrative fiction.
- Use “hostile” instead of real-world unit classifications; NPC AI is intentionally abstract.
- Use “disabled/retreating” rather than graphic kill-focused language in design docs where the outcome doesn't require it.
- “Rescue” and “evacuation” describe the mission's humanitarian objective; don't imply the game teaches real rescue procedures.
- Use `Proposed`, `Approved`, `Needs Validation`, or `Deferred` for decision status.
- Use “target” for a performance goal and “measured” only when actual result evidence exists.
- Dates use ISO format `YYYY-MM-DD` in technical records; prose uses unambiguous month/day/year where needed.

## Common unresolved placeholders

Before release, replace or resolve: `<publisher>` package namespace, actual support email/site, privacy-policy URL, named test devices/OS builds, approved engine/version, licenses/evidence paths, final store asset dimensions, final age rating, finalized localization languages and final Play target API verification. Placeholders must not ship in the listing or app UI.

## Related documents

[Story](03_STORY_WORLD_AND_LORE.md) · [Asset conventions](16_ASSET_PIPELINE_AND_FILE_CONVENTIONS.md) · [QA](20_TESTING_AND_QUALITY_ASSURANCE.md) · [Index](28_DOCUMENTATION_INDEX.md)
