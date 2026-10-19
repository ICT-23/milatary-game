# 14 — React Native Implementation Plan

**Purpose:** How to implement [12](12_TECHNICAL_ARCHITECTURE.md) once Phase 0 passes. **Status:** Proposed (conditional on ADR-001, [13](13_TECH_STACK_AND_RENDERING_DECISION.md)) · **Last reviewed:** 2026-10-18
If Option C is chosen, this file is superseded by an engine plan.

## 1. Proposed stack (versions `Needs Validation` — pin after official compatibility check)
| Concern | Choice |
|---|---|
| Framework | Expo (current stable SDK at start), React Native New Architecture, TypeScript strict |
| Rendering | @shopify/react-native-skia |
| Animation/gestures | react-native-reanimated, react-native-gesture-handler |
| Navigation | React Navigation or Expo Router |
| State | Zustand (UI), plain TS (sim) |
| Audio | expo-audio (verify EXP-04) |
| Storage | expo-file-system (atomic write strategy) |
| Orientation | expo-screen-orientation (landscape lock) |
| Haptics | expo-haptics |
| Testing | Jest, React Native Testing Library, Detox or Maestro (device UI tests) |
| Lint/format | ESLint, Prettier, TypeScript strict |

## 2. Folder structure
```
/app                # entry, navigation
/src
  /game-core        # pure TS simulation
  /render           # Skia renderers
  /ui               # screens, HUD, components, theme tokens
  /content          # missions, characters, items JSON + schemas
  /services         # save, audio, assets, settings, logger
  /i18n
/assets             # see 16
/tools              # content validators, sprite pack scripts
/docs
```

## 3. Implementation sequence
| Step | Output | Gate |
|---|---|---|
| 1 | Project scaffold, landscape lock, CI | Builds on device |
| 2 | Fixed-step loop + Skia test scene | EXP-01/02 pass |
| 3 | Movement, camera, collision | MECH-001 AC |
| 4 | Interact/carry, objectives | P-01 playable |
| 5 | Aim/fire, enemies, cover | M-102 playable |
| 6 | Stealth | M-103 playable |
| 7 | Save service | EXP-05 pass |
| 8 | Dialogue/choices | Flags persisted |
| 9 | Progression/skills/loadout | PROG ACs |
| 10 | Squad orders, beacon repair | M-104, M-202 |
| 11 | Content for remaining missions | Campaign complete |
| 12 | Polish, audio, cinematics | QA gates |

## 4. Coding rules
- `game-core` imports nothing from `react`, `react-native`, or Expo (lint rule).
- Sim uses fixed dt = 1/30 s and seeded RNG.
- No per-frame allocations in hot loops; reuse arrays; pool particles.
- HUD reads throttled selectors; avoid re-render per tick.
- Every module has unit tests; every mission has a headless "golden path" replay test.
- No secrets, no network calls (MVP).

## 5. Key risks
JS-thread contention (mitigate: worklets for input/camera; keep sim light), memory (atlas budgets), Skia version drift, predictive-back behaviour on API 36.

## 6. Definition of done (per task)
Code merged, unit tests pass, acceptance criteria satisfied, perf budget respected (measured on reference device), docs updated.
