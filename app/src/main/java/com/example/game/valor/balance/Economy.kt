package com.example.game.valor.balance

/**
 * Section 6.1: the payout envelope for a single mission clear in an act.
 *
 * Cash, Steel and Parts are the table's own ranges. Nothing outside a band is
 * ever awarded for one clear, so the act a mission belongs to fully determines
 * how far the player can progress from it.
 */
data class IncomeBand(
    val act: Int,
    val cash: IntRange,
    val steel: IntRange,
    val parts: IntRange
)

/** Section 6.1 closing lines plus the difficulty scaling named in the header. */
object MissionIncome {

    const val BONUS_OBJECTIVE_CASH_BONUS = 0.20f
    const val THREE_STAR_CASH_BONUS = 0.25f
    const val THREE_STARS = 3

    val BANDS: List<IncomeBand> = listOf(
        IncomeBand(1, 80..250, 2..5, 0..1),
        IncomeBand(2, 300..900, 5..12, 1..3),
        IncomeBand(3, 800..2_200, 10..20, 3..6),
        IncomeBand(4, 2_000..5_000, 15..30, 5..10),
        IncomeBand(5, 4_500..9_000, 25..45, 8..15),
        IncomeBand(6, 8_000..16_000, 35..60, 12..25)
    )

    fun bandFor(act: Int): IncomeBand = BANDS[(act - 1).coerceIn(0, BANDS.lastIndex)]

    /**
     * "Bonus objective: +20% cash. 3★: +25% cash." Both bonuses are earned on
     * top of the base figure and the difficulty multiplier is applied last, so
     * a Recruit run never out-earns a Soldier run with the same result.
     */
    fun cash(baseCash: Int, stars: Int, bonusObjective: Boolean, difficulty: Difficulty): Int {
        var total = baseCash.toFloat()
        if (bonusObjective) total *= 1f + BONUS_OBJECTIVE_CASH_BONUS
        if (stars >= THREE_STARS) total *= 1f + THREE_STAR_CASH_BONUS
        return (total * difficulty.rewardMult).toInt()
    }

    /** Steel and Parts follow the same reward multiplier as cash. */
    fun resource(baseAmount: Int, difficulty: Difficulty): Int = (baseAmount * difficulty.rewardMult).toInt()

    /** Section 7 base cash must sit inside its act's band; bonuses are added on top. */
    fun isBaseInBand(act: Int, baseCash: Int): Boolean = baseCash in bandFor(act).cash
}

/** Section 6.2: every source of Medals. A fixed award is a range of one value. */
enum class MedalSource(val displayName: String, val min: Int, val max: Int) {
    ACHIEVEMENT("Achievement", 5, 25),
    ACT_COMPLETION("Act completion", 50, 50),
    WEEKLY_CHALLENGE("Weekly challenge", 10, 30),
    REWARDED_AD("Rewarded ad", 2, 2);

    val isRange: Boolean get() = max > min
}

/** Section 6.2: what Medals buy, and the fairness rule that bounds the store. */
object MedalEconomy {

    const val REWARDED_ADS_PER_DAY = 3
    const val REVIVE_TOKEN_PRICE = 5
    const val CRATE_PRICE = 30
    val COSMETIC_SKINS: IntRange = 50..300
    val INSTANT_CRAFT: IntRange = 10..40

    fun rewardedAdMedals(adsWatched: Int): Int =
        adsWatched.coerceIn(0, REWARDED_ADS_PER_DAY) * MedalSource.REWARDED_AD.min

    /** Sources that pay a range are variable; act completion is a flat award. */
    fun variableSources(): List<MedalSource> = MedalSource.entries.filter { it.isRange }

    /**
     * "Never sold at a price that makes gameplay items exclusive." Gameplay is
     * bought with Cash, Steel or Parts only; Medals buy time and appearance.
     */
    fun isGameplayCurrency(currency: String): Boolean =
        currency.trim().lowercase() in GAMEPLAY_CURRENCIES

    val GAMEPLAY_CURRENCIES: Set<String> = setOf("cash", "steel", "parts")
}

/** Section 6.3: the list of sinks, in the order the table states them. */
enum class Sink(val displayName: String) {
    WEAPON_UPGRADES("Weapon upgrades"),
    ATTACHMENT_CRAFTING("Attachment crafting"),
    ARMOR_TIERS("Armor tiers"),
    CONSUMABLES("Consumables (medkits, grenades, mines)"),
    COSMETIC_SKINS("Cosmetic skins"),
    SQUAD_GEAR("Squad gear"),
    DRONE_UPGRADES("Drone upgrades"),
    REPAIR_FEES("Repair fees (Elite failure, -5% cash)")
}

/** Section 6.4: the three rarity bands and their visible odds. */
enum class LootRarity(val displayName: String, val chancePercent: Int) {
    COMMON("Common", 70),
    RARE("Rare", 25),
    EPIC("Epic", 5)
}

/**
 * Section 6.4: one crate row. [cash], [steel] and [parts] are null where the
 * table does not quote a number, so nothing is invented.
 */
data class CrateSpec(
    val id: String,
    val name: String,
    val cash: IntRange?,
    val steel: IntRange?,
    val parts: IntRange?,
    val bonusItems: List<String>
)

object CrateCatalog {

    const val SUPPLY_ID = "crate_supply"
    const val ARMORY_ID = "crate_armory"
    const val COMMAND_ID = "crate_command"

    /** "Pity timer: Epic guaranteed within 20 crates." */
    const val PITY_TIMER = 20

    val CRATES: List<CrateSpec> = listOf(
        CrateSpec(SUPPLY_ID, "Supply", 150..400, null, null, listOf("1-3 grenades/medkits")),
        CrateSpec(ARMORY_ID, "Armory", null, 5..20, 2..8, listOf("1 attachment blueprint piece")),
        CrateSpec(COMMAND_ID, "Command", null, null, null, listOf("Skin", "large Cash", "mastery boost"))
    )

    fun byId(id: String): CrateSpec? = CRATES.firstOrNull { it.id == id }

    /** "All odds visible in-game" - the three bands must always total 100%. */
    val oddsTotalPercent: Int get() = LootRarity.entries.sumOf { it.chancePercent }

    fun epicGuaranteed(cratesSinceEpic: Int): Boolean = cratesSinceEpic >= PITY_TIMER

    /** The rarity a crate opens at, given the pity counter before the pull. */
    fun rarityForRoll(rollPercent: Int, cratesSinceEpic: Int): LootRarity {
        if (epicGuaranteed(cratesSinceEpic)) return LootRarity.EPIC
        var upperBound = 0
        for (rarity in LootRarity.entries) {
            upperBound += rarity.chancePercent
            if (rollPercent < upperBound) return rarity
        }
        return LootRarity.COMMON
    }

    /** "150-400 Cash": a normalised roll picks an amount inside the band. */
    fun roll(range: IntRange?, roll: Float): Int? = range?.let {
        val span = it.last - it.first
        it.first + (span * roll.coerceIn(0f, 1f)).toInt()
    }
}
