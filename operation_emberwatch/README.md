# OPERATION: EMBERWATCH

**Purpose:** Entry point for the original story-driven military action game documentation.  
**Status:** Proposed design baseline; no implementation claimed  
**Last reviewed:** 2026-10-19

## Project at a glance

*Operation: Emberwatch* is a single-player, landscape Android action-adventure in which a recruit at the fictional Meridian Defence Academy grows into a reliable field specialist. Training, squad trust, rescue-focused missions, exploration, restrained combat and consequential leadership choices form one connected narrative. The story takes place in the fictional coastal republic of Veyra after a catastrophic storm exposes failures in a regional emergency-warning network called Emberwatch.

The goal is a polished, original, modestly scoped game—not an open world, a multiplayer service or a military training simulator. The first quality milestone is one 8–12 minute vertical-slice mission with responsive controls and a complete gameplay loop. The proposed first shippable campaign is five missions with a self-contained ending. A ten-mission campaign is the longer-term vision and must not be treated as an MVP commitment.

## What this repository contains

This repository is the design and delivery specification. Begin with the [documentation index](docs/28_DOCUMENTATION_INDEX.md), then read the [project brief](docs/00_PROJECT_BRIEF.md), [vision](docs/01_GAME_VISION_AND_DESIGN_PILLARS.md), [GDD](docs/02_MASTER_GAME_DESIGN_DOCUMENT.md), and [technical decision](docs/13_TECH_STACK_AND_RENDERING_DECISION.md). Requirements and scope are tracked in the [backlog](docs/23_TASK_BACKLOG_AND_ACCEPTANCE_CRITERIA.md).

## Core decisions

- **Identity:** fictional republic of Veyra; Meridian Defence Academy; recruit Mara Venn; the Emberwatch civil-warning relay mystery.
- **Format:** single-player, offline-first, third-person 3D, landscape, mission-based, touch-first Android controls.
- **Recommended engine:** Unity for the entire first release, subject to a short technical spike. This is the lowest-risk fit for real-time 3D animation, camera, input, lighting and Android profiling. React Native remains appropriate for companion or content-heavy interfaces, but should not be inserted into the game runtime without a concrete need.
- **MVP:** one polished vertical slice first, then a self-contained five-mission campaign only if the slice meets quality gates.
- **Business model:** paid-free core; no forced ads or in-app purchases in the MVP; no account or backend required.
- **Branding:** *OPERATION: EMBERWATCH* is a working title only. Trademark, store-name, domain and package-name checks are release gates.

## How to use these documents

1. Treat [the GDD](docs/02_MASTER_GAME_DESIGN_DOCUMENT.md) as the main gameplay authority.
2. Treat [the story bible](docs/03_STORY_WORLD_AND_LORE.md) and [character bible](docs/04_CHARACTER_AND_FACTION_BIBLE.md) as the authorities for narrative names and facts.
3. Treat [mission design](docs/05_CAMPAIGN_AND_MISSION_DESIGN.md) as the authority for mission sequencing and outcomes.
4. Treat [technical architecture](docs/12_TECHNICAL_ARCHITECTURE.md) and [technology decision](docs/13_TECH_STACK_AND_RENDERING_DECISION.md) as the authorities for implementation boundaries.
5. Use requirement IDs and acceptance criteria in the [backlog](docs/23_TASK_BACKLOG_AND_ACCEPTANCE_CRITERIA.md) and [QA plan](docs/20_TESTING_AND_QUALITY_ASSURANCE.md) to define done.
6. If implementation or prototype evidence contradicts an assumption, log the result in [the decision log](docs/26_RISK_REGISTER_AND_DECISION_LOG.md), update the authority document, then update dependent docs.

## Status terminology

- **Proposed:** recommended design, not yet validated in a build.
- **Approved:** explicitly accepted by the project owner after review.
- **Needs Validation:** requires a prototype, device test, policy check or legal review.
- **Deferred:** intentionally excluded from the current release scope.

No game source code, artwork, animations, audio, store listing, signed build or Play Console submission is included in this documentation package. Asset references are production instructions, not claims that assets already exist.
