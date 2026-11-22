package com.example.game.valor.gdd

import com.example.game.vanguard.CaseRecorder
import com.example.game.vanguard.ContractCase
import com.example.game.vanguard.approxEquals
import com.example.game.vanguard.observed

/** Section 4: the twenty-rank ladder and prestige. */
object GddRankCases {

    fun run(): List<ContractCase> {
        val r = CaseRecorder(ValorGddAudit.GROUP_RANKS)

        val expectedXp = listOf(
            0, 500, 1_500, 3_500, 7_000, 12_000, 20_000, 32_000, 48_000,
            70_000, 100_000, 140_000, 190_000, 250_000, 330_000, 430_000,
            550_000, 700_000, 900_000
        )

        r.check(
            "Ladder shape", "20 contiguous rungs",
            RankCurve.RANKS.size == 20 && RankCurve.isContiguous(),
            observed(RankCurve.RANKS.size, 20)
        )
        r.check(
            "XP curve", "0 to 900,000 in the printed steps",
            RankCurve.RANKS.mapNotNull { it.cumulativeXp } == expectedXp,
            observed(
                RankCurve.RANKS.mapNotNull { it.cumulativeXp }.takeLast(3),
                expectedXp.takeLast(3)
            )
        )
        r.check(
            "Curve climbs", "every rung costs more than the one below",
            RankCurve.isStrictlyIncreasing(), observed(RankCurve.isStrictlyIncreasing(), true)
        )
        r.check(
            "First rung", "Recruit at 0 XP",
            RankCurve.byIndex(1)?.title == "Recruit" &&
                RankCurve.byIndex(1)?.cumulativeXp == 0,
            observed(RankCurve.byIndex(1)?.title, "Recruit")
        )
        r.check(
            "Commander", "General (Commander) at 900,000",
            RankCurve.byIndex(19)?.title == "General (Commander)" &&
                RankCurve.byIndex(19)?.cumulativeXp == 900_000,
            observed(RankCurve.byIndex(19)?.cumulativeXp, 900_000)
        )
        r.check(
            "Honor rung", "rank 20 is story-gated, not XP-gated",
            RankCurve.byIndex(20)?.storyGated == true &&
                RankCurve.byIndex(20)?.cumulativeXp == null,
            observed(RankCurve.byIndex(20)?.cumulativeXp, "null")
        )
        r.check(
            "Rank at XP", "500 XP holds Private, 499 still Recruit",
            RankCurve.rankAt(500).title == "Private" && RankCurve.rankAt(499).title == "Recruit",
            observed("${RankCurve.rankAt(500).title}/${RankCurve.rankAt(499).title}", "Private/Recruit")
        )
        r.check(
            "Progress", "250 of the first 500 is half",
            approxEquals(RankCurve.progressToNext(250), 0.5f),
            observed(RankCurve.progressToNext(250), 0.5f)
        )
        r.check(
            "XP to next", "300 XP needs 200 more",
            RankCurve.xpToNext(300) == 200, observed(RankCurve.xpToNext(300), 200)
        )
        r.check(
            "Drone unlock", "drone strike arrives at Colonel (15)",
            RankCurve.unlockRankOf("Drone strike unlocked (ARIA)") == 15 &&
                !RankCurve.isUnlocked(14, "Drone strike unlocked (ARIA)") &&
                RankCurve.isUnlocked(15, "Drone strike unlocked (ARIA)"),
            observed(RankCurve.unlockRankOf("Drone strike unlocked (ARIA)"), 15)
        )
        r.check(
            "Swarm unlock", "multi-drone swarm at Brigadier General (16)",
            RankCurve.unlockRankOf("Multi-drone swarm") == 16,
            observed(RankCurve.unlockRankOf("Multi-drone swarm"), 16)
        )
        r.check(
            "Unlock cells", "30 unlock entries across the ladder",
            RankCurve.RANKS.sumOf { it.unlocks.size } == 30,
            observed(RankCurve.RANKS.sumOf { it.unlocks.size }, 30)
        )
        r.check(
            "Promotions", "0 to 3,500 XP is three promotions",
            RankCurve.promotionsBetween(0, 3_500).map { it.index } == listOf(2, 3, 4),
            observed(RankCurve.promotionsBetween(0, 3_500).map { it.index }, "[2, 3, 4]")
        )
        r.check(
            "Unlocks through", "a Captain carries everything below rank 12",
            RankCurve.unlocksThrough(12).containsAll(
                listOf("Basic rifle", "First gun attachment slot (optic)")
            ) && !RankCurve.unlocksThrough(12).contains("Multi-drone swarm"),
            observed(RankCurve.unlocksThrough(12).size, ">2")
        )
        r.check(
            "Prestige", "opens on the story rung and keeps cosmetics",
            Prestige.availableAt(20) && !Prestige.availableAt(19) &&
                Prestige.RULES.size == 3 &&
                Prestige.keptAfterReenlist(setOf("gold")) == setOf("gold"),
            observed(Prestige.availableAt(19), false)
        )
        r.check(
            "Prestige stars", "a first re-enlist carries one star",
            Prestige.starCount(1) == 1 && Prestige.starCount(-1) == 0,
            observed(Prestige.starCount(1), 1)
        )

        return r.result()
    }
}

/** Section 4.1: the eight awards and their thresholds. */
object GddMedalCases {

    fun run(): List<ContractCase> {
        val r = CaseRecorder(ValorGddAudit.GROUP_MEDALS)

        r.check(
            "Award count", "8 medals",
            GddMedal.entries.size == 8 && MedalGallery.count() == 8,
            observed(GddMedal.entries.size, 8)
        )
        r.check(
            "Headshot thresholds", "50 / 200 / 1,000",
            listOf(
                MedalRules.MARKSMAN_HEADSHOTS,
                MedalRules.SHARPSHOOTER_HEADSHOTS,
                MedalRules.EXPERT_HEADSHOTS
            ) == listOf(50, 200, 1_000),
            observed(MedalRules.EXPERT_HEADSHOTS, 1_000)
        )
        r.check(
            "Fresh profile", "no awards before a mission",
            MedalRules.earned(MedalProgress()).isEmpty(),
            observed(MedalRules.earned(MedalProgress()).size, 0)
        )
        r.check(
            "Marksman", "50 headshots earns the first badge",
            MedalRules.earned(MedalProgress(headshots = 50)) == listOf(GddMedal.MARKSMAN),
            observed(MedalRules.earned(MedalProgress(headshots = 50)).size, 1)
        )
        r.check(
            "Headshot ladder", "1,000 headshots earns all three badges",
            MedalRules.earned(MedalProgress(headshots = 1_000)) == listOf(
                GddMedal.MARKSMAN, GddMedal.SHARPSHOOTER, GddMedal.EXPERT
            ),
            observed(MedalRules.earned(MedalProgress(headshots = 1_000)).size, 3)
        )
        r.check(
            "Purple Heart", "9% health awards it, 10% does not",
            MedalRules.earned(MedalProgress(lowestHealthPercent = 9)) ==
                listOf(GddMedal.PURPLE_HEART) &&
                MedalRules.earned(MedalProgress(lowestHealthPercent = 10)).isEmpty(),
            observed(MedalRules.earned(MedalProgress(lowestHealthPercent = 9)).size, 1)
        )
        r.check(
            "Bronze Star", "all bonus objectives in an act",
            GddMedal.BRONZE_STAR in
                MedalRules.earned(MedalProgress(actsWithAllBonusObjectives = setOf(1))),
            observed(
                MedalRules.earned(MedalProgress(actsWithAllBonusObjectives = setOf(1))),
                "[Bronze]"
            )
        )
        r.check(
            "Silver Star", "an act cleared on Hard",
            GddMedal.SILVER_STAR in
                MedalRules.earned(MedalProgress(actsClearedOnHard = setOf(2))),
            observed(MedalRules.earned(MedalProgress(actsClearedOnHard = setOf(2))), "[Silver]")
        )
        r.check(
            "Distinguished Service Cross", "a boss beaten without damage",
            GddMedal.DISTINGUISHED_SERVICE_CROSS in
                MedalRules.earned(MedalProgress(bossesBeatenFlawless = 1)),
            observed(MedalRules.earned(MedalProgress(bossesBeatenFlawless = 1)), "[DSC]")
        )
        r.check(
            "Medal of Honor", "awards only on the story finale",
            MedalRules.earned(MedalProgress(storyComplete = true)) ==
                listOf(GddMedal.MEDAL_OF_HONOR),
            observed(MedalRules.earned(MedalProgress(storyComplete = true)).size, 1)
        )
        r.check(
            "Next badge", "50 headshots points at Sharpshooter",
            MedalRules.nextHeadshotMedal(50) == GddMedal.SHARPSHOOTER,
            observed(MedalRules.nextHeadshotMedal(50), GddMedal.SHARPSHOOTER)
        )
        r.check(
            "Remaining headshots", "Expert needs 1,000 from zero",
            MedalRules.headshotsRemaining(0, GddMedal.EXPERT) == 1_000,
            observed(MedalRules.headshotsRemaining(0, GddMedal.EXPERT), 1_000)
        )
        r.check(
            "Missing list", "a fresh profile is missing all eight",
            MedalRules.missing(MedalProgress()).size == 8,
            observed(MedalRules.missing(MedalProgress()).size, 8)
        )
        r.check(
            "Story award", "the Medal of Honor is the only story award",
            MedalGallery.isStoryAward(GddMedal.MEDAL_OF_HONOR) &&
                !MedalGallery.isStoryAward(GddMedal.SILVER_STAR),
            observed(MedalGallery.isStoryAward(GddMedal.SILVER_STAR), false)
        )

        return r.result()
    }
}
