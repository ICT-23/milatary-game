# 18 — Monetization and Fair Play

**Purpose:** Protect the core experience and define any future commercial options.  
**Status:** MVP: no monetization; future ideas deferred  
**Last reviewed:** 2026-10-19

## MVP policy

The first release is complete without payment, ads, accounts, energy timers or network access. This is intentional: the team's early risk is delivering responsive controls, reliable saves, an ending and a polished visual slice—not integrating ad mediation or purchase validation. Do not integrate ads or in-app purchases before the core campaign and QA gates are passed.

## Principles

- No ad during active gameplay, combat, rescue, timed interactions or cinematics.
- Never sell access to a core mission, required skill, evidence clue or ending.
- No loot boxes or randomized paid rewards in the current design.
- No misleading countdowns, fake limited offers or coercive prompts.
- No advantage framed as “pay to skip frustration” when the underlying mission is intentionally made difficult.
- If cosmetic content is sold later, clearly display price and contents before purchase and restore eligible purchases according to Play rules.
- Do not introduce analytics/ad SDKs without reviewing permissions, collection, retention and Data safety declarations.

## Possible future model (not approved)

If the game reaches a quality threshold and user research demonstrates willingness to pay, evaluate one of these rather than adding several at once:

1. **Premium one-time purchase:** strongest fit for an authored offline story, but market willingness and acquisition economics need validation.
2. **Optional cosmetic pack:** limited to uniform cosmetics, title-screen art or soundtrack; must not affect capability.
3. **Optional rewarded ad outside missions:** only in a dedicated menu area and only if it does not compromise offline-first design; requires a network SDK, consent/policy review, suitable content rating and privacy changes.

The default remains no monetization until a separate product decision is recorded.

## Future technical dependencies

- Current Play Billing Library and applicable policy requirements if digital purchases are added.
- Server-side purchase verification if a service is introduced; never trust client-only flags for paid entitlements.
- Entitlement restore, duplicate transaction handling, refunds and offline access rules.
- Ad SDK version/license, child-directed and age-audience configuration, ad content rating fit, consent flows where required, and clear network-off behavior.
- Store listing and Data safety updates for any new SDK or data collection.

## Fair play tests

- Entire MVP campaign completes with no purchase and no network connection.
- No mandatory rewarded ad appears at a mission boundary.
- No time-gated unlock blocks story progression.
- Cosmetic unlocks have no hidden stat impact.
- Purchase UI (if added later) distinguishes one-time purchase from subscription, recurring billing or consumables accurately.

## Go/no-go questions before monetization

1. Is the full campaign already enjoyable and technically stable?
2. Has the team identified a real player problem this feature solves?
3. Is the value explicit, non-coercive and independent from required progression?
4. Can policy and privacy requirements be implemented within the budget?
5. Can the game remain understandable and complete when offline?

If any answer is no, defer monetization.

## Related documents

[Progression](07_PLAYER_PROGRESSION_AND_ECONOMY.md) · [Privacy/security](19_PRIVACY_SECURITY_AND_COMPLIANCE.md) · [Google Play release](21_GOOGLE_PLAY_RELEASE_PLAN.md)
