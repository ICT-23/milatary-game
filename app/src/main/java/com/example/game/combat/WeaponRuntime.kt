package com.example.game.combat

import kotlin.math.min
import kotlin.random.Random

/** Why a trigger pull produced no round. Every gate is evaluated in order. */
enum class FireBlock(val displayName: String) {
    DEAD("Operator down"),
    NOT_EQUIPPED("Weapon not equipped"),
    SWAPPING("Weapon swap in progress"),
    SPRINTING("Cannot fire while sprinting"),
    RELOADING("Reload in progress"),
    COOLDOWN("Cyclic rate cooldown"),
    SAFE_MODE("Receiver on SAFE"),
    EMPTY("Magazine empty"),
    JAMMED("Stoppage - clear malfunction")
}

/** External state the runtime cannot know about (movement, inventory, life). */
data class FireReadiness(
    val isAlive: Boolean = true,
    val isEquipped: Boolean = true,
    val isSprinting: Boolean = false,
    val isMeleeing: Boolean = false,
    val triggerHeld: Boolean = true
)

data class FireAttempt(
    val fired: Boolean,
    val block: FireBlock? = null,
    val spreadDegrees: Float = 0f,
    val jammedNow: Boolean = false,
    val heatPercent: Int = 0
)

/** Snapshot for the HUD so UI never reads mutable runtime internals. */
data class WeaponReadout(
    val weaponName: String,
    val loadedRounds: Int,
    val magazineCapacity: Int,
    val reserveRounds: Int,
    val heatPercent: Int,
    val isJammed: Boolean,
    val isReloading: Boolean,
    val reloadProgress: Float,
    val spreadDegrees: Float,
    val malfunctionPrompt: String?
)

/**
 * Section 11.4: exact weapon state at a checkpoint. A retry restores these values
 * verbatim, so a player never gains rounds by dying and never loses a reload.
 */
data class WeaponSnapshot(
    val loadedRounds: Int,
    val reserveRounds: Int,
    val chamberLoaded: Boolean,
    val heat: Float,
    val isJammed: Boolean,
    val shotsFired: Int,
    val roundsConsumed: Int
)

/**
 * Verified weapon runtime: readiness gates, magazine accounting, cyclic rate,
 * barrel heat, stoppages and the two-step malfunction clearance.
 *
 * Damage is never produced here; this type only decides whether a round left the
 * barrel and with what spread. BallisticsEngine + DamageResolver own the rest.
 */
class WeaponRuntime(
    val build: WeaponBuild,
    private val random: Random = Random.Default
) {
    val stats: GunsmithStats get() = build.stats

    var loadedRounds: Int = stats.magazineCapacity
        private set
    var reserveRounds: Int = stats.reserveRounds
        private set
    var chamberLoaded: Boolean = true
        private set
    var heat: Float = 0f
        private set
    var isJammed: Boolean = false
        private set
    var isReloading: Boolean = false
        private set
    var isSwapping: Boolean = false
        private set
    var reloadProgress: Float = 0f
        private set
    var recoilImpulse: Float = 0f
        private set
    var shotsFired: Int = 0
        private set
    var roundsConsumed: Int = 0
        private set
    var lastFireAtSeconds: Float = -999f
        private set

    private var malfunctionStage = 0
    private var reloadElapsed = 0f

    val cooldownSeconds: Float get() = 60f / stats.rpm.coerceAtLeast(30f)
    val heatPercent: Int get() = (heat * 100f).toInt().coerceIn(0, 100)
    val isOverheated: Boolean get() = heat > OVERHEAT_THRESHOLD

    fun setSwapping(swapping: Boolean) {
        isSwapping = swapping
    }

    /**
     * Attempts one trigger pull. Ordering follows the firing pipeline:
     * readiness -> cooldown -> fire mode -> ammo -> stoppage.
     */
    fun tryExecuteShot(
        nowSeconds: Float,
        readiness: FireReadiness = FireReadiness(),
        stamina01: Float = 1f,
        movement01: Float = 0f
    ): FireAttempt {
        val spread = currentSpread(stamina01, movement01)
        val block = gate(nowSeconds, readiness)
        if (block != null) {
            return FireAttempt(fired = false, block = block, spreadDegrees = spread, heatPercent = heatPercent)
        }

        lastFireAtSeconds = nowSeconds
        loadedRounds = (loadedRounds - 1).coerceAtLeast(0)
        roundsConsumed += 1
        shotsFired += 1
        chamberLoaded = loadedRounds > 0
        heat = (heat + HEAT_PER_SHOT).coerceAtMost(1f)
        recoilImpulse = (recoilImpulse + stats.verticalRecoil * 0.4f).coerceAtMost(6f)

        var jammedNow = false
        if (heat > OVERHEAT_THRESHOLD && random.nextFloat() < JAM_CHANCE_PER_SHOT) {
            isJammed = true
            malfunctionStage = 0
            jammedNow = true
        }

        return FireAttempt(
            fired = true,
            block = null,
            spreadDegrees = currentSpread(stamina01, movement01),
            jammedNow = jammedNow,
            heatPercent = heatPercent
        )
    }

    private fun gate(nowSeconds: Float, readiness: FireReadiness): FireBlock? {
        if (!readiness.isAlive) return FireBlock.DEAD
        if (!readiness.isEquipped) return FireBlock.NOT_EQUIPPED
        if (readiness.isMeleeing || isSwapping) return FireBlock.SWAPPING
        if (readiness.isSprinting) return FireBlock.SPRINTING
        if (isReloading) return FireBlock.RELOADING
        if (nowSeconds - lastFireAtSeconds < cooldownSeconds) return FireBlock.COOLDOWN
        if (stats.fireMode == FireMode.SAFE) return FireBlock.SAFE_MODE
        if (isJammed) return FireBlock.JAMMED
        if (loadedRounds <= 0 || !chamberLoaded) return FireBlock.EMPTY
        return null
    }

    fun currentSpread(stamina01: Float, movement01: Float = 0f): Float {
        val fatigue = 1f - stamina01.coerceIn(0f, 1f)
        var spread = stats.spreadDegrees +
            recoilImpulse * 0.22f +
            fatigue * 1.1f +
            movement01.coerceIn(0f, 1f) * 0.8f
        if (heat > OVERHEAT_THRESHOLD) spread *= 2.5f
        return spread.coerceIn(0.02f, 12f)
    }

    fun beginReload(): Boolean {
        if (isReloading || isJammed) return false
        if (loadedRounds >= stats.magazineCapacity || reserveRounds <= 0) return false
        isReloading = true
        reloadElapsed = 0f
        reloadProgress = 0f
        return true
    }

    /** Two-stage clearance: tap magazine, then rack the charging handle. */
    fun clearMalfunctionStep(): String? {
        if (!isJammed) return null
        return when (malfunctionStage) {
            0 -> {
                malfunctionStage = 1
                "TAP MAGAZINE accepted - now RACK CHARGING HANDLE"
            }
            else -> {
                isJammed = false
                malfunctionStage = 0
                chamberLoaded = loadedRounds > 0
                "STOPPAGE CLEARED"
            }
        }
    }

    fun tick(dt: Float) {
        val step = dt.coerceIn(0f, 0.25f)
        heat = (heat - HEAT_COOL_PER_SECOND * step).coerceAtLeast(0f)
        recoilImpulse *= (1f - min(1f, RECOIL_RECOVERY_PER_SECOND * step))

        if (isReloading) {
            reloadElapsed += step
            reloadProgress = (reloadElapsed / stats.reloadSeconds).coerceIn(0f, 1f)
            if (reloadElapsed >= stats.reloadSeconds) finishReload()
        }
    }

    private fun finishReload() {
        val missing = stats.magazineCapacity - loadedRounds
        val take = min(missing, reserveRounds)
        loadedRounds += take
        reserveRounds -= take
        chamberLoaded = loadedRounds > 0
        isReloading = false
        reloadElapsed = 0f
        reloadProgress = 0f
    }

    fun refillForDrill() {
        loadedRounds = stats.magazineCapacity
        reserveRounds = stats.reserveRounds
        chamberLoaded = true
        heat = 0f
        isJammed = false
        isReloading = false
        reloadElapsed = 0f
        reloadProgress = 0f
        recoilImpulse = 0f
    }

    fun snapshot(): WeaponSnapshot = WeaponSnapshot(
        loadedRounds = loadedRounds,
        reserveRounds = reserveRounds,
        chamberLoaded = chamberLoaded,
        heat = heat,
        isJammed = isJammed,
        shotsFired = shotsFired,
        roundsConsumed = roundsConsumed
    )

    /** Restores a checkpoint snapshot; the caller decides when a retry is legal. */
    fun restore(snapshot: WeaponSnapshot) {
        loadedRounds = snapshot.loadedRounds.coerceIn(0, stats.magazineCapacity)
        reserveRounds = snapshot.reserveRounds.coerceAtLeast(0)
        chamberLoaded = snapshot.chamberLoaded
        heat = snapshot.heat.coerceIn(0f, 1f)
        isJammed = snapshot.isJammed
        shotsFired = snapshot.shotsFired.coerceAtLeast(0)
        roundsConsumed = snapshot.roundsConsumed.coerceAtLeast(0)
        isReloading = false
        reloadElapsed = 0f
        reloadProgress = 0f
        recoilImpulse = 0f
        lastFireAtSeconds = -999f
        malfunctionStage = 0
    }

    fun readout(): WeaponReadout = WeaponReadout(
        weaponName = build.name,
        loadedRounds = loadedRounds,
        magazineCapacity = stats.magazineCapacity,
        reserveRounds = reserveRounds,
        heatPercent = heatPercent,
        isJammed = isJammed,
        isReloading = isReloading,
        reloadProgress = reloadProgress,
        spreadDegrees = currentSpread(1f),
        malfunctionPrompt = when {
            !isJammed -> null
            malfunctionStage == 0 -> "TAP MAGAZINE"
            else -> "RACK CHARGING HANDLE"
        }
    )

    companion object {
        const val OVERHEAT_THRESHOLD = 0.85f
        const val JAM_CHANCE_PER_SHOT = 0.05f
        const val HEAT_PER_SHOT = 0.018f
        const val HEAT_COOL_PER_SECOND = 0.11f
        const val RECOIL_RECOVERY_PER_SECOND = 2.4f
    }
}
