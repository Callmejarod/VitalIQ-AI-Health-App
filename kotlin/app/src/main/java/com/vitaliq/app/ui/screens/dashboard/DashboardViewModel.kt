package com.vitaliq.app.ui.screens.dashboard

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.vitaliq.app.data.model.DashboardSummaryDto
import com.vitaliq.app.data.model.ProfileDto
import com.vitaliq.app.data.repository.DashboardRepository
import com.vitaliq.app.di.ServiceLocator
import kotlinx.coroutines.async
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class DashboardUiState {
    object Loading : DashboardUiState()
    data class Success(
        val summary: DashboardSummaryDto,
        val profile: ProfileDto
    ) : DashboardUiState()
    data class Error(val message: String) : DashboardUiState()
}

class DashboardViewModel(
    private val repo: DashboardRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState

    init {
        Log.d("VitalIQ", "DashboardViewModel created")
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = DashboardUiState.Loading
            try {
                supervisorScope {
                    val summaryDeferred = async { repo.getDashboardSummary() }
                    val profileDeferred = async { repo.getProfile() }
                    val summary = summaryDeferred.await()
                    val profile = profileDeferred.await()
                    _uiState.value = DashboardUiState.Success(summary, profile)
                }
            } catch (e: Exception) {
                _uiState.value = DashboardUiState.Error(e.message ?: "Failed to load dashboard")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        Log.d("VitalIQ", "DashboardViewModel cleared")
    }

    companion object {
        // Wiring lives in the composition root (ServiceLocator), not here.
        // This ViewModel only knows the DashboardRepository interface.
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                DashboardViewModel(ServiceLocator.dashboardRepository)
            }
        }
    }
}
