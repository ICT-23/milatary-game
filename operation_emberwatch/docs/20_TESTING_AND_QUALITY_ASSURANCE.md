# 20 — Testing and Quality Assurance

**Purpose:** Define repeatable tests and release quality gates.  
**Status:** Proposed; no tests have been executed by this documentation task  
**Last reviewed:** 2026-10-19

## Testing strategy

Testing is layered. Unit-test pure gameplay/data rules; integration-test objective/mission transitions; playtest complete missions for feel and narrative clarity; test builds on physical devices for performance, lifecycle, touch and graphics; run a policy/license/asset audit before release. Test reports must record actual environment and actual outcome. A case in this document is a planned test, not evidence that it passed.

## Severity scale

- **S0 Blocker:** app cannot launch, campaign save lost/corrupted, core mission cannot finish, data/security/privacy failure, release package invalid.
- **S1 Critical:** major mission softlock, crash/ANR in common path, controls unusable, objective/reward flags corrupt.
- **S2 Major:** feature behaves incorrectly with workaround, significant readability/performance/accessibility issue.
- **S3 Minor:** cosmetic/wording/rare edge case with little impact.

## Test case register

| ID | Area / prerequisites | Actions | Expected result | Severity if failed |
|---|---|---|---|---|
| QA-BOOT-001 | Clean install, supported device | Launch first time with network off | App opens onboarding/title; no login/network gate. | S0 |
| QA-NAV-001 | Main menu | Open settings, return, open mission board, press system back | Back behavior follows documented screen stack; no blank screen. | S2 |
| QA-IN-001 | M01 movement tutorial | Move, release stick, sprint, change camera | Character stops after input release and camera stays controllable. | S1 |
| QA-IN-002 | Any active mission | Rotate camera 180°, vary sensitivity and handedness, restart app | Look controls are consistent and preferences persist. | S2 |
| QA-UI-001 | Landscape device with cutout | Open HUD/menu and operate buttons on both sides | Controls remain within safe areas and are not blocked by system gestures. | S1 |
| QA-GAME-001 | M01 | Skip optional training markers and finish main objective | Main objective remains completable; bonus score only differs. | S1 |
| QA-GAME-002 | M02 | Move outside companion follow range and return to route | Companion recovers safely and does not block mission trigger. | S1 |
| QA-GAME-003 | Interactable available | Spam interaction tap/hold, interrupt with pause/damage | Interactable commits once, cancels safely and updates objective once. | S1 |
| QA-GAME-004 | Combat encounter | Fire while paused, at civilians, and after mission completion | Gameplay input is ignored when paused/complete; civilians are never valid targets. | S0/S1 |
| QA-GAME-005 | M03 stealth | Trigger suspicion, break line of sight, retry checkpoint | Alert cues are understandable and mission recovers without softlock. | S2 |
| QA-GAME-006 | M04 escort | Leave group behind, regroup, activate checkpoint | NPCs follow safe route or reset at checkpoint; no escort deadlock. | S1 |
| QA-CHOICE-001 | M01 choice | Select each branch in separate clean saves; complete mission and relaunch | `CHOICE_01_SHELTER` persists and only documented later line changes. | S1 |
| QA-CHOICE-002 | M05 choice | Run both handover choices; view ending and codex | Both branches reach complete ending, with expected epilogue differences. | S1 |
| QA-SAVE-001 | Any checkpoint | Force-stop app after checkpoint and relaunch | Continue loads latest safe checkpoint without corrupting data. | S0 |
| QA-SAVE-002 | Existing valid save | Simulate corrupted active file with valid backup | Backup recovery succeeds or a clear recoverable path appears; no silent overwrite. | S0 |
| QA-SAVE-003 | Old fixture schema | Upgrade build and load fixture | Migration succeeds; story flags, ranks and settings remain coherent. | S0 |
| QA-SAVE-004 | Mission reward | Restart/replay mission and repeat objective event | XP/reward ledger grants first-completion reward once; best medal updates correctly. | S1 |
| QA-LIFE-001 | Active mission | Pull notification shade, lock/unlock, receive simulated interruption | Mission pauses safely and resumes with correct input/audio state. | S1 |
| QA-AUD-001 | Gameplay + settings | Adjust each audio slider, mute, interrupt audio focus | Sliders are independent, mute persists, sound does not double after resume. | S2 |
| QA-ACC-001 | Accessibility settings | Increase text size, enable captions/reduced motion and finish M01 | Objectives/dialogue are readable; no button covers a required prompt. | S1 |
| QA-OFF-001 | Installed build | Enable airplane mode and complete five-mission campaign | No mission, save or ending depends on network. | S0 |
| QA-PERF-001 | Low-tier test device | Capture M01/M04/M05, 10-minute gameplay loop | Frame-time/thermal metrics recorded against target; no S0/S1 issue. | S1 |
| QA-PERF-002 | After mission exit | Repeat mission → menu 5 times | Memory returns within established tolerance; no accumulating assets or severe degradation. | S2 |
| QA-PERF-003 | Low quality preset | Play night/rain scene | Critical objective/enemy/interaction cues remain visible. | S1 |
| QA-COMP-001 | Supported Android version matrix | Install and update the release candidate | Installs and launches on all declared supported versions; no unexpected permission prompt. | S0 |
| QA-COMP-002 | Device with constrained storage | Trigger low-space save/write condition where safe | Last good save is not corrupted; clear error/recovery behavior. | S0 |
| QA-REL-001 | Clean checkout | Produce signed release artifact from build instructions | Reproducible package and version metadata; credentials not committed. | S0 |
| QA-POL-001 | Release candidate + SDK list | Inspect manifest, permissions, third-party SDKs, privacy page and Data safety | All declarations match actual binary and external dependencies. | S0 |
| QA-IP-001 | All shipping assets | Audit manifest and license evidence | Every shipped font/model/audio/texture has approved rights record. | S0 |

## Automation candidates

Automate pure functions: rank thresholds, XP totals, reward idempotency, mission objective graph validation, save migration, schema validation, localization key resolution and branch-flag combinations. Unity/Godot engine integration tests can validate objective triggers and state transitions; device tests remain necessary for touch, frame-time, thermal, gestures, audio and OS lifecycle.

## Test data and save fixtures

Maintain clean-save fixture, one save at each MVP mission checkpoint, completed campaign fixture for each choice branch, older schema fixtures, corrupt active file with good backup, and corrupt both files. Never use a real player's personal data as a test fixture. Test fixture IDs and build version are recorded in output.

## Quality gates

### Gate A — playable prototype
- Movement/camera/tutorial objective works on physical Android device.
- No launch blocker or persistent input capture bug.
- One mission can be completed and failed/retried.

### Gate B — vertical slice
- Full M01 flow with checkpoint, optional objective, dialogue choice and debrief works.
- Save/resume passes QA-SAVE-001 and QA-SAVE-004.
- Objective remains legible on low settings and safe-area layouts.
- Frame-time/thermal report recorded; critical defects resolved.

### Gate C — MVP campaign content lock
- M01–M05 each pass mission completion, restart, failure and offline tests.
- Every branch flag has a test; no ending branch is incomplete.
- All text/VO keys exist; asset list is licensed.

### Gate D — release candidate
- No open S0/S1 bugs.
- Supported-device matrix complete and known limitations documented.
- Privacy, target API, app bundle, content rating, Play testing and listing assets reviewed.
- Clean signed build reproducible; signing secrets protected.
- Actual test results—not just planned cases—attached to release record.

## Bug report template

`BUG-ID | severity | build | device + OS | mission/scene/checkpoint | prerequisites | exact steps | expected | actual | reproduction rate | logs/video | owner | fix commit | retest result`.

## Related documents

[Mechanics](06_GAMEPLAY_MECHANICS.md) · [Save model](15_DATA_MODEL_AND_SAVE_SYSTEM.md) · [Performance](17_PERFORMANCE_AND_DEVICE_COMPATIBILITY.md) · [Play release](21_GOOGLE_PLAY_RELEASE_PLAN.md)
