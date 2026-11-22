package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Academy merit progression, armory loadout and drill records.
 * Kept in its own table so the campaign save schema stays untouched.
 */
@Entity(tableName = "academy_progress")
data class AcademyProgressEntity(
    @PrimaryKey val id: Int = 1,
    val meritPoints: Int = 0,
    val rankIndex: Int = 0,
    val weaponId: String = "V9_SERVICE",
    val loadoutJson: String = "",
    val rangeBestScore: Int = 0,
    val obstacleBestSeconds: Int = 0,
    val killhouseBestScore: Int = 0,
    val droneBestScore: Int = 0,
    val drillRuns: Int = 0,
    val updatedAt: Long = System.currentTimeMillis()
)
