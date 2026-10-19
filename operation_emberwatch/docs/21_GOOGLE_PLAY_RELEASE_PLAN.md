# 21 — Google Play Release Plan

**Purpose:** Provide a verifiable path from dev build to production submission.  
**Status:** Proposed; platform checks last reviewed 2026-10-19  
**Last reviewed:** 2026-10-19

## Important status

This document is a release plan, not evidence that an app has been built, accepted or published. Google Play approval is not guaranteed. Re-check current Play Console requirements immediately before first submission and every later release.

## Application identity

Proposed display name: **Operation: Emberwatch** (working title; legal and store collision checks required). Proposed package ID: `com.<publisher>.operationemberwatch`; replace `<publisher>` with a stable, owned namespace after confirming available identity and release ownership. Do not use a placeholder package ID in a production build. Package/application ID is a long-term identity choice; verify carefully before first upload.

## Build types and tracks

| Type | Use | Signing / data |
|---|---|---|
| Editor/development build | Local iteration, verbose diagnostics, test menu allowed | Debug signing; never distribute as production. |
| Internal testing | Quick sanity builds for a small trusted group | Play internal track or controlled APK; test crash, basic flow, device install. |
| Closed testing | Controlled beta with planned test cases and feedback channel | Track build/version and test cohort; account requirements may apply. |
| Production candidate | Release-like, optimized, no dev menu, final disclosures | Signed Android App Bundle (AAB) from clean checkout. |
| Production release | Public distribution after review and access requirements | Increment version code, preserve signing continuity, monitor issues. |

## Current verified platform points (checked 2026-10-19)

1. **Target API:** Google Play's official target API page states that from **August 31, 2026**, new apps and updates must target **Android 16 / API level 36 or higher**, with listed form-factor exceptions. The deadline is already in effect as of the date on this document. Confirm whether a current extension or exception applies in the project's Play Console; do not rely on one as the baseline.
2. **App Bundle:** New Play apps use Android App Bundle (AAB) publication format; Google Play generates device-optimized APKs from the bundle. Confirm latest bundle/asset delivery limits with official docs before release.
3. **Personal developer accounts:** For personal accounts created after November 13, 2023, current Play guidance requires a closed test with at least 12 testers opted in continuously for at least 14 days before applying for production access. Other account types or policy changes may differ; check the actual account's Console dashboard.
4. **Content ratings:** Complete the Play Console/IARC questionnaire accurately. Ratings are determined by rating authorities based on responses; do not promise a Teen or other rating before submission.
5. **Privacy/Data safety:** Publish a privacy policy accessible from the store listing and within the app, and complete the Data safety form consistently with actual app and SDK behavior—even if the intended product is offline-first.
6. **Google AI Studio:** Its Android workflow can publish to internal testing according to current docs, but production releases are managed directly in Play Console. This game's Unity recommendation requires the engine's own build pipeline.

## Release checklist

### Identity and ownership
- [ ] Brand/title availability checked across Google Play, web, domains and relevant trademark registries with legal advice as needed.
- [ ] Stable package ID selected and controlled by the project owner/publisher.
- [ ] Play Console account and ownership/contact details complete.
- [ ] Publisher identity, support contact and website are real and maintained.

### Build and signing
- [ ] Engine version and build toolchain pinned/documented.
- [ ] Target SDK/API set to the applicable current Play requirement (currently API 36 baseline on this review date).
- [ ] Required ABI(s) and 64-bit rules verified for engine/plugins.
- [ ] Version code incremented and user-visible version name set.
- [ ] Production build is an AAB unless current policy explicitly allows another route for this case.
- [ ] App signing configured; keystore and passwords stored securely outside repository/CI logs.
- [ ] Clean build succeeds from a clean checkout; symbols/diagnostics retained securely as appropriate.
- [ ] No debug menu, test content, test credentials, unsupported permissions or unlicensed assets.

### Content and listing
- [ ] Final title and icon pass branding/trademark review.
- [ ] Store description accurately describes fictional single-player action; no real military training claim.
- [ ] Screenshots captured from actual release build; no misleading UI or unsupported features.
- [ ] Feature graphic and any required listing assets meet current Play specs.
- [ ] Content rating questionnaire submitted and matches actual violence/language/purchases/ads.
- [ ] Target audience and app category set accurately.
- [ ] App access instructions supplied if review requires special access (MVP has no login wall).

### Privacy and policy
- [ ] Public privacy policy URL active, accessible and not gated behind login/geofencing.
- [ ] In-game privacy/legal screen links to the same policy.
- [ ] Data safety form matches final build, manifest and SDK behaviors.
- [ ] Ads/IAP declarations are correct (MVP has none).
- [ ] Data deletion/contact path matches actual stored data and current policy.
- [ ] All third-party SDK and asset licenses checked.

### Testing / rollout
- [ ] Internal testing first, followed by closed testing if applicable/required.
- [ ] For qualifying new personal accounts, recruit at least 12 testers who remain opted in continuously for 14 days and provide useful feedback; check current Console instructions.
- [ ] Collect and prioritize tester reports; fix S0/S1 defects.
- [ ] Device compatibility, save migration, process-death and thermal tests pass.
- [ ] Production access request completed where required; do not presume passing the numeric threshold guarantees approval.
- [ ] Start staged rollout if supported/appropriate and monitor Android vitals/crashes/reviews.

## Versioning and hotfix strategy

Use semantic user-facing version names (`0.1.0`, `1.0.0`) with monotonically increasing Android version code. Build metadata includes git commit, engine version, content version and build channel. Hotfixes should change one clearly scoped area, increment version code, run focused regression and save migration tests, then use the appropriate track. If an issue is severe, halt rollout or use Play's currently available staged-release controls; do not promise that a published version can always be instantly removed from every device.

## Store asset plan

Create only after the build is stable: app icon with original relay mark; 5–8 genuine screenshots showing academy, field traversal, dialogue choice, rescue and beacon payoff; feature graphic per current Play requirements; short description; full description; privacy policy; support email/site. No reference-game screenshots or fabricated feature images. Store assets should match actual game visuals.

## Ownership and ongoing operations

Assign one owner for Play Console access recovery, signing-key backup, policy review, support email and emergency hotfix. Use least-privilege access; never commit secrets. Keep a release ledger with version, commit, AAB hash, target API, signing configuration (no secret values), tests, track, submission date, review decision and known issues.

## Official sources

- Target API requirements: https://support.google.com/googleplay/android-developer/answer/11926878
- Android App Bundle: https://developer.android.com/guide/app-bundle
- Testing requirements for new personal developer accounts: https://support.google.com/googleplay/android-developer/answer/14151465
- Content ratings: https://support.google.com/googleplay/android-developer/answer/9859655
- Data safety: https://support.google.com/googleplay/android-developer/answer/10787469
- Developer policy and privacy policy requirements: https://support.google.com/googleplay/android-developer/answer/18258653
- Android app versioning: https://developer.android.com/studio/publish/versioning
- Google AI Studio Android workflow: https://ai.google.dev/gemini-api/docs/aistudio-android

Recheck current policy dates and the specific Play Console account settings before submission.
