package com.vitaliq.app.ui.screens.insights

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.vitaliq.app.data.model.InsightDto
import com.vitaliq.app.data.repository.InsightsRepository
import com.vitaliq.app.di.ServiceLocator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class InsightsUiState {
    object Loading : InsightsUiState()
    object Empty : InsightsUiState()
    data class Success(val insight: InsightDto, val generating: Boolean = false) : InsightsUiState()
    data class Error(val message: String) : InsightsUiState()
}

class InsightsViewModel(
    private val repo: InsightsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<InsightsUiState>(InsightsUiState.Loading)
    val uiState: StateFlow<InsightsUiState> = _uiState

    init {
        Log.d("VitalIQ", "InsightsViewModel created")
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = InsightsUiState.Loading
            try {
                val insight = repo.latestInsight()
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
                val insight = repo.generateInsights()
                _uiState.value = InsightsUiState.Success(insight)
            } catch (e: Exception) {
                _uiState.value = InsightsUiState.Error(e.message ?: "Failed to generate insights")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        Log.d("VitalIQ", "InsightsViewModel cleared")
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                InsightsViewModel(ServiceLocator.insightsRepository)
            }
        }
    }
}
