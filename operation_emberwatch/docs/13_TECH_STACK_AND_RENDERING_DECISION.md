# 13 — Technology Stack and Rendering Decision

**Purpose:** Record the engine/runtime choice and its trade-offs.  
**Status:** Proposed decision; Phase 0 spike is mandatory  
**Last reviewed:** 2026-10-19

## Product requirements driving the choice

The game needs a real-time third-person camera, skinned 3D characters, animation transitions, collision, character navigation, interaction triggers, lighting, VFX, audio mixing, pause/background lifecycle, offline saves and reproducible Android release builds. The user prefers React Native/TypeScript and wants to use Google AI Studio where practical. Those preferences are valuable but do not remove the need for a suitable 3D runtime.

## Decision comparison

| Option | Description | Benefits | Costs/risks | Suitability |
|---|---|---|---|---|
| **A — React Native 2D/2.5D** | React Native/TypeScript app with a restricted 2.5D presentation and simple action scenes. Rendering candidate (e.g. Skia/GL-based library) must be selected and benchmarked; RN components alone are not a full 3D engine. | Matches JS/TS skill preference; UI iteration is familiar; accessible menus. | Requires custom or third-party scene graph, camera, asset pipeline, collision, skeletal animation, AI/navigation, profiling and native integration. Package compatibility and long-term maintenance risk; premium 3D target must be reduced. | Viable only if the art ambition is deliberately lowered and the renderer passes a benchmark. |
| **B — React Native shell + 3D engine** | RN owns menus/campaign/profile/settings; a dedicated engine renders gameplay, connected via native Android boundary or engine-as-library integration. | Preserves RN for content-heavy interface; native 3D where needed. | Two runtimes, build/lifecycle complexity, input handoff, back handling, save ownership, memory overhead, deep links and crash debugging across boundary. Expo Go alone is insufficient for arbitrary native integration; custom native builds required. | Technically possible, but not the smallest path for one compact offline game. Defer unless RN reuse has measurable benefit. |
| **C — Dedicated engine end-to-end** | Unity or Godot owns menus and gameplay; one Android application/package. | Mature scene/animation/input/audio workflow, unified lifecycle, profiler, asset pipeline and build target. | Different coding model from React Native; engine learning/licensing decision; UI must be authored in engine. | **Recommended** for the stated 3D action ambition. |

## Recommended stack

**Working recommendation:** Unity + C# for the whole game, Unity UI for menus/HUD, version-controlled source assets, engine-local persistence with a carefully designed save repository, Android builds produced through the engine's supported Android pipeline and Play Console for distribution. Use a render pipeline appropriate for mobile (prototype Universal Render Pipeline settings if Unity is chosen); do not lock shader/post-processing choices until physical-device tests.

**Alternative:** Godot if the team is more productive in it or licensing/ownership requirements make it preferable. Its official Android export documentation currently describes OpenJDK 17 and the Android SDK setup; check the engine version's current stable guide, export templates and plugin status before selecting. Do not assume C# Android support is risk-free; verify current limits if using C#.

**React Native role:** retain as a later option for a companion app, external account-less community tool or a completely separate campaign guide only if product research justifies it. Do not duplicate game screens in RN and Unity in the first release. A hybrid experiment must prove startup, memory, pause/resume, back navigation, input focus and release packaging before being approved.

## Phase 0 technical experiment

Create a throwaway graybox—not production content—with: landscape boot/menu; one controllable character; follow camera; four interactable objects; one hostile AI; one companion follow path; one objective/checkpoint; save/resume; basic rain and light; Android install. Measure build time, first load, frame-time, memory, device heat and integration friction on nominated low/mid-tier Android hardware.

Pass criteria:
- movement and camera are controllable using touch;
- no persistent 30 FPS target violation in a 10-minute mid-tier run (exceptions recorded by scene and fixed or justified);
- save/checkpoint resumes consistently after forced process termination;
- Android release package can be built from a clean checkout;
- no critical native plugin conflict and no dependency without an owner;
- editor, build, source control and licensing constraints fit the actual team.

If Unity fails, prototype Godot with the same content and metrics before reconsidering a React Native 2.5D pivot. If the project pivots to RN 2.5D, update the art bible, GDD, mission assets and store promises before continuing.

## Google AI Studio compatibility and intended use

Official Google AI Studio documentation describes two different Build workflows:
- **Web app workflow:** React is the default client framework and a Node.js server-side runtime is available for suitable features.
- **Native Android workflow:** generates a Gradle project based on Kotlin and Jetpack Compose; current documented limitations include a single-activity/single-module model, no C/C++ NDK code, and client-side-only Android app output. Android project export is documented as ZIP download; production release must be managed in Play Console even where AI Studio can publish to internal testing.

Therefore, AI Studio should be used as a design/brainstorming and prototyping aid, not claimed as a one-click Unity project generator or React Native native-game builder. Prompt an AI coding agent to create or review focused modules only after documentation is stable. Export generated web/Android code to version control and review every dependency, permission and generated claim. For the chosen Unity stack, implement in the Unity editor/IDE and Android toolchain; AI Studio may help generate prose, UI concepts, test ideas or non-runtime React prototypes.

## Official verification sources

- Google AI Studio Android workflow and limitations: https://ai.google.dev/gemini-api/docs/aistudio-android
- Google AI Studio build mode (web vs Android): https://ai.google.dev/gemini-api/docs/aistudio-build-mode
- React Native performance: https://reactnative.dev/docs/performance
- React Native New Architecture: https://reactnative.dev/architecture/landing-page
- Expo SDK versions and Android API mapping: https://docs.expo.dev/versions/latest/
- Expo development builds and native module workflow: https://docs.expo.dev/develop/development-builds/introduction/
- Godot Android export: https://docs.godotengine.org/en/stable/tutorials/export/exporting_for_android.html
- Android game performance: https://developer.android.com/games/optimize/gameperformance
- Google Play target API requirements: https://support.google.com/googleplay/android-developer/answer/11926878

**Verification note:** URLs reviewed 2026-10-19. Re-check versions and policy dates at implementation and release. Version-number recommendations are intentionally not frozen in this decision until Phase 0.

## Decision status

- Engine end-to-end: **Proposed recommendation**.
- Unity vs Godot: **Needs Validation** during Phase 0.
- React Native shell: **Deferred**.
- Google AI Studio as production game engine: **Rejected** for this 3D runtime; use as support tooling only.
