package com.vitaliq.app.data.repository

import com.vitaliq.app.data.model.InsightDto

interface InsightsRepository {
    suspend fun latestInsight(): InsightDto?
    suspend fun generateInsights(): InsightDto
}
