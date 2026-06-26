package com.vitaliq.app.data.repository

import com.vitaliq.app.data.api.ApiService
import com.vitaliq.app.data.local.dao.WorkoutDao
import com.vitaliq.app.data.local.entity.toDto
import com.vitaliq.app.data.local.entity.toEntity
import com.vitaliq.app.data.model.WorkoutDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WorkoutRepositoryImpl(
    private val api: ApiService,
    private val dao: WorkoutDao
) : WorkoutRepository {

    override suspend fun createWorkout(dto: WorkoutDto): WorkoutDto = withContext(Dispatchers.IO) {
        val result = api.createWorkout(dto)
        dao.insert(result.toEntity())
        result
    }

    override suspend fun listWorkouts(): List<WorkoutDto> = withContext(Dispatchers.IO) {
        try {
            val workouts = api.listWorkouts()
            dao.insertAll(workouts.map { it.toEntity() })
            workouts
        } catch (e: Exception) {
            val cached = dao.getAll()
            if (cached.isEmpty()) throw e
            cached.map { it.toDto() }
        }
    }
}
