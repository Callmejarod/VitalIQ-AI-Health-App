package com.vitaliq.app.data.local.dao

import androidx.room.*
import com.vitaliq.app.data.local.entity.InsightEntity

@Dao
interface InsightDao {
    @Query("SELECT * FROM insight LIMIT 1")
    suspend fun get(): InsightEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(insight: InsightEntity)
}
