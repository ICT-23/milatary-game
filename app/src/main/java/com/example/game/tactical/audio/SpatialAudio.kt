package com.example.game.tactical.audio

import com.example.game.combat.Vec3
import kotlin.math.abs
import kotlin.math.log10

/**
 * Section 6, VFX & SFX: positional 3D audio.
 *
 * Gunfire is modelled twice. The muzzle report travels at 343 m/s, while the
 * supersonic crack rides the bullet's shockwave and arrives at muzzle-velocity
 * speed - which is why a distant round cracks before it reports. A suppressor
 * drops the peak and the audible radius, and that radius is what decides
 * whether an enemy AI hears the shot at all.
 */
enum class AudioZone(val displayName: String, val reverbSeconds: Float, val wetMix: Float) {
    OUTDOOR("Open Terrain", 0.40f, 0.05f),
    BARRACKS("Barracks Interior", 1.10f, 0.18f),
    CONCRETE_HALL("Concrete Hall", 2.30f, 0.35f),
    TUNNEL("Tunnel", 3.60f, 0.50f)
}

data class AudioMix(
    val master: Float = 0.85f,
    val sfx: Float = 0.90f,
    val music: Float = 0.55f,
    val ambient: Float = 0.65f
) {
    fun clamped(): AudioMix = AudioMix(
        master = master.coerceIn(0f, 1f),
        sfx = sfx.coerceIn(0f, 1f),
        music = music.coerceIn(0f, 1f),
        ambient = ambient.coerceIn(0f, 1f)
    )

    /** Combined playback gain; the mixer must never exceed unity. */
    val effectiveSfx: Float get() = (master * sfx).coerceIn(0f, 1f)
}

data class ShotReport(
    val distanceMeters: Float,
    val reportDelaySeconds: Float,
    val crackDelaySeconds: Float,
    val peakDb: Float,
    val audibleRadiusMeters: Float
) {
    /** True when the shockwave reaches the listener before the muzzle report. */
    val cracksBeforeReport: Boolean get() = crackDelaySeconds < reportDelaySeconds
}

object SpatialAudio {

    const val SPEED_OF_SOUND_MPS = 343f
    const val DEFAULT_BULLET_SPEED_MPS = 880f
    const val UNSILENCED_PEAK_DB = 165f
    const val SUPPRESSED_PEAK_DB = 128f
    const val UNSILENCED_AUDIBLE_METERS = 250f
    const val SUPPRESSED_AUDIBLE_METERS = 40f
    const val REFERENCE_DISTANCE_METERS = 1f

    fun reportDelaySeconds(distanceMeters: Float): Float =
        (distanceMeters.coerceAtLeast(0f) / SPEED_OF_SOUND_MPS)

    fun crackDelaySeconds(distanceMeters: Float, bulletSpeedMps: Float = DEFAULT_BULLET_SPEED_MPS): Float =
        (distanceMeters.coerceAtLeast(0f) / bulletSpeedMps.coerceAtLeast(1f))

    /** Inverse-square law: 6 dB lost per doubling of distance. */
    fun peakDbAt(peakDb: Float, distanceMeters: Float): Float =
        peakDb - 20f * log10(distanceMeters.coerceAtLeast(REFERENCE_DISTANCE_METERS))

    fun audibleRadiusMeters(isSuppressed: Boolean): Float =
        if (isSuppressed) SUPPRESSED_AUDIBLE_METERS else UNSILENCED_AUDIBLE_METERS

    fun isAudible(distanceMeters: Float, isSuppressed: Boolean): Boolean =
        distanceMeters <= audibleRadiusMeters(isSuppressed)

    fun shotReport(
        distanceMeters: Float,
        isSuppressed: Boolean,
        bulletSpeedMps: Float = DEFAULT_BULLET_SPEED_MPS
    ): ShotReport {
        val peak = if (isSuppressed) SUPPRESSED_PEAK_DB else UNSILENCED_PEAK_DB
        return ShotReport(
            distanceMeters = distanceMeters,
            reportDelaySeconds = reportDelaySeconds(distanceMeters),
            crackDelaySeconds = crackDelaySeconds(distanceMeters, bulletSpeedMps),
            peakDb = peakDbAt(peak, distanceMeters),
            audibleRadiusMeters = audibleRadiusMeters(isSuppressed)
        )
    }

    /**
     * Stereo pan of a source relative to the listener, -1 hard left to +1 hard
     * right. [listenerRight] is the camera's unit right vector.
     */
    fun panStereo(listenerRight: Vec3, listenerPosition: Vec3, sourcePosition: Vec3): Float {
        val toSource = (sourcePosition - listenerPosition)
        val distance = toSource.length()
        if (distance <= 0.0001f) return 0f
        return (toSource.normalized().dot(listenerRight.normalized())).coerceIn(-1f, 1f)
    }

    /** How much of the zone's reverb reaches the listener, 0..1. */
    fun reverbAmount(zone: AudioZone, distanceMeters: Float): Float {
        val falloff = (1f - distanceMeters / UNSILENCED_AUDIBLE_METERS).coerceIn(0f, 1f)
        return zone.wetMix * falloff
    }

    /** Absolute stereo balance for the HUD/audio debug overlay. */
    fun balanceLabel(pan: Float): String = when {
        abs(pan) < 0.08f -> "CENTER"
        pan < 0f -> "LEFT ${"%.0f".format(abs(pan) * 100)}%"
        else -> "RIGHT ${"%.0f".format(pan * 100)}%"
    }
}
