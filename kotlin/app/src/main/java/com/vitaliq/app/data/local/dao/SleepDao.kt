package com.vitaliq.app.data.local.dao

import androidx.room.*
import com.vitaliq.app.data.local.entity.SleepEntity

@Dao
interface SleepDao {
    @Query("SELECT * FROM sleep ORDER BY loggedAt DESC")
    suspend fun getAll(): List<SleepEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(sleep: SleepEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(sleepList: List<SleepEntity>)
}
