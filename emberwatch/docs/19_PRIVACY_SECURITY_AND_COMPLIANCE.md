# 19 — Privacy, Security and Compliance

**Purpose:** Data handling, permissions, legal. **Status:** Proposed · **Last reviewed:** 2026-10-18 · Re-verify every item against current Google Play policy before submission (REL checklist in [21](21_GOOGLE_PLAY_RELEASE_PLAN.md)).

## 1. Data inventory (MVP)
| Data | Collected? | Where | Purpose |
|---|---|---|---|
| Player-chosen name | Locally entered | Device only | Dialogue |
| Progress/settings | Yes | Device only | Gameplay |
| Crash data | Play Console Android vitals (platform-provided) | Google | Stability |
| Advertising ID, analytics, location, contacts | **No** | — | — |
Net: no personal data leaves the device from the app itself (`Needs Validation` — confirm after dependency audit; transitive SDKs must be inspected).

## 2. Android permissions
| Permission | Needed? | Why |
|---|---|---|
| INTERNET | **No (MVP)** — remove if any library adds it; note dev builds may need it for Metro and must not ship that way | — |
| VIBRATE | Yes (haptics) | Haptic feedback (normal permission) |
| Storage/media | No | Saves are in app-private storage |
| Camera/mic/location | No | — |
Check final merged manifest in CI (`aapt dump permissions`) and fail on unexpected permissions.

## 3. Privacy policy and Data Safety
A privacy policy URL is required in Play Console even for apps collecting nothing; host it on a stable public URL and bundle a copy offline. Complete the Data Safety form accurately (declare "no data collected" only if verified). If any SDK is added, update both.

## 4. Third-party SDKs and licences
Maintain `THIRD_PARTY_LICENSES.md` and in-app licence screen (#28 in [08](08_UI_UX_AND_SCREEN_SPECIFICATIONS.md)). Audit each dependency's licence and data behaviour.

## 5. Security
| Topic | Policy |
|---|---|
| Secrets | None in repo/docs; signing keys stored in password manager/secure vault; Play App Signing enabled |
| Build | Release builds minified; debug menu stripped in production |
| Saves | Checksummed, app-private; not encrypted (tamper acceptable) |
| Dependencies | `npm audit`/Dependabot, lockfile committed |
| Repo | Secret scanning enabled; `.gitignore` for keystores, `.env` |

## 6. Content and responsibility
Fictional nations; non-graphic; no real extremist symbols; avoid real-world political parallels naming actual countries. Provide Codex disclaimer: "fictional entertainment." Content rating via IARC questionnaire; expect Teen-range outcome (`Needs Validation`; answer honestly).

## 7. Copyright
No assets from the reference or other games; maintain manifest ([16](16_ASSET_PIPELINE_AND_FILE_CONVENTIONS.md)); fonts/music licence evidence stored.

## 8. Children
Not directed at children under 13; do not market as such. If audience declared includes children, additional Families Policy applies (`Deferred` — avoid).

## 9. Acceptance
| ID | Criterion |
|---|---|
| PRIV-001 | Final manifest permissions equal approved list |
| PRIV-002 | Dependency audit shows no analytics/ad SDK |
| PRIV-003 | Privacy policy reachable and consistent with Data Safety |
| PRIV-004 | Repo contains no secrets (scan passes) |
