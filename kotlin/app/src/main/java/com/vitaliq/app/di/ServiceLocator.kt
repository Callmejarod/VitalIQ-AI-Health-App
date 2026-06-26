package com.vitaliq.app.di

import android.content.Context
import com.vitaliq.app.data.api.ApiService
import com.vitaliq.app.data.api.RetrofitClient
import com.vitaliq.app.data.local.AppDatabase
import com.vitaliq.app.data.repository.DashboardRepository
import com.vitaliq.app.data.repository.DashboardRepositoryImpl
import com.vitaliq.app.data.repository.HealthRepository
import com.vitaliq.app.data.repository.HealthRepositoryImpl
import com.vitaliq.app.data.repository.InsightsRepository
import com.vitaliq.app.data.repository.InsightsRepositoryImpl
import com.vitaliq.app.data.repository.ProfileRepository
import com.vitaliq.app.data.repository.ProfileRepositoryImpl
import com.vitaliq.app.data.repository.WorkoutRepository
import com.vitaliq.app.data.repository.WorkoutRepositoryImpl

/**
 * Manual composition root (plain Kotlin — NO dependency-injection framework).
 *
 * This object is the ONLY place in the app that knows about the concrete data
 * sources (Retrofit + Room). It constructs each [*RepositoryImpl] from the API
 * service and the Room DAOs and hands ViewModels back the repository INTERFACE.
 *
 * Consequence (the architectural test the course asks for): a ViewModel never
 * references Retrofit, Room, a DAO, or AppDatabase. Swapping a data source —
 * e.g. replacing Retrofit with Room as the source of truth — changes only the
 * matching RepositoryImpl and the one wiring line here; not a single ViewModel
 * file has to change.
 *
 * [init] must be called once at process start (see [com.vitaliq.app.VitalIQApp])
 * with the application context so the database/cache outlives any one screen.
 */
object ServiceLocator {

    private lateinit var appContext: Context

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    // --- Concrete data sources (private: nothing outside this file may see them) ---
    private val database: AppDatabase by lazy { AppDatabase.getInstance(appContext) }
    private val api: ApiService by lazy { RetrofitClient.apiService }

    // --- Repositories, exposed only as interfaces ---
    val dashboardRepository: DashboardRepository by lazy {
        DashboardRepositoryImpl(api, database.profileDao())
    }

    val workoutRepository: WorkoutRepository by lazy {
        WorkoutRepositoryImpl(api, database.workoutDao())
    }

    val healthRepository: HealthRepository by lazy {
        HealthRepositoryImpl(
            api,
            database.healthEntryDao(),
            database.medicationDao(),
            database.nutritionDao(),
            database.sleepDao()
        )
    }

    val profileRepository: ProfileRepository by lazy {
        ProfileRepositoryImpl(api, database.profileDao())
    }

    val insightsRepository: InsightsRepository by lazy {
        InsightsRepositoryImpl(api, database.insightDao())
    }
}
