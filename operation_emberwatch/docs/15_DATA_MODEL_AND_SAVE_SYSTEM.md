# 15 — Data Model and Save System

**Purpose:** Define persistent data, IDs and recoverable save behavior.  
**Status:** Proposed; serialization format depends on chosen engine  
**Last reviewed:** 2026-10-19

## Separation of static content and player progress

**Static content** includes mission definitions, character dialogue, reward definitions, rank thresholds, item definitions, objective graphs and localization strings. It ships with the game and is versioned with the build.

**Player state** includes selected profile, completed missions, current safe checkpoint, XP, rank, unlocked content, skill selections, relationship bands, story flags, personal bests, settings and save metadata. Player state must not duplicate full authored content blocks.

## Entity model

| Entity | Key fields | Rules |
|---|---|---|
| PlayerProfile | `profileId`, `displayName` optional, `createdAt`, `selectedDifficulty` | No login identifier. Display name is optional and local. |
| CampaignState | `campaignId`, `startedAt`, `chapterId`, `currentMissionId`, `safeCheckpointId`, `campaignStatus` | MVP supports one active local campaign; New Campaign warns before replacing it. |
| MissionProgress | `missionId`, `status`, `bestMedal`, `bestDuration`, `firstCompletedAt`, `optionalObjectiveIds`, `lastSafeCheckpointId` | Completion status transitions forward; replay may improve records. |
| PlayerProgression | `totalXp`, `rankId`, `skillPoints`, `unlockedSkillIds`, `unlockedCosmeticIds`, `rewardLedger` | Reward ledger keys prevent duplicate grants. |
| RelationshipState | `characterId`, `relationshipBand`, `storyReactionFlags` | Discrete bands and authored flags; no hidden moral total. |
| StoryState | `choiceId` → `optionId`, `storyFlagId` → boolean/string enum, `codexEntryIds` | Choice keys stable across releases. |
| SettingsState | `controlScale`, `handedness`, `cameraSensitivity`, `aimAssist`, `textScale`, `subtitleBackground`, `reduceMotion`, volume values, graphics preset | Validate/clamp on load; safe defaults on unknown values. |
| SaveMetadata | `schemaVersion`, `contentVersion`, `saveTimestampUtc`, `checksum`, `lastWriteReason`, `buildVersion` | Debug details may be stripped from release output if not needed. |

## Example mission definition (illustrative schema)

```json
{
  "missionId": "M02_SIGNAL_IN_THE_RAIN",
  "chapterId": "CH01_GREYWATER",
  "entrySceneId": "SCN_M02_GREYWATER_APPROACH",
  "difficultyProfile": "STANDARD_BASELINE",
  "primaryObjectives": [
    {"objectiveId": "OBJ_M02_REACH_RELAY", "type": "ReachMarker", "markerId": "MRK_RELAY_ENTRY"},
    {"objectiveId": "OBJ_M02_RESTORE_BEACON", "type": "ActivateInteractable", "targetId": "INT_RELAY_BREAKER"}
  ],
  "optionalObjectives": [
    {"objectiveId": "OPT_M02_RECOVER_KIT", "type": "CollectItem", "targetId": "ITM_MAINTENANCE_KIT"}
  ],
  "checkpointIds": ["CP_M02_RELAY_ENTRY", "CP_M02_BEFORE_BREAKER"],
  "rewardKey": "M02_BASE_REWARD",
  "nextMissionId": "M03_THE_MISSING_MINUTE"
}
```

Treat this as a data-shape example, not engine-specific runtime code. Validate that all referenced IDs exist before packaging.

## Example character definition (illustrative schema)

```json
{
  "characterId": "CHR_MARA_VENN",
  "displayNameKey": "character.mara.name",
  "callsign": "Venn",
  "roleKey": "character.mara.role",
  "modelAssetId": "CHR_MaraVenn_Body_High_v01",
  "voiceStyleKey": "voice.mara.style",
  "relationshipBands": ["DISTANT", "WORKING", "TRUSTED"],
  "missionAppearances": ["M01_THE_ASSESSMENT", "M02_SIGNAL_IN_THE_RAIN", "M03_THE_MISSING_MINUTE", "M04_A_ROAD_THROUGH_GREYWATER", "M05_EMBERWATCH"]
}
```

## Save write contract

1. Capture a consistent snapshot at a safe event boundary.
2. Serialize to a temporary path or temporary slot; include schema/content/build versions.
3. Flush and close the write.
4. Read/validate the temporary data and checksum if implemented.
5. Replace the active file atomically where platform filesystem guarantees allow; retain previous known-good backup.
6. On failure, keep the last known-good save and notify the player with a recoverable message.

Don't write the full save every frame. Save on new campaign, checkpoint, mission completion, committed choice, one-time reward, settings confirmation and exit to menu. Coalesce repeated setting changes briefly but ensure an immediate safe write on explicit confirmation/backgrounding if supported.

## Recovery and corruption policy

- Missing save → first-launch or New Campaign state.
- Valid save at current schema → load normally.
- Older schema → backup then migrate one version at a time; validate after each step.
- Unknown newer schema → preserve file, show compatibility message, offer safe new save only after explicit consent.
- Malformed/corrupt save → attempt known-good backup; if backup works, explain recovery; otherwise provide “Start new campaign” and “Export diagnostics” only if an implementation exists and is privacy-safe.
- Missing mission/checkpoint ID after content update → map through a migration table or fall back to last completed mission hub; never force-load a nonexistent scene.
- Invalid numeric values/enum IDs → clamp to safe default and log diagnostic in development.

## Idempotency

Rewards and objective transitions use immutable keys. Before applying `M05_BASE_REWARD`, check `rewardLedger` for that key. Commit reward and mission completion in the same snapshot. Dialogue choices commit only after the player confirms one option. When replaying, write `bestDuration = min(old, current)` when duration is a metric and best medal as the highest achieved; never overwrite the first-completion timestamp.

## Local privacy

MVP saves ordinary game progress and settings only. Do not store contact lists, precise location, microphone recordings, advertising ID or real identity. A local display name is optional and can be cleared by deleting the campaign/save. Explain in privacy/legal UI that save data is stored on the device and uninstalling may remove it, subject to actual platform backup behavior; verify engine/device behavior before making a public claim.

## Versioning and migration tests

Every schema change requires fixture files for previous released schema versions and tests for old-to-new migration. Add migrations rather than editing past migration semantics. Each save schema has a monotonic integer version; content version can be a release string. Test interrupted save replacement, low storage, damaged temp file, corrupted active save, corrupted backup, unsupported newer schema, duplicate event, and forced app termination at checkpoint.

## Related documents

[Architecture](12_TECHNICAL_ARCHITECTURE.md) · [Progression](07_PLAYER_PROGRESSION_AND_ECONOMY.md) · [Task backlog](23_TASK_BACKLOG_AND_ACCEPTANCE_CRITERIA.md) · [QA](20_TESTING_AND_QUALITY_ASSURANCE.md)
