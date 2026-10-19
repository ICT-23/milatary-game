# 25 — AI Prompt Library

**Purpose:** Supply focused prompts for future AI-assisted work that obey the approved documentation.  
**Status:** Proposed templates; each output requires human review  
**Last reviewed:** 2026-10-19

## How to use

Copy one prompt at a time into the selected coding/design assistant. Include the relevant Markdown documents as context where supported. Ask for a limited deliverable and explicit gaps. Never ask an agent to “build the whole game” in one pass; never treat generated code, art or technical statements as verified until reviewed and tested. Do not place credentials, private data or unlicensed proprietary assets into prompts.

## Prompt 01 — Original character concept

> You are a senior character concept designer. Design an original production-ready concept brief for **[character ID/name]** in *Operation: Emberwatch*. Treat `docs/03_STORY_WORLD_AND_LORE.md`, `docs/04_CHARACTER_AND_FACTION_BIBLE.md`, `docs/09_VISUAL_ART_DIRECTION.md`, and `docs/16_ASSET_PIPELINE_AND_FILE_CONVENTIONS.md` as authoritative. Preserve canonical age, motivation, voice, character arc and faction context. Provide front/side/back silhouette notes, materials, palette tokens, three expression beats, animation needs, mobile readability risks, model/texture budget recommendations and IP/originality checks. Do not imitate any named game's characters or use real military insignia. Distinguish confirmed canon from new proposal. Do not claim an actual image or model asset has been created.

## Prompt 02 — Original environment brief

> Create a detailed environment-art brief for **[M01 Academy / Greywater Relay / other approved location]**. Follow `docs/05_CAMPAIGN_AND_MISSION_DESIGN.md`, `docs/09_VISUAL_ART_DIRECTION.md`, `docs/10_GRAPHICS_ANIMATION_AND_VFX.md`, and `docs/16_ASSET_PIPELINE_AND_FILE_CONVENTIONS.md`. Specify player route, landmark hierarchy, safe and danger zones, interaction anchors, modular kit, lighting/time/weather, low-quality behavior, collision needs, estimated asset list and acceptance checks. Keep it buildable by a small team; reuse the shared kit. Use only fictional Veyra art direction; do not reproduce external screenshots/layouts. Mark unvalidated dimensions as blockout assumptions.

## Prompt 03 — Mission design review

> Review **[mission ID]** against `docs/02_MASTER_GAME_DESIGN_DOCUMENT.md`, `docs/03_STORY_WORLD_AND_LORE.md`, `docs/04_CHARACTER_AND_FACTION_BIBLE.md`, `docs/05_CAMPAIGN_AND_MISSION_DESIGN.md`, and `docs/06_GAMEPLAY_MECHANICS.md`. Return a sequence table with objective states, player input, response, feedback, checkpoint, failure/retry, characters, asset needs, save/story flags and tests. Verify one primary objective, at most two optional objectives, at least two activity rhythms, a non-combat beat and a link to the next mission. Flag contradictions rather than silently changing canon.

## Prompt 04 — UI screen specification / critique

> Design or critique **[screen ID]** using `docs/08_UI_UX_AND_SCREEN_SPECIFICATIONS.md`, `docs/02_MASTER_GAME_DESIGN_DOCUMENT.md`, and `docs/09_VISUAL_ART_DIRECTION.md`. Return landscape wireframe, hierarchy, primary/secondary actions, state matrix (loading/empty/error/disabled), Android back/safe-area behavior, touch target audit, accessibility labels, 35% localization expansion risk and acceptance criteria. Use solid, high-contrast surfaces and existing color/spacing tokens. Do not copy the reference game's UI or invent backend/account flows.

## Prompt 05 — Implement one engine module

> Implement only **[module name]** for the engine selected in `docs/13_TECH_STACK_AND_RENDERING_DECISION.md`. Treat `docs/12_TECHNICAL_ARCHITECTURE.md`, `docs/02_MASTER_GAME_DESIGN_DOCUMENT.md`, `docs/06_GAMEPLAY_MECHANICS.md`, and `docs/15_DATA_MODEL_AND_SAVE_SYSTEM.md` as requirements. Before coding, list inputs/outputs, state ownership, dependencies and edge cases. Keep the module small, testable and free of network/account dependencies. UI must not directly award XP or mark objectives complete. Provide unit/integration tests, exact files changed, build/test commands and known limitations. Do not alter other modules or change architecture without asking for approval; if tools cannot run, say tests were not executed.

## Prompt 06 — Save system and migration review

> Audit the save/persistence proposal against `docs/12_TECHNICAL_ARCHITECTURE.md`, `docs/15_DATA_MODEL_AND_SAVE_SYSTEM.md`, `docs/07_PLAYER_PROGRESSION_AND_ECONOMY.md`, and `docs/20_TESTING_AND_QUALITY_ASSURANCE.md`. Enumerate partial-write risks, idempotency/reward duplication risks, old-schema migration cases, corrupt active/backup behavior, process-kill timing and privacy implications. Produce test fixtures and migration tests. Do not claim atomic replacement is guaranteed unless the chosen platform/API is verified.

## Prompt 07 — Performance review

> Review **[scene/build/profile report]** using `docs/10_GRAPHICS_ANIMATION_AND_VFX.md`, `docs/17_PERFORMANCE_AND_DEVICE_COMPATIBILITY.md`, and `docs/20_TESTING_AND_QUALITY_ASSURANCE.md`. Separate measured facts from hypotheses. Identify CPU vs GPU vs memory vs thermal evidence, the most likely bottleneck, one change at a time, an A/B test plan and visual/regression risk. Never invent FPS results or claim universal device performance. Cite the exact device, OS, build and measurement method.

## Prompt 08 — QA test generation

> Generate test cases for **[feature/requirement ID]** using `docs/06_GAMEPLAY_MECHANICS.md`, `docs/15_DATA_MODEL_AND_SAVE_SYSTEM.md`, `docs/20_TESTING_AND_QUALITY_ASSURANCE.md`, and `docs/23_TASK_BACKLOG_AND_ACCEPTANCE_CRITERIA.md`. Each test must include ID, prerequisites, steps, expected result, severity, device/environment and cleanup. Include normal, boundary, interruption, low-memory, retry/replay, offline and accessibility cases where applicable. Do not mark any test passed; status remains Not Run until executed.

## Prompt 09 — Dialogue and localization edit

> Edit dialogue for **[scene/character]** while preserving canon in `docs/03_STORY_WORLD_AND_LORE.md`, `docs/04_CHARACTER_AND_FACTION_BIBLE.md`, `docs/05_CAMPAIGN_AND_MISSION_DESIGN.md`, and UI readability rules in `docs/08_UI_UX_AND_SCREEN_SPECIFICATIONS.md`. Keep voice distinctions, separate known facts from suspicions, make options concrete rather than moralized, and avoid exposition dumps. Return stable line/choice IDs, speaker, line, context, consequence tier and any localization note. Flag changes to campaign flags for approval.

## Prompt 10 — Architecture and dependency review

> Review the proposed implementation change against `docs/12_TECHNICAL_ARCHITECTURE.md`, `docs/13_TECH_STACK_AND_RENDERING_DECISION.md`, `docs/14_REACT_NATIVE_IMPLEMENTATION_PLAN.md`, and `docs/26_RISK_REGISTER_AND_DECISION_LOG.md`. Explain whether the change alters runtime boundaries, lifecycle, save ownership, dependency compatibility, Android permissions, licensing or performance. Require official current sources for version/policy claims. Return options, risks, a minimal experiment and decision log update; do not add dependencies without a reason.

## Prompt 11 — Store listing and policy review

> Review the release package against `docs/19_PRIVACY_SECURITY_AND_COMPLIANCE.md`, `docs/21_GOOGLE_PLAY_RELEASE_PLAN.md`, `docs/24_REFERENCE_ANALYSIS_AND_ORIGINALITY.md`, and `docs/17_PERFORMANCE_AND_DEVICE_COMPATIBILITY.md`. Check that marketing claims match actual build features, no rating or approval is promised, privacy and Data safety match SDK/manifest behavior, assets are owned/licensed, target API is current, AAB/signing is correct and title cleared. Browse current official policy pages and record check date. Flag anything unverified; do not submit or publish on assumptions.

## Prompt 12 — Cross-document consistency audit

> Audit every file listed in `docs/28_DOCUMENTATION_INDEX.md`. Compare canonical character/missions/story flags, MVP scope, orientation, rank values, technology choice, save behavior, permissions, test IDs and release dates. Report contradictions with file/heading references and recommended minimal fixes. Check relative links and missing sections. Do not rewrite a source-of-truth decision silently; propose changes and list all dependent docs that would need updates.

## Prompt 13 — Art/IP rights audit

> Inspect the asset manifest and related records using `docs/09_VISUAL_ART_DIRECTION.md`, `docs/10_GRAPHICS_ANIMATION_AND_VFX.md`, `docs/16_ASSET_PIPELINE_AND_FILE_CONVENTIONS.md`, and `docs/24_REFERENCE_ANALYSIS_AND_ORIGINALITY.md`. Find assets with missing source/license, copied or reference-derived material, generated logos/text, unclear model rights, missing attributions, unsupported use rights or visual similarities that warrant human review. Label unverifiable items as unresolved, not approved.

## Prompt 14 — Build/release audit

> Run or review the actual release steps in `docs/20_TESTING_AND_QUALITY_ASSURANCE.md` and `docs/21_GOOGLE_PLAY_RELEASE_PLAN.md`. Return exact tool versions, build command, artifact type/hash, target API, package ID, permission list, tests executed and failures. Do not claim Play approval or tester requirements satisfied without Console evidence. Never print signing secrets. Stop if the app identity, privacy URL or current policy requirement is unresolved.

## Output contract for every prompt

Every AI-assisted task should return: **(1)** source documents used, **(2)** changed files, **(3)** tests/commands actually executed, **(4)** evidence or citations for external claims, **(5)** unverified assumptions, **(6)** risks and follow-up work, **(7)** whether it changed any accepted requirements. Review the result in version control before merging.
