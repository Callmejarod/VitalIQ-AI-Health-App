package com.vitaliq.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.vitaliq.app.data.model.WorkoutDto

@Entity(tableName = "workouts")
data class WorkoutEntity(
    @PrimaryKey val id: String,
    val startedAt: String,
    val endedAt: String,
    val durationSeconds: Int,
    val activityType: String,
    val steps: Int,
    val avgIntensity: Float,
    val activityBreakdownJson: String
)

private val gson = Gson()

fun WorkoutEntity.toDto() = WorkoutDto(
    id = id,
    startedAt = startedAt,
    endedAt = endedAt,
    durationSeconds = durationSeconds,
    activityType = activityType,
    steps = steps,
    avgIntensity = avgIntensity,
    activityBreakdown = gson.fromJson(activityBreakdownJson, object : TypeToken<Map<String, Int>>() {}.type)
)

fun WorkoutDto.toEntity() = WorkoutEntity(
    id = id ?: startedAt,
    startedAt = startedAt,
    endedAt = endedAt,
    durationSeconds = durationSeconds,
    activityType = activityType,
    steps = steps,
    avgIntensity = avgIntensity,
    activityBreakdownJson = gson.toJson(activityBreakdown)
)
