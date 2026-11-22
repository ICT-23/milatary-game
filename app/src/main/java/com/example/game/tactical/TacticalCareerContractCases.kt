package com.example.game.tactical

import com.example.game.combat.Vec3
import com.example.game.tactical.academy.AcademyHub
import com.example.game.tactical.academy.CourseStation
import com.example.game.tactical.academy.DrillSergeant
import com.example.game.tactical.academy.FiringRange
import com.example.game.tactical.academy.ObstacleCourse
import com.example.game.tactical.academy.ObstacleRun
import com.example.game.tactical.academy.RangeRun
import com.example.game.tactical.academy.SergeantEvent
import com.example.game.tactical.academy.StationSplit
import com.example.game.tactical.academy.TrainingGrade
import com.example.game.tactical.progression.CampaignAct
import com.example.game.tactical.progression.Medal
import com.example.game.tactical.progression.MedalEvaluator
import com.example.game.tactical.progression.MissionApproach
import com.example.game.tactical.progression.MissionRecord
import com.example.game.tactical.progression.RankLadder
import com.example.game.tactical.progression.RankRequirement
import com.example.game.tactical.state.GameStateManager
import com.example.game.vanguard.CaseRecorder
import com.example.game.vanguard.ContractCase
import com.example.game.vanguard.approxEquals
import com.example.game.vanguard.core.errorCodeOrNull
import com.example.game.vanguard.core.valueOrNull
import com.example.game.vanguard.observed

/** Section 3 ladders, the four medals, Phase 1 persistence and Phase 5 academies. */
internal object TacticalCareerContractCases {

    private val cleanRecord = MissionRecord(
        missionId = "T-1",
        act = CampaignAct.ACT_I,
        survived = true,
        accuracyPercent = 90f,
        headshots = 6,
        lowestHealthPercent = 100,
        traumaKitsApplied = 1,
        approach = MissionApproach.ASSAULT,
        alarmTriggered = false,
        squadmatesSaved = 4,
        squadmatesTotal = 4
    )
    fun run(): List<ContractCase> = rankCases() + medalCases() + stateCases() + academyCases()

    private fun rankCases(): List<ContractCase> {
        val recorder = CaseRecorder(TacticalContractAudit.GROUP_RANK)

        recorder.check(
            "Eight-tier ladder", "8 tiers", RankLadder.TIERS.size == 8,
            observed(RankLadder.TIERS.size, 8)
        )
        val thresholds = RankLadder.TIERS.map { it.xpRequired }
        recorder.check(
            "XP thresholds", "0/500/1500/3500/7000/12000/20000/35000",
            thresholds == listOf(0, 500, 1_500, 3_500, 7_000, 12_000, 20_000, 35_000),
            observed(thresholds, "0/500/1500/3500/7000/12000/20000/35000")
        )
        val qualified = setOf(RankRequirement.ENLISTMENT, RankRequirement.BASIC_RIFLE_QUAL)
        recorder.check(
            "Private requires the rifle qual", "index 1",
            RankLadder.heldIndex(500, qualified) == 1,
            observed(RankLadder.heldIndex(500, qualified), 1)
        )
        recorder.check(
            "XP alone cannot promote", "index 0 without the qual",
            RankLadder.heldIndex(500, setOf(RankRequirement.ENLISTMENT)) == 0,
            observed(RankLadder.heldIndex(500, setOf(RankRequirement.ENLISTMENT)), 0)
        )
        val blocked = RankLadder.eligibility(
            4_900,
            setOf(
                RankRequirement.ENLISTMENT,
                RankRequirement.BASIC_RIFLE_QUAL,
                RankRequirement.FIRST_COMBAT_MISSION
            )
        )
        recorder.check(
            "Sergeant blocked by squad trial",
            "missing SQUAD_LEADER_TRIAL",
            blocked.rankIndex == 2 &&
                blocked.missingRequirement == RankRequirement.SQUAD_LEADER_TRIAL &&
                !blocked.isReady,
            observed(blocked.rankIndex, 2)
        )
        recorder.check(
            "Drone clearance at Lieutenant", "true at 4, false at 3",
            RankLadder.hasClearance(4, RankLadder.DRONE_UNLOCK_INDEX) &&
                !RankLadder.hasClearance(3, RankLadder.DRONE_UNLOCK_INDEX),
            observed(RankLadder.hasClearance(3, RankLadder.DRONE_UNLOCK_INDEX), false)
        )
        recorder.check(
            "Rank bar progress", "0.25 between Corporal and Sergeant",
            approxEquals(RankLadder.progressToNext(2_000, 2), 0.25f),
            observed(RankLadder.progressToNext(2_000, 2), 0.25f)
        )
        recorder.check(
            "Private privileges", "full 4-man squad commands at Sergeant",
            RankLadder.privilegesBetween(1, 3).contains("Full 4-man squad commands"),
            observed(RankLadder.privilegesBetween(1, 3), "Full 4-man squad commands")
        )
        recorder.check(
            "General rung", "GEN + campaign victory",
            RankLadder.tierAt(7).code == "GEN" &&
                RankLadder.tierAt(7).requirement == RankRequirement.CAMPAIGN_VICTORY,
            observed(RankLadder.tierAt(7).code, "GEN")
        )
        return recorder.result()
    }

    private fun medalCases(): List<ContractCase> {
        val recorder = CaseRecorder(TacticalContractAudit.GROUP_MEDAL)

        recorder.check(
            "Sharpshooter at the threshold", "85% and 5 headshots awards",
            MedalEvaluator.has(
                cleanRecord.copy(accuracyPercent = 85f, headshots = 5),
                Medal.SHARPSHOOTER
            ),
            observed(MedalEvaluator.medals(cleanRecord.copy(accuracyPercent = 85f, headshots = 5)), Medal.SHARPSHOOTER)
        )
        recorder.check(
            "Sharpshooter below the threshold", "84% does not award",
            !MedalEvaluator.has(cleanRecord.copy(accuracyPercent = 84f), Medal.SHARPSHOOTER),
            observed(MedalEvaluator.medals(cleanRecord.copy(accuracyPercent = 84f)), "no SHARPSHOOTER")
        )
        recorder.check(
            "Purple Heart below 15%", "14% with a kit awards",
            MedalEvaluator.has(cleanRecord.copy(lowestHealthPercent = 14), Medal.PURPLE_HEART),
            observed(MedalEvaluator.medals(cleanRecord.copy(lowestHealthPercent = 14)), Medal.PURPLE_HEART)
        )
        recorder.check(
            "Purple Heart boundary", "exactly 15% does not award",
            !MedalEvaluator.has(cleanRecord.copy(lowestHealthPercent = 15), Medal.PURPLE_HEART),
            observed(MedalEvaluator.medals(cleanRecord.copy(lowestHealthPercent = 15)), "no PURPLE_HEART")
        )
        val quiet = cleanRecord.copy(approach = MissionApproach.STEALTH, alarmTriggered = false)
        recorder.check(
            "Tactical Genius", "stealth with no alarm awards",
            MedalEvaluator.has(quiet, Medal.TACTICAL_GENIUS),
            observed(MedalEvaluator.medals(quiet), Medal.TACTICAL_GENIUS)
        )
        recorder.check(
            "Tactical Genius requires silence", "a triggered alarm voids it",
            !MedalEvaluator.has(quiet.copy(alarmTriggered = true), Medal.TACTICAL_GENIUS),
            observed(MedalEvaluator.medals(quiet.copy(alarmTriggered = true)), "no TACTICAL_GENIUS")
        )
        val honor = cleanRecord.copy(act = CampaignAct.ACT_III)
        recorder.check(
            "Medal of Honor", "all squadmates saved in Act III awards",
            MedalEvaluator.has(honor, Medal.MEDAL_OF_HONOR),
            observed(MedalEvaluator.medals(honor), Medal.MEDAL_OF_HONOR)
        )
        recorder.check(
            "Medal of Honor act gate", "Act II does not award",
            !MedalEvaluator.has(honor.copy(act = CampaignAct.ACT_II), Medal.MEDAL_OF_HONOR),
            observed(MedalEvaluator.medals(honor.copy(act = CampaignAct.ACT_II)), "no MEDAL_OF_HONOR")
        )
        recorder.check(
            "Ribbon wall", "four ribbons listed",
            MedalEvaluator.ribbonWall(cleanRecord).size == 4,
            observed(MedalEvaluator.ribbonWall(cleanRecord).size, 4)
        )
        return recorder.result()
    }

    private fun stateCases(): List<ContractCase> {
        val recorder = CaseRecorder(TacticalContractAudit.GROUP_STATE)

        val saved = GameStateManager.save(
            GameStateManager.fresh("VANCE").copy(xp = 1_500, credits = 400, rankIndex = 2)
        )
        val loaded = GameStateManager.load(saved).valueOrNull()
        recorder.check(
            "Save round trip", "XP, credits and rank survive JSON",
            loaded != null && loaded.xp == 1_500 && loaded.credits == 400 && loaded.rankIndex == 2,
            observed(loaded?.xp, 1_500)
        )
        recorder.check(
            "Missing version rejected", "SAVE_CORRUPT",
            GameStateManager.load("{}").errorCodeOrNull() == "SAVE_CORRUPT",
            observed(GameStateManager.load("{}").errorCodeOrNull(), "SAVE_CORRUPT")
        )
        recorder.check(
            "Malformed JSON rejected", "SAVE_CORRUPT",
            GameStateManager.load("not-json").errorCodeOrNull() == "SAVE_CORRUPT",
            observed(GameStateManager.load("not-json").errorCodeOrNull(), "SAVE_CORRUPT")
        )
        val blocked = GameStateManager.applyMission(GameStateManager.fresh("VANCE"), cleanRecord, 500, 0)
        recorder.check(
            "Promotion waits for the qual", "no promotion at 500 XP without BASIC_RIFLE_QUAL",
            blocked.promotion == null,
            observed(blocked.promotion, null)
        )
        val qualifiedProfile = GameStateManager.satisfy(
            GameStateManager.fresh("VANCE"),
            RankRequirement.BASIC_RIFLE_QUAL
        )
        val promoted = GameStateManager.applyMission(qualifiedProfile, cleanRecord, 500, 0)
        recorder.check(
            "Promotion ceremony", "RCT to PVT",
            promoted.promotion?.to?.code == "PVT" && promoted.profile.rankIndex == 1,
            observed(promoted.promotion?.to?.code, "PVT")
        )
        recorder.check(
            "Ceremony plays once", "second promote returns null",
            GameStateManager.promote(promoted.profile) == null,
            observed(GameStateManager.promote(promoted.profile), null)
        )
        recorder.check(
            "Negative XP ignored", "XP stays at 0",
            GameStateManager.awardXp(GameStateManager.fresh(), -250).xp == 0,
            observed(GameStateManager.awardXp(GameStateManager.fresh(), -250).xp, 0)
        )
        return recorder.result()
    }

    private fun academyCases(): List<ContractCase> {
        val recorder = CaseRecorder(TacticalContractAudit.GROUP_ACADEMY)

        recorder.check(
            "Course par", "120 s across six stations",
            approxEquals(ObstacleCourse.PAR_SECONDS, 120f),
            observed(ObstacleCourse.PAR_SECONDS, 120f)
        )
        val parRun = ObstacleRun(CourseStation.entries.map { StationSplit(it, it.parSplitSeconds) }, 0)
        val parResult = ObstacleCourse.evaluate(parRun).valueOrNull()
        recorder.check(
            "Par run passes", "PASS at exactly par",
            parResult?.grade == TrainingGrade.PASS && parResult.passed,
            observed(parResult?.grade, TrainingGrade.PASS)
        )
        val fastRun = ObstacleRun(CourseStation.entries.map { StationSplit(it, it.parSplitSeconds * 0.8f) }, 0)
        recorder.check(
            "Fast run distinguishes", "DISTINCTION at 80% of par",
            ObstacleCourse.evaluate(fastRun).valueOrNull()?.grade == TrainingGrade.DISTINCTION,
            observed(ObstacleCourse.evaluate(fastRun).valueOrNull()?.grade, TrainingGrade.DISTINCTION)
        )
        recorder.check(
            "Fall limit", "four falls fail the course",
            ObstacleCourse.evaluate(parRun.copy(falls = 4)).valueOrNull()?.passed == false,
            observed(ObstacleCourse.evaluate(parRun.copy(falls = 4)).valueOrNull()?.passed, false)
        )
        val duplicated = ObstacleRun(
            listOf(StationSplit(CourseStation.TIRE_RUN, 20f), StationSplit(CourseStation.TIRE_RUN, 20f)),
            0
        )
        recorder.check(
            "Duplicate station rejected", "DUPLICATE_STATION",
            ObstacleCourse.evaluate(duplicated).errorCodeOrNull() == "DUPLICATE_STATION",
            observed(ObstacleCourse.evaluate(duplicated).errorCodeOrNull(), "DUPLICATE_STATION")
        )
        recorder.check(
            "Basic Rifle Qual", "38 of 50 qualifies",
            FiringRange.evaluate(RangeRun(50, 38)).valueOrNull()?.qualified == true,
            observed(FiringRange.evaluate(RangeRun(50, 38)).valueOrNull()?.qualified, true)
        )
        recorder.check(
            "Basic Rifle Qual floor", "37 of 50 does not qualify",
            FiringRange.evaluate(RangeRun(50, 37)).valueOrNull()?.qualified == false,
            observed(FiringRange.evaluate(RangeRun(50, 37)).valueOrNull()?.qualified, false)
        )
        recorder.check(
            "Impossible score rejected", "40 hits from 30 shots",
            FiringRange.evaluate(RangeRun(30, 40)).errorCodeOrNull() == "IMPOSSIBLE_SCORE",
            observed(FiringRange.evaluate(RangeRun(30, 40)).errorCodeOrNull(), "IMPOSSIBLE_SCORE")
        )
        recorder.check(
            "Range scoring", "40 hits + 5 headshots = 475",
            FiringRange.score(RangeRun(50, 40, 5)) == 475,
            observed(FiringRange.score(RangeRun(50, 40, 5)), 475)
        )
        recorder.check(
            "Academy hub zones", "Barracks, Armory, Range, Briefing",
            AcademyHub.ZONES.size == 4 && AcademyHub.ZONES.map { it.id } ==
                listOf("barracks", "armory", "firing_range", "briefing_room"),
            observed(AcademyHub.ZONES.map { it.id }, "barracks/armory/firing_range/briefing_room")
        )
        recorder.check(
            "Hub collision sweep", "kit locker blocks the sweep",
            AcademyHub.blockages(Vec3(4.2f, 0f, 2.4f), 0.4f).any { it.id == "kit_locker" },
            observed(AcademyHub.blockages(Vec3(4.2f, 0f, 2.4f), 0.4f).map { it.id }, "kit_locker")
        )
        recorder.check(
            "Zone lookup", "x=24 resolves to the firing range",
            AcademyHub.zoneContaining(Vec3(24f, 0f, 0f))?.id == "firing_range",
            observed(AcademyHub.zoneContaining(Vec3(24f, 0f, 0f))?.id, "firing_range")
        )
        recorder.check(
            "Drill Sergeant", "Vance delivers the range brief",
            DrillSergeant.NAME.contains("Vance") &&
                DrillSergeant.line(SergeantEvent.RANGE_START).contains("38 hits"),
            observed(DrillSergeant.NAME, "Drill Sergeant Vance")
        )
        return recorder.result()
    }
}
