# 23 — Task Backlog and Acceptance Criteria

**Purpose:** Prioritised backlog. **Status:** Proposed · **Last reviewed:** 2026-10-18
Estimates are **preliminary ideal person-days (ipd)**, ±50%, not commitments. Priority P0 = blocking, P1 = MVP, P2 = nice.

| ID | Task | Phase | Pri | Est (ipd) | Depends on | Acceptance / test |
|---|---|---|---|---|---|---|
| TASK-001 | Founder review of docs; resolve open questions | 0 | P0 | 2 | — | DEC log entries |
| TASK-002 | Verify Expo/RN/Skia/Reanimated compatibility matrix | 0 | P0 | 1 | 001 | Matrix in [13](13_TECH_STACK_AND_RENDERING_DECISION.md) |
| TASK-003 | EXP-01 render stress scene | 0 | P0 | 3 | 002 | FPS recorded on device |
| TASK-004 | EXP-02 input latency | 0 | P0 | 2 | 003 | ≤ 100 ms or fail |
| TASK-005 | EXP-03 memory/atlas test | 0 | P0 | 2 | 003 | Memory recorded |
| TASK-006 | EXP-04 audio latency/voices | 0 | P1 | 2 | 002 | TC-019 base |
| TASK-007 | EXP-05 atomic save kill test | 0 | P0 | 2 | 002 | SAVE-001 |
| TASK-008 | EXP-06 Godot spike (fallback baseline) | 0 | P1 | 4 | 001 | Comparative notes |
| TASK-009 | Decision gate: approve ADR-001 | 0 | P0 | 0.5 | 003–008 | DEC-001 |
| TASK-010 | Scaffold repo, CI, lint, landscape lock | 1 | P0 | 2 | 009 | TC-001 |
| TASK-011 | Fixed-step loop + determinism harness | 1 | P0 | 3 | 010 | Unit tests |
| TASK-012 | Movement + camera | 1 | P0 | 3 | 011 | MECH-001 AC, TC-003 |
| TASK-013 | HUD base + virtual stick | 1 | P0 | 3 | 012 | UI-002/003 |
| TASK-014 | Objective system | 1 | P0 | 2 | 011 | Unit tests |
| TASK-015 | Interact/carry | 1 | P0 | 3 | 014 | TC-006 |
| TASK-016 | Save service v1 | 1 | P0 | 4 | 007 | SAVE-001…004 |
| TASK-020 | P-01 greybox → polish | 2 | P1 | 8 | 015 | Playtest |
| TASK-021 | Aim/fire/reload + aim assist | 2 | P1 | 5 | 012 | TC-004 |
| TASK-022 | Cover system | 2 | P1 | 4 | 021 | AC-003 |
| TASK-023 | Enemy AI FSM + detection | 2 | P1 | 6 | 021 | AC-004 |
| TASK-024 | Dialogue system + choices | 2 | P1 | 5 | 016 | TC-009 |
| TASK-025 | Audio service + ducking | 2 | P1 | 4 | 006 | AUD-001/002 |
| TASK-026 | Asset loader + manifest validator | 2 | P1 | 3 | 010 | AST-001 |
| TASK-027 | Art: hero character + Ridge env (slice) | 2 | P1 | 15 | 026 | ART-001/002 |
| TASK-028 | Audio: music cue + SFX set (slice) | 2 | P1 | 8 | — | Mix review |
| TASK-029 | M-101 and M-102 content | 2 | P1 | 6 | 020, 021 | CAM-001 partial |
| TASK-030 | Debrief + XP/rank UI | 2 | P1 | 4 | 016 | PROG-001 |
| TASK-031 | Cutscene #1 | 2 | P1 | 4 | 024 | Review |
| TASK-032 | Slice playtest + perf report | 2 | P0 | 4 | 029–031 | Gate 2 |
| TASK-040 | Stealth missions (M-103) content | 3 | P1 | 5 | 023 | TC-005 |
| TASK-041 | Squad AI + orders | 3 | P1 | 8 | 023 | TC-007 |
| TASK-042 | M-104/M-105 content | 3 | P1 | 6 | 041 | CAM-001 |
| TASK-043 | Beacon repair minigame | 3 | P1 | 4 | 013 | TC-008 |
| TASK-044 | Escort system (M-201) | 3 | P1 | 6 | 041 | CAM-001 |
| TASK-045 | M-202/M-203 content | 3 | P1 | 8 | 043, 015 | CAM-001 |
| TASK-046 | M-301 content + parley choice | 3 | P1 | 8 | 024 | TC-009/010 |
| TASK-047 | M-401/M-402 + ending logic | 3 | P1 | 8 | 046 | CAM-002 |
| TASK-048 | Skills, loadout, trust systems | 3 | P1 | 8 | 030 | PROG-002/003 |
| TASK-049 | Codex, campaign map, squad profiles | 3 | P1 | 6 | 048 | UI-001 |
| TASK-050 | Settings, accessibility, audio screens | 3 | P1 | 5 | 025 | TC-019/020 |
| TASK-051 | Art: remaining characters/environments | 3 | P1 | 60 | 027 | GFX-001 |
| TASK-052 | Audio: full music/SFX/voice | 3 | P1 | 25 | 028 | AUD-003 |
| TASK-053 | Writing: full script and Codex | 3 | P1 | 15 | — | Review |
| TASK-054 | Cutscenes #2–5 | 3 | P2 | 12 | 047 | Review |
| TASK-060 | Feature freeze + content lock | 3 | P0 | 1 | all 04x | Gate 3 |
| TASK-070 | Golden-path replays for 12 missions | 4 | P0 | 6 | 060 | CAM-001 automated |
| TASK-071 | Device matrix testing | 4 | P0 | 8 | 060 | Section 5 of 17 filled |
| TASK-072 | Perf optimisation pass | 4 | P0 | 8 | 071 | PERF-001…004 |
| TASK-073 | Save/interrupt/low-mem testing | 4 | P0 | 4 | 060 | TC-011…016 |
| TASK-074 | Accessibility audit | 4 | P1 | 3 | 060 | Checklist |
| TASK-075 | Bug fixing | 4 | P0 | 15 | 070–074 | QG-1…7 |
| TASK-080 | Release build config, signing setup | 5 | P0 | 3 | 075 | TC-022 |
| TASK-081 | Permissions/licence/privacy audit | 5 | P0 | 2 | 075 | PRIV-001…004 |
| TASK-082 | Privacy policy + Data Safety + rating | 5 | P0 | 2 | 081 | REL-004…006 |
| TASK-083 | Store assets and listing | 5 | P1 | 6 | — | REL-009 |
| TASK-084 | Trademark/name check | 5 | P0 | 1 | — | REL-010 |
| TASK-085 | Closed test run + fixes | 5 | P0 | 10 | 080 | QG-3 |
| TASK-086 | Policy re-verification and submission | 5 | P0 | 2 | 082–085 | Checklist signed |
