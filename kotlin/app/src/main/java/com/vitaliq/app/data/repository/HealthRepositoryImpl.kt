package com.vitaliq.app.data.repository

import com.vitaliq.app.data.api.ApiService
import com.vitaliq.app.data.local.dao.HealthEntryDao
import com.vitaliq.app.data.local.entity.toDto
import com.vitaliq.app.data.local.entity.toEntity
import com.vitaliq.app.data.model.HealthEntryDto
import com.vitaliq.app.data.model.MedicationDto
import com.vitaliq.app.data.model.NutritionDto
import com.vitaliq.app.data.model.SleepDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class HealthRepositoryImpl(
    private val api: ApiService,
    private val dao: HealthEntryDao
) : HealthRepository {

    override suspend fun createHealthEntry(dto: HealthEntryDto): HealthEntryDto = withContext(Dispatchers.IO) {
        val result = api.createHealthEntry(dto)
        dao.insert(result.toEntity())
        result
    }

    override suspend fun listHealthEntries(type: String?): List<HealthEntryDto> = withContext(Dispatchers.IO) {
        try {
            val entries = api.listHealthEntries(type)
            dao.insertAll(entries.map { it.toEntity() })
            entries
        } catch (e: Exception) {
            val cached = if (type != null) dao.getByType(type) else dao.getAll()
            if (cached.isEmpty()) throw e
            cached.map { it.toDto() }
        }
    }

    override suspend fun createMedication(dto: MedicationDto): MedicationDto = withContext(Dispatchers.IO) {
        api.createMedication(dto)
    }

    override suspend fun createNutrition(dto: NutritionDto): NutritionDto = withContext(Dispatchers.IO) {
        api.createNutrition(dto)
    }

    override suspend fun createSleep(dto: SleepDto): SleepDto = withContext(Dispatchers.IO) {
        api.createSleep(dto)
    }
}
