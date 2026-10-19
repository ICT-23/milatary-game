# 28 — Documentation Index

**Purpose:** Entry point and navigation map for the complete specification.  
**Status:** Proposed; documentation repository completeness check pending automation  
**Last reviewed:** 2026-10-19

## Recommended reading order

1. [README.md](../README.md) — product summary and how to use this repository.
2. [00 Project Brief](00_PROJECT_BRIEF.md) — scope, assumptions and open decisions.
3. [01 Game Vision and Design Pillars](01_GAME_VISION_AND_DESIGN_PILLARS.md) — creative decision filter.
4. [02 Master Game Design Document](02_MASTER_GAME_DESIGN_DOCUMENT.md) — primary playable rules.
5. [03 Story, World and Lore](03_STORY_WORLD_AND_LORE.md) and [04 Character and Faction Bible](04_CHARACTER_AND_FACTION_BIBLE.md) — canonical narrative.
6. [05 Campaign and Mission Design](05_CAMPAIGN_AND_MISSION_DESIGN.md) and [06 Gameplay Mechanics](06_GAMEPLAY_MECHANICS.md) — mission implementation detail.
7. [07 Progression and Economy](07_PLAYER_PROGRESSION_AND_ECONOMY.md) and [08 UI/UX Specifications](08_UI_UX_AND_SCREEN_SPECIFICATIONS.md) — player feedback and screen requirements.
8. [09 Visual Art Direction](09_VISUAL_ART_DIRECTION.md), [10 Graphics/Animation/VFX](10_GRAPHICS_ANIMATION_AND_VFX.md), [11 Audio/Cinematics](11_AUDIO_AND_CINEMATIC_DIRECTION.md) — asset/production style.
9. [12 Technical Architecture](12_TECHNICAL_ARCHITECTURE.md), [13 Tech Stack and Rendering Decision](13_TECH_STACK_AND_RENDERING_DECISION.md), [14 React Native Implementation Plan](14_REACT_NATIVE_IMPLEMENTATION_PLAN.md) — runtime boundaries and tools.
10. [15 Data Model and Save System](15_DATA_MODEL_AND_SAVE_SYSTEM.md), [16 Asset Pipeline](16_ASSET_PIPELINE_AND_FILE_CONVENTIONS.md), [17 Performance](17_PERFORMANCE_AND_DEVICE_COMPATIBILITY.md) — production constraints.
11. [18 Monetization](18_MONETIZATION_AND_FAIR_PLAY.md), [19 Privacy/Security/Compliance](19_PRIVACY_SECURITY_AND_COMPLIANCE.md), [21 Google Play Release Plan](21_GOOGLE_PLAY_RELEASE_PLAN.md) — product and publication gates.
12. [20 Testing and QA](20_TESTING_AND_QUALITY_ASSURANCE.md), [22 MVP Roadmap](22_MVP_ROADMAP_AND_PHASE_GATES.md), [23 Task Backlog](23_TASK_BACKLOG_AND_ACCEPTANCE_CRITERIA.md) — execution.
13. [24 Reference Analysis and Originality](24_REFERENCE_ANALYSIS_AND_ORIGINALITY.md), [25 AI Prompt Library](25_AI_PROMPT_LIBRARY.md), [26 Risk Register and Decision Log](26_RISK_REGISTER_AND_DECISION_LOG.md), [27 Glossary](27_GLOSSARY_AND_NAMING_CONVENTIONS.md).

## File catalogue

| File | Purpose / main authority |
|---|---|
| [README.md](../README.md) | Project entry point, design decisions and navigation. |
| [00_PROJECT_BRIEF.md](00_PROJECT_BRIEF.md) | Platform, audience, product boundary, MVP constraints and assumptions. |
| [01_GAME_VISION_AND_DESIGN_PILLARS.md](01_GAME_VISION_AND_DESIGN_PILLARS.md) | Core player promise, pillars, design guardrails. |
| [02_MASTER_GAME_DESIGN_DOCUMENT.md](02_MASTER_GAME_DESIGN_DOCUMENT.md) | Camera, controls, game loop, combat, squad, saves, accessibility and core acceptance rules. |
| [03_STORY_WORLD_AND_LORE.md](03_STORY_WORLD_AND_LORE.md) | Veyra, The Long Rain, Emberwatch mystery, timeline and canonical choices. |
| [04_CHARACTER_AND_FACTION_BIBLE.md](04_CHARACTER_AND_FACTION_BIBLE.md) | Major character roles, visual identity, voice and arc. |
| [05_CAMPAIGN_AND_MISSION_DESIGN.md](05_CAMPAIGN_AND_MISSION_DESIGN.md) | Five complete MVP mission briefs plus a deferred ten-mission expansion outline. |
| [06_GAMEPLAY_MECHANICS.md](06_GAMEPLAY_MECHANICS.md) | Implementable rules, feedback, failure modes and test targets per system. |
| [07_PLAYER_PROGRESSION_AND_ECONOMY.md](07_PLAYER_PROGRESSION_AND_ECONOMY.md) | XP, ranks, skills, cosmetics, relationship and replay economy. |
| [08_UI_UX_AND_SCREEN_SPECIFICATIONS.md](08_UI_UX_AND_SCREEN_SPECIFICATIONS.md) | 28 screens, navigation, design tokens, wireframes and accessibility. |
| [09_VISUAL_ART_DIRECTION.md](09_VISUAL_ART_DIRECTION.md) | Original visual identity, color palette, environment/character look and prototype budgets. |
| [10_GRAPHICS_ANIMATION_AND_VFX.md](10_GRAPHICS_ANIMATION_AND_VFX.md) | Asset inventory, model/texture/animation/VFX standards and performance gates. |
| [11_AUDIO_AND_CINEMATIC_DIRECTION.md](11_AUDIO_AND_CINEMATIC_DIRECTION.md) | Music/sound families, mixing, subtitles and limited MVP cinematics. |
| [12_TECHNICAL_ARCHITECTURE.md](12_TECHNICAL_ARCHITECTURE.md) | Module boundaries, data flow, save ownership, offline strategy and diagnostics. |
| [13_TECH_STACK_AND_RENDERING_DECISION.md](13_TECH_STACK_AND_RENDERING_DECISION.md) | RN vs hybrid vs dedicated engine, current AI Studio workflow and research URLs. |
| [14_REACT_NATIVE_IMPLEMENTATION_PLAN.md](14_REACT_NATIVE_IMPLEMENTATION_PLAN.md) | Honest, gated options to retain RN without assuming it is a 3D engine. |
| [15_DATA_MODEL_AND_SAVE_SYSTEM.md](15_DATA_MODEL_AND_SAVE_SYSTEM.md) | Persistent schema, sample content shapes, atomic save, migration and recovery. |
| [16_ASSET_PIPELINE_AND_FILE_CONVENTIONS.md](16_ASSET_PIPELINE_AND_FILE_CONVENTIONS.md) | Repository layout, stable IDs, manifest, asset rights, import and localization workflow. |
| [17_PERFORMANCE_AND_DEVICE_COMPATIBILITY.md](17_PERFORMANCE_AND_DEVICE_COMPATIBILITY.md) | Device tiers, measurable performance goals, profiler/thermal tests and caveats. |
| [18_MONETIZATION_AND_FAIR_PLAY.md](18_MONETIZATION_AND_FAIR_PLAY.md) | No-monetization MVP rule and controlled future options. |
| [19_PRIVACY_SECURITY_AND_COMPLIANCE.md](19_PRIVACY_SECURITY_AND_COMPLIANCE.md) | Permissions, local data, privacy policy, Data safety, security and IP checks. |
| [20_TESTING_AND_QUALITY_ASSURANCE.md](20_TESTING_AND_QUALITY_ASSURANCE.md) | Concrete test catalogue, severity levels, phase gates and bug report template. |
| [21_GOOGLE_PLAY_RELEASE_PLAN.md](21_GOOGLE_PLAY_RELEASE_PLAN.md) | Current target API/testing/AAB requirements and release checklist. |
| [22_MVP_ROADMAP_AND_PHASE_GATES.md](22_MVP_ROADMAP_AND_PHASE_GATES.md) | Phase 0–5 scope, deliverables, risks and go/no-go criteria. |
| [23_TASK_BACKLOG_AND_ACCEPTANCE_CRITERIA.md](23_TASK_BACKLOG_AND_ACCEPTANCE_CRITERIA.md) | Preliminary prioritized backlog with relative estimates and test links. |
| [24_REFERENCE_ANALYSIS_AND_ORIGINALITY.md](24_REFERENCE_ANALYSIS_AND_ORIGINALITY.md) | Reference lessons, originality matrix and prohibited imitation boundaries. |
| [25_AI_PROMPT_LIBRARY.md](25_AI_PROMPT_LIBRARY.md) | Reusable prompts for concept, mission, UI, implementation, QA, performance, IP and release review. |
| [26_RISK_REGISTER_AND_DECISION_LOG.md](26_RISK_REGISTER_AND_DECISION_LOG.md) | Risks, proposed decisions, reopening criteria and change template. |
| [27_GLOSSARY_AND_NAMING_CONVENTIONS.md](27_GLOSSARY_AND_NAMING_CONVENTIONS.md) | Canonical names, stable IDs, writing rules and release placeholders. |
| [28_DOCUMENTATION_INDEX.md](28_DOCUMENTATION_INDEX.md) | This index and recommended reading path. |

## Source-of-truth ownership

- Gameplay rules: `02_MASTER_GAME_DESIGN_DOCUMENT.md` and `06_GAMEPLAY_MECHANICS.md`.
- Narrative canon and names: `03_STORY_WORLD_AND_LORE.md` and `04_CHARACTER_AND_FACTION_BIBLE.md`.
- Mission order/outcomes: `05_CAMPAIGN_AND_MISSION_DESIGN.md`.
- Technology and module ownership: `12_TECHNICAL_ARCHITECTURE.md` and `13_TECH_STACK_AND_RENDERING_DECISION.md`.
- Save schema: `15_DATA_MODEL_AND_SAVE_SYSTEM.md`.
- Asset rights/import: `16_ASSET_PIPELINE_AND_FILE_CONVENTIONS.md`.
- Test evidence: `20_TESTING_AND_QUALITY_ASSURANCE.md` plus actual test reports when they exist.
- Publication requirements: `21_GOOGLE_PLAY_RELEASE_PLAN.md`, reverified before each submission.

## Completion status and limitations

All requested Markdown paths are intended to exist in this package. Documents define the design and plan only. No code, runtime assets, test executions, engine project, signed artifact, actual Play Console form or approval is claimed. Placeholder publisher/contact/policy data and device-specific measured results remain to be resolved before release.
