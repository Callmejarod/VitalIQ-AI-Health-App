package com.vitaliq.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.vitaliq.app.data.local.dao.HealthEntryDao
import com.vitaliq.app.data.local.dao.InsightDao
import com.vitaliq.app.data.local.dao.ProfileDao
import com.vitaliq.app.data.local.dao.WorkoutDao
import com.vitaliq.app.data.local.entity.HealthEntryEntity
import com.vitaliq.app.data.local.entity.InsightEntity
import com.vitaliq.app.data.local.entity.ProfileEntity
import com.vitaliq.app.data.local.entity.WorkoutEntity

@Database(
    entities = [WorkoutEntity::class, HealthEntryEntity::class, ProfileEntity::class, InsightEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun workoutDao(): WorkoutDao
    abstract fun healthEntryDao(): HealthEntryDao
    abstract fun profileDao(): ProfileDao
    abstract fun insightDao(): InsightDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase = INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "vitaliq.db"
            ).build().also { INSTANCE = it }
        }
    }
}
