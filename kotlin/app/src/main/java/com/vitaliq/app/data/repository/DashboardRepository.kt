package com.vitaliq.app.data.repository

import com.vitaliq.app.data.model.DashboardSummaryDto
import com.vitaliq.app.data.model.ProfileDto

interface DashboardRepository {
    suspend fun getDashboardSummary(): DashboardSummaryDto
    suspend fun getProfile(): ProfileDto
}
