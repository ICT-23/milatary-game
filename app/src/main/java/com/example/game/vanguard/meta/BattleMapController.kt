package com.example.game.vanguard.meta

import com.example.game.vanguard.core.GameErrorCategory
import com.example.game.vanguard.core.GameResult

/**
 * Section 2 Meta: BattleMapController.
 *
 * Theater control is a pure fold over zone holdings, never a per-frame counter.
 * A zone moves through HOSTILE -> CONTESTED -> FRIENDLY -> SECURED only by
 * explicit shifts, so the strategic layer and the tactical mission cannot
 * disagree about who owns the map.
 */
enum class ZoneControl(val displayName: String, val controlValue: Int) {
    HOSTILE("Hostile", 0),
    CONTESTED("Contested", 50),
    FRIENDLY("Friendly", 100),
    SECURED("Secured", 100);

    val isHeld: Boolean get() = this == FRIENDLY || this == SECURED
}

data class BattleMapZone(
    val zoneId: String,
    val name: String,
    val sectorId: String,
    val control: ZoneControl = ZoneControl.HOSTILE,
    val garrisonStrength: Int = 0
)

/** One strategic tick result; [changed] is empty when nothing crossed a boundary. */
data class BattleMapUpdate(
    val zones: List<BattleMapZone>,
    val changed: List<String>
) {
    val friendlyCount: Int get() = zones.count { it.control.isHeld }
    val securedCount: Int get() = zones.count { it.control == ZoneControl.SECURED }
}

object BattleMapController {

    /** Pressure applied by a completed mission, expressed in control points. */
    const val MISSION_PRESSURE = 60
    const val ATTRITION_PRESSURE = -20

    private fun controlFor(points: Int): ZoneControl = when {
        points <= 0 -> ZoneControl.HOSTILE
        points < 50 -> ZoneControl.CONTESTED
        points < 100 -> ZoneControl.FRIENDLY
        else -> ZoneControl.SECURED
    }

    /** Advances one zone by [delta] control points, clamping at both ends. */
    fun shift(zone: BattleMapZone, delta: Int): BattleMapZone {
        val current = zone.control.controlValue
        val next = (current + delta).coerceIn(0, 100)
        return zone.copy(control = controlFor(next))
    }

    /**
     * Applies [delta] to every zone listed in [zoneIds]. Unknown ids fail the
     * whole tick so the caller cannot silently believe a sector moved.
     */
    fun advance(
        zones: List<BattleMapZone>,
        zoneIds: Collection<String>,
        delta: Int
    ): GameResult<BattleMapUpdate> {
        val unknown = zoneIds.filter { id -> zones.none { it.zoneId == id } }
        if (unknown.isNotEmpty()) {
            return GameResult.fail(
                GameErrorCategory.INVALID_STATE,
                "UNKNOWN_ZONE",
                "No battle map zone with id ${unknown.first()}"
            )
        }
        val targets = zoneIds.toSet()
        val changed = mutableListOf<String>()
        val next = zones.map { zone ->
            if (zone.zoneId !in targets) return@map zone
            val shifted = shift(zone, delta)
            if (shifted.control != zone.control) changed += zone.zoneId
            shifted
        }
        return GameResult.Ok(BattleMapUpdate(zones = next, changed = changed))
    }

    /** Share of the map under friendly control, 0f..1f. */
    fun overallControl(zones: List<BattleMapZone>): Float {
        if (zones.isEmpty()) return 0f
        return zones.sumOf { it.control.controlValue } / (zones.size * 100f)
    }

    /**
     * The next zone a commander should press: the hostile zone with the
     * weakest garrison, then by id so the choice is deterministic.
     */
    fun nextObjective(zones: List<BattleMapZone>): BattleMapZone? = zones
        .filter { !it.control.isHeld }
        .sortedWith(compareBy({ it.garrisonStrength }, { it.zoneId }))
        .firstOrNull()

    /** Securing every zone is the theater win condition. */
    fun isTheaterSecured(zones: List<BattleMapZone>): Boolean =
        zones.isNotEmpty() && zones.all { it.control == ZoneControl.SECURED }
}
