package com.vitaliq.app.data.local.dao

import androidx.room.*
import com.vitaliq.app.data.local.entity.HealthEntryEntity

@Dao
interface HealthEntryDao {
    @Query("SELECT * FROM health_entries ORDER BY loggedAt DESC")
    suspend fun getAll(): List<HealthEntryEntity>

    @Query("SELECT * FROM health_entries WHERE entryType = :type ORDER BY loggedAt DESC")
    suspend fun getByType(type: String): List<HealthEntryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<HealthEntryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: HealthEntryEntity)
}
