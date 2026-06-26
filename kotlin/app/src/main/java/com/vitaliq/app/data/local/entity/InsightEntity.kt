package com.vitaliq.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.vitaliq.app.data.model.InsightDto
import com.vitaliq.app.data.model.SuggestionDto

@Entity(tableName = "insight")
data class InsightEntity(
    @PrimaryKey val rowId: String = "latest",
    val overallScore: Int,
    val categoryScoresJson: String,
    val suggestionsJson: String,
    val trendSummary: String,
    val snapshotJson: String,
    val usedFallback: Boolean,
    val empty: Boolean
)

private val gson = Gson()

fun InsightEntity.toDto() = InsightDto(
    overallScore = overallScore,
    categoryScores = gson.fromJson(categoryScoresJson, object : TypeToken<Map<String, Int>>() {}.type),
    suggestions = gson.fromJson(suggestionsJson, object : TypeToken<List<SuggestionDto>>() {}.type),
    trendSummary = trendSummary,
    snapshot = gson.fromJson(snapshotJson, object : TypeToken<Map<String, Any>>() {}.type),
    usedFallback = usedFallback,
    empty = empty
)

fun InsightDto.toEntity() = InsightEntity(
    rowId = "latest",
    overallScore = overallScore,
    categoryScoresJson = gson.toJson(categoryScores),
    suggestionsJson = gson.toJson(suggestions),
    trendSummary = trendSummary,
    snapshotJson = gson.toJson(snapshot),
    usedFallback = usedFallback,
    empty = empty
)
