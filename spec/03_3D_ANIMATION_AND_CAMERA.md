# 03 — 3D, Animation and Camera

The game must read as a real 3D place from the first frame. This file lists what
that means concretely.

## Renderer

| Setting | Value | Why |
| --- | --- | --- |
| Renderer | `WebGLRenderer({ antialias: true })` | silhouette edges matter in low-poly |
| Pixel ratio | `min(devicePixelRatio, 2)` | sharp on phones without melting them |
| Shadow map | `PCFSoftShadowMap`, 2048² | soft contact shadows, not stair-stepped |
| Tone mapping | `ACESFilmic`, exposure ~1.05 | stops the emissive cores from clipping |
| Output colour space | `sRGB` (r128: `outputEncoding`) | matches the CSS palette |
| Fog | `FogExp2(theme.fog, theme.fogDensity)` | distance reads as depth |

## Lighting

1. **Sun** — one `DirectionalLight` per scene at the theme colour, casting
   shadows. The shadow camera is fitted to the playable bounds, so a 200 m map
   still gets a crisp shadow under the player.
2. **Hemisphere fill** — `theme.hemiSky` above, `theme.hemiGround` below, so
   shadowed geometry goes cool, never black.
3. **Accent points** — beacons and reactor cores use point lights with a short
   falloff and `decay`, so darkness themes still have readable hot spots.
4. **Muzzle light** — 12 m falloff, 45 ms flash, only while firing.

## Sky

A 420 m inverted sphere with a vertical gradient texture built from the theme:
`sky` at the horizon, a 25% darkening toward the zenith, a subtle warm band where
the sun sits. No external image assets — it is generated on a 2×256 canvas so it
stays inside the offline bundle.

## Camera

Third-person over-the-shoulder, and it looks **along the aim direction** so the
crosshair is truthful:

```
chest   = player.pos + (0, 2.05, 0)
flat    = normalize(aim.x, 0, aim.z)
desired = chest - flat * 8.6 + (0, 0.9 + aimPitch * 3.0, 0)
camera.position.lerp(desired, min(1, dt * 9))
camera.lookAt(chest + aim * 60)
```

- Aiming up raises the camera and tilts the view up; the body stays in the lower
  third of the frame.
- Camera shake is applied after `lookAt` so the reticle stays honest.
- No camera collision — the level design keeps the player out of corners too
  tight for an 8.6 m boom.

## Player animation

| State | What moves |
| --- | --- |
| Idle | torso breathes 0.05 m, left arm settles to a support pose |
| Walk | legs swing ±0.75 rad, arms counter-swing, torso bobs 0.14 m |
| Airborne | legs tuck, arms stay on the weapon |
| Fire | recoil kicks the torso and arm back, muzzle light flashes |
| Death | model drops and the screen goes to the fail overlay |

All cycles are driven by `player.animTime` so they run at the same rate as the
simulation and freeze with it.

## Hostile animation

| Rig | Animated parts |
| --- | --- |
| Scout | four rotors spin fast, body hovers on a sine bob, yaw turns toward the player, tilts into its strafe |
| Sentinel | head yaw/pitch tracks the player with a damped turn, tripod static, red sensor pulses |
| Walker | two legs stride with a 0.35 rad swing, body rocks, torso yaws to face the player |
| Command frame | leg pairs stride, shoulder pods flare before a volley, core pulses amber |
| Any | hit flinch: 1.08× scale pulse over 120 ms, plus a spark burst at the impact point |
| Any death | rig rotates forward and sinks 1.2 s, sparks scaled by mass |

## VFX inventory

| Effect | Trigger | Implementation |
| --- | --- | --- |
| Muzzle flash | every shot | point light + billboard flash, 45 ms |
| Tracer | every shot | additive line from muzzle to resolved impact, 60 ms |
| Impact sparks | hit | 5–60 particles, colour by surface |
| Damage number | hit | DOM text projected from world space, floats up, 700 ms |
| Hitmarker | hit / kill | crosshair X, red for hit, white larger for kill |
| Enemy bolt | hostile fire | glowing sphere + trail, 3.2 s life |
| Scanner ring | scan button | expanding ring, 1.1 s |
| Extraction smoke | chapter end | rising smoke columns at the LZ |
| Low-health vignette | health < 40% | CSS radial pulse |
| Damage flash | player hit | CSS red edge flash, 260 ms |

## Environment requirements per theme

Every theme provides: ground plane, boundary structures that read as walls, at
least one tall landmark visible from spawn, at least one light source with
colour, and geometry that creates sight-line breaks. `tools/free-scan.js` must
show a walkable path from spawn to every objective.

## Performance budget

- ≤ 220 draw calls, ≤ 120k triangles per scene.
- No per-frame allocation in the hot loop: no `new THREE.Vector3()` inside
  `updateDrones`/`updateBullets`/`updatePlayer`.
- HUD text updates throttled to 200 ms (`updateHUD`), except health and ammo
  which must react instantly.
- Damage numbers and tracers are pooled/removed after their lifetime, never left
  in the scene graph.
