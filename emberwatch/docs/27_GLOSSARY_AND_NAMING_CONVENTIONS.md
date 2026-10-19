# 27 — Glossary and Naming Conventions

**Purpose:** Canonical terms. **Status:** Proposed · **Last reviewed:** 2026-10-18

## 1. Glossary
| Term | Meaning |
|---|---|
| Emberwatch | Network of 14 hilltop beacon towers |
| Tower 7 | The tower that went dark in the Great Harrow Flood |
| Halvorn Ridge Academy / the Ridge | CDC training academy |
| Lantern | Five-person squad; player's is Third Lantern |
| CDC | Corvane Defence Corps |
| MPS | Ministry of Provincial Security |
| Sable Compact | Breakaway Harlow Reach coalition |
| Harlow Reach | Border province below Harrow Dam |
| XP | Service XP; drives rank |
| MP | Merit Points; spent on skills |
| RC | Requisition Credits; spent on equipment |
| Trust | 0–100 meter per squadmate |
| Flag | Persisted story variable (prefix `F_`) |
| Evidence token | Item collected for endings (prefix `E_`) |
| Sprite atlas | Packed image of animation frames |
| Quality profile | Low/Medium/High rendering settings |
| EXP | Technical experiment |
| ADR | Architecture decision record |

## 2. Rank names (exact)
Recruit (R1) · Cadet (R2) · Corporal (R3) · Sergeant (R4) · Warrant Specialist (R5) · Lieutenant (R6)

## 3. ID conventions
| Prefix | Meaning | Example |
|---|---|---|
| P-nn | Prologue | P-01 |
| M-nnn | Mission (chapter digit first) | M-202 |
| CH-nn | Character | CH-02 |
| MECH-nnn | Mechanic | MECH-004 |
| GDD/PROG/UI/ART/GFX/AUD/TECH/SAVE/AST/PERF/PRIV/MON/CAM/FAIR/ORG | Requirement families | — |
| TC-nnn | Test case | TC-011 |
| QG-n | Quality gate | QG-3 |
| TASK-nnn | Backlog item | TASK-016 |
| EXP-nn | Experiment | EXP-01 |
| DEC-nnn / R-nn | Decision / Risk | — |
| REL-nnn | Release checklist item | REL-004 |

## 4. Status labels
`Proposed`, `Approved`, `Needs Validation`, `Deferred`. Scope labels: MVP, Post-MVP, Optional.

## 5. Code naming
TypeScript: `camelCase` variables/functions, `PascalCase` types/components, `SCREAMING_SNAKE_CASE` constants; files `kebab-case.ts`; test files `*.test.ts`. Content IDs `snake_case` in JSON keys but canonical display IDs preserved as strings (`"M-202"`).
Asset naming: see [16](16_ASSET_PIPELINE_AND_FILE_CONVENTIONS.md).
