package com.vitaliq.app.data.repository

import com.vitaliq.app.data.api.ApiService
import com.vitaliq.app.data.local.dao.HealthEntryDao
import com.vitaliq.app.data.local.dao.MedicationDao
import com.vitaliq.app.data.local.dao.NutritionDao
import com.vitaliq.app.data.local.dao.SleepDao
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
    private val healthEntryDao: HealthEntryDao,
    private val medicationDao: MedicationDao,
    private val nutritionDao: NutritionDao,
    private val sleepDao: SleepDao
) : HealthRepository {

    override suspend fun createHealthEntry(dto: HealthEntryDto): HealthEntryDto = withContext(Dispatchers.IO) {
        val result = api.createHealthEntry(dto)
        healthEntryDao.insert(result.toEntity())
        result
    }

    override suspend fun listHealthEntries(type: String?): List<HealthEntryDto> = withContext(Dispatchers.IO) {
        try {
            val entries = api.listHealthEntries(type)
            healthEntryDao.insertAll(entries.map { it.toEntity() })
            entries
        } catch (e: Exception) {
            val cached = if (type != null) healthEntryDao.getByType(type) else healthEntryDao.getAll()
            if (cached.isEmpty()) throw e
            cached.map { it.toDto() }
        }
    }

    override suspend fun createMedication(dto: MedicationDto): MedicationDto = withContext(Dispatchers.IO) {
        val result = api.createMedication(dto)
        medicationDao.insert(result.toEntity())
        result
    }

    override suspend fun listMedications(): List<MedicationDto> = withContext(Dispatchers.IO) {
        try {
            val meds = api.listMedications()
            medicationDao.insertAll(meds.map { it.toEntity() })
            meds
        } catch (e: Exception) {
            val cached = medicationDao.getAll()
            if (cached.isEmpty()) throw e
            cached.map { it.toDto() }
        }
    }

    override suspend fun createNutrition(dto: NutritionDto): NutritionDto = withContext(Dispatchers.IO) {
        val result = api.createNutrition(dto)
        nutritionDao.insert(result.toEntity())
        result
    }

    override suspend fun listNutrition(kind: String?): List<NutritionDto> = withContext(Dispatchers.IO) {
        try {
            val entries = api.listNutrition(kind)
            nutritionDao.insertAll(entries.map { it.toEntity() })
            entries
        } catch (e: Exception) {
            val cached = if (kind != null) nutritionDao.getByKind(kind) else nutritionDao.getAll()
            if (cached.isEmpty()) throw e
            cached.map { it.toDto() }
        }
    }

    override suspend fun createSleep(dto: SleepDto): SleepDto = withContext(Dispatchers.IO) {
        val result = api.createSleep(dto)
        sleepDao.insert(result.toEntity())
        result
    }

    override suspend fun listSleep(): List<SleepDto> = withContext(Dispatchers.IO) {
        try {
            val entries = api.listSleep()
            sleepDao.insertAll(entries.map { it.toEntity() })
            entries
        } catch (e: Exception) {
            val cached = sleepDao.getAll()
            if (cached.isEmpty()) throw e
            cached.map { it.toDto() }
        }
    }
}
