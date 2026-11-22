# 02 — Rules, Combat and Systems

Everything below is normative. Numbers are the shipped values unless a chapter
overrides them.

## Core loop

```
DEPLOY -> BRIEF -> TRAVERSE -> CONTACT -> OBJECTIVE CLEAR -> NEXT OBJECTIVE
                                                              |
                                                    CHOICE -> EXTRACT -> DEBRIEF
```

- A chapter is an ordered list of objectives. Exactly one is active.
- The active objective owns the waypoint beacon, the HUD title and detail lines.
- Objectives complete by fulfilling their `kind`:
  `talk`, `scan`, `interact`, `reach`, `destroy`, `defend`, `choice`.
  `auto: true` completes the moment the player is inside its radius.
- On completion: `playSound('objective')`, toast, HUD refresh, next objective.
- The chapter ends at extraction. A `destroy` objective that spawns a boss is
  only satisfied when the boss itself is dead.

## Player

| Stat | Value |
| --- | --- |
| Move speed | 10.5 m/s scaled by the device sensitivity setting (clamped 0.6–1.4) |
| Health | 100, regenerates 3.5/s after 8 s without taking damage |
| Magazine | 30 rounds, reserve from the chapter (`reserveAmmo`, default 120) |
| Reload | 1.7 s, cannot fire during it |
| Fire rate | one shot per 0.115 s |
| Jump | 8.5 m/s initial, 24 m/s² gravity |
| Player radius | 0.55 m for collision |

Damage taken: an enemy bolt that reaches within 1.7 m of the player's chest deals
the hostile's `damage`. Below 40% health the low-health vignette pulses.

## Aiming — the hard rule

**A shot can only damage an enemy whose body the aim ray intersects.**

1. The aim direction is `(sin(yaw)·cos(pitch), sin(pitch), cos(yaw)·cos(pitch))`
   where `yaw` is `player.rotY` and `pitch` is `player.aimPitch`, clamped to
   roughly ±28°. Dragging up raises the barrel, dragging down lowers it.
2. The aim ray starts at the player's chest (`y + 1.55`) and is resolved against
   every alive hostile's hitboxes, nearest hit wins.
3. Hitboxes are stacked spheres, defined per archetype:

   | Part | Multiplier | Note |
   | --- | --- | --- |
   | Head / sensor / core | ×2 damage | rarer, smaller, worth aiming for |
   | Body | ×1 damage | the default target |
   | Legs | ×0.5, rounds up to 1 at minimum | staggers, does not kill quickly |
   | Boss core | ×1, but the boss has 60 HP | aim matters, position matters more |

4. **Cover matters.** The ray is sampled every 1.2 m against the level colliders.
   If a wall is nearer than the hostile, the shot is a miss no matter how dead
   centre the aim was, and the impact lands on the wall.
5. There is **no soft lock**. `acquireTarget` is only used to drive the crosshair
   readout, never to bend a bullet.
6. Crosshair states, evaluated every frame:
   - idle — thin amber cross,
   - **on body** — turns red and shows `BODY`,
   - **on head** — turns red, thicker, shows `HEAD`,
   - reloading — dimmed and rotating.

## Firing sequence

```
input -> rate limit -> ammo check -> consume round -> muzzle flash + recoil
      -> resolve aim ray -> [hit] sparks + damage number + flinch + hitmarker
                         -> [miss] impact puff on the surface that was hit
      -> tracer line drawn from the muzzle to the resolved point (60 ms)
```

Ammo is consumed by the trigger pull, not by the hit. Firing on empty triggers
`startReload` and plays the dry click.

## Damage and death

- `damageDrone(drone, point, amount, part)` applies `amount` after the part
  multiplier and returns whether the unit died.
- A dying unit stops being targetable immediately, spawns sparks scaled by its
  mass, plays an explosion, shakes the camera, and its rig tips over and sinks
  for 1.2 s before being removed. It is never just switched off.
- Kills credit `mission.killed[squad] += 1` so `destroy` and `defend`
  objectives can never be short-circuited.
- Score: scout 250, sentinel 400, walker 600, escort 250, command frame 3000,
  range target 150. Time bonus at extraction is `max(0, par − elapsed) × 5`.

## Enemy rules

| Archetype | Behaviour |
| --- | --- |
| Scout | Hovers, strafes its patrol circle, fires aimed bolts with 5° error |
| Sentinel | Static turret, tracks the player with a damped head turn, fast bolts, 30 m alert radius |
| Walker | Closes to 14 m, stomps, heavy slow bolts, 40 m alert radius |
| Escort | Wave-only scout, squad `escort`, does not count toward `guard` objectives |
| Command frame | Boss: holds 10–17 m, fires 3-bolt volleys, deploys escorts at 66% and 33% hull |

Alert rule: a hostile engages only inside its `alertRadius`, or when it or a unit
of its squad has been shot. Wave spawns keep the legacy tuning (4 HP, 42 m
alert, 2.4 s cadence) so `defend` waves never get easier by accident.

## Non-combat systems

- **Scanner** — 1.1 s cooldown, expands a ground ring from the player; it is a
  readability tool, not a detector, and grants no information the player lacks.
- **Supply caches** — `{ type: 'ammo' | 'medkit', x, z }`, picked up inside
  1.6 m, restores 45 reserve rounds or 35 health, toast on pickup.
- **Choices** — one per chapter, two protocols. Each carries a score bonus and a
  relationship tag that the Android roster reads.
- **Pause** — freezes the simulation, opens the overlay, drops all held input.
- **Abort** — returns to HQ on Android, reloads the deployment screen in browser.

## Failure and recovery

- Death: `SIMULATION FAILED` overlay, retry restarts the chapter.
- All latched input is cleared on death, pause, dialogue, choice and focus loss,
  so a chapter can never resume running or firing on its own.
