package com.example.game.valor.launch

/**
 * Section 3.1: the event table.
 *
 * The enum carries the document's parameter names verbatim, so a caller cannot
 * invent a field and the gate can prove every row shipped with its parameters.
 */
enum class AnalyticsEvent(val eventName: String, val parameters: List<String>) {
    SESSION_START("session_start", listOf("version", "device_tier")),
    TUTORIAL_STEP("tutorial_step", listOf("step_id", "completed")),
    MISSION_START("mission_start", listOf("mission_id", "difficulty", "loadout_primary", "rank")),
    MISSION_END(
        "mission_end",
        listOf("mission_id", "result", "time_s", "stars", "deaths", "accuracy", "damage_taken")
    ),
    PLAYER_DEATH("player_death", listOf("mission_id", "cause", "checkpoint_idx")),
    RANK_UP("rank_up", listOf("new_rank", "total_play_time")),
    WEAPON_PURCHASE("weapon_purchase", listOf("weapon_id", "cost", "currency")),
    WEAPON_CRAFT("craft", listOf("weapon_id", "cost", "currency")),
    WEAPON_UPGRADE("upgrade", listOf("weapon_id", "cost", "currency")),
    AD_OFFERED("ad_offered", listOf("placement", "reward_type")),
    AD_STARTED("ad_started", listOf("placement", "reward_type")),
    AD_COMPLETED("ad_completed", listOf("placement", "reward_type")),
    AD_FAILED("ad_failed", listOf("placement", "reward_type")),
    IAP_PURCHASE("iap_purchase", listOf("product_id", "price", "currency")),
    SETTINGS_CHANGED("settings_changed", listOf("key", "value")),
    PERF_SAMPLE("perf_sample", listOf("avg_fps", "min_fps", "device_model", "quality_preset")),
    BUG_REPORT("bug_report", listOf("scene", "mission_id", "state"));

    /** "result (win/fail/quit)" - the only three values [MISSION_END] may carry. */
    companion object {
        val MISSION_RESULTS: List<String> = listOf("win", "fail", "quit")

        fun byName(name: String): AnalyticsEvent? =
            entries.firstOrNull { it.eventName.equals(name.trim(), ignoreCase = true) }
    }
}

/** One event ready to send: the row plus the values it carries. */
data class AnalyticsPayload(
    val event: AnalyticsEvent,
    val values: Map<String, String>
) {
    val missing: List<String> get() = event.parameters.filterNot { it in values }
    val unexpected: List<String> get() = values.keys.filterNot { it in event.parameters }
    val isComplete: Boolean get() = missing.isEmpty() && unexpected.isEmpty()
}

/** Section 3.1 and 3.2: the rules that keep the stream honest, plus the dashboards. */
object AnalyticsRules {

    /** "perf_sample ... sampled 1%". One frame in a hundred reports. */
    const val PERF_SAMPLE_RATE = 0.01
    const val PERF_SAMPLE_EVERY = 100

    /** Section 5.3: a withheld consent still allows what the player asked for. */
    val ESSENTIAL_WITHOUT_CONSENT: Set<AnalyticsEvent> = setOf(AnalyticsEvent.BUG_REPORT)

    /** Fields that identify a device rather than a session. */
    val IDENTIFYING_FIELDS: Set<String> = setOf("device_model")

    val DASHBOARDS: List<String> = listOf(
        "Funnel (install -> tutorial -> Act 1 -> Act 2 ...)",
        "Mission fail hot-spots",
        "Difficulty settings used",
        "Ad opt-in rate",
        "Crash-free rate",
        "FPS by device"
    )

    fun isPerfSample(tick: Long): Boolean = tick % PERF_SAMPLE_EVERY == 0L

    /** 1% of 1,000 sampled frames is ten reports, not a hundred. */
    fun expectedPerfSamples(frames: Int): Int = frames / PERF_SAMPLE_EVERY

    fun requiresConsent(event: AnalyticsEvent): Boolean = event !in ESSENTIAL_WITHOUT_CONSENT

    fun dashboardCount(): Int = DASHBOARDS.size

    /** Every row must name at least one parameter, or the table is decoration. */
    fun allEventsNameParameters(): Boolean = entries().all { it.parameters.isNotEmpty() }

    fun entries(): List<AnalyticsEvent> = AnalyticsEvent.entries

    fun build(event: AnalyticsEvent, values: Map<String, String>): AnalyticsPayload =
        AnalyticsPayload(event, values.filterKeys { it in event.parameters })

    /** `mission_end` may only carry win, fail or quit. */
    fun missionResultValid(payload: AnalyticsPayload): Boolean =
        payload.event != AnalyticsEvent.MISSION_END ||
            payload.values["result"] in AnalyticsEvent.MISSION_RESULTS
}

/**
 * Section 3.1 plus 5.3: the only door an event leaves through.
 *
 * When analytics consent is withheld nothing but a player-initiated report is
 * sent, and even that drops the identifying field.
 */
class AnalyticsTracker(
    private val consent: () -> ConsentState
) {

    private val sentEvents = mutableListOf<AnalyticsPayload>()
    private var blockedEvents = 0
    private var essentialWithheld = 0

    fun sent(): List<AnalyticsPayload> = sentEvents.toList()

    fun blockedCount(): Int = blockedEvents

    fun sentCount(): Int = sentEvents.size

    /** Returns true when the payload was accepted for upload. */
    fun track(event: AnalyticsEvent, values: Map<String, String>): Boolean {
        val state = consent()
        val payload = AnalyticsRules.build(event, values)
        if (!payload.isComplete) {
            blockedEvents += 1
            return false
        }
        if (state.analyticsAllowed) {
            sentEvents += payload
            return true
        }
        if (AnalyticsRules.requiresConsent(event)) {
            blockedEvents += 1
            return false
        }
        val stripped = payload.copy(
            values = payload.values.filterKeys { it !in AnalyticsRules.IDENTIFYING_FIELDS }
        )
        if (!stripped.isComplete) {
            essentialWithheld += 1
            return false
        }
        sentEvents += stripped
        return true
    }

    fun withheldEssentials(): Int = essentialWithheld
}

/** Section 3.3: one KPI row, with a bound on the side the document constrains. */
data class KpiTarget(
    val kpi: String,
    val targetLabel: String,
    val lowerBound: Double?,
    val upperBound: Double?
) {
    val isRange: Boolean get() = lowerBound != null && upperBound != null

    fun passes(value: Double): Boolean =
        (lowerBound == null || value >= lowerBound) &&
            (upperBound == null || value <= upperBound)
}

/** Section 3.3: the soft-launch benchmarks the staged rollout is watched against. */
object KpiTargets {

    const val TUTORIAL_COMPLETION = "Tutorial completion"
    const val D1 = "D1 retention"
    const val D7 = "D7 retention"
    const val D30 = "D30 retention"
    const val CRASH_FREE = "Crash-free sessions"
    const val ANR = "ANR rate"
    const val SESSION_LENGTH = "Avg session length"
    const val REWARDED_OPT_IN = "Rewarded-ad opt-in"

    val ALL: List<KpiTarget> = listOf(
        KpiTarget(TUTORIAL_COMPLETION, ">= 75%", lowerBound = 75.0, upperBound = null),
        KpiTarget(D1, ">= 35%", lowerBound = 35.0, upperBound = null),
        KpiTarget(D7, ">= 12%", lowerBound = 12.0, upperBound = null),
        KpiTarget(D30, ">= 4%", lowerBound = 4.0, upperBound = null),
        KpiTarget(CRASH_FREE, ">= 99.5%", lowerBound = 99.5, upperBound = null),
        KpiTarget(ANR, "< 0.47%", lowerBound = null, upperBound = 0.47),
        KpiTarget(SESSION_LENGTH, "8-15 min", lowerBound = 8.0, upperBound = 15.0),
        KpiTarget(REWARDED_OPT_IN, ">= 30% of eligible players", lowerBound = 30.0, upperBound = null)
    )

    fun target(kpi: String): KpiTarget? =
        ALL.firstOrNull { it.kpi.equals(kpi.trim(), ignoreCase = true) }

    fun crashFree(): KpiTarget = target(CRASH_FREE) ?: ALL.first()

    fun anr(): KpiTarget = target(ANR) ?: ALL.first()

    /** The whole board for one reporting period: the numbers and what missed. */
    fun misses(measured: Map<String, Double>): List<KpiTarget> =
        ALL.filter { target -> measured[target.kpi]?.let { !target.passes(it) } ?: false }

    fun passesAll(measured: Map<String, Double>): Boolean = misses(measured).isEmpty()
}
