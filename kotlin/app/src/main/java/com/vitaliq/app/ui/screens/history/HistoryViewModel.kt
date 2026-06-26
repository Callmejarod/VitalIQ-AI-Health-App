package com.vitaliq.app.ui.screens.history

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.vitaliq.app.data.repository.HealthRepository
import com.vitaliq.app.data.repository.WorkoutRepository
import com.vitaliq.app.di.ServiceLocator
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

class HistoryViewModel(
    private val workoutRepo: WorkoutRepository,
    private val healthRepo: HealthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<HistoryUiState>(HistoryUiState.Loading)
    val uiState: StateFlow<HistoryUiState> = _uiState

    init {
        Log.d("VitalIQ", "HistoryViewModel created")
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = HistoryUiState.Loading
            try {
                val workoutsDeferred = async { workoutRepo.listWorkouts() }
                val weightDeferred = async { healthRepo.listHealthEntries("weight") }
                val bpDeferred = async { healthRepo.listHealthEntries("bp") }

                val workouts = workoutsDeferred.await()
                val weightEntries = weightDeferred.await()
                val bpEntries = bpDeferred.await()

                val steps = workouts
                    .takeLast(14)
                    .mapIndexed { i, w ->
                        val label = w.startedAt.take(10).substring(5)
                        Pair(label, w.steps.toFloat())
                    }

                val weight = weightEntries
                    .takeLast(14)
                    .mapNotNull { entry ->
                        val kg = (entry.value["weight_kg"] as? Number)?.toFloat()
                        val label = entry.loggedAt?.take(10)?.substring(5) ?: ""
                        if (kg != null) Pair(label, kg * 2.20462f) else null
                    }

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

    override fun onCleared() {
        super.onCleared()
        Log.d("VitalIQ", "HistoryViewModel cleared")
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                HistoryViewModel(
                    ServiceLocator.workoutRepository,
                    ServiceLocator.healthRepository
                )
            }
        }
    }
}
