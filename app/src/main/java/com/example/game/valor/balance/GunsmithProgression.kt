package com.example.game.valor.balance

import kotlin.math.pow

/** Section 2.6: what each mastery tier hands back. */
data class MasteryTier(val kills: Int, val reward: String)

object GunsmithProgression {

    const val MIN_LEVEL = 1
    const val MAX_LEVEL = 10

    const val DAMAGE_PER_LEVEL = 0.03f
    const val HANDLING_PER_LEVEL = 0.02f
    const val ACCURACY_PER_LEVEL = 0.01f

    /** "baseCost = 10% of weapon price (min 150)". */
    const val BASE_COST_FRACTION = 0.10f
    const val MIN_BASE_COST = 150f
    const val COST_GROWTH = 1.35f

    /** "Also costs Steel: 2 x level". */
    const val STEEL_PER_LEVEL = 2

    val MASTERY_TIERS: List<MasteryTier> = listOf(
        MasteryTier(50, "Camo"),
        MasteryTier(150, "Charm"),
        MasteryTier(400, "Special attachment"),
        MasteryTier(1_000, "Mastery emblem"),
        MasteryTier(2_500, "Signature finish")
    )

    /** Levels gained past the as-purchased level 1. */
    private fun stepsAt(level: Int): Int = level.coerceIn(MIN_LEVEL, MAX_LEVEL) - 1

    fun damageMultiplier(level: Int): Float = 1f + stepsAt(level) * DAMAGE_PER_LEVEL

    /** Handling is a recoil reduction, so the value is subtracted from recoil. */
    fun handlingMultiplier(level: Int): Float = 1f + stepsAt(level) * HANDLING_PER_LEVEL

    fun accuracyMultiplier(level: Int): Float = 1f + stepsAt(level) * ACCURACY_PER_LEVEL

    fun baseCost(weaponPrice: Int): Float =
        (weaponPrice * BASE_COST_FRACTION).coerceAtLeast(MIN_BASE_COST)

    /**
     * Cost of the step that leaves [currentLevel] for the next one, exactly as
     * the table states it: baseCost x 1.35^(level - 1). Level 10 has no step.
     */
    fun upgradeCost(weaponPrice: Int, currentLevel: Int): Float {
        if (currentLevel >= MAX_LEVEL) return 0f
        val level = currentLevel.coerceAtLeast(MIN_LEVEL)
        return baseCost(weaponPrice) * COST_GROWTH.toDouble().pow(level - 1).toFloat()
    }

    fun steelCost(currentLevel: Int): Int =
        if (currentLevel >= MAX_LEVEL) 0 else STEEL_PER_LEVEL * currentLevel.coerceAtLeast(MIN_LEVEL)

    /** Cash spent taking a weapon from level 1 to [level]. */
    fun totalCashTo(weaponPrice: Int, level: Int): Float =
        (MIN_LEVEL until level.coerceIn(MIN_LEVEL, MAX_LEVEL))
            .sumOf { upgradeCost(weaponPrice, it).toDouble() }
            .toFloat()

    /** Steel spent taking a weapon from level 1 to [level]. */
    fun totalSteelTo(level: Int): Int =
        (MIN_LEVEL until level.coerceIn(MIN_LEVEL, MAX_LEVEL)).sumOf { steelCost(it) }

    fun tierForKills(kills: Int): MasteryTier? = MASTERY_TIERS.lastOrNull { kills >= it.kills }

    fun nextTier(kills: Int): MasteryTier? = MASTERY_TIERS.firstOrNull { kills < it.kills }

    /** Applies the level multipliers to a base figure; order is damage, then accuracy. */
    fun upgradedDamage(baseDamage: Float, level: Int): Float = baseDamage * damageMultiplier(level)

    fun upgradedRecoil(baseRecoil: Float, level: Int): Float =
        baseRecoil / handlingMultiplier(level)
}
