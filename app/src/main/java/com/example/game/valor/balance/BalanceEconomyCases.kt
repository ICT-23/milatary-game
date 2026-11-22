package com.example.game.valor.balance

import com.example.game.vanguard.CaseRecorder
import com.example.game.vanguard.ContractCase
import com.example.game.vanguard.approxEquals
import com.example.game.vanguard.observed

/** Section 6 economy tables and the section 9 schema checklists. */
object BalanceEconomyCases {

    fun run(): List<ContractCase> {
        val money = CaseRecorder(BalanceAudit.GROUP_ECONOMY)
        economyCases(money)
        val schemas = CaseRecorder(BalanceAudit.GROUP_SCHEMAS)
        schemaCases(schemas)
        return money.result() + schemas.result()
    }

    private fun economyCases(r: CaseRecorder) {
        r.check(
            "Income bands", "six acts, act 1 80-250 cash, act 6 8,000-16,000",
            MissionIncome.BANDS.size == 6 &&
                MissionIncome.bandFor(1).cash == 80..250 &&
                MissionIncome.bandFor(6).cash == 8_000..16_000,
            observed("${MissionIncome.bandFor(1).cash}/${MissionIncome.bandFor(6).cash}", "80..250/8000..16000")
        )
        r.check(
            "Band resources", "act 3 pays 10-20 steel and 3-6 parts",
            MissionIncome.bandFor(3).steel == 10..20 && MissionIncome.bandFor(3).parts == 3..6,
            observed("${MissionIncome.bandFor(3).steel}/${MissionIncome.bandFor(3).parts}", "10..20/3..6")
        )
        r.check(
            "Cash bonuses", "1,000 base +20% objective +25% three-star = 1,500",
            MissionIncome.cash(1_000, 3, true, Difficulty.SOLDIER) == 1_500,
            observed(MissionIncome.cash(1_000, 3, true, Difficulty.SOLDIER), 1_500)
        )
        r.check(
            "Difficulty scales payout", "the same clear pays 1.6x on Elite",
            MissionIncome.cash(1_000, 3, true, Difficulty.ELITE) == 2_400,
            observed(MissionIncome.cash(1_000, 3, true, Difficulty.ELITE), 2_400)
        )
        r.check(
            "Resources scale too", "10 steel becomes 16 on Elite",
            MissionIncome.resource(10, Difficulty.ELITE) == 16,
            observed(MissionIncome.resource(10, Difficulty.ELITE), 16)
        )
        r.check(
            "Medal sources", "4 sources; act completion pays 50",
            MedalSource.entries.size == 4 && MedalSource.ACT_COMPLETION.min == 50,
            observed(MedalSource.ACT_COMPLETION.min, 50)
        )
        r.check(
            "Rewarded ads", "2 each, at most 3 per day",
            MedalSource.REWARDED_AD.min == 2 &&
                MedalEconomy.rewardedAdMedals(9) == 6 &&
                MedalEconomy.rewardedAdMedals(0) == 0,
            observed(MedalEconomy.rewardedAdMedals(9), 6)
        )
        r.check(
            "Medal prices", "revive 5, crate 30, skins 50-300, instant craft 10-40",
            MedalEconomy.REVIVE_TOKEN_PRICE == 5 && MedalEconomy.CRATE_PRICE == 30 &&
                MedalEconomy.COSMETIC_SKINS == 50..300 && MedalEconomy.INSTANT_CRAFT == 10..40,
            observed(MedalEconomy.COSMETIC_SKINS, "50..300")
        )
        r.check(
            "Gameplay is never medal-gated", "Cash, Steel and Parts are the gameplay currencies",
            MedalEconomy.isGameplayCurrency("Cash") && MedalEconomy.isGameplayCurrency("steel") &&
                MedalEconomy.isGameplayCurrency("parts") && !MedalEconomy.isGameplayCurrency("medals"),
            observed(MedalEconomy.GAMEPLAY_CURRENCIES, "[cash, steel, parts]")
        )
        r.check(
            "Sink list", "8 sinks from section 6.3",
            Sink.entries.size == 8, observed(Sink.entries.size, 8)
        )

        val supply = CrateCatalog.byId(CrateCatalog.SUPPLY_ID)
        val armory = CrateCatalog.byId(CrateCatalog.ARMORY_ID)
        val command = CrateCatalog.byId(CrateCatalog.COMMAND_ID)
        r.check(
            "Crate contents", "Supply 150-400 cash; Armory 5-20 steel, 2-8 parts",
            supply?.cash == 150..400 && armory?.steel == 5..20 && armory?.parts == 2..8 &&
                command?.bonusItems?.size == 3,
            observed("${supply?.cash}/${armory?.steel}/${armory?.parts}", "150..400/5..20/2..8")
        )
        r.check(
            "Visible odds", "70 / 25 / 5 totals 100%",
            CrateCatalog.oddsTotalPercent == 100 &&
                LootRarity.COMMON.chancePercent == 70 &&
                LootRarity.RARE.chancePercent == 25 &&
                LootRarity.EPIC.chancePercent == 5,
            observed(CrateCatalog.oddsTotalPercent, 100)
        )
        r.check(
            "Odds boundaries", "69 stays common, 70 opens rare, 95 opens epic",
            CrateCatalog.rarityForRoll(69, 0) == LootRarity.COMMON &&
                CrateCatalog.rarityForRoll(70, 0) == LootRarity.RARE &&
                CrateCatalog.rarityForRoll(94, 0) == LootRarity.RARE &&
                CrateCatalog.rarityForRoll(95, 0) == LootRarity.EPIC,
            observed(CrateCatalog.rarityForRoll(95, 0), LootRarity.EPIC)
        )
        r.check(
            "Pity timer", "epic guaranteed by the 20th crate",
            !CrateCatalog.epicGuaranteed(19) && CrateCatalog.epicGuaranteed(20) &&
                CrateCatalog.rarityForRoll(0, 20) == LootRarity.EPIC,
            observed(CrateCatalog.PITY_TIMER, 20)
        )
        r.check(
            "Amount roll", "a full roll pays the band ceiling",
            CrateCatalog.roll(150..400, 0f) == 150 && CrateCatalog.roll(150..400, 1f) == 400 &&
                CrateCatalog.roll(null, 0.5f) == null,
            observed("${CrateCatalog.roll(150..400, 0f)}/${CrateCatalog.roll(150..400, 1f)}", "150/400")
        )
    }

    private fun schemaCases(r: CaseRecorder) {
        r.check(
            "Four schemas", "WeaponData, EnemyData, MissionData, RankData",
            FieldChecklists.ALL.size == 4 &&
                FieldChecklists.byName("WeaponData") != null &&
                FieldChecklists.byName("RankData") != null,
            observed(FieldChecklists.ALL.map { it.schemaName }, 4)
        )
        r.check(
            "Checklist sizes", "31 weapon, 13 enemy, 13 mission, 5 rank fields",
            FieldChecklists.WEAPON_DATA.size == 31 &&
                FieldChecklists.ENEMY_DATA.size == 13 &&
                FieldChecklists.MISSION_DATA.size == 13 &&
                FieldChecklists.RANK_DATA.size == 5,
            observed(
                "${FieldChecklists.WEAPON_DATA.size}/${FieldChecklists.ENEMY_DATA.size}/" +
                    "${FieldChecklists.MISSION_DATA.size}/${FieldChecklists.RANK_DATA.size}",
                "31/13/13/5"
            )
        )
        r.check(
            "Rank schema complete", "the rank table already carries all five fields",
            FieldChecklists.RANKS.isComplete && FieldChecklists.RANKS.coveragePercent == 100,
            observed(FieldChecklists.RANKS.missing, "[]")
        )
        r.check(
            "Weapon gaps reported", "recoil curve, noise radius and audio refs are not authored",
            listOf("recoilCurve", "noiseRadius", "audio refs").all {
                it in FieldChecklists.WEAPONS.missing
            } && !FieldChecklists.WEAPONS.isComplete,
            observed(FieldChecklists.WEAPONS.missing.size, FieldChecklists.WEAPON_DATA.size)
        )
        r.check(
            "Mission gaps reported", "scene, star rules, story beat and checkpoints are not authored",
            listOf("scene", "starRules", "storyBeat", "checkpoints").all {
                it in FieldChecklists.MISSIONS.missing
            },
            observed(FieldChecklists.MISSIONS.missing, 5)
        )
        r.check(
            "Enemy gaps reported", "vision, hearing and behaviours are not authored",
            FieldChecklists.ENEMIES.missing ==
                listOf("visionRange/angle", "hearingRadius", "behaviors"),
            observed(FieldChecklists.ENEMIES.missing, 3)
        )
        r.check(
            "No stray fields", "every declared field exists in its checklist",
            FieldChecklists.ALL.all { FieldChecklists.strayFields(it).isEmpty() },
            observed(
                FieldChecklists.ALL.flatMap { FieldChecklists.strayFields(it) },
                "[]"
            )
        )
        r.check(
            "Weapon coverage", "15 of 31 fields carry table data",
            FieldChecklists.WEAPONS.coveragePercent == 48 &&
                approxEquals(FieldChecklists.WEAPONS.coveragePercent.toFloat(), 48f),
            observed(FieldChecklists.WEAPONS.coveragePercent, 48)
        )
    }
}
