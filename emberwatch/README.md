# OPERATION: EMBERWATCH

**Status:** Proposed (documentation phase) · **Last reviewed:** 2026-10-18 · **Working title** (see [REL-010](docs/21_GOOGLE_PLAY_RELEASE_PLAN.md) for name/trademark checks)

An original, single-player, story-driven military action and progression game for Android. You begin as an inexperienced recruit at **Halvorn Ridge Academy**, train with **Third Lantern** squad, and are drawn into a flood-relief crisis in the **Harlow Reach** that hides a conspiracy about a failed beacon network called **Emberwatch**.

> This repository currently contains **documentation only**. No game source code exists yet. Per the master brief, implementation starts only after the Phase 0 technical experiments in [docs/22_MVP_ROADMAP_AND_PHASE_GATES.md](docs/22_MVP_ROADMAP_AND_PHASE_GATES.md) are complete.

## One-paragraph pitch
Landscape, offline, 2.5D tactical action. Short, varied missions (training, recon, rescue, escort, traversal, squad coordination, a few cinematic beats) feed one continuous campaign. Your choices change squad trust, a few mission outcomes and the ending. No payment is required, no account, no backend.

## How to use these documents
1. Read [docs/28_DOCUMENTATION_INDEX.md](docs/28_DOCUMENTATION_INDEX.md) for the reading order.
2. Treat each shared system as having **one authoritative file** (listed in the index). Other files link, they do not redefine.
3. Every decision carries a status: `Proposed`, `Approved`, `Needs Validation`, or `Deferred`. Nothing is `Approved` until the founder signs it off in [26_RISK_REGISTER_AND_DECISION_LOG.md](docs/26_RISK_REGISTER_AND_DECISION_LOG.md).
4. AI coding agents: start from [docs/25_AI_PROMPT_LIBRARY.md](docs/25_AI_PROMPT_LIBRARY.md); every prompt names the files it must read.
5. Do not commit secrets (signing keys, passwords, API keys). See [19_PRIVACY_SECURITY_AND_COMPLIANCE.md](docs/19_PRIVACY_SECURITY_AND_COMPLIANCE.md).

## Headline decisions (all `Proposed`)
| Topic | Decision | Authority |
|---|---|---|
| Orientation | Landscape, locked | [08](docs/08_UI_UX_AND_SCREEN_SPECIFICATIONS.md) |
| Rendering | Option A: React Native + Expo + Skia 2.5D with pre-rendered sprites; fallback to Godot (Option C) if experiments fail | [13](docs/13_TECH_STACK_AND_RENDERING_DECISION.md) |
| MVP campaign | Prologue + 11 missions, 4 chapters, 3 endings | [05](docs/05_CAMPAIGN_AND_MISSION_DESIGN.md) |
| Monetization | None in MVP | [18](docs/18_MONETIZATION_AND_FAIR_PLAY.md) |
| Backend | None; local saves only | [15](docs/15_DATA_MODEL_AND_SAVE_SYSTEM.md) |
| Target API | Android 16 (API 36) per Google Play policy, verified 2026-10-18 | [21](docs/21_GOOGLE_PLAY_RELEASE_PLAN.md) |

## Originality
Inspired only at the level of genre by *Military Academy 3D*. No story, names, art, UI, code or marketing is reused. See [24_REFERENCE_ANALYSIS_AND_ORIGINALITY.md](docs/24_REFERENCE_ANALYSIS_AND_ORIGINALITY.md).

## What is NOT done
No assets exist, no code exists, no device test has been performed, and nothing has been submitted to Google Play. All performance numbers are targets or estimates.
