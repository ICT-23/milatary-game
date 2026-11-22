package com.example.game.valor.gdd

/**
 * Section 6: one drone of the support suite.
 *
 * The section header gates the whole suite at Colonel (rank 15), and the rank
 * table puts the swarm at Brigadier General (16). Cooldowns and durations are
 * filled only where Section 6 prints one.
 */
enum class DroneType(
    val displayName: String,
    val effect: String,
    val unlockRank: Int,
    val cooldownSeconds: Int? = null,
    val durationSeconds: Int? = null
) {
    ARIA_RECON(
        "ARIA Recon Drone", "Marks enemies through walls", 15,
        durationSeconds = 15
    ),
    STRIKE(
        "Strike Drone", "Player aims on the tactical map; drone fires a precision missile",
        15, cooldownSeconds = 60
    ),
    SWARM(
        "Swarm Drones", "Multiple small hits in an area", 16
    ),
    DEFENSE(
        "Defense Drone", "Hovers near the squad and shoots incoming rockets", 15
    ),
    PILOT(
        "Pilot mode", "Player briefly controls a drone camera in first-person FPV", 15
    )
}

object DroneSupport {

    const val COLONEL_RANK = 15
    const val BRIGADIER_GENERAL_RANK = 16

    val DRONES: List<DroneType> = DroneType.entries

    fun byName(name: String): DroneType? =
        DroneType.entries.firstOrNull { it.displayName.equals(name.trim(), ignoreCase = true) }

    fun availableAt(rank: Int, drone: DroneType): Boolean = rank >= drone.unlockRank

    fun unlockedAt(rank: Int): List<DroneType> = DroneType.entries.filter { rank >= it.unlockRank }

    /** "Limited battery/charges per mission to keep balance." */
    const val LIMITED_CHARGES = true

    /**
     * A drone with a printed cooldown is ready once that many seconds have passed;
     * one without a cooldown is ready whenever a charge remains.
     */
    fun isReady(drone: DroneType, secondsSinceLastUse: Float, chargesLeft: Int): Boolean {
        if (chargesLeft <= 0) return false
        val cooldown = drone.cooldownSeconds ?: return true
        return secondsSinceLastUse >= cooldown
    }

    /** Marks are only visible for the printed duration. */
    fun marksVisible(drone: DroneType, secondsSinceCast: Float): Boolean {
        val duration = drone.durationSeconds ?: return false
        return secondsSinceCast <= duration
    }
}

/** Section 5.5 + 6: what the radio can call, and the rank that unlocks it. */
data class SupportCall(
    val name: String,
    val detail: String,
    val unlockRank: Int?,
    val eraDependent: Boolean = false
)

object SupportCalls {

    /** Rank gate of the "radio support (smoke)" row in Section 4. */
    const val SMOKE_RANK = 9

    /** Rank gate of "artillery strike call-in" in Section 4. */
    const val ARTILLERY_RANK = 11

    val CALLS: List<SupportCall> = listOf(
        SupportCall("Smoke", "Radio support (smoke)", SMOKE_RANK),
        SupportCall("Mortar", "Other support: mortar", null),
        SupportCall("Artillery", "Artillery strike call-in", ARTILLERY_RANK),
        SupportCall(
            "Airstrike", "Era-dependent air support", null, eraDependent = true
        ),
        SupportCall("Medic call", "Medic call", null)
    )

    fun byName(name: String): SupportCall? =
        CALLS.firstOrNull { it.name.equals(name.trim(), ignoreCase = true) }

    /**
     * Calls with a printed rank are gated by it. Calls without one belong to the
     * era rather than the ladder, so they are never blocked by rank alone.
     */
    fun availableAt(rank: Int, call: SupportCall): Boolean =
        call.unlockRank?.let { rank >= it } ?: true

    fun rankGated(): List<SupportCall> = CALLS.filter { it.unlockRank != null }

    fun availableCalls(rank: Int): List<SupportCall> = CALLS.filter { availableAt(rank, it) }
}
