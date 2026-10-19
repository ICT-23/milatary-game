# 26 — Risk Register and Decision Log

**Purpose:** Track risks, decisions, open questions, and experiment results. **Status:** Proposed · **Last reviewed:** 2026-10-18

## 1. Decision log
| ID | Decision | Status | Rationale | Owner | Date |
|---|---|---|---|---|---|
| DEC-001 | Option A (RN + Skia 2.5D) with fallback to Option C | Proposed | Preserves RN preference, lowest effort; gated by EXP-01…05 | Founder | — |
| DEC-002 | Landscape locked | Proposed | Controls/HUD consistency | Founder | — |
| DEC-003 | No backend/accounts/ads/IAP in MVP | Proposed | Scope, privacy | Founder | — |
| DEC-004 | MVP = 12 entries, 3 endings | Proposed | Complete arc with small team | Founder | — |
| DEC-005 | Local-only saves, checksum + backup | Proposed | Offline, simple | Founder | — |
| DEC-006 | Working title kept pending trademark check | Needs Validation | REL-010 | Founder | — |
| DEC-007 | minSdk 24 | Needs Validation | Depends on Expo SDK template | Eng | — |

## 2. Risk register
Scale: L/M/H.
| ID | Risk | Likelihood | Impact | Mitigation | Trigger |
|---|---|---|---|---|---|
| R-01 | RN/Skia cannot hit frame/latency targets | M | H | EXP-01/02 first; fallback Option C | EXP fail |
| R-02 | Art volume exceeds team capacity | H | H | Reuse templates, sprite pipeline, cut list in [22](22_MVP_ROADMAP_AND_PHASE_GATES.md) | Slip > 20% |
| R-03 | Dependency/version drift (Expo/RN/Skia) | M | M | Pin versions; compatibility matrix; upgrade windows | Build break |
| R-04 | Play policy/target-API changes | M | M | Re-verify at phases; keep deps current | Policy update |
| R-05 | Save corruption | L | H | Atomic writes, backups, EXP-05 | Any report |
| R-06 | Memory crashes on low-end | M | H | Budgets, profiles, soak tests | OOM logs |
| R-07 | Narrative scope creep | M | M | Choice taxonomy, locked endings | New branch requests |
| R-08 | IP similarity to reference | L | H | ORG rules, reviews | Reviewer flag |
| R-09 | Unlicensed assets slip in | M | H | Manifest CI gate | CI fail |
| R-10 | New-account Play testing requirement delays release | M | M | Start closed test early | Play Console notice |
| R-11 | Predictive-back/edge-to-edge issues on API 36 | M | M | Test on Android 16 devices early | TC-002 fails |
| R-12 | Team size smaller than assumed | M | H | Re-baseline backlog | A-01 invalid |

## 3. Open questions
1. Budget and team size (A-01)? 2. Free vs paid launch? 3. Studio name and package ID? 4. Additional languages? 5. Voice acting budget? 6. Who owns the first real-device test lab?

## 4. Experiment results (fill when run; empty means NOT RUN)
| EXP | Date | Device/OS/build | Result | Pass? |
|---|---|---|---|---|
| EXP-01 | — | — | Not run | — |
| EXP-02 | — | — | Not run | — |
| EXP-03 | — | — | Not run | — |
| EXP-04 | — | — | Not run | — |
| EXP-05 | — | — | Not run | — |
| EXP-06 | — | — | Not run | — |
