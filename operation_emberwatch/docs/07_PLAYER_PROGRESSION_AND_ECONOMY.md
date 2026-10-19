# 07 — Player Progression and Economy

**Purpose:** Define rewards and advancement without grind or paid power.  
**Status:** Proposed; values are tuning estimates  
**Last reviewed:** 2026-10-19

## Design policy

Progression rewards curiosity, completing missions and helping people. It must not punish casual play, gate story completion behind perfect scores or rely on randomized loot. The MVP has no premium currency, paid boosts, energy timers, loot boxes, daily streaks or forced advertising.

## Experience and ranks

XP is a finite campaign record. Values below are starting estimates for tuning, not tested balance data.

| Rank | Total XP threshold | Narrative meaning | Unlock |
|---|---:|---|---|
| Recruit | 0 | New academy arrival | M01, training board |
| Cadet | 100 | Completes first assessment | M02, basic field profile |
| Field Cadet | 180 | Demonstrates reliable field support | M03 and M04 as story allows |
| Specialist | 520 | Trusted to coordinate relay restoration | M05 and campaign ending |
| Section Lead | 800 | Optional long-term milestone, not required by MVP | Proposed post-MVP replay challenge |

Base mission rewards: M01 100 XP, M02 80 XP, M03 80 XP, M04 120 XP, M05 140 XP (520 XP total). With the current proposed thresholds, M01 awards Cadet, M02 crosses Field Cadet, and M05 awards Specialist. Section Lead at 800 XP remains a post-MVP replay/expansion milestone. Optional objectives award a codex/cosmetic record rather than extra XP in the MVP, so the expected main-campaign total is stable. These figures are preliminary and must be rebalanced after playtesting. Do not grant a second full reward on replay.

## Optional objectives and medals

| Result | Reward | Limits |
|---|---|---|
| Primary objective completion | Full base XP and story progress | Always achievable with default path. |
| One optional objective | Small bonus or codex item | Does not gate mission access. |
| Bronze / silver / gold drill | Score record and cosmetic badge | No permanent combat stat penalty. |
| All evidence gathered | Expanded codex entry / epilogue line | Story ending still resolves without all evidence. |
| Replay best score | Best medal stored | No unlimited XP farming. |

## Skills

MVP provides at most three optional skill points. Candidate skills:

1. **Steady Focus:** slightly wider aim-assist acquisition window; never necessary to hit required targets.
2. **Quick Hands:** reduce hold time for non-combat interactions within an authored limit; accessibility setting remains separate.
3. **Field Utility:** add one extra flare/patch charge in missions that support that utility.

Each skill has a plain description, preview and confirmation before spending. Points may be refunded outside active missions in MVP to encourage experimentation. Skills do not have rarity tiers.

## Equipment and cosmetics

Loadout changes are limited to cosmetic uniform accents, pack silhouette variations, reticle shape/scale and a small set of fictional utility tools. Story-essential equipment is granted automatically before use. Any future weapon selection must be a readability-driven arcade choice with bounded balance impact; no realistic attachment economy in the MVP.

## Relationship states

Store numeric trust in a bounded range (recommended 0–3 discrete bands) or explicit story flags rather than an unexplained large number. Each major companion may be `DISTANT`, `WORKING`, or `TRUSTED`. Transition rules are documented per choice; players receive reaction lines that explain shifts. Trust affects banter, one assist animation or epilogue lines—not mission access or combat damage multipliers.

## Difficulty balance

Story mode: generous aim assist, readable warning cues, one extra checkpoint where appropriate, reduced enemy pressure. Standard: intended baseline. Field difficulty is deferred unless QA verifies it. Never scale enemy health endlessly to match player level; mission pressure is authored and stable. Changing difficulty does not invalidate story completion or achievements.

## Replayability and completion

After M05, players can replay completed missions, attempt missed optional objectives, improve drill medals and browse the codex. The post-campaign screen states “Local story complete” and does not imply that more missions already exist. Future content must be clearly labelled as not yet available if advertised at all.

## Economy data integrity

Every award uses a unique key such as `M02_BASE_REWARD` and a `granted` record. Grant is idempotent. Replaying a mission updates best score and optional completion flags but does not repeat first-completion XP or one-time unlocks. No online server is needed for local-only progression; tamper resistance is not a priority for this offline single-player MVP.

## Analytics stance

No third-party analytics SDK is required. QA may collect local debug counters in development builds, but production telemetry is deferred and requires a privacy review, disclosure changes and measured product value. Do not collect identity or precise location data to support progression.

## Related documents

[GDD](02_MASTER_GAME_DESIGN_DOCUMENT.md) · [Save model](15_DATA_MODEL_AND_SAVE_SYSTEM.md) · [Fair play](18_MONETIZATION_AND_FAIR_PLAY.md) · [QA](20_TESTING_AND_QUALITY_ASSURANCE.md)
