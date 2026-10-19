# 26 — Risk Register and Decision Log

**Purpose:** Track uncertainty and make design/technical decisions explicit.  
**Status:** Initial register; decisions are proposed unless stated otherwise  
**Last reviewed:** 2026-10-19

## Risk scale

Likelihood and impact use Low / Medium / High. Priority is based on the combination and timing of risk, not a mathematical claim. Owner is TBD until team assignments are known.

## Risk register

| ID | Risk | Likelihood | Impact | Mitigation / experiment | Trigger / owner |
|---|---|---|---|---|---|
| R-01 | Chosen engine or render settings cannot sustain intended 3D style on low/mid Android | Medium | High | Phase 0 graybox, scene/frame-time profiling, define low preset; decide before detailed art. | Repeated frame-time miss; Tech Lead TBD. |
| R-02 | React Native preference expands into a complex two-runtime architecture | Medium | High | Engine end-to-end recommendation; RN hybrid only after a dedicated integration spike. | Any proposal to embed RN in gameplay; Architecture Owner TBD. |
| R-03 | AI Studio output misunderstood as directly generating the chosen engine project | Medium | Medium | Document workflow boundary; use AI Studio only for support/prototypes, Unity/Godot toolchain for runtime. | Project kickoff/review; Tech Lead TBD. |
| R-04 | Story scope expands beyond five-mission MVP | High | High | Freeze MVP, require change request and cross-doc updates; validate M01 first. | New character/mission/system proposal; Producer TBD. |
| R-05 | High-fidelity assets are created before scale/camera/performance validation | Medium | High | Graybox and one hero-character proof before asset multiplication. | Art production starts before slice; Art Lead TBD. |
| R-06 | NPC navigation creates softlocks in escort/companion missions | Medium | High | Simple authored routes, anchors, watchdog, recovery tests. | Any repeated path failure; Gameplay Lead TBD. |
| R-07 | Save corruption or duplicated XP on interruption/replay | Medium | High | Atomic snapshot, backup, version migration, idempotent reward keys and forced-stop tests. | Save test failure; Systems Owner TBD. |
| R-08 | Performance degrades during long session due to heat/memory | Medium | High | 10-minute/thermal and repeated-scene test; monitor live assets. | Thermal drop or memory growth; Performance Owner TBD. |
| R-09 | Play target API or testing policy changes before launch | Medium | High | Re-check official pages immediately before submission; keep policy checklist dated. | Pre-release review; Release Owner TBD. |
| R-10 | New personal Play account requires longer closed-testing cycle | Medium | Medium | Check actual account type and Console dashboard early; recruit genuine testers. | Play Console shows requirement; Release Owner TBD. |
| R-11 | Working title is unavailable or conflicts with a mark | Medium | High | Trademark/store/domain search and legal review before public marketing. | Branding lock; Project Owner TBD. |
| R-12 | Asset/music/model rights are unclear | Medium | High | Manifest license evidence; block unknown assets from shipping. | Asset review; Producer/Legal TBD. |
| R-13 | Game rating differs from audience assumption | Medium | Medium | Avoid gore, accurately complete IARC questionnaire and adapt listing. | Content lock; Release Owner TBD. |
| R-14 | AI-generated material contains text artifacts, inconsistent character design or rights concern | Medium | Medium | Human art review, provenance, consistency passes and IP checklist. | Candidate asset submit; Art Lead TBD. |
| R-15 | Story choices appear consequential but have no visible result | Medium | Medium | Consequence matrix, choice branch tests, visible immediate reaction. | Narrative review; Narrative Lead TBD. |
| R-16 | Scope of voiced cinematics exceeds budget | High | Medium | Text-first fallback, limited voiced anchors, short real-time cutscenes. | VO estimate exceeds budget; Producer TBD. |
| R-17 | Privacy declaration differs from third-party SDK behavior | Low in MVP / increases if SDK added | High | No analytics/ads initially; inspect final binary manifest/dependencies; align Data safety. | New SDK or network call; Privacy Owner TBD. |
| R-18 | Reference imitation creeps into art/UI/mission layouts | Low–Medium | High | Originality reviews, licensed source refs, no traced screenshots/assets. | Milestone art review; Creative Director TBD. |

## Decision log

| ID | Decision | Status | Rationale | Revisit condition |
|---|---|---|---|---|
| D-001 | Working title is OPERATION: EMBERWATCH | Proposed | Internal naming only; legal clearance pending. | Title search or publisher preference. |
| D-002 | Setting is fictional Veyra and crisis is The Long Rain / Emberwatch relay outage | Proposed | Creates a character-led stakes-based mystery distinct from reference. | Only through approved story review. |
| D-003 | MVP is offline, single-player, landscape, third-person 3D | Proposed | Matches core desired experience and avoids unnecessary services. | Technical spike suggests untenable cost; consider explicit 2.5D pivot. |
| D-004 | Five missions comprise a complete MVP campaign; M01 is first vertical slice | Proposed | Protects scope while ensuring complete narrative resolution. | User tests or budget change. |
| D-005 | Dedicated engine end-to-end is recommended; Unity is initial candidate, Godot an alternative | Proposed / Needs Validation | Lowest boundary complexity for 3D camera/animation/rendering. | Phase 0 device and team-skill tests. |
| D-006 | React Native shell/hybrid is deferred | Deferred | Two-runtime overhead is not justified for one offline action game. | Specific product requirement plus passing integration spike. |
| D-007 | Google AI Studio is support tooling, not chosen 3D runtime generator | Proposed | Official Android mode outputs Kotlin/Compose; web mode defaults React. | Tool capability materially changes; verify official docs. |
| D-008 | No ads, IAP, accounts, analytics SDK or backend in MVP | Proposed | Focus on quality and minimize privacy burden. | Post-MVP product evidence and policy review. |
| D-009 | Three choices have flavor/local/campaign-tier consequences, but all lead to complete ending | Proposed | Meaningful agency without unbounded branch scope. | Narrative and QA review. |
| D-010 | User-facing claims require evidence; performance values are targets | Approved working rule | Prevents unsupported guarantees. | Never relaxed. |

## Change proposal template

`Change ID | Current decision/requirement | Proposed change | User value | Scope impact | Dependencies | Performance/privacy/IP impact | Docs/tests to update | Prototype evidence | Decision owner | Status`

Any change to engine, mission count, campaign flags, saved IDs, data collection, permissions, monetization or publishing identity must be recorded here before downstream implementation is updated.

## Related documents

[Brief](00_PROJECT_BRIEF.md) · [Technology choice](13_TECH_STACK_AND_RENDERING_DECISION.md) · [Roadmap](22_MVP_ROADMAP_AND_PHASE_GATES.md) · [Backlog](23_TASK_BACKLOG_AND_ACCEPTANCE_CRITERIA.md)
