package com.example.data

import com.example.game.MissionCatalog
import com.example.game.academy.DrillId
import com.example.game.academy.DrillResult
import com.example.game.combat.AttachmentSocket
import com.example.game.combat.LoadoutCodec
import com.example.game.combat.MeritRules
import com.example.game.combat.RankLadder
import com.example.game.combat.RankTier
import com.example.game.progression.AwardEvidence
import com.example.game.progression.AwardLedger
import com.example.game.progression.CampaignEnding
import com.example.game.progression.CampaignVariables
import com.example.game.progression.Commendation
import com.example.game.progression.EndingEvaluator
import com.example.game.progression.GradeEvidence
import com.example.game.progression.MissionGrade
import com.example.game.progression.MissionGrader
import com.example.game.progression.PromotionEvidence
import com.example.game.progression.PromotionLadder
import com.example.game.progression.Qualification
import com.example.game.progression.RewardRules
import kotlinx.coroutines.flow.Flow

/** Outcome of a cleared chapter, so the UI never has to re-read the profile. */
data class MissionCompletion(
    val awardedXp: Int,
    val rankTitle: String,
    val rankLevel: Int,
    val experiencePoints: Int,
    val nextMissionId: String?,
    val meritAwarded: Int = 0,
    val academyRankTitle: String? = null,
    val academyRankIndex: Int = 0,
    /** Rank held before the clear, so the AAR can show the jump. */
    val previousRankTitle: String = "Cadet",
    /** Non-null only when this clear earned a campaign rank promotion. */
    val promotedRank: String? = null,
    /** Cipher intelligence the chosen override path added. */
    val intelGained: Int = 0,
    /** Section 6.4 transparent grade and the recorded reason for it. */
    val grade: MissionGrade = MissionGrade.C,
    val gradeScore: Int = 0,
    val gradeReasons: List<String> = emptyList(),
    /** Section 6.5 commendations this clear actually earned. */
    val awardsEarned: List<Commendation> = emptyList(),
    /** Section 17.1 rewards. */
    val serviceCreditsGained: Int = 0,
    val commendationTokensGained: Int = 0,
    /** What still blocks the next promotion, per section 6.1. */
    val promotionOutstanding: List<String> = emptyList(),
    /** Section 3.7: resolved ending, set only when the final chapter is cleared. */
    val ending: CampaignEnding? = null
)

/** Result of a merit award, including every promotion it triggered. */
data class MeritAward(
    val pointsAwarded: Int,
    val totalMerit: Int,
    val previousRankIndex: Int,
    val newRankIndex: Int,
    val promoted: List<RankTier>
) {
    val didPromote: Boolean get() = promoted.isNotEmpty()
    val promotedTitle: String? get() = promoted.lastOrNull()?.display
}

class EmberwatchRepository(private val dao: EmberwatchDao) {

    val campaignProfileFlow: Flow<CampaignProfileEntity?> = dao.getCampaignProfileFlow()
    val allMissionsFlow: Flow<List<MissionProgressEntity>> = dao.getAllMissionProgressFlow()
    val storyFlagsFlow: Flow<List<StoryFlagEntity>> = dao.getAllStoryFlagsFlow()
    val settingsFlow: Flow<TacticalSettingsEntity?> = dao.getTacticalSettingsFlow()
    val academyProgressFlow: Flow<AcademyProgressEntity?> = dao.getAcademyProgressFlow()

    suspend fun getOrCreateProfile(): CampaignProfileEntity {
        val existing = dao.getCampaignProfile()
        if (existing != null) return existing

        val defaultProfile = CampaignProfileEntity(
            callsign = "SPECTRE-7",
            archetype = "Balanced Field Operative",
            rankTitle = "Cadet",
            rankLevel = 1,
            experiencePoints = 0,
            currentMissionId = "M1_SIGNAL_IN_DUST",
            morale = 100,
            vossTrust = 50,
            idrisRespect = 50,
            leenaAffinity = 50,
            tomasSync = 50,
            kadeRivalry = 50
        )
        dao.saveCampaignProfile(defaultProfile)
        seedInitialMissions()
        return defaultProfile
    }

    private suspend fun seedInitialMissions() {
        val initialMissions = listOf(
            MissionProgressEntity(
                missionId = "M1_SIGNAL_IN_DUST",
                isUnlocked = true,
                isCompleted = false
            ),
            MissionProgressEntity(
                missionId = "M2_BROKEN_RELAY",
                isUnlocked = false,
                isCompleted = false
            ),
            MissionProgressEntity(
                missionId = "M3_SILENT_CROSSING",
                isUnlocked = false,
                isCompleted = false
            ),
            MissionProgressEntity(
                missionId = "M4_THE_LONG_RETURN",
                isUnlocked = false,
                isCompleted = false
            ),
            MissionProgressEntity(
                missionId = "M5_FAULT_LINES",
                isUnlocked = false,
                isCompleted = false
            ),
            MissionProgressEntity(
                missionId = "M6_EMBER_PROTOCOL",
                isUnlocked = false,
                isCompleted = false
            ),
            MissionProgressEntity(
                missionId = "M7_LAST_SIGNAL",
                isUnlocked = false,
                isCompleted = false
            )
        )
        for (mission in initialMissions) {
            if (dao.getMissionProgress(mission.missionId) == null) {
                dao.saveMissionProgress(mission)
            }
        }
    }

    suspend fun saveProfile(profile: CampaignProfileEntity) {
        dao.saveCampaignProfile(profile)
    }

    suspend fun completeMission(
        missionId: String,
        score: Int,
        stars: Int,
        civilianChoice: String?,
        timeSeconds: Long,
        healthPercent: Int = 100,
        accuracyPercent: Int = 100,
        optionalCompleted: Int = 0,
        optionalTotal: Int = 1,
        violations: Int = 0,
        squadOrdersIssued: Int = 0,
        keyIntelVerified: Int = 0,
        keyIntelTotal: Int = 0,
        repairObjectiveEfficient: Boolean = false,
        weatherAffected: Boolean = false
    ): MissionCompletion? {
        val dossier = MissionCatalog.byId(missionId)
        val previous = dao.getMissionProgress(missionId)
        val profile = dao.getCampaignProfile() ?: return null
        // Counted before this record lands, so the promotion evidence boundary is explicit.
        val completedBefore = dao.countCompletedMissions()

        // A replay should never downgrade a record the player already owns.
        val bestScore = maxOf(previous?.score ?: 0, score)
        val bestStars = maxOf(previous?.stars ?: 0, stars)
        val bestTime = if (previous != null && previous.completionTimeSeconds > 0L) {
            minOf(previous.completionTimeSeconds, timeSeconds)
        } else {
            timeSeconds
        }

        val record = MissionProgressEntity(
            missionId = missionId,
            isCompleted = true,
            isUnlocked = true,
            civilianChoice = civilianChoice ?: previous?.civilianChoice,
            score = bestScore,
            stars = bestStars,
            completionTimeSeconds = bestTime,
            completedAt = System.currentTimeMillis()
        )
        dao.saveMissionProgress(record)

        // Unlock the next chapter in catalogue order.
        MissionCatalog.nextAfter(missionId)?.let { next ->
            val nextProgress = dao.getMissionProgress(next.id)
            if (nextProgress == null) {
                dao.saveMissionProgress(MissionProgressEntity(missionId = next.id, isUnlocked = true))
            } else if (!nextProgress.isUnlocked) {
                dao.saveMissionProgress(nextProgress.copy(isUnlocked = true))
            }
        }

        if (civilianChoice != null) {
            setStoryFlag("${missionId}_CHOICE", civilianChoice)
        }

        // Only the first clear of a chapter pays out full experience.
        val firstClear = previous == null || !previous.isCompleted
        val awardedXp = if (firstClear) dossier.rewardXp else dossier.rewardXp / 4
        val updatedXp = profile.experiencePoints + awardedXp

        // Section 6.4: a transparent grade with the reason it was awarded.
        val primaryTotal = dossier.objectiveSequence.size.coerceAtLeast(1)
        val gradeResult = MissionGrader.evaluate(
            GradeEvidence(
                primaryCompleted = primaryTotal,
                primaryTotal = primaryTotal,
                optionalCompleted = optionalCompleted.coerceIn(0, optionalTotal),
                optionalTotal = optionalTotal,
                accuracyPercent = accuracyPercent,
                squadConditionPercent = healthPercent,
                civilianSafetyPercent = if (civilianChoice == "INTERCEPT_DATA") 50 else 100,
                civiliansAtRisk = true,
                speedMatters = false,
                withinParTime = timeSeconds <= dossier.parTimeSeconds,
                violations = violations
            )
        )

        // Section 17.2: full payout on the first clear, limited repeat reward after.
        val creditsGained = RewardRules.credits(gradeResult.grade, firstClear)

        // Section 6.1 / 6.2: a rank is only granted when every piece of its evidence holds.
        val qualifications = qualificationsFor(dao.countCompletedMissions())
        val leadership = leadershipScoreFor(profile)
        val evidenceBefore = PromotionEvidence(
            serviceXp = profile.experiencePoints,
            completedMissions = completedBefore,
            qualifications = qualificationsFor(completedBefore),
            leadershipScore = leadership
        )
        val evidenceAfter = PromotionEvidence(
            serviceXp = updatedXp,
            completedMissions = dao.countCompletedMissions(),
            qualifications = qualifications,
            leadershipScore = leadership
        )
        val promotions = PromotionLadder.promotionsBetween(evidenceBefore, evidenceAfter)
        val rung = PromotionLadder.rankFor(evidenceAfter)
        val review = PromotionLadder.review(evidenceAfter)

        // Section 6.5: commendations are idempotent through the story-flag ledger,
        // so replaying a chapter can never mint the same medal twice.
        val recordedAwards = dao.getAllStoryFlags().map { it.flagKey }.toSet()
        val awardEvidence = AwardEvidence(
            coreQualificationsComplete = qualifications.containsAll(CORE_QUALIFICATIONS),
            protectedObjectivesAllSafe = civilianChoice == "SAFEGUARD_CIVILIANS" && violations == 0,
            squadAllHealthy = healthPercent >= SQUAD_HEALTHY_PERCENT,
            repairObjectiveEfficient = repairObjectiveEfficient,
            keyIntelVerified = keyIntelVerified,
            keyIntelTotal = keyIntelTotal,
            weatherAffected = weatherAffected,
            objectiveLost = false,
            squadOrdersIssued = squadOrdersIssued,
            missionGrade = gradeResult.grade,
            campaignComplete = MissionCatalog.nextAfter(missionId) == null,
            leadershipScore = leadership
        )
        val newAwards = AwardLedger.newlyEarned(awardEvidence, recordedAwards)
        for (award in newAwards) {
            setStoryFlag(award.awardId, "EARNED:${System.currentTimeMillis()}")
        }

        // Merit Points feed the academy ladder, independent of campaign XP.
        val meritFull = MeritRules.missionMerit(score, stars, healthPercent)
        val meritAward = awardMerit(if (firstClear) meritFull else meritFull / 4)

        val nextChapter = MissionCatalog.nextAfter(missionId)?.id ?: missionId
        val impact = CHOICE_IMPACT[civilianChoice] ?: ChoiceImpact()

        val updatedProfile = profile.copy(
            experiencePoints = updatedXp,
            rankTitle = rung.title,
            rankLevel = rung.index,
            currentMissionId = nextChapter,
            morale = (profile.morale + impact.morale).coerceIn(0, 100),
            intel = (profile.intel + impact.intel).coerceAtLeast(0),
            vossTrust = (profile.vossTrust + impact.voss).coerceIn(0, 100),
            idrisRespect = (profile.idrisRespect + impact.idris).coerceIn(0, 100),
            leenaAffinity = (profile.leenaAffinity + impact.leena).coerceIn(0, 100),
            tomasSync = (profile.tomasSync + impact.tomas).coerceIn(0, 100),
            kadeRivalry = (profile.kadeRivalry + impact.kade).coerceIn(0, 100),
            serviceCredits = profile.serviceCredits + creditsGained,
            commendationTokens = profile.commendationTokens + newAwards.size,
            civilianSafetyRecord = CampaignVariables.clampScore(
                profile.civilianSafetyRecord + if (civilianChoice == "INTERCEPT_DATA") -10 else 8
            ),
            missionIntegrity = CampaignVariables.clampScore(
                profile.missionIntegrity + if (violations == 0) 5 else -violations * 8
            ),
            resourceReadiness = CampaignVariables.clampScore(
                profile.resourceReadiness + if (violations == 0) 2 else -5
            ),
            // Section 3.6: the remaining campaign variables move only on recorded events.
            commandReputation = CampaignVariables.clampScore(
                profile.commandReputation + reputationDelta(gradeResult.grade)
            ),
            intelligenceConfidence = CampaignVariables.clampScore(
                profile.intelligenceConfidence + impact.intel + keyIntelVerified * 2
            ),
            mentorRespect = CampaignVariables.clampScore(
                profile.mentorRespect + if (violations == 0) 4 else -violations * 6
            ),
            lastActiveAt = System.currentTimeMillis()
        )
        dao.saveCampaignProfile(updatedProfile)

        // Section 3.7: the ending resolves from the variables the campaign recorded,
        // and only once the last chapter in the catalogue is cleared.
        val campaignFinished = MissionCatalog.nextAfter(missionId) == null
        val resolvedEnding = if (campaignFinished) {
            EndingEvaluator.evaluate(campaignVariablesOf(updatedProfile)).also { ending ->
                setStoryFlag(CAMPAIGN_ENDING_FLAG, ending.name)
            }
        } else {
            null
        }

        return MissionCompletion(
            awardedXp = awardedXp,
            rankTitle = rung.title,
            rankLevel = rung.index,
            experiencePoints = updatedXp,
            nextMissionId = MissionCatalog.nextAfter(missionId)?.id,
            meritAwarded = meritAward.pointsAwarded,
            academyRankTitle = meritAward.promotedTitle,
            academyRankIndex = meritAward.newRankIndex,
            previousRankTitle = PromotionLadder.rankFor(evidenceBefore).title,
            promotedRank = promotions.lastOrNull()?.title,
            intelGained = impact.intel,
            grade = gradeResult.grade,
            gradeScore = gradeResult.score,
            gradeReasons = gradeResult.reasons,
            awardsEarned = newAwards,
            serviceCreditsGained = creditsGained,
            commendationTokensGained = RewardRules.commendationTokens(newAwards.size),
            promotionOutstanding = review.outstanding,
            ending = resolvedEnding
        )
    }

    /**
     * Qualifications are earned by activities that exist, never by pressing a
     * claim button. Drill records and cleared chapters are the only sources.
     */
    private suspend fun qualificationsFor(completedMissions: Int): Set<Qualification> {
        val academy = dao.getAcademyProgress()
        val earned = mutableSetOf<Qualification>()
        if (completedMissions >= 1) {
            earned += Qualification.SAFETY_ORIENTATION
            earned += Qualification.FIELD_DEPLOYMENT
        }
        if (completedMissions >= 2) earned += Qualification.SQUAD_EVALUATION
        if (completedMissions >= 5) earned += Qualification.LEADERSHIP_ASSESSMENT
        if (completedMissions >= 6) earned += Qualification.MULTI_OBJECTIVE
        if (completedMissions >= 7) earned += Qualification.URBAN_OPERATION
        if ((academy?.obstacleBestSeconds ?: 0) > 0) earned += Qualification.PHYSICAL_STANDARD
        if ((academy?.rangeBestScore ?: 0) > 0) {
            earned += Qualification.MARKSMANSHIP
            earned += Qualification.NAVIGATION
        }
        if ((academy?.droneBestScore ?: 0) > 0) earned += Qualification.NAVIGATION
        if ((academy?.killhouseBestScore ?: 0) > 0) earned += Qualification.SPECIALIST
        return earned
    }

    /** Leadership is the average of the recorded campaign variables, not a single stat. */
    private fun leadershipScoreFor(profile: CampaignProfileEntity): Int =
        campaignVariablesOf(profile).leadershipScore

    /** Read model for the seven durable campaign variables (section 3.6). */
    fun campaignVariablesOf(profile: CampaignProfileEntity): CampaignVariables = CampaignVariables(
        squadTrust = mapOf(
            "VOSS" to profile.vossTrust,
            "IDRIS" to profile.idrisRespect,
            "LEENA" to profile.leenaAffinity,
            "TOMAS" to profile.tomasSync
        ),
        commandReputation = profile.commandReputation,
        civilianSafetyRecord = profile.civilianSafetyRecord,
        missionIntegrity = profile.missionIntegrity,
        intelligenceConfidence = profile.intelligenceConfidence,
        resourceReadiness = profile.resourceReadiness,
        mentorRespect = profile.mentorRespect
    ).normalised()

    /** Section 6.4 grades move command reputation; a D can undo two clean clears. */
    private fun reputationDelta(grade: MissionGrade): Int = when (grade) {
        MissionGrade.S -> 8
        MissionGrade.A -> 5
        MissionGrade.B -> 1
        MissionGrade.C -> -3
        MissionGrade.D -> -8
    }

    private companion object {
        /** Section 3.7: the resolved ending is recorded once, for the campaign summary. */
        const val CAMPAIGN_ENDING_FLAG = "CAMPAIGN_ENDING"
        /** Section 6.5 "Academy Distinction" needs every core qualification. */
        val CORE_QUALIFICATIONS = setOf(
            Qualification.SAFETY_ORIENTATION,
            Qualification.PHYSICAL_STANDARD,
            Qualification.MARKSMANSHIP,
            Qualification.NAVIGATION
        )

        /** Section 6.5 "Squad First": every squadmate extracted healthy. */
        const val SQUAD_HEALTHY_PERCENT = 80

        /** Section 6.5 "Precision Mark": accuracy a passed range run must reach. */
        const val PRECISION_MARK_ACCURACY = 85
    }

    // ------------------------------------------------------------------ ACADEMY

    suspend fun getOrCreateAcademyProgress(): AcademyProgressEntity {
        val existing = dao.getAcademyProgress()
        if (existing != null) return existing
        val fresh = AcademyProgressEntity()
        dao.saveAcademyProgress(fresh)
        return fresh
    }

    /** Awards MP, recomputes the rank from scratch and reports every promotion. */
    suspend fun awardMerit(points: Int): MeritAward {
        val current = getOrCreateAcademyProgress()
        val awarded = points.coerceAtLeast(0)
        val total = current.meritPoints + awarded
        val previousRank = current.rankIndex
        val newRank = RankLadder.indexFor(total)
        dao.saveAcademyProgress(
            current.copy(
                meritPoints = total,
                rankIndex = newRank,
                updatedAt = System.currentTimeMillis()
            )
        )
        return MeritAward(
            pointsAwarded = awarded,
            totalMerit = total,
            previousRankIndex = previousRank,
            newRankIndex = newRank,
            promoted = RankLadder.promotionsBetween(previousRank, newRank)
        )
    }

    suspend fun saveLoadout(weaponId: String, selection: Map<AttachmentSocket, String>) {
        val current = getOrCreateAcademyProgress()
        dao.saveAcademyProgress(
            current.copy(
                weaponId = weaponId,
                loadoutJson = LoadoutCodec.encode(weaponId, selection),
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    /** Stores drill bests and pays the drill's merit in one transaction. */
    suspend fun recordDrillResult(result: DrillResult): MeritAward {
        val current = getOrCreateAcademyProgress()
        val updated = when (result.drill) {
            DrillId.RANGE -> current.copy(rangeBestScore = maxOf(current.rangeBestScore, result.score))
            DrillId.OBSTACLE -> current.copy(
                obstacleBestSeconds = if (current.obstacleBestSeconds <= 0) {
                    result.durationSeconds.toInt()
                } else {
                    minOf(current.obstacleBestSeconds, result.durationSeconds.toInt())
                }
            )
            DrillId.KILLHOUSE -> current.copy(killhouseBestScore = maxOf(current.killhouseBestScore, result.score))
            DrillId.DRONE -> current.copy(droneBestScore = maxOf(current.droneBestScore, result.score))
        }.copy(
            drillRuns = current.drillRuns + 1,
            updatedAt = System.currentTimeMillis()
        )
        dao.saveAcademyProgress(updated)
        // Section 6.5 "Precision Mark": a passed range qualification with few wasted rounds.
        if (result.drill == DrillId.RANGE &&
            result.passed &&
            result.accuracyPercent >= PRECISION_MARK_ACCURACY &&
            getStoryFlag(Commendation.PRECISION_MARK.awardId) == null
        ) {
            setStoryFlag(Commendation.PRECISION_MARK.awardId, "EARNED:${System.currentTimeMillis()}")
        }
        return awardMerit(result.meritAwarded)
    }

    suspend fun getOrCreateAcademyLoadout(): Pair<String, Map<AttachmentSocket, String>> {
        val progress = getOrCreateAcademyProgress()
        return LoadoutCodec.decode(progress.loadoutJson.ifBlank { progress.weaponId })
    }

    suspend fun setStoryFlag(key: String, value: String) {
        dao.setStoryFlag(StoryFlagEntity(flagKey = key, flagValue = value))
    }

    suspend fun getStoryFlag(key: String): String? {
        return dao.getStoryFlag(key)
    }

    suspend fun getOrCreateSettings(): TacticalSettingsEntity {
        val existing = dao.getTacticalSettings()
        if (existing != null) return existing
        val defaultSettings = TacticalSettingsEntity()
        dao.saveTacticalSettings(defaultSettings)
        return defaultSettings
    }

    suspend fun updateSettings(settings: TacticalSettingsEntity) {
        dao.saveTacticalSettings(settings)
    }

    suspend fun resetAllCampaignData() {
        dao.clearCampaignProfile()
        dao.clearAllMissionProgress()
        dao.clearStoryFlags()
        dao.clearAcademyProgress()
        getOrCreateProfile()
        getOrCreateAcademyProgress()
    }
}

/**
 * Relationship shift attached to a chapter decision. Kade's column is rivalry,
 * so a positive value pushes him further from the squad.
 */
private data class ChoiceImpact(
    val voss: Int = 0,
    val idris: Int = 0,
    val leena: Int = 0,
    val tomas: Int = 0,
    val kade: Int = 0,
    val intel: Int = 0,
    val morale: Int = 0
)

private val CHOICE_IMPACT = mapOf(
    "SAFEGUARD_CIVILIANS" to ChoiceImpact(voss = 15, idris = 20, leena = 6, tomas = 5, kade = 10, morale = 5),
    "INTERCEPT_DATA" to ChoiceImpact(voss = 5, idris = -5, leena = 10, tomas = -4, kade = -15, intel = 25, morale = -4),
    "DETAIN_KADE" to ChoiceImpact(voss = 10, leena = 4, tomas = 6, kade = 25, morale = 3),
    "COVER_KADE" to ChoiceImpact(voss = -6, leena = -4, tomas = -6, kade = -20, morale = -3),
    "BROADCAST_TRUTH" to ChoiceImpact(voss = 12, idris = 10, leena = 8, tomas = 4, morale = 8),
    "DESTROY_ARRAY" to ChoiceImpact(voss = 4, idris = -8, leena = -10, tomas = -5, kade = 10, morale = -8)
)
