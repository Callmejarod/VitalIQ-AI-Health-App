package com.vitaliq.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.vitaliq.app.data.model.MedicationDto

@Entity(tableName = "medications")
data class MedicationEntity(
    @PrimaryKey val id: String,
    val name: String,
    val dosage: String,
    val frequency: String,
    val takenAt: String?
)

fun MedicationEntity.toDto() = MedicationDto(
    id = id,
    name = name,
    dosage = dosage,
    frequency = frequency,
    takenAt = takenAt
)

fun MedicationDto.toEntity() = MedicationEntity(
    id = id ?: "med-${takenAt ?: System.currentTimeMillis()}",
    name = name,
    dosage = dosage,
    frequency = frequency,
    takenAt = takenAt
)
