package com.vitaliq.app.data.model

import com.google.gson.annotations.SerializedName

data class ProfileDto(
    @SerializedName("user_id") val userId: String? = null,
    val name: String = "",
    val age: Int? = null,
    @SerializedName("height_cm") val heightCm: Float? = null,
    @SerializedName("weight_kg") val weightKg: Float? = null,
    @SerializedName("fitness_goal") val fitnessGoal: String? = null,
    @SerializedName("daily_step_goal") val dailyStepGoal: Int = 8000,
    @SerializedName("daily_water_goal_ml") val dailyWaterGoalMl: Int = 2000,
    @SerializedName("updated_at") val updatedAt: String? = null
)

data class DashboardSummaryDto(
    @SerializedName("steps_today") val stepsToday: Int = 0,
    @SerializedName("workout_minutes_today") val workoutMinutesToday: Int = 0,
    @SerializedName("workouts_today") val workoutsToday: Int = 0,
    @SerializedName("last_sleep_hours") val lastSleepHours: Float? = null,
    @SerializedName("water_ml_today") val waterMlToday: Int = 0,
    @SerializedName("last_hr") val lastHr: Int? = null,
    @SerializedName("last_bp") val lastBp: Map<String, Int>? = null,
    @SerializedName("health_score") val healthScore: Int = 0
)

data class WorkoutDto(
    val id: String? = null,
    @SerializedName("started_at") val startedAt: String,
    @SerializedName("ended_at") val endedAt: String,
    @SerializedName("duration_seconds") val durationSeconds: Int,
    @SerializedName("activity_type") val activityType: String,
    val steps: Int,
    @SerializedName("avg_intensity") val avgIntensity: Float,
    @SerializedName("activity_breakdown") val activityBreakdown: Map<String, Int>
)

data class HealthEntryDto(
    val id: String? = null,
    @SerializedName("entry_type") val entryType: String,
    val value: Map<String, Any>,
    @SerializedName("logged_at") val loggedAt: String? = null
)

data class MedicationDto(
    val id: String? = null,
    val name: String,
    val dosage: String,
    val frequency: String,
    @SerializedName("taken_at") val takenAt: String? = null
)

data class NutritionDto(
    val id: String? = null,
    val kind: String,
    @SerializedName("meal_name") val mealName: String? = null,
    val calories: Int? = null,
    @SerializedName("protein_g") val proteinG: Float? = null,
    @SerializedName("carbs_g") val carbsG: Float? = null,
    @SerializedName("fats_g") val fatsG: Float? = null,
    @SerializedName("water_ml") val waterMl: Int? = null,
    @SerializedName("logged_at") val loggedAt: String? = null
)

data class SleepDto(
    val id: String? = null,
    val hours: Float,
    val quality: Int? = null,
    @SerializedName("logged_at") val loggedAt: String? = null
)

data class SuggestionDto(
    val title: String,
    val detail: String,
    val priority: Int,
    val category: String
)

data class InsightDto(
    @SerializedName("overall_score") val overallScore: Int = 0,
    @SerializedName("category_scores") val categoryScores: Map<String, Int> = emptyMap(),
    val suggestions: List<SuggestionDto> = emptyList(),
    @SerializedName("trend_summary") val trendSummary: String = "",
    val snapshot: Map<String, Any> = emptyMap(),
    @SerializedName("used_fallback") val usedFallback: Boolean = false,
    val empty: Boolean = false
)
