package com.vitaliq.app.data.repository

import com.vitaliq.app.data.api.ApiService
import com.vitaliq.app.data.local.dao.InsightDao
import com.vitaliq.app.data.local.entity.toDto
import com.vitaliq.app.data.local.entity.toEntity
import com.vitaliq.app.data.model.InsightDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class InsightsRepositoryImpl(
    private val api: ApiService,
    private val dao: InsightDao
) : InsightsRepository {

    override suspend fun latestInsight(): InsightDto? = withContext(Dispatchers.IO) {
        try {
            val dto = api.latestInsight()
            if (dto != null) dao.insert(dto.toEntity())
            dto
        } catch (e: Exception) {
            dao.get()?.toDto()
        }
    }

    override suspend fun generateInsights(): InsightDto = withContext(Dispatchers.IO) {
        val dto = api.generateInsights()
        dao.insert(dto.toEntity())
        dto
    }
}
