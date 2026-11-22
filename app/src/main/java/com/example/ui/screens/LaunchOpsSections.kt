package com.example.ui.screens

import androidx.compose.runtime.Composable
import com.example.game.valor.launch.AdKind
import com.example.game.valor.launch.AdPlacements
import com.example.game.valor.launch.AgeBand
import com.example.game.valor.launch.AnalyticsEvent
import com.example.game.valor.launch.AnalyticsRules
import com.example.game.valor.launch.AppVersion
import com.example.game.valor.launch.BuildPipeline
import com.example.game.valor.launch.BuildType
import com.example.game.valor.launch.Cadence
import com.example.game.valor.launch.ConsentGate
import com.example.game.valor.launch.ContentNotes
import com.example.game.valor.launch.DeviceMatrix
import com.example.game.valor.launch.DevEnvironment
import com.example.game.valor.launch.FtueFlow
import com.example.game.valor.launch.FtueRules
import com.example.game.valor.launch.IpSafety
import com.example.game.valor.launch.KpiTargets
import com.example.game.valor.launch.LaunchCalendar
import com.example.game.valor.launch.LegalDocuments
import com.example.game.valor.launch.ListingCopy
import com.example.game.valor.launch.LiveOpsCalendar
import com.example.game.valor.launch.MonetizationPrinciples
import com.example.game.valor.launch.PlayChecklist
import com.example.game.valor.launch.QuickStart
import com.example.game.valor.launch.ReleaseGate
import com.example.game.valor.launch.ReleaseStage
import com.example.game.valor.launch.RemoteConfig
import com.example.game.valor.launch.RepositorySpec
import com.example.game.valor.launch.RetentionHooks
import com.example.game.valor.launch.RiskRegister
import com.example.game.valor.launch.RolloutPlan
import com.example.game.valor.launch.Schedule
import com.example.game.valor.launch.Severity
import com.example.game.valor.launch.StoreCatalogue
import com.example.game.valor.launch.StoreListing
import com.example.game.valor.launch.TestPlan
import com.example.ui.components.MonoLabel
import com.example.ui.components.TacticalDetailRow
import com.example.ui.theme.AlertRed
import com.example.ui.theme.RadarCyan
import com.example.ui.theme.SignalAmber

/** Sections 1.1 - 1.4: the environment and the repository rules. */
@Composable
internal fun LaunchEnvironmentSection() {
    GddSection("DEV ENVIRONMENT", "SECTION 1.1 - 1.2 - TOOLS, PACKAGES, SETTINGS", RadarCyan) {
        TacticalDetailRow(
            "PROJECT",
            "${DevEnvironment.PROJECT_NAME} on ${DevEnvironment.TEMPLATE}"
        )
        TacticalDetailRow(
            "TOOLS",
            "${DevEnvironment.TOOLS.size} rows, from Unity LTS to the AI coding tool"
        )
        TacticalDetailRow(
            "PACKAGES",
            "${DevEnvironment.PACKAGES.size} entries, " +
                "${DevEnvironment.requiredPackages().size} required"
        )
        TacticalDetailRow(
            "BACKEND",
            "${DevEnvironment.SCRIPTING_BACKEND} / ${DevEnvironment.TARGET_ARCH} / " +
                "${DevEnvironment.MANAGED_STRIPPING} stripping"
        )
        TacticalDetailRow(
            "GRAPHICS",
            DevEnvironment.graphicsApiVariants().joinToString(" + ") +
                " / ${DevEnvironment.COLOR_SPACE}"
        )
        TacticalDetailRow("QUALITY", DevEnvironment.QUALITY_LEVELS.joinToString(" / "))
        TacticalDetailRow("INPUT", DevEnvironment.INPUT_SYSTEM)
        TacticalDetailRow(
            "ASSEMBLIES",
            DevEnvironment.ASSEMBLY_DEFINITIONS.joinToString(", ")
        )
    }
}

/** Sections 1.3 - 1.4: branches, ignore rules, LFS and the root documents. */
@Composable
internal fun LaunchRepositorySection() {
    GddSection("REPOSITORY", "SECTION 1.3 - 1.4 - BRANCHES, LFS, ROOT DOCS", SignalAmber) {
        TacticalDetailRow("BRANCHES", RepositorySpec.BRANCHES.joinToString(" / "))
        TacticalDetailRow(
            "MAIN",
            if (RepositorySpec.isDirectCommitAllowed("main")) "DIRECT COMMITS" else "PR ONLY",
            RadarCyan
        )
        TacticalDetailRow(
            "IGNORE",
            "${RepositorySpec.GITIGNORE_ENTRIES.size} entries, ${RepositorySpec.LFS_PATTERNS.size} LFS patterns"
        )
        TacticalDetailRow(
            "SERIALIZATION",
            "${RepositorySpec.ASSET_SERIALIZATION} / ${RepositorySpec.VERSION_CONTROL_MODE}"
        )
        TacticalDetailRow("TAGS", RepositorySpec.TAGS.joinToString(" "))
        RepositorySpec.COMMIT_EXAMPLES.forEach { example ->
            MonoLabel(text = example, size = 8)
        }
        MonoLabel(text = "ROOT DOCUMENTS", size = 8, color = RadarCyan)
        RepositorySpec.ROOT_DOCUMENTS.forEach { doc -> MonoLabel(text = doc) }
    }
}

/** Section 2: build types, versioning, signing, the build script and CI. */
@Composable
internal fun LaunchBuildSection() {
    GddSection("BUILD & AUTOMATION", "SECTION 2 - TYPES, VERSION, SIGNING, CI", RadarCyan) {
        BuildType.entries.forEach { type ->
            TacticalDetailRow(
                "${type.displayName} (${type.format})",
                "${type.purpose} - ${type.settings}"
            )
        }
        TacticalDetailRow("ABOUT", BuildPipeline.aboutLine(AppVersion.CURRENT))
        TacticalDetailRow(
            "SIGNING",
            "${BuildPipeline.SIGNING_MODE}, ${BuildPipeline.KEYSTORE_BACKUP_COPIES} backups, " +
                "keystore in Git: ${BuildPipeline.KEYSTORE_STORED_IN_GIT}"
        )
        TacticalDetailRow("BUILD SCRIPT", BuildPipeline.BUILD_METHOD)
        TacticalDetailRow("OUTPUT", BuildPipeline.OUTPUT_DIRECTORY)
        BuildPipeline.CI_JOBS.forEach { job -> MonoLabel(text = job, size = 8) }
    }
}

/** Sections 3.1 - 3.3: the event table, the dashboards and the KPI board. */
@Composable
internal fun LaunchAnalyticsSection() {
    GddSection("ANALYTICS & TELEMETRY", "SECTION 3 - EVENTS, DASHBOARDS, KPIs", SignalAmber) {
        TacticalDetailRow(
            "EVENTS",
            "${AnalyticsEvent.entries.size} rows, ${AnalyticsRules.DASHBOARDS.size} dashboards"
        )
        AnalyticsEvent.entries.forEach { event ->
            TacticalDetailRow(event.eventName, event.parameters.joinToString(", "))
        }
        TacticalDetailRow(
            "SAMPLING",
            "perf_sample at ${(AnalyticsRules.PERF_SAMPLE_RATE * 100).toInt()}% " +
                "(${AnalyticsRules.expectedPerfSamples(1_000)} per 1,000 frames)"
        )
        MonoLabel(text = "SOFT-LAUNCH TARGETS", size = 8, color = RadarCyan)
        KpiTargets.ALL.forEach { kpi ->
            TacticalDetailRow(kpi.kpi, kpi.targetLabel)
        }
    }
}

/** Sections 4.1 - 4.4: principles, products, placements and remote config. */
@Composable
internal fun LaunchMonetizationSection() {
    GddSection("MONETIZATION", "SECTION 4 - PRODUCTS, ADS, REMOTE CONFIG", SignalAmber) {
        TacticalDetailRow(
            "PRINCIPLES",
            "${MonetizationPrinciples.ruleCount()} rules, pay-to-win: " +
                MonetizationPrinciples.PAY_TO_WIN
        )
        StoreCatalogue.PRODUCTS.forEach { product ->
            TacticalDetailRow(
                product.name,
                "${product.type.displayName} - ${product.priceTier}"
            )
        }
        MonoLabel(text = "AD PLACEMENTS", size = 8, color = RadarCyan)
        AdPlacements.ROWS.forEach { row ->
            TacticalDetailRow(
                row.placement,
                "${row.kind.displayName} - ${if (row.used) row.reward else "not used"}"
            )
        }
        TacticalDetailRow(
            "CAPS",
            "${AdPlacements.MIN_MISSIONS_BETWEEN} missions / " +
                "${AdPlacements.MIN_MINUTES_BETWEEN} min / first " +
                "${AdPlacements.FIRST_SESSION_MINUTES} min ad-free"
        )
        TacticalDetailRow(
            "REMOTE",
            "${RemoteConfig.GROUPS.size} groups, ${RemoteConfig.DEFAULTS.size} shipped defaults"
        )
        TacticalDetailRow(
            "CRATE ODDS",
            RemoteConfig().odds()?.let { odds -> odds.tiers().joinToString() } ?: "unset"
        )
        TacticalDetailRow(
            "REWARDED",
            "${AdPlacements.rowsFor(AdKind.REWARDED).size} placements, banners: " +
                AdPlacements.bannersUsed()
        )
    }
}

/** Section 5: documents, the Play checklist, consent and the IP rules. */
@Composable
internal fun LaunchPrivacySection() {
    // An unknown age is the strict fallback, so this label is the default answer.
    val defaultAdMode =
        if (ConsentGate.requiresNonPersonalizedAds(AgeBand.UNKNOWN)) {
            "non-personalized only"
        } else {
            "personalized allowed"
        }
    GddSection("PRIVACY, LEGAL & COMPLIANCE", "SECTION 5 - CONSENT, PLAY, IP", RadarCyan) {
        TacticalDetailRow(
            "DOCUMENTS",
            LegalDocuments.ALL.joinToString(", ") { it.name }
        )
        TacticalDetailRow(
            "PLAY CHECKLIST",
            "${PlayChecklist.ITEMS.size} items, " +
                "${PlayChecklist.ALLOWED_PERMISSIONS.size} allowed permissions"
        )
        TacticalDetailRow(
            "RATING",
            "${PlayChecklist.TARGET_RATING}, ads declared: ${PlayChecklist.DECLARES_ADS}"
        )
        TacticalDetailRow(
            "CONSENT",
            "age gate -> consent dialog -> $defaultAdMode"
        )
        TacticalDetailRow("AWARD NAME", IpSafety.awardName())
        TacticalDetailRow(
            "MODERN FACTIONS",
            ContentNotes.FICTIONAL_MODERN_FACTIONS.joinToString(", ")
        )
        TacticalDetailRow(
            "ASSET LICENSES",
            IpSafety.LICENSES_FILE
        )
        MonoLabel(
            text = "REAL ACTORS, HATE GROUPS OR ATROCITY RECREATION: NONE",
            size = 8
        )
    }
}

/** Sections 6.1 - 6.2: the first ten minutes and the retention hooks. */
@Composable
internal fun LaunchFtueSection() {
    GddSection("ONBOARDING", "SECTION 6 - FTUE AND RETENTION", SignalAmber) {
        FtueFlow.STEPS.forEach { step ->
            TacticalDetailRow(step.clock, "${step.label} - ${step.detail}")
        }
        TacticalDetailRow(
            "RULES",
            "no ad for ${FtueRules.NO_AD_MINUTES} min, prompts dismissible, " +
                "tournament optional"
        )
        TacticalDetailRow(
            "CONTROLS",
            "customizable from the first gameplay screen: " +
                FtueRules.controlsScreenReachableAtFirstFrame()
        )
        MonoLabel(text = "RETENTION HOOKS", size = 8, color = RadarCyan)
        RetentionHooks.ALL.forEach { hook ->
            TacticalDetailRow(hook.hook, hook.surface)
        }
        TacticalDetailRow(
            "PUSH",
            "opt-in, max ${RetentionHooks.PUSH_MAX_PER_DAY}/day: \"${RetentionHooks.PUSH_MESSAGE}\""
        )
    }
}

/** Section 7: the store listing assets, copy and screenshot plan. */
@Composable
internal fun LaunchStoreSection() {
    GddSection("STORE LISTING", "SECTION 7 - ASSETS, COPY, SCREENSHOTS", RadarCyan) {
        StoreListing.ASSETS.forEach { asset ->
            TacticalDetailRow(asset.asset, asset.spec)
        }
        TacticalDetailRow("TITLE", ListingCopy.TITLE)
        TacticalDetailRow(
            "SHORT",
            "${ListingCopy.SHORT_DESCRIPTION} (${ListingCopy.SHORT_DESCRIPTION.length}/" +
                "${ListingCopy.SHORT_MAX_CHARS})"
        )
        TacticalDetailRow(
            "FULL",
            "${ListingCopy.FULL_DESCRIPTION_OUTLINE.size} paragraphs, " +
                "max ${ListingCopy.FULL_MAX_CHARS} chars"
        )
        TacticalDetailRow("KEYWORDS", ListingCopy.KEYWORDS.joinToString(", "))
        MonoLabel(text = "SCREENSHOT PLAN", size = 8, color = RadarCyan)
        StoreListing.SCREENSHOT_PLAN.forEachIndexed { index, shot ->
            MonoLabel(text = "${index + 1}. $shot")
        }
        StoreListing.TRAILER_BEATS.forEach { beat -> MonoLabel(text = beat) }
    }
}

/** Section 8: the device matrix, the test types, severity and the gates. */
@Composable
internal fun LaunchQaSection() {
    GddSection("TESTING & QA", "SECTION 8 - DEVICES, TESTS, GATES", SignalAmber) {
        DeviceMatrix.TIERS.forEach { tier ->
            TacticalDetailRow(
                tier.tier,
                "${tier.specs} -> ${tier.target}${tier.preset?.let { " ($it)" } ?: ""}"
            )
        }
        TacticalDetailRow(
            "DEVICE SET",
            "${DeviceMatrix.MIN_REAL_DEVICES} real devices across " +
                DeviceMatrix.BRANDS.joinToString(", ")
        )
        TestPlan.TYPES.forEach { type ->
            TacticalDetailRow(type.type, type.coverage)
        }
        Severity.entries.forEach { severity ->
            TacticalDetailRow("${severity.code} ${severity.meaning}", severity.rule)
        }
        ReleaseStage.entries.forEach { stage ->
            TacticalDetailRow(stage.displayName, stage.requirement)
        }
        TacticalDetailRow(
            "CURRENT GATE",
            ReleaseGate.evaluate(
                ReleaseStage.RC,
                s0 = 0,
                s1 = 0,
                coreSystemsPlayable = true,
                allContentIn = true,
                performanceTargetsMet = true,
                storeAssetsDone = true,
                privacyFormsDone = true
            ).summary(),
            RadarCyan
        )
    }
}

/** Sections 9 - 10: the launch calendar, the rollout, the cadence and support. */
@Composable
internal fun LaunchLiveOpsSection() {
    GddSection("LAUNCH & LIVE OPS", "SECTION 9 - 10 - CALENDAR, ROLLOUT, CADENCE", RadarCyan) {
        LaunchCalendar.MILESTONES.forEach { milestone ->
            TacticalDetailRow(milestone.label, milestone.action)
        }
        TacticalDetailRow("ROLLOUT", RolloutPlan.stageLabel())
        TacticalDetailRow(
            "KPI WATCH",
            "crash-free ${KpiTargets.crashFree().targetLabel}, ANR ${KpiTargets.anr().targetLabel}"
        )
        MonoLabel(text = "LIVE-OPS CADENCE", size = 8, color = RadarCyan)
        LiveOpsCalendar.CADENCES.forEach { cadence: Cadence ->
            TacticalDetailRow(cadence.displayName, cadence.content)
        }
        TacticalDetailRow(
            "WEEKLY CYCLE",
            (0..2).joinToString(" / ") { LiveOpsCalendar.weeklyModifier(it).displayName }
        )
        TacticalDetailRow(
            "SUPPORT",
            "report a bug (logs + save), FAQ, email, review replies - " +
                "top 3 complaints fixed per patch"
        )
    }
}

/** Sections 11 - 13: the risk register, the schedule and the quick-start list. */
@Composable
internal fun LaunchProgramSection() {
    GddSection("RISK, SCHEDULE & QUICK START", "SECTION 11 - 13", AlertRed) {
        RiskRegister.ROWS.forEach { risk ->
            TacticalDetailRow(
                risk.risk,
                "${risk.likelihood.label}/${risk.impact.label} - ${risk.mitigation}"
            )
        }
        MonoLabel(text = "SCHEDULE", size = 8, color = RadarCyan)
        Schedule.PHASES.forEach { phase ->
            TacticalDetailRow(phase.phase, phase.durationLabel)
        }
        TacticalDetailRow("TOTAL", Schedule.TOTAL_LABEL)
        TacticalDetailRow(
            "SHIP LINE",
            "Acts 1-${Schedule.SHIP_FIRST_ACTS} first, later acts as free updates"
        )
        MonoLabel(text = "QUICK START", size = 8, color = RadarCyan)
        QuickStart.STEPS.forEachIndexed { index, step ->
            MonoLabel(text = "${index + 1}. $step")
        }
    }
}
