package com.example.game.vanguard.combat

import com.example.game.vanguard.core.GameErrorCategory
import com.example.game.vanguard.core.GameResult
import com.example.game.vanguard.gunsmith.MuzzleDeviceKind
import com.example.game.vanguard.gunsmith.MuzzleSpec

/**
 * Section 2 Combat: AcousticReverbSystem.
 *
 * A shot is heard, not just seen. The muzzle device decides how far the report
 * carries, the environment decides how long it rings, and a suppressor is the
 * only thing that removes the flash and shortens the tail.
 */
enum class AcousticEnvironment(
    val displayName: String,
    val reverbTailSeconds: Float,
    val radiusMultiplier: Float
) {
    OPEN_FIELD("Open Field", 0.6f, 1.0f),
    URBAN_STREET("Urban Street", 1.4f, 1.15f),
    INDOOR_CORRIDOR("Indoor Corridor", 2.1f, 1.35f),
    SUBTERRANEAN("Subterranean", 2.8f, 1.1f)
}

data class AcousticProfile(
    val radiusMeters: Float,
    val reverbTailSeconds: Float,
    val peakDecibels: Float,
    val firstShotPopDecibels: Float,
    val isSuppressed: Boolean,
    val isOccluded: Boolean
) {
    /** Enemy AI uses this radius to decide whether it heard the shot. */
    fun isAudibleAt(distanceMeters: Float): Boolean = distanceMeters <= radiusMeters
}

object AcousticReverbSystem {

    const val BASE_RADIUS_METERS = 120f
    const val BASE_SHOT_DECIBELS = 160f
    const val SUPPRESSED_DECIBEL_CUT = 24f
    const val OCCLUSION_RADIUS_FACTOR = 0.55f
    const val OCCLUSION_DECIBEL_CUT = 12f
    const val SUPPRESSED_TAIL_FACTOR = 0.6f

    fun profile(
        muzzle: MuzzleSpec,
        environment: AcousticEnvironment,
        shooterOccluded: Boolean = false,
        firstShot: Boolean = false
    ): GameResult<AcousticProfile> {
        if (muzzle.acousticRadiusPercent <= 0f) {
            return GameResult.fail(
                GameErrorCategory.INCOMPATIBLE,
                "INVALID_MUZZLE_ACOUSTICS",
                "Muzzle device reports a zero acoustic radius"
            )
        }

        val suppressed = muzzle.kind == MuzzleDeviceKind.SOUND_SUPPRESSOR
        val radius = BASE_RADIUS_METERS *
            (muzzle.acousticRadiusPercent / 100f) *
            environment.radiusMultiplier *
            (if (shooterOccluded) OCCLUSION_RADIUS_FACTOR else 1f)

        val tail = environment.reverbTailSeconds * (if (suppressed) SUPPRESSED_TAIL_FACTOR else 1f)

        val peak = BASE_SHOT_DECIBELS -
            (if (suppressed) SUPPRESSED_DECIBEL_CUT else 0f) -
            (if (shooterOccluded) OCCLUSION_DECIBEL_CUT else 0f)

        return GameResult.Ok(
            AcousticProfile(
                radiusMeters = radius,
                reverbTailSeconds = tail,
                peakDecibels = peak,
                firstShotPopDecibels = if (firstShot) muzzle.firstShotPopDecibels else 0f,
                isSuppressed = suppressed,
                isOccluded = shooterOccluded
            )
        )
    }
}
