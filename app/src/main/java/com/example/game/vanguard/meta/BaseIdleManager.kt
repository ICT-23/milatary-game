package com.example.game.vanguard.meta

/**
 * Section 2 Meta: BaseIdleManager.
 *
 * The academy keeps producing while the app is closed. Accrual is a pure
 * function of two UTC timestamps and is capped, so a player cannot farm weeks
 * of intel by changing nothing but the wall clock forward.
 */
data class IdleRates(
    val intelPerHour: Float,
    val suppliesPerHour: Float
) {
    init {
        require(intelPerHour >= 0f && suppliesPerHour >= 0f) { "Idle rates cannot be negative" }
    }
}

data class IdleAccrual(
    val elapsedHours: Float,
    val creditedHours: Float,
    val intel: Int,
    val supplies: Int,
    val wasCapped: Boolean
)

object BaseIdleManager {

    const val MAX_OFFLINE_HOURS = 12f

    /** A rate table that matches the early campaign, used by the meta screen. */
    val ACADEMY_RATES = IdleRates(intelPerHour = 30f, suppliesPerHour = 45f)

    fun accrue(
        lastSeenUtcSeconds: Long,
        nowUtcSeconds: Long,
        rates: IdleRates = ACADEMY_RATES
    ): IdleAccrual {
        val rawHours = (nowUtcSeconds - lastSeenUtcSeconds).coerceAtLeast(0L) / 3_600f
        val credited = rawHours.coerceAtMost(MAX_OFFLINE_HOURS)
        return IdleAccrual(
            elapsedHours = rawHours,
            creditedHours = credited,
            intel = (credited * rates.intelPerHour).toInt(),
            supplies = (credited * rates.suppliesPerHour).toInt(),
            wasCapped = rawHours > MAX_OFFLINE_HOURS
        )
    }

    /** Rewards are paid once per claim window, never per frame. */
    fun claimToken(lastSeenUtcSeconds: Long, nowUtcSeconds: Long): String =
        "IDLE_CLAIM_${lastSeenUtcSeconds}_$nowUtcSeconds"
}
