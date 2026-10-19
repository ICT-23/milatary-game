# 18 — Monetization and Fair Play

**Purpose:** Define what is and isn't monetized. **Status:** Proposed · **Last reviewed:** 2026-10-18

## 1. MVP decision
**No ads, no IAP, no paid currency in MVP (Proposed).** The game must be complete and enjoyable without payment. Business model for launch is either free or a one-time paid app (`Needs Validation` — founder).

## 2. Fair-play rules (apply to any future monetization)
| ID | Rule |
|---|---|
| FAIR-001 | No purchase required to finish campaign or reach any ending |
| FAIR-002 | No purchase sells stat advantage or skips ranks |
| FAIR-003 | No ads during gameplay, dialogue, cinematics or failure/retry |
| FAIR-004 | No dark patterns (fake timers, confusing close buttons, nagging) |
| FAIR-005 | Child-appropriate; parental gate on any future purchase flow |

## 3. Future options (all `Deferred`)
| Option | Description | Dependencies | Review risks |
|---|---|---|---|
| Premium unlock | One-time paid app or "Full Campaign" unlock | Play Billing Library; Play Console products | Must follow Play Payments policy |
| Cosmetic packs | Outfits only | Play Billing | Loot-box rules: no random paid rewards |
| Rewarded ads (optional) | Opt-in extra RC after mission, on debrief screen only | Ads SDK (e.g., AdMob), consent (UMP), Data Safety updates | Privacy, ad-policy, child-directed concerns |
| Tip jar | Supporter badge | Play Billing | Low |
Technical note: any of these introduces network, SDK disclosures and Data Safety changes; see [19](19_PRIVACY_SECURITY_AND_COMPLIANCE.md). Policy must be re-verified against current Google Play Developer Policy before implementation.

## 4. Economy integrity
RC and MP are earn-only, bounded, local. Save tampering is tolerated (offline single-player) but not facilitated.

## 5. Acceptance
| ID | Criterion |
|---|---|
| MON-001 | MVP binary contains no ad/billing SDK |
| MON-002 | Any future monetization passes FAIR-001…005 review before merge |
