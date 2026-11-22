package com.example.game.valor.balance

/** Section 3 slot column. */
enum class GearSlot(val displayName: String) {
    HELMET("Helmet"),
    VEST("Vest"),
    MEDICAL("Medical"),
    MASK("Gas mask"),
    OPTICS("Optics"),
    BACKPACK("Backpack")
}

/**
 * Section 3: one row of the armour and gear table.
 *
 * The typed fields carry only what the table states numerically; [effect] keeps
 * the printed sentence so the shop can show the same wording the bible uses.
 */
data class GearItem(
    val id: String,
    val name: String,
    val slot: GearSlot,
    val tier: Int?,
    val effect: String,
    val price: Int,
    val unlockRank: Int,
    val headMultiplier: Float? = null,
    val armorPoints: Float? = null,
    val armorAbsorb: Float? = null,
    val healFraction: Float? = null,
    val useSeconds: Float? = null,
    val gasReduction: Float? = null,
    val reserveBonusPercent: Int? = null
)

object GearCatalog {

    /** Section 3: helmets lower the 2.5 head multiplier. */
    val HELMETS: List<GearItem> = listOf(
        GearItem("helmet_1", "Helmet", GearSlot.HELMET, 1, "Headshot mult 2.5 -> 2.0", 500, 3,
            headMultiplier = 2.0f),
        GearItem("helmet_2", "Helmet", GearSlot.HELMET, 2, "Headshot mult 2.5 -> 1.5", 3_000, 9,
            headMultiplier = 1.5f),
        GearItem("helmet_3", "Helmet", GearSlot.HELMET, 3, "Headshot mult 2.5 -> 1.2", 12_000, 14,
            headMultiplier = 1.2f)
    )

    /** Section 3: vests are an armour pool with an absorb share. */
    val VESTS: List<GearItem> = listOf(
        GearItem("vest_1", "Vest", GearSlot.VEST, 1, "50 armor points, absorb 40%", 600, 3,
            armorPoints = 50f, armorAbsorb = 0.40f),
        GearItem("vest_2", "Vest", GearSlot.VEST, 2, "100 armor points, absorb 50%", 3_500, 9,
            armorPoints = 100f, armorAbsorb = 0.50f),
        GearItem("vest_3", "Vest", GearSlot.VEST, 3, "150 armor points, absorb 60%", 14_000, 14,
            armorPoints = 150f, armorAbsorb = 0.60f)
    )

    /** Section 3: backpacks raise the reserve ammunition cap. */
    val BACKPACKS: List<GearItem> = listOf(
        GearItem("backpack_1", "Backpack", GearSlot.BACKPACK, 1, "Reserve ammo cap +15%", 800, 3,
            reserveBonusPercent = 15),
        GearItem("backpack_2", "Backpack", GearSlot.BACKPACK, 2, "Reserve ammo cap +30%", 4_000, 9,
            reserveBonusPercent = 30),
        GearItem("backpack_3", "Backpack", GearSlot.BACKPACK, 3, "Reserve ammo cap +50%", 12_000, 14,
            reserveBonusPercent = 50)
    )

    /** Section 3: single-tier equipment. */
    val EQUIPMENT: List<GearItem> = listOf(
        GearItem("medkit", "Medkit", GearSlot.MEDICAL, null, "Heals 100%, 3.0 s use time (interruptible)",
            150, 1, healFraction = 1.0f, useSeconds = 3.0f),
        GearItem("bandage", "Bandage", GearSlot.MEDICAL, null, "Heals 30%, 1.5 s",
            50, 1, healFraction = 0.30f, useSeconds = 1.5f),
        GearItem("gas_mask", "Gas mask", GearSlot.MASK, null, "Gas dmg -80%",
            400, 4, gasReduction = 0.8f),
        GearItem("night_vision", "Night vision", GearSlot.OPTICS, null, "Brightens dark, outlines",
            5_000, 13),
        GearItem("binoculars", "Binoculars", GearSlot.OPTICS, null, "Marks enemies 20 s",
            600, 5)
    )

    val ALL: List<GearItem> = HELMETS + VESTS + EQUIPMENT + BACKPACKS

    const val BINOCULAR_MARK_SECONDS = 20f

    fun byId(id: String): GearItem? = ALL.firstOrNull { it.id == id }

    fun ofSlot(slot: GearSlot): List<GearItem> = ALL.filter { it.slot == slot }

    fun unlockedAt(rank: Int): List<GearItem> = ALL.filter { it.unlockRank <= rank }

    /** Highest helmet tier the operator can wear at a rank, or null for bare-headed. */
    fun bestHelmetAt(rank: Int): GearItem? = HELMETS.filter { it.unlockRank <= rank }.maxByOrNull { it.tier ?: 0 }

    fun bestVestAt(rank: Int): GearItem? = VESTS.filter { it.unlockRank <= rank }.maxByOrNull { it.tier ?: 0 }

    fun bestBackpackAt(rank: Int): GearItem? =
        BACKPACKS.filter { it.unlockRank <= rank }.maxByOrNull { it.tier ?: 0 }

    fun headMultiplierAt(rank: Int): Float =
        ArmorMath.headMultiplier(bestHelmetAt(rank)?.tier)

    /** Reserve rounds after the backpack bonus, rounded down to whole rounds. */
    fun reserveWithBackpack(baseReserve: Int, backpack: GearItem?): Int {
        val bonus = backpack?.reserveBonusPercent ?: 0
        return baseReserve + baseReserve * bonus / 100
    }
}
