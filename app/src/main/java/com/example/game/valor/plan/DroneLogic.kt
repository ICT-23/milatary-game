package com.example.game.valor.plan

import com.example.game.valor.gdd.DroneSupport
import com.example.game.valor.gdd.DroneType

/** Section 5.13: one FPV sortie, which saves and restores the player's state. */
data class FpvState(
    val batterySeconds: Float,
    val bodyVulnerable: Boolean = true,
    val savedCameraPose: String = ""
)

/** Section 5.13: at most one active call of each type at a time. */
class ActiveCalls {

    private val byType = mutableMapOf<DroneType, String>()

    fun begin(type: DroneType, callId: String): Boolean {
        if (byType.containsKey(type)) return false
        byType[type] = callId
        return true
    }

    fun finish(type: DroneType, callId: String) {
        if (byType[type] == callId) byType.remove(type)
    }

    fun active(type: DroneType): String? = byType[type]

    fun activeCount(): Int = byType.size
}

/** Section 5.13: the charges and cooldowns that gate every call. */
class DroneLoadout(initialCharges: Map<DroneType, Int>) {

    private val charges = initialCharges.toMutableMap()
    private val lastUseSeconds = mutableMapOf<DroneType, Float>()

    fun chargesLeft(drone: DroneType): Int = charges[drone] ?: 0

    fun secondsSinceLastUse(drone: DroneType, nowSeconds: Float): Float {
        val last = lastUseSeconds[drone] ?: return Float.MAX_VALUE
        return nowSeconds - last
    }

    /** Consumes exactly one charge and stamps the cooldown clock. */
    fun consume(drone: DroneType, nowSeconds: Float): Boolean {
        val left = chargesLeft(drone)
        if (left <= 0) return false
        charges[drone] = left - 1
        lastUseSeconds[drone] = nowSeconds
        return true
    }

    fun refund(drone: DroneType) {
        charges[drone] = chargesLeft(drone) + 1
    }
}

/**
 * Section 5.13: rank gate, charges, cooldowns, the strike delay with its
 * no-strike zone, the swarm window and the FPV battery.
 */
object DroneRules {

    const val ONE_ACTIVE_CALL_PER_TYPE = true
    const val STRIKE_DELAY_SECONDS = 2f
    const val SWARM_DURATION_SECONDS = 3f
    const val RECON_MARKS_VISUAL_ONLY = true
    const val FPV_LEAVING_RESTORES_STATE = true
    const val NEEDS_CHARGE_AND_COOLDOWN = true

    /** "Unlocked at Colonel (rank 15)", read from the design table. */
    fun unlockedAt(rank: Int, drone: DroneType): Boolean = DroneSupport.availableAt(rank, drone)

    fun canCall(
        rank: Int,
        drone: DroneType,
        chargesLeft: Int,
        secondsSinceLastUse: Float
    ): Boolean = unlockedAt(rank, drone) && DroneSupport.isReady(drone, secondsSinceLastUse, chargesLeft)

    /** Recon marks last 15 s and show through walls; visual only, no damage. */
    fun marksVisible(drone: DroneType, secondsSinceCast: Float): Boolean =
        DroneSupport.marksVisible(drone, secondsSinceCast)

    /** Marks are removed when the marked enemy dies. */
    fun marksAfterDeaths(marks: List<String>, deadIds: Set<String>): List<String> =
        marks.filterNot { it in deadIds }

    fun reconDealsDamage(): Boolean = false

    /** "Cannot target inside a no-strike zone (civilians/allies in radius)." */
    fun canTarget(noStrikeZone: Boolean, alliesInRadius: Boolean, civiliansInRadius: Boolean): Boolean =
        !noStrikeZone && !alliesInRadius && !civiliansInRadius

    fun strikeDelaySeconds(): Float = STRIKE_DELAY_SECONDS

    /** Swarm: N small hits across the 3 s window, one every [intervalSeconds]. */
    fun swarmHits(intervalSeconds: Float): Int =
        if (intervalSeconds <= 0f) 0 else (SWARM_DURATION_SECONDS / intervalSeconds).toInt().coerceAtLeast(1)

    fun enterFpv(batterySeconds: Float, savedCameraPose: String): FpvState =
        FpvState(batterySeconds = batterySeconds, savedCameraPose = savedCameraPose)

    /** The body stays vulnerable while the player is in the drone. */
    fun bodyVulnerable(state: FpvState): Boolean = state.bodyVulnerable

    fun tickFpv(state: FpvState, deltaSeconds: Float): FpvState =
        state.copy(batterySeconds = (state.batterySeconds - deltaSeconds).coerceAtLeast(0f))

    fun fpvExpired(state: FpvState): Boolean = state.batterySeconds <= 0f

    /** Leaving restores the exact pose the player left. */
    fun exitFpv(state: FpvState): String = state.savedCameraPose
}
