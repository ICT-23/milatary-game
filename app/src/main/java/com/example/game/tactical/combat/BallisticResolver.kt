package com.example.game.tactical.combat

import com.example.game.combat.BallisticsEngine
import com.example.game.combat.Vec3
import kotlin.random.Random

/**
 * Section 5.1 items 1 and 2: the shot vector and the physical raycast.
 *
 * A shot starts at MuzzleTransform.position, never at the camera. The ray is
 * stopped by any solid obstacle closer than the target, and gravity is modelled
 * as a real drop over the projectile's flight time - not as a hit chance.
 */
data class MuzzleTransform(val position: Vec3, val forward: Vec3)

/** One body part of one actor. Radius is the collider sphere for that segment. */
data class HitboxTarget(
    val bodyId: String,
    val center: Vec3,
    val radius: Float,
    val component: HitboxComponent,
    val target: TargetProfile = TargetProfile(),
    val isSniperRifle: Boolean = false
)

data class Obstacle(
    val obstacleId: String,
    val center: Vec3,
    val radius: Float,
    val penetrable: Boolean = false,
    val material: String = "CONCRETE"
)

data class ShotRequest(
    val muzzle: MuzzleTransform,
    val aimPoint: Vec3,
    val isAimingDownSights: Boolean,
    val spreadDegrees: Float = 0f,
    val swayDegrees: Float = 0f,
    val recoilDegrees: Float = 0f,
    val hipAccuracyBonusPercent: Int = 0,
    val muzzleVelocityMps: Float = 880f,
    val bulletDropEnabled: Boolean = true
)

data class ShotOutcome(
    val shotVector: Vec3,
    val aimPointUsed: Vec3,
    val hitTarget: HitboxTarget?,
    val blockedBy: String?,
    val distanceMeters: Float,
    val dropMeters: Float,
    val penetratedObstacleId: String?
) {
    val isBlocked: Boolean get() = blockedBy != null
    val didHit: Boolean get() = hitTarget != null
}

object BallisticResolver {

    const val GRAVITY_MPS2 = 9.81f

    /** Section 5.1: hip-fire widens the cone; a laser sight narrows it again. */
    fun effectiveHipCone(spreadDegrees: Float, hipAccuracyBonusPercent: Int): Float {
        val bonus = hipAccuracyBonusPercent.coerceIn(0, 100) / 100f
        return (spreadDegrees * (1f - bonus)).coerceAtLeast(0f)
    }

    /** Time of flight for a flat shot at [distanceMeters]. */
    fun flightSeconds(distanceMeters: Float, muzzleVelocityMps: Float): Float =
        distanceMeters / muzzleVelocityMps.coerceAtLeast(1f)

    /** Real gravity drop: 0.5 * g * t^2, in metres. */
    fun dropMeters(distanceMeters: Float, muzzleVelocityMps: Float): Float {
        val t = flightSeconds(distanceMeters, muzzleVelocityMps)
        return 0.5f * GRAVITY_MPS2 * t * t
    }

    /**
     * ADS aligns the weapon to the camera ray (modified by sway and recoil);
     * hip-fire samples inside the dynamic cone. Either way the vector starts at
     * the muzzle, not at the reticle.
     */
    fun shotVector(request: ShotRequest, random: Random = Random.Default): Vec3 {
        val cameraDirection = (request.aimPoint - request.muzzle.position).normalized()
        if (request.isAimingDownSights) {
            val cone = (request.swayDegrees + request.recoilDegrees).coerceAtLeast(0f)
            return BallisticsEngine.applySpread(cameraDirection, cone, random)
        }
        val cone = effectiveHipCone(request.spreadDegrees, request.hipAccuracyBonusPercent)
        return BallisticsEngine.applySpread(cameraDirection, cone, random)
    }

    private data class Trace(
        val distance: Float,
        val hitTarget: HitboxTarget?,
        val obstacle: Obstacle?
    )

    private fun trace(origin: Vec3, direction: Vec3, targets: List<HitboxTarget>, obstacles: List<Obstacle>): Trace {
        var best = Trace(Float.MAX_VALUE, null, null)
        targets.forEach { candidate ->
            val distance = BallisticsEngine.raySphere(origin, direction, candidate.center, candidate.radius)
            if (distance != null && distance < best.distance) {
                best = Trace(distance, candidate, null)
            }
        }
        obstacles.forEach { obstacle ->
            val distance = BallisticsEngine.raySphere(origin, direction, obstacle.center, obstacle.radius)
            if (distance != null && distance < best.distance) {
                best = Trace(distance, null, obstacle)
            }
        }
        return best
    }

    /**
     * Resolves one trigger pull. Pass 1 measures the flight distance, pass 2
     * re-aims with the gravity drop applied so a distant shot needs real holdover.
     */
    fun resolve(
        request: ShotRequest,
        targets: List<HitboxTarget>,
        obstacles: List<Obstacle> = emptyList(),
        random: Random = Random.Default
    ): ShotOutcome {
        val firstVector = shotVector(request, random)
        val firstPass = trace(request.muzzle.position, firstVector, targets, obstacles)
        val measureDistance = when {
            firstPass.distance != Float.MAX_VALUE -> firstPass.distance
            else -> request.muzzle.position.distanceTo(request.aimPoint)
        }
        val drop = if (request.bulletDropEnabled) {
            dropMeters(measureDistance, request.muzzleVelocityMps)
        } else {
            0f
        }

        val correctedAim = request.aimPoint + Vec3(0f, drop, 0f)
        val corrected = request.copy(aimPoint = correctedAim)
        val vector = shotVector(corrected, random)
        val pass = trace(request.muzzle.position, vector, targets, obstacles)

        if (pass.distance == Float.MAX_VALUE) {
            return ShotOutcome(
                shotVector = vector,
                aimPointUsed = correctedAim,
                hitTarget = null,
                blockedBy = null,
                distanceMeters = measureDistance,
                dropMeters = drop,
                penetratedObstacleId = null
            )
        }

        val obstacle = pass.obstacle
        if (obstacle != null) {
            if (!obstacle.penetrable) {
                return ShotOutcome(
                    shotVector = vector,
                    aimPointUsed = correctedAim,
                    hitTarget = null,
                    blockedBy = obstacle.obstacleId,
                    distanceMeters = pass.distance,
                    dropMeters = drop,
                    penetratedObstacleId = null
                )
            }
            val beyond = trace(
                request.muzzle.position + vector * (pass.distance + 0.05f),
                vector,
                targets,
                obstacles.filter { it.obstacleId != obstacle.obstacleId }
            )
            val hit = beyond.hitTarget
            return ShotOutcome(
                shotVector = vector,
                aimPointUsed = correctedAim,
                hitTarget = if (beyond.obstacle == null) hit else null,
                blockedBy = if (beyond.obstacle != null) beyond.obstacle.obstacleId else null,
                distanceMeters = if (hit != null) pass.distance + beyond.distance else pass.distance,
                dropMeters = drop,
                penetratedObstacleId = obstacle.obstacleId
            )
        }

        return ShotOutcome(
            shotVector = vector,
            aimPointUsed = correctedAim,
            hitTarget = pass.hitTarget,
            blockedBy = null,
            distanceMeters = pass.distance,
            dropMeters = drop,
            penetratedObstacleId = null
        )
    }

    /** Convenience: the damage of a resolved shot, or null when nothing was hit. */
    fun damageOf(
        outcome: ShotOutcome,
        baseDamage: Float,
        ammo: AmmoProfile,
        effectiveRange: Float,
        maxRange: Float
    ): DamageResult? {
        val hit = outcome.hitTarget ?: return null
        return DamageModel.resolve(
            baseDamage = baseDamage,
            hitbox = hit.component,
            distanceMeters = outcome.distanceMeters,
            effectiveRangeMeters = effectiveRange,
            maxRangeMeters = maxRange,
            target = hit.target,
            ammo = ammo,
            isSniperRifle = hit.isSniperRifle
        )
    }
}
