# 00 — Project Brief

**Purpose:** Define the product boundary and decision baseline.  
**Status:** Proposed  
**Last reviewed:** 2026-10-19

## One-sentence pitch

A rookie from a storm-battered coastal province joins Meridian Defence Academy, discovers that a supposedly destroyed emergency-warning network may have been deliberately silenced, and must earn squad trust while restoring civilian evacuation routes during a fragile ceasefire.

## Product definition

| Attribute | Definition |
|---|---|
| Working title | OPERATION: EMBERWATCH — check trademarks, game-store naming and domains before public branding. |
| Genre | Story-driven, mission-based third-person action-adventure with training, light stealth, rescue and squad support. |
| Platform | Android phones first; landscape orientation. Tablet layout may be supported after phone UX is stable. |
| Players | One player; no PvP or online co-op. |
| Audience | Older teens and adults who enjoy approachable action, progression and character-led military fiction. Final age rating depends on the shipped build and rating questionnaire. |
| Session | 6–12 minutes for a standard mission; 2–4 minutes for a training drill; longer only for story finale. |
| Connectivity | Offline after installation for all core play. Network use is not required by the MVP. |
| Core promise | Clear objectives, responsive controls, meaningful character choices and a complete story arc. |
| Not the goal | Real-world combat instruction, open-world simulation, multiplayer, realistic weapons simulation, political propaganda, or a service dependent on accounts. |

## MVP boundaries

**Must be built before the first store candidate:** boot/title flow; local profile/save; onboarding; one academy training mission; one field mission in a compact environment; basic movement, camera, interaction and fictionalized combat; one rescue/escort objective; mission completion/failure/retry; dialogue choice affecting a visible relationship/story flag; pause/settings; basic subtitles and audio sliders; quality settings; credits/legal/privacy surface; Android release build; test evidence on physical devices.

**MVP campaign target, conditional on the vertical slice:** five missions forming a complete beginning, middle and end. The vertical slice is the first validation gate; the team must not begin producing all five at once before controls, performance, art scale and save flow have been proven.

**Explicitly out of MVP:** multiplayer, cloud saves, accounts, live operations, procedural campaign, open world, vehicles requiring bespoke physics, destructible environments, fully branching chapters, voiced dialogue for every line, paid currency, ads, battle pass, online AI features and a large cinematic pipeline.

## Success measures (targets, not results)

- A new player reaches movement and first objective without external instructions in under 90 seconds.
- At least 90% of moderated first-time playtesters can identify their current objective without facilitator help.
- On the nominated mid-tier reference device, gameplay aims for 30 FPS sustained; 60 FPS is a stretch profile, not a universal promise.
- Main menu-to-playable mission target: under 12 seconds after first boot and cache warm-up on the reference device; cold first launch measured separately.
- No known save corruption, softlock, blocker crash or mission-completion blocker in a release candidate.
- All supported-device claims are based on an explicit matrix, not assumptions.

## Constraints and assumptions

1. Team size, production budget, art outsourcing capacity and timeline are unknown; estimates are preliminary.
2. All names, factions, locations and world history in this package are fictional and must remain consistent.
3. Unity is the working recommendation because the target experience is real-time 3D. Validate licensing, version, Android export pipeline and team skill before commitment.
4. Google Play requirements can change; the release checklist records requirements checked on 2026-10-19 and requires re-verification before submission.
5. The reference game is used only to understand high-level genre appeal. No reference assets, story, UI, mission sequence or distinctive character design may be reused.

## Decision owners and open questions

| Question | Default for planning | Gate to resolve |
|---|---|---|
| Unity or Godot? | Unity is preferred for the current art/animation ambition; Godot is a viable cost/licensing alternative if team expertise and prototype results support it. | Phase 0 renderer and Android package spike. |
| React Native? | Not in the live 3D runtime for MVP. | Revisit only if there is a specific shell/companion requirement and a proven integration prototype. |
| Monetization? | No ads or purchases in MVP. | Product review after user testing, not before core quality. |
| Rating? | Aim to avoid graphic injury and explicit gore; do not claim a rating before questionnaire. | Final content-lock review and IARC questionnaire. |
| Name? | Working title only. | Trademark/store search and legal review. |

## Required reading

[Vision](01_GAME_VISION_AND_DESIGN_PILLARS.md) · [GDD](02_MASTER_GAME_DESIGN_DOCUMENT.md) · [Story](03_STORY_WORLD_AND_LORE.md) · [Technical decision](13_TECH_STACK_AND_RENDERING_DECISION.md) · [Roadmap](22_MVP_ROADMAP_AND_PHASE_GATES.md)
