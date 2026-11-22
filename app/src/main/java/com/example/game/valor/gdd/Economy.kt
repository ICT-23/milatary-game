package com.example.game.valor.gdd

/** Section 9: one currency row. */
data class Currency(val name: String, val source: String, val use: String)

/**
 * Section 9 currency block. Medals are "premium-lite" but never a shortcut:
 * the fairness rule below keeps them earnable in play.
 */
object Currencies {

    const val CASH = "Cash"
    const val STEEL = "Steel"
    const val PARTS = "Parts"
    const val MEDALS = "Medals"

    val ALL: List<Currency> = listOf(
        Currency(CASH, "Earned in missions", "Buys guns and repairs"),
        Currency(STEEL, "Found in missions", "Crafts attachments"),
        Currency(PARTS, "Found in missions", "Crafts attachments"),
        Currency(MEDALS, "Achievements, rewarded ads, one-time purchase", "Rare premium-lite currency")
    )

    /** Section 9: the only currency the store can sell. */
    val PREMIUM_LITE: Set<String> = setOf(MEDALS)

    fun byName(name: String): Currency? =
        ALL.firstOrNull { it.name.equals(name.trim(), ignoreCase = true) }

    fun craftingCurrencies(): List<String> = listOf(STEEL, PARTS)
}

/** Section 9 "Upgrade categories": the six things the player can invest in. */
enum class UpgradeCategory(val displayName: String, val detail: String) {
    WEAPONS("Weapons", "Damage, handling, levels and attachments"),
    ARMOR("Armor", "Helmets, vests and gear tiers"),
    SQUAD("Squad", "AI teammates and their commands"),
    DRONES("Drones", "Recon, strike, swarm, defense and pilot mode"),
    FITNESS("Fitness", "Health and stamina"),
    SKILLS("Skills", "Four branches fed by rank points")
}

/** Section 9 skill-tree block: one point per rank, four branches. */
data class SkillBranchSpec(val name: String, val effects: List<String>)

object SkillTreeSpec {

    const val POINTS_PER_RANK = 1

    val BRANCHES: List<SkillBranchSpec> = listOf(
        SkillBranchSpec("Marksman", listOf("Less recoil", "Faster ADS", "Headshot bonus")),
        SkillBranchSpec(
            "Tactician", listOf("Squad commands", "Bigger radio range", "Cooldown reduction")
        ),
        SkillBranchSpec("Survivor", listOf("More health", "Faster healing", "Armor bonus")),
        SkillBranchSpec("Stealth", listOf("Quieter steps", "Faster takedowns"))
    )

    /** One point per promotion, so the first rung has earned nothing yet. */
    fun pointsEarnedAtRank(rank: Int): Int =
        (rank - 1).coerceAtLeast(0) * POINTS_PER_RANK

    fun pointsForPromotions(fromRank: Int, toRank: Int): Int =
        (toRank - fromRank).coerceAtLeast(0) * POINTS_PER_RANK

    fun byName(name: String): SkillBranchSpec? =
        BRANCHES.firstOrNull { it.name.equals(name.trim(), ignoreCase = true) }

    fun effectCount(): Int = BRANCHES.sumOf { it.effects.size }
}

/**
 * Section 9 monetization rules. Every rule is a constraint the store must obey,
 * so they are kept as data the settings and shop screens can assert against.
 */
data class MonetizationRule(val kind: String, val detail: String)

object Monetization {

    val AD_REWARDS: List<String> = listOf("2x mission reward", "Free revive", "Free crate")

    val RULES: List<MonetizationRule> = listOf(
        MonetizationRule("Rewarded ads", "2x mission reward, free revive, free crate"),
        MonetizationRule("One-time purchase", "Remove Ads + Starter Pack"),
        MonetizationRule("Cosmetics", "Optional cosmetic skins"),
        MonetizationRule("Ads", "No ads during gameplay"),
        MonetizationRule("Fair play", "No pay-to-win: everything is also earnable by playing")
    )

    /** Section 9: ads never interrupt a mission. */
    fun adsDuringGameplay(): Boolean = false

    /** Section 9: nothing is purchase-only. */
    fun payToWin(): Boolean = false

    fun adRewardCount(): Int = AD_REWARDS.size

    fun ruleFor(kind: String): MonetizationRule? =
        RULES.firstOrNull { it.kind.equals(kind.trim(), ignoreCase = true) }
}
