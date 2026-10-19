# 07 — Player Progression and Economy

**Purpose:** Authoritative numbers for XP, ranks, skills, equipment, trust and rewards. **Status:** Proposed (all values `Needs Validation` via playtest) · **Last reviewed:** 2026-10-18

## 1. Currencies (only three; all earned in play)
| Name | Code | Source | Spend |
|---|---|---|---|
| Service XP | XP | Missions, objectives, medals | Drives rank |
| Merit Points | MP | 1 per promotion + 1 at M-104 | Skills |
| Requisition Credits | RC | Missions, optional objectives | Equipment/outfits |
No premium currency, no timers, no energy system. See [18](18_MONETIZATION_AND_FAIR_PLAY.md).

## 2. Rank ladder
| Rank | ID | XP threshold | Earned around | Unlocks |
|---|---|---|---|---|
| Recruit | R1 | 0 | Start | Basic orders |
| Cadet | R2 | 150 | After P-01 | Loadout screen |
| Corporal | R3 | 900 | After M-103 | Squad ability slot 1 |
| Sergeant | R4 | 2000 | After M-201 | Squad order wheel full set |
| Warrant Specialist | R5 | 3500 | After M-203 | Gadget slot 2 |
| Lieutenant | R6 | 5000 | After M-401 | Epilogue title, cosmetic insignia |
Check: base mission XP total = 5,150 (see table §3); optional objectives add ≈ 20–30% (estimate). Thresholds chosen so each rank is reachable on the main path.

## 3. XP sources
| Mission | Base XP | Optional bonus (max) |
|---|---|---|
| P-01 | 150 | +30 |
| M-101 | 250 | +40 |
| M-102 | 300 | +50 |
| M-103 | 350 | +60 |
| M-104 | 400 | +60 |
| M-105 | 450 | +60 |
| M-201 | 500 | +80 |
| M-202 | 550 | +80 |
| M-203 | 600 | +100 |
| M-301 | 700 | +100 |
| M-401 | 900 | +100 |
| M-402 | 0 | 0 |
Medal multiplier: Bronze ×1.0, Silver ×1.1, Gold ×1.2 (applied to base only).

## 4. Skills (3 trees × 4 nodes; ~8 MP available in campaign, so players choose)
| Tree | Node 1 (1 MP) | Node 2 (1) | Node 3 (2) | Node 4 (2) |
|---|---|---|---|---|
| Marksman | Steady Aim (−10% spread) | Quick Reload (−20%) | Focus Fire (assist cone +4°) | Longwatch (range +15%) |
| Fieldcraft | Soft Steps (noise −25%) | Sure Footing (slope/water slowdown −25%) | Shadow (detection fill −15%) | Beacon Hand (repair −20% time) |
| Leadership | Clear Orders (cooldown −10%) | Steady Voice (squad regen +) | Rally (revive time ×0.7) | Lantern Call (one free ability use/mission) |

## 5. Equipment (MVP)
| Slot | Item | Unlock | Cost (RC) | Key stat |
|---|---|---|---|---|
| Primary | C-9 Ridge Carbine | Start | 0 | Balanced |
| Primary | Harrier SMG | M-103 | 300 | Fast fire, short range |
| Primary | Longwatch DMR | M-201 | 500 | Range, slow fire |
| Sidearm | Pike-9 | Start | 0 | Reliable |
| Gadget | Signal Flare | Start | 0 | Distract/light |
| Gadget | Smoke Canister | M-102 | 150 | Break line of sight |
| Gadget | Rope Launcher | M-202 | 250 | Traversal shortcut |
| Outfit | Ridge Greens / Harlow Waders / Lantern Dress | various | 0–200 | Cosmetic only |
Total RC earned by campaign ≈ 1,800 (estimate): enough for most but not all items, encouraging choice.

## 6. Trust meters
| Character | Start | Gain examples | Effect thresholds |
|---|---|---|---|
| Voss | 40 | Follow orders, protect civilians | ≥60 private scene M-202 |
| Relay | 40 | Protect comms, credit | ≥50 required for Open Beacon |
| Fathom | 40 | Save civilians | ≥60 extra revive in final |
| Bram | 40 | Support in M-203 | ≥60 better epilogue |
| Corin | 20 | Share credit | ≥40 ally in M-301 |
Range clamp 0–100. Changes shown in debrief with ±N icons.

## 7. Rewards feedback
| Event | Feedback |
|---|---|
| XP gain | Count-up bar, soft chime |
| Rank-up | Ceremony card with insignia, short fanfare, haptic |
| Item unlock | Card with 3D-look sprite and "Equip now" |
| Trust change | Character portrait pulse + arrow |
| Medal | Banner at debrief |

## 8. Difficulty and economy tuning
Economy affected by difficulty only through damage and assist; rewards are identical across modes (Veteran gets a cosmetic "Veteran" outfit once).

## 9. Acceptance
| ID | Criterion |
|---|---|
| PROG-001 | Rank derived from total XP only (no hidden state) |
| PROG-002 | Spending RC/MP never leaves negative balance; refunds possible for skills via Respec (free, once per chapter) |
| PROG-003 | Trust is clamped 0–100 and migrates in saves |
| PROG-004 | A player completing the main path with no optionals reaches R6 at M-401 |
