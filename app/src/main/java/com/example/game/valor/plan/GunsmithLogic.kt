package com.example.game.valor.plan

import com.example.game.valor.balance.GunsmithProgression

/** Section 5.18 craft flow, in the order the document lists it. */
enum class CraftStage(val displayName: String, val order: Int) {
    SELECT_WEAPON("Select base weapon", 1),
    SELECT_SLOT("Select slot", 2),
    CHOOSE_PART("Choose part", 3),
    COST_CHECK("Cost check", 4),
    CONFIRM("Confirm", 5),
    DEDUCT("Deduct resources", 6),
    ADD_TO_INVENTORY("Part added to inventory", 7),
    EQUIP("Equip", 8)
}

/** Section 5.18 "collect N pieces → unlock blueprint". */
class BlueprintState(val piecesNeeded: Int) {

    var pieces: Int = 0
        private set

    var crafted: Boolean = false
        private set

    val unlocked: Boolean get() = pieces >= piecesNeeded

    fun collect(amount: Int): Boolean {
        if (amount <= 0) return unlocked
        pieces += amount
        return unlocked
    }

    fun craft(): Boolean {
        if (!unlocked || crafted) return false
        crafted = true
        return true
    }
}

/** Section 5.18: the step-through that refuses any out-of-order stage. */
class CraftFlow {

    var stage: CraftStage = CraftStage.SELECT_WEAPON
        private set

    fun advance(): CraftStage? {
        val next = CraftStage.entries.firstOrNull { it.order == stage.order + 1 } ?: return null
        stage = next
        return next
    }

    /** A rejected cost check stops the flow where it is. */
    fun reset() {
        stage = CraftStage.SELECT_WEAPON
    }
}

/**
 * Section 5.18: upgrade levels 1-10 with no skipping, a preview that never
 * changes live stats, cosmetic-only skins and a 50% dismantle return.
 */
object GunsmithRules {

    const val MIN_LEVEL = GunsmithProgression.MIN_LEVEL
    const val MAX_LEVEL = GunsmithProgression.MAX_LEVEL
    const val NO_LEVEL_SKIPPING = true
    const val SKINS_ARE_COSMETIC = true
    const val PREVIEW_CHANGES_REAL_STATS = false

    val CRAFT_STAGES: List<CraftStage> = CraftStage.entries

    fun canUpgrade(currentLevel: Int): Boolean = currentLevel < MAX_LEVEL

    /** Only the next rung is purchasable, so a level can never be skipped. */
    fun canReach(currentLevel: Int, targetLevel: Int): Boolean =
        targetLevel == currentLevel + 1 && targetLevel in MIN_LEVEL..MAX_LEVEL

    fun upgradeCost(weaponPrice: Int, currentLevel: Int): Float =
        GunsmithProgression.upgradeCost(weaponPrice, currentLevel)

    fun steelCost(currentLevel: Int): Int = GunsmithProgression.steelCost(currentLevel)

    /** Damage and handling come from the tuned table, never from this file. */
    fun damageAt(baseDamage: Float, level: Int): Float =
        GunsmithProgression.upgradedDamage(baseDamage, level)

    fun recoilAt(baseRecoil: Float, level: Int): Float =
        GunsmithProgression.upgradedRecoil(baseRecoil, level)

    /** The preview returns the candidate numbers and leaves the live weapon alone. */
    fun preview(live: WeaponStats, candidate: WeaponStats): WeaponStats = candidate

    fun liveAfterPreview(live: WeaponStats): WeaponStats = live

    fun skinsChangeStats(): Boolean = !SKINS_ARE_COSMETIC

    fun dismantleRefund(paidCash: Int): Int = DismantleRules.refund(paidCash)

    /** Confirming is what commits the preview to the live weapon. */
    fun confirmPreview(live: WeaponStats, preview: WeaponStats, confirmed: Boolean): WeaponStats =
        if (confirmed) preview else live
}
