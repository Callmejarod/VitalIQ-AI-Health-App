package com.vitaliq.app.ui.screens.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vitaliq.app.data.api.RetrofitClient
import com.vitaliq.app.data.model.InsightDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class InsightsUiState {
    object Loading : InsightsUiState()
    object Empty : InsightsUiState()
    data class Success(val insight: InsightDto, val generating: Boolean = false) : InsightsUiState()
    data class Error(val message: String) : InsightsUiState()
}

class InsightsViewModel : ViewModel() {

    private val api = RetrofitClient.apiService

    private val _uiState = MutableStateFlow<InsightsUiState>(InsightsUiState.Loading)
    val uiState: StateFlow<InsightsUiState> = _uiState

    fun load() {
        viewModelScope.launch {
            _uiState.value = InsightsUiState.Loading
            try {
                val insight = api.latestInsight()
                _uiState.value = when {
                    insight == null || insight.empty -> InsightsUiState.Empty
                    else -> InsightsUiState.Success(insight)
                }
            } catch (e: Exception) {
                _uiState.value = InsightsUiState.Error(e.message ?: "Failed to load insights")
            }
        }
    }

    fun generateInsights() {
        val currentSuccess = (_uiState.value as? InsightsUiState.Success)
        _uiState.value = currentSuccess?.copy(generating = true) ?: InsightsUiState.Loading
        viewModelScope.launch {
            try {
                val insight = api.generateInsights()
                _uiState.value = InsightsUiState.Success(insight)
            } catch (e: Exception) {
                _uiState.value = InsightsUiState.Error(e.message ?: "Failed to generate insights")
            }
        }
    }
}
