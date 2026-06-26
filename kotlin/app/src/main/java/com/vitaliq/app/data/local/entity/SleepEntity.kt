package com.vitaliq.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.vitaliq.app.data.model.SleepDto

@Entity(tableName = "sleep")
data class SleepEntity(
    @PrimaryKey val id: String,
    val hours: Float,
    val quality: Int?,
    val loggedAt: String?
)

fun SleepEntity.toDto() = SleepDto(
    id = id,
    hours = hours,
    quality = quality,
    loggedAt = loggedAt
)

fun SleepDto.toEntity() = SleepEntity(
    id = id ?: "sleep-${loggedAt ?: System.currentTimeMillis()}",
    hours = hours,
    quality = quality,
    loggedAt = loggedAt
)
