package com.example.game.valor.plan

import kotlin.math.min

/**
 * Section 5.6 ammo model: a loaded magazine plus carried reserve. Total is never
 * negative and the reserve is capped by the backpack tier.
 */
data class AmmoState(
    val magazine: Int,
    val magSize: Int,
    val reserve: Int,
    val reserveCap: Int
) {
    val total: Int get() = magazine + reserve
    val magazineFull: Boolean get() = magazine >= magSize
    val hasReserve: Boolean get() = reserve > 0
}

/** Where the weapon is used; only the two non-combat contexts allow infinite ammo. */
enum class AmmoContext(val displayName: String) {
    COMBAT("Combat"),
    TRAINING_RANGE("Training Range"),
    DEBUG("Debug")
}

/**
 * Section 5.6: "Ammo only transferred at reload completion (not at start)"
 * and "cancel → reload aborted, no ammo moved".
 */
class ReloadProgress(
    val startedAtSeconds: Float,
    val durationSeconds: Float,
    private val snapshot: AmmoState
) {
    fun progress(nowSeconds: Float): Float =
        if (durationSeconds <= 0f) 1f
        else ((nowSeconds - startedAtSeconds) / durationSeconds).coerceIn(0f, 1f)

    fun isComplete(nowSeconds: Float): Boolean = nowSeconds - startedAtSeconds >= durationSeconds

    /** Values are untouched mid-reload: the snapshot is what the HUD shows. */
    fun midReloadAmmo(): AmmoState = snapshot

    /** Called once the timer finishes; moves ammo, never creates it. */
    fun finish(): AmmoState = AmmoRules.finish(snapshot)

    /** Swap, sprint, death or vault: nothing moves. */
    fun cancel(): AmmoState = snapshot
}

/** Section 5.6: one weapon's own ammunition, so a swap never loses it. */
class WeaponInstance(val id: String, var ammo: AmmoState, val reloadPerShell: Boolean = false)

/** Section 5.6: the reload, pickup and swap rules in one testable place. */
object AmmoRules {

    const val AMMO_IS_MOVED_NOT_CREATED = true
    const val RELOAD_TRANSFERS_AT_COMPLETION = true
    const val SHELL_RELOAD_INTERRUPTIBLE = true
    const val EXCESS_PICKUP_WASTED = true
    const val AMMO_FULL_MESSAGE = "AMMO FULL"

    /** "allowed if magazine < magSize AND reserve > 0 AND not already reloading". */
    fun canStartReload(state: AmmoState, alreadyReloading: Boolean): Boolean =
        !alreadyReloading && state.magazine < state.magSize && state.reserve > 0

    /** "tactical, mag not empty" or "emptyReloadTime, mag empty". */
    fun reloadDuration(tacticalSeconds: Float, emptySeconds: Float, magazine: Int): Float =
        if (magazine <= 0) emptySeconds else tacticalSeconds

    /** `need = magSize - magazine; take = min(need, reserve)`; the total never changes. */
    fun finish(state: AmmoState): AmmoState {
        val need = (state.magSize - state.magazine).coerceAtLeast(0)
        val take = min(need, state.reserve.coerceAtLeast(0))
        return state.copy(magazine = state.magazine + take, reserve = state.reserve - take)
    }

    /** Cancelling leaves the values exactly as they were before the reload began. */
    fun cancel(state: AmmoState): AmmoState = state

    /** "Shell-by-shell: +1 per shell, can be interrupted by fire." */
    fun loadOneShell(state: AmmoState): AmmoState {
        if (state.magazine >= state.magSize || state.reserve <= 0) return state
        return state.copy(magazine = state.magazine + 1, reserve = state.reserve - 1)
    }

    fun interruptibleByFire(reloadPerShell: Boolean): Boolean = reloadPerShell && SHELL_RELOAD_INTERRUPTIBLE

    /** "Pickups add to reserve up to cap; excess is wasted." */
    fun pickup(state: AmmoState, crateAmount: Int): Pair<AmmoState, Int> {
        val room = (state.reserveCap - state.reserve).coerceAtLeast(0)
        val taken = crateAmount.coerceAtLeast(0).coerceAtMost(room)
        val wasted = crateAmount.coerceAtLeast(0) - taken
        return state.copy(reserve = state.reserve + taken) to wasted
    }

    fun isFull(state: AmmoState): Boolean = state.magazine >= state.magSize && state.reserve >= state.reserveCap

    /** "Infinite ammo only in Training Range / debug." */
    fun infiniteAllowed(context: AmmoContext): Boolean = context != AmmoContext.COMBAT

    /** The weapon you leave keeps its own magazine and reserve. */
    fun swap(from: WeaponInstance, to: WeaponInstance): Pair<WeaponInstance, WeaponInstance> = from to to

    /** Reload and ADS are both cancelled by a swap; the ammo is not. */
    fun reloadSurvivesSwap(): Boolean = false
}
