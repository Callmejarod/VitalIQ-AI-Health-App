package com.vitaliq.app.data.api

import com.vitaliq.app.data.model.*
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.http.*

interface ApiService {

    @GET("profile")
    suspend fun getProfile(): ProfileDto

    @PUT("profile")
    suspend fun updateProfile(@Body body: ProfileDto): ProfileDto

    @POST("workouts")
    suspend fun createWorkout(@Body body: WorkoutDto): WorkoutDto

    @GET("workouts")
    suspend fun listWorkouts(): List<WorkoutDto>

    @POST("health-entries")
    suspend fun createHealthEntry(@Body body: HealthEntryDto): HealthEntryDto

    @GET("health-entries")
    suspend fun listHealthEntries(@Query("entry_type") type: String? = null): List<HealthEntryDto>

    @POST("medications")
    suspend fun createMedication(@Body body: MedicationDto): MedicationDto

    @GET("medications")
    suspend fun listMedications(): List<MedicationDto>

    @POST("nutrition")
    suspend fun createNutrition(@Body body: NutritionDto): NutritionDto

    @GET("nutrition")
    suspend fun listNutrition(@Query("kind") kind: String? = null): List<NutritionDto>

    @POST("sleep")
    suspend fun createSleep(@Body body: SleepDto): SleepDto

    @GET("sleep")
    suspend fun listSleep(): List<SleepDto>

    @POST("insights/generate")
    suspend fun generateInsights(): InsightDto

    @GET("insights/latest")
    suspend fun latestInsight(): InsightDto?

    @GET("dashboard/summary")
    suspend fun dashboardSummary(): DashboardSummaryDto

    @Multipart
    @POST("import")
    suspend fun importFile(@Part file: MultipartBody.Part): ResponseBody
}
