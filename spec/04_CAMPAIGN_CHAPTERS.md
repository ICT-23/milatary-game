# 04 — Campaign: Ten Chapters

The campaign is authored, not generated. Each chapter is data in `levels.js`:
theme, spawn, objectives, hostiles, supplies, dialogue, choice, and par time. The
Android dossier in `MissionDefinition.kt` mirrors the same order and ids, because
`nextAfter()` derives unlock order from that list.

| # | id | Title | Theme | Hostiles | Beat |
| --- | --- | --- | --- | --- | --- |
| 1 | `M1_SIGNAL_IN_DUST` | Signal in the Dust | coastal | 2 scouts | Teach movement, talk, scan, one choice |
| 2 | `M2_BROKEN_RELAY` | Broken Relay | coastal | 4 scouts, 1 sentinel | First turret: cover matters |
| 3 | `M3_SILENT_CROSSING` | Silent Crossing | marsh | 5 scouts, 1 sentinel, 1 walker | First walker: reposition, aim at the head |
| 4 | `M4_THE_LONG_RETURN` | The Long Return | highway | 6 scouts, 1 sentinel, 1 walker | Convoy ambush, defend waves |
| 5 | `M5_FAULT_LINES` | Fault Lines | mine | 6 scouts, 1 sentinel, 1 walker | Dark theme, beacons become navigation |
| 6 | `M6_EMBER_PROTOCOL` | Ember Protocol | fort | 8 scouts, 2 sentinels, 1 walker | Three-wave defence of the transmitter |
| 7 | `M7_LAST_SIGNAL` | Last Signal | platform | 5 elites, 2 sentinels, 1 walker, 1 boss | The Architect's command frame |
| 8 | `M8_ASHES_OF_CHIMERA` | Ashes of Chimera | platform | 6 scouts, 2 sentinels, 1 walker | Platform is scuttling: timed extraction |
| 9 | `M9_THE_QUIET_COAST` | The Quiet Coast | marsh | 8 scouts, 2 sentinels, 2 walkers | Hunt the surviving cells, hold a bridge |
| 10 | `M10_EMBERWATCH` | Emberwatch | fort | 10 scouts, 3 sentinels, 2 walkers, 1 boss | Final defence of the academy, Warden frame |

## Chapter 8 — Ashes of Chimera

The platform is tearing itself apart. Objective chain:

1. `reach` **REACH THE LOWER DECK** — the deck is already tilted; the player has
   to cross open ground under fire.
2. `destroy` **SILENCE THE PURSUIT** — 4 scouts, squad `pursuit`.
3. `scan` **RECOVER THE FLIGHT RECORDER** — the evidence that clears Emberwatch.
4. `destroy` **BREAK THE LOCKDOWN** — 1 sentinel + 1 walker, squad `lockdown`.
5. `choice` — Protocol Alpha: transmit the recorder from the deck. Protocol
   Bravo: carry it out by hand.
6. `reach` **EXTRACT BEFORE SCUTTLE** — par 300 s.

## Chapter 9 — The Quiet Coast

Three cells survived the platform. The marsh is where they hide.

1. `talk` **LINK UP WITH TOMAS**.
2. `destroy` **CLEAR THE NORTH HIDE** — 4 scouts, 1 sentinel.
3. `destroy` **CLEAR THE SOUTH HIDE** — 4 scouts, 1 walker.
4. `defend` **HOLD THE BRIDGE** — 3 waves, walker in the final wave.
5. `choice` — Protocol Alpha: take prisoners. Protocol Bravo: burn the hide.
6. `reach` **EXTRACT** — par 360 s.

## Chapter 10 — Emberwatch

The academy itself is the last target. The chapter is a defence inverted: you
must hold three points, then break the frame that is shelling them.

1. `destroy` **REPEL THE FIRST ASSAULT** — 5 scouts, squad `assault`.
2. `defend` **HOLD THE PARADE GROUND** — 3 waves from the gate.
3. `destroy` **BREAK THE SIEGE** — 3 sentinels, squad `siege`.
4. `story` beat — the Warden command frame lands on the parade ground.
5. `destroy` **DESTROY THE WARDEN FRAME** — boss, squad `warden`, escorts at
   66% and 33% hull, par 540 s.
6. `choice` — Protocol Alpha: broadcast the truth live. Protocol Bravo: seal
   the archive.
7. `reach` **STAND DOWN** — extraction at the LZ.

## Authoring rules for every chapter

- The spawn point is walkable with a 0.7 m pad, and every objective centre is
  walkable with a 0.55 m pad (`tools/level-audit.js` enforces this).
- Every ground hostile is a *placed* hostile with its own footprint of 1.2 m
  (sentinel), 1.3 m (walker) or 1.7 m (boss), also audited.
- Hostiles placed on the route carry squad `guard` unless a `destroy` objective
  names them, in which case they carry that objective's squad.
- Wave hostiles never carry `guard`, so a `defend` objective can never wait on a
  unit it does not spawn.
- Alert radii are set so the player is never shot in the back at spawn: no
  hostile within its alert radius of the spawn point.
- Each chapter has at least 4 supply caches, each at least 1.2 m from geometry.
- Par time is reachable without sprint mechanics: roughly the walking distance
  at 10.5 m/s plus 12 s per hostile group.

## Campaign structure

- Chapter 1 is always unlocked; chapter *n+1* unlocks when *n* is cleared.
- Stars: 1 for completion, +1 at ≥60% health, +1 at ≤ par time.
- The finale's debrief shows `OPERATION EMBERWATCH // COMPLETE` instead of a
  next-chapter line.
