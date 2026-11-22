# 05 — UI, HUD and Chapter Select

## HUD contract

The HUD is static markup in `index.html`; `game.js` only reads ids, never builds
layout in the hot loop. `tools/dom-audit.js` fails the build if a referenced id
is missing.

| Region | ids | Behaviour |
| --- | --- | --- |
| Objective badge | `hud-obj-title`, `hud-obj-detail`, `hud-obj-progress`, `hud-sector` | Updates on objective change and every 200 ms |
| Telemetry | `hud-health-bar`, `hud-health-text`, `hud-ammo-display`, `hud-score`, `hud-timer`, `btn-pause` | Health and ammo instant, score and timer throttled |
| Compass | `compass-arrow`, `compass-distance` | Points at the waypoint, shows metres |
| Crosshair | `.crosshair`, `#crosshair-label` | Lock state: `BODY` / `HEAD`, colour + thickness change |
| Feedback | `hitmarker`, `damage-flash`, `low-health`, `pickup-toast`, `ammo-warning` | Event driven, auto-hide |
| Boss | `boss-bar`, `boss-bar-fill`, `boss-name` | Hidden until a boss is alive |
| Damage numbers | `damage-layer` | One absolutely positioned div per hit, projected from world space |
| Controls | joystick zone, aim zone, `btn-fire`, `btn-jump`, `btn-reload`, `btn-scan`, `btn-interact` | Touch first, mirrored on keyboard |

Colour language: amber `#E7A83E` is the player and objectives, red `#C84C4C` is
damage and hostility, green `#38D39F` is success and progress, cyan is intel.

## Deployment screen (chapter select)

Purpose: prove the campaign exists and let the player pick a chapter.

Shown when the page is opened **without** a `mission` parameter, and reachable
from the pause overlay through `CHAPTER SELECT`. The Android WebView always sends
a mission id through the bridge, so the deployment screen never appears there.

Layout:

```
OPERATION: EMBERWATCH                     [ 3 / 10 CLEARED ]
CHAPTER SELECT
+-----------------------------------------------------------+
| CH01  SIGNAL IN THE DUST        coastal   ***   CLEARED   |
| CH02  BROKEN RELAY              coastal   **    CLEARED   |
| CH03  SILENT CROSSING           marsh     *     UNLOCKED  |
| CH04  THE LONG RETURN           highway         LOCKED    |
| ...                                                        |
+-----------------------------------------------------------+
[ UNLOCK ALL CHAPTERS ]            [ BEGIN CH03 ]
```

Rules:

- Every chapter is always visible, including locked ones — the campaign must read
  as ten chapters, not one.
- A card shows: chapter number, title, theme tag, hostile count, stars earned.
- Locked cards are dimmed, not hidden. Selecting one plays a toast explaining
  which chapter unlocks it.
- Progress is stored in `localStorage` under `emberwatch.progress.v1` as
  `{ "M1_SIGNAL_IN_DUST": 3, ... }`.
- Clearing a chapter writes its stars and unlocks the next one.
- `UNLOCK ALL CHAPTERS` (browser only) seeds every chapter at one star so a
  reviewer can jump straight to the finale.

## Overlays

| Overlay | ids | Notes |
| --- | --- | --- |
| Boot card | `boot-card` | Chapter / title / sector, fades after 2.6 s |
| Dialogue | `dialogue-modal`, `dialogue-speaker`, `dialogue-text`, `dialogue-next-btn` | Drops held input while open |
| Choice | `choice-modal`, `choice-title`, `choice-desc`, `choice-options` | Two protocol cards |
| Pause | `pause-modal`, `pause-resume-btn`, `pause-restart-btn`, `pause-chapter-btn`, `pause-abort-btn` | Idempotent pause |
| Fail | `defeat-modal`, `defeat-retry-btn`, `defeat-abort-btn` | |
| Victory | `victory-modal`, `victory-stars`, `victory-time`, `victory-score`, `victory-copy`, `victory-continue-btn` | |

All overlays sit above the HUD layer, are `pointer-events: auto`, and use the
same dark panel + amber header treatment.

## Accessibility

- High contrast mode brightens HUD text and thickens the crosshair.
- All text ≥ 9 px with 1 px letter spacing on a dark backing plate.
- Touch targets ≥ 44 px.
- No information is conveyed by colour alone: the crosshair label spells out the
  lock state, stars are glyphs (`★`/`☆`), locks say `LOCKED`.
