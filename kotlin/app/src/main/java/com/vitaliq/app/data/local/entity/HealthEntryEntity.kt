package com.vitaliq.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.vitaliq.app.data.model.HealthEntryDto

@Entity(tableName = "health_entries")
data class HealthEntryEntity(
    @PrimaryKey val id: String,
    val entryType: String,
    val valueJson: String,
    val loggedAt: String?
)

private val gson = Gson()

fun HealthEntryEntity.toDto() = HealthEntryDto(
    id = id,
    entryType = entryType,
    value = gson.fromJson(valueJson, object : TypeToken<Map<String, Any>>() {}.type),
    loggedAt = loggedAt
)

fun HealthEntryDto.toEntity() = HealthEntryEntity(
    id = id ?: "$entryType-${loggedAt ?: System.currentTimeMillis()}",
    entryType = entryType,
    valueJson = gson.toJson(value),
    loggedAt = loggedAt
)
