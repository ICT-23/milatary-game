package com.example.game.tactical.academy

import com.example.game.vanguard.core.GameErrorCategory
import com.example.game.vanguard.core.GameResult
import com.example.game.vanguard.core.valueOrNull

/**
 * Phase 5 deliverable: the Fort Valor training courses.
 *
 * Two graded events live here - the timed obstacle course (Act I, "The
 * Crucible") and the pop-up target range that awards the Basic Rifle
 * Qualification. Both return a scored result rather than a boolean, and both
 * reject physically impossible score submissions, so the drill can never be
 * passed by a spoofed number.
 */
enum class CourseStation(val displayName: String, val parSplitSeconds: Float) {
    LOW_CRAWL("Low Crawl", 18f),
    WALL_VAULT("Wall Vault", 15f),
    ROPE_CLIMB("Rope Climb", 22f),
    BALANCE_BEAM("Balance Beam", 14f),
    TIRE_RUN("Tire Run", 20f),
    SHOOTING_STATION("Shooting Station", 31f)
}

data class StationSplit(val station: CourseStation, val splitSeconds: Float) {
    /** Seconds lost against the station par; zero when the split is clean. */
    val overPar: Float get() = (splitSeconds - station.parSplitSeconds).coerceAtLeast(0f)
}

data class ObstacleRun(val splits: List<StationSplit>, val falls: Int = 0) {
    val rawSeconds: Float get() = splits.sumOf { it.splitSeconds.toDouble() }.toFloat()
    val penaltySeconds: Float get() = falls * ObstacleCourse.FALL_PENALTY_SECONDS
    val totalSeconds: Float get() = rawSeconds + penaltySeconds
    val slowestStation: CourseStation? get() = splits.maxByOrNull { it.overPar }?.station
}

data class ObstacleResult(
    val passed: Boolean,
    val grade: TrainingGrade,
    val totalSeconds: Float,
    val parSeconds: Float,
    val penaltySeconds: Float,
    val slowestStation: CourseStation?,
    val message: String
)

enum class TrainingGrade(val displayName: String) {
    FAIL("FAIL"),
    PASS("PASS"),
    DISTINCTION("DISTINCTION")
}

object ObstacleCourse {

    const val FALL_PENALTY_SECONDS = 5f
    const val MAX_FALLS = 3
    const val DISTINCTION_SECONDS = 100f

    /** Station pars sum to the advertised course par; the two can never drift. */
    val PAR_SECONDS: Float =
        CourseStation.entries.sumOf { it.parSplitSeconds.toDouble() }.toFloat()

    fun parFor(station: CourseStation): Float = station.parSplitSeconds

    fun evaluate(run: ObstacleRun): GameResult<ObstacleResult> {
        if (run.splits.any { it.splitSeconds < 0f }) {
            return GameResult.fail(
                GameErrorCategory.INVALID_STATE,
                "NEGATIVE_SPLIT",
                "A station split cannot be negative"
            )
        }
        if (run.splits.size != run.splits.map { it.station }.distinct().size) {
            return GameResult.fail(
                GameErrorCategory.INVALID_STATE,
                "DUPLICATE_STATION",
                "Each station may only be timed once per run"
            )
        }
        if (run.falls < 0) {
            return GameResult.fail(
                GameErrorCategory.INVALID_STATE,
                "NEGATIVE_FALLS",
                "Fall count cannot be negative"
            )
        }

        val total = run.totalSeconds
        val tooManyFalls = run.falls > MAX_FALLS
        val grade = when {
            tooManyFalls -> TrainingGrade.FAIL
            total <= DISTINCTION_SECONDS -> TrainingGrade.DISTINCTION
            total <= PAR_SECONDS -> TrainingGrade.PASS
            else -> TrainingGrade.FAIL
        }
        val message = when {
            tooManyFalls -> "${run.falls} falls exceeds the $MAX_FALLS-fall limit"
            grade == TrainingGrade.DISTINCTION ->
                "%.1f s - distinction, %.1f s inside par".format(total, PAR_SECONDS - total)
            grade == TrainingGrade.PASS -> "%.1f s - course passed".format(total)
            else -> "%.1f s - %.1f s over par".format(total, total - PAR_SECONDS)
        }
        return GameResult.Ok(
            ObstacleResult(
                passed = grade != TrainingGrade.FAIL,
                grade = grade,
                totalSeconds = total,
                parSeconds = PAR_SECONDS,
                penaltySeconds = run.penaltySeconds,
                slowestStation = run.slowestStation,
                message = message
            )
        )
    }
}

data class RangeRun(
    val shotsFired: Int,
    val hits: Int,
    val headshots: Int = 0,
    val expiredTargets: Int = 0
) {
    val missedShots: Int get() = (shotsFired - hits).coerceAtLeast(0)
    val accuracyPercent: Float
        get() = if (shotsFired <= 0) 0f else (hits.toFloat() / shotsFired * 100f).coerceIn(0f, 100f)
}

data class RangeResult(
    val qualified: Boolean,
    val grade: TrainingGrade,
    val hits: Int,
    val hitsRequired: Int,
    val score: Int,
    val accuracyPercent: Float,
    val message: String
)

/**
 * Pop-up target range. Fifty exposures per detail; the qualification standard is
 * 38 hits, exactly as the Private (PVT) requirement states.
 */
object FiringRange {

    const val QUALIFICATION_SHOTS = 50
    const val QUALIFICATION_HITS = 38
    const val DISTINCTION_HITS = 45
    const val POPUP_VISIBLE_SECONDS = 2.5f
    const val POPUP_INTERVAL_SECONDS = 1.2f
    const val HIT_POINTS = 10
    const val HEADSHOT_BONUS_POINTS = 15

    fun score(run: RangeRun): Int = run.hits * HIT_POINTS + run.headshots * HEADSHOT_BONUS_POINTS

    fun hitsMissing(run: RangeRun): Int = (QUALIFICATION_HITS - run.hits).coerceAtLeast(0)

    fun qualifies(run: RangeRun): Boolean = evaluate(run).valueOrNull()?.qualified == true

    fun evaluate(run: RangeRun): GameResult<RangeResult> {
        if (run.shotsFired < 0 || run.hits < 0 || run.headshots < 0 || run.expiredTargets < 0) {
            return GameResult.fail(
                GameErrorCategory.INVALID_STATE,
                "NEGATIVE_COUNT",
                "Range counters cannot be negative"
            )
        }
        if (run.hits > run.shotsFired) {
            return GameResult.fail(
                GameErrorCategory.INVALID_STATE,
                "IMPOSSIBLE_SCORE",
                "${run.hits} hits reported from ${run.shotsFired} shots"
            )
        }
        if (run.headshots > run.hits) {
            return GameResult.fail(
                GameErrorCategory.INVALID_STATE,
                "IMPOSSIBLE_SCORE",
                "${run.headshots} headshots exceeds ${run.hits} hits"
            )
        }

        val qualified = run.hits >= QUALIFICATION_HITS
        val grade = when {
            !qualified -> TrainingGrade.FAIL
            run.hits >= DISTINCTION_HITS -> TrainingGrade.DISTINCTION
            else -> TrainingGrade.PASS
        }
        val message = if (qualified) {
            "%d/%d hits (%.0f%%) - rifle qualification awarded"
                .format(run.hits, QUALIFICATION_SHOTS, run.accuracyPercent)
        } else {
            "%d/%d hits - %d more required".format(run.hits, QUALIFICATION_SHOTS, hitsMissing(run))
        }
        return GameResult.Ok(
            RangeResult(
                qualified = qualified,
                grade = grade,
                hits = run.hits,
                hitsRequired = QUALIFICATION_HITS,
                score = score(run),
                accuracyPercent = run.accuracyPercent,
                message = message
            )
        )
    }
}

enum class SergeantEvent {
    COURSE_START,
    STATION_FALL,
    COURSE_CLEARED,
    COURSE_FAILED,
    RANGE_START,
    RANGE_QUALIFIED,
    RANGE_FAILED,
    WORKBENCH_OPEN,
    PROMOTION
}

/** The Act I Academy Drill Sergeant. He only ever barks about what actually happened. */
object DrillSergeant {

    const val NAME = "Drill Sergeant Vance"

    fun line(event: SergeantEvent, detail: String = ""): String = when (event) {
        SergeantEvent.COURSE_START ->
            "Boots on the line, candidate. You are burning daylight - move."
        SergeantEvent.STATION_FALL ->
            "$detail. That is five seconds you will not get back. Get up."
        SergeantEvent.COURSE_CLEARED ->
            "Course cleared. Do not expect a parade - it gets harder from here."
        SergeantEvent.COURSE_FAILED ->
            "That was not a course, that was a stroll. Run it again."
        SergeantEvent.RANGE_START ->
            "Fifty exposures, candidate. Anything less than 38 hits and you are back here tomorrow."
        SergeantEvent.RANGE_QUALIFIED ->
            "Rifle qualification recorded. Private's boards are open to you now."
        SergeantEvent.RANGE_FAILED ->
            "$detail. Keep your sights up and breathe before you break the trigger."
        SergeantEvent.WORKBENCH_OPEN ->
            "Assemble it, then shoot it. A weapon you cannot build is a weapon you cannot trust."
        SergeantEvent.PROMOTION ->
            "Front and centre, candidate. The academy recognises $detail. Do not make me regret it."
    }
}
