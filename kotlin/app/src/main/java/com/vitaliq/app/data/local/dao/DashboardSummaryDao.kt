package com.vitaliq.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.vitaliq.app.data.local.entity.DashboardSummaryEntity

@Dao
interface DashboardSummaryDao {
    @Query("SELECT * FROM dashboard_summary WHERE id = 'singleton' LIMIT 1")
    suspend fun get(): DashboardSummaryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: DashboardSummaryEntity)
}
