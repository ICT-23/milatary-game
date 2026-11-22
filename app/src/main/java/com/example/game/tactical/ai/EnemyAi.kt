package com.example.game.tactical.ai

/**
 * Section 5.2, Enemy AI Finite State Machine.
 *
 *     PATROL -> ALERT -> SEEK_COVER -> ENGAGE -> FLANK / SUPPRESS
 *
 * The same aiming rule the player obeys applies here: an AI that is facing away,
 * reloading, suppressed or blocked by cover cannot deal damage no matter how
 * close the player is.
 */
enum class AiState(val displayName: String) {
    PATROL("Patrol"),
    ALERT("Alert"),
    SEEK_COVER("Seek Cover"),
    ENGAGE("Engage"),
    FLANK("Flank"),
    SUPPRESS("Suppress")
}

enum class AiEvent {
    HEARS_GUNFIRE,
    HEARS_SUPPRESSED_GUNFIRE,
    SEES_PLAYER,
    LOST_SIGHT,
    TOOK_FIRE,
    COVER_REACHED,
    SUPPRESSED,
    RECEIVED_FLANK_ORDER,
    RECEIVED_SUPPRESS_ORDER,
    CALM_TIMEOUT
}

data class AiContext(
    val playerInVisionCone: Boolean,
    val hasClearLineOfSight: Boolean,
    val isFacingPlayer: Boolean,
    val muzzleAngleToPlayerDegrees: Float,
    val isReloading: Boolean,
    val isSuppressed: Boolean,
    val coverAvailable: Boolean,
    val distanceToPlayerMeters: Float
)

data class AiBrain(
    val state: AiState = AiState.PATROL,
    val alertness: Int = 0
) {
    val isAggressive: Boolean get() = state == AiState.ENGAGE || state == AiState.FLANK
}

object EnemyAi {

    const val MAX_MUZZLE_ANGLE_DEGREES = 35f
    const val MAX_ENGAGE_RANGE_METERS = 220f
    const val ALERT_GAIN = 40

    /**
     * Pure transition. An event that the current state cannot use leaves the
     * brain untouched instead of dropping into an undefined state.
     */
    fun transition(brain: AiBrain, event: AiEvent, context: AiContext): AiBrain {
        val next = when (brain.state) {
            AiState.PATROL -> when (event) {
                AiEvent.HEARS_GUNFIRE, AiEvent.SEES_PLAYER -> AiState.ALERT
                else -> AiState.PATROL
            }
            AiState.ALERT -> when {
                event == AiEvent.CALM_TIMEOUT -> AiState.PATROL
                context.playerInVisionCone && context.coverAvailable -> AiState.SEEK_COVER
                context.playerInVisionCone -> AiState.ENGAGE
                event == AiEvent.LOST_SIGHT -> AiState.PATROL
                else -> AiState.ALERT
            }
            AiState.SEEK_COVER -> when (event) {
                AiEvent.COVER_REACHED -> AiState.ENGAGE
                AiEvent.TOOK_FIRE -> AiState.SEEK_COVER
                AiEvent.CALM_TIMEOUT -> AiState.ALERT
                else -> AiState.SEEK_COVER
            }
            AiState.ENGAGE -> when (event) {
                AiEvent.TOOK_FIRE, AiEvent.SUPPRESSED -> AiState.SEEK_COVER
                AiEvent.LOST_SIGHT -> AiState.SUPPRESS
                AiEvent.RECEIVED_FLANK_ORDER -> AiState.FLANK
                AiEvent.RECEIVED_SUPPRESS_ORDER -> AiState.SUPPRESS
                else -> AiState.ENGAGE
            }
            AiState.SUPPRESS -> when (event) {
                AiEvent.SEES_PLAYER -> AiState.ENGAGE
                AiEvent.TOOK_FIRE -> AiState.SEEK_COVER
                AiEvent.CALM_TIMEOUT -> AiState.ALERT
                else -> AiState.SUPPRESS
            }
            AiState.FLANK -> when (event) {
                AiEvent.SEES_PLAYER -> AiState.ENGAGE
                AiEvent.SUPPRESSED -> AiState.SEEK_COVER
                AiEvent.CALM_TIMEOUT -> AiState.ALERT
                else -> AiState.FLANK
            }
        }
        val alertness = when {
            next == AiState.PATROL -> (brain.alertness - 20).coerceAtLeast(0)
            next == AiState.ALERT -> (brain.alertness + ALERT_GAIN).coerceAtMost(100)
            else -> 100
        }
        return AiBrain(state = next, alertness = alertness)
    }

    /** An unsuppressed shot is heard; a suppressed shot is not, per Section 4.1. */
    fun eventForGunshot(isSuppressed: Boolean): AiEvent =
        if (isSuppressed) AiEvent.HEARS_SUPPRESSED_GUNFIRE else AiEvent.HEARS_GUNFIRE

    /**
     * The AI cannot damage the player unless every physical condition holds:
     * it is engaging, facing the player, not reloading, not suppressed, inside
     * range, and its muzzle points at a visible hitbox through clear air.
     */
    fun mayDamagePlayer(brain: AiBrain, context: AiContext): Boolean {
        if (!brain.isAggressive) return false
        if (!context.isFacingPlayer) return false
        if (context.isReloading) return false
        if (context.isSuppressed) return false
        if (context.distanceToPlayerMeters > MAX_ENGAGE_RANGE_METERS) return false
        if (!context.hasClearLineOfSight) return false
        if (!context.playerInVisionCone) return false
        return kotlin.math.abs(context.muzzleAngleToPlayerDegrees) <= MAX_MUZZLE_ANGLE_DEGREES
    }

    fun denialReason(brain: AiBrain, context: AiContext): String? = when {
        !brain.isAggressive -> "AI is ${brain.state.displayName.lowercase()}, not engaging"
        !context.isFacingPlayer -> "AI is facing away"
        context.isReloading -> "AI is reloading"
        context.isSuppressed -> "AI is suppressed behind cover"
        context.distanceToPlayerMeters > MAX_ENGAGE_RANGE_METERS -> "Player outside engage range"
        !context.hasClearLineOfSight -> "No clear raycast to a visible hitbox"
        !context.playerInVisionCone -> "Player outside the vision cone"
        kotlin.math.abs(context.muzzleAngleToPlayerDegrees) > MAX_MUZZLE_ANGLE_DEGREES ->
            "Muzzle ${context.muzzleAngleToPlayerDegrees} deg off target"
        else -> null
    }
}
