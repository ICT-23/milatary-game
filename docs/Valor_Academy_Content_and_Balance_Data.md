# VALOR ACADEMY — CONTENT & BALANCE DATA
### Exact numbers: weapons, enemies, difficulty, economy, XP, and all 60 missions

> **Companion files:** `Valor_Academy_Game_Design.md` (what), `Valor_Academy_Implementation_Plan.md` (how/logic).
> **This file:** the **numbers and content tables** that go into ScriptableObjects / JSON.
>
> **PROTECTION RULE (same as the Implementation Plan, Section 2):**
> If any of these values are already implemented and tuned in the project (`EXISTS-WORKING`), **do not overwrite them**. Use this file only to fill in what is missing, or as a reference for tuning. All numbers here are a **first draft** — final values come from playtesting.

---

## 1. HOW TO READ THE TABLES

- **Dmg** = damage per bullet/pellet (torso, before multipliers). Head ×2.5, Limb ×0.7 (Implementation Plan 5.4).
- **RPM** = rounds per minute. **Range** in meters = distance where damage reaches `minDamageMult` (default 0.5 for rifles, 0.3 for shotguns/SMG).
- **Acc** = base accuracy 0–1 (higher = tighter spread). **RecV** = vertical recoil per shot (degrees).
- **Unlock** = player rank number (see Section 5).
- Shots-to-kill (STK) target: **a 100-HP enemy** should die in the stated body shots.

---

## 2. WEAPON DATA (all values start-point)

### 2.1 Pistols
| ID | Name | Era | Dmg | RPM | Range | Acc | RecV | Reload | Mag | Reserve | Unlock | Price | STK (body) |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| pistol_mk1 | Service Pistol Mk1 | All | 34 | 300 | 35 | 0.75 | 1.0 | 1.5 | 7 | 49 | 1 | 0 (start) | 3 |
| revolver_frontier | Frontier Revolver | WW1 | 55 | 150 | 40 | 0.80 | 2.0 | 3.0 | 6 | 36 | 3 | 800 | 2 |
| pistol_9mm | Sidearm 9mm | Modern | 28 | 420 | 30 | 0.72 | 0.9 | 1.4 | 15 | 75 | 9 | 2,500 | 4 |
| pistol_auto | Machine Pistol "Wasp" | Modern | 18 | 900 | 20 | 0.55 | 1.1 | 1.7 | 20 | 100 | 13 | 5,500 | 6 |

### 2.2 Rifles
| ID | Name | Era | Dmg | RPM | Range | Acc | RecV | Reload | Mag | Reserve | Unlock | Price | STK |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| bolt_pioneer1 | Pioneer Bolt Rifle | WW1 | 85 | 45 | 90 | 0.90 | 2.5 | 3.2 | 5 | 40 | 1 | 0 (start) | 2 |
| bolt_pioneer2 | Pioneer Mk2 | WW1/2 | 95 | 55 | 100 | 0.90 | 2.4 | 2.9 | 5 | 40 | 4 | 3,000 | 2 (1 head) |
| semi_liberty | Liberty Battle Rifle | WW2 | 48 | 240 | 70 | 0.80 | 1.6 | 2.6 | 8 | 64 | 6 | 4,500 | 3 |
| ar_vanguard | AR Vanguard | Cold War | 28 | 650 | 60 | 0.78 | 1.2 | 2.1 | 30 | 180 | 11 | 9,000 | 4 |
| carbine_raptor | Carbine "Raptor" | Modern | 25 | 700 | 45 | 0.80 | 0.9 | 2.0 | 30 | 180 | 12 | 10,000 | 4 |
| ar_sentinel | AR Sentinel (heavy) | Modern | 33 | 550 | 70 | 0.82 | 1.4 | 2.3 | 30 | 150 | 14 | 16,000 | 3 |
| dmr_hawk | DMR "Hawk" | Modern | 62 | 200 | 120 | 0.90 | 1.8 | 2.4 | 10 | 60 | 15 | 22,000 | 2 |

> Note: the `ar_vanguard` JSON example in the Design doc (unlock rank 6) was a placeholder. If it is already implemented with rank 6, keep it; otherwise use rank 11 above.

### 2.3 SMGs, LMGs, Shotguns
| ID | Name | Era | Dmg | RPM | Range | Acc | RecV | Reload | Mag | Reserve | Unlock | Price | STK |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| smg_trench | Trench Broom SMG | WW1 | 24 | 600 | 25 | 0.65 | 0.9 | 2.2 | 32 | 192 | 4 | 2,800 | 5 |
| smg_hornet | Hornet SMG | WW2 | 22 | 750 | 28 | 0.68 | 0.8 | 2.0 | 30 | 180 | 7 | 4,000 | 5 |
| smg_viper | Viper SMG (suppressed) | Modern | 21 | 800 | 30 | 0.72 | 0.7 | 1.8 | 30 | 180 | 13 | 9,500 | 5 |
| lmg_anvil | Machine Gun "Anvil" | WW1 | 30 | 500 | 70 | 0.60 | 1.5 | 5.5 | 100 | 400 | 7 | 7,000 | 4 |
| lmg_titanbelt | Belt-fed LMG "Bulwark" | Modern | 32 | 700 | 80 | 0.62 | 1.3 | 6.0 | 150 | 450 | 16 | 25,000 | 4 |
| sg_breaker | Pump Shotgun "Breaker" | All | 9 ×8 pellets | 70 | 18 | 0.55 | 3.5 | 0.55/shell | 6 | 36 | 5 | 3,500 | 2 (close) |
| sg_auto | Auto Shotgun "Hurricane" | Modern | 8 ×8 pellets | 180 | 15 | 0.50 | 3.0 | 3.2 | 10 | 50 | 13 | 12,000 | 2 (close) |

### 2.4 Snipers & Launcher
| ID | Name | Era | Dmg | RPM | Range | Acc | Reload | Mag | Reserve | Unlock | Price | Notes |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| sniper_longwatch | Sniper "Longwatch" | WW2 | 140 | 40 | 200 | 0.97 | 3.6 | 5 | 30 | 8 | 9,000 | Body = 2 shots, head = 1 shot kill; scope 8× |
| sniper_titan | Anti-Material "Titan" | Modern | 300 | 30 | 250 | 0.98 | 4.5 | 5 | 25 | 17 | 40,000 | Penetrates light cover; kills Heavy in 1–2 |
| launcher_hammer | Rocket Launcher "Hammer" | Modern | 250 direct + splash r=6 m | 20 | 120 | 0.85 | 4.5 | 1 | 6 | 12 | 20,000 | Vehicles & bunkers ×2 damage |

### 2.5 Melee & Throwables
| ID | Name | Dmg | Notes | Unlock | Price |
|---|---|---|---|---|---|
| knife_trench | Trench Knife | 60 (front), instant kill from behind (stealth) | Silent; 0.6 s swing | 1 | 0 |
| knife_combat | Combat Knife | 70 | Faster swing 0.45 s | 8 | 1,500 |
| grenade_frag | Frag Grenade | 150 max, r=7 m, fuse 3.0 s | Falloff exponent 1.5 | 2 | 100 each |
| grenade_smoke | Smoke Grenade | – | 12 s cloud r=6 m, blocks AI vision | 5 | 80 each |
| grenade_flash | Flash Grenade | – | Blinds 4 s (enemy in view cone) | 9 | 120 each |
| grenade_gas | Gas Grenade (WW1) | 12 dmg/s ×8 s, r=6 m | Gas mask negates 80% | 4 | 100 each |
| claymore_mine | Proximity Mine | 200, r=5 m | Triggers on enemy within 3 m | 10 | 250 each |

### 2.6 Weapon upgrade (Gunsmith) numbers
- Levels 1–10. Each level: **+3% damage, +2% handling (recoil −), +1% accuracy.** Level 10 = ≈ +30% damage, +20% handling, +10% accuracy.
- **Upgrade cost** (Cash) = `baseCost × 1.35^(level−1)` where `baseCost` = 10% of weapon price (min 150).
- Also costs **Steel**: `2 × level`.
- Weapon mastery tiers: 50 / 150 / 400 / 1,000 / 2,500 kills → unlock camo, charm, special attachment.

### 2.7 Attachments (modifier list)
| Slot | Name | Modifiers | Craft cost (Steel / Parts / Cash) | Unlock rank |
|---|---|---|---|---|
| Optic | Red-dot | ADS FOV 55→50; accuracy +3% | 5 / 2 / 300 | 3 |
| Optic | 4× scope | ADS FOV 28; ADS time +10% | 10 / 4 / 900 | 6 |
| Optic | 8× scope | ADS FOV 12; ADS time +25%; sway +15% | 18 / 8 / 2,400 | 10 |
| Optic | Thermal | Highlights heat sources; ADS time +20% | 30 / 15 / 6,000 | 15 |
| Muzzle | Suppressor | Noise ×0.3; Damage ×0.95; no flash | 15 / 8 / 1,800 | 8 |
| Muzzle | Compensator | Recoil V −15% | 8 / 3 / 700 | 6 |
| Muzzle | Flash hider | Muzzle flash ×0.3 | 6 / 2 / 500 | 6 |
| Barrel | Long barrel | Range +20%; Mobility −5% | 12 / 5 / 1,200 | 9 |
| Barrel | Short barrel | Mobility +8%; Range −15% | 10 / 4 / 900 | 9 |
| Underbarrel | Foregrip | Recoil H −12%; Recoil V −8% | 8 / 3 / 800 | 6 |
| Underbarrel | Bipod | Recoil −35% when prone/in cover; ADS time +10% | 12 / 5 / 1,300 | 10 |
| Magazine | Extended mag | Mag ×1.4; Reload +8% | 10 / 4 / 1,000 | 6 |
| Magazine | Fast mag | Reload −25% | 10 / 4 / 1,000 | 6 |
| Stock | Heavy stock | Recoil −10%; Mobility −4% | 8 / 3 / 700 | 8 |
| Stock | Light stock | Mobility +6%; Recoil +5% | 8 / 3 / 700 | 8 |
| Ammo | Armor-piercing | Armor pierce +40%; Damage ×0.95 | 20 / 10 / 2,000 | 11 |
| Ammo | Hollow-point | Damage ×1.15 vs unarmored; Armor pierce −30% | 18 / 8 / 1,800 | 11 |
| Ammo | Tracer | Visible trail; Accuracy feedback | 6 / 2 / 400 | 7 |

Rules: stacking follows Implementation Plan 5.7 (Add first, then Multiply; clamp; recompute from base).

---

## 3. ARMOR & GEAR

| Item | Tier | Effect | Price | Unlock |
|---|---|---|---|---|
| Helmet | T1 | Headshot mult 2.5 → 2.0 | 500 | 3 |
| Helmet | T2 | → 1.5 | 3,000 | 9 |
| Helmet | T3 | → 1.2 | 12,000 | 14 |
| Vest | T1 | 50 armor points, absorb 40% | 600 | 3 |
| Vest | T2 | 100 armor points, absorb 50% | 3,500 | 9 |
| Vest | T3 | 150 armor points, absorb 60% | 14,000 | 14 |
| Medkit | – | Heals 100%, 3.0 s use time (interruptible) | 150 | 1 |
| Bandage | – | Heals 30%, 1.5 s | 50 | 1 |
| Gas mask | – | Gas dmg −80% | 400 | 4 |
| Night vision | – | Brightens dark, outlines | 5,000 | 13 |
| Binoculars | – | Marks enemies 20 s | 600 | 5 |
| Backpack | T1/T2/T3 | Reserve ammo cap +15% / +30% / +50% | 800 / 4,000 / 12,000 | 3/9/14 |

Player base health 100, regen rules as in Implementation Plan 5.5.

---

## 4. ENEMIES & DIFFICULTY

### 4.1 Base enemy stats (Normal)
| Type | HP | Armor | Weapon class | Accuracy | Reaction (s) | Speed (m/s) | XP | Cash drop |
|---|---|---|---|---|---|---|---|---|
| Rifleman | 100 | 0 | Rifle | 0.35 | 0.6 | 3.5 | 20 | 10–30 |
| Rusher | 70 | 0 | Knife / SMG | 0.25 | 0.4 | 5.5 | 15 | 5–20 |
| Sniper | 80 | 0 | Sniper | 0.7 | 1.0 (+1.0 laser warning) | 2.8 | 40 | 30–60 |
| Heavy gunner | 250 | 100 | LMG | 0.30 | 0.8 | 2.2 | 60 | 40–80 |
| Grenadier | 100 | 0 | Rifle + grenades | 0.35 | 0.6 | 3.5 | 35 | 20–40 |
| Officer | 120 | 40 | Pistol | 0.45 | 0.5 | 3.2 | 80 | 60–120 |
| Medic | 80 | 0 | Pistol | 0.2 | 0.8 | 3.4 | 30 | 20–40 |
| Armored vehicle | 1,200 | 400 | Mounted MG | 0.5 | 1.0 | 4.0 | 150 | 150 |
| Tank | 2,500 | 800 | Cannon + MG | 0.55 | 1.2 | 3.0 | 300 | 300 |
| Enemy drone | 120 | 0 | Mini gun | 0.4 | 0.5 | 8.0 | 50 | 40 |
| Boss (mini) | 800–1,500 | 100–200 | Varies | 0.5 | 0.4 | 3.0 | 500 | 500 |
| Boss (final) | 3,000 | 300 | Multi-phase | 0.6 | 0.3 | 3.5 | 2,000 | 2,000 |

Accuracy here means the AI's cone width factor (higher = tighter); the AI still obeys hit detection (Implementation Plan 5.3, 5.10).

### 4.2 Difficulty multipliers
| Setting | Enemy HP | Enemy damage | Enemy accuracy | Enemy reaction time | Player regen | XP & cash mult | Friendly fire |
|---|---|---|---|---|---|---|---|
| Recruit (Easy) | ×0.8 | ×0.6 | ×0.8 | ×1.4 | Fast | ×0.8 | Off |
| Soldier (Normal) | ×1.0 | ×1.0 | ×1.0 | ×1.0 | Normal | ×1.0 | Off for player→ally |
| Veteran (Hard) | ×1.2 | ×1.4 | ×1.15 | ×0.8 | None | ×1.25 | On |
| Elite (Insane) | ×1.5 | ×1.8 | ×1.3 | ×0.65 | None, 1 medkit max | ×1.6 | On, ally death fails mission |

### 4.3 Time-to-kill sanity targets (Normal)
- Player with Lv1 AR vs Rifleman: ≈ 0.6 s (4 body shots @ 650 RPM).
- Rifleman vs player (unarmored, 100 HP, 0.35 accuracy): ≈ 4–6 s under sustained fire when in the open; cover must reduce this to > 10 s. Tune if fights feel too fast or too slow.
- Heavy gunner: ≈ 3 s with focused rifle fire (armor first), 1.5 s with AP rounds.

### 4.4 Wave scaling (Survival mode)
```
waveEnemyCount = 4 + wave * 1.5        (cap 30 total, 8 simultaneous)
enemyHP mult   = 1 + wave * 0.03
enemyAccuracy  = base + wave * 0.005   (cap +0.2)
Every 5th wave = Elite wave (Officer + Heavy)
Every 10th wave = Mini-boss
Rewards: cash = 50 * wave; XP = 30 * wave (no XP from respawn-farm)
```

---

## 5. RANKS — REVISED XP CURVE (v2 draft)

> The Design doc XP table grows very steeply near the end (900,000 XP). This **v2 curve is smoother** and fits a ~15–20 hour campaign + replay modes.
> **If the v1 table is already implemented and working, KEEP IT** and just scale mission XP rewards instead. Use v2 only for a new build.

| # | Rank | Cumulative XP (v2) | Skill point | Unlock highlights |
|---|---|---|---|---|
| 1 | Recruit | 0 | – | Pistol Mk1, Pioneer Bolt Rifle, Trench Knife |
| 2 | Private | 300 | 1 | Frag grenade, medkit |
| 3 | Private First Class | 900 | 1 | Red-dot, helmet T1, vest T1, Revolver |
| 4 | Corporal | 2,000 | 1 | Trench SMG, Pioneer Mk2, gas grenade/mask |
| 5 | Sergeant | 3,800 | 1 | Shotgun, smoke grenade, **1 AI squadmate** |
| 6 | Staff Sergeant | 6,500 | 1 | Semi-auto rifle, 4× scope, compensator, foregrip |
| 7 | Sergeant First Class | 10,000 | 1 | LMG, SMG Hornet, tracer ammo |
| 8 | Master Sergeant | 14,500 | 1 | Sniper Longwatch, suppressor, camo set 1 |
| 9 | Sergeant Major | 20,000 | 2 | **2 AI squadmates**, T2 gear, flash grenade |
| 10 | Second Lieutenant | 26,500 | 1 | Mission planning table, 8× scope, mine |
| 11 | First Lieutenant | 34,000 | 1 | Artillery call-in, AR Vanguard |
| 12 | Captain | 43,000 | 1 | Vehicle missions, launcher, carbine |
| 13 | Major | 53,000 | 2 | Night vision, machine pistol |
| 14 | Lieutenant Colonel | 64,500 | 1 | **3 AI squadmates**, T3 gear, AR Sentinel |
| 15 | Colonel | 77,000 | 2 | **Drone strike (ARIA)**, thermal, DMR |
| 16 | Brigadier General | 91,000 | 1 | Drone swarm, Bulwark LMG |
| 17 | Major General | 106,000 | 1 | Titan anti-material, legendary skins |
| 18 | Lieutenant General | 122,000 | 2 | Battle command mini-missions |
| 19 | General (Commander) | 140,000 | 2 | Final campaign missions |
| 20 | Medal of Honor Holder | Story complete | – | New Game+, Prestige |

Rank-to-act mapping: **Act 1:** ranks 1–3 · **Act 2:** 4–6 · **Act 3:** 7–10 · **Act 4:** 11–13 · **Act 5:** 14–15 · **Act 6:** 16–19. Replay modes close the XP gap to 140,000 for the last ranks if needed.

**XP sources** (Normal): see enemy table (kill XP); headshot +5; objective 50–500; mission completion = mission base XP × star multiplier (1★ ×1.0, 2★ ×1.2, 3★ ×1.5); first clear +25%.

### 5.1 Skill tree (spend skill points)
| Branch | Skill (5 levels each unless noted) | Effect per level |
|---|---|---|
| Marksman | Steady Hands | Recoil −4% |
| Marksman | Quick Draw | ADS time −5% |
| Marksman | Deadeye | Headshot damage +3% |
| Marksman | Hold Breath (1) | Sniper breath hold +2 s |
| Tactician | Radio Discipline | Support cooldown −5% |
| Tactician | Squad Leader | Squad accuracy +4% |
| Tactician | Quartermaster | Ammo pickup +10% |
| Tactician | Eyes Up (1) | Mark time +5 s |
| Survivor | Tough | Max health +5 |
| Survivor | Field Medic | Heal item speed +8% |
| Survivor | Plated | Armor absorb +2% |
| Survivor | Second Wind (1) | Once per mission survive lethal damage with 1 HP |
| Stealth | Light Foot | Footstep noise −8% |
| Stealth | Silent Hand | Takedown time −8% |
| Stealth | Shadow | Detection speed −5% |
| Stealth | Ghost (1) | Suppressed noise radius −30% extra |

---

## 6. ECONOMY TABLES

### 6.1 Income per mission (base, scaled by difficulty & stars)
| Act | Cash per mission | Steel | Parts |
|---|---|---|---|
| 1 | 80–250 | 2–5 | 0–1 |
| 2 | 300–900 | 5–12 | 1–3 |
| 3 | 800–2,200 | 10–20 | 3–6 |
| 4 | 2,000–5,000 | 15–30 | 5–10 |
| 5 | 4,500–9,000 | 25–45 | 8–15 |
| 6 | 8,000–16,000 | 35–60 | 12–25 |

Cash per kill: see enemy table drops. Bonus objective: +20% cash. 3★: +25% cash.

### 6.2 Medals (premium-lite currency)
- Earned from: achievements (5–25), act completion (50), weekly challenge (10–30), rewarded ads (max 3 per day, 2 each).
- Spent on: extra revive tokens (5), cosmetic skins (50–300), crate (30), instant-craft (10–40).
- Never sold at a price that makes gameplay items exclusive — all gameplay items obtainable with Cash/Steel/Parts.

### 6.3 Sinks (so players always have something to spend on)
Weapon upgrades, attachment crafting, armor tiers, consumables (medkits, grenades, mines), cosmetic skins, squad gear, drone upgrades, repair fees (optional on Elite: lose 5% cash on failure).

### 6.4 Crates (drops from missions / ads)
| Crate | Contents | Odds (example) |
|---|---|---|
| Supply | 150–400 Cash, 1–3 grenades/medkits | Common 70% |
| Armory | 5–20 Steel, 2–8 Parts, 1 attachment blueprint piece | Rare 25% |
| Command | Skin, large Cash, mastery boost | Epic 5% |
All odds visible in-game. Pity timer: Epic guaranteed within 20 crates.

---

## 7. THE 60 MISSIONS

Legend — **Types:** TUT tutorial · ASL assault · DEF defend · SNP sniper overwatch · STL stealth · ESC escort · RES rescue · VEH vehicle · RAIL rail shooter · DEM demolition · BOSS boss · DRN drone command · MIX mixed.
**Base XP** is before kills/bonus (kills add ≈ 25–35%). Unlock column shows new content/story at mission end.

### ACT 1 — BOOT CAMP (Fort Valor Academy) · Ranks 1–3 · ≈ 900 XP total
| # | Mission | Type | Location | Objectives | Base XP | Cash | Unlock / Story |
|---|---|---|---|---|---|---|---|
| 1 | Arrival Day | TUT | Barracks yard | Move, look, interact; meet Brooks, Lena, Vance | 30 | 80 | Character creator saved |
| 2 | Obstacle Course | TUT | Training grounds | Sprint, vault, crawl; finish in time (generous) | 40 | 90 | Stamina stat |
| 3 | Rifle Range | TUT | Range 1 | Hit 10 targets, reload, swap to pistol | 60 | 100 | Armory opened |
| 4 | Draw & Fire | TUT | Range 2 | Pistol quick-draw drill, headshot targets | 70 | 110 | Revolver blueprint piece |
| 5 | Grenade Basics | TUT | Range 3 | Throw frag at 3 zones; smoke drill | 80 | 120 | Frag grenade |
| 6 | Cover & Move | ASL | Live-fire course | Use cover, clear 8 pop-up targets | 90 | 140 | Cover tutorial done |
| 7 | Medic Drill | TUT | Field hospital | Heal self, revive dummy, carry | 90 | 150 | Medkit |
| 8 | Night Exercise | STL | Forest course | Reach checkpoint unseen; silent takedown drill | 120 | 170 | Stealth tutorial |
| 9 | Squad Exercise | DEF | Mock village | Defend flag vs practice enemies (non-lethal, paint rounds) | 150 | 200 | Squad concept |
| 10 | Graduation Day | MIX | Academy grounds | Exam: range + course + defend; Vance cheats → **choice** | 200 | 250 | Rank → PFC; Act 2 opens |

### ACT 2 — THE GREAT WAR (trenches) · Ranks 4–6 · ≈ 5,600 XP
| # | Mission | Type | Location | Objectives | Base XP | Cash | Unlock / Story |
|---|---|---|---|---|---|---|---|
| 11 | Mud Road | ASL | Supply road | Clear ambush, reach trench | 300 | 320 | Trench SMG |
| 12 | Over the Top | ASL | No-man's-land | Cross field, take first trench; smoke use | 400 | 380 | Smoke grenade |
| 13 | Wire Cutters | DEM | Barbed-wire line | Cut 3 gaps under fire | 450 | 420 | Gas threat introduced |
| 14 | Gas Alarm | DEF | Command dugout | Defend while gas cloud rolls in (mask timer) | 500 | 470 | Gas mask |
| 15 | Machine-Gun Nest | ASL | Hill 204 | Flank & destroy 2 MG nests | 550 | 520 | LMG blueprint |
| 16 | Night Raid | STL | Enemy trench | Silent entry, steal maps, escape | 600 | 580 | Suppressor blueprint |
| 17 | Farmhouse Defense | DEF | Ruined farm | Hold 4 waves | 650 | 640 | Shotgun |
| 18 | Brooks Down | RES | Trench hospital | Rescue wounded Brooks under fire | 700 | 700 | **Story: Brooks wounded** |
| 19 | Bunker Sweep | ASL | Fortified bunkers | Clear 3 bunkers, capture orders | 800 | 800 | Staff Sergeant rank |
| 20 | Trench Butcher | BOSS | Main trench line | Defeat armored heavy; use grenades | 1,000 | 900 | **Boss 1** · Kessler seen |

### ACT 3 — INTERWAR STORM · Ranks 7–10 · ≈ 20,000 XP
| # | Mission | Type | Location | Objectives | Base XP | Cash | Unlock / Story |
|---|---|---|---|---|---|---|---|
| 21 | Officer Trials | MIX | Officer school | Leadership exam: command 2 AI | 1,200 | 900 | Officer path |
| 22 | Old Town Patrol | ASL | European town | Clear patrol, capture radio | 1,400 | 1,000 | Semi-auto rifle |
| 23 | Train Station Chase | RAIL | Train roof | Gunner on moving train, protect carriage | 1,600 | 1,100 | First rail mission |
| 24 | Snow Pass Ambush | DEF | Mountain pass | Hold pass 5 waves, sniper threat | 1,800 | 1,200 | Sniper Longwatch |
| 25 | Docks Stealth | STL | Harbor | Sabotage 3 cranes unseen | 2,000 | 1,400 | Silent takedown skill |
| 26 | Informant Escort | ESC | City alleys | Escort informant to safehouse | 2,000 | 1,500 | Choice: trust informant |
| 27 | Rooftop Overwatch | SNP | City rooftops | Protect convoy with sniper | 2,200 | 1,600 | Spotter mechanic |
| 28 | Armored Car Heist | VEH | Highway | Chase & stop armored car | 2,400 | 1,800 | Vehicle basics |
| 29 | Vance Showdown | MIX | Warehouse | Vance ally / rival path (choice-dependent) | 2,600 | 2,000 | **Vance arc resolves** |
| 30 | Chapel Duel | BOSS | Ruined chapel | Duel young Kessler in 3 phases | 3,000 | 2,200 | **Boss 2** · Lieutenant |

### ACT 4 — WORLD AT WAR · Ranks 11–13 · ≈ 26,500 XP
| # | Mission | Type | Location | Objectives | Base XP | Cash | Unlock / Story |
|---|---|---|---|---|---|---|---|
| 31 | Beach Landing | ASL | Coastal beach | Storm beach, take bunkers | 2,200 | 2,200 | Artillery call-in |
| 32 | Hedgerow Push | ASL | Hedgerow fields | Advance field by field | 2,300 | 2,400 | Squad commands |
| 33 | Bombed City Street | MIX | Ruined city | Clear block, rescue civilians | 2,400 | 2,600 | Civilian rules |
| 34 | Tank Support | VEH | City outskirts | Escort tank; destroy AT guns | 2,500 | 2,800 | Tank gunner |
| 35 | Airfield Defense | DEF | Airfield | Defend against waves + air raid | 2,600 | 3,000 | Anti-air gun |
| 36 | Bridge Demolition | DEM | River bridge | Plant charges; hold; extract | 2,700 | 3,200 | Demolition skill |
| 37 | Sniper Alley | SNP | Ruined street | Eliminate enemy sniper team | 2,800 | 3,400 | DMR blueprint |
| 38 | Lena's Stand | DEF | Church tower | Protect Lena; **choice** decides her fate | 3,000 | 3,600 | **Story: Lena** |
| 39 | Fortress Assault | ASL | Fortress outer | Breach multiple gates | 3,200 | 3,800 | Launcher |
| 40 | Fortress Commander | BOSS | Fortress bunker | Multi-wave + rooftop boss | 4,000 | 5,000 | **Boss 3** · Medal nomination |

### ACT 5 — COLD SHADOWS · Ranks 14–15 · ≈ 24,000 XP
| # | Mission | Type | Location | Objectives | Base XP | Cash | Unlock / Story |
|---|---|---|---|---|---|---|---|
| 41 | Desert Convoy | VEH | Desert road | Protect convoy; drive lead truck | 2,000 | 4,500 | Desert gear |
| 42 | Canyon Recon | STL | Canyon | Mark 5 targets undetected | 2,100 | 4,800 | Binoculars use |
| 43 | Refinery Infiltration | MIX | Oil refinery | Sabotage with fire hazards | 2,200 | 5,200 | Explosive barrels |
| 44 | Night Vision Raid | ASL | Night city | Clear compound in darkness | 2,300 | 5,600 | Night vision |
| 45 | Helicopter Gunner | RAIL | Helicopter | Door gunner support run | 2,400 | 6,000 | Air support |
| 46 | Arctic Approach | ASL | Ice shelf | Cross ice field, blizzard visibility | 2,500 | 6,400 | Cold effects |
| 47 | Ghost Sniper | BOSS | Snowstorm | Duel enemy sniper | 2,800 | 7,200 | **Boss 4** |
| 48 | Leak Investigation | MIX | HQ + safehouse | Search clues, interrogate, choice | 2,600 | 7,600 | **Betrayal** plot |
| 49 | Safehouse Defense | DEF | Safehouse | Hold against assault | 2,700 | 8,000 | Squad upgrades |
| 50 | The Traitor | MIX | Command base | Confront traitor (choice: arrest/spare) | 3,000 | 9,000 | Colonel |

### ACT 6 — DRONE AGE · Ranks 16–19 · ≈ 63,000 XP total (with replay)
| # | Mission | Type | Location | Objectives | Base XP | Cash | Unlock / Story |
|---|---|---|---|---|---|---|---|
| 51 | ARIA Awakens | DRN | Tech base | Learn drone control | 5,000 | 8,000 | **Drone strike** |
| 52 | Skyline Rooftops | SNP | Megacity | Overwatch + drone recon | 5,500 | 9,000 | Recon drone |
| 53 | Swarm Defense | DEF | Rooftop base | Counter enemy drones | 6,000 | 10,000 | Defense drone |
| 54 | Carrier Deck | ASL | Aircraft carrier | Board and clear | 6,500 | 11,000 | Naval set |
| 55 | Comms Blackout | STL | Radar station | Silent infiltration, EMP | 7,000 | 12,000 | EMP device |
| 56 | Drone Factory | MIX | Factory | Destroy production lines | 7,500 | 13,000 | Swarm drone |
| 57 | FPV Strike | DRN | Canyon fortress | FPV pilot through tunnels | 8,000 | 14,000 | FPV mode |
| 58 | Command Bunker | ASL | Mountain bunker | Multi-floor assault | 8,500 | 15,000 | Titan blueprint |
| 59 | Kessler's Gambit | MIX | Network hub | Multi-stage: hack, defend, escape | 9,000 | 16,000 | Final prep |
| 60 | Final Duel: Kessler | BOSS | Arena | 4-phase boss + drone swarm → **Medal of Honor** | 12,000 | 20,000 | Endings, New Game+ |

### Side content (always available from Academy hub)
| Content | Rewards |
|---|---|
| Tournaments (Bronze/Silver/Gold) | 200–2,000 Cash, mastery XP, medals |
| Survival | Cash/XP by wave, leaderboard |
| Daily Operation | Crate + medals |
| Weekly Challenge | Medals + skin pieces |
| Boss Rush | Rare crate |
| Mission Mastery (3★ all) | Skins + titles |

---

## 8. CHARACTER & STORY STATS (for NPC data)
| NPC | HP | Weapon | Role in combat | Special |
|---|---|---|---|---|
| Sgt. Brooks | 200 | Rifle/Pistol | Ally (Acts 1–2), then support voice | StoryCritical |
| Lena Hartwell | 120 | Sniper | Overwatch ally; marks targets | StoryCritical (Act 4 choice) |
| Vance | 150 | Rifle | Ally or rival boss (Act 3) | StoryCritical |
| General Whitfield | – | – | Briefing only | – |
| Kessler (young) | 1,000 | Pistol + saber | Boss 2 | 3 phases |
| Kessler (final) | 3,000 | Rifle + drones | Final boss | 4 phases |

---

## 9. JSON / SCRIPTABLEOBJECT FIELD CHECKLIST

Every `WeaponData` must have: id, name, class, era, damage, pelletCount, rpm, fireModes[], range, minDamageMult, accuracy, spreadBase, adsSpreadMult, adsFov, adsTime, recoilCurve, recoilRecovery, reloadTime, emptyReloadTime, magSize, reserveCap, penetration, armorPierce, noiseRadius, moveMult, swapTime, unlockRank, price, slots[], mesh/prefab refs, audio refs.
`EnemyData`: id, type, hp, armor, weapon, accuracy, reaction, speed, visionRange/angle, hearingRadius, xp, cashDrop[min,max], behaviors.
`MissionData`: id, act, name, type, scene, objectives[], baseXP, baseCash, resources, starRules, unlocks[], storyBeat, checkpoints[].
`RankData`: index, name, xpRequired, skillPoints, unlocks[].

---

*End of Content & Balance Data. After implementing, run tests and adjust numbers only through the data assets.*
