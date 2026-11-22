package com.example.game.tactical.combat

import com.example.game.tactical.gunsmith.AssembledWeapon
import com.example.game.tactical.gunsmith.AttachmentSlot

/**
 * Phase 3 deliverable: the WeaponController.
 *
 * It owns ammunition, the reload state machine, cyclic rate and the procedural
 * recoil/sway accumulators, and it hands the shooter a MuzzleTransform. It never
 * decides a hit - that belongs to BallisticResolver.
 */
enum class FireDenial(val displayName: String) {
    NONE("Shot away"),
    RELOADING("Reload in progress"),
    EMPTY("Magazine empty"),
    COOLDOWN("Cyclic rate cooldown"),
    ON_SAFE("Receiver on safe"),
    NO_WEAPON("No weapon equipped")
}

data class WeaponRuntimeState(
    val weapon: AssembledWeapon,
    val loadedRounds: Int,
    val reserveRounds: Int,
    val isReloading: Boolean = false,
    val reloadRemainingSeconds: Float = 0f,
    val cooldownSeconds: Float = 0f,
    val accumulatedRecoilDegrees: Float = 0f,
    val swayDegrees: Float = 0f,
    val shotsFired: Int = 0,
    val isOnSafe: Boolean = false
) {
    val isMagazineEmpty: Boolean get() = loadedRounds <= 0
    val spreadDegrees: Float
        get() = WeaponController.BASE_HIP_CONE_DEGREES +
            accumulatedRecoilDegrees * WeaponController.RECOIL_TO_CONE_DEGREES
}

data class FireAttempt(val fired: Boolean, val denial: FireDenial, val state: WeaponRuntimeState)

object WeaponController {

    const val BASE_HIP_CONE_DEGREES = 3.5f
    const val RECOIL_TO_CONE_DEGREES = 0.35f
    const val MAX_ACCUMULATED_RECOIL_DEGREES = 12f
    const val RECOIL_SETTLE_PER_SECOND = 9f
    const val SWAY_PER_SECOND = 0.6f
    const val MAX_SWAY_DEGREES = 1.2f
    const val BASE_RELOAD_SECONDS = 2.2f
    const val DEBUG_RAY_DEFAULT = false

    /** Phase 3 visual debug toggle: draws live trajectories in the 3D scene. */
    var debugRay: Boolean = DEBUG_RAY_DEFAULT

    fun equip(weapon: AssembledWeapon, reserveRounds: Int): WeaponRuntimeState =
        WeaponRuntimeState(
            weapon = weapon,
            loadedRounds = weapon.stats.magazineSize,
            reserveRounds = reserveRounds.coerceAtLeast(0)
        )

    fun cyclesPerSecond(state: WeaponRuntimeState): Float = state.weapon.stats.fireRateRpm / 60f

    /**
     * One trigger pull. The round is consumed only when the shot is actually
     * away, so a blocked pull never costs ammunition.
     */
    fun tryFire(state: WeaponRuntimeState): FireAttempt {
        if (state.isOnSafe) return FireAttempt(false, FireDenial.ON_SAFE, state)
        if (state.isReloading) return FireAttempt(false, FireDenial.RELOADING, state)
        if (state.cooldownSeconds > 0f) return FireAttempt(false, FireDenial.COOLDOWN, state)
        if (state.isMagazineEmpty) return FireAttempt(false, FireDenial.EMPTY, state)

        val recoil = (state.accumulatedRecoilDegrees + state.weapon.stats.effectiveRecoil)
            .coerceAtMost(MAX_ACCUMULATED_RECOIL_DEGREES)
        val interval = 60f / state.weapon.stats.fireRateRpm.coerceAtLeast(1f)

        val next = state.copy(
            loadedRounds = state.loadedRounds - 1,
            cooldownSeconds = interval,
            accumulatedRecoilDegrees = recoil,
            shotsFired = state.shotsFired + 1
        )
        return FireAttempt(true, FireDenial.NONE, next)
    }

    fun beginReload(state: WeaponRuntimeState): WeaponRuntimeState {
        if (state.isReloading) return state
        if (state.reserveRounds <= 0) return state
        if (state.loadedRounds >= state.weapon.stats.magazineSize) return state
        val multiplier = state.weapon.attachments[AttachmentSlot.MAGAZINE]?.reloadSecondsMultiplier ?: 1f
        return state.copy(isReloading = true, reloadRemainingSeconds = BASE_RELOAD_SECONDS * multiplier)
    }

    /** Advances timers, completes a reload and settles the recoil spring. */
    fun tick(state: WeaponRuntimeState, deltaSeconds: Float): WeaponRuntimeState {
        if (deltaSeconds <= 0f) return state
        val cooling = (state.cooldownSeconds - deltaSeconds).coerceAtLeast(0f)
        val settledRecoil = (state.accumulatedRecoilDegrees - RECOIL_SETTLE_PER_SECOND * deltaSeconds)
            .coerceAtLeast(0f)
        val sway = (state.swayDegrees + SWAY_PER_SECOND * deltaSeconds)
            .coerceIn(0f, MAX_SWAY_DEGREES)

        var next = state.copy(
            cooldownSeconds = cooling,
            accumulatedRecoilDegrees = settledRecoil,
            swayDegrees = sway
        )

        if (next.isReloading) {
            val remaining = next.reloadRemainingSeconds - deltaSeconds
            if (remaining <= 0f) {
                val needed = next.weapon.stats.magazineSize - next.loadedRounds
                val drawn = needed.coerceAtMost(next.reserveRounds.coerceAtLeast(0))
                next = next.copy(
                    isReloading = false,
                    reloadRemainingSeconds = 0f,
                    loadedRounds = next.loadedRounds + drawn,
                    reserveRounds = next.reserveRounds - drawn
                )
            } else {
                next = next.copy(reloadRemainingSeconds = remaining)
            }
        }
        return next
    }

    fun toggleSafety(state: WeaponRuntimeState): WeaponRuntimeState = state.copy(isOnSafe = !state.isOnSafe)
}
