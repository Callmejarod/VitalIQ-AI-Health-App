package com.vitaliq.app.data.repository

import com.vitaliq.app.data.model.HealthEntryDto
import com.vitaliq.app.data.model.MedicationDto
import com.vitaliq.app.data.model.NutritionDto
import com.vitaliq.app.data.model.SleepDto

interface HealthRepository {
    suspend fun createHealthEntry(dto: HealthEntryDto): HealthEntryDto
    suspend fun listHealthEntries(type: String? = null): List<HealthEntryDto>
    suspend fun createMedication(dto: MedicationDto): MedicationDto
    suspend fun listMedications(): List<MedicationDto>
    suspend fun createNutrition(dto: NutritionDto): NutritionDto
    suspend fun listNutrition(kind: String? = null): List<NutritionDto>
    suspend fun createSleep(dto: SleepDto): SleepDto
    suspend fun listSleep(): List<SleepDto>
}
