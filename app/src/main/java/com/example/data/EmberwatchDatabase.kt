package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        CampaignProfileEntity::class,
        MissionProgressEntity::class,
        StoryFlagEntity::class,
        TacticalSettingsEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class EmberwatchDatabase : RoomDatabase() {
    abstract fun dao(): EmberwatchDao

    companion object {
        @Volatile
        private var INSTANCE: EmberwatchDatabase? = null

        fun getDatabase(context: Context): EmberwatchDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    EmberwatchDatabase::class.java,
                    "emberwatch_local.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
