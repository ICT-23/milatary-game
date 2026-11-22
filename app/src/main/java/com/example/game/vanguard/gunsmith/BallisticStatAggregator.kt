package com.example.game.vanguard.gunsmith

import com.example.game.vanguard.core.GameErrorCategory
import com.example.game.vanguard.core.GameResult
import com.example.game.vanguard.core.then
import kotlin.math.roundToInt

/**
 * The numbers the combat layer consumes. Nothing else in the project is allowed
 * to re-derive them from parts, which is what keeps the armory card, the recoil
 * spring and the ballistics ray in agreement.
 */
data class AssemblyStats(
    val caliber: Caliber,
    val massKg: Float,
    val overallLengthMm: Float,
    val centerOfBalanceMm: Float,
    val muzzleVelocityMps: Float,
    val mechanicalMoa: Float,
    val trueRpm: Float,
    val recoilImpulse: Float,
    val kickOffReduction: Float,
    val magazineCapacity: Int,
    val hidesFlash: Boolean,
    val smokeParticleAlphaCap: Float,
    val acousticRadiusPercent: Float,
    val firstShotPopDecibels: Float,
    val triggerInputDelayMs: Float,
    val beddingZeroShiftMoa: Float,
    val optic: OpticSpec,
    val bolt: BoltSpec
) {
    /** 60.0 / TruePhysicsRPM - the same value gate 1 enforces. */
    val fireIntervalSeconds: Float get() = 60f / trueRpm.coerceAtLeast(1f)

    val hudRpm: Int get() = (60f / fireIntervalSeconds).roundToInt()
}

data class BarrelSwap(
    val loadout: WeaponLoadout,
    val stats: AssemblyStats,
    val zeroShiftMoa: Float
) {
    /** Section 3.2: the swap must hold under three Minutes of Angle. */
    val beddingPreserved: Boolean get() = zeroShiftMoa < BallisticStatAggregator.MAX_BEDDING_ZERO_SHIFT_MOA
}

/**
 * Section 3.2: assembles a loadout and validates the socket tree. A part that
 * does not fit is a typed failure, never a silently degraded weapon.
 */
object BallisticStatAggregator {

    /** "preserves < 3.0 SUB MOA zero alignment" - strictly less than 3.0. */
    const val MAX_BEDDING_ZERO_SHIFT_MOA = 3.0f

    private val BASE_IMPULSE: Map<Caliber, Float> = mapOf(
        Caliber.NINE_NINETEEN to 0.90f,
        Caliber.FORTY_FIVE_ACP to 1.15f,
        Caliber.FIVE_FIVE_SIX to 1.30f,
        Caliber.THREE_ZERO_EIGHT to 1.90f,
        Caliber.THREE_HUNDRED_WIN_MAG to 2.40f,
        Caliber.TWELVE_GAUGE to 3.10f
    )

    fun assemble(loadout: WeaponLoadout): GameResult<AssemblyStats> {
        val caliber = loadout.barrel.caliber

        if (caliber !in loadout.bolt.supportedCalibers) {
            return GameResult.fail(
                GameErrorCategory.INCOMPATIBLE,
                "BOLT_CALIBER_MISMATCH",
                "The fitted bolt head does not accept ${caliber.label}"
            )
        }
        if (!loadout.muzzle.threadPattern.equals(loadout.barrel.muzzleThread, ignoreCase = true)) {
            return GameResult.fail(
                GameErrorCategory.INCOMPATIBLE,
                "MUZZLE_THREAD_MISMATCH",
                "Muzzle device thread does not match the barrel"
            )
        }
        if (!loadout.trigger.isWithinSpec) {
            return GameResult.fail(
                GameErrorCategory.INCOMPATIBLE,
                "TRIGGER_OUT_OF_SPEC",
                "Trigger pull must sit between 900 g and 1500 g"
            )
        }
        if (loadout.magazine.capacity <= 0) {
            return GameResult.fail(
                GameErrorCategory.INCOMPATIBLE,
                "EMPTY_MAGAZINE_SPEC",
                "A magazine must hold at least one round"
            )
        }

        val zeroShift = barrelSwapZeroShiftMoa(
            chassis = loadout.chassis,
            previousBarrel = loadout.barrel,
            nextBarrel = loadout.barrel
        )

        val mass = loadout.chassis.weightKg +
            loadout.barrel.weightKg +
            loadout.optic.weightKg +
            loadout.muzzle.weightKg +
            loadout.stock.weightKg +
            0.35f
        val overallLength = loadout.stock.totalLengthOfPullMm +
            loadout.chassis.receiverLengthMm +
            loadout.barrel.lengthMm
        val centerOfBalance = centerOfBalanceMm(loadout, overallLength, mass)
        val velocity = caliber.muzzleVelocityBaseMps *
            (loadout.barrel.lengthCm / 60f).coerceIn(0.80f, 1.12f)
        val moa = (loadout.barrel.expectedMoa / loadout.chassis.material.rigidity) + loadout.trigger.jerkMoa
        val impulseBase = BASE_IMPULSE[caliber] ?: 1f
        val impulse = impulseBase *
            (1f - loadout.stock.kickOff.reduction) *
            (1f - loadout.muzzle.recoilReductionPercent / 100f)

        return GameResult.Ok(
            AssemblyStats(
                caliber = caliber,
                massKg = mass,
                overallLengthMm = overallLength,
                centerOfBalanceMm = centerOfBalance,
                muzzleVelocityMps = velocity,
                mechanicalMoa = moa.coerceAtLeast(0.05f),
                trueRpm = loadout.bolt.type.cycleRateRpm,
                recoilImpulse = impulse.coerceAtLeast(0.01f),
                kickOffReduction = loadout.stock.kickOff.reduction,
                magazineCapacity = loadout.magazine.capacity,
                hidesFlash = loadout.muzzle.hidesFlash,
                smokeParticleAlphaCap = loadout.muzzle.smokeParticleAlphaCap,
                acousticRadiusPercent = loadout.muzzle.acousticRadiusPercent,
                firstShotPopDecibels = loadout.muzzle.firstShotPopDecibels,
                triggerInputDelayMs = loadout.trigger.inputDelayMs,
                beddingZeroShiftMoa = zeroShift,
                optic = loadout.optic,
                bolt = loadout.bolt
            )
        )
    }

    /**
     * Swaps a barrel on an assembled weapon. The bedding geometry decides how
     * far the zero moves, and a chassis that cannot hold 3 SUB MOA refuses the
     * swap instead of quietly ruining the rifle.
     */
    fun swapBarrel(loadout: WeaponLoadout, nextBarrel: BarrelSpec): GameResult<BarrelSwap> {
        val bolt = VanguardLoadouts.boltFor(nextBarrel.caliber)
        if (bolt == null) {
            return GameResult.fail(
                GameErrorCategory.INCOMPATIBLE,
                "NO_BOLT_HEAD",
                "No bolt head in the kit accepts ${nextBarrel.caliber.label}"
            )
        }
        if (!loadout.muzzle.threadPattern.equals(nextBarrel.muzzleThread, ignoreCase = true)) {
            return GameResult.fail(
                GameErrorCategory.INCOMPATIBLE,
                "MUZZLE_THREAD_MISMATCH",
                "Fitted muzzle device will not thread onto ${nextBarrel.caliber.label}"
            )
        }
        val zeroShift = barrelSwapZeroShiftMoa(loadout.chassis, loadout.barrel, nextBarrel)
        if (zeroShift >= MAX_BEDDING_ZERO_SHIFT_MOA) {
            return GameResult.fail(
                GameErrorCategory.INCOMPATIBLE,
                "ZERO_SHIFT_EXCEEDS_SPEC",
                "Bedding cannot hold sub-3 MOA zero for this barrel"
            )
        }

        val updated = loadout.withBarrel(nextBarrel).copy(bolt = bolt)
        return assemble(updated).then { assembled ->
            GameResult.Ok(
                BarrelSwap(
                    loadout = updated,
                    stats = assembled.copy(beddingZeroShiftMoa = zeroShift),
                    zeroShiftMoa = zeroShift
                )
            )
        }
    }

    /**
     * Bedding geometry plus the mass difference between the two barrels. A match
     * chassis barely moves; a field chassis moves more but still inside spec for
     * every barrel in the kit.
     */
    fun barrelSwapZeroShiftMoa(
        chassis: ReceiverChassis,
        previousBarrel: BarrelSpec,
        nextBarrel: BarrelSpec
    ): Float {
        val bedding = (1f - chassis.beddingGeometryRating.coerceIn(0f, 1f)) * 10f
        val massDelta = kotlin.math.abs(nextBarrel.weightKg - previousBarrel.weightKg) * 0.25f
        return bedding + massDelta
    }

    private fun centerOfBalanceMm(loadout: WeaponLoadout, overallMm: Float, massKg: Float): Float {
        if (massKg <= 0f) return overallMm * 0.5f
        val barrelMoment = loadout.barrel.weightKg * (loadout.barrel.lengthMm * 0.5f)
        val chassisMoment = loadout.chassis.weightKg *
            (loadout.barrel.lengthMm + loadout.chassis.receiverLengthMm * 0.5f)
        val stockMoment = loadout.stock.weightKg * (overallMm - loadout.stock.totalLengthOfPullMm * 0.5f)
        val opticMoment = loadout.optic.weightKg * (loadout.barrel.lengthMm + loadout.chassis.receiverLengthMm * 0.4f)
        val total = barrelMoment + chassisMoment + stockMoment + opticMoment
        return (total / massKg).coerceIn(0f, overallMm)
    }
}
