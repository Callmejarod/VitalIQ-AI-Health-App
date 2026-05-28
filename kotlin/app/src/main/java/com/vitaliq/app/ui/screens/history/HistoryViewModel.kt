package com.vitaliq.app.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vitaliq.app.data.api.RetrofitClient
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class HistoryData(
    val steps: List<Pair<String, Float>> = emptyList(),
    val weight: List<Pair<String, Float>> = emptyList(),
    val bpSys: List<Pair<String, Float>> = emptyList(),
    val bpDia: List<Pair<String, Float>> = emptyList(),
    val activityCounts: Map<String, Int> = emptyMap()
)

sealed class HistoryUiState {
    object Loading : HistoryUiState()
    data class Success(val data: HistoryData) : HistoryUiState()
    data class Error(val message: String) : HistoryUiState()
}

class HistoryViewModel : ViewModel() {

    private val api = RetrofitClient.apiService

    private val _uiState = MutableStateFlow<HistoryUiState>(HistoryUiState.Loading)
    val uiState: StateFlow<HistoryUiState> = _uiState

    fun load() {
        viewModelScope.launch {
            _uiState.value = HistoryUiState.Loading
            try {
                val workoutsDeferred = async { api.listWorkouts() }
                val weightDeferred = async { api.listHealthEntries("weight") }
                val bpDeferred = async { api.listHealthEntries("bp") }

                val workouts = workoutsDeferred.await()
                val weightEntries = weightDeferred.await()
                val bpEntries = bpDeferred.await()

                // Steps — last 14 workout sessions
                val steps = workouts
                    .takeLast(14)
                    .mapIndexed { i, w ->
                        val label = w.startedAt.take(10).substring(5) // MM-DD
                        Pair(label, w.steps.toFloat())
                    }

                // Weight — last 14 entries
                val weight = weightEntries
                    .takeLast(14)
                    .mapNotNull { entry ->
                        val kg = (entry.value["weight_kg"] as? Number)?.toFloat()
                        val label = entry.loggedAt?.take(10)?.substring(5) ?: ""
                        if (kg != null) Pair(label, kg * 2.20462f) else null
                    }

                // BP — last 14 entries, split sys/dia
                val bpSys = bpEntries
                    .takeLast(14)
                    .mapNotNull { entry ->
                        val sys = (entry.value["sys"] as? Number)?.toFloat()
                        val label = entry.loggedAt?.take(10)?.substring(5) ?: ""
                        if (sys != null) Pair(label, sys) else null
                    }
                val bpDia = bpEntries
                    .takeLast(14)
                    .mapNotNull { entry ->
                        val dia = (entry.value["dia"] as? Number)?.toFloat()
                        val label = entry.loggedAt?.take(10)?.substring(5) ?: ""
                        if (dia != null) Pair(label, dia) else null
                    }

                // Activity — count occurrences per type
                val activityCounts = mutableMapOf("stationary" to 0, "walking" to 0, "mixed" to 0, "running" to 0)
                workouts.forEach { w ->
                    activityCounts[w.activityType] = (activityCounts[w.activityType] ?: 0) + 1
                }

                _uiState.value = HistoryUiState.Success(
                    HistoryData(
                        steps = steps,
                        weight = weight,
                        bpSys = bpSys,
                        bpDia = bpDia,
                        activityCounts = activityCounts
                    )
                )
            } catch (e: Exception) {
                _uiState.value = HistoryUiState.Error(e.message ?: "Failed to load history")
            }
        }
    }
}
