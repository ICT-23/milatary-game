package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        CampaignProfileEntity::class,
        MissionProgressEntity::class,
        StoryFlagEntity::class,
        TacticalSettingsEntity::class,
        AcademyProgressEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class EmberwatchDatabase : RoomDatabase() {
    abstract fun dao(): EmberwatchDao

    companion object {
        @Volatile
        private var INSTANCE: EmberwatchDatabase? = null

        /** Additive migration: existing campaign saves are never dropped. */
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `academy_progress` (" +
                        "`id` INTEGER NOT NULL, " +
                        "`meritPoints` INTEGER NOT NULL, " +
                        "`rankIndex` INTEGER NOT NULL, " +
                        "`weaponId` TEXT NOT NULL, " +
                        "`loadoutJson` TEXT NOT NULL, " +
                        "`rangeBestScore` INTEGER NOT NULL, " +
                        "`obstacleBestSeconds` INTEGER NOT NULL, " +
                        "`killhouseBestScore` INTEGER NOT NULL, " +
                        "`droneBestScore` INTEGER NOT NULL, " +
                        "`drillRuns` INTEGER NOT NULL, " +
                        "`updatedAt` INTEGER NOT NULL, " +
                        "PRIMARY KEY(`id`))"
                )
            }
        }

        /** Adds the Intel column the override dilemma writes to. */
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE `campaign_profile` ADD COLUMN `intel` INTEGER NOT NULL DEFAULT 0"
                )
            }
        }

        /**
         * Adds the economy, the durable campaign variables and the display / audio
         * options. Purely additive, so no existing progress is touched.
         */
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `campaign_profile` ADD COLUMN `serviceCredits` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `campaign_profile` ADD COLUMN `commendationTokens` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `campaign_profile` ADD COLUMN `civilianSafetyRecord` INTEGER NOT NULL DEFAULT 50")
                db.execSQL("ALTER TABLE `campaign_profile` ADD COLUMN `missionIntegrity` INTEGER NOT NULL DEFAULT 50")
                db.execSQL("ALTER TABLE `campaign_profile` ADD COLUMN `resourceReadiness` INTEGER NOT NULL DEFAULT 50")
                db.execSQL("ALTER TABLE `tactical_settings` ADD COLUMN `qualityPreset` TEXT NOT NULL DEFAULT 'MEDIUM'")
                db.execSQL("ALTER TABLE `tactical_settings` ADD COLUMN `masterVolume` REAL NOT NULL DEFAULT 0.9")
                db.execSQL("ALTER TABLE `tactical_settings` ADD COLUMN `dialogueVolume` REAL NOT NULL DEFAULT 0.85")
                db.execSQL("ALTER TABLE `tactical_settings` ADD COLUMN `subtitlesEnabled` INTEGER NOT NULL DEFAULT 1")
            }
        }

        /**
         * Completes the seven campaign variables so they can be read back exactly as
         * they were written. Additive only: no column is renamed or dropped.
         */
        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `campaign_profile` ADD COLUMN `commandReputation` INTEGER NOT NULL DEFAULT 50")
                db.execSQL("ALTER TABLE `campaign_profile` ADD COLUMN `intelligenceConfidence` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `campaign_profile` ADD COLUMN `mentorRespect` INTEGER NOT NULL DEFAULT 50")
            }
        }

        fun getDatabase(context: Context): EmberwatchDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    EmberwatchDatabase::class.java,
                    "emberwatch_local.db"
                )
                    // No destructive fallback: a save is never replaced with an
                    // empty default state, per the save-reliability requirements.
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
