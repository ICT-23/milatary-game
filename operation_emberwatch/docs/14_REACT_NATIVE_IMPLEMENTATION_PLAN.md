# 14 — React Native Implementation Plan

**Purpose:** Preserve the React Native preference without misrepresenting its role in a 3D game.  
**Status:** Proposed / React Native runtime deferred  
**Last reviewed:** 2026-10-19

## Decision summary

React Native and TypeScript are excellent for many cross-platform application screens, but ordinary React Native UI components do not provide a complete real-time 3D action-game engine. This project's required third-person 3D camera, skeletal animation, collision, environment, AI and mobile graphics pipeline point toward a dedicated engine. The default MVP therefore does **not** put React Native inside the game runtime.

This file exists for two safe options: (1) a separately validated React Native 2.5D pivot if the team intentionally reduces scope, or (2) a later shell/companion integration if a product requirement justifies the complexity. Neither option is currently approved.

## Option 1 — Reduced-scope React Native 2.5D prototype

Possible layers:
- React Native/TypeScript owns navigation, settings, mission board, dialogue, save records and accessibility.
- A specifically selected renderer owns game-world drawing. Before committing, compare maintenance state, React Native New Architecture compatibility, touch handling, asset formats, animation and Android builds for current packages.
- Deterministic gameplay systems (movement, collisions, objectives, health and enemy state) update on a controlled loop separate from ordinary React UI re-rendering.
- Render state is passed with minimal allocations; high-frequency game state must not trigger a full React component tree re-render on every frame.

Required experiment before approval:
1. Render one animated character and a small 3D/2.5D scene.
2. Implement touch movement and camera/aim input.
3. Demonstrate collision, one hostile state and one mission objective.
4. Export/install a release-like Android build and profile CPU/GPU/memory/thermals.
5. Confirm needed native modules support the selected React Native and Expo versions.
6. Prove accessibility controls, pause/background, safe save and low-memory recovery.

If this experiment cannot match the target experience without custom native rendering work larger than a small engine prototype, stop the RN route rather than layering more dependencies on top.

## Option 2 — React Native shell around a dedicated engine

Potential boundary (if later approved):
- RN: external account-free campaign journal, accessibility preferences that don't require real-time access, help/credits, support, optional companion content.
- Engine: all gameplay, menus needed while playing, in-game settings used by mission controls, mission progression and local save authority.
- Bridge: only small typed commands/events (`RequestOpenCompanion`, `RequestPause`, `MissionCompletedSummary`); no per-frame transforms or combat data.
- Save ownership: engine remains authoritative. If preferences need sharing, define a versioned minimal contract and one-way migration; avoid two writers to the same save file.

Hybrid pass gate: test Android process lifecycle, engine resume, RN navigation back stack, keyboard/accessibility focus, status/navigation bars, audio focus, memory peak, crash recovery and package signing on physical hardware. Use a native development build, not just Expo Go, if custom native modules are required. Expo docs describe development builds as the route for packages containing native code; adding native libraries usually requires a new native build.

## Tooling and version control

If RN is authorized later:
- Use TypeScript with strict mode and stable package versions; pin Node/Gradle/JDK/Android SDK to a documented matrix.
- Select a current Expo SDK only after its supported React Native version, Android `compileSdkVersion`, `targetSdkVersion` and native module compatibility have been checked in the official SDK table.
- If custom native integration is needed, use a development build and check native-module compatibility. Do not assume a managed preview app can load arbitrary native code.
- Keep renderer packages isolated behind a `GameRenderer` boundary until the spike selects one.
- Separate UI state from gameplay simulation state; do not use React state setters as a 60-Hz physics loop.
- Write automated unit tests for mission rules, objective transitions and save migration independently of rendering.

## Suggested source structure only if RN path is approved

```text
src/
  app/                 # navigation and app flow
  screens/             # title, mission board, settings, codex
  components/          # accessible UI primitives
  game/
    simulation/        # deterministic state and rules
    renderer/          # isolated renderer adapter
    input/             # touch mapping and control configuration
    missions/          # validated authored configuration
  data/
    content/           # static mission and character data
    save/              # repository and schema migrations
  services/            # audio, settings, diagnostics
  accessibility/       # preference model and application hooks
```

This is a proposed pattern, not a claim that a React Native project has been generated.

## AI-assisted workflow with Google AI Studio

1. Keep the Markdown specifications in Git as the source of truth.
2. Use Google AI Studio web Build mode to prototype React-based non-game interface concepts if useful; export and review the code before reuse.
3. Do not use AI Studio's native Android Build mode expecting React Native output: the official workflow documents Kotlin + Jetpack Compose for Android projects.
4. For the selected Unity stack, implement gameplay in Unity. AI assistance may generate unit-test ideas, data validation scripts, UI copy or documentation patches, but every code change is reviewed and compiled in the target environment.
5. Never paste keys, signing credentials, private player data or proprietary third-party assets into prompts.

## Acceptance criteria before any RN code is approved

- [ ] Engine/runtime decision has passed the Phase 0 comparison.
- [ ] Renderer dependency, license, maintenance state and native compatibility are documented.
- [ ] A physical-device prototype demonstrates movement, camera and objective completion.
- [ ] 10-minute frame-time and thermal report is available.
- [ ] Clean release build installs and resumes safely after process termination.
- [ ] React Native does not duplicate mission truth or write save state independently.

## Official references

- React Native performance: https://reactnative.dev/docs/performance
- React Native architecture: https://reactnative.dev/architecture/landing-page
- Expo SDK version compatibility: https://docs.expo.dev/versions/latest/
- Expo development builds: https://docs.expo.dev/develop/development-builds/introduction/
- Google AI Studio Android output: https://ai.google.dev/gemini-api/docs/aistudio-android
