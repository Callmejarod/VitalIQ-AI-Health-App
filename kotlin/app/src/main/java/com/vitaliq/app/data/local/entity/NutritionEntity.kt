package com.vitaliq.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.vitaliq.app.data.model.NutritionDto

@Entity(tableName = "nutrition")
data class NutritionEntity(
    @PrimaryKey val id: String,
    val kind: String,
    val mealName: String?,
    val calories: Int?,
    val proteinG: Float?,
    val carbsG: Float?,
    val fatsG: Float?,
    val waterMl: Int?,
    val loggedAt: String?
)

fun NutritionEntity.toDto() = NutritionDto(
    id = id,
    kind = kind,
    mealName = mealName,
    calories = calories,
    proteinG = proteinG,
    carbsG = carbsG,
    fatsG = fatsG,
    waterMl = waterMl,
    loggedAt = loggedAt
)

fun NutritionDto.toEntity() = NutritionEntity(
    id = id ?: "$kind-${loggedAt ?: System.currentTimeMillis()}",
    kind = kind,
    mealName = mealName,
    calories = calories,
    proteinG = proteinG,
    carbsG = carbsG,
    fatsG = fatsG,
    waterMl = waterMl,
    loggedAt = loggedAt
)
