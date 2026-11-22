package com.example.game.valor.launch

/** Section 11: how likely a risk is, in the document's own three words. */
enum class Likelihood(val label: String) { HIGH("High"), MED("Med"), LOW("Low") }

/** Section 11: how much a risk costs if it lands. */
enum class Impact(val label: String) { HIGH("High"), MED("Med"), LOW("Low") }

/** Section 11: one risk row with its mitigation. */
data class RiskRow(
    val risk: String,
    val likelihood: Likelihood,
    val impact: Impact,
    val mitigation: String
) {
    val isHighImpact: Boolean get() = impact == Impact.HIGH
}

/** Section 11: the register, kept whole so a mitigation cannot quietly vanish. */
object RiskRegister {

    val ROWS: List<RiskRow> = listOf(
        RiskRow(
            "Scope too big for solo dev",
            Likelihood.HIGH,
            Impact.HIGH,
            "Build vertical slice first (Act 1 + 1 mission of Act 2), cut acts/features not " +
                "essential; keep modular"
        ),
        RiskRow(
            "Performance on low-end phones",
            Likelihood.HIGH,
            Impact.HIGH,
            "Early profiling every milestone; presets; strict budgets"
        ),
        RiskRow(
            "Asset licensing problems",
            Likelihood.MED,
            Impact.HIGH,
            "License log; avoid dubious sources"
        ),
        RiskRow(
            "AI-generated code overwrites work",
            Likelihood.MED,
            Impact.HIGH,
            "Protection protocol (Implementation Plan Section 2), Git commits per task, " +
                "branch workflow"
        ),
        RiskRow(
            "Logic bugs (stuck missions, damage errors)",
            Likelihood.MED,
            Impact.HIGH,
            "Unit tests + bots + watchdogs"
        ),
        RiskRow(
            "Play Store rejection (policy)",
            Likelihood.MED,
            Impact.MED,
            "Follow checklist (Section 5.2) early; test content rating & data safety"
        ),
        RiskRow(
            "Low retention",
            Likelihood.MED,
            Impact.HIGH,
            "FTUE polish, replay modes, soft-launch iteration"
        ),
        RiskRow(
            "Burnout / motivation",
            Likelihood.MED,
            Impact.MED,
            "Milestones with visible progress, weekly playable builds"
        ),
        RiskRow(
            "Legal (IP / Medal of Honor wording)",
            Likelihood.LOW,
            Impact.MED,
            "Original IP; use alternate in-game award name if concerned"
        ),
        RiskRow(
            "Save corruption",
            Likelihood.LOW,
            Impact.HIGH,
            "Atomic writes, backups, migration tests"
        )
    )

    fun riskNamed(risk: String): RiskRow? =
        ROWS.firstOrNull { it.risk.startsWith(risk.trim(), ignoreCase = true) }

    fun highImpact(): List<RiskRow> = ROWS.filter { it.isHighImpact }

    fun highestLikelihood(): List<RiskRow> =
        ROWS.filter { it.likelihood == Likelihood.HIGH }

    /** The two risks the document rates High/High are the ones to design around first. */
    fun topRisks(): List<RiskRow> =
        ROWS.filter { it.likelihood == Likelihood.HIGH && it.impact == Impact.HIGH }
}

/** Section 12: one phase of the solo-plus-AI schedule. */
data class SchedulePhase(
    val phase: String,
    val durationLabel: String,
    val minWeeks: Int,
    val maxWeeks: Int
)

/** Section 12: the rough schedule and the cut line the document recommends. */
object Schedule {

    val PHASES: List<SchedulePhase> = listOf(
        SchedulePhase("Prototype (player, shooting, 1 enemy)", "2-3 weeks", 2, 3),
        SchedulePhase(
            "Vertical slice (Act 1 playable, ranks, armory basics, good graphics in 1 scene)",
            "6-8 weeks",
            6,
            8
        ),
        SchedulePhase("Core content (Acts 2-4)", "3-4 months", 12, 17),
        SchedulePhase("Late content (Acts 5-6, drones, replay modes)", "2-3 months", 8, 13),
        SchedulePhase("Polish, optimization, QA, store", "1-2 months", 4, 9)
    )

    const val TOTAL_LABEL = "~8-11 months"
    const val TOTAL_MIN_MONTHS = 8
    const val TOTAL_MAX_MONTHS = 11

    /** "Ship Act 1-2 first as v1.0 and add acts as free updates if time is short". */
    const val SHIP_FIRST_ACTS = 2
    const val ACTS_SHIP_AS_FREE_UPDATES = true

    fun totalMinWeeks(): Int = PHASES.sumOf { it.minWeeks }

    fun totalMaxWeeks(): Int = PHASES.sumOf { it.maxWeeks }

    fun phaseNamed(phase: String): SchedulePhase? =
        PHASES.firstOrNull { it.phase.startsWith(phase.trim(), ignoreCase = true) }

    /** Weeks are the unit that fits both the 2-week prototype and the 4-month block. */
    fun monthsFor(weeks: Int): Double = weeks / WEEKS_PER_MONTH

    const val WEEKS_PER_MONTH = 4.33
}

/**
 * Section 13: the ordered quick-start checklist.
 *
 * Steps are one-based so the numbers match the document in the UI.
 */
object QuickStart {

    const val PLAYTEST_FRIENDS = 5

    val STEPS: List<String> = listOf(
        "Create Unity URP project, set Android build target, packages (Section 1)",
        "Create Git repo + LFS + .gitignore; commit baseline",
        "Put all 5 .md files + BUILD_STATUS.md + CHANGELOG.md in repo root",
        "Run Phase 0 audit (Implementation Plan Section 6) with the Universal Prefix (Section 11.1)",
        "Build Phase 1 -> Phase 2 (player + shooting + damage logic) and test hard",
        "Build Act 1 vertical slice with real-looking graphics in one scene",
        "Set up analytics + crash reporting early",
        "Playtest with $PLAYTEST_FRIENDS friends; fix feel",
        "Continue phases; keep committing small",
        "Prepare Play Console, privacy policy, closed test"
    )

    fun step(number: Int): String? = STEPS.getOrNull(number - 1)

    fun stepNumber(text: String): Int =
        STEPS.indexOfFirst { it.equals(text.trim(), ignoreCase = true) } + 1

    fun total(): Int = STEPS.size
}

/** The state of the Section 13 list: which steps are done and what is next. */
class QuickStartTracker(completed: Set<Int> = emptySet()) {

    private val done: MutableSet<Int> = completed.toMutableSet()

    fun isDone(stepNumber: Int): Boolean = stepNumber in done

    fun markDone(stepNumber: Int): Boolean {
        if (stepNumber !in 1..QuickStart.total()) return false
        done += stepNumber
        return true
    }

    fun nextStep(): Int? = (1..QuickStart.total()).firstOrNull { it !in done }

    fun isComplete(): Boolean = done.containsAll(1..QuickStart.total())

    fun progressLabel(): String = "${done.count { it in 1..QuickStart.total() }} / ${QuickStart.total()}"
}
