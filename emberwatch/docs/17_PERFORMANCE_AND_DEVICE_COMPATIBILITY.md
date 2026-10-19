# 17 — Performance and Device Compatibility

**Purpose:** Measurable targets and test plan. **Status:** Proposed — **all numbers are targets or estimates, not verified results** · **Last reviewed:** 2026-10-18
Experiments: [13](13_TECH_STACK_AND_RENDERING_DECISION.md). Tests: [20](20_TESTING_AND_QUALITY_ASSURANCE.md).

## 1. Device segments (assumptions)
| Tier | Example spec | Profile | Target FPS |
|---|---|---|---|
| Low | 3 GB RAM, entry SoC, 2019–2021 | Low | 30 stable |
| Mid (reference) | 4–6 GB RAM, 2021–2024 mid-range | Medium | 30 stable, 60 where possible |
| High | 8 GB+, recent | High | 60 |
Minimum Android version: proposed **Android 7.0 (API 24)** pending check of the chosen Expo SDK template (`Needs Validation`; target SDK is separately **API 36**, see [21](21_GOOGLE_PLAY_RELEASE_PLAN.md)).
Storage: install ≤ 500 MB total with asset packs (estimate); saves < 5 MB.

## 2. Budgets
| Item | Low | Medium | High |
|---|---|---|---|
| Resident RAM in mission | ≤ 110 MB | ≤ 150 MB | ≤ 220 MB |
| Texture/atlas memory | ≤ 40 MB | ≤ 70 MB | ≤ 110 MB |
| Draw calls/frame (Skia batches) | ≤ 60 | ≤ 120 | ≤ 200 |
| Particles on screen | ≤ 60 | ≤ 150 | ≤ 300 |
| Active animated sprites | ≤ 16 | ≤ 28 | ≤ 40 |
| Sim tick cost | ≤ 6 ms | ≤ 6 ms | ≤ 6 ms of 33 ms budget |
| Cold start to title | ≤ 4 s | ≤ 3 s | ≤ 2.5 s |
| Mission load | ≤ 8 s | ≤ 6 s | ≤ 4 s |
| Input-to-visual latency | ≤ 100 ms | ≤ 100 ms | ≤ 80 ms |

## 3. Quality auto-selection
First launch: run 5-second benchmark scene; choose profile; allow manual override. Runtime governor: if frame time > 40 ms for 3 s → reduce particles → reduce parallax → drop to 30 FPS cap → suggest Low profile.

## 4. Thermal, battery, low memory
- Thermal: use Android thermal status API (via native module, `Needs Validation`) to reduce effects.
- Battery: 30 FPS cap option; pause sim when backgrounded; no wake locks outside gameplay (screen-on only during missions).
- Low memory: on trim-memory callback, free caches, lower profile, prompt save.

## 5. Compatibility test matrix (to be filled with real devices)
| Slot | Requirement | Device (TBD) | Result |
|---|---|---|---|
| D1 | Low-tier, 3 GB | — | Not tested |
| D2 | Mid-range, 4 GB (reference) | — | Not tested |
| D3 | Mid-range, notch/cutout | — | Not tested |
| D4 | High-tier, 120 Hz | — | Not tested |
| D5 | Tablet (landscape) | — | Not tested |
| D6 | Android 16 device | — | Not tested |
| D7 | Oldest supported OS | — | Not tested |

## 6. Monitoring and profiling
Dev: Android Studio Profiler, Perfetto, Flipper/React Native DevTools, Skia frame stats. Production: Android vitals in Play Console (crash/ANR) — no third-party SDK in MVP (see [19](19_PRIVACY_SECURITY_AND_COMPLIANCE.md)).

## 7. Prototype vs verified
| Status | Meaning |
|---|---|
| Assumed | Target set before testing |
| Measured | Result recorded with device, build, date |
Only "Measured" may be quoted as fact. Record in [26](26_RISK_REGISTER_AND_DECISION_LOG.md).

## 8. Acceptance
| ID | Criterion |
|---|---|
| PERF-001 | Medium profile holds ≥ 30 FPS (p95 frame ≤ 40 ms) in M-201 on D2 |
| PERF-002 | RAM budget met in the heaviest mission (M-401) |
| PERF-003 | No ANR in 30-min soak on D1 and D2 |
| PERF-004 | Low-memory simulation doesn't crash and preserves progress |
