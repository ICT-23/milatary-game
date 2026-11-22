package com.example.game.valor.launch

import com.example.game.vanguard.CaseRecorder
import com.example.game.vanguard.ContractCase
import com.example.game.vanguard.observed

/** Sections 1 to 3: the environment, the build, the telemetry. */
object LaunchFoundationCases {

    fun devCases(): List<ContractCase> {
        val r = CaseRecorder(LaunchOpsAudit.GROUP_DEV)
        checkTools(r)
        checkPackages(r)
        checkStack(r)
        checkRepository(r)
        return r.result()
    }

    fun buildCases(): List<ContractCase> {
        val r = CaseRecorder(LaunchOpsAudit.GROUP_BUILD)
        checkBuildTypes(r)
        checkVersioning(r)
        checkSigning(r)
        checkScript(r)
        return r.result()
    }

    fun analyticsCases(): List<ContractCase> {
        val r = CaseRecorder(LaunchOpsAudit.GROUP_ANALYTICS)
        checkEventTable(r)
        checkPayloadRules(r)
        checkSampling(r)
        checkConsentGate(r)
        checkKpis(r)
        return r.result()
    }

    private fun checkTools(r: CaseRecorder) {
        r.check(
            "Eight tools", "every row names a purpose",
            DevEnvironment.TOOLS.size == 8 && DevEnvironment.TOOLS.all { it.purpose.isNotBlank() },
            observed(DevEnvironment.TOOLS.size, 8)
        )
        r.check(
            "Fifteen packages, two optional",
            "URP through Burst, with Mobile Notifications and Burst as the optional pair",
            DevEnvironment.PACKAGES.size == 15 &&
                DevEnvironment.requiredPackages().size == 13 &&
                DevEnvironment.packageNamed("Mobile Notifications")?.required == false &&
                DevEnvironment.packageNamed("Burst / Mathematics")?.required == false,
            observed(DevEnvironment.requiredPackages().size, 13)
        )
        r.check(
            "IL2CPP, ARM64, Medium stripping, Linear, Vulkan plus GLES3",
            "the five project settings the section prints",
            DevEnvironment.SCRIPTING_BACKEND == "IL2CPP" &&
                DevEnvironment.TARGET_ARCH == "ARM64" &&
                DevEnvironment.MANAGED_STRIPPING == "Medium" &&
                DevEnvironment.COLOR_SPACE == "Linear" &&
                DevEnvironment.graphicsApiVariants() == listOf("Vulkan", "OpenGLES3"),
            observed(DevEnvironment.graphicsApiVariants(), "Vulkan/OpenGLES3")
        )
        r.check(
            "Five assembly definitions", "Core, Gameplay, UI and the two test assemblies",
            DevEnvironment.ASSEMBLY_DEFINITIONS.size == 5 &&
                DevEnvironment.ASSEMBLY_DEFINITIONS.contains("Valor.Tests.PlayMode"),
            observed(DevEnvironment.ASSEMBLY_DEFINITIONS.size, 5)
        )
        r.check(
            "Three quality levels and two action maps",
            "Low / Medium / High, on-screen controls plus gamepad",
            DevEnvironment.QUALITY_LEVELS.size == 3 &&
                DevEnvironment.ACTION_MAPS == listOf("On-screen controls", "Gamepad"),
            observed(DevEnvironment.QUALITY_LEVELS, "Low/Medium/High")
        )
    }

    private fun checkPackages(r: CaseRecorder) {
        r.check(
            "Eight ignore entries and twelve LFS patterns",
            "Library through Builds, and png through unitypackage",
            RepositorySpec.GITIGNORE_ENTRIES.size == 8 &&
                RepositorySpec.LFS_PATTERNS.size == 12 &&
                RepositorySpec.LFS_PATTERNS.contains("*.blend"),
            observed(RepositorySpec.LFS_PATTERNS.size, 12)
        )
        r.check(
            "Five branches, main protected",
            "feature/ and fix/ and release/ merge into main, never commit to it",
            RepositorySpec.BRANCHES.size == 5 &&
                !RepositorySpec.isDirectCommitAllowed("main") &&
                RepositorySpec.isDirectCommitAllowed("develop") &&
                RepositorySpec.MERGES_VIA_PULL_REQUEST &&
                RepositorySpec.TESTS_BEFORE_MERGE,
            observed(RepositorySpec.isDirectCommitAllowed("main"), false)
        )
        r.check(
            "Four tags", "prototype, alpha, beta and 1.0.0",
            RepositorySpec.TAGS.size == 4 &&
                RepositorySpec.TAGS.first() == "v0.1.0-prototype" &&
                RepositorySpec.TAGS.last() == "v1.0.0",
            observed(RepositorySpec.TAGS.size, 4)
        )
        r.check(
            "Conventional commits",
            "the two shipped examples parse and a bare message does not",
            RepositorySpec.COMMIT_EXAMPLES.all { RepositorySpec.isConventionalCommit(it) } &&
                !RepositorySpec.isConventionalCommit("fix stuff"),
            observed(RepositorySpec.isConventionalCommit("fix stuff"), false)
        )
        r.check(
            "Nine root documents",
            "the five specs plus BUILD_STATUS, CHANGELOG, ASSET_LICENSES and README",
            RepositorySpec.ROOT_DOCUMENTS.size == 9 &&
                RepositorySpec.rootDocumentsPresent(emptyList()).size == 9,
            observed(RepositorySpec.ROOT_DOCUMENTS.size, 9)
        )
    }

    private fun checkStack(r: CaseRecorder) {
        r.check(
            "Rules keep their meaning on the shipped stack",
            "IL2CPP/Unity rows translate, so the table is not dead text",
            DevEnvironment.runsOnCurrentStack("Engine").contains("Compose") &&
                DevEnvironment.runsOnCurrentStack("Engine") != "Engine",
            observed(DevEnvironment.runsOnCurrentStack("Engine"), "Kotlin + Compose")
        )
    }

    private fun checkRepository(r: CaseRecorder) {
        r.check(
            "Text serialization and visible meta files",
            "the two version-control settings the section prints",
            RepositorySpec.ASSET_SERIALIZATION == "Force Text" &&
                RepositorySpec.VERSION_CONTROL_MODE == "Visible Meta Files" &&
                RepositorySpec.MAIN_ALWAYS_PLAYABLE,
            observed(RepositorySpec.VERSION_CONTROL_MODE, "Visible Meta Files")
        )
        r.check(
            "Root document audit is actionable",
            "a missing CHANGELOG is reported by name",
            RepositorySpec.rootDocumentsPresent(listOf("README.md")) ==
                RepositorySpec.ROOT_DOCUMENTS.filterNot { it == "README.md" },
            observed(RepositorySpec.rootDocumentsPresent(listOf("README.md")).size, 8)
        )
    }

    private fun checkBuildTypes(r: CaseRecorder) {
        r.check(
            "Three build types",
            "debug APK, release-candidate AAB and release AAB",
            BuildType.entries.size == 3 &&
                BuildType.DEBUG_APK.format == "APK" &&
                BuildType.RELEASE_CANDIDATE_AAB.format == "AAB" &&
                BuildType.entries.count { it.shipsToPlayers } == 1 &&
                BuildType.RELEASE_AAB.shipsToPlayers,
            observed(BuildType.entries.map { it.format }, listOf("APK", "AAB", "AAB"))
        )
        r.check(
            "Debug carries the debug surface, release does not",
            "dev flags and the debug panel never ride a release build",
            BuildPipeline.allowsDebugFlags(BuildType.DEBUG_APK) &&
                !BuildPipeline.allowsDebugFlags(BuildType.RELEASE_AAB) &&
                BuildPipeline.mustBeMinified(BuildType.RELEASE_AAB),
            observed(BuildPipeline.allowsDebugFlags(BuildType.RELEASE_AAB), false)
        )
    }

    private fun checkVersioning(r: CaseRecorder) {
        val current = AppVersion.CURRENT
        r.check(
            "Semantic version",
            "MAJOR.MINOR.PATCH with an incrementing Android version code",
            current.name == "1.0.0" &&
                AppVersion.NAME_FORMAT == "MAJOR.MINOR.PATCH" &&
                current.bump(VersionBump.PATCH) == AppVersion(1, 0, 1, 2) &&
                current.nextUpload() == AppVersion(1, 0, 0, 2),
            observed(current.nextUpload(), AppVersion(1, 0, 0, 2))
        )
        r.check(
            "Version parses both forms",
            "'0.3.2+41' keeps its code and '1.0' is rejected",
            AppVersion.parse("0.3.2+41") == AppVersion(0, 3, 2, 41) &&
                AppVersion.parse("1.0") == null,
            observed(AppVersion.parse("0.3.2+41"), AppVersion(0, 3, 2, 41))
        )
        r.check(
            "About line",
            "Settings -> About prints the name and the build number",
            BuildPipeline.aboutLine() == "Version 1.0.0 (build 1)" &&
                AppVersion.SHOWN_IN_SETTINGS_ABOUT,
            observed(BuildPipeline.aboutLine(), "Version 1.0.0 (build 1)")
        )
    }

    private fun checkSigning(r: CaseRecorder) {
        r.check(
            "Play App Signing, key out of Git, two backups",
            "the upload keystore lives in a password manager, not the repository",
            BuildPipeline.SIGNING_MODE == "Play App Signing" &&
                !BuildPipeline.KEYSTORE_STORED_IN_GIT &&
                BuildPipeline.KEYSTORE_BACKUP_COPIES == 2 &&
                BuildPipeline.KEYSTORE_ENV_VARS.size == 3,
            observed(BuildPipeline.KEYSTORE_ENV_VARS.size, 3)
        )
    }

    private fun checkScript(r: CaseRecorder) {
        r.check(
            "Editor build entry point",
            "BuildScript.BuildAndroidAab into Builds/ from the documented command line",
            BuildPipeline.SCRIPT_FILE == "BuildScript.cs" &&
                BuildPipeline.BATCH_COMMAND.contains("-executeMethod BuildScript.BuildAndroidAab") &&
                BuildPipeline.BATCH_COMMAND.contains("-logFile build.log") &&
                BuildPipeline.OUTPUT_DIRECTORY == "Builds/",
            observed(BuildPipeline.OUTPUT_DIRECTORY, "Builds/")
        )
        r.check(
            "Three CI jobs", "tests on PR, APK on develop, AAB on release tags",
            BuildPipeline.CI_JOBS.size == 3 &&
                BuildPipeline.CI_JOBS.last().contains("AAB"),
            observed(BuildPipeline.CI_JOBS.size, 3)
        )
    }

    private fun checkEventTable(r: CaseRecorder) {
        r.check(
            "Seventeen event rows",
            "the table's rows, each carrying at least one parameter",
            AnalyticsEvent.entries.size == 17 && AnalyticsRules.allEventsNameParameters(),
            observed(AnalyticsEvent.entries.size, 17)
        )
        r.check(
            "mission_end parameter list",
            "mission_id, result, time_s, stars, deaths, accuracy, damage_taken",
            AnalyticsEvent.MISSION_END.parameters == listOf(
                "mission_id", "result", "time_s", "stars", "deaths", "accuracy", "damage_taken"
            ),
            observed(AnalyticsEvent.MISSION_END.parameters.size, 7)
        )
        r.check(
            "session_start and perf_sample parameters",
            "version + device_tier, and the four perf fields",
            AnalyticsEvent.SESSION_START.parameters == listOf("version", "device_tier") &&
                AnalyticsEvent.PERF_SAMPLE.parameters ==
                listOf("avg_fps", "min_fps", "device_model", "quality_preset"),
            observed(AnalyticsEvent.PERF_SAMPLE.parameters.size, 4)
        )
        r.check(
            "Six dashboards",
            "funnel, fail hot-spots, difficulty, ad opt-in, crash-free, FPS by device",
            AnalyticsRules.DASHBOARDS.size == 6 &&
                AnalyticsRules.DASHBOARDS.any { it.contains("FPS by device") },
            observed(AnalyticsRules.DASHBOARDS.size, 6)
        )
    }

    private fun checkPayloadRules(r: CaseRecorder) {
        val complete = AnalyticsRules.build(
            AnalyticsEvent.MISSION_END,
            mapOf(
                "mission_id" to "M1", "result" to "win", "time_s" to "95", "stars" to "3",
                "deaths" to "0", "accuracy" to "0.62", "damage_taken" to "18"
            )
        )
        val partial = AnalyticsRules.build(
            AnalyticsEvent.MISSION_END, mapOf("mission_id" to "M1")
        )
        r.check(
            "A full payload passes, a partial one names its gaps",
            "six missing fields on a one-field payload",
            complete.isComplete && !partial.isComplete && partial.missing.size == 6,
            observed(partial.missing.size, 6)
        )
        r.check(
            "mission_end accepts only win, fail or quit",
            "a fourth result value is refused",
            AnalyticsRules.missionResultValid(complete) &&
                !AnalyticsRules.missionResultValid(complete.copy(values = complete.values + ("result" to "draw"))),
            observed(AnalyticsEvent.MISSION_RESULTS, listOf("win", "fail", "quit"))
        )
        r.check(
            "Unknown fields never travel",
            "a key outside the row is dropped before the payload is built",
            AnalyticsRules.build(
                AnalyticsEvent.SESSION_START,
                mapOf("version" to "1.0.0", "device_tier" to "MID", "user_id" to "42")
            ).values.containsKey("user_id").not(),
            observed(false, false)
        )
    }

    private fun checkSampling(r: CaseRecorder) {
        r.check(
            "perf_sample is sampled at 1%",
            "one frame in a hundred, ten reports per thousand frames",
            AnalyticsRules.PERF_SAMPLE_RATE == 0.01 &&
                AnalyticsRules.isPerfSample(100) &&
                !AnalyticsRules.isPerfSample(101) &&
                AnalyticsRules.expectedPerfSamples(1_000) == 10,
            observed(AnalyticsRules.expectedPerfSamples(1_000), 10)
        )
    }

    private fun checkConsentGate(r: CaseRecorder) {
        val denied = ConsentState(ageBand = AgeBand.UNDER_13, decided = true)
        val deniedTracker = AnalyticsTracker { denied }
        val perfBlocked = deniedTracker.track(
            AnalyticsEvent.PERF_SAMPLE,
            mapOf(
                "avg_fps" to "44", "min_fps" to "28",
                "device_model" to "Test", "quality_preset" to "MEDIUM"
            )
        )
        val reportSent = deniedTracker.track(
            AnalyticsEvent.BUG_REPORT,
            mapOf("scene" to "ARMORY", "mission_id" to "M1", "state" to "IDLE")
        )
        r.check(
            "Consent withholds telemetry but honours a bug report",
            "perf_sample blocked, bug_report sent without an identifying field",
            !perfBlocked && reportSent && deniedTracker.blockedCount() == 1 &&
                deniedTracker.sentCount() == 1 &&
                deniedTracker.sent().none { payload ->
                    payload.values.keys.any { it in AnalyticsRules.IDENTIFYING_FIELDS }
                },
            observed(deniedTracker.sentCount(), 1)
        )

        val allowed = ConsentState(
            ageBand = AgeBand.THIRTEEN_OR_OLDER,
            analyticsAllowed = true,
            personalizedAdsAllowed = true,
            decided = true
        )
        val allowedTracker = AnalyticsTracker { allowed }
        val missionSent = allowedTracker.track(
            AnalyticsEvent.MISSION_START,
            mapOf(
                "mission_id" to "M1", "difficulty" to "VETERAN",
                "loadout_primary" to "RIFLE_AR", "rank" to "PVT"
            )
        )
        r.check(
            "Consent opens the stream",
            "the same payload is accepted once analytics is allowed",
            missionSent && allowedTracker.sentCount() == 1 &&
                allowedTracker.sent().first().event == AnalyticsEvent.MISSION_START,
            observed(allowedTracker.sentCount(), 1)
        )
    }

    private fun checkKpis(r: CaseRecorder) {
        val tutorial = KpiTargets.target(KpiTargets.TUTORIAL_COMPLETION)
        val anr = KpiTargets.anr()
        val session = KpiTargets.target(KpiTargets.SESSION_LENGTH)
        r.check(
            "Eight KPI targets",
            "75% tutorial, 35% D1, 12% D7, 4% D30, 99.5% crash-free, 0.47% ANR, 8-15 min, 30%",
            KpiTargets.ALL.size == 8 &&
                KpiTargets.target(KpiTargets.D1)?.lowerBound == 35.0 &&
                KpiTargets.target(KpiTargets.D7)?.lowerBound == 12.0 &&
                KpiTargets.target(KpiTargets.D30)?.lowerBound == 4.0,
            observed(KpiTargets.ALL.size, 8)
        )
        r.check(
            "Bands are exclusive on the failing side",
            "74.9% tutorial misses, 0.5% ANR misses, 16 minutes misses the session band",
            tutorial?.passes(75.0) == true && tutorial?.passes(74.9) == false &&
                anr.passes(0.4) && !anr.passes(0.5) &&
                session?.passes(10.0) == true && session?.passes(16.0) == false,
            observed(tutorial?.passes(74.9), false)
        )
        r.check(
            "A board reports only what missed",
            "one miss is named, the rest stay quiet",
            KpiTargets.misses(
                mapOf(
                    KpiTargets.TUTORIAL_COMPLETION to 82.0,
                    KpiTargets.D1 to 37.0,
                    KpiTargets.CRASH_FREE to 99.2
                )
            ) == listOf(KpiTargets.crashFree()),
            observed(KpiTargets.misses(mapOf(KpiTargets.CRASH_FREE to 99.2)).size, 1)
        )
    }
}
