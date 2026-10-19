# 23 — Task Backlog and Acceptance Criteria

**Purpose:** Convert documentation into prioritized, testable work.  
**Status:** Preliminary backlog; estimates are relative and not commitments  
**Last reviewed:** 2026-10-19

## Estimation scale

Use preliminary effort points for relative planning, not calendar days: **1** tiny, **2** small, **3** medium, **5** large, **8** very large/needs decomposition. A task sized 8 should normally be split after technical discovery. Owner and sprint/date remain unassigned because team capacity is unknown.

## Priority meanings

- **P0:** blocks prototype or release safety; do first.
- **P1:** required for vertical slice or MVP campaign.
- **P2:** valuable, but can follow after P0/P1.
- **P3:** future/optional/deferred.

## Backlog

| ID | Task | Priority | Estimate | Dependencies | Acceptance / tests |
|---|---|---|---:|---|---|
| OW-001 | Approve engine/toolchain via device spike | P0 | 5 | None | Documented Unity/Godot comparison, input demo, signed Android test build, performance baseline. |
| OW-002 | Create repository, branch policy and asset manifest | P0 | 2 | OW-001 can run in parallel with docs | Clean checkout builds; no secrets; manifest IDs validate. |
| OW-003 | Implement boot/title/menu skeleton | P0 | 3 | OW-001 | QA-BOOT-001 and QA-NAV-001 pass. |
| OW-004 | Touch movement + follow camera | P0 | 5 | OW-001 | QA-IN-001/002 pass on low/mid physical devices. |
| OW-005 | Implement interaction and objective state machine | P0 | 5 | OW-004 | QA-GAME-003, objective idempotency tests pass. |
| OW-006 | Implement checkpoint, pause/resume and local save | P0 | 5 | OW-003, OW-005 | QA-SAVE-001/004 and QA-LIFE-001 pass. |
| OW-007 | Build M01 academy graybox | P0 | 5 | OW-004/005 | Primary task playable end-to-end; no softlock. |
| OW-008 | Design touch HUD and control preferences | P1 | 3 | OW-004 | UI safe-area, handedness, scaling and accessibility tests pass. |
| OW-009 | Add training score and mission debrief | P1 | 3 | OW-005/007 | Retry/score behavior and one-time reward tested. |
| OW-010 | Add dialogue/choice framework | P1 | 5 | OW-005/006 | QA-CHOICE-001 branch flag persists after restart. |
| OW-011 | Create player + core NPC rigs/animations | P1 | 8 | OW-001, art bible | Rig/LOD/import checks pass; actual device frame-time measured. |
| OW-012 | Produce M01 art/audio vertical slice | P1 | 8 | OW-007/008/011 | M01 meets vertical slice gate; all asset rights recorded. |
| OW-013 | Add companion Follow/Hold/Assist | P1 | 5 | OW-005/011 | QA-GAME-002; companion cannot block required interactions. |
| OW-014 | Add M02 relay repair + exploration | P1 | 5 | OW-012/013 | Mission completes, retry works offline, M02 checkpoint tests pass. |
| OW-015 | Add M03 evidence/stealth system | P1 | 5 | OW-014, MECH-005 | Suspicion understood; evidence flags and CHOICE_02 persist. |
| OW-016 | Add M04 escort and contained combat | P1 | 8 | OW-013, combat baseline | QA-GAME-004/006; civilian target protection passes. |
| OW-017 | Add M05 climax and ending | P1 | 8 | OW-015/016 | Both CHOICE_03 options reach same complete outcome with expected epilogue changes. |
| OW-018 | Implement XP/rank/skill/reward ledger | P1 | 5 | OW-006/009 | QA-SAVE-004; no duplicate XP on retry/replay. |
| OW-019 | Implement codex, character pages and mission board | P2 | 3 | OW-014–018 | Locked/empty states work; data is locally available. |
| OW-020 | Audio buses, subtitles and reduced motion | P1 | 5 | OW-008, OW-012 | QA-AUD-001 and QA-ACC-001 pass. |
| OW-021 | Quality profiles and graphics settings | P1 | 5 | OW-012 | QA-PERF-003; gameplay readability consistent. |
| OW-022 | Save migration and corruption recovery | P0 | 5 | OW-006, OW-018 | All fixture schemas and corrupted-save cases verified. |
| OW-023 | Device matrix and performance profiling | P0 | 5 | OW-012 | Reports for low/mid/high candidates and 10-minute thermal run. |
| OW-024 | Privacy/manifest/SDK audit | P0 | 3 | Build pipeline, final dependency list | QA-POL-001 passes; policy and binary match. |
| OW-025 | Trademark/title/package ID review | P0 | 2 | Project owner/publisher identity | Written search result and approved identity decision. |
| OW-026 | Store listing and authentic screenshots | P1 | 3 | Content lock, title approval | Assets comply with current Console specs and match shipping build. |
| OW-027 | AAB signing + clean release build | P0 | 3 | OW-023/024 | QA-REL-001 passes; no credentials in repo/history. |
| OW-028 | Internal/closed testing and feedback loop | P0 | 3 | OW-027, Play Console account | Track builds, tester feedback and fixes documented; qualifying account requirements met. |
| OW-029 | Release candidate regression | P0 | 5 | OW-014–028 | No S0/S1; offline campaign, choices, saves, lifecycle, privacy and performance pass. |
| OW-030 | Optional RN/2.5D prototype comparison | P3 | 5 | OW-001 only if still a serious preference | Same graybox tested and measured; route explicitly accepted or closed. |
| OW-031 | M06–M10 expansion concepts | P3 | 8 | MVP shipped and user feedback | Updated scope case; no code/content commitment until approved. |

## Suggested critical path

`OW-001 → OW-004 → OW-005 → OW-006 → OW-007 → OW-012 → OW-014/015/016 → OW-017 → OW-022/023/024/027 → OW-029`

Parallel tracks can include UI design tokens, story/dialogue authoring and asset licensing, but avoid locking high-detail environments before camera, scale and performance are proven.

## Definition of done (all tasks)

- Acceptance criteria reproduced and actual results recorded.
- Code/content is reviewed and version-controlled.
- No unlicensed asset or secret added.
- Automated tests added or manual regression case linked.
- Requirements and dependent docs updated if behavior changed.
- Error, loading, empty and accessibility states considered.
- Clean build passes for the affected target.
- Any known limitation is written in the issue/decision log.

## Backlog maintenance

After each prototype/phase review, re-estimate with evidence. Do not silently reduce scope by omitting safety, save, offline, accessibility or release requirements. If a requirement is deferred, update the GDD, mission plan, UI, tests and release claims together.
