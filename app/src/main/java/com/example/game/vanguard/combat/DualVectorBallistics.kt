package com.example.game.vanguard.combat

import com.example.game.combat.BallisticBody
import com.example.game.combat.BallisticsEngine
import com.example.game.combat.Vec3
import com.example.game.vanguard.core.GameError
import com.example.game.vanguard.core.GameErrorCategory
import com.example.game.vanguard.core.GameResult
import kotlin.math.acos

/**
 * Gates 2 and 3 of Section 3.1.
 *
 * Gate 2: the optic is decoupled from camera FOV - in ADS the camera is aligned
 * *to* the optic socket, never the reverse, so zooming cannot move the reticle
 * off the barrel line.
 *
 * Gate 3: damage is legal only when the sight vector lands on the collider
 * inside the aiming cone AND the segment from the muzzle tip to that exact
 * impact point meets no cover geometry first. Anything else is zero damage.
 */
data class AdsAlignment(
    val cameraForward: Vec3,
    val opticSightForward: Vec3,
    val cameraFovDegrees: Float
) {
    /** Proven by construction: the camera follows the optic, not the FOV. */
    val isFovDecoupled: Boolean
        get() = nearlyEqual(cameraForward, opticSightForward)

    private fun nearlyEqual(a: Vec3, b: Vec3): Boolean =
        (a - b).length() < 0.0001f
}

data class AimRequest(
    val opticSightOrigin: Vec3,
    val opticSightDirection: Vec3,
    val muzzleOrigin: Vec3,
    val hitPoint: Vec3,
    /** Everything that could stop the round: walls, desks, shields. */
    val coverBodies: List<BallisticBody> = emptyList(),
    val angularToleranceDegrees: Float = 1.0f,
    /** Mechanical dispersion of the assembly, in Minute of Angle. */
    val mechanicalDispersionMoa: Float = 1.0f,
    /** Extra error contributed by stance, stamina and recent recoil. */
    val stanceSwayDegrees: Float = 0f
)

data class AimSolution(
    val angularErrorDegrees: Float,
    val toleranceDegrees: Float,
    val distanceMeters: Float,
    val muzzleToImpactMeters: Float,
    val impactPoint: Vec3,
    val blockingBodyId: String?
) {
    val unobstructed: Boolean get() = blockingBodyId == null
}

object DualVectorBallistics {

    private const val MOA_PER_DEGREE = 60f
    private const val MIN_DISTANCE_METERS = 0.1f
    private const val SURFACE_EPSILON_METERS = 0.01f

    /** Gate 2. Zoom changes how much the world fills the screen, never the sight line. */
    fun alignDownSights(opticSightSocketForward: Vec3, cameraFovDegrees: Float): AdsAlignment {
        val forward = opticSightSocketForward.normalized()
        return AdsAlignment(
            cameraForward = forward,
            opticSightForward = forward,
            cameraFovDegrees = cameraFovDegrees
        )
    }

    /**
     * Gate 3. Dispersion and sway are added to the operator's angular error
     * instead of being hidden inside a probability roll, so the same input always
     * yields the same verdict.
     */
    fun resolve(request: AimRequest): GameResult<AimSolution> {
        val toTarget = request.hitPoint - request.opticSightOrigin
        val distance = toTarget.length()
        if (distance < MIN_DISTANCE_METERS) {
            return GameResult.fail(
                GameErrorCategory.AIM,
                "INVALID_HIT_POINT",
                "Impact point coincides with the optic origin"
            )
        }

        val sight = request.opticSightDirection.normalized()
        val targetDirection = toTarget.normalized()
        val cosine = sight.dot(targetDirection).coerceIn(-1f, 1f)
        val baseAngle = Math.toDegrees(acos(cosine.toDouble())).toFloat()
        val dispersion = request.mechanicalDispersionMoa.coerceAtLeast(0f) / MOA_PER_DEGREE
        val sway = request.stanceSwayDegrees.coerceAtLeast(0f)
        val totalError = baseAngle + dispersion + sway
        val tolerance = request.angularToleranceDegrees.coerceAtLeast(0f)

        if (totalError > tolerance) {
            return GameResult.Err(
                GameError(
                    category = GameErrorCategory.AIM,
                    code = "NOT_AIMED_AT_TARGET",
                    message = "Sight vector missed the collider by ${"%.2f".format(totalError)} deg",
                    detail = mapOf(
                        "angularErrorDegrees" to "%.2f".format(totalError),
                        "toleranceDegrees" to "%.2f".format(tolerance)
                    )
                )
            )
        }

        val muzzleToImpact = request.hitPoint - request.muzzleOrigin
        val segmentLength = muzzleToImpact.length()
        if (segmentLength < MIN_DISTANCE_METERS) {
            return GameResult.fail(
                GameErrorCategory.GEOMETRY,
                "MUZZLE_INSIDE_TARGET",
                "Muzzle tip is inside the target volume"
            )
        }

        val segmentDirection = muzzleToImpact.normalized()
        val blocking = firstBlockingBody(request.coverBodies, request.muzzleOrigin, segmentDirection, segmentLength)
        if (blocking != null) {
            return GameResult.Err(
                GameError(
                    category = GameErrorCategory.GEOMETRY,
                    code = "TRAJECTORY_BLOCKED",
                    message = "Cover interrupts the barrel line of sight",
                    detail = mapOf("blockingBodyId" to blocking)
                )
            )
        }

        return GameResult.Ok(
            AimSolution(
                angularErrorDegrees = totalError,
                toleranceDegrees = tolerance,
                distanceMeters = distance,
                muzzleToImpactMeters = segmentLength,
                impactPoint = request.hitPoint,
                blockingBodyId = null
            )
        )
    }

    /**
     * Walks every cover collider and returns the closest one that sits between
     * the muzzle and the impact point. A body that only touches the impact
     * surface itself is not cover.
     */
    fun firstBlockingBody(
        coverBodies: List<BallisticBody>,
        muzzleOrigin: Vec3,
        direction: Vec3,
        segmentLength: Float
    ): String? {
        var closestId: String? = null
        var closestDistance = Float.MAX_VALUE
        for (body in coverBodies) {
            val hit = BallisticsEngine.raySphere(muzzleOrigin, direction, body.center, body.radius) ?: continue
            if (hit >= segmentLength - SURFACE_EPSILON_METERS) continue
            if (hit < closestDistance) {
                closestDistance = hit
                closestId = body.bodyId
            }
        }
        return closestId
    }

    /**
     * Zero-damage contract expressed as a number, so a HUD or scoring layer can
     * read it without re-deriving the rule.
     */
    fun damageMultiplierFor(result: GameResult<AimSolution>): Float =
        if (result is GameResult.Ok) 1f else 0f
}
