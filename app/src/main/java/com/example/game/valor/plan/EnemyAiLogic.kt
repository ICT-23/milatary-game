package com.example.game.valor.plan

/** Section 5.10 states, in the order the document lists them. */
enum class CombatAiState(val displayName: String) {
    IDLE_PATROL("Idle/Patrol"),
    SUSPICIOUS("Suspicious"),
    ALERT("Alert"),
    COMBAT("Combat"),
    FLANK("Flank"),
    TAKE_COVER("TakeCover"),
    RETREAT_HEAL("Retreat/Heal"),
    DEAD("Dead")
}

/** Section 5.10 stimuli that move an AI between states. */
enum class AiStimulus(val displayName: String) {
    HEARD_NOISE("Heard a noise"),
    SAW_TARGET("Target in the cone with line of sight"),
    LOST_TARGET("Lost sight"),
    TOOK_DAMAGE("Took damage"),
    LOW_HEALTH_WITH_MEDIC("Below 30% with a medic"),
    SQUAD_LOSS_HIGH("Squad lost more than 50%"),
    PLAYER_IN_COVER_LONG("Player stayed in cover"),
    KILLED("Killed")
}

data class AiTransition(val from: CombatAiState, val on: AiStimulus, val to: CombatAiState)

/** What the perception step observed this tick. */
data class Perception(
    val insideCone: Boolean,
    val losClear: Boolean,
    val smokeBlocked: Boolean = false,
    val targetAlive: Boolean = true
)

/** Section 5.10 "accuracy grows with time-in-sight, decreases with movement/distance/cover". */
data class AimErrorInputs(
    val baseErrorDegrees: Float,
    val secondsInSight: Float,
    val targetMoving: Boolean,
    val distanceMeters: Float,
    val targetInCover: Boolean
)

/**
 * Section 5.10: perception, memory, alerting, reaction and the combat decision
 * rules. An AI shot is resolved by the same `HitDetection` as the player's, so a
 * miss is a miss.
 */
object EnemyAiRules {

    const val VISION_CONE_DEGREES = 90f
    const val VISION_RANGE_METERS = 40f
    const val MEMORY_SECONDS = 8f
    const val ALERT_RADIUS_METERS = 20f
    const val ALERT_DELAY_MIN_SECONDS = 1f
    const val ALERT_DELAY_MAX_SECONDS = 2f
    const val REACTION_MIN_SECONDS = 0.3f
    const val REACTION_MAX_SECONDS = 0.8f
    const val RETREAT_HEALTH_FRACTION = 0.30f
    const val SQUAD_FALLBACK_LOSS_FRACTION = 0.50f
    const val FAR_UPDATE_HZ = 10f
    const val AI_MISSES_DEAL_DAMAGE = false

    /** Section 8 "active AI in combat" per quality tier: Low 4, Medium 6, High 8. */
    val ACTIVE_AI_BY_TIER: List<Int> = listOf(4, 6, 8)

    /** The eight states form this transition table; anything else keeps the state. */
    val TRANSITIONS: List<AiTransition> = listOf(
        AiTransition(CombatAiState.IDLE_PATROL, AiStimulus.HEARD_NOISE, CombatAiState.SUSPICIOUS),
        AiTransition(CombatAiState.IDLE_PATROL, AiStimulus.SAW_TARGET, CombatAiState.ALERT),
        AiTransition(CombatAiState.SUSPICIOUS, AiStimulus.SAW_TARGET, CombatAiState.ALERT),
        AiTransition(CombatAiState.SUSPICIOUS, AiStimulus.LOST_TARGET, CombatAiState.IDLE_PATROL),
        AiTransition(CombatAiState.ALERT, AiStimulus.SAW_TARGET, CombatAiState.COMBAT),
        AiTransition(CombatAiState.ALERT, AiStimulus.LOST_TARGET, CombatAiState.SUSPICIOUS),
        AiTransition(CombatAiState.COMBAT, AiStimulus.PLAYER_IN_COVER_LONG, CombatAiState.FLANK),
        AiTransition(CombatAiState.COMBAT, AiStimulus.TOOK_DAMAGE, CombatAiState.TAKE_COVER),
        AiTransition(CombatAiState.FLANK, AiStimulus.SAW_TARGET, CombatAiState.COMBAT),
        AiTransition(CombatAiState.TAKE_COVER, AiStimulus.LOW_HEALTH_WITH_MEDIC, CombatAiState.RETREAT_HEAL),
        AiTransition(CombatAiState.TAKE_COVER, AiStimulus.SQUAD_LOSS_HIGH, CombatAiState.IDLE_PATROL),
        AiTransition(CombatAiState.RETREAT_HEAL, AiStimulus.SAW_TARGET, CombatAiState.COMBAT)
    )

    fun next(state: CombatAiState, stimulus: AiStimulus): CombatAiState {
        if (state == CombatAiState.DEAD) return CombatAiState.DEAD
        if (stimulus == AiStimulus.KILLED) return CombatAiState.DEAD
        return TRANSITIONS.firstOrNull { it.from == state && it.on == stimulus }?.to ?: state
    }

    fun isReachable(target: CombatAiState): Boolean =
        target == CombatAiState.IDLE_PATROL ||
            TRANSITIONS.any { it.to == target } ||
            target == CombatAiState.DEAD

    /** Fog or night shortens the cone; the factor is data, the direction is the rule. */
    fun effectiveVisionRange(baseRangeMeters: Float, visibilityMultiplier: Float): Float =
        baseRangeMeters * visibilityMultiplier.coerceAtLeast(0f)

    /** Seen only inside the cone AND with an unblocked ray to head/chest AND no smoke. */
    fun sees(perception: Perception): Boolean =
        perception.insideCone &&
            perception.losClear &&
            !perception.smokeBlocked &&
            perception.targetAlive

    /** "Keeps the last known position for 8 s after losing sight." */
    fun remembers(secondsSinceLastSeen: Float): Boolean = secondsSinceLastSeen <= MEMORY_SECONDS

    fun alertDelay(seconds: Float): Float =
        seconds.coerceIn(ALERT_DELAY_MIN_SECONDS, ALERT_DELAY_MAX_SECONDS)

    fun alertsAllies(distanceMeters: Float): Boolean = distanceMeters <= ALERT_RADIUS_METERS

    /** "First-shot delay = 0.3-0.8 s so the player is never instantly shot." */
    fun reactionDelay(seconds: Float): Float =
        seconds.coerceIn(REACTION_MIN_SECONDS, REACTION_MAX_SECONDS)

    fun canFire(reactionElapsedSeconds: Float, reactionSeconds: Float): Boolean =
        reactionElapsedSeconds >= reactionSeconds

    /**
     * Spread shrinks with time in sight and grows with a moving target, distance
     * and cover. The penalty rates are tuning data; the signs are the rule.
     */
    fun aimErrorDegrees(
        inputs: AimErrorInputs,
        tighteningPerSecond: Float,
        movementPenalty: Float,
        distancePenaltyPerMeter: Float,
        coverPenalty: Float
    ): Float {
        var error = inputs.baseErrorDegrees - tighteningPerSecond * inputs.secondsInSight
        if (inputs.targetMoving) error += movementPenalty
        error += distancePenaltyPerMeter * inputs.distanceMeters
        if (inputs.targetInCover) error += coverPenalty
        return error.coerceAtLeast(0f)
    }

    fun retreats(healthFraction: Float, hasMedic: Boolean): Boolean =
        healthFraction < RETREAT_HEALTH_FRACTION && hasMedic

    fun fallsBack(squadLossFraction: Float): Boolean =
        squadLossFraction > SQUAD_FALLBACK_LOSS_FRACTION

    /** "Flank: if player has been in cover > N s, 1-2 enemies path to a flank point." */
    fun flankers(secondsPlayerInCover: Float, thresholdSeconds: Float): List<Int> =
        if (secondsPlayerInCover > thresholdSeconds) listOf(1, 2) else emptyList()

    /** "Stagger AI updates (10 Hz for far)." */
    fun updateHz(isFar: Boolean, nearHz: Float = 60f): Float = if (isFar) FAR_UPDATE_HZ else nearHz

    /** "Max 6-8 active combat AIs; others in cheap states." */
    fun activeAiCap(tierIndex: Int): Int = ACTIVE_AI_BY_TIER[tierIndex.coerceIn(0, ACTIVE_AI_BY_TIER.lastIndex)]

    /** "Stuck detection: re-path or teleport after 5 s out of sight." */
    fun shouldRecoverFromStuck(stuckSeconds: Float, outOfSight: Boolean): Boolean =
        outOfSight && stuckSeconds >= 5f
}
