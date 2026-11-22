# VALOR ACADEMY — SETUP, LAUNCH & LIVE-OPS GUIDE
### Dev environment, Git, builds, analytics, monetization, Play Store, onboarding, QA, risks

> **Companion files:** Game Design, Implementation Plan, Content & Balance Data, Art/Audio/UI Bible.
> **PROTECTION RULE:** If any of this is already set up (project settings, Git repo, SDKs, build scripts), **inspect it first and extend it — never reset or overwrite working configuration.**

---

## 1. DEV ENVIRONMENT SETUP

### 1.1 Tools
| Tool | Purpose |
|---|---|
| Unity Hub + latest **Unity LTS** (check the current LTS version on unity.com) | Engine |
| Android Build Support (SDK, NDK, OpenJDK) | Android builds |
| Visual Studio / VS Code / Rider | C# editing |
| Git + Git LFS | Version control for large binary assets |
| Blender | Model fixes, LODs |
| Audacity / Reaper | Audio editing |
| Android Studio (for `adb`, Logcat) | Device debugging |
| Claude Code / AI coding tool | Vibe-coding |

### 1.2 Project creation
- Template: **3D (URP)** mobile. Name: `ValorAcademy`.
- Packages (Package Manager): Universal RP, Input System, Cinemachine, Timeline, AI Navigation, TextMeshPro, Addressables, Localization, Test Framework, Animation Rigging, Post Processing (via URP), Unity IAP (In-App Purchasing), Mobile Notifications (optional), Unity Analytics/Firebase (choose one pipeline), Burst/Mathematics (optional).
- Scripting backend: **IL2CPP**, ARM64 (+ ARMv7 only if you must support old phones), managed stripping Medium.
- Graphics API: Vulkan with OpenGLES3 fallback (test both).
- Color space: Linear.
- Min Android API: choose per Google Play's current requirements and your target devices (check current Play policy for required target API level before release).
- Quality levels: Low / Medium / High mapped to URP assets (see Art Bible Section 2 & Implementation Plan Section 8).
- Input: new Input System with on-screen controls; keep Gamepad action map.
- Assembly definitions: `Valor.Core`, `Valor.Gameplay`, `Valor.UI`, `Valor.Tests.EditMode`, `Valor.Tests.PlayMode`.

### 1.3 Repository
```
.gitignore: Unity template (Library/, Temp/, Obj/, Logs/, UserSettings/, *.csproj, *.sln, Builds/)
Git LFS track: *.png *.jpg *.psd *.fbx *.blend *.wav *.mp3 *.ogg *.mp4 *.exr *.hdr *.unitypackage
Asset Serialization: Force Text; Version Control: Visible Meta Files
Branches:  main (always playable) · develop · feature/<name> · fix/<name> · release/<version>
Commits:   small, one system each: "feat(weapons): add recoil curve", "fix(mission): kill-plane counts as dead"
Tags:      v0.1.0-prototype, v0.5.0-alpha, v0.9.0-beta, v1.0.0
```
Rule: **never commit directly to `main`**; merge via PR/review; run tests before merge.

### 1.4 Folder docs to keep in repo root
`Valor_Academy_Game_Design.md`, `Valor_Academy_Implementation_Plan.md`, `Valor_Academy_Content_and_Balance_Data.md`, `Valor_Academy_Art_Audio_UI_Bible.md`, this file, `BUILD_STATUS.md`, `CHANGELOG.md`, `ASSET_LICENSES.md`, `README.md`.

---

## 2. BUILD & AUTOMATION

### 2.1 Build types
| Build | Purpose | Settings |
|---|---|---|
| Debug (APK) | Daily testing | Development build, profiler, debug panel, cheats |
| Release Candidate (AAB) | Play internal testing | IL2CPP, no dev flags, minify/strip, signed |
| Release (AAB) | Production | Same as RC + final version code |

### 2.2 Versioning
`MAJOR.MINOR.PATCH` (e.g. 0.3.2) + incrementing **Android version code** every upload. Show version in Settings → About.

### 2.3 Signing
Use **Play App Signing**; keep the upload keystore backed up in 2 safe places (never in Git). Record keystore alias/path in a private password manager.

### 2.4 Build script (editor method)
Create `BuildScript.cs` with `BuildAndroidAab()` that sets scenes, version, keystore (from env vars), and outputs to `Builds/`. Run via command line: `Unity -batchmode -quit -projectPath . -executeMethod BuildScript.BuildAndroidAab -logFile build.log`.

### 2.5 CI (optional, later)
GitHub Actions + GameCI: run EditMode/PlayMode tests on each PR, build an APK artifact on `develop`, AAB on release tags.

---

## 3. ANALYTICS & TELEMETRY

Choose Firebase Analytics + Crashlytics (or Unity equivalent). **Respect consent** (Section 5.3).

### 3.1 Events
| Event | Parameters |
|---|---|
| `session_start` | version, device_tier |
| `tutorial_step` | step_id, completed |
| `mission_start` | mission_id, difficulty, loadout_primary, rank |
| `mission_end` | mission_id, result (win/fail/quit), time_s, stars, deaths, accuracy, damage_taken |
| `player_death` | mission_id, cause (enemy_type), checkpoint_idx |
| `rank_up` | new_rank, total_play_time |
| `weapon_purchase` / `craft` / `upgrade` | weapon_id, cost, currency |
| `ad_offered` / `ad_started` / `ad_completed` / `ad_failed` | placement, reward_type |
| `iap_purchase` | product_id, price, currency |
| `settings_changed` | key, value (graphics preset, aim assist level) |
| `perf_sample` | avg_fps, min_fps, device_model, quality preset (sampled 1%) |
| `bug_report` | scene, mission_id, state |

### 3.2 Key dashboards
Funnel (install → tutorial → Act 1 → Act 2 …), mission fail hot-spots, difficulty settings used, ad opt-in rate, crash-free rate, FPS by device.

### 3.3 Targets (healthy benchmarks to aim for; vary by game/region)
| KPI | Soft-launch goal |
|---|---|
| Tutorial completion | ≥ 75% |
| D1 retention | ≥ 35% |
| D7 retention | ≥ 12% |
| D30 retention | ≥ 4% |
| Crash-free sessions | ≥ 99.5% |
| ANR rate | < 0.47% (Google Play bad-behavior threshold area) |
| Avg session length | 8–15 min |
| Rewarded-ad opt-in | ≥ 30% of eligible players |

---

## 4. MONETIZATION DETAILS

### 4.1 Principles (from Implementation Plan 5.22)
- No forced ads during gameplay or cutscenes.
- No pay-to-win; every gameplay item earnable.
- Transparent odds for any random reward; pity timers.
- Parental-friendly: age-appropriate store info; no manipulative countdown pressure.

### 4.2 Products
| Product | Type | Suggested price (adjust per region — use Play's local price tiers) |
|---|---|---|
| Remove Ads (interstitials) | One-time | Low |
| Starter Pack (Remove Ads + 2 weapons + 500 Medals + Steel) | One-time | Low-mid |
| Medal packs (S/M/L) | Consumable | Tiered |
| Cosmetic skin bundles | Non-consumable | Tiered |
| Season/Battle pass (later, optional) | Time-limited | Mid |

India-friendly pricing: offer local price tiers and UPI-friendly checkout via Google Play billing; consider a lower-price starter pack for emerging markets.

### 4.3 Ad placements
| Placement | Type | Trigger | Reward |
|---|---|---|---|
| Double rewards | Rewarded | Results screen button | ×2 Cash/XP of that mission |
| Revive | Rewarded | Fail screen button | 50% HP revive once per mission |
| Free crate | Rewarded | Armory shop (cooldown 4 h) | Supply crate |
| Extra daily medals | Rewarded | Daily tab (max 3) | 2 Medals each |
| Interstitial | Interstitial | Between missions only, ≥ 3 missions apart, never after failing, never in first 20 minutes | – |
| Banner | – | **Not used** (keeps UI clean) | – |
Cap: ≤ 1 interstitial per 5 minutes; frequency adjustable via remote config.

### 4.4 Remote config (no new build needed)
Ad frequency, reward amounts, daily-op seed list, event dates, difficulty hotfix multipliers (e.g. fix an impossible tournament remotely), crate odds (within legal transparency). Always ship safe defaults inside the build.

---

## 5. PRIVACY, LEGAL & COMPLIANCE

### 5.1 Documents needed
- Privacy Policy (hosted URL), Terms of Service, Data deletion instructions/page.
- Support email & website.

### 5.2 Google Play requirements checklist (verify against current Play policy before release)
- [ ] Developer account (one-time fee) verified
- [ ] App content: privacy policy URL, ads declaration (contains ads: Yes), target audience & content (not for children under 13 unless built to Families policy), data safety form (collect/share: ad ID, analytics, purchases, crash logs), government apps / news / financial declarations as applicable
- [ ] Content rating questionnaire (IARC) — expect Teen/12+ (violence); answer honestly (realistic violence, no gore if blood disabled)
- [ ] Target API level meets Play's current requirement
- [ ] 64-bit builds, AAB format
- [ ] Permissions minimal (INTERNET, ACCESS_NETWORK_STATE; avoid unnecessary ones)
- [ ] Ads SDK compliance + consent flow (UMP for EEA/UK; handle India/US state privacy rules via consent UI)
- [ ] Billing via Google Play Billing only
- [ ] Testing track requirement for new personal developer accounts (closed testing with testers for a required period before production — check the current rule)
- [ ] Store listing assets ready (Section 7)

### 5.3 Consent flow
First launch: age gate (neutral, don't encourage lying) → consent dialog (ads personalization/analytics) → persistent "Privacy settings" entry in Settings. If a player is under 13 or age unknown → non-personalized ads only / no analytics identifiers beyond essentials.

### 5.4 Intellectual-property safety
- Original title, characters, story, art, music, voice. Do not use the original game's assets, name, logo or icon.
- Use generic weapon names; don't show real brand logos or trademarks.
- "Medal of Honor" is a real US award name — keep references respectful and in-story; avoid using it as a product brand or in the app name/icon. Consider an in-game alternate name like "Medal of Valor" if you want zero risk (editable string).
- Music/audio/fonts/assets: only licensed or original; keep `ASSET_LICENSES.md`.
- Add credits screen with all required attributions.

### 5.5 Content notes
Depict war with respect: no real-world hate groups, no real terror/atrocity recreation, no real living political figures. Use fictional nations/factions for the enemy in modern acts (suggestion: "Northern Coalition", "Red Meridian").

---

## 6. ONBOARDING (FTUE — First-Time User Experience)

### 6.1 First 10 minutes flow
```
0:00  Splash → short consent/age → language
0:30  Cinematic: arrival at Fort Valor (20 s, skippable)
1:00  Character creator (quick: 3 taps; "randomize" button)
2:00  Mission 1: movement + look (contextual prompts, big arrows)
4:00  Mission 2/3: first shots, headshot hint, reload prompt
7:00  First reward: Rank-up to Private → celebratory popup → Armory intro (equip a free item)
9:00  Mission 4 begins; "Daily reward" popup (small)
```
Rules: never show an ad in the first 20 minutes; every tutorial prompt is dismissible/skippable; controls are customizable from the first screen of gameplay; the first tournament is optional and passable (Implementation Plan 5.15).

### 6.2 Retention hooks
Daily login reward (7-day cycle), Daily Operation, Weekly Challenge, rank goals shown on Results, "next unlock" teaser, medal collection screen, push notifications (opt-in, max 1/day: "Your daily op is ready").

---

## 7. STORE LISTING

### 7.1 Assets
| Asset | Spec |
|---|---|
| App icon | 512×512 PNG, original emblem (e.g. shield + star + silhouette) |
| Feature graphic | 1024×500 |
| Phone screenshots | 6–8, landscape, 1920×1080 min, with short captions |
| Tablet screenshots | 4 (7"/10") |
| Trailer | 30–45 s YouTube link (gameplay-first: 0–5 s hook explosion/cinematic, 5–25 s variety, 25–40 s progression, end with logo) |
| Short description | ≤ 80 chars |
| Full description | ≤ 4,000 chars |

### 7.2 Listing copy template (original text — do not copy competitors)
**Title:** Valor Academy: Rise of the Commander
**Short:** Train. Fight. Rise through the ranks in realistic 3D war missions.
**Full (outline):**
1. Hook paragraph (fantasy: from recruit to commander).
2. Key features bullets: realistic 3D graphics; 60-mission story campaign across 6 eras; build and customize weapons; 20 ranks; drone strikes; squad commands; offline play.
3. Replay content: Survival, Daily Ops, Weekly Challenges, New Game+.
4. Fair play promise: no forced ads during missions.
5. Call to action.
**Keywords (ASO):** military game, army shooter, 3D war, offline shooting game, commander, sniper, soldier training, WW2 game, tactical shooter (avoid competitor brand names).

### 7.3 Screenshot plan
1. Cinematic hero shot with logo
2. Third-person firefight at golden hour
3. Gunsmith screen (stats + 3D weapon)
4. Rank/medal ceremony
5. Drone strike tactical map
6. Different eras montage
7. Squad commands
8. Weather/graphics showcase

---

## 8. TESTING & QA

### 8.1 Device matrix
| Tier | Example specs | Target |
|---|---|---|
| Low | 3 GB RAM, entry GPU, Android 9–11 | 30 FPS, Low preset |
| Mid | 4–6 GB RAM, mid GPU | 45–60 FPS, Medium |
| High | 8 GB+ flagship | 60 FPS, High |
| Tablet | 10" | UI scaling check |
Test at least 6 real devices (mix of Samsung, Xiaomi/Redmi, Realme/Oppo/Vivo, Motorola, Pixel if possible) — important for India market.

### 8.2 Test types
- **Smoke test (every build):** menu → mission → shoot → win → result → shop → quit/resume.
- **Regression suite:** from Implementation Plan Section 7.
- **Soak test:** 2 hours continuous play, memory stable, no crash.
- **Interruptions:** phone call, notification, app switch, screen lock, low battery, airplane mode, storage full.
- **Save tests:** force-close during save, update from older version, reinstall.
- **Network tests:** offline play works; ads/IAP fail gracefully.
- **Localization:** pseudo-locale and RTL pass.
- **Balance playtests:** 10+ testers, record fail points; tweak via data.
- **Security basics:** obfuscate/strip, no secrets in build, server-validate purchases if any online economy is added; offline cheating risk accepted for single-player but protect IAP entitlements.

### 8.3 Bug severity
| Sev | Meaning | Rule |
|---|---|---|
| S0 | Crash / progress loss / blocker | Fix before any release |
| S1 | Major feature broken | Fix before release |
| S2 | Annoying/visual | Schedule |
| S3 | Minor polish | Backlog |

### 8.4 Release gates
- Alpha: all core systems playable, placeholders OK.
- Beta: all content in, 0 S0, ≤ 5 S1, performance targets met.
- RC: 0 S0/S1, store assets done, privacy/data forms done.

---

## 9. LAUNCH PLAN

| Stage | Action |
|---|---|
| T-8 weeks | Internal alpha (friends/family), fix core feel |
| T-6 weeks | Closed beta (Play closed testing, 20+ testers), start collecting feedback form |
| T-4 weeks | Soft launch in 2–3 countries (e.g. India + one more) via staged rollout, analyze KPIs (Section 3.3) |
| T-2 weeks | Fix top issues, finalize store listing, trailer, press kit |
| T-0 | Staged rollout 10% → 50% → 100% watching crash/ANR |
| T+1 week | Patch 1 (hotfix), community post |
| T+4 weeks | Content update 1 (new Survival map + Weekly events) |

Marketing basics: short gameplay clips for Reels/Shorts/TikTok (15–30 s, "from recruit to commander" progression), dev log posts, Reddit/Discord communities, small YouTuber outreach, press kit page (logo, screenshots, description).

---

## 10. LIVE-OPS CALENDAR (post-launch)

| Cadence | Content |
|---|---|
| Daily | Daily Operation (seeded), daily login reward |
| Weekly | Weekly Challenge (modifier: headshots only / low ammo / one-hit) |
| Bi-weekly | Limited skin or crate rotation (cosmetic) |
| Monthly | New Survival map or mission pack (3–5 missions), balance patch |
| Quarterly | Season (30-tier free + premium track — cosmetic/convenience only), new weapon family, new era chapter (DLC-style free update) |
| Events | National/holiday themed cosmetic events; community milestones (e.g. "1M enemies defeated → everyone gets a crate") |

Support: in-game "Report a bug" (attaches logs + save), FAQ, email, respond to Play reviews (especially ad/bug complaints) — fix top-3 complaints each patch.

---

## 11. RISK REGISTER

| Risk | Likelihood | Impact | Mitigation |
|---|---|---|---|
| Scope too big for solo dev | High | High | Build vertical slice first (Act 1 + 1 mission of Act 2), cut acts/features not essential; keep modular |
| Performance on low-end phones | High | High | Early profiling every milestone; presets; strict budgets |
| Asset licensing problems | Med | High | License log; avoid dubious sources |
| AI-generated code overwrites work | Med | High | Protection protocol (Implementation Plan §2), Git commits per task, branch workflow |
| Logic bugs (stuck missions, damage errors) | Med | High | Unit tests + bots + watchdogs |
| Play Store rejection (policy) | Med | Med | Follow checklist (Section 5.2) early; test content rating & data safety |
| Low retention | Med | High | FTUE polish, replay modes, soft-launch iteration |
| Burnout / motivation | Med | Med | Milestones with visible progress, weekly playable builds |
| Legal (IP / Medal of Honor wording) | Low | Med | Original IP; use alternate in-game award name if concerned |
| Save corruption | Low | High | Atomic writes, backups, migration tests |

---

## 12. REALISTIC SCHEDULE (solo + AI, part-time) — rough

| Phase | Duration |
|---|---|
| Prototype (player, shooting, 1 enemy) | 2–3 weeks |
| Vertical slice (Act 1 playable, ranks, armory basics, good graphics in 1 scene) | 6–8 weeks |
| Core content (Acts 2–4) | 3–4 months |
| Late content (Acts 5–6, drones, replay modes) | 2–3 months |
| Polish, optimization, QA, store | 1–2 months |
**Total ≈ 8–11 months** (faster with a team or ready-made asset packs). Ship **Act 1–2 first** as v1.0 and add acts as free updates if time is short — better than never releasing.

---

## 13. QUICK START CHECKLIST (do these in order)

1. [ ] Create Unity URP project, set Android build target, packages (Section 1)
2. [ ] Create Git repo + LFS + `.gitignore`; commit baseline
3. [ ] Put all 5 `.md` files + `BUILD_STATUS.md` + `CHANGELOG.md` in repo root
4. [ ] Run **Phase 0 audit** (Implementation Plan §6) with the Universal Prefix (§11.1)
5. [ ] Build Phase 1 → Phase 2 (player + shooting + damage logic) and test hard
6. [ ] Build Act 1 vertical slice with real-looking graphics in one scene
7. [ ] Set up analytics + crash reporting early
8. [ ] Playtest with 5 friends; fix feel
9. [ ] Continue phases; keep committing small
10. [ ] Prepare Play Console, privacy policy, closed test

---

*End of Setup, Launch & Live-Ops Guide.*
