# ASSET LICENSES

Licence log required by `docs/Valor_Academy_Launch_and_LiveOps.md` Section 5.4
("Music/audio/fonts/assets: only licensed or original; keep `ASSET_LICENSES.md`")
and read by `com.example.game.valor.launch.IpSafety`.

Rule: a file enters the repository only with a row here. If the source or the
licence is unknown, the asset is not shipped.

| Asset | Path | Source | Licence | Attribution required |
|---|---|---|---|---|
| Three.js r13x (minified) | `app/src/main/assets/game/three.min.js` | Three.js Authors, SPDX MIT (header kept in file) | MIT | Yes - copyright line retained in the bundled file |
| Game viewport (HTML/JS/levels) | `app/src/main/assets/game/index.html`, `game.js`, `levels.js` | Original work for this project | Original | No |
| Kotlin sources and Compose UI | `app/src/main/java/**` | Original work for this project | Original | No |
| Launcher icon (adaptive background + foreground) | `app/src/main/res/drawable/ic_launcher_background.xml`, `ic_launcher_foreground.xml`, `res/mipmap-*` | Generated for this project | Original | No |
| Audio | `game/AudioFeedback.kt` | Synthesised at runtime; no audio files shipped | Original | No |
| Fonts | Not bundled - the UI uses the platform `FontFamily.Monospace` | Android platform | Platform | No |

## What is not in the repository

- No art, audio, voice, music, model, texture or UI file from any commercial
  game. `IpSafety.REUSES_ORIGINAL_GAME_ASSETS` is `false` and the gate fails if
  it ever becomes `true`.
- No real brand logos or trademarks (`IpSafety.SHOWS_REAL_BRAND_LOGOS` is
  `false`).
- No stock asset of unknown provenance.

## Content naming

- Weapon names are generic (`IpSafety.GENERIC_WEAPON_NAMES`).
- Enemy factions in modern acts are fictional: Northern Coalition and Red
  Meridian (`ContentNotes.FICTIONAL_MODERN_FACTIONS`).
- The in-game top award is "Medal of Valor"; the real award name is referenced
  respectfully in story text only and never used as a product brand
  (`IpSafety.REAL_AWARD_NAME_USED_AS_BRAND` is `false`).

## Before adding a third-party asset

1. Record the asset, path, source URL, licence name and whether attribution is
   required in the table above.
2. Add the attribution to the credits screen (`IpSafety.CREDITS_SCREEN`).
3. Re-run the launch gate: `LAUNCH_PRIVACY` checks licence file, credits screen
   and the IP booleans together.
