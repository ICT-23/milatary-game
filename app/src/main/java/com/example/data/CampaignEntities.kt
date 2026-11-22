package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "campaign_profile")
data class CampaignProfileEntity(
    @PrimaryKey val id: Int = 1,
    val callsign: String = "SPECTRE-7",
    val archetype: String = "Balanced Field Operative",
    val rankTitle: String = "Junior Cadet",
    val rankLevel: Int = 1,
    val experiencePoints: Int = 0,
    val currentMissionId: String = "M1_SIGNAL_IN_DUST",
    val morale: Int = 100,
    /** Cipher intelligence recovered from saboteur transmissions. */
    val intel: Int = 0,
    val vossTrust: Int = 50,
    val idrisRespect: Int = 50,
    val leenaAffinity: Int = 50,
    val tomasSync: Int = 50,
    val kadeRivalry: Int = 50,
    /** Section 17.1 economy: earned through missions, spent on approved unlocks. */
    val serviceCredits: Int = 0,
    /** Section 17.1 economy: earned from awards only. */
    val commendationTokens: Int = 0,
    /** Section 3.6: outcomes of civilian-protection objectives. */
    val civilianSafetyRecord: Int = 50,
    /** Section 3.6: objectives completed without avoidable damage. */
    val missionIntegrity: Int = 50,
    /** Section 3.6: supplies and support availability. */
    val resourceReadiness: Int = 50,
    /** Section 3.6: how senior leadership views the operative. */
    val commandReputation: Int = 50,
    /** Section 3.6: quality of the information gathered and verified. */
    val intelligenceConfidence: Int = 0,
    /** Section 3.6: the mentor's evaluation of discipline and judgement. */
    val mentorRespect: Int = 50,
    val createdAt: Long = System.currentTimeMillis(),
    val lastActiveAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "mission_progress")
data class MissionProgressEntity(
    @PrimaryKey val missionId: String,
    val isCompleted: Boolean = false,
    val isUnlocked: Boolean = false,
    val civilianChoice: String? = null, // "SAFEGUARD_CIVILIANS" or "INTERCEPT_DATA"
    val score: Int = 0,
    val stars: Int = 0,
    val completionTimeSeconds: Long = 0,
    val objectivesCompletedJson: String = "[]",
    val completedAt: Long = 0
)

@Entity(tableName = "story_flags")
data class StoryFlagEntity(
    @PrimaryKey val flagKey: String,
    val flagValue: String,
    val updatedTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "tactical_settings")
data class TacticalSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val joystickSensitivity: Float = 1.0f,
    val hapticEnabled: Boolean = true,
    val soundEffectsVolume: Float = 0.8f,
    val musicVolume: Float = 0.7f,
    val scannerAutoPing: Boolean = true,
    val highContrastHud: Boolean = false,
    /** Section 21.3 quality preset: LOW, MEDIUM or HIGH. */
    val qualityPreset: String = "MEDIUM",
    /** Section 15.2: separate master and dialogue buses. */
    val masterVolume: Float = 0.9f,
    val dialogueVolume: Float = 0.85f,
    /** Section 15.2: important dialogue must always be readable. */
    val subtitlesEnabled: Boolean = true
)
