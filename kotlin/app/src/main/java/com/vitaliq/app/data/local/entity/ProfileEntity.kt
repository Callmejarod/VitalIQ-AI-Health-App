package com.vitaliq.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.vitaliq.app.data.model.ProfileDto

@Entity(tableName = "profile")
data class ProfileEntity(
    @PrimaryKey val userId: String,
    val name: String,
    val age: Int?,
    val heightCm: Float?,
    val weightKg: Float?,
    val fitnessGoal: String?,
    val dailyStepGoal: Int,
    val dailyWaterGoalMl: Int,
    val updatedAt: String?
)

fun ProfileEntity.toDto() = ProfileDto(
    userId = userId,
    name = name,
    age = age,
    heightCm = heightCm,
    weightKg = weightKg,
    fitnessGoal = fitnessGoal,
    dailyStepGoal = dailyStepGoal,
    dailyWaterGoalMl = dailyWaterGoalMl,
    updatedAt = updatedAt
)

fun ProfileDto.toEntity() = ProfileEntity(
    userId = userId ?: "me",
    name = name,
    age = age,
    heightCm = heightCm,
    weightKg = weightKg,
    fitnessGoal = fitnessGoal,
    dailyStepGoal = dailyStepGoal,
    dailyWaterGoalMl = dailyWaterGoalMl,
    updatedAt = updatedAt
)
