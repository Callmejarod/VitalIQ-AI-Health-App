package com.vitaliq.app.data.local.dao

import androidx.room.*
import com.vitaliq.app.data.local.entity.NutritionEntity

@Dao
interface NutritionDao {
    @Query("SELECT * FROM nutrition ORDER BY loggedAt DESC")
    suspend fun getAll(): List<NutritionEntity>

    @Query("SELECT * FROM nutrition WHERE kind = :kind ORDER BY loggedAt DESC")
    suspend fun getByKind(kind: String): List<NutritionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(nutrition: NutritionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(nutrition: List<NutritionEntity>)
}
