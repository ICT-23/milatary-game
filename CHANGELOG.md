# CHANGELOG

Required by `docs/Valor_Academy_Launch_and_LiveOps.md` Section 1.4 and tracked by
`com.example.game.valor.plan.ChangelogSpec`, which keeps the four headings below.
One entry per merged task, newest first; a commit that adds a system adds a line.

## Unreleased

### Added

- `game/valor/launch` - Sections 1-13 of the Launch & Live-Ops document as
  shipping tables and runtime rules: dev environment, repository rules, build
  types, versioning, signing, CI jobs, the 17-event analytics table, the consent
  gate, the eight KPI targets, the store catalogue, the six ad placements and
  their caps, remote config with in-build defaults, the FTUE clock, the retention
  hooks, the store listing limits, the device/test matrix, the release gates, the
  launch calendar, the staged rollout, the live-ops cadence, the risk register,
  the schedule and the quick-start list.
- `LaunchOpsAudit` - a ten-group contract gate that runs those tables in-process
  on every launch.
- `ui/screens/LaunchOpsScreen.kt` and `LaunchOpsSections.kt` - the read-out for
  Sections 1-13, reached from the title screen, with the gate in the footer.
- Settings: a persistent "Privacy settings" entry (Section 5.3) and an About
  readout that prints the version from `BuildPipeline.aboutLine()` (Section 2.2).
- `ASSET_LICENSES.md` - the licence log Section 5.4 requires.

### Modified

- `ui/EmberwatchViewModel.kt` - `AppScreen.LAUNCH` added to the enum.
- `MainActivity.kt` - one new branch and its BackHandler case; no existing
  branch was rewritten.
- `ui/screens/TitleScreen.kt` - `onOpenLaunch` parameter and the launch button
  after the implementation-plan button.
- `ui/screens/SettingsScreen.kt` - two additive cards above the reset zone.
- `game/valor/launch/MonetizationSpec.kt` - remote-config helpers renamed from
  `intercept*` to `interstitial*`.

### Removed

- Nothing. Every existing screen, table and route is unchanged.

### Tests

- 116 contract cases across `LAUNCH_DEV`, `LAUNCH_BUILD`, `LAUNCH_ANALYTICS`,
  `LAUNCH_MONETIZATION`, `LAUNCH_PRIVACY`, `LAUNCH_FTUE`, `LAUNCH_STORE`,
  `LAUNCH_QA`, `LAUNCH_LIVEOPS` and `LAUNCH_RISK`, each asserting the printed
  number against the shipped table.
- Cross-document checks: `AdsRules.MIN_MISSIONS_BETWEEN` and
  `TournamentRules.BLOCKS_STORY_PROGRESSION` must still agree with the launch
  document.

## 1.0.0 - 2026-10-31

### Added

- Campaign loop, mission select, debrief, roster, academy, armory, ranks, drill.
- `game/vanguard` - the seven-group core contract gate.
- `game/valor/gdd`, `balance`, `plan` - design document, balance data and
  implementation plan transcribed as data plus runtime rules.
- Room persistence (schema version 5, additive migrations).

### Modified

- None.

### Removed

- None.

### Tests

- `VanguardContractAudit`, `ValorPlanAudit` and the launch gate run at startup.
