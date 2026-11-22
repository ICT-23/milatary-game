package com.example.game.valor.balance

import com.example.game.vanguard.CaseRecorder
import com.example.game.vanguard.ContractCase
import com.example.game.vanguard.approxEquals
import com.example.game.vanguard.observed

/** Section 5 rank curve and XP sources, plus the section 5.1 skill tree. */
object BalanceProgressionCases {

    fun run(): List<ContractCase> {
        val ranks = CaseRecorder(BalanceAudit.GROUP_RANKS)
        rankCases(ranks)
        val skills = CaseRecorder(BalanceAudit.GROUP_SKILLS)
        skillCases(skills)
        return ranks.result() + skills.result()
    }

    private fun rankCases(r: CaseRecorder) {
        r.check(
            "Rank count", "20 rungs in the v2 curve",
            RankTable.RANKS.size == 20, observed(RankTable.RANKS.size, 20)
        )
        r.check(
            "Rank names", "Recruit at 1, General (Commander) at 19, Medal of Honor at 20",
            RankTable.byIndex(1)?.name == "Recruit" &&
                RankTable.byIndex(10)?.name == "Second Lieutenant" &&
                RankTable.byIndex(19)?.name == "General (Commander)" &&
                RankTable.byIndex(20)?.name == "Medal of Honor Holder",
            observed(RankTable.byIndex(19)?.name, "General (Commander)")
        )
        r.check(
            "Cumulative XP", "0 / 300 / 900 ... 140,000",
            RankTable.byIndex(1)?.cumulativeXp == 0 &&
                RankTable.byIndex(2)?.cumulativeXp == 300 &&
                RankTable.byIndex(3)?.cumulativeXp == 900 &&
                RankTable.byIndex(19)?.cumulativeXp == 140_000,
            observed(RankTable.byIndex(19)?.cumulativeXp, 140_000)
        )
        r.check(
            "Story-gated top rank", "rank 20 is earned by the ending, not by XP",
            RankTable.byIndex(20)?.cumulativeXp == null &&
                RankTable.byIndex(20)?.storyGated == true &&
                RankTable.rankAt(999_999).index == 19,
            observed(RankTable.rankAt(999_999).index, 19)
        )
        r.check(
            "Curve is monotonic", "no rung costs less XP than the one before it",
            RankTable.RANKS.mapNotNull { it.cumulativeXp }.zipWithNext().all { (a, b) -> b > a },
            observed(RankTable.RANKS.mapNotNull { it.cumulativeXp }.size, 19)
        )
        r.check(
            "Skill point total", "the table awards 23 points",
            RankTable.RANKS.sumOf { it.skillPoints } == 23,
            observed(RankTable.RANKS.sumOf { it.skillPoints }, 23)
        )
        val spans = (1..6).map { RankTable.actXpSpan(it) }
        r.check(
            "Act XP spans", "900 / 5,600 / 20,000 / 26,500 / 24,000 / 63,000",
            spans == listOf(900, 5_600, 20_000, 26_500, 24_000, 63_000),
            observed(spans, "[900, 5600, 20000, 26500, 24000, 63000]")
        )
        r.check(
            "Rank-to-act bands", "1-3 / 4-6 / 7-10 / 11-13 / 14-15 / 16-19",
            (1..6).map { act -> RankTable.forAct(act).map { it.index } } == listOf(
                listOf(1, 2, 3), listOf(4, 5, 6), listOf(7, 8, 9, 10),
                listOf(11, 12, 13), listOf(14, 15), listOf(16, 17, 18, 19)
            ),
            observed(RankTable.forAct(6).map { it.index }, "[16, 17, 18, 19]")
        )
        r.check(
            "Rank lookup", "900 XP holds rank 3 and the next rung is 2,000",
            RankTable.rankAt(900).index == 3 && RankTable.nextRank(900)?.cumulativeXp == 2_000,
            observed(RankTable.rankAt(900).index, 3)
        )
        r.check(
            "Progress bar", "150 of the 0-300 span is half way",
            approxEquals(RankTable.progressToNext(150), 0.5f),
            observed(RankTable.progressToNext(150), 0.5f)
        )
        r.check(
            "Star multipliers", "1 star x1.0, 2 x1.2, 3 x1.5",
            RankTable.STAR_MULTIPLIERS == listOf(1.0f, 1.2f, 1.5f) &&
                approxEquals(RankTable.starMultiplier(3), 1.5f),
            observed(RankTable.STAR_MULTIPLIERS, "[1.0, 1.2, 1.5]")
        )
        r.check(
            "Mission XP", "100 base pays 100 / 120 / 150 by star count",
            RankTable.missionXp(100, 1, false) == 100 &&
                RankTable.missionXp(100, 2, false) == 120 &&
                RankTable.missionXp(100, 3, false) == 150,
            observed(RankTable.missionXp(100, 3, false), 150)
        )
        r.check(
            "First clear bonus", "3 stars plus 25% on the first clear",
            RankTable.missionXp(100, 3, true) == 187,
            observed(RankTable.missionXp(100, 3, true), 187)
        )
        r.check(
            "Other XP sources", "headshot +5, objective 50-500",
            RankTable.HEADSHOT_BONUS_XP == 5 && RankTable.OBJECTIVE_XP_RANGE == 50..500,
            observed("${RankTable.HEADSHOT_BONUS_XP}/${RankTable.OBJECTIVE_XP_RANGE}", "5/50..500")
        )
    }

    private fun skillCases(r: CaseRecorder) {
        r.check(
            "Skill count", "4 branches x 4 skills",
            SkillTreeData.SKILLS.size == 16 &&
                SkillBranch.entries.all { SkillTreeData.ofBranch(it).size == 4 },
            observed(SkillTreeData.SKILLS.size, 16)
        )
        r.check(
            "Level caps", "12 skills at 5 levels, 4 single-level skills",
            SkillTreeData.SKILLS.count { it.maxLevel == 5 } == 12 &&
                SkillTreeData.SKILLS.count { it.maxLevel == 1 } == 4,
            observed(SkillTreeData.SKILLS.count { it.maxLevel == 1 }, 4)
        )
        r.check(
            "One capstone per branch", "each branch has exactly one 1-level skill",
            SkillBranch.entries.all { branch ->
                SkillTreeData.ofBranch(branch).count { it.maxLevel == 1 } == 1
            },
            observed(SkillTreeData.SKILLS.filter { it.maxLevel == 1 }.map { it.name }.size, 4)
        )
        r.check(
            "Points awarded", "the rank curve yields 23 points",
            SkillTreeData.TOTAL_POINTS_AWARDED == 23,
            observed(SkillTreeData.TOTAL_POINTS_AWARDED, 23)
        )
        r.check(
            "Tree cannot be maxed", "64 levels against 23 points, so choices are permanent",
            SkillTreeData.MAX_TOTAL_LEVELS == 64 &&
                SkillTreeData.TOTAL_POINTS_AWARDED < SkillTreeData.MAX_TOTAL_LEVELS,
            observed(
                "${SkillTreeData.TOTAL_POINTS_AWARDED}/${SkillTreeData.MAX_TOTAL_LEVELS}",
                "23 < 64"
            )
        )
        r.check(
            "Recoil skill", "Steady Hands -4% per level, -20% at max",
            approxEquals(SkillTreeData.recoilReductionPercent(mapOf("steady_hands" to 5)), -20f) &&
                approxEquals(SkillTreeData.percentToFactor(-20f), 0.8f),
            observed(SkillTreeData.recoilReductionPercent(mapOf("steady_hands" to 5)), -20f)
        )
        r.check(
            "Health skill", "Tough +5 HP per level, +25 at max",
            approxEquals(SkillTreeData.maxHealthBonus(mapOf("tough" to 5)), 25f),
            observed(SkillTreeData.maxHealthBonus(mapOf("tough" to 5)), 25f)
        )
        r.check(
            "Single-level clamp", "Hold Breath stays at +2 s whatever level is passed",
            approxEquals(SkillTreeData.byId("hold_breath")?.valueAt(9) ?: 0f, 2f),
            observed(SkillTreeData.byId("hold_breath")?.valueAt(9), 2f)
        )
        r.check(
            "Spend guard", "points are spent, never overdrawn",
            SkillTreeData.canSpend(mapOf("tough" to 5), 5) &&
                !SkillTreeData.canSpend(mapOf("tough" to 5), 4),
            observed(SkillTreeData.spentPoints(mapOf("tough" to 5)), 5)
        )
        r.check(
            "Cap guard", "a level above the skill's cap is rejected",
            !SkillTreeData.canSpend(mapOf("tough" to 9), 99),
            observed(SkillTreeData.byId("tough")?.maxLevel, 5)
        )
    }
}
