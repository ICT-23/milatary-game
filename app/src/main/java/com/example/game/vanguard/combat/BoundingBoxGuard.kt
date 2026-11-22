package com.example.game.vanguard.combat

/**
 * Section 3.1 gate 6: the forward capsule check that keeps barrels, optics and
 * head hitboxes out of walls, desks and ballistic shields.
 *
 * The guard runs every frame, not just on fire, so the compressed-ready pose is
 * already blended in by the time the operator presses the trigger.
 */
data class ClearanceProbe(
    /** Distances to nearby geometry along the weapon's forward axis. */
    val obstacleDistancesMeters: List<Float>,
    val weaponOverallLengthMeters: Float,
    val capsuleRadiusMeters: Float = 0.22f
) {
    val nearestObstacleMeters: Float
        get() = obstacleDistancesMeters.filter { it.isFinite() }.minOrNull() ?: Float.MAX_VALUE

    /** Half the weapon plus the capsule fat, which is the space a pose needs. */
    val requiredEnvelopeMeters: Float
        get() = weaponOverallLengthMeters.coerceAtLeast(0f) * 0.5f + capsuleRadiusMeters.coerceAtLeast(0f)
}

data class ClearanceResult(
    val nearestObstacleMeters: Float,
    val envelopeMeters: Float,
    val muzzleClearanceMeters: Float,
    val weaponCompressed: Boolean,
    val compressedReadyBlend: Float,
    val canFire: Boolean
)

/** Poses that must never overlap; the transition table below enforces it. */
enum class PoseAction {
    IDLE,
    SPRINT,
    PRONE_TRANSITION,
    MELEE_ATTACK,
    LEAN,
    RELOAD
}

object BoundingBoxGuard {

    private val ILLEGAL_PAIRS: Set<Set<PoseAction>> = setOf(
        setOf(PoseAction.PRONE_TRANSITION, PoseAction.MELEE_ATTACK),
        setOf(PoseAction.PRONE_TRANSITION, PoseAction.LEAN),
        setOf(PoseAction.PRONE_TRANSITION, PoseAction.SPRINT)
    )

    fun evaluate(probe: ClearanceProbe): ClearanceResult {
        val nearest = probe.nearestObstacleMeters
        val envelope = probe.requiredEnvelopeMeters
        val clearance = if (nearest == Float.MAX_VALUE) Float.MAX_VALUE else nearest - envelope
        val compressed = clearance <= 0f
        val blend = if (envelope <= 0f) 0f else ((envelope - nearest) / envelope).coerceIn(0f, 1f)
        return ClearanceResult(
            nearestObstacleMeters = nearest,
            envelopeMeters = envelope,
            muzzleClearanceMeters = clearance,
            weaponCompressed = compressed,
            compressedReadyBlend = if (compressed) blend.coerceAtLeast(0.05f) else 0f,
            canFire = !compressed
        )
    }

    /** Barrels retracted into compressed ready cannot discharge until clear. */
    fun applyTo(state: MechanicalState, clearance: ClearanceResult): MechanicalState =
        state.copy(isMuzzleObstructedByWall = !clearance.canFire)

    /**
     * State-machine guard: prone transitions, melee swings, leaning and
     * sprinting are mutually exclusive so no animation can clip the floor or
     * push a hitbox through geometry.
     */
    fun transitionAllowed(current: PoseAction, next: PoseAction): Boolean {
        if (current == next) return true
        return ILLEGAL_PAIRS.none { it.contains(current) && it.contains(next) }
    }

    /** Convenience for an animation state machine that holds two pending actions. */
    fun firstIllegalPair(pending: List<PoseAction>): Set<PoseAction>? =
        ILLEGAL_PAIRS.firstOrNull { pair ->
            pending.containsAll(pair)
        }
}
