# OPERATION: EMBERWATCH

An original Android-first, story-driven military adventure prototype built with **React Native + TypeScript + Expo**. The first playable mission is **Signal in the Dust**.

## What is implemented

- Original Asterra field map drawn with SVG elements (not a static screenshot).
- Recruit profile with editable callsign and three visual kits.
- Briefing screen, five-stage mission objective flow, on-screen movement joystick, proximity-based interactions, scan/target feedback, pause menu and debrief.
- Scripted dialogue for Captain Mara Voss and engineer Leena Suri.
- Consequential civilian-vs-intelligence choice that persists squad trust, intel and story flags.
- Local save storage through AsyncStorage. No game server, account, or gameplay API is required.
- Campaign dossier for the seven-operation roadmap. Mission 01 is implemented; missions 02–07 are currently design cards, not playable levels.
- Custom app icon, adaptive icon, splash artwork, and EAS build profiles.

## Requirements

- Node.js **22.13 or newer** for the selected Expo SDK 57 baseline.
- npm, a compatible Android phone with Expo Go or a development build, or Android Studio/emulator.
- An Expo account only for cloud EAS builds. Core gameplay is offline. Version ranges are aligned to the Expo SDK 57 / React Native 0.86 baseline; always run `npx expo install --check` before testing or building to verify the currently compatible patches.

## Run the project

```bash
npm install
npx expo install --check
npx expo start
```

Scan the QR code with a compatible Expo Go client, or press `a` to open an Android emulator. If your installed Expo Go does not support this SDK, create a development build instead:

```bash
npx expo install expo-dev-client
npx expo run:android
```

Type-check the source after dependencies install:

```bash
npm run typecheck
```

## Create an Android build

For a device-installable test APK:

```bash
npm install --global eas-cli
eas login
eas build:configure
eas build --platform android --profile preview
```

For a Google Play upload (AAB):

```bash
eas build --platform android --profile production
```

The build command produces an artifact only after a real EAS/native build completes. This source bundle does not include a compiled APK/AAB, Play Console listing, signing key, or store approval.

## Controls

- **Move:** drag the virtual joystick at bottom-left.
- **Scan:** tap SCAN to show distance and the active target.
- **Interact:** move close to the glowing target (roughly 37 map units) and tap the large right-hand action button.
- **Pause:** use the pause icon in the HUD.
- At the civilian decision, pick one of two options. The choice affects the debrief and local save values.

## Mission path

1. Meet Captain Voss.
2. Restore the field relay with Leena's help.
3. Recover the suspicious signal log.
4. Choose whether to evacuate civilians first or secure extra intelligence.
5. Reach the western rally point and review the after-action report.

## Project structure

```text
App.tsx
app.json
eas.json
assets/
  icon.png
  adaptive-icon.png
  splash.png
src/
  components/
    ControlPad.tsx
    WorldMap.tsx
  data/
    missions.ts
  lib/
    storage.ts
  types/
    game.ts
  theme.ts
```

## Current limitations / honest status

- This is a playable **first-mission prototype**, not a finished commercial 3D military game. It uses an original illustrated top-down map rather than high-end 3D.
- No weapons/combat loop, enemy AI, voice acting, cinematic video, advanced pathfinding, settings page, analytics, multiplayer, store billing or cloud sync is included.
- The civilian choice changes saved consequences and debrief narration. Long-term branches for missions 02–07 are a roadmap, not implemented campaign levels.
- The supplied brief ended partway through Mission 01's objective list, so the prototype follows the material supplied and fills the remaining interaction flow conservatively.
- The build has **not** been compiled or played on an Android device in this workspace. The package-install step timed out in this workspace, so only source/configuration smoke checks were completed; actual dependency resolution, native compilation, and device behavior remain unverified. Install dependencies and run the steps above before treating it as tested or publishing it.

## Release checklist before Play Store submission

1. Run `npx expo install --check`, `npm run typecheck`, and `npx expo-doctor`.
2. Test joystick, map target reachability, text layout, app pause/resume, app relaunch/save restoration, both narrative choices, and low-end Android performance.
3. Add an appropriate content rating, privacy policy if required by the final data practices, screenshots, store listing, age/audience settings and support contact.
4. Confirm package name, app title, icon, target SDK, version, signing, and Play Console testing requirements.
5. Produce a production `.aab`, test it through Play Console's internal testing track, then submit for review.
