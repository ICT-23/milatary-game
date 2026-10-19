# 19 — Privacy, Security and Compliance

**Purpose:** Minimize data handling and set release review requirements.  
**Status:** Proposed; privacy/legal review required before publication  
**Last reviewed:** 2026-10-19

## Data minimization position

The MVP is offline single-player. Its intended runtime data is local game progress, settings and optional local profile display name. It does not need account creation, contacts, precise location, camera, microphone, SMS, phone state, Bluetooth, background location or a server API. Do not request these permissions merely because a third-party sample or generated project includes them.

Actual manifest permissions must be reviewed from the final packaged build; this document is not a substitute for inspecting engine/plugin merged manifests.

## Permission plan

| Permission/data access | MVP need | Decision |
|---|---|---|
| Internet/network | Not needed for gameplay; Play handles store distribution separately | Do not add app network capability unless final engine/plugin configuration requires a justified permission; verify merged manifest. |
| External storage / all files access | Not needed; saves go in app-private storage | Do not request broad file access. |
| Camera / microphone | Not needed | Do not request. |
| Location | Not needed; campaign map is fictional art | Do not request. |
| Contacts / phone / SMS | Not needed | Do not request. |
| Notifications | Not needed for MVP | Defer; no marketing or reminder notifications. |
| Billing | Not needed | No billing library in MVP. |
| Advertising ID | Not needed | No ad SDK in MVP. |
| Crash/analytics SDK | Not needed initially | Defer pending measured value, disclosure and retention decisions. |

The final engine may declare platform capabilities for its own runtime; inspect and document each one rather than assuming table values guarantee a clean manifest.

## Local data and deletion

- Store save files in the platform's app-private location using the engine-supported persistent data directory.
- Provide an in-game “Delete local save” or “Reset campaign” action with an explicit confirmation of what will be erased.
- Do not transmit save data externally in MVP.
- Explain that deleting the app may remove local progress; confirm actual Android/engine backup behavior before shipping exact wording.
- If account creation is added later, implement an accessible account deletion process and delete associated user data according to current policy and legal obligations. Account deletion is not part of MVP.

## Privacy policy and Google Play disclosure

Google Play's current official policy says all apps must link to a comprehensive privacy policy in Play Console and make a policy link/text accessible in the app, including apps that do not access personal/sensitive data. The policy must accurately describe developer contact, data accessed/collected/used/shared, security practices, retention and deletion. The Play Data safety form must accurately reflect the shipped code, SDKs and their behavior; do not claim “no data collected” until every dependency and behavior is audited.

Before release, replace placeholders with the real publisher entity, support contact and a publicly accessible non-PDF privacy policy URL. Ensure the app name or publisher is identified in the policy. Keep the store Data safety form consistent with the privacy policy and final release build.

## Secure development practices

- No API keys or passwords in source, docs, logs, localization files, generated images or source-control history.
- Signing keystores and passwords are stored in access-controlled secure storage; encrypted backups and recovery ownership must be documented.
- Review engine plugins for declared permissions, network calls, telemetry, licenses and transitive dependencies.
- Use HTTPS if any future network service is approved; no client-side secret is trusted.
- Do not log personal or sensitive data. Debug logs must not contain a user's real identity or arbitrary file contents.
- Keep a dependency/license inventory and update vulnerable dependencies through controlled, tested changes.
- Verify release builds do not contain development menus, debug endpoints, test credentials or internal local file paths.

## Content and age suitability

Content is fictional military action, stylized violence without graphic gore. This is an artistic target, not a guaranteed store rating. Complete the current Google Play/IARC content-rating questionnaire accurately for the final build, including combat, simulated violence, language, purchases and ads (if any). Re-run it when content/features change. The title and store marketing must match actual content and not mislead users about real military training.

## Intellectual property

- *OPERATION: EMBERWATCH* is a working title; clear trademark/store/domain availability before commercialization.
- Do not use Military Academy 3D's code, art, UI, names, story beats, screenshots, audio or distinctive mission layouts.
- Document rights for fonts, source art, generated assets, models, animations, music, SFX, middleware and open-source dependencies.
- Preserve license files/notices and provide attribution where required.
- Avoid real national flags, insignia, real groups and real ongoing conflicts used as close proxies.
- Do not use AI-generated likenesses of real people without rights/consent.

## Release compliance checklist

- [ ] Final manifest and SDK/plugin permissions reviewed.
- [ ] No unnecessary data collection or runtime network calls.
- [ ] In-game privacy link/text and Play Console privacy URL are live and accurate.
- [ ] Data safety form matches actual release binary and SDK inventory.
- [ ] IARC content rating submitted and store page matches content.
- [ ] Target audience selection is accurate.
- [ ] Any ads/IAP (currently none) have required disclosures and policy checks.
- [ ] Asset and dependency licenses recorded.
- [ ] Application ID and title checked for conflicts.
- [ ] Publisher contact and support path functional.

## Official references (checked 2026-10-19)

- Google Play Developer Program Policy: https://support.google.com/googleplay/android-developer/answer/18258653
- Data safety form: https://support.google.com/googleplay/android-developer/answer/10787469
- Android guidance for declaring data use: https://developer.android.com/privacy-and-security/declare-data-use
- Play content ratings: https://support.google.com/googleplay/android-developer/answer/9859655
- Play target API requirements: https://support.google.com/googleplay/android-developer/answer/11926878

Policy may change; reverify immediately before submission.
