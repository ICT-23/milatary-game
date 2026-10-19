package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface EmberwatchDao {

    // Campaign Profile
    @Query("SELECT * FROM campaign_profile WHERE id = 1 LIMIT 1")
    fun getCampaignProfileFlow(): Flow<CampaignProfileEntity?>

    @Query("SELECT * FROM campaign_profile WHERE id = 1 LIMIT 1")
    suspend fun getCampaignProfile(): CampaignProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveCampaignProfile(profile: CampaignProfileEntity)

    @Query("DELETE FROM campaign_profile")
    suspend fun clearCampaignProfile()

    // Mission Progress
    @Query("SELECT * FROM mission_progress")
    fun getAllMissionProgressFlow(): Flow<List<MissionProgressEntity>>

    @Query("SELECT * FROM mission_progress WHERE missionId = :missionId LIMIT 1")
    suspend fun getMissionProgress(missionId: String): MissionProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveMissionProgress(progress: MissionProgressEntity)

    @Query("DELETE FROM mission_progress")
    suspend fun clearAllMissionProgress()

    // Story Flags
    @Query("SELECT * FROM story_flags")
    fun getAllStoryFlagsFlow(): Flow<List<StoryFlagEntity>>

    @Query("SELECT flagValue FROM story_flags WHERE flagKey = :key LIMIT 1")
    suspend fun getStoryFlag(key: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setStoryFlag(flag: StoryFlagEntity)

    @Query("DELETE FROM story_flags")
    suspend fun clearStoryFlags()

    // Settings
    @Query("SELECT * FROM tactical_settings WHERE id = 1 LIMIT 1")
    fun getTacticalSettingsFlow(): Flow<TacticalSettingsEntity?>

    @Query("SELECT * FROM tactical_settings WHERE id = 1 LIMIT 1")
    suspend fun getTacticalSettings(): TacticalSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveTacticalSettings(settings: TacticalSettingsEntity)
}
