package com.vitaliq.app.ui.screens.log

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vitaliq.app.data.api.RetrofitClient
import com.vitaliq.app.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class LogUiState(
    val expandedCard: String? = null,
    val submitting: Boolean = false,
    val snackbarMessage: String? = null
)

class LogViewModel : ViewModel() {

    private val api = RetrofitClient.apiService

    private val _uiState = MutableStateFlow(LogUiState())
    val uiState: StateFlow<LogUiState> = _uiState

    fun expandCard(type: String) {
        _uiState.value = _uiState.value.copy(expandedCard = if (_uiState.value.expandedCard == type) null else type)
    }

    fun dismissSnackbar() {
        _uiState.value = _uiState.value.copy(snackbarMessage = null)
    }

    fun logWeight(weightKg: Float) {
        submit {
            api.createHealthEntry(
                HealthEntryDto(
                    entryType = "weight",
                    value = mapOf("weight_kg" to weightKg)
                )
            )
        }
    }

    fun logBloodPressure(sys: Int, dia: Int) {
        submit {
            api.createHealthEntry(
                HealthEntryDto(
                    entryType = "bp",
                    value = mapOf("sys" to sys, "dia" to dia)
                )
            )
        }
    }

    fun logHeartRate(bpm: Int) {
        submit {
            api.createHealthEntry(
                HealthEntryDto(
                    entryType = "hr",
                    value = mapOf("hr" to bpm)
                )
            )
        }
    }

    fun logMedication(name: String, dosage: String, frequency: String) {
        submit {
            api.createMedication(MedicationDto(name = name, dosage = dosage, frequency = frequency))
        }
    }

    fun logMeal(name: String, calories: Int, proteinG: Float, carbsG: Float, fatsG: Float) {
        submit {
            api.createNutrition(
                NutritionDto(
                    kind = "meal",
                    mealName = name,
                    calories = calories,
                    proteinG = proteinG,
                    carbsG = carbsG,
                    fatsG = fatsG
                )
            )
        }
    }

    fun logWater(waterMl: Int) {
        submit {
            api.createNutrition(NutritionDto(kind = "water", waterMl = waterMl))
        }
    }

    fun logSleep(hours: Float, quality: Int?) {
        submit {
            api.createSleep(SleepDto(hours = hours, quality = quality))
        }
    }

    private fun submit(block: suspend () -> Any) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(submitting = true)
            try {
                block()
                _uiState.value = _uiState.value.copy(
                    submitting = false,
                    expandedCard = null,
                    snackbarMessage = "Logged successfully!"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    submitting = false,
                    snackbarMessage = "Error: ${e.message}"
                )
            }
        }
    }
}
