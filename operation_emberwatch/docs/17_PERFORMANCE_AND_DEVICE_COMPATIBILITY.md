# 17 — Performance and Device Compatibility

**Purpose:** Define a measurable Android support plan without inventing guarantees.  
**Status:** Proposed targets; all performance numbers need real-device validation  
**Last reviewed:** 2026-10-19

## Support strategy

The game targets Android phones with modern 64-bit support and a practical baseline of memory/GPU performance. Do not choose the final minimum Android API from a generic Unity/Godot engine capability table alone: engine version, plugins, Play target-API rules and actual device tests all matter. Reconfirm the engine's current system requirements and Play's current submission target before each release candidate.

### Device test tiers

| Tier | Purpose | Nomination rule | Must-pass behavior |
|---|---|---|---|
| Low supported | Minimum target profile, limited memory/GPU | Physical device at the bottom of the team-approved support matrix, named in test report | Installs, launches, finishes M01, saves/resumes, does not overheat/crash in standard test loop; may use Low quality preset. |
| Mid reference | Main tuning device | Representative Android device commonly available to intended audience | Default preset targets sustained 30 FPS during typical gameplay and stable touch latency. |
| High | Headroom and visual ceiling | Recent capable device with supported graphics APIs | Higher effects/resolution where tested; no broken material/shader or physics differences. |
| OS boundary | API/platform verification | Oldest supported Android version and current target version(s) available to team | Install/update, permissions, back behavior and data migration validated. |
| Form-factor boundary | Notched/cutout landscape screen | Device with left/right cutout or gesture navigation | Safe-area UI and touch controls remain usable. |

The exact physical models and OS build numbers must be recorded in the test matrix before Phase 2. Emulator results do not substitute for physical GPU, thermal, touch, low-memory and audio-focus tests.

## Initial performance objectives

These are **targets** to measure, not actual test results.

| Metric | Initial target | Measurement method |
|---|---|---|
| Gameplay frame rate | 30 FPS sustained on mid reference; minimize drops below 27 FPS in representative 10-minute capture | Engine profiler plus on-device trace; report median and 1% low where measurement is reliable. |
| Higher-quality profile | Explore stable 60 FPS only on high tier; not a universal requirement | Compare frame time, thermal throttling and power. |
| Frame-time budget at 30 FPS | ~33.3 ms per frame | Separate CPU, GPU, render-thread and wait time. |
| Warm menu-to-playable time | Target under 12 sec on mid reference after first-run setup | Repeat at least 10 runs; report median and worst run. |
| Mission memory | Determine during vertical slice; hold 20% safety margin under observed low-device memory pressure | Native/engine profiler and OS memory diagnostics. |
| App size | Minimize; establish a first vertical-slice baseline before adding cinematic assets | Compare APK/AAB and install size from actual Play-generated delivery. |
| Thermals | No sustained degradation that makes controls unreliable during a 10-minute loop | Repeat same mission with screen brightness and room conditions recorded. |
| Recovery | Return to a safe checkpoint after process kill without save corruption | Scripted and manual test. |

## Budgets to track

At scene/mission level record: triangles, visible skinned meshes, draw calls/material batches, texture residency, audio memory, VFX particle count, active lights, shadow casters, shader variants, CPU time by system, GPU frame time, allocations/GC spikes (where applicable), battery/thermal state, time-to-interactive and memory after mission exit.

Use quality profiles to lower expensive visual extras first. The low profile must preserve objective contrast, player/enemy silhouette, subtitles, hit feedback and interaction prompts. Do not alter gameplay speed, enemy position or mission timers by quality preset.

## Optimization order

1. Measure a representative scene and get a baseline before optimizing.
2. Establish whether the bottleneck is CPU, GPU, memory, storage, shader compilation, GC/allocations, or thermal throttling.
3. Fix the highest-cost measured issue. Avoid speculative changes across multiple systems at once.
4. Rebuild and run identical tests on the same device conditions.
5. Compare performance data and visual/UX regression; keep a before/after capture.
6. Repeat after every major asset addition and before release.

For Unity, use the Unity Profiler and Android-native tools appropriate to the installed editor version. For Android generally, official Android game guidance recommends measurement, scene-level hotspot identification, CPU/GPU-bound checks and verification after changes. Tool choice should follow the current official docs and device availability.

## Loading, thermal and low-memory behavior

- Load only the next required scene and common UI dependencies; avoid preloading every campaign level.
- Use a loading transition before heavy asset groups and make it safe to return after load failure.
- Stop optional effects and preload jobs on low-memory signals; do not purge required gameplay textures mid-mission.
- Avoid permanent full-screen rain particles or many transparent overlapping effects.
- Avoid backgrounding continuous mission simulation. Pause timers and AI when app loses focus.
- On long play sessions, track frame-time change over 10/20 minutes; thermal throttling can make an initially smooth run misleading.
- Do not hold repeated references to exited mission scenes or duplicated materials.

## Compatibility checklist

- supported minimum and current Android versions;
- 64-bit build/runtime requirements for chosen engine/plugins;
- ABI and graphics API availability;
- landscape rotation, cutouts, system gesture insets;
- pause/resume and notification shade;
- Bluetooth/headset and audio-focus changes if applicable;
- low storage during save;
- low memory and process death;
- update from previous save schema;
- app uninstall/reinstall behavior clearly described;
- airplane mode and no network;
- OS font scale/accessibility setting interaction with game UI;
- controller optional behavior only if the team chooses to support it.

## Release gate

No claim such as “runs at 60 FPS on all phones” is allowed without strong evidence and a defined matrix. Release candidate must include performance capture for M01, M04 and M05, a 10-minute thermal run on low/mid representatives, memory checks around scene transition and test results for crash/ANR behavior. Failed tests create tracked bugs with device model, OS/build, scene, reproduction steps, logs and severity.

## Official references

- Android game performance optimization: https://developer.android.com/games/optimize/gameperformance
- Android system tracing: https://developer.android.com/games/optimize
- React Native performance (if RN route is later approved): https://reactnative.dev/docs/performance
- Expo SDK Android compatibility table: https://docs.expo.dev/versions/latest/
- Google Play target API requirements: https://support.google.com/googleplay/android-developer/answer/11926878

Last checked: 2026-10-19. Current engine-specific requirements and platform policy must be rechecked before each release.
