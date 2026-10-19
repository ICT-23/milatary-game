# 20 — Testing and Quality Assurance

**Purpose:** Test strategy, concrete cases, quality gates. **Status:** Proposed · **Last reviewed:** 2026-10-18
**Nothing here has been executed.** Results go in a separate `qa/results/` log once testing starts; no feature may be called "tested" without a recorded result.

## 1. Strategy
| Level | Tool (proposed) | Scope |
|---|---|---|
| Unit | Jest | game-core rules, save, migrations, schema validators |
| Replay/gameplay | Headless sim + recorded inputs | Mission golden paths, determinism |
| Component/UI | React Native Testing Library | Screens, states, labels |
| E2E device | Maestro or Detox | Navigation, touch flows |
| Performance | Profiler + scripted scenes | FPS, memory |
| Manual | Test plan below | Feel, audio, accessibility |

## 2. Test cases (representative; extend per backlog)
Severity: S1 blocker, S2 major, S3 minor.
| ID | Area | Prereq | Action | Expected | Sev |
|---|---|---|---|---|---|
| TC-001 | Nav | Fresh install | Launch → New Campaign → begin | P-01 loads in landscape | S1 |
| TC-002 | Back | In mission | Press back/gesture | Pause opens (no exit) | S2 |
| TC-003 | Movement | M-101 | Push stick full | Sprint; stamina drains; latency ≤ 100 ms | S1 |
| TC-004 | Combat | M-102 | Fire at target | Hit feedback ≤ 80 ms; ammo decrements | S1 |
| TC-005 | Stealth | M-103 | Enter cone 1.2 s | Alert; icon "!" shape | S2 |
| TC-006 | Rescue | P-01 | Carry civilian to culvert | Counts as saved; can't fire | S1 |
| TC-007 | Orders | M-104 | Issue Hold | Squad stops ≤ 0.5 s | S2 |
| TC-008 | Beacon | M-202 | Complete 3 steps | Node lit; objective ticks | S1 |
| TC-009 | Choice | M-202 | Copy log | Flag `F_TOWER7_LOG_COPIED=true` saved | S1 |
| TC-010 | Ending gate | Test save | Missing log flag | Open Beacon greyed with reason | S2 |
| TC-011 | Save/resume | Mid-mission | Home → kill → relaunch → Continue | At last checkpoint, state identical | S1 |
| TC-012 | Corrupt save | Corrupt json | Launch | Recovery dialog, .bak loads | S1 |
| TC-013 | Migration | v1 fixture | Load in v2 build | Migrated, backup kept | S1 |
| TC-014 | Interrupt | Mission | Incoming call | Auto-pause; resume works | S2 |
| TC-015 | Offline | Airplane mode | Full session | No errors, no network calls | S1 |
| TC-016 | Low memory | Mission | Simulate trim | Profile drops; no crash | S1 |
| TC-017 | Layout | Cutout device | Open HUD | No clipped controls | S2 |
| TC-018 | Touch | Any | 3-finger touch | Extra pointer ignored safely | S3 |
| TC-019 | Audio | Mission | Mute/volume sliders | Independent, persist | S2 |
| TC-020 | A11y | Settings | Enable colour-blind mode | Markers change shape+colour | S2 |
| TC-021 | Perf | M-201 D2 | 10-min run | p95 frame ≤ 40 ms | S1 |
| TC-022 | Release | Signed AAB | Install via internal track | Launches; no debug menu | S1 |
| TC-023 | Progress | Main path no optionals | Reach M-401 end | Rank R6 | S2 |
| TC-024 | Perm | Final APK | Dump permissions | Matches approved list | S1 |

## 3. Regression
Automated golden-path replay for all 12 missions on every merge to main; nightly device smoke test on at least one physical device.

## 4. Accessibility test checklist
Screen reader labels present; contrast ≥ 4.5:1; subtitle sizes; no colour-only cues; haptics off works; reduced motion; touch target ≥ 48 dp.

## 5. Quality gates (MVP complete only when all pass)
| Gate | Criterion |
|---|---|
| QG-1 | All S1 cases pass on D1, D2, D6 |
| QG-2 | 0 open S1, ≤ 5 open S2 |
| QG-3 | Crash-free sessions ≥ 99.5% in closed test (Play vitals) |
| QG-4 | PERF-001…004 pass |
| QG-5 | All 3 endings reachable via real play |
| QG-6 | SAVE-001…005 pass |
| QG-7 | PRIV-001…004 pass |

## 6. Defect process
Severity, steps, device, build, logs; triage twice weekly; hotfix rules in [21](21_GOOGLE_PLAY_RELEASE_PLAN.md).
