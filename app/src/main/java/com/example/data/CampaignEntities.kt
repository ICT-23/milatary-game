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
    val vossTrust: Int = 50,
    val idrisRespect: Int = 50,
    val leenaAffinity: Int = 50,
    val tomasSync: Int = 50,
    val kadeRivalry: Int = 50,
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
    val highContrastHud: Boolean = false
)
