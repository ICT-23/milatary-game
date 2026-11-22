# 01 — Vision and Design Pillars

## The fantasy

You are **Spectre-7**, the field operative of Emberwatch, a military academy that
has been framed for an atrocity it did not commit. Ten chapters take you from a
dust-choked coastal relay outpost to the transmission deck where the truth is
finally broadcast. You are alone in the field, guided by radio. Everything you
do is observed: the squad rates your run, the campaign remembers your decisions.

The promise to the player, in one line:

> **Aim at it, hit it, and it dies — and every chapter looks like a place, not a
> grey box.**

## Pillars

### 1. Honest shooting
The crosshair is the truth. A shot damages the enemy **only** when the aim ray
actually intersects the enemy's body. No aim assist that quietly hits what you
were not pointing at, no wallhack, no damage at range when the shot was blocked
by cover. Feedback is immediate and physical: muzzle flash, tracer, impact
sparks, a floating damage number, a hitmarker, and a visible flinch on the body
that was hit.

### 2. Readable 3D
The world is lit, shadowed, fogged and animated. Every hostile has a rig with
moving parts. The player has a walk cycle, a combat stance, recoil, and a
breathing idle. Nothing in frame is a static, unshaded rectangle:
- a real directional sun with shadow casting over the play area,
- hemisphere fill so shadows are blue-grey instead of black,
- a gradient sky dome instead of a flat clear colour,
- exponential fog matched to the theme so distance reads as depth,
- contact shadows under the player and every ground hostile,
- emissive accents (visor, reactor cores, beacons) that glow in the dark themes.

### 3. Chapters, not levels
Ten chapters, each with a title, a sector, a briefing, a hostile profile, an
objective chain and a choice with consequences. Progress is visible: the
deployment screen shows every chapter, which are cleared, which are locked and
how many stars each was worth.

### 4. Consequences that persist
Choices made in a chapter change the squad's view of you (trust, discipline,
intel). Stars are earned by finishing, staying above 60% health, and beating par
time. The Android layer persists both; the browser build persists stars and
unlocks locally.

### 5. Touch-first, desktop-capable
Everything is playable with two thumbs. On desktop the same verbs exist on the
keyboard and the mouse. A control is never the only way to do something.

## Target experience beat by beat

1. **Boot** — chapter card slams in over the live 3D scene: chapter number,
   title, sector. Two and a half seconds, then it fades.
2. **Brief** — radio dialogue with the squad, objective appears, waypoint
   beacon lifts off the objective.
3. **Approach** — you move through geometry that blocks sight lines. Enemies are
   visible before they are lethal; walkers stomp, sentinels track, scouts hover.
4. **Contact** — you aim, see the crosshair lock, fire, watch the body react.
5. **Push** — objectives chain: reach, talk, scan, destroy, defend, choose.
6. **Climax** — the chapter's hardest fight, with a boss health strip for the
   command frames.
7. **Debrief** — stars, time, score, the consequence of your choice, next chapter.

## Non-goals

- No multiplayer, no live service, no ads, no loot boxes.
- No procedural generation of objectives; every chapter is authored.
- No photoreal rendering. The look is stylised low-poly military, and the budget
  is "smooth on a mid-range phone".
