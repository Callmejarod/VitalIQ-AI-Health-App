package com.vitaliq.app.data.repository

import com.vitaliq.app.data.api.ApiService
import com.vitaliq.app.data.local.dao.ProfileDao
import com.vitaliq.app.data.local.entity.toDto
import com.vitaliq.app.data.local.entity.toEntity
import com.vitaliq.app.data.model.DashboardSummaryDto
import com.vitaliq.app.data.model.ProfileDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DashboardRepositoryImpl(
    private val api: ApiService,
    private val profileDao: ProfileDao
) : DashboardRepository {

    override suspend fun getDashboardSummary(): DashboardSummaryDto = withContext(Dispatchers.IO) {
        api.dashboardSummary()
    }

    override suspend fun getProfile(): ProfileDto = withContext(Dispatchers.IO) {
        try {
            val dto = api.getProfile()
            profileDao.insert(dto.toEntity())
            dto
        } catch (e: Exception) {
            profileDao.get()?.toDto() ?: throw e
        }
    }
}
