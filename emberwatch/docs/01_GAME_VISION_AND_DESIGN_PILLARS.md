# 01 — Game Vision and Design Pillars

**Purpose:** Define the experience and the rules used to resolve design disagreements. **Status:** Proposed · **Last reviewed:** 2026-10-18

## 1. Vision statement
*"You are not the chosen one. You are the person who stayed up, learned the work, and kept the lights on when it mattered."* Players grow from a nervous recruit into a trusted specialist whose decisions help a region decide whether to hide or reveal the truth about its own failure.

## 2. Five experiences
| # | Experience | How it appears |
|---|---|---|
| 1 | Academy | Playable training that teaches movement, aiming, stealth, rescue, coordination |
| 2 | Story | Squad relationships, rival arc, Emberwatch mystery |
| 3 | Operations | Varied mission types (see [05](05_CAMPAIGN_AND_MISSION_DESIGN.md)) |
| 4 | Progression | XP, ranks, skills, equipment, trust |
| 5 | Consequences | Tracked flags and trust values change dialogue, some outcomes, ending |

## 3. Design pillars
| ID | Pillar | Means | Does NOT mean |
|---|---|---|---|
| PIL-1 | **Earned Rank** | Promotions follow demonstrated skill; ranks unlock responsibilities (calling squad orders), not just numbers | Pay-to-skip |
| PIL-2 | **Varied Rhythm** | No two consecutive missions share the same dominant verb | Every mission is shooting |
| PIL-3 | **People over Plot** | Each mission advances at least one relationship or character arc | Long exposition |
| PIL-4 | **Choices that Echo** | Few, clear decisions with visible follow-up in next 1–2 missions | Fully branching campaign |
| PIL-5 | **Readable on a Phone** | One-thumb-per-function controls, large silhouettes, minimal HUD | Dense menus |

**Tie-break rule:** If two features conflict, choose the one that better serves PIL-5, then PIL-2, then PIL-3.

## 4. Target feeling by phase
| Phase | Player should feel |
|---|---|
| Prologue | Urgent, capable-despite-fear |
| Ch.1 Academy | Awkward → competent, belonging |
| Ch.2 Harlow Reach | Responsible, uneasy |
| Ch.3 Fracture | Doubt, divided loyalty |
| Ch.4 Summit | Weight of choice, earned resolve |

## 5. Differentiators from the genre (summary; detail in [24](24_REFERENCE_ANALYSIS_AND_ORIGINALITY.md))
Humanitarian-conflict framing; a mystery rooted in infrastructure failure rather than a villain commander; mission verbs beyond shooting (beacon repair, flood rescue, signal relay); squad trust as a mechanic.

## 6. Constraints
Small team; offline; no backend; Android-first; no required purchase. Consequence: pre-rendered sprites and reusable mission templates (see [13](13_TECH_STACK_AND_RENDERING_DECISION.md)).

## 7. Success metrics (targets, not guarantees)
| Metric | Target | Status |
|---|---|---|
| Tutorial completion | ≥ 85% in playtests | Needs Validation |
| Prologue-to-M-101 retention in closed test | ≥ 70% | Needs Validation |
| Crash-free sessions | ≥ 99.5% | Needs Validation |
