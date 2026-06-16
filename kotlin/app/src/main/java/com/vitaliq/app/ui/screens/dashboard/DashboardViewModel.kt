package com.vitaliq.app.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vitaliq.app.data.api.RetrofitClient
import com.vitaliq.app.data.model.DashboardSummaryDto
import com.vitaliq.app.data.model.ProfileDto
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

class DashboardViewModel : ViewModel() {

    private val api = RetrofitClient.apiService

    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState

    fun load() {
        viewModelScope.launch {
            _uiState.value = DashboardUiState.Loading
            try {
                supervisorScope {
                    val summaryDeferred = async { api.dashboardSummary() }
                    val profileDeferred = async { api.getProfile() }
                    val summary = summaryDeferred.await()
                    val profile = profileDeferred.await()
                    _uiState.value = DashboardUiState.Success(summary, profile)
                }
            } catch (e: Exception) {
                _uiState.value = DashboardUiState.Error(e.message ?: "Failed to load dashboard")
            }
        }
    }
}
