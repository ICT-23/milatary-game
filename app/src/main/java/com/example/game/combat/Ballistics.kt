package com.example.game.combat

import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

/** Minimal 3D vector, kept inside the combat module so it stays engine agnostic. */
data class Vec3(val x: Float, val y: Float, val z: Float) {
    operator fun plus(other: Vec3) = Vec3(x + other.x, y + other.y, z + other.z)
    operator fun minus(other: Vec3) = Vec3(x - other.x, y - other.y, z - other.z)
    operator fun times(scalar: Float) = Vec3(x * scalar, y * scalar, z * scalar)

    fun dot(other: Vec3): Float = x * other.x + y * other.y + z * other.z
    fun cross(other: Vec3) = Vec3(
        y * other.z - z * other.y,
        z * other.x - x * other.z,
        x * other.y - y * other.x
    )

    fun length(): Float = sqrt(x * x + y * y + z * z)

    fun normalized(): Vec3 {
        val len = length()
        return if (len > 0.00001f) Vec3(x / len, y / len, z / len) else Zero
    }

    fun distanceTo(other: Vec3): Float = (this - other).length()

    companion object {
        val Zero = Vec3(0f, 0f, 0f)
        val Up = Vec3(0f, 1f, 0f)
    }
}

/** Body the ballistic ray can strike. Obstructions are physical geometry, not targets. */
data class BallisticBody(
    val bodyId: String,
    val center: Vec3,
    val radius: Float,
    val zone: HitZone = HitZone.UPPER_TORSO,
    val surface: SurfaceType = SurfaceType.FLESH,
    val isDamageable: Boolean = true,
    val isObstruction: Boolean = false
) {
    companion object {
        fun wall(id: String, center: Vec3, radius: Float): BallisticBody =
            BallisticBody(
                bodyId = id,
                center = center,
                radius = radius,
                surface = SurfaceType.CONCRETE,
                isDamageable = false,
                isObstruction = true
            )
    }
}

/** Four-condition validation outcome of one trigger pull. */
enum class ShotRejectReason {
    NONE,
    NO_COLLISION,
    BLOCKED_BY_OBSTRUCTION,
    NOT_DAMAGEABLE,
    MUZZLE_VALIDATION_FAILED,
    OUT_OF_RANGE
}

/** Everything the caller needs to render a tracer, decal or damage number. */
data class ShotResolution(
    val valid: Boolean,
    val rejectReason: ShotRejectReason,
    val hitBodyId: String?,
    val hitZone: HitZone,
    val hitPoint: Vec3,
    val hitNormal: Vec3,
    val surface: SurfaceType,
    val distanceMeters: Float,
    val direction: Vec3,
    val aimPoint: Vec3,
    val blockedByBodyId: String? = null
) {
    /** Damage may only be delivered on a fully validated hit. */
    val canApplyDamage: Boolean
        get() = valid && rejectReason == ShotRejectReason.NONE && hitBodyId != null
}

/**
 * Physical aim verification.
 *
 * 1. The player's aim ray finds the intended point.
 * 2. The muzzle ray to that same point must not be blocked.
 * 3. The muzzle ray must strike the same body the player aimed at.
 * 4. Only bodies that implement [Damageable] can ever take damage.
 */
object BallisticsEngine {

    fun raySphere(origin: Vec3, direction: Vec3, center: Vec3, radius: Float): Float? {
        val toCenter = center - origin
        val projection = toCenter.dot(direction)
        if (projection < 0f) return null
        val closest = origin + direction * projection
        val offset = center.distanceTo(closest)
        if (offset > radius) return null
        val halfChord = sqrt((radius * radius - offset * offset).coerceAtLeast(0f))
        val entry = projection - halfChord
        return if (entry >= 0f) entry else projection + halfChord
    }

    /** Spread is applied *before* the ray, per the firing pipeline. */
    fun applySpread(direction: Vec3, spreadDegrees: Float, random: Random): Vec3 {
        if (spreadDegrees <= 0.001f) return direction.normalized()
        val radius = kotlin.math.tan(Math.toRadians(spreadDegrees.toDouble())).toFloat()
        val angle = random.nextFloat() * (Math.PI * 2.0).toFloat()
        val magnitude = sqrt(random.nextFloat()) * radius
        val right = Vec3(direction.z, 0f, -direction.x).normalized()
        val up = right.cross(direction.normalized()).normalized()
        return (direction.normalized() + right * (cos(angle) * magnitude) + up * (sin(angle) * magnitude))
            .normalized()
    }

    fun resolve(
        aimOrigin: Vec3,
        aimDirection: Vec3,
        muzzleOrigin: Vec3,
        bodies: List<BallisticBody>,
        maxRangeMeters: Float,
        spreadDegrees: Float = 0f,
        random: Random = Random.Default
    ): ShotResolution {
        val firedDirection = applySpread(aimDirection, spreadDegrees, random)
        val aimHit = nearestBody(aimOrigin, firedDirection, bodies, maxRangeMeters)

        if (aimHit == null) {
            val fallback = aimOrigin + firedDirection * maxRangeMeters
            return ShotResolution(
                valid = false,
                rejectReason = ShotRejectReason.NO_COLLISION,
                hitBodyId = null,
                hitZone = HitZone.LOWER_TORSO,
                hitPoint = fallback,
                hitNormal = Vec3.Up,
                surface = SurfaceType.GENERIC,
                distanceMeters = maxRangeMeters,
                direction = firedDirection,
                aimPoint = fallback
            )
        }

        val body = aimHit.body
        val aimPoint = aimOrigin + firedDirection * aimHit.distance
        val toTarget = (aimPoint - muzzleOrigin)
        val muzzleDirection = toTarget.normalized()
        val muzzleDistance = toTarget.length()

        val muzzleHit = nearestBody(muzzleOrigin, muzzleDirection, bodies, maxRangeMeters + 5f)
        val obstruction = muzzleHit?.takeIf { it.body.isObstruction && it.distance < muzzleDistance - 0.02f }

        if (obstruction != null) {
            return ShotResolution(
                valid = false,
                rejectReason = ShotRejectReason.BLOCKED_BY_OBSTRUCTION,
                hitBodyId = null,
                hitZone = HitZone.LOWER_TORSO,
                hitPoint = muzzleOrigin + muzzleDirection * obstruction.distance,
                hitNormal = (muzzleOrigin + muzzleDirection * obstruction.distance - obstruction.body.center).normalized(),
                surface = obstruction.body.surface,
                distanceMeters = obstruction.distance,
                direction = muzzleDirection,
                aimPoint = aimPoint,
                blockedByBodyId = obstruction.body.bodyId
            )
        }

        if (body.isObstruction) {
            return ShotResolution(
                valid = false,
                rejectReason = ShotRejectReason.BLOCKED_BY_OBSTRUCTION,
                hitBodyId = null,
                hitZone = HitZone.LOWER_TORSO,
                hitPoint = aimPoint,
                hitNormal = (aimPoint - body.center).normalized(),
                surface = body.surface,
                distanceMeters = aimHit.distance,
                direction = firedDirection,
                aimPoint = aimPoint,
                blockedByBodyId = body.bodyId
            )
        }

        if (muzzleHit == null || muzzleHit.body.bodyId != body.bodyId) {
            return ShotResolution(
                valid = false,
                rejectReason = ShotRejectReason.MUZZLE_VALIDATION_FAILED,
                hitBodyId = null,
                hitZone = HitZone.LOWER_TORSO,
                hitPoint = aimPoint,
                hitNormal = (aimPoint - body.center).normalized(),
                surface = body.surface,
                distanceMeters = aimHit.distance,
                direction = firedDirection,
                aimPoint = aimPoint
            )
        }

        if (!body.isDamageable) {
            return ShotResolution(
                valid = false,
                rejectReason = ShotRejectReason.NOT_DAMAGEABLE,
                hitBodyId = body.bodyId,
                hitZone = body.zone,
                hitPoint = aimPoint,
                hitNormal = (aimPoint - body.center).normalized(),
                surface = body.surface,
                distanceMeters = aimHit.distance,
                direction = firedDirection,
                aimPoint = aimPoint
            )
        }

        return ShotResolution(
            valid = true,
            rejectReason = ShotRejectReason.NONE,
            hitBodyId = body.bodyId,
            hitZone = body.zone,
            hitPoint = aimPoint,
            hitNormal = (aimPoint - body.center).normalized(),
            surface = body.surface,
            distanceMeters = aimHit.distance,
            direction = firedDirection,
            aimPoint = aimPoint
        )
    }

    private data class Proximity(val body: BallisticBody, val distance: Float)

    private fun nearestBody(
        origin: Vec3,
        direction: Vec3,
        bodies: List<BallisticBody>,
        maxRange: Float
    ): Proximity? {
        var best: Proximity? = null
        for (body in bodies) {
            val distance = raySphere(origin, direction, body.center, body.radius) ?: continue
            if (distance > maxRange) continue
            if (best == null || distance < best.distance) best = Proximity(body, distance)
        }
        return best
    }
}

/** Deterministic ballistic math from the spec (drop, drag, crosswind). */
object BallisticMath {

    const val GRAVITY = 9.81f
    private const val KNOT_METERS_PER_SECOND = 0.514444f

    fun timeOfFlight(distanceMeters: Float, velocityMetersPerSecond: Float): Float =
        if (velocityMetersPerSecond <= 0f) 0f else distanceMeters / velocityMetersPerSecond

    /** y(t) = y0 + v0y t - 0.5 g t^2, solved for a level shot. */
    fun gravityDropMeters(distanceMeters: Float, velocityMetersPerSecond: Float): Float {
        val t = timeOfFlight(distanceMeters, velocityMetersPerSecond)
        return 0.5f * GRAVITY * t * t
    }

    /** Lateral displacement caused by a crosswind coupling into the bullet. */
    fun crosswindDriftMeters(
        distanceMeters: Float,
        velocityMetersPerSecond: Float,
        windKnots: Float,
        coupling: Float = 0.5f
    ): Float {
        val t = timeOfFlight(distanceMeters, velocityMetersPerSecond)
        val wind = windKnots * KNOT_METERS_PER_SECOND
        return 0.5f * wind * coupling * t * t
    }

    /**
     * Numeric drag integration of Fd = -0.5 rho v^2 Cd A v-hat.
     * Returns the retained velocity after [distanceMeters].
     */
    fun retainedVelocity(
        initialVelocity: Float,
        distanceMeters: Float,
        dragCoefficient: Float = 0.16f,
        areaSquareMeters: Float = 0.0005f,
        massKg: Float = 0.0118f,
        steps: Int = 40
    ): Float {
        if (initialVelocity <= 0f || distanceMeters <= 0f) return initialVelocity
        val step = distanceMeters / steps.coerceAtLeast(1)
        var velocity = initialVelocity
        repeat(steps) {
            val dragForce = 0.5f * 1.225f * velocity * velocity * dragCoefficient * areaSquareMeters
            val deceleration = dragForce / massKg
            val dt = if (velocity > 0f) step / velocity else 0f
            velocity = (velocity - deceleration * dt).coerceAtLeast(1f)
        }
        return velocity
    }

    /** Meters of correction a turret click of [mils] buys at [distanceMeters]. */
    fun milCorrectionMeters(mils: Float, distanceMeters: Float): Float = mils * distanceMeters / 1000f
}
