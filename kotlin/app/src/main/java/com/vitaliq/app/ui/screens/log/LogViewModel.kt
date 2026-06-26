package com.vitaliq.app.ui.screens.log

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.vitaliq.app.data.api.RetrofitClient
import com.vitaliq.app.data.local.AppDatabase
import com.vitaliq.app.data.model.HealthEntryDto
import com.vitaliq.app.data.model.MedicationDto
import com.vitaliq.app.data.model.NutritionDto
import com.vitaliq.app.data.model.SleepDto
import com.vitaliq.app.data.repository.HealthRepository
import com.vitaliq.app.data.repository.HealthRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class LogUiState(
    val expandedCard: String? = null,
    val submitting: Boolean = false,
    val snackbarMessage: String? = null
)

class LogViewModel(
    private val healthRepo: HealthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LogUiState())
    val uiState: StateFlow<LogUiState> = _uiState

    init {
        Log.d("VitalIQ", "LogViewModel created")
    }

    fun expandCard(type: String) {
        _uiState.value = _uiState.value.copy(expandedCard = if (_uiState.value.expandedCard == type) null else type)
    }

    fun dismissSnackbar() {
        _uiState.value = _uiState.value.copy(snackbarMessage = null)
    }

    fun logWeight(weightKg: Float) {
        submit {
            healthRepo.createHealthEntry(
                HealthEntryDto(entryType = "weight", value = mapOf("weight_kg" to weightKg))
            )
        }
    }

    fun logBloodPressure(sys: Int, dia: Int) {
        submit {
            healthRepo.createHealthEntry(
                HealthEntryDto(entryType = "bp", value = mapOf("sys" to sys, "dia" to dia))
            )
        }
    }

    fun logHeartRate(bpm: Int) {
        submit {
            healthRepo.createHealthEntry(
                HealthEntryDto(entryType = "hr", value = mapOf("hr" to bpm))
            )
        }
    }

    fun logMedication(name: String, dosage: String, frequency: String) {
        submit {
            healthRepo.createMedication(MedicationDto(name = name, dosage = dosage, frequency = frequency))
        }
    }

    fun logMeal(name: String, calories: Int, proteinG: Float, carbsG: Float, fatsG: Float) {
        submit {
            healthRepo.createNutrition(
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
            healthRepo.createNutrition(NutritionDto(kind = "water", waterMl = waterMl))
        }
    }

    fun logSleep(hours: Float, quality: Int?) {
        submit {
            healthRepo.createSleep(SleepDto(hours = hours, quality = quality))
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

    override fun onCleared() {
        super.onCleared()
        Log.d("VitalIQ", "LogViewModel cleared")
    }

    companion object {
        fun factory(context: Context): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val db = AppDatabase.getInstance(context)
                LogViewModel(
                    HealthRepositoryImpl(RetrofitClient.apiService, db.healthEntryDao())
                )
            }
        }
    }
}
