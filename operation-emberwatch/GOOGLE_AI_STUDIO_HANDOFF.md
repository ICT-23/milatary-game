# Google AI Studio workflow note

The supplied game requirement is **React Native + TypeScript**. The attached source project follows that requirement.

Google's current documentation describes two Build-mode project targets: React web apps and native Android apps built with Kotlin/Jetpack Compose. It does not list React Native as a direct native project target. Sources: [Google AI Studio Build mode](https://ai.google.dev/gemini-api/docs/aistudio-build-mode) and [Build Android apps in Google AI Studio](https://ai.google.dev/gemini-api/docs/aistudio-android).

## Keep React Native (recommended if the stack requirement is firm)

1. Extract this project ZIP.
2. Open the folder in VS Code or another React Native-capable editor.
3. Install Node.js 22.13+ and run `npm install`.
4. Run `npx expo install --check`, then `npx expo start`.
5. Preview on a compatible Android device/emulator; use a development build for native dependencies if Expo Go does not match the SDK.
6. Produce an Android APK for device testing or a production AAB with EAS Build. Steps are in `README.md`.

## If building directly inside Google AI Studio is more important than React Native

Use Build mode's **Android** platform picker and ask the agent to port the game into a separate native Kotlin/Jetpack Compose project. That is a different implementation and must not be represented as the React Native project in this bundle. Verify that it preserves the mission state machine, screen flow, map movement, local saves and both narrative-choice outcomes. AI Studio's Android mode supports browser-based emulator preview and device installation according to Google's documentation.

Do not paste the React Native files into a web-app prompt and call the result a native React Native game. The browser web preview is not a native Android build.

## Copy/paste prompt for an intentional native Kotlin port

Use this only after choosing **Android** in AI Studio. It creates a different Kotlin implementation; it does not preserve the React Native stack requirement.

```text
Build a fully interactive, native Android game prototype named OPERATION: EMBERWATCH using Kotlin and Jetpack Compose. This is a deliberate Kotlin port, not React Native. Do not build a web app.

Create an original offline single-player story-driven military adventure set in the fictional coastal nation of Asterra. Use a polished top-down 2.5D visual presentation, a custom illustrated coastal field map, layered terrain and buildings, olive/blue-green environments, restrained signal-lime accents (#C5E56A), dark navy-green UI (#0A1215), muted teal (#7ABDB4), and warm amber (#E79A59). Avoid copied characters, maps, assets, story, logos or layouts from existing games.

Implement these real screens: title, recruit profile, mission briefing, playable mission, civilian choice, pause/settings, after-action report and campaign dossier. The first mission, Signal in the Dust, must be playable end-to-end. Use a touch virtual joystick at bottom-left, interact/scan controls at bottom-right, an always-visible objective HUD, a pause button, and clear out-of-range feedback. Use a Canvas/custom drawing layer or another reliable native rendering approach for the map; never use a non-interactive screenshot as the game world.

Mission sequence: (1) reach Captain Mara Voss and hear orders; (2) reach the field relay and help engineer Leena Suri restore it; (3) inspect and collect the suspicious signal log, which shows a timestamp predating the official alert; (4) choose between evacuating stranded civilians first (+2 squad trust) or securing extra signal intelligence first (+1 intel, -1 trust); (5) reach the western rally point and show a debrief affected by the selected choice. All targets must be reachable; interactions must require proximity and explicit input. The movement joystick must stop when released and when paused. Control sensitivity must be adjustable in the pause screen.

Characters: Captain Mara Voss (reserved mentor with an earlier failed evacuation), Idris Kane (empathetic medic), Leena Suri (communications engineer), Tomas Vale (careful reconnaissance specialist), Rowan Kade (ambitious rival whose attitude can change) and Sera Dain, a former signal director who engineers blackouts to force adoption of a centralized network after her brother died in a real communication failure. Her concern about neglect is understandable, but deliberately endangering settlements is culpable. Reveal her identity gradually; resolve the central mystery in the first campaign.

Show seven planned operations in the dossier: Signal in the Dust, Broken Relay, Silent Crossing, The Long Return, Fault Lines, Ember Protocol, Last Signal. Mission 01 is the only fully playable mission in this first increment; mark other missions as planned/locked rather than pretending they are implemented.

Use local device persistence (DataStore or SharedPreferences) for callsign, selected kit, trust, intel, completed mission, story flags and the last choice. No account system, multiplayer, backend, paid service, API key or AI calls during gameplay. Include a restart/reset flow and robust failure handling. Keep code separated into screen, map/rendering, controls, mission state, campaign data and local-save modules.

Before finishing, compile and launch the app in the available Android emulator. Fix all build/runtime errors, verify touch movement, target reachability, both choice branches, pause/resume, save restoration after process restart, and responsive layout on narrow phones. Report exactly what was run and tested; do not claim a test passed if it was not executed. Build only the first mission to a polished standard before expanding the campaign.
```
