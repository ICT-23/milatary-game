package com.example.game.squad

/** The six squad roles from section 10.3. */
enum class SquadRole(
    val title: String,
    val contribution: String,
    val exampleCommand: String
) {
    SCOUT("Scout", "Spots routes and points of interest", "Mark / observe location"),
    MEDIC("Medic", "Supports downed teammates", "Assist / regroup"),
    ENGINEER("Engineer", "Interacts with repair and technical objectives", "Repair / restore device"),
    SUPPORT("Support", "Helps maintain team coverage in a mission", "Hold position / support"),
    MARKSMAN("Marksman", "Covers a designated observation point in scripted missions", "Observe / cover area"),
    TEAM_LEADER("Team leader", "Coordinates squad status and order availability", "Regroup / move to marker")
}

/** The initial command set from section 10.4. */
enum class SquadOrder(val title: String, val detail: String) {
    FOLLOW_ME("Follow me", "Squadmate moves with the player"),
    MOVE_TO_MARKER("Move to marker", "Squadmate advances to the marked point"),
    HOLD_POSITION("Hold position", "Squadmate stays and covers the current position"),
    REGROUP("Regroup", "Squadmate returns to the player"),
    OBSERVE_AREA("Observe area", "Squadmate watches the marked area and reports"),
    INTERACT_OBJECTIVE("Interact with objective", "Squadmate works the assigned mission objective"),
    ASSIST_REVIVE("Assist / revive", "Squadmate stabilises a downed teammate")
}

/** Explicit outcome states so an order is never silently dropped. */
enum class OrderStatus(val label: String) {
    ACCEPTED("ORDER RECEIVED"),
    IN_PROGRESS("ORDER IN PROGRESS"),
    UNREACHABLE("ORDER BLOCKED: LOCATION UNREACHABLE"),
    ROLE_MISMATCH("ORDER BLOCKED: ROLE CANNOT PERFORM TASK"),
    DOWNED("ORDER BLOCKED: SQUADMATE DOWN"),
    ALREADY_THERE("ORDER COMPLETE: ALREADY IN POSITION")
}

/** Live state of one squadmate. Kept engine agnostic. */
data class SquadmateState(
    val id: String,
    val callsign: String,
    val role: SquadRole,
    val trust: Int = 50,
    val healthPercent: Int = 100,
    val isDowned: Boolean = false,
    val x: Float = 0f,
    val y: Float = 0f
) {
    val isAvailable: Boolean get() = !isDowned && healthPercent > 0
}

/** The result of pressing a squad-order button. */
data class SquadOrderResult(
    val order: SquadOrder,
    val squadmateId: String,
    val status: OrderStatus,
    val message: String,
    val movesSquadmate: Boolean = false
)

/**
 * Context-aware order handling from section 10.4. An order that cannot be
 * carried out reports a clear status instead of disappearing.
 */
object SquadCommandSystem {

    /** Orders only a technical specialist can own. */
    private val TECHNICAL_ORDERS = setOf(SquadOrder.INTERACT_OBJECTIVE)

    /** Orders that only make sense with a downed teammate present. */
    private val RESCUE_ORDERS = setOf(SquadOrder.ASSIST_REVIVE)

    /**
     * @param reachable whether the marker can actually be pathed to
     * @param teammateDown whether any squadmate needs assistance
     * @param distanceToMarker metres between the squadmate and the marker
     */
    fun issue(
        order: SquadOrder,
        squadmate: SquadmateState,
        reachable: Boolean = true,
        teammateDown: Boolean = false,
        distanceToMarker: Float = 99f
    ): SquadOrderResult {
        if (!squadmate.isAvailable) {
            return SquadOrderResult(
                order, squadmate.id, OrderStatus.DOWNED,
                "${squadmate.callsign} is down and cannot receive orders."
            )
        }

        if (order in TECHNICAL_ORDERS && squadmate.role != SquadRole.ENGINEER) {
            return SquadOrderResult(
                order, squadmate.id, OrderStatus.ROLE_MISMATCH,
                "${squadmate.callsign} is ${squadmate.role.title.lowercase()} " +
                    "and cannot work a technical objective. Assign the engineer."
            )
        }

        if (order in RESCUE_ORDERS && squadmate.role != SquadRole.MEDIC) {
            return SquadOrderResult(
                order, squadmate.id, OrderStatus.ROLE_MISMATCH,
                "Only the medic can stabilise a downed teammate."
            )
        }

        if (order in RESCUE_ORDERS && !teammateDown) {
            return SquadOrderResult(
                order, squadmate.id, OrderStatus.ACCEPTED,
                "${squadmate.callsign} holds the aid kit ready; nobody needs assistance."
            )
        }

        if ((order == SquadOrder.MOVE_TO_MARKER || order == SquadOrder.OBSERVE_AREA) && !reachable) {
            return SquadOrderResult(
                order, squadmate.id, OrderStatus.UNREACHABLE,
                "${squadmate.callsign} cannot path to that marker. Choose another position."
            )
        }

        if (order == SquadOrder.REGROUP && distanceToMarker < 1.5f) {
            return SquadOrderResult(
                order, squadmate.id, OrderStatus.ALREADY_THERE,
                "${squadmate.callsign} is already on you."
            )
        }

        val acknowledged = when (order) {
            SquadOrder.FOLLOW_ME -> "${squadmate.callsign} falling in on you."
            SquadOrder.MOVE_TO_MARKER -> "${squadmate.callsign} moving to the marker."
            SquadOrder.HOLD_POSITION -> "${squadmate.callsign} holding this position."
            SquadOrder.REGROUP -> "${squadmate.callsign} regrouping."
            SquadOrder.OBSERVE_AREA -> "${squadmate.callsign} observing the marked area."
            SquadOrder.INTERACT_OBJECTIVE -> "${squadmate.callsign} working the objective."
            SquadOrder.ASSIST_REVIVE -> "${squadmate.callsign} moving to stabilise."
        }

        val moves = order == SquadOrder.FOLLOW_ME ||
            order == SquadOrder.MOVE_TO_MARKER ||
            order == SquadOrder.REGROUP
        return SquadOrderResult(order, squadmate.id, OrderStatus.ACCEPTED, acknowledged, moves)
    }

    /** Orders a role can be given without a role-mismatch refusal. */
    fun ordersFor(role: SquadRole): List<SquadOrder> = when (role) {
        SquadRole.ENGINEER -> SquadOrder.values().toList()
        SquadRole.MEDIC -> SquadOrder.values().toList()
        SquadRole.SCOUT, SquadRole.MARKSMAN, SquadRole.SUPPORT, SquadRole.TEAM_LEADER ->
            SquadOrder.values().filter { it !in TECHNICAL_ORDERS && it !in RESCUE_ORDERS }
    }
}
