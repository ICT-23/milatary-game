package com.example.game.valor.launch

/**
 * Section 9: one row of the launch plan.
 *
 * [offsetWeeks] is negative before launch, zero on launch day and positive after,
 * which is how the document writes T-8 through T+4.
 */
data class LaunchMilestone(val offsetWeeks: Int, val action: String) {

    val label: String
        get() = when {
            offsetWeeks < 0 -> "T-$offsetWeeks weeks"
            offsetWeeks == 0 -> "T-0"
            else -> "T+$offsetWeeks weeks"
        }
}

/** Section 9: the seven milestones and the staged rollout they end in. */
object LaunchCalendar {

    const val CLOSED_BETA_TESTERS = 20
    const val SOFT_LAUNCH_MIN_COUNTRIES = 2
    const val SOFT_LAUNCH_MAX_COUNTRIES = 3

    val MILESTONES: List<LaunchMilestone> = listOf(
        LaunchMilestone(
            -8,
            "Internal alpha (friends/family), fix core feel"
        ),
        LaunchMilestone(
            -6,
            "Closed beta (Play closed testing, $CLOSED_BETA_TESTERS+ testers), " +
                "start collecting feedback form"
        ),
        LaunchMilestone(
            -4,
            "Soft launch in $SOFT_LAUNCH_MIN_COUNTRIES-$SOFT_LAUNCH_MAX_COUNTRIES countries " +
                "(e.g. India + one more) via staged rollout, analyze KPIs (Section 3.3)"
        ),
        LaunchMilestone(-2, "Fix top issues, finalize store listing, trailer, press kit"),
        LaunchMilestone(0, "Staged rollout 10% -> 50% -> 100% watching crash/ANR"),
        LaunchMilestone(1, "Patch 1 (hotfix), community post"),
        LaunchMilestone(4, "Content update 1 (new Survival map + Weekly events)")
    )

    val MARKETING_BASICS: List<String> = listOf(
        "Short gameplay clips for Reels/Shorts/TikTok (15-30 s, \"from recruit to commander\")",
        "Dev log posts",
        "Reddit/Discord communities",
        "Small YouTuber outreach",
        "Press kit page (logo, screenshots, description)"
    )

    fun milestoneFor(offsetWeeks: Int): LaunchMilestone? =
        MILESTONES.firstOrNull { it.offsetWeeks == offsetWeeks }

    fun dueBy(weeksSinceStart: Int): List<LaunchMilestone> =
        MILESTONES.filter { it.offsetWeeks <= weeksSinceStart }

    /** Nothing happens before T-8; the plan is a straight line from there. */
    fun startsAtWeek(): Int = MILESTONES.first().offsetWeeks
}

/**
 * Section 9: "staged rollout 10% -> 50% -> 100% watching crash/ANR".
 *
 * The guard holds the current percentage when either KPI is missed, which is the
 * only reason the document gives for stopping an expansion.
 */
object RolloutPlan {

    val STAGES: List<Int> = listOf(10, 50, 100)

    /** Null means "stay where you are"; the rollout is complete at 100. */
    fun nextStage(currentPercent: Int, crashFreePercent: Double, anrPercent: Double): Int? {
        if (!KpiTargets.crashFree().passes(crashFreePercent)) return null
        if (!KpiTargets.anr().passes(anrPercent)) return null
        val index = STAGES.indexOf(currentPercent)
        if (index < 0 || index == STAGES.lastIndex) return null
        return STAGES[index + 1]
    }

    fun isComplete(currentPercent: Int): Boolean = currentPercent >= STAGES.last()

    fun stageLabel(): String = STAGES.joinToString(" -> ") { "$it%" }
}

/** Section 10: one cadence of the live-ops calendar. */
enum class Cadence(val displayName: String, val content: String) {
    DAILY("Daily", "Daily Operation (seeded), daily login reward"),
    WEEKLY("Weekly", "Weekly Challenge (modifier: headshots only / low ammo / one-hit)"),
    BIWEEKLY("Bi-weekly", "Limited skin or crate rotation (cosmetic)"),
    MONTHLY("Monthly", "New Survival map or mission pack (3-5 missions), balance patch"),
    QUARTERLY(
        "Quarterly",
        "Season (30-tier free + premium track - cosmetic/convenience only), " +
            "new weapon family, new era chapter (DLC-style free update)"
    ),
    EVENTS(
        "Events",
        "National/holiday themed cosmetic events; community milestones " +
            "(e.g. \"1M enemies defeated -> everyone gets a crate\")"
    )
}

/** Section 10: the three weekly modifiers, rotating in the order the document lists. */
enum class WeeklyModifier(val displayName: String) {
    HEADSHOTS_ONLY("Headshots only"),
    LOW_AMMO("Low ammo"),
    ONE_HIT("One-hit");

    companion object {
        /** Week 0 is the first week of the rotation, not a calendar week number. */
        fun forWeek(weekIndex: Int): WeeklyModifier {
            val safe = if (weekIndex < 0) 0 else weekIndex
            return entries[safe % entries.size]
        }
    }
}

/** Section 10: the calendar as data the live-ops screen can print. */
object LiveOpsCalendar {

    val CADENCES: List<Cadence> = Cadence.entries

    fun contentFor(cadence: Cadence): String = cadence.content

    fun cadenceNamed(name: String): Cadence? =
        CADENCES.firstOrNull { it.displayName.equals(name.trim(), ignoreCase = true) }

    fun weeklyModifier(weekIndex: Int): WeeklyModifier = WeeklyModifier.forWeek(weekIndex)

    /** Daily content is seeded, so two devices opening the same day agree. */
    fun dailySeedPlan(seed: Long): String = "Daily Operation seed $seed, daily login reward"
}

/** Section 10: support obligations that survive launch day. */
object SupportChannels {

    const val TOP_COMPLAINTS_FIXED_PER_PATCH = 3

    val CHANNELS: List<String> = listOf(
        "In-game \"Report a bug\" (attaches logs + save)",
        "FAQ",
        "Support email",
        "Play review responses (especially ad/bug complaints)"
    )

    const val BUG_REPORT_ATTACHES_LOGS = true
    const val BUG_REPORT_ATTACHES_SAVE = true

    fun channelNamed(name: String): String? =
        CHANNELS.firstOrNull { it.startsWith(name.trim(), ignoreCase = true) }

    fun fixTargetPerPatch(): Int = TOP_COMPLAINTS_FIXED_PER_PATCH

    /** A report without the save file cannot be reproduced, so both attachments ship. */
    fun reportPayload(scene: String, missionId: String, state: String): Map<String, String> =
        mapOf(
            "scene" to scene,
            "mission_id" to missionId,
            "state" to state,
            "logs" to if (BUG_REPORT_ATTACHES_LOGS) "attached" else "missing",
            "save" to if (BUG_REPORT_ATTACHES_SAVE) "attached" else "missing"
        )
}
