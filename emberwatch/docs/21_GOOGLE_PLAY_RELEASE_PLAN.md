# 21 — Google Play Release Plan

**Purpose:** Publishing guide. **Status:** Proposed · **Last reviewed:** 2026-10-18 · **Requirements last verified:** 2026-10-18 (only the target-API item was verified against official text this session; all others are **Needs Validation** and must be re-checked in Play Console before submission). This plan does not claim Google Play acceptance.

## 1. Verified requirement
| Item | Statement | Source |
|---|---|---|
| Target API | New apps and updates must target Android 16 (API 36) or higher; existing apps must target at least Android 15 (API 35) to remain visible to new users on newer OS | https://developer.android.com/google/play/requirements/target-sdk ; https://support.google.com/googleplay/android-developer/answer/11926878 |
| Edge-to-edge | Apps targeting API 36 can no longer opt out of edge-to-edge (reported by secondary source) | `Needs Validation` — https://developer.android.com/about/versions/16/behavior-changes-16 |
| Back handling | API 36 apps don't receive legacy onBackPressed on Android 16+; use predictive-back APIs (secondary source) | `Needs Validation` — same page |
| Extension to 1 Nov 2026 | Mentioned by a vendor blog only | `Needs Validation` (do not rely on it) |

## 2. Items to verify before submission (checklist)
| ID | Item | Where to verify |
|---|---|---|
| REL-001 | Android App Bundle (.aab) required for new apps | Play Console help |
| REL-002 | 16 KB memory page-size support for native libs (Android 15+ targets) | https://developer.android.com/guide/practices/page-sizes |
| REL-003 | Testing requirements for new personal developer accounts (closed test with minimum testers/duration) | https://support.google.com/googleplay/android-developer |
| REL-004 | Data Safety form | Play Console |
| REL-005 | Content rating (IARC) | Play Console |
| REL-006 | Privacy policy URL | Play Console |
| REL-007 | Ads/IAP declarations (N/A for MVP) | Play Console |
| REL-008 | App access / target audience declarations | Play Console |
| REL-009 | Store listing assets: icon 512², feature graphic 1024×500, ≥ 2 phone screenshots (landscape allowed) | Play Console |
| REL-010 | Name availability, trademark search for "Emberwatch", package name uniqueness | Trademark databases (e.g., WIPO/USPTO/IP India) + Play Console |

## 3. Application identity
| Item | Proposal |
|---|---|
| Display name | Operation: Emberwatch (working) |
| Package ID | `com.<studio>.emberwatch` (permanent after first upload — choose carefully) |
| Versioning | `versionName` = semver (1.0.0); `versionCode` = monotonic integer from CI |
| Signing | Play App Signing enabled; upload key kept offline/vaulted; **never in repo** |

## 4. Build types
| Type | Use | Notes |
|---|---|---|
| Development | Dev client, debug menu | Never distributed |
| Preview/internal | Release-mode APK/AAB for testers | Internal testing track |
| Production | Signed AAB | Staged rollout |

## 5. Release tracks
Internal testing (team, up to the Play limit) → Closed testing (invited testers; satisfies REL-003 if required) → Open testing (optional) → Production with staged rollout (e.g., 5% → 20% → 50% → 100%, `Proposed`).

## 6. Store listing drafts
- **Short description (≤80 chars):** "Train, lead and decide: a story-driven military academy adventure." 
- **Full description:** outline — premise, features (varied missions, squad bonds, choices, offline), no promises of rating/approval; no misleading references to other games' names.
- **Screenshots (landscape):** hub, briefing, HUD stealth, rescue, dialogue choice, rank promotion.
- **Feature graphic:** 1024×500 key art with title.
- Do not use competitor names in metadata (policy risk).

## 7. Pre-submission checklist
All QG-1…7 ([20](20_TESTING_AND_QUALITY_ASSURANCE.md)) pass; permissions verified; licence manifest complete; privacy policy live; Data Safety matches reality; pre-launch report reviewed; rollback plan ready.

## 8. Post-release and rollback
Monitor Android vitals (crash/ANR), reviews. Halt staged rollout on crash-rate regression. Hotfix: branch from release tag, bump versionCode, internal test, staged rollout. Rollback = halt rollout and ship fixed higher versionCode (Play does not downgrade installs).

## 9. Policy re-verification
Re-read current Google Play Developer Policy Center and target-API page at: start of Phase 5, the day before submission, and after any rejection.
