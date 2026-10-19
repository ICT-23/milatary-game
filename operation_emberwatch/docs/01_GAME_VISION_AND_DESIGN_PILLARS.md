# 01 — Game Vision and Design Pillars

**Purpose:** Make creative trade-offs consistent.  
**Status:** Proposed  
**Last reviewed:** 2026-10-19

## Vision

Deliver a compact, cinematic, character-led military adventure where being dependable matters more than being the strongest shooter. Players earn responsibility through observation, calm decisions, teamwork and rescue—not through endless combat volume. The world feels grounded and weathered but remains fictional, readable and appropriate for mobile action.

## Pillars

### P1 — Earn the uniform
Training is playable and has consequences. Drills teach one reusable skill each; the game later asks the player to apply it under pressure. A training score gives feedback, not a gate that permanently blocks story progress.

**Test:** Every core ability is first taught in a low-pressure context before a mission depends on it.

### P2 — People are the stakes
Squadmates have independent goals, useful disagreements and growth. Trust is a readable narrative system, not a hidden moral score. Decisions can change reactions and selected mission details, but do not create an unbudgeted branching epic.

**Test:** Each major choice has a visible immediate reaction and a documented later consequence or an explicit label that it is local-only.

### P3 — Action with rhythm
Move, observe, help a teammate, navigate a hazard, use short combat bursts, recover and reflect. No mission is only an enemy wave. Combat is stylized and accessible; threat readability is more important than simulation.

**Test:** Each mission has at least two distinct activity rhythms and a non-combat objective that matters.

### P4 — Clarity before spectacle
The screen shows one primary objective, clear interaction affordances and legible threats. Camera, color, animation and audio support the next player decision. Cinematics are short and skippable after first watch where possible.

**Test:** In playtests, players can identify the objective and available interaction without reading a long tutorial.

### P5 — A complete offline story
Core progression, saves, settings and the ending work without an account, ads or network access. The first release stands on its own.

**Test:** A clean install can complete the campaign in airplane mode after installation.

## Experience target

| Dimension | Target |
|---|---|
| Camera | Third-person follow camera with controlled shoulder offset; no first-person aim mode in MVP. |
| Pace | Calm tutorial → focused drill → field tension → rescue urgency → reflective resolution. |
| Visual tone | Weathered coastal realism with restrained stylization, warm rescue lights against slate-blue storm environments. |
| Difficulty | Accessible default, adjustable camera and aim assist, forgiving checkpoints, no punitive rank loss. |
| Session | One mission can fit a short play session; quitting mid-mission resumes from the last checkpoint. |
| Narrative | Serious but humane. Avoid glorifying conflict; emphasize civilian safety, accountability and cooperation. |

## Non-goals / guardrails

- Do not portray fictional operations as real-world procedure or training.
- Do not reward harm to civilians or make indiscriminate fire a viable strategy.
- Do not use cruelty, gore or real political groups as spectacle.
- Do not make rank progression a grind or sell essential capability.
- Do not claim realistic ballistics, authentic procedures or historical accuracy.
- Do not add a feature merely because a competitor includes it.

## Player promise and feedback rules

Every action receives a clear response: movement has responsive acceleration and footstep feedback; a successful interaction changes the world or objective text; an invalid interaction gives a short reason; damage has visual/audio feedback but avoids screen-filling flashes; mission failure preserves the last checkpoint; rewards state what was earned and what it unlocks.

## Feature decision filter

A candidate feature is accepted for MVP only when it strengthens at least one pillar, can be tested on target devices, fits the save model, has specified UX feedback, has an owner in the backlog, and does not make the complete campaign dependent on a future service. If it requires more than one novel system, prototype it before committing.

## Related authorities

[Project brief](00_PROJECT_BRIEF.md) · [GDD](02_MASTER_GAME_DESIGN_DOCUMENT.md) · [Mechanics](06_GAMEPLAY_MECHANICS.md) · [Originality](24_REFERENCE_ANALYSIS_AND_ORIGINALITY.md)
