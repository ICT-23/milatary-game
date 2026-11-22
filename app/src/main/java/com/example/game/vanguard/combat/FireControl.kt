package com.example.game.vanguard.combat

import com.example.game.vanguard.core.GameErrorCategory
import com.example.game.vanguard.core.GameResult
import kotlin.math.roundToInt

/**
 * Section 3.1 gate 1 / Section 3.2 Socket_TriggerGroup:
 * the three-position manual safety. The position is authoritative for both the
 * trigger and the bolt, so the UI never has to infer what a click should do.
 */
enum class SafetyMode(
    val position: Int,
    val displayName: String,
    val blocksTrigger: Boolean,
    val blocksBolt: Boolean
) {
    POS1_FIRE_AND_RELOAD(1, "Pos 1 - Fire + Reload", blocksTrigger = false, blocksBolt = false),
    POS2_SAFE_LOAD_UNLOAD(2, "Pos 2 - Safe Load / Unload", blocksTrigger = true, blocksBolt = false),
    POS3_FULL_LOCK(3, "Pos 3 - Full Lock", blocksTrigger = true, blocksBolt = true);

    val allowsFiring: Boolean get() = !blocksTrigger
    val allowsBoltCycle: Boolean get() = !blocksBolt
}

/**
 * Everything gate 1 needs, in one immutable value. [nextAllowedFireTimestamp] is
 * the single variable the HUD reads, which is what prevents a menu from
 * advertising a different cyclic rate than the physics tick enforces.
 */
data class MechanicalState(
    val safety: SafetyMode = SafetyMode.POS1_FIRE_AND_RELOAD,
    val hasChamberedRound: Boolean = true,
    val isBoltLockedClosed: Boolean = true,
    val isReloading: Boolean = false,
    val isMuzzleObstructedByWall: Boolean = false,
    val truePhysicsRpm: Float = 750f,
    val nextAllowedFireTimestamp: Float = 0f,
    val chamberedRounds: Int = 1
) {
    /** 60.0 / TruePhysicsRPM, exactly as the specification states it. */
    val fireIntervalSeconds: Float get() = 60f / truePhysicsRpm.coerceAtLeast(1f)

    /** Derived from the same interval, so a stat card can never desynchronise. */
    val hudRpm: Int get() = (60f / fireIntervalSeconds).roundToInt()

    val isReadyToFire: Boolean
        get() = safety.allowsFiring &&
            hasChamberedRound &&
            isBoltLockedClosed &&
            !isReloading &&
            !isMuzzleObstructedByWall
}

data class FireAuthorization(
    val state: MechanicalState,
    val firedAtSeconds: Float,
    val roundConsumed: Boolean
)

/**
 * Gate 1. Evaluation order is fixed by the contract; the first failing gate
 * produces the error, so a blocked shot never reaches ballistics.
 */
object FireControl {

    fun evaluate(nowSeconds: Float, state: MechanicalState): GameResult<MechanicalState> {
        if (state.safety.blocksTrigger) {
            return GameResult.fail(
                GameErrorCategory.SAFETY,
                "SAFETY_BLOCKED",
                "Safety ${state.safety.displayName} blocks the trigger"
            )
        }
        if (!state.hasChamberedRound || state.chamberedRounds <= 0) {
            return GameResult.fail(
                GameErrorCategory.AMMUNITION,
                "NO_CHAMBERED_ROUND",
                "No chambered round"
            )
        }
        if (!state.isBoltLockedClosed) {
            return GameResult.fail(
                GameErrorCategory.NOT_READY,
                "BOLT_OPEN",
                "Bolt is not locked closed"
            )
        }
        if (state.isReloading) {
            return GameResult.fail(
                GameErrorCategory.NOT_READY,
                "RELOADING",
                "A reload is in progress"
            )
        }
        if (state.isMuzzleObstructedByWall) {
            return GameResult.fail(
                GameErrorCategory.GEOMETRY,
                "MUZZLE_OBSTRUCTED",
                "Muzzle is obstructed by geometry"
            )
        }
        if (nowSeconds < state.nextAllowedFireTimestamp) {
            return GameResult.fail(
                GameErrorCategory.TIMING,
                "RATE_LIMITED",
                "Cyclic rate cooldown has not elapsed"
            )
        }
        return GameResult.Ok(state)
    }

    /**
     * Commits a legal trigger pull: consumes the chambered round and schedules
     * the next shot from [MechanicalState.fireIntervalSeconds].
     */
    fun discharge(nowSeconds: Float, state: MechanicalState): GameResult<FireAuthorization> {
        val ready = evaluate(nowSeconds, state)
        if (ready is GameResult.Err) return ready
        val remaining = (state.chamberedRounds - 1).coerceAtLeast(0)
        val advanced = state.copy(
            hasChamberedRound = remaining > 0,
            chamberedRounds = remaining,
            nextAllowedFireTimestamp = nowSeconds + state.fireIntervalSeconds
        )
        return GameResult.Ok(
            FireAuthorization(
                state = advanced,
                firedAtSeconds = nowSeconds,
                roundConsumed = true
            )
        )
    }

    /** Chambering a round is only legal while the bolt can move. */
    fun chamberRound(state: MechanicalState): GameResult<MechanicalState> {
        if (!state.safety.allowsBoltCycle) {
            return GameResult.fail(
                GameErrorCategory.SAFETY,
                "BOLT_LOCKED",
                "Safety ${state.safety.displayName} locks the bolt carrier"
            )
        }
        return GameResult.Ok(state.copy(hasChamberedRound = true, chamberedRounds = 1))
    }

    /**
     * Section 3.2 Socket_Magazine: a dry reload opens the bolt, so the weapon is
     * not fireable until [chamberRound] runs again.
     */
    fun dryReload(state: MechanicalState): GameResult<MechanicalState> {
        if (!state.safety.allowsBoltCycle) {
            return GameResult.fail(
                GameErrorCategory.SAFETY,
                "BOLT_LOCKED",
                "Safety ${state.safety.displayName} locks the bolt carrier"
            )
        }
        return GameResult.Ok(
            state.copy(
                isReloading = true,
                isBoltLockedClosed = false,
                hasChamberedRound = false,
                chamberedRounds = 0
            )
        )
    }

    /**
     * A tactical reload keeps the chambered round, so the operator stays ready
     * to fire the instant the fresh magazine seats.
     */
    fun finishReload(state: MechanicalState, retainedChamberedRound: Boolean): MechanicalState =
        state.copy(
            isReloading = false,
            isBoltLockedClosed = true,
            hasChamberedRound = retainedChamberedRound || state.hasChamberedRound,
            chamberedRounds = if (retainedChamberedRound || state.hasChamberedRound) 1 else 0
        )
}
