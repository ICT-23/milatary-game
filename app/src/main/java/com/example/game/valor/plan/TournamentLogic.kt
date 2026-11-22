package com.example.game.valor.plan

/** Section 5.15: the three pass thresholds. The scores themselves are mission data. */
enum class TournamentTier(val displayName: String) {
    BRONZE("Bronze"),
    SILVER("Silver"),
    GOLD("Gold")
}

/** Section 5.15: the threshold table supplied by the tournament data asset. */
data class TournamentThresholds(val bronze: Int, val silver: Int, val gold: Int) {

    /** Highest tier reached, or null when even bronze was missed. */
    fun tierFor(score: Int): TournamentTier? = when {
        score >= gold -> TournamentTier.GOLD
        score >= silver -> TournamentTier.SILVER
        score >= bronze -> TournamentTier.BRONZE
        else -> null
    }
}

/** Section 5.15: the score multiplier of one target hit. */
data class TargetScore(val headshot: Boolean, val centerMass: Boolean)

/**
 * Section 5.15: a tournament run. The timer only starts once 3-2-1 finishes,
 * pauses stop it, and failing never blocks the story.
 */
class TournamentRun(
    val baseTimeSeconds: Float,
    val rankTimeScale: Float,
    val thresholds: TournamentThresholds
) {
    var countdownRemaining: Float = TournamentRules.COUNTDOWN_SECONDS
        private set

    var elapsedSeconds: Float = 0f
        private set

    var running: Boolean = false
        private set

    var paused: Boolean = false
        private set

    var score: Int = 0
        private set

    val timeLimitSeconds: Float = TournamentRules.timeLimit(baseTimeSeconds, rankTimeScale)

    fun registerTarget(target: TargetScore, headshotMultiplier: Float, centerMultiplier: Float) {
        if (!running || paused) return
        score += TournamentRules.score(listOf(target), headshotMultiplier, centerMultiplier)
    }

    val countdownFinished: Boolean get() = countdownRemaining <= 0f

    val timedOut: Boolean get() = elapsedSeconds >= timeLimitSeconds

    fun tick(deltaSeconds: Float) {
        if (countdownRemaining > 0f) {
            countdownRemaining = (countdownRemaining - deltaSeconds).coerceAtLeast(0f)
            if (countdownRemaining <= 0f) running = true
            return
        }
        if (!running || paused) return
        elapsedSeconds += deltaSeconds
        if (timedOut) running = false
    }

    fun pause() {
        paused = true
    }

    fun resume() {
        paused = false
    }

    fun finish(): TournamentTier? = thresholds.tierFor(score)
}

object TournamentRules {

    const val COUNTDOWN_SECONDS = 3f
    const val PLAYTEST_PASS_TARGET = 0.80f
    const val RETRY_ALLOWED = true
    const val BLOCKS_STORY_PROGRESSION = false
    const val PAUSE_STOPS_TIMER = true

    /** "Time limit = baseTime * difficultyScale[rank]". */
    fun timeLimit(baseTimeSeconds: Float, rankScale: Float): Float =
        baseTimeSeconds * rankScale.coerceAtLeast(0f)

    /** "Score = hits x multiplier (headshot/center bonus)." */
    fun score(hits: Int, multiplier: Float): Int =
        (hits.coerceAtLeast(0) * multiplier.coerceAtLeast(0f)).toInt()

    fun score(targets: List<TargetScore>, headshotMultiplier: Float, centerMultiplier: Float): Int =
        targets.sumOf { target ->
            val multiplier = when {
                target.headshot -> headshotMultiplier
                target.centerMass -> centerMultiplier
                else -> 1f
            }
            score(1, multiplier)
        }

    /** "Targets cannot spawn off-screen or behind obstructions." */
    fun validSpawn(onScreen: Boolean, obstructed: Boolean): Boolean = onScreen && !obstructed

    /** "Playtest target: 80% pass rate" for the first tournament. */
    fun meetsPassTarget(passes: Int, attempts: Int): Boolean =
        attempts > 0 && passes.toFloat() / attempts >= PLAYTEST_PASS_TARGET

    fun retryImmediately(): Boolean = RETRY_ALLOWED

    fun blocksStory(): Boolean = BLOCKS_STORY_PROGRESSION

    fun pauseStopsTimer(): Boolean = PAUSE_STOPS_TIMER
}
