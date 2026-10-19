package com.example.data

import kotlinx.coroutines.flow.Flow

class EmberwatchRepository(private val dao: EmberwatchDao) {

    val campaignProfileFlow: Flow<CampaignProfileEntity?> = dao.getCampaignProfileFlow()
    val allMissionsFlow: Flow<List<MissionProgressEntity>> = dao.getAllMissionProgressFlow()
    val storyFlagsFlow: Flow<List<StoryFlagEntity>> = dao.getAllStoryFlagsFlow()
    val settingsFlow: Flow<TacticalSettingsEntity?> = dao.getTacticalSettingsFlow()

    suspend fun getOrCreateProfile(): CampaignProfileEntity {
        val existing = dao.getCampaignProfile()
        if (existing != null) return existing

        val defaultProfile = CampaignProfileEntity(
            callsign = "SPECTRE-7",
            archetype = "Balanced Field Operative",
            rankTitle = "Junior Cadet",
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
        timeSeconds: Long
    ) {
        val record = MissionProgressEntity(
            missionId = missionId,
            isCompleted = true,
            isUnlocked = true,
            civilianChoice = civilianChoice,
            score = score,
            stars = stars,
            completionTimeSeconds = timeSeconds,
            completedAt = System.currentTimeMillis()
        )
        dao.saveMissionProgress(record)

        // Unlock next mission if M1 was completed
        if (missionId == "M1_SIGNAL_IN_DUST") {
            val nextMission = dao.getMissionProgress("M2_BROKEN_RELAY")
            if (nextMission != null) {
                dao.saveMissionProgress(nextMission.copy(isUnlocked = true))
            }
            // Update profile rank and XP
            val profile = dao.getCampaignProfile()
            if (profile != null) {
                val updatedXp = profile.experiencePoints + 250
                val updatedRank = if (updatedXp >= 200) "Cadet Operative" else profile.rankTitle
                dao.saveCampaignProfile(
                    profile.copy(
                        experiencePoints = updatedXp,
                        rankTitle = updatedRank,
                        rankLevel = 2,
                        currentMissionId = "M2_BROKEN_RELAY",
                        vossTrust = if (civilianChoice == "SAFEGUARD_CIVILIANS") profile.vossTrust + 15 else profile.vossTrust + 5,
                        idrisRespect = if (civilianChoice == "SAFEGUARD_CIVILIANS") profile.idrisRespect + 20 else profile.idrisRespect - 5,
                        kadeRivalry = if (civilianChoice == "INTERCEPT_DATA") profile.kadeRivalry - 10 else profile.kadeRivalry + 10
                    )
                )
            }
        }
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
        getOrCreateProfile()
    }
}
