# 06 — Gameplay Mechanics

**Purpose:** Authoritative rules for every mechanic. **Status:** Proposed · **Last reviewed:** 2026-10-18
Numeric values are starting values (`Needs Validation` via playtest). Layout of controls: [08](08_UI_UX_AND_SCREEN_SPECIFICATIONS.md).

## 1. Control scheme (landscape)
| Zone | Input | Function |
|---|---|---|
| Left thumb | Floating virtual stick | Move; push to edge to sprint |
| Right thumb | Fire button (large) + aim drag | Aim direction; auto-aim assist |
| Right cluster | Interact, Reload, Cover/Crouch, Ability | Context actions |
| Top | Pause, Objective, Squad order wheel | Meta |
Alternative: tap-to-aim mode (accessibility). Left-handed mirror.

## 2. Mechanics specification
Format: **Rules · Input · Response/feedback · Edge cases · Acceptance.**

### MECH-001 Movement
- Rules: walk 3.0 m/s, sprint 5.0 m/s (stamina 4 s, regen 3 s), crouch 1.5 m/s; collision with navmesh; slopes up to 30°.
- Input: stick vector magnitude maps speed.
- Feedback: footstep audio per surface; dust puffs; camera look-ahead 1.5 m.
- Edge: stick released mid-sprint → decelerate 0.15 s; multitouch ignored if >2 pointers.
- Acceptance: AC-001 stick-to-motion latency ≤ 100 ms on target devices (measured, EXP-02).

### MECH-002 Aiming and firing
- Rules: aim direction from right-drag; aim assist snaps within 12° cone to nearest valid target (strength by difficulty); weapon stats in [07](07_PLAYER_PROGRESSION_AND_ECONOMY.md).
- Response: muzzle flash, tracer, hit spark, damage number optional, controller haptic tick.
- Edge: no ammo → click feedback and auto-reload prompt; friendly fire off.
- Acceptance: AC-002 fire-to-feedback ≤ 80 ms; ammo count accurate across pause/resume.

### MECH-003 Cover
- Rules: entering cover near cover object (≤1 m) by Cover button; in cover, incoming hit chance reduced 50% from front arc; peek by aim drag.
- Edge: destructible light cover breaks after N hits (flagged on prop).
- Acceptance: AC-003 cover entry/exit ≤ 0.3 s, no stuck states in 100 scripted trials.

### MECH-004 Stealth/detection
- Rules: enemy vision cone 70° / 8 m (day), 5 m (night); noise radius sprint 6 m, walk 3 m, crouch 1 m. Detection meter fills 0→1 over 1.2 s in the cone. States: Idle→Suspicious (0.4)→Alert (1.0).
- Feedback: cone drawn as soft wedge; "?" icon then "!" icon with shape change (colour-blind safe); audio sting.
- Edge: line-of-sight blockers; allies in sight add cover; detected player triggers search for 10 s.
- Acceptance: AC-004 deterministic detection under replay of scripted inputs.

### MECH-005 Health, down and revive
- Rules: 5 pips; regen of 1 pip after 6 s unhurt (max to nearest full pip); at 0 → Downed 8 s (Standard); squadmate or Field Dressing revives; else fail.
- Acceptance: AC-005 fail triggers only after timer or no allies alive.

### MECH-006 Rescue/carry
- Rules: interact to lift a civilian; carry speed 2.0 m/s; cannot fire while carrying; drop at safe zone to complete.
- Edge: carrier downed → civilian drops safely; multiple civilians guided by Follow tether.
- Acceptance: AC-006 no civilian can be lost to a physics glitch in 50 scripted runs.

### MECH-007 Squad orders
- Rules: wheel with Hold / Follow / Advance / Use Ability (per member). Cooldowns on abilities: Relay Ping 20 s, Field Dressing 25 s, Breach 30 s.
- AI: allies avoid blocking player; path-follow with formation offsets.
- Acceptance: AC-007 orders take effect within 0.5 s; AI never stands inside player collider for >1 s.

### MECH-008 Beacon repair (signature verb)
- Rules: hold Interact on a beacon node; 3-step timing minigame (align ring, balance pressure, light flare); each step 3–5 s; failure adds 5 s penalty but never hard-fails except in M-401.
- Feedback: haptic pulse; beacon glow; audio swell.
- Acceptance: AC-008 completable one-handed; skip option in accessibility menu ("Assist: auto-complete after 2 fails").

### MECH-009 Environmental traversal
- Rules: climb ropes/ladders (hold direction), cross unstable planks (balance assist), flood-water slows 40% and drifts; deep water fatal only in training failure, otherwise respawn at checkpoint.

### MECH-010 Timed objectives
Timer shown top-centre with colour + numeric; pause stops timer; difficulty adds ±20%.

### MECH-011 Dialogue and choices
Max 3 options; 10 s optional timer **off by default** (accessibility). Choice recorded as flag (see [15](15_DATA_MODEL_AND_SAVE_SYSTEM.md)). Each choice labelled internally Cosmetic / Local / Campaign.

### MECH-012 Vehicle (post-MVP)
Simple lane-based river skiff or truck segment; `Deferred`.

## 3. Choice taxonomy
| Class | Example | Effect |
|---|---|---|
| Cosmetic | Banter option | Dialogue variation |
| Local | F_MERCY_P01 | Changes P-01 bonus and M-101 greeting |
| Campaign | F_RIVAL_ALLY, F_TOWER7_LOG_COPIED, F_KETH_TRUST | Gate endings, later scenes |

## 4. Edge-case policy
App backgrounded mid-mission → auto-pause and snapshot; incoming call → pause; low-memory → drop to Medium profile warning; thermal → reduce particles first.

## 5. Replayability
Optional objectives (OO), medals (Bronze/Silver/Gold by time + accuracy + civilians saved), Veteran mode, alternate choices.

## 6. Test hooks
All mechanics expose deterministic simulation (seeded RNG, fixed 30 Hz tick) to support automated tests ([20](20_TESTING_AND_QUALITY_ASSURANCE.md)).
