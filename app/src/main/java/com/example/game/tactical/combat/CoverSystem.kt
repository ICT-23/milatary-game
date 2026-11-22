package com.example.game.tactical.combat

import com.example.game.combat.Vec3
import com.example.game.tactical.movement.Stance

/**
 * Section 5.2, contextual 3D cover.
 *
 * Cover never grants damage immunity. It only hides the hitboxes that are
 * physically behind the obstacle mesh; peeking or aiming moves the head and
 * thorax back into the open, and the ballistic resolver then treats them like
 * any other exposed target.
 */
enum class CoverKind(val displayName: String, val minHeightMeters: Float) {
    LOW("Low Cover (Crouch)", 0.50f),
    HIGH("High Cover (Standing Corner Peek)", 1.15f)
}

/** One measured surface in front of the character, supplied by the physics query. */
data class CoverProbe(
    val obstacleId: String,
    val distanceMeters: Float,
    val heightMeters: Float,
    val normal: Vec3
)

data class CoverState(
    val obstacleId: String? = null,
    val kind: CoverKind? = null,
    val normal: Vec3 = Vec3.Zero,
    val isPeeking: Boolean = false
) {
    val isInCover: Boolean get() = obstacleId != null && kind != null
}

object CoverSystem {

    const val MAX_SNAP_DISTANCE_METERS = 1.6f

    /** The nearest surface inside snap distance that is tall enough to lean on. */
    fun detect(probes: List<CoverProbe>): CoverState {
        val best = probes
            .filter { it.distanceMeters in 0f..MAX_SNAP_DISTANCE_METERS }
            .filter { it.heightMeters >= CoverKind.LOW.minHeightMeters }
            .minByOrNull { it.distanceMeters }
            ?: return CoverState()
        return CoverState(
            obstacleId = best.obstacleId,
            kind = classify(best.heightMeters),
            normal = best.normal.normalized(),
            isPeeking = false
        )
    }

    fun classify(heightMeters: Float): CoverKind? = when {
        heightMeters >= CoverKind.HIGH.minHeightMeters -> CoverKind.HIGH
        heightMeters >= CoverKind.LOW.minHeightMeters -> CoverKind.LOW
        else -> null
    }

    /** Aiming or peeking is what pushes the upper body out of cover. */
    fun peek(state: CoverState, aimingOrPeeking: Boolean): CoverState =
        if (!state.isInCover) state else state.copy(isPeeking = aimingOrPeeking)

    /**
     * Movement is pinned to the cover plane: the component along the wall normal
     * is removed so the character slides along the surface instead of through it.
     */
    fun lockedDirection(intent: Vec3, normal: Vec3): Vec3 {
        val unitNormal = normal.normalized()
        if (unitNormal.length() <= 0.0001f) return intent
        return intent - unitNormal * intent.dot(unitNormal)
    }

    /** True when the character is close enough for the engine to snap the mesh to cover. */
    fun canSnap(state: CoverState, closingSpeedMetersPerSecond: Float): Boolean =
        state.isInCover && closingSpeedMetersPerSecond > 0.5f

    /**
     * Hitboxes hidden by the obstacle in this exact pose. Everything not listed
     * here is a valid target for the next raycast.
     */
    fun concealed(state: CoverState, stance: Stance): Set<HitboxComponent> {
        if (!state.isInCover) return emptySet()
        if (state.isPeeking) return emptySet()
        return when (state.kind) {
            CoverKind.LOW -> when (stance) {
                Stance.PRONE -> setOf(HitboxComponent.HEAD, HitboxComponent.THORAX, HitboxComponent.STOMACH)
                Stance.CROUCHING -> setOf(HitboxComponent.HEAD, HitboxComponent.THORAX)
                Stance.STANDING -> emptySet()
            }
            CoverKind.HIGH -> setOf(HitboxComponent.HEAD, HitboxComponent.THORAX)
            null -> emptySet()
        }
    }

    fun exposed(state: CoverState, stance: Stance): Set<HitboxComponent> =
        HitboxComponent.entries.toSet() - concealed(state, stance)

    fun isExposed(state: CoverState, stance: Stance, component: HitboxComponent): Boolean =
        component in exposed(state, stance)
}
