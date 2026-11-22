package com.example.game.valor.gdd

import com.example.game.vanguard.CaseRecorder
import com.example.game.vanguard.ContractCase
import com.example.game.vanguard.observed

/** Section 9: currencies, upgrade categories, the skill tree and fair play. */
object GddEconomyCases {

    fun run(): List<ContractCase> {
        val r = CaseRecorder(ValorGddAudit.GROUP_ECONOMY)

        r.check(
            "Currencies", "Cash, Steel, Parts and Medals",
            Currencies.ALL.size == 4 &&
                Currencies.ALL.map { it.name } == listOf("Cash", "Steel", "Parts", "Medals"),
            observed(Currencies.ALL.map { it.name }, "[4 rows]")
        )
        r.check(
            "Crafting currencies", "attachments cost Steel and Parts",
            Currencies.craftingCurrencies() == listOf("Steel", "Parts"),
            observed(Currencies.craftingCurrencies(), "[Steel, Parts]")
        )
        r.check(
            "Premium-lite", "Medals are the only sellable currency",
            Currencies.PREMIUM_LITE == setOf("Medals"),
            observed(Currencies.PREMIUM_LITE, "{Medals}")
        )
        r.check(
            "Medal sources", "earned from achievements, ads and a one-time purchase",
            Currencies.byName("Medals")?.source?.contains("Achievements") == true,
            observed(Currencies.byName("Medals")?.source, "contains Achievements")
        )
        r.check(
            "Upgrade categories", "6 things to invest in",
            UpgradeCategory.entries.size == 6 &&
                UpgradeCategory.entries.map { it.displayName } ==
                listOf("Weapons", "Armor", "Squad", "Drones", "Fitness", "Skills"),
            observed(UpgradeCategory.entries.size, 6)
        )
        r.check(
            "Skill points", "one point per rank, none at Recruit",
            SkillTreeSpec.POINTS_PER_RANK == 1 &&
                SkillTreeSpec.pointsEarnedAtRank(1) == 0 &&
                SkillTreeSpec.pointsEarnedAtRank(20) == 19,
            observed(SkillTreeSpec.pointsEarnedAtRank(20), 19)
        )
        r.check(
            "Promotion points", "four ranks pay four points",
            SkillTreeSpec.pointsForPromotions(1, 5) == 4 &&
                SkillTreeSpec.pointsForPromotions(5, 5) == 0,
            observed(SkillTreeSpec.pointsForPromotions(1, 5), 4)
        )
        r.check(
            "Skill branches", "4 branches holding 11 effects",
            SkillTreeSpec.BRANCHES.size == 4 && SkillTreeSpec.effectCount() == 11,
            observed(SkillTreeSpec.effectCount(), 11)
        )
        r.check(
            "Marksman branch", "recoil, ADS and headshots",
            SkillTreeSpec.byName("Marksman")?.effects ==
                listOf("Less recoil", "Faster ADS", "Headshot bonus"),
            observed(SkillTreeSpec.byName("Marksman")?.effects?.size, 3)
        )
        r.check(
            "Ad rewards", "double reward, revive, crate",
            Monetization.AD_REWARDS ==
                listOf("2x mission reward", "Free revive", "Free crate") &&
                Monetization.adRewardCount() == 3,
            observed(Monetization.adRewardCount(), 3)
        )
        r.check(
            "Monetization rules", "five rules, including no ads in gameplay",
            Monetization.RULES.size == 5 &&
                !Monetization.adsDuringGameplay() &&
                !Monetization.payToWin(),
            observed(Monetization.RULES.size, 5)
        )
        r.check(
            "Fair play rule", "everything is also earnable by playing",
            Monetization.ruleFor("Fair play")?.detail?.contains("earnable") == true,
            observed(Monetization.ruleFor("Fair play")?.detail, "contains earnable")
        )

        return r.result()
    }
}

/** Section 10: the 26 environments, weather and lite destruction. */
object GddWorldCases {

    fun run(): List<ContractCase> {
        val r = CaseRecorder(ValorGddAudit.GROUP_WORLD)

        r.check(
            "Act environments", "6 acts holding 5/4/4/5/4/4 environments",
            WorldMap.ACTS.size == 6 &&
                WorldMap.ACTS.map { it.environments.size } == listOf(5, 4, 4, 5, 4, 4),
            observed(WorldMap.ACTS.map { it.environments.size }, "[5, 4, 4, 5, 4, 4]")
        )
        r.check(
            "Environment total", "26 environments across the campaign",
            WorldMap.totalEnvironments() == 26 && WorldMap.allActsPopulated(),
            observed(WorldMap.totalEnvironments(), 26)
        )
        r.check(
            "Act titles", "the art bible names the acts",
            WorldMap.forAct(1)?.title == "Boot Camp" &&
                WorldMap.forAct(2)?.title == "Great War",
            observed(WorldMap.forAct(2)?.title, "Great War")
        )
        r.check(
            "Academy grounds", "Act 1 holds the barracks and the range",
            WorldMap.hasEnvironment(1, "Barracks") &&
                WorldMap.hasEnvironment(1, "Range") &&
                WorldMap.hasEnvironment(1, "Obstacle course"),
            observed(WorldMap.environmentsFor(1), "[5 act-1 environments]")
        )
        r.check(
            "Bunker", "Act 4 ends at the fortress bunker",
            WorldMap.hasEnvironment(4, "Fortress bunker"),
            observed(WorldMap.environmentsFor(4), "contains Fortress bunker")
        )
        r.check(
            "Drone factory", "Act 6 reaches the drone factory",
            WorldMap.hasEnvironment(6, "Drone factory"),
            observed(WorldMap.environmentsFor(6), "contains Drone factory")
        )
        r.check(
            "Weather", "5 dynamic weather states",
            WorldSystems.WEATHER.size == 5 &&
                WorldSystems.hasWeather("Sandstorm") &&
                WorldSystems.hasWeather("Day/night cycle"),
            observed(WorldSystems.WEATHER.size, 5)
        )
        r.check(
            "Destruction", "lite destruction over four target classes",
            WorldSystems.DESTRUCTION.size == 4 && WorldSystems.destructionIsLite(),
            observed(WorldSystems.DESTRUCTION.size, 4)
        )

        return r.result()
    }
}
