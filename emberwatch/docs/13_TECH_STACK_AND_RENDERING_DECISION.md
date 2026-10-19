# 13 — Tech Stack and Rendering Decision (ADR-001)

**Purpose:** Honest comparison and recommendation. **Status:** Proposed — `Needs Validation` (experiments below) · **Last reviewed:** 2026-10-18

## 1. Context
Founder prefers React Native + TypeScript + Expo, Android-first. The vision includes 3D-looking characters, environments, animation and action. **React Native is a UI framework, not a game engine**; real-time 3D in RN requires extra rendering libraries and native modules and carries performance risk.

## 2. Verified facts (checked 2026-10-18)
| Claim | Source | Confidence |
|---|---|---|
| Google Play: new apps/updates must target Android 16 (API 36) or higher | https://developer.android.com/google/play/requirements/target-sdk and https://support.google.com/googleplay/android-developer/answer/11926878 | Official |
| Google AI Studio Build mode generates web apps and **native Android apps in Kotlin + Jetpack Compose** (preview emulator, device install via adb, Play internal testing) — not React Native | https://ai.google.dev/gemini-api/docs/aistudio-build-mode | Official |
| Expo SDK 56 (stable May 2026) uses React Native 0.85 / React 19.2 | Third-party summary https://www.issoh.co.jp/tech/details/12044/ ; verify at https://expo.dev/changelog | **Needs Validation** (not official) |
| New Architecture is default since Expo SDK 53 | https://expo.dev/changelog/sdk-53 | Official |
| React Native core 0.87 (11 Aug 2026), per vendor blog | https://ecorpit.com/react-native-app-development-company/ | **Needs Validation** |

## 3. Options
| | A: RN 2.5D (Skia) | B: RN shell + 3D engine | C: Full engine (Godot/Unity) |
|---|---|---|---|
| Visuals | Pre-rendered 3D-look sprites, painted env | Real-time 3D | Real-time 3D |
| Dev effort | Lowest–medium | **Highest** (two runtimes, bridge, build complexity) | Medium–high |
| Perf risk | JS-thread sim; Skia fill-rate | Cross-runtime overhead, memory duplicates | Lowest per-visual |
| Team fit | Web/JS devs | Needs RN + engine + native | Needs engine skills |
| Build/publish | Expo/Gradle AAB, simple | Complex (embed engine view in RN) | Engine export to AAB, standard |
| UI/menu dev | Fast | Fast | Engine UI (less convenient) |
| Preserves RN preference | **Yes** | Partly | No |
| Visual ceiling | Medium–high (stylised) | High | High |
| Main risk | Can't reach desired polish/framerate | Integration cost | Abandons stack preference |

### Option details
- **A:** Characters modeled in Blender, rendered to 8-direction sprites; Skia draws atlases at 30–60 FPS. Sim in TS at 30 Hz. Uses `@shopify/react-native-skia`, `react-native-reanimated`, `react-native-gesture-handler` (`Needs Validation`: versions compatible with chosen Expo SDK).
- **B:** Not recommended for a small team; two runtimes complicate lifecycle, input, memory, debugging and release.
- **C:** Godot 4 (lightweight, open) or Unity; best for true 3D, but menus/progression move into the engine.

## 4. Recommendation (Proposed)
**Adopt Option A for MVP**, gated by experiments. **If any gate fails, switch to Option C (Godot 4 preferred for size/licence simplicity; confirm Android export requirements), not Option B.** This preserves the founder's React Native preference where it makes technical sense without pretending RN is a 3D engine.

Consequences: stylised 2.5D look (not free-camera 3D); art pipeline requires Blender→sprite render; smaller visual ceiling; fastest route to a shippable MVP.

## 5. Technical experiments (Phase 0 gates)
| ID | Experiment | Pass criterion (targets) | Fail action |
|---|---|---|---|
| EXP-01 | Skia scene: 1 player + 20 sprites + 3 parallax layers + 100 particles | ≥ 55 FPS on mid-range reference device; ≥ 30 FPS on low | Re-evaluate to Option C |
| EXP-02 | Touch stick + fire input latency | ≤ 100 ms end-to-end | Move input handling to UI-thread worklets; else Option C |
| EXP-03 | Memory with 3 atlases + backgrounds | ≤ 150 MB resident on Medium | Lower res/atlas strategy |
| EXP-04 | Audio latency & concurrent voices (expo-audio vs alternatives) | ≤ 100 ms; 16 voices | Native module or alternative lib |
| EXP-05 | Atomic save + kill-app recovery | 0 corrupt saves in 200 forced kills | Redesign writer |
| EXP-06 | Godot spike (parallel): same scene | Baseline comparison | Informs fallback |
Experiments must record device, OS, build type, and measured results in [26](26_RISK_REGISTER_AND_DECISION_LOG.md). **No result is assumed until recorded.**

## 6. Dependency compatibility checks
Before Phase 1: confirm Expo SDK ↔ RN ↔ Skia ↔ Reanimated ↔ Gesture Handler matrix via official docs (https://docs.expo.dev, https://shopify.github.io/react-native-skia/, https://docs.swmansion.com/react-native-reanimated/); confirm New Architecture support for every native module; confirm minSdk for Expo SDK in use (proposed 24, `Needs Validation`).

## 7. Google AI Studio workflow (Proposed)
1. Use AI Studio or any LLM only to **draft/refine documentation and code snippets**; it does not generate React Native projects in native Android mode (Kotlin/Compose only). 
2. Keep this repository as the source of truth; paste/sync docs via Git. AI Studio's "Import from GitHub" can read the repo for context.
3. Implement the app in a local RN/Expo environment (VS Code/Android Studio), build via EAS or Gradle, test on device.
4. If Option C is chosen, same repo holds the engine project in a `/game/` folder.

## 8. Decision status
`Proposed` → founder approval recorded in [26](26_RISK_REGISTER_AND_DECISION_LOG.md) DEC-001 after EXP-01…05.
