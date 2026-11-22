package com.example.game.valor.launch

import com.example.game.vanguard.CaseRecorder
import com.example.game.vanguard.ContractCase
import com.example.game.vanguard.observed

/** Sections 8 to 13: the QA matrix, the launch plan, the calendar and the risk register. */
object LaunchQaCases {

    fun run(): List<ContractCase> {
        val r = CaseRecorder(LaunchOpsAudit.GROUP_QA)
        checkMatrix(r)
        checkTestTypes(r)
        checkSeverity(r)
        checkGates(r)
        return r.result()
    }

    private fun checkMatrix(r: CaseRecorder) {
        r.check(
            "Four device tiers",
            "Low at 30 FPS, Mid at 45-60, High at 60, and a tablet scaling check",
            DeviceMatrix.TIERS.size == 4 &&
                DeviceMatrix.LOW.target == "30 FPS" &&
                DeviceMatrix.MID.target == "45-60 FPS" &&
                DeviceMatrix.HIGH.target == "60 FPS" &&
                DeviceMatrix.tabletsNeedScalingCheck(),
            observed(DeviceMatrix.TIERS.map { it.target }, listOf("30 FPS", "45-60 FPS", "60 FPS"))
        )
        r.check(
            "RAM classifies the tier",
            "3 GB is Low, 6 GB is Mid, 8 GB is High",
            DeviceMatrix.classify(3) == DeviceMatrix.LOW &&
                DeviceMatrix.classify(6) == DeviceMatrix.MID &&
                DeviceMatrix.classify(8) == DeviceMatrix.HIGH &&
                DeviceMatrix.fpsTargetFor("LOW") == "30 FPS",
            observed(DeviceMatrix.classify(8).tier, "High")
        )
        r.check(
            "Six real devices across five brands",
            "Samsung, Xiaomi/Redmi, Realme/Oppo/Vivo, Motorola and Pixel",
            DeviceMatrix.MIN_REAL_DEVICES == 6 && DeviceMatrix.BRANDS.size == 5,
            observed(DeviceMatrix.MIN_REAL_DEVICES, 6)
        )
    }

    private fun checkTestTypes(r: CaseRecorder) {
        r.check(
            "Nine test types",
            "smoke, regression, soak, interruptions, save, network, localization, balance, security",
            TestPlan.TYPES.size == 9 &&
                TestPlan.typeNamed("Smoke")?.coverage?.contains("quit/resume") == true &&
                TestPlan.typeNamed("Soak")?.coverage?.contains("2 hours") == true,
            observed(TestPlan.TYPES.size, 9)
        )
        r.check(
            "Soak and playtest limits",
            "two hours without a crash, ten-plus testers, offline play works",
            TestPlan.SOAK_HOURS == 2 &&
                TestPlan.MIN_BALANCE_TESTERS == 10 &&
                TestPlan.OFFLINE_PLAY_WORKS &&
                TestPlan.ADS_FAIL_GRACEFULLY &&
                !TestPlan.SECRETS_IN_BUILD,
            observed(TestPlan.MIN_BALANCE_TESTERS, 10)
        )
        r.check(
            "A leak fails the soak even without a crash",
            "10 MB passes, 40 MB fails, a crash fails",
            TestPlan.soakStable(memoryGrowthMb = 10.0, crashed = false) &&
                !TestPlan.soakStable(memoryGrowthMb = 40.0, crashed = false) &&
                !TestPlan.soakStable(memoryGrowthMb = 1.0, crashed = true),
            observed(TestPlan.soakStable(40.0, false), false)
        )
    }

    private fun checkSeverity(r: CaseRecorder) {
        r.check(
            "Four severities with their rules",
            "S0 and S1 block a release; S2 schedules, S3 backlogs",
            Severity.entries.size == 4 &&
                Severity.S0.blocksRelease && Severity.S1.blocksRelease &&
                !Severity.S2.blocksRelease && !Severity.S3.blocksRelease &&
                Severity.S3.rule == "Backlog",
            observed(Severity.entries.map { it.code }, listOf("S0", "S1", "S2", "S3"))
        )
    }

    private fun checkGates(r: CaseRecorder) {
        val alpha = ReleaseGate.evaluate(
            ReleaseStage.ALPHA, s0 = 3, s1 = 9, coreSystemsPlayable = true,
            allContentIn = false, performanceTargetsMet = false,
            storeAssetsDone = false, privacyFormsDone = false
        )
        val alphaBlocked = ReleaseGate.evaluate(
            ReleaseStage.ALPHA, s0 = 0, s1 = 0, coreSystemsPlayable = false,
            allContentIn = false, performanceTargetsMet = false,
            storeAssetsDone = false, privacyFormsDone = false
        )
        val beta = ReleaseGate.evaluate(
            ReleaseStage.BETA, s0 = 0, s1 = 5, coreSystemsPlayable = true,
            allContentIn = true, performanceTargetsMet = true,
            storeAssetsDone = false, privacyFormsDone = false
        )
        val betaOverLimit = ReleaseGate.evaluate(
            ReleaseStage.BETA, s0 = 0, s1 = 6, coreSystemsPlayable = true,
            allContentIn = true, performanceTargetsMet = true,
            storeAssetsDone = false, privacyFormsDone = false
        )
        r.check(
            "Alpha only needs playable core systems",
            "bugs and missing content are allowed, a dead core loop is not",
            alpha.passed && !alphaBlocked.passed,
            observed(alpha.summary(), "PASS - Alpha")
        )
        r.check(
            "Beta allows five S1 and no S0",
            "six S1 blocks the gate",
            beta.passed && !betaOverLimit.passed &&
                ReleaseGate.BETA_MAX_S1 == 5,
            observed(betaOverLimit.summary(), "BLOCKED")
        )
        val rc = ReleaseGate.evaluate(
            ReleaseStage.RC, s0 = 0, s1 = 0, coreSystemsPlayable = true,
            allContentIn = true, performanceTargetsMet = true,
            storeAssetsDone = true, privacyFormsDone = true
        )
        val rcMissingForms = ReleaseGate.evaluate(
            ReleaseStage.RC, s0 = 0, s1 = 0, coreSystemsPlayable = true,
            allContentIn = true, performanceTargetsMet = true,
            storeAssetsDone = true, privacyFormsDone = false
        )
        r.check(
            "RC needs zero S0/S1 plus the store and privacy paperwork",
            "a clean build without data-safety forms still cannot ship",
            rc.passed && !rcMissingForms.passed &&
                ReleaseGate.shipBlocked(0, 1) && !ReleaseGate.shipBlocked(0, 0) &&
                ReleaseStage.RC.requirement.contains("0 S0/S1"),
            observed(rcMissingForms.summary(), "BLOCKED")
        )
    }
}

/** Sections 9 and 10: the launch calendar, the rollout and the live-ops cadence. */
object LaunchLiveOpsCases {

    fun run(): List<ContractCase> {
        val r = CaseRecorder(LaunchOpsAudit.GROUP_LIVEOPS)
        checkMilestones(r)
        checkRollout(r)
        checkCalendar(r)
        checkSupport(r)
        return r.result()
    }

    private fun checkMilestones(r: CaseRecorder) {
        r.check(
            "Seven milestones from T-8 to T+4",
            "the document's offsets, with launch day labelled T-0",
            LaunchCalendar.MILESTONES.size == 7 &&
                LaunchCalendar.milestoneFor(-8)?.label == "T-8 weeks" &&
                LaunchCalendar.milestoneFor(0)?.label == "T-0" &&
                LaunchCalendar.milestoneFor(4)?.label == "T+4 weeks",
            observed(LaunchCalendar.MILESTONES.map { it.offsetWeeks }, listOf(-8, -6, -4, -2, 0, 1, 4))
        )
        r.check(
            "Closed beta and soft-launch sizes",
            "20+ testers, then 2-3 countries including India",
            LaunchCalendar.CLOSED_BETA_TESTERS == 20 &&
                LaunchCalendar.SOFT_LAUNCH_MIN_COUNTRIES == 2 &&
                LaunchCalendar.SOFT_LAUNCH_MAX_COUNTRIES == 3 &&
                LaunchCalendar.milestoneFor(-4)?.action?.contains("India") == true,
            observed(LaunchCalendar.CLOSED_BETA_TESTERS, 20)
        )
        r.check(
            "Five marketing basics",
            "clips, dev logs, communities, YouTubers and a press kit",
            LaunchCalendar.MARKETING_BASICS.size == 5 &&
                LaunchCalendar.MARKETING_BASICS.any { it.contains("Press kit") },
            observed(LaunchCalendar.MARKETING_BASICS.size, 5)
        )
    }

    private fun checkRollout(r: CaseRecorder) {
        r.check(
            "Staged rollout 10, 50, 100",
            "the three percentages in order",
            RolloutPlan.STAGES == listOf(10, 50, 100) &&
                RolloutPlan.stageLabel() == "10% -> 50% -> 100%" &&
                RolloutPlan.isComplete(100),
            observed(RolloutPlan.stageLabel(), "10% -> 50% -> 100%")
        )
        r.check(
            "A missed KPI holds the rollout",
            "99.6% crash-free and 0.2% ANR move to 50; 99.0% or 0.5% do not",
            RolloutPlan.nextStage(10, 99.6, 0.2) == 50 &&
                RolloutPlan.nextStage(10, 99.0, 0.2) == null &&
                RolloutPlan.nextStage(50, 99.6, 0.5) == null &&
                RolloutPlan.nextStage(100, 99.9, 0.0) == null,
            observed(RolloutPlan.nextStage(10, 99.0, 0.2), null)
        )
    }

    private fun checkCalendar(r: CaseRecorder) {
        r.check(
            "Six cadences",
            "daily, weekly, bi-weekly, monthly, quarterly and events",
            LiveOpsCalendar.CADENCES.size == 6 &&
                Cadence.DAILY.content.contains("Daily Operation") &&
                Cadence.WEEKLY.content.contains("headshots only") &&
                Cadence.QUARTERLY.content.contains("30-tier") &&
                Cadence.MONTHLY.content.contains("3-5 missions"),
            observed(Cadence.entries.map { it.displayName }, "Daily ... Events")
        )
        r.check(
            "Weekly modifiers rotate",
            "headshots only, low ammo, one-hit, then back to headshots",
            LiveOpsCalendar.weeklyModifier(0) == WeeklyModifier.HEADSHOTS_ONLY &&
                LiveOpsCalendar.weeklyModifier(1) == WeeklyModifier.LOW_AMMO &&
                LiveOpsCalendar.weeklyModifier(2) == WeeklyModifier.ONE_HIT &&
                LiveOpsCalendar.weeklyModifier(3) == WeeklyModifier.HEADSHOTS_ONLY &&
                LiveOpsCalendar.weeklyModifier(-1) == WeeklyModifier.HEADSHOTS_ONLY,
            observed(LiveOpsCalendar.weeklyModifier(1).displayName, "Low ammo")
        )
        r.check(
            "Daily content is seeded",
            "one seed names the whole day's operation",
            LiveOpsCalendar.dailySeedPlan(42L) == "Daily Operation seed 42, daily login reward",
            observed(LiveOpsCalendar.dailySeedPlan(42L), "seed 42")
        )
    }

    private fun checkSupport(r: CaseRecorder) {
        val payload = SupportChannels.reportPayload("ARMORY", "M1", "IDLE")
        r.check(
            "Four support channels",
            "in-game report with logs and save, FAQ, email, review responses",
            SupportChannels.CHANNELS.size == 4 &&
                SupportChannels.channelNamed("In-game") != null &&
                SupportChannels.channelNamed("FAQ") != null,
            observed(SupportChannels.CHANNELS.size, 4)
        )
        r.check(
            "A bug report carries the logs and the save",
            "three bug_report parameters plus the two attachments",
            SupportChannels.fixTargetPerPatch() == 3 &&
                payload["logs"] == "attached" && payload["save"] == "attached" &&
                payload["mission_id"] == "M1",
            observed(payload["logs"], "attached")
        )
    }
}

/** Sections 11 to 13: the risk register, the schedule and the quick-start list. */
object LaunchRiskCases {

    fun run(): List<ContractCase> {
        val r = CaseRecorder(LaunchOpsAudit.GROUP_RISK)
        checkRisks(r)
        checkSchedule(r)
        checkQuickStart(r)
        return r.result()
    }

    private fun checkRisks(r: CaseRecorder) {
        r.check(
            "Ten risks, each mitigated",
            "scope, performance, licensing, AI overwrites, logic bugs, policy, retention, " +
                "burnout, legal, save corruption",
            RiskRegister.ROWS.size == 10 &&
                RiskRegister.ROWS.all { it.mitigation.isNotBlank() } &&
                RiskRegister.highImpact().size == 7,
            observed(RiskRegister.ROWS.size, 10)
        )
        r.check(
            "Two High/High risks lead the design",
            "scope and low-end performance are the pair to cut against",
            RiskRegister.topRisks().size == 2 &&
                RiskRegister.topRisks().all { it.likelihood == Likelihood.HIGH },
            observed(RiskRegister.topRisks().map { it.risk }, "scope, performance")
        )
        r.check(
            "Save corruption keeps its atomic-write mitigation",
            "the mitigation the save system already ships",
            RiskRegister.riskNamed("Save corruption")?.mitigation?.contains("Atomic") == true,
            observed(RiskRegister.riskNamed("Save corruption")?.impact, Impact.HIGH)
        )
    }

    private fun checkSchedule(r: CaseRecorder) {
        r.check(
            "Five phases in the documented durations",
            "2-3 weeks, 6-8 weeks, then 3-4, 2-3 and 1-2 months",
            Schedule.PHASES.size == 5 &&
                Schedule.PHASES.map { it.durationLabel } ==
                listOf("2-3 weeks", "6-8 weeks", "3-4 months", "2-3 months", "1-2 months"),
            observed(Schedule.PHASES.map { it.durationLabel }.first(), "2-3 weeks")
        )
        r.check(
            "The envelope is 8-11 months",
            "the total the document prints, with weeks converting cleanly",
            Schedule.TOTAL_LABEL == "~8-11 months" &&
                Schedule.TOTAL_MIN_MONTHS == 8 && Schedule.TOTAL_MAX_MONTHS == 11 &&
                Schedule.monthsFor(Schedule.PHASES[2].minWeeks) in 2.7..3.1,
            observed(Schedule.monthsFor(12), 2.77)
        )
        r.check(
            "Ship Acts 1-2 first",
            "the cut line that turns a stalled project into a release",
            Schedule.SHIP_FIRST_ACTS == 2 && Schedule.ACTS_SHIP_AS_FREE_UPDATES,
            observed(Schedule.SHIP_FIRST_ACTS, 2)
        )
    }

    private fun checkQuickStart(r: CaseRecorder) {
        r.check(
            "Ten ordered steps",
            "URP project through Play Console, privacy policy and closed test",
            QuickStart.total() == 10 &&
                QuickStart.step(1)?.contains("URP") == true &&
                QuickStart.step(10)?.contains("Play Console") == true &&
                QuickStart.step(11) == null,
            observed(QuickStart.total(), 10)
        )
        val tracker = QuickStartTracker()
        val startedEmpty = tracker.nextStep()
        val marked = tracker.markDone(1)
        val outOfRange = tracker.markDone(11)
        (1..QuickStart.total()).forEach { tracker.markDone(it) }
        r.check(
            "The checklist tracks its own progress",
            "next is 1, then 2 after the first tick, and complete at ten",
            startedEmpty == 1 && marked && !outOfRange && tracker.isComplete() &&
                tracker.progressLabel() == "10 / 10",
            observed(tracker.progressLabel(), "10 / 10")
        )
    }
}
