package com.vitaliq.app.data.repository

import com.vitaliq.app.data.model.WorkoutDto

interface WorkoutRepository {
    suspend fun createWorkout(dto: WorkoutDto): WorkoutDto
    suspend fun listWorkouts(): List<WorkoutDto>
}
