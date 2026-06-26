package com.vitaliq.app.data.local.dao

import androidx.room.*
import com.vitaliq.app.data.local.entity.MedicationEntity

@Dao
interface MedicationDao {
    @Query("SELECT * FROM medications ORDER BY takenAt DESC")
    suspend fun getAll(): List<MedicationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(medication: MedicationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(medications: List<MedicationEntity>)
}
