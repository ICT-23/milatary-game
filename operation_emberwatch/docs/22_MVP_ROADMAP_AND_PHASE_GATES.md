# 22 — MVP Roadmap and Phase Gates

**Purpose:** Stage production so the game is validated before content scale increases.  
**Status:** Proposed; durations intentionally not promised  
**Last reviewed:** 2026-10-19

## Scheduling principle

Team size, budget and available hours were not provided; calendar estimates would be misleading. Manage by deliverables and exit gates, then assign dates after the team confirms capacity. The highest risks are real-time 3D performance, touch controls, animation/content cost, save integrity and the actual Play submission path.

## Phase 0 — Documentation and technical validation

**Goal:** Resolve engine, art scale, camera/control and Android build risks.  
**Deliverables:** reviewed docs; toolchain/asset rights plan; Unity-vs-Godot spike (and RN 2.5D spike only if seriously considered); one Android graybox; initial device matrix; decision log entry.  
**Dependencies:** working development machine, physical Android devices, version control.  
**Tasks:** install selected engine and Android toolchain; create landscape app; test input/camera/character/scene loading/save; profile 10-minute loop; confirm export to signed internal build.  
**Acceptance:** Phase 0 spike passes criteria in [technology decision](13_TECH_STACK_AND_RENDERING_DECISION.md).  
**Risks:** engine learning curve; Android graphics variance; source-asset storage.  
**Definition of done:** decision approved or pivot recorded; high-risk assumptions have evidence.

## Phase 1 — Playable prototype

**Goal:** Build the smallest complete gameplay loop.  
**Deliverables:** boot/menu → M01 graybox → movement/interact → objective completion → debrief → safe save/retry.  
**Tasks:** player controller, follow camera, contextual interaction, one objective state machine, checkpoint, local save, pause/resume, basic HUD and one test scene.  
**Acceptance:** a first-time tester completes the task without developer help; force-stop recovery works; controls remain usable on low/mid devices.  
**Risks:** controls feel poor, game loop architecture becomes coupled.  
**Definition of done:** automated tests cover mission state and save logic; no S0/S1 blocker; measured baseline exists.

## Phase 2 — Vertical slice (M01)

**Goal:** Demonstrate intended player experience and production quality.  
**Deliverables:** polished academy-to-shelter mission, core character art, one decision, objective updates, audio direction, checkpoint, debrief, control/accessibility settings and real-device performance report.  
**Tasks:** final graybox geometry; art kit; animation pass; scene lighting; subtitle pipeline; reward/save; tutorials; scripted cinematics; QA/iteration.  
**Acceptance:** mission can be completed/retried, story choice persists, offline mode works, objective readability passes playtest, budget gates pass.  
**Risks:** visual polish masks control issues; content asset costs grow.  
**Definition of done:** vertical slice reviewed by representative players and the project owner; no new campaign production before this gate.

## Phase 3 — MVP campaign (M01–M05)

**Goal:** Deliver complete local story ending.  
**Deliverables:** five mission layouts, dialogue/choice flags, progression, all required models/props, local codex, settings/accessibility, local saves/migrations.  
**Tasks:** build mission shells from shared prefab kits; add mechanics gradually; write and review dialogue; author optional objectives; implement replay/reward policy; localize string keys in one launch language.  
**Acceptance:** all campaign branches reach the completed ending; no mission requires network or perfect score; each mission has checkpoints and retry path.  
**Risks:** branching/cinematic creep; NPC navigation bugs; inconsistent story canon.  
**Definition of done:** content lock plus full regression of campaign and save schema.

## Phase 4 — QA and optimization

**Goal:** Stabilize real-device behavior.  
**Deliverables:** test matrix/report, crash and performance log, verified save migrations, approved asset/licenses, privacy/manifest review, release-candidate build.  
**Tasks:** unit/integration/device tests; low-end optimization; heat/memory run; accessibility check; text expansion; store asset capture; clean build/signing verification.  
**Acceptance:** no open S0/S1; all required quality gates in [QA plan](20_TESTING_AND_QUALITY_ASSURANCE.md) pass with evidence.  
**Risks:** late engine or package updates; platform policy changes; device-specific shader bugs.  
**Definition of done:** release candidate is reproducible and owner signs readiness review.

## Phase 5 — Google Play preparation

**Goal:** Meet current store submission requirements and run required test tracks.  
**Deliverables:** AAB, store listing assets, privacy URL/policy, data safety answers, content rating, target API verification, Play Console testing track, release ledger.  
**Tasks:** review account type/testing criteria; upload internal test; collect issues; run qualifying closed test if applicable; prepare production access request; staged rollout plan.  
**Acceptance:** all current Console-required forms are complete; test feedback reviewed; submission accepted if Google grants approval.  
**Risks:** policy changes, testing cohort attrition, asset/listing mismatch, title/trademark issue.  
**Definition of done:** release status accurately reported; acceptance is not claimed before Console says so.

## Phase-gate rule

No phase begins at scale until the previous phase's acceptance evidence is reviewed. Design changes that increase simultaneous active characters, new biomes, branching endings, online features or unique animations require a scope/cost update before approval.

## Long-term expansion

M06–M10 are detailed but deferred full-campaign continuation missions. They are prioritized only after the five-mission MVP release is stable and player feedback supports the broader regional arc. Do not include future mission art or “coming soon” features in store marketing until delivery is credible.

## Related documents

[Brief](00_PROJECT_BRIEF.md) · [Decision record](13_TECH_STACK_AND_RENDERING_DECISION.md) · [Backlog](23_TASK_BACKLOG_AND_ACCEPTANCE_CRITERIA.md) · [Risk register](26_RISK_REGISTER_AND_DECISION_LOG.md)
