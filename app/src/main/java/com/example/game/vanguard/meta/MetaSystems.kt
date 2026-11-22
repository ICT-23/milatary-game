package com.example.game.vanguard.meta

import com.example.game.vanguard.core.GameErrorCategory
import com.example.game.vanguard.core.GameResult

/**
 * Section 3.4 item 2: production queues are driven by UTC timestamps only.
 *
 * `elapsed = now - startUtc` means a player who closes the app for six hours
 * returns to six hours of progress, and two devices with the same clock agree to
 * the second. No frame delta is ever stored.
 */
enum class ProductionKind(val displayName: String) {
    NAVAL_SHIP("Naval Ship"),
    GROUND_VEHICLE("Ground Vehicle")
}

data class ProductionOrder(
    val id: String,
    val name: String,
    val kind: ProductionKind,
    val stationId: String,
    val startUtcSeconds: Long,
    val durationSeconds: Long
) {
    init {
        require(durationSeconds > 0L) { "A production order needs a positive duration" }
    }
}

data class ProductionProgress(
    val order: ProductionOrder,
    val elapsedSeconds: Long,
    val progress01: Float,
    val remainingSeconds: Long
) {
    val isComplete: Boolean get() = progress01 >= 1f
}

object ProductionQueue {

    fun progress(order: ProductionOrder, nowUtcSeconds: Long): ProductionProgress {
        val elapsed = (nowUtcSeconds - order.startUtcSeconds).coerceAtLeast(0L)
        val fraction = (elapsed.toFloat() / order.durationSeconds.toFloat()).coerceIn(0f, 1f)
        val remaining = (order.durationSeconds - elapsed).coerceAtLeast(0L)
        return ProductionProgress(order, elapsed, fraction, remaining)
    }

    fun progressAll(orders: List<ProductionOrder>, nowUtcSeconds: Long): List<ProductionProgress> =
        orders.map { progress(it, nowUtcSeconds) }

    fun completed(orders: List<ProductionOrder>, nowUtcSeconds: Long): List<ProductionProgress> =
        progressAll(orders, nowUtcSeconds).filter { it.isComplete }

    fun unfinished(orders: List<ProductionOrder>, nowUtcSeconds: Long): List<ProductionOrder> =
        progressAll(orders, nowUtcSeconds).filterNot { it.isComplete }.map { it.order }

    /**
     * Offline catch-up. Returns what finished while the app was closed and the
     * queue that is still running; the caller pays rewards once per order id.
     */
    fun catchUp(orders: List<ProductionOrder>, nowUtcSeconds: Long): Pair<List<ProductionProgress>, List<ProductionOrder>> =
        completed(orders, nowUtcSeconds) to unfinished(orders, nowUtcSeconds)
}

// -------------------------------------------------------------- presidency

enum class BudgetSector(val displayName: String) {
    CIVIL_INFRASTRUCTURE("Civil Infrastructure"),
    DEFENSE_RND("Defense R&D"),
    NAVAL_GROUND_FLEETS("Naval / Ground Fleets")
}

/** Whole-percentage split of the national budget; the three must total 100. */
data class BudgetAllocation(
    val civilInfrastructure: Int = 34,
    val defenseRnd: Int = 33,
    val navalGroundFleets: Int = 33
) {
    val total: Int get() = civilInfrastructure + defenseRnd + navalGroundFleets

    operator fun get(sector: BudgetSector): Int = when (sector) {
        BudgetSector.CIVIL_INFRASTRUCTURE -> civilInfrastructure
        BudgetSector.DEFENSE_RND -> defenseRnd
        BudgetSector.NAVAL_GROUND_FLEETS -> navalGroundFleets
    }
}

data class GovernanceBudget(
    val grossBillions: Float,
    val allocation: BudgetAllocation,
    val civilInfrastructureBillions: Float,
    val defenseRndBillions: Float,
    val fleetBillions: Float,
    val nationalDevelopmentIndex: Float
)

/**
 * Section 3.4 item 3: the Commander-in-Chief loop. Allocation must be complete
 * before it can be enacted, and the development index rewards balanced spending
 * rather than a single maxed line item.
 */
object PresidentialGovernance {

    fun allocate(gdpBillions: Float, allocation: BudgetAllocation): GameResult<GovernanceBudget> {
        if (allocation.total != 100) {
            return GameResult.fail(
                GameErrorCategory.ECONOMY,
                "BUDGET_NOT_ALLOCATED",
                "The three sectors must total exactly 100 percent"
            )
        }
        if (allocation.civilInfrastructure < 0 || allocation.defenseRnd < 0 || allocation.navalGroundFleets < 0) {
            return GameResult.fail(
                GameErrorCategory.ECONOMY,
                "NEGATIVE_BUDGET_LINE",
                "No sector may receive a negative allocation"
            )
        }
        if (gdpBillions <= 0f) {
            return GameResult.fail(
                GameErrorCategory.ECONOMY,
                "NO_GDP",
                "Gross domestic product must be positive"
            )
        }

        val civil = gdpBillions * allocation.civilInfrastructure / 100f
        val defense = gdpBillions * allocation.defenseRnd / 100f
        val fleets = gdpBillions * allocation.navalGroundFleets / 100f
        return GameResult.Ok(
            GovernanceBudget(
                grossBillions = gdpBillions,
                allocation = allocation,
                civilInfrastructureBillions = civil,
                defenseRndBillions = defense,
                fleetBillions = fleets,
                nationalDevelopmentIndex = developmentIndex(allocation)
            )
        )
    }

    /**
     * 0-100 index: a perfectly even split scores highest, because the doctrine
     * loop is about balancing the nation rather than starving two sectors.
     */
    fun developmentIndex(allocation: BudgetAllocation): Float {
        if (allocation.total != 100) return 0f
        val ideal = 100f / BudgetSector.entries.size
        val deviation = BudgetSector.entries
            .sumOf { kotlin.math.abs(allocation[it] - ideal).toDouble() }
            .toFloat()
        return (100f - deviation).coerceIn(0f, 100f)
    }

    /** Rebalance helper for the policy sliders; never returns an invalid split. */
    fun rebalance(sector: BudgetSector, newValue: Int): GameResult<BudgetAllocation> {
        val clamped = newValue.coerceIn(0, 100)
        val remaining = 100 - clamped
        val firstShare = (remaining + 1) / 2
        val secondShare = remaining - firstShare
        val updated = when (sector) {
            BudgetSector.CIVIL_INFRASTRUCTURE ->
                BudgetAllocation(clamped, firstShare, secondShare)
            BudgetSector.DEFENSE_RND ->
                BudgetAllocation(firstShare, clamped, secondShare)
            BudgetSector.NAVAL_GROUND_FLEETS ->
                BudgetAllocation(firstShare, secondShare, clamped)
        }
        if (updated.total != 100) {
            return GameResult.fail(
                GameErrorCategory.ECONOMY,
                "REBALANCE_FAILED",
                "Rebalancing did not produce a complete budget"
            )
        }
        return GameResult.Ok(updated)
    }
}
