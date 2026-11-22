package com.example.game.vanguard.gunsmith

/**
 * Section 3.2: the seven fixed sockets of the workbench. A weapon is only
 * "assembled" when every socket is filled, which is what stops a half-built
 * receiver from reaching the range.
 */
enum class PartSocket(val displayName: String) {
    BARREL("Socket_Barrel"),
    BOLT_ACTION("Socket_BoltAction"),
    TRIGGER_GROUP("Socket_TriggerGroup"),
    OPTIC_MOUNT("Socket_OpticMount"),
    MUZZLE("Socket_Muzzle"),
    STOCK_AND_PAD("Socket_StockAndPad"),
    MAGAZINE("Socket_Magazine")
}

/** Calibres named in Section 3.2. Diameters are real-world references only. */
enum class Caliber(val label: String, val nominalDiameterMm: Float, val muzzleVelocityBaseMps: Float) {
    NINE_NINETEEN("9x19mm", 9.01f, 360f),
    FORTY_FIVE_ACP(".45ACP", 11.43f, 260f),
    FIVE_FIVE_SIX("5.56x45mm", 5.70f, 920f),
    THREE_ZERO_EIGHT(".308Win", 7.82f, 850f),
    THREE_HUNDRED_WIN_MAG(".300WinMag", 7.62f, 940f),
    TWELVE_GAUGE("12 Gauge", 18.50f, 400f)
}

enum class ChassisMaterial(val rigidity: Float) {
    STANDARD_ALLOY(0.85f),
    TITANIUM_MATCH(1.0f)
}

/** The receiver every part bolts onto. Bedding rating drives zero hold on swap. */
data class ReceiverChassis(
    val id: String,
    val name: String,
    val material: ChassisMaterial,
    val beddingGeometryRating: Float,
    val weightKg: Float,
    val receiverLengthMm: Float
)

/** Socket_Barrel: calibre, length, profile, thread, twist. */
data class BarrelSpec(
    val id: String,
    val caliber: Caliber,
    val lengthCm: Float,
    val profileMm: Float,
    val twistInches: String,
    val muzzleThread: String,
    val weightKg: Float,
    val expectedMoa: Float
) {
    val lengthMm: Float get() = lengthCm * 10f
    val isMagnum: Boolean get() = lengthCm >= 62f
}

enum class BoltType(val cycleRateRpm: Float) {
    STANDARD_ROTATING(660f),
    BRX1_LINEAR(780f)
}

/** Socket_BoltAction: rotating or the BRX1 straight-pull linear bolt. */
data class BoltSpec(
    val id: String,
    val name: String,
    val type: BoltType,
    val handleDegrees: Int,
    val reversibleEjection: Boolean,
    val zeroJamRapidCycle: Boolean,
    val supportedCalibers: Set<Caliber>
) {
    val supports: Boolean get() = supportedCalibers.isNotEmpty()
}

/** Socket_TriggerGroup: three pull weights, three-position safety lives separately. */
data class TriggerSpec(
    val id: String,
    val name: String,
    val pullWeightGrams: Int,
    val inputDelayMs: Float,
    val jerkMoa: Float
) {
    val isWithinSpec: Boolean get() = pullWeightGrams in MIN_GRAMS..MAX_GRAMS

    companion object {
        const val MIN_GRAMS = 900
        const val MAX_GRAMS = 1_500
    }
}

/** Second or first focal plane. The plane decides how subtension scales. */
enum class ReticlePlane { SFP, FFP }

/**
 * Socket_OpticMount. An SFP reticle holds a constant visual thickness, so its
 * MOA subtension is exact at maximum zoom and doubles at half power; an FFP
 * reticle scales with the image and stays exact in MRAD at every zoom.
 */
data class OpticSpec(
    val id: String,
    val name: String,
    val plane: ReticlePlane,
    val minZoom: Float,
    val maxZoom: Float,
    val moaSubtenseAtMaxZoom: Float,
    val parallaxMinMeters: Float,
    val hasZeroStop: Boolean,
    val weightKg: Float
) {
    /** Section 3.2: parallax turret runs from 10 m to infinity. */
    fun parallaxValidAt(distanceMeters: Float): Boolean =
        distanceMeters >= parallaxMinMeters - 0.001f

    /** Effective MOA subtension at a given magnification. */
    fun moaSubtenseAt(zoom: Float): Float {
        val clamped = zoom.coerceIn(minZoom, maxZoom)
        return when (plane) {
            ReticlePlane.SFP -> moaSubtenseAtMaxZoom * (maxZoom / clamped)
            ReticlePlane.FFP -> moaSubtenseAtMaxZoom
        }
    }

    fun milHoldoverAt(zoom: Float, targetMoa: Float): Float =
        if (moaSubtenseAt(zoom) <= 0f) 0f else targetMoa / moaSubtenseAt(zoom)
}

enum class MuzzleDeviceKind { THREAD_PROTECTOR, FTE_MUZZLE_BRAKE, SOUND_SUPPRESSOR }

/**
 * Socket_Muzzle. A suppressor removes flash, shrinks the acoustic radius,
 * models the first-shot pop and caps smoke particle alpha so VFX never cover
 * the optic.
 */
data class MuzzleSpec(
    val id: String,
    val name: String,
    val kind: MuzzleDeviceKind,
    val threadPattern: String,
    val hidesFlash: Boolean,
    val smokeParticleAlphaCap: Float,
    val acousticRadiusPercent: Float,
    val recoilReductionPercent: Float,
    val firstShotPopDecibels: Float,
    val weightKg: Float
)

/** Hydraulic buffers from Section 3.2, capped at 70% impulse reduction. */
enum class KickOffBuffer(val reduction: Float) {
    NONE(0f),
    KICK_OFF_I(0.25f),
    KICK_OFF_PLUS(0.45f),
    KICK_OFF_MEGA(0.70f)
}

/** Socket_StockAndPad: 365 mm base plus 12.7 mm spacers and a MicroCore pad. */
data class StockSpec(
    val id: String,
    val name: String,
    val lengthOfPullMm: Float,
    val spacerMm: Float,
    val kickOff: KickOffBuffer,
    val microCorePad: Boolean,
    val weightKg: Float
) {
    val totalLengthOfPullMm: Float get() = lengthOfPullMm + spacerMm

    companion object {
        const val BASE_LENGTH_OF_PULL_MM = 365f
        const val SPACER_MM = 12.7f
    }
}

/** Socket_Magazine: magazines are discrete objects, not an abstract pool. */
data class MagazineSpec(
    val id: String,
    val name: String,
    val capacity: Int,
    val retainsChamberedRoundOnTacticalReload: Boolean = true
)
