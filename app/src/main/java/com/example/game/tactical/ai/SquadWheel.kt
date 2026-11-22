package com.example.game.tactical.ai

import com.example.game.tactical.progression.RankLadder

/**
 * Phase 5 deliverable: the Squad Command Wheel.
 *
 * Four orders, exactly as the plan lists them: Follow, Hold Position, Suppress
 * Target and Flank. The wheel reports a refusal instead of silently ignoring a
 * press, so the player always knows what the squad did.
 */
enum class SquadOrder(val displayName: String, val requiresTarget: Boolean) {
    FOLLOW("Follow", requiresTarget = false),
    HOLD_POSITION("Hold Position", requiresTarget = false),
    SUPPRESS_TARGET("Suppress Target", requiresTarget = true),
    FLANK("Flank", requiresTarget = true)
}

enum class SquadAvailability(val label: String) {
    READY("READY"),
    REFUSED_DOWNED("REFUSED: SQUADMATE DOWN"),
    REFUSED_NO_TARGET("REFUSED: NO TARGET DESIGNATED"),
    REFUSED_RANK("REFUSED: SQUAD COMMAND NOT UNLOCKED"),
    REFUSED_ALREADY_THERE("NO CHANGE: ALREADY IN POSITION")
}

/** Sergeant (tier 3) unlocks the full four-man wheel. */
const val FULL_SQUAD_COMMAND_RANK_INDEX = 3

data class SquadMate(
    val id: String,
    val callsign: String,
    val healthPercent: Int = 100,
    val isDowned: Boolean = false
) {
    val isAvailable: Boolean get() = !isDowned && healthPercent > 0
}

data class SquadOrderResult(
    val order: SquadOrder,
    val mateId: String,
    val availability: SquadAvailability,
    val message: String
) {
    val accepted: Boolean get() = availability == SquadAvailability.READY
}

object SquadCommandWheel {

    /** Corporal (tier 2) grants a single wingman; Sergeant grants the full wheel. */
    const val WINGMAN_RANK_INDEX = 2

    fun commandSlots(rankIndex: Int): Int = when {
        rankIndex >= FULL_SQUAD_COMMAND_RANK_INDEX -> 4
        rankIndex >= WINGMAN_RANK_INDEX -> 1
        else -> 0
    }

    fun isUnlocked(rankIndex: Int): Boolean = RankLadder.hasClearance(rankIndex, WINGMAN_RANK_INDEX)

    /** The mates the player may actually command at this rank. */
    fun orderedSquad(rankIndex: Int, mates: List<SquadMate>): List<SquadMate> =
        mates.filter { it.isAvailable }.take(commandSlots(rankIndex))

    fun issue(
        order: SquadOrder,
        mate: SquadMate,
        rankIndex: Int,
        hasDesignatedTarget: Boolean,
        alreadyInPosition: Boolean = false
    ): SquadOrderResult {
        if (!isUnlocked(rankIndex)) {
            return SquadOrderResult(
                order, mate.id, SquadAvailability.REFUSED_RANK,
                "Squad commands unlock at Corporal (${RankLadder.tierAt(WINGMAN_RANK_INDEX).code})"
            )
        }
        if (!mate.isAvailable) {
            return SquadOrderResult(
                order, mate.id, SquadAvailability.REFUSED_DOWNED,
                "${mate.callsign} is down and cannot receive orders"
            )
        }
        if (order.requiresTarget && !hasDesignatedTarget) {
            return SquadOrderResult(
                order, mate.id, SquadAvailability.REFUSED_NO_TARGET,
                "Designate a target before ordering ${order.displayName.lowercase()}"
            )
        }
        if (order == SquadOrder.HOLD_POSITION && alreadyInPosition) {
            return SquadOrderResult(
                order, mate.id, SquadAvailability.REFUSED_ALREADY_THERE,
                "${mate.callsign} is already holding this position"
            )
        }
        val message = when (order) {
            SquadOrder.FOLLOW -> "${mate.callsign} is falling in on you"
            SquadOrder.HOLD_POSITION -> "${mate.callsign} is holding position"
            SquadOrder.SUPPRESS_TARGET -> "${mate.callsign} is suppressing the target"
            SquadOrder.FLANK -> "${mate.callsign} is flanking wide"
        }
        return SquadOrderResult(order, mate.id, SquadAvailability.READY, message)
    }

    fun wheelFor(rankIndex: Int): List<SquadOrder> =
        if (isUnlocked(rankIndex)) SquadOrder.entries.toList() else emptyList()
}
