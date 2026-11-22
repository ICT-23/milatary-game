package com.example.game.valor.plan

import com.example.game.valor.gdd.Currencies

/** Section 5.17: the four currencies, named from the design table. */
enum class PlanCurrency(val displayName: String, val gameplay: Boolean) {
    CASH(Currencies.CASH, true),
    STEEL(Currencies.STEEL, true),
    PARTS(Currencies.PARTS, true),
    MEDALS(Currencies.MEDALS, false)
}

/** Section 5.17: balances are integers that never go negative. */
data class Wallet(
    val cash: Int = 0,
    val steel: Int = 0,
    val parts: Int = 0,
    val medals: Int = 0
) {
    fun amount(currency: PlanCurrency): Int = when (currency) {
        PlanCurrency.CASH -> cash
        PlanCurrency.STEEL -> steel
        PlanCurrency.PARTS -> parts
        PlanCurrency.MEDALS -> medals
    }

    fun with(currency: PlanCurrency, value: Int): Wallet {
        val safe = value.coerceIn(0, EconomyService.MAX_BALANCE)
        return when (currency) {
            PlanCurrency.CASH -> copy(cash = safe)
            PlanCurrency.STEEL -> copy(steel = safe)
            PlanCurrency.PARTS -> copy(parts = safe)
            PlanCurrency.MEDALS -> copy(medals = safe)
        }
    }

    fun canAfford(currency: PlanCurrency, cost: Int): Boolean =
        cost >= 0 && amount(currency) >= cost

    fun totalGameplay(): Int = cash + steel + parts
}

/** Section 5.17: the three blocks a purchase can hit before money is touched. */
data class PurchaseGate(
    val rankLocked: Boolean = false,
    val alreadyOwned: Boolean = false,
    val inventoryFull: Boolean = false,
    val consumable: Boolean = false
) {
    fun blockedReason(): String? = when {
        rankLocked -> "Rank locked"
        alreadyOwned && !consumable -> "Already owned"
        inventoryFull -> "Inventory full"
        else -> null
    }
}

data class PurchaseResult(val success: Boolean, val reason: String? = null, val wallet: Wallet)

/**
 * Section 5.17: every currency change goes through here. Spending is atomic:
 * check, deduct, grant, save - and a failed step rolls the balance back.
 */
class EconomyService(initialWallet: Wallet = Wallet()) {

    var wallet: Wallet = initialWallet
        private set

    /** Section 5.17 "duplicate-purchase guard: ignore taps while processing". */
    var processing: Boolean = false
        private set

    var completedPurchases: Int = 0
        private set

    fun earn(currency: PlanCurrency, amount: Int, reason: String, log: DebugLog? = null): Wallet {
        if (amount < 0) {
            log?.record(LogCategory.ECONOMY, "Negative earn rejected: $reason")
            return wallet
        }
        val next = wallet.amount(currency).toLong() + amount
        wallet = wallet.with(currency, next.coerceAtMost(MAX_BALANCE.toLong()).toInt())
        return wallet
    }

    fun spend(
        currency: PlanCurrency,
        amount: Int,
        reason: String,
        gate: PurchaseGate = PurchaseGate(),
        grant: () -> Boolean = { true },
        save: () -> Boolean = { true },
        log: DebugLog? = null
    ): PurchaseResult {
        if (processing) {
            log?.record(LogCategory.ECONOMY, "Ignored duplicate tap: $reason")
            return PurchaseResult(false, "Transaction already in progress", wallet)
        }
        gate.blockedReason()?.let { blocked ->
            log?.record(LogCategory.ECONOMY, "$blocked: $reason")
            return PurchaseResult(false, blocked, wallet)
        }
        if (amount < 0) {
            log?.record(LogCategory.ECONOMY, "Negative cost rejected: $reason")
            return PurchaseResult(false, "Cost cannot be negative", wallet)
        }
        if (!wallet.canAfford(currency, amount)) {
            log?.record(LogCategory.ECONOMY, "Insufficient ${currency.displayName}: $reason")
            return PurchaseResult(false, "Insufficient funds", wallet)
        }

        processing = true
        val before = wallet
        wallet = wallet.with(currency, wallet.amount(currency) - amount)

        if (!grant()) {
            wallet = before
            processing = false
            log?.record(LogCategory.ECONOMY, "Grant failed, rolled back: $reason")
            return PurchaseResult(false, "Grant failed, no charge", wallet)
        }
        if (!save()) {
            wallet = before
            processing = false
            log?.record(LogCategory.ECONOMY, "Save failed, rolled back: $reason")
            return PurchaseResult(false, "Save failed, no charge", wallet)
        }

        processing = false
        completedPurchases += 1
        return PurchaseResult(true, null, wallet)
    }

    companion object {
        const val MAX_BALANCE = Int.MAX_VALUE
        const val NO_NEGATIVE_BALANCES = true
        const val CLAMPS_AT_MAX = true
    }
}

/** Section 5.17: a rewarded reward exists only after the completion callback. */
object RewardedAdRules {

    const val GRANTS_ON_COMPLETION_ONLY = true

    fun grant(completed: Boolean, reward: () -> Unit): Boolean {
        if (!completed) return false
        reward()
        return true
    }
}

/** Section 5.18: dismantling returns half of the resources, never the fitted parts. */
object DismantleRules {

    const val REFUND_FRACTION = 0.5f

    fun refund(paid: Int): Int = (paid * REFUND_FRACTION).toInt()

    fun returnsEquippedParts(): Boolean = false
}
