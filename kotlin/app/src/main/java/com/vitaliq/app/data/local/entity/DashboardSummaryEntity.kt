package com.vitaliq.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.vitaliq.app.data.model.DashboardSummaryDto

@Entity(tableName = "dashboard_summary")
data class DashboardSummaryEntity(
    @PrimaryKey val id: String = "singleton",
    val stepsToday: Int,
    val workoutMinutesToday: Int,
    val workoutsToday: Int,
    val lastSleepHours: Float?,
    val waterMlToday: Int,
    val lastHr: Int?,
    val lastBpJson: String?,
    val healthScore: Int
)

private val gson = Gson()

fun DashboardSummaryEntity.toDto() = DashboardSummaryDto(
    stepsToday = stepsToday,
    workoutMinutesToday = workoutMinutesToday,
    workoutsToday = workoutsToday,
    lastSleepHours = lastSleepHours,
    waterMlToday = waterMlToday,
    lastHr = lastHr,
    lastBp = lastBpJson?.let { gson.fromJson(it, object : TypeToken<Map<String, Int>>() {}.type) },
    healthScore = healthScore
)

fun DashboardSummaryDto.toEntity() = DashboardSummaryEntity(
    stepsToday = stepsToday,
    workoutMinutesToday = workoutMinutesToday,
    workoutsToday = workoutsToday,
    lastSleepHours = lastSleepHours,
    waterMlToday = waterMlToday,
    lastHr = lastHr,
    lastBpJson = lastBp?.let { gson.toJson(it) },
    healthScore = healthScore
)
