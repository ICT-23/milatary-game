package com.example.game.vanguard.ui

import com.example.game.vanguard.combat.MechanicalState

/**
 * Section 2 UI: HUDController.
 *
 * Section 3.1 gate 1 requires the HUD cyclic rate to read the same variable the
 * physics tick enforces. This controller therefore never takes an RPM argument;
 * it derives every combat readout from the [MechanicalState] the domain produced.
 */
data class HudReadout(
    val rpmText: String,
    val ammoText: String,
    val safetyText: String,
    val reloadPromptVisible: Boolean,
    val obstructionWarningVisible: Boolean,
    val triggerReady: Boolean
)

object HudController {

    /** The HUD is a projection of domain state, never a second source of truth. */
    fun readout(state: MechanicalState, magazineCapacity: Int): HudReadout {
        val capacity = magazineCapacity.coerceAtLeast(0)
        val loaded = state.chamberedRounds.coerceIn(0, capacity)
        return HudReadout(
            rpmText = "${state.hudRpm} RPM",
            ammoText = "$loaded / $capacity",
            safetyText = state.safety.displayName,
            reloadPromptVisible = loaded == 0 && !state.isReloading,
            obstructionWarningVisible = state.isMuzzleObstructedByWall,
            triggerReady = state.isReadyToFire
        )
    }

    /** Reload needs a free safety position, a magazine with room, and no running reload. */
    fun reloadIsAvailable(state: MechanicalState, magazineCapacity: Int): Boolean =
        !state.isReloading &&
            state.safety.allowsBoltCycle &&
            state.chamberedRounds < magazineCapacity.coerceAtLeast(0)

    /** Bolts are gated by safety position 3, exactly like the domain rule. */
    fun boltIsAvailable(state: MechanicalState): Boolean =
        !state.isReloading && state.safety.allowsBoltCycle
}
