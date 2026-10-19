# 25 — AI Prompt Library

**Purpose:** Reusable, focused prompts for AI agents. **Status:** Proposed · **Last reviewed:** 2026-10-18
Rule for every prompt: *read the listed files first; do not contradict them; if a conflict is found, stop and report it; do not invent names, IDs or numbers; do not include secrets.*
Replace `{{…}}` placeholders.

## P-A Create an original character
**Read:** [03](03_STORY_WORLD_AND_LORE.md), [04](04_CHARACTER_AND_FACTION_BIBLE.md), [09](09_VISUAL_ART_DIRECTION.md), [24](24_REFERENCE_ANALYSIS_AND_ORIGINALITY.md).
```
Create a new character for OPERATION: EMBERWATCH: role {{role}}, appears in {{missions}}.
Follow the character sheet template in 04 (appearance/silhouette, personality, strengths/weaknesses, background, motivation, relationships, voice, animation/expression, gameplay purpose, arc, missions).
Use the next free CH-ID. Adult age (>=19). Must pass the black-silhouette test and not resemble any existing game character. Output Markdown ready to append to 04 and list any updates needed in 05.
```

## P-B Design an environment
**Read:** [09](09_VISUAL_ART_DIRECTION.md), [10](10_GRAPHICS_ANIMATION_AND_VFX.md), [16](16_ASSET_PIPELINE_AND_FILE_CONVENTIONS.md), [05](05_CAMPAIGN_AND_MISSION_DESIGN.md).
```
Write a one-page environment brief for {{location}}: mood, palette (use only palette tokens), key landmarks, gameplay layout (routes, cover, sightlines), lighting (one key + one warm accent), parallax layer list, prop list with asset IDs, and per-profile (Low/Med/High) differences. Include a licensing/source note per reference image.
```

## P-C Build a mission
**Read:** [05](05_CAMPAIGN_AND_MISSION_DESIGN.md), [06](06_GAMEPLAY_MECHANICS.md), [07](07_PLAYER_PROGRESSION_AND_ECONOMY.md), [15](15_DATA_MODEL_AND_SAVE_SYSTEM.md).
```
Produce the full spec and JSON config for mission {{ID}} using the fields in 05 §4 and the schema in 15. Introduce at most one new mechanic. Define checkpoints, failure conditions, rewards consistent with 07, flags written, and tests (TC-style). Do not change canon in 03/04.
```

## P-D Refine a UI screen
**Read:** [08](08_UI_UX_AND_SCREEN_SPECIFICATIONS.md), [09](09_VISUAL_ART_DIRECTION.md), [06](06_GAMEPLAY_MECHANICS.md).
```
Refine screen #{{n}} {{name}}: provide ASCII wireframe, component tree, states (loading/error/empty), safe-area handling, back behaviour, accessibility labels, tokens used, and acceptance criteria. Landscape only. Min touch target 48 dp.
```

## P-E Implement a module
**Read:** [12](12_TECHNICAL_ARCHITECTURE.md), [14](14_REACT_NATIVE_IMPLEMENTATION_PLAN.md), [15](15_DATA_MODEL_AND_SAVE_SYSTEM.md), [23](23_TASK_BACKLOG_AND_ACCEPTANCE_CRITERIA.md).
```
Implement {{TASK-ID}} in TypeScript strict. Respect module boundaries (game-core has no React/RN imports), fixed 30 Hz sim, no per-frame allocations. Include unit tests that cover each acceptance criterion in the backlog row. Do not add dependencies without listing licence and data behaviour. Stop if ADR-001 is not Approved.
```

## P-F Test a feature
**Read:** [20](20_TESTING_AND_QUALITY_ASSURANCE.md), [06](06_GAMEPLAY_MECHANICS.md), the relevant mission in [05](05_CAMPAIGN_AND_MISSION_DESIGN.md).
```
Write test cases (ID, prerequisites, actions, expected, severity) and automated tests for {{feature}}. Do not state results; leave Result columns "Not run".
```

## P-G Review performance
**Read:** [17](17_PERFORMANCE_AND_DEVICE_COMPATIBILITY.md), [13](13_TECH_STACK_AND_RENDERING_DECISION.md), [12](12_TECHNICAL_ARCHITECTURE.md).
```
Given this profiler output {{data}}, device {{device}} and build {{type}}, compare against budgets in 17, identify the top 3 bottlenecks, propose fixes ordered by effort/impact, and say which findings are measured vs inferred.
```

## P-H Write dialogue/scene
**Read:** [03](03_STORY_WORLD_AND_LORE.md), [04](04_CHARACTER_AND_FACTION_BIBLE.md), [11](11_AUDIO_AND_CINEMATIC_DIRECTION.md).
```
Write scene {{id}} for {{characters}}: lines <=14 words typical, voice per 04, include choice classes (Cosmetic/Local/Campaign) and flags set. Max 3 options. No real-world references.
```

## P-I Release preparation
**Read:** [21](21_GOOGLE_PLAY_RELEASE_PLAN.md), [19](19_PRIVACY_SECURITY_AND_COMPLIANCE.md).
```
Re-verify each REL item against current official Google Play/Android documentation, cite URLs and dates, and mark anything unverifiable as Needs Validation. Do not claim approval.
```

## P-J Originality review
**Read:** [24](24_REFERENCE_ANALYSIS_AND_ORIGINALITY.md), [09](09_VISUAL_ART_DIRECTION.md).
```
Review {{asset/text}} against ORG-001…006 and the reference log; list any resemblance risks and required changes.
```
