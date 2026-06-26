package com.vitaliq.app.ui.screens.workout

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.vitaliq.app.BuildConfig
import com.vitaliq.app.data.model.WorkoutDto
import com.vitaliq.app.data.repository.WorkoutRepository
import com.vitaliq.app.di.ServiceLocator
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.format.DateTimeFormatter
import kotlin.math.abs
import kotlin.math.sqrt

sealed class WorkoutUiState {
    object Idle : WorkoutUiState()
    data class Active(
        val elapsedSeconds: Int,
        val steps: Int,
        val currentActivityType: String,
        val activityBreakdown: Map<String, Int>,
        val avgIntensity: Float,
        val stepCounterAvailable: Boolean
    ) : WorkoutUiState()
    object Submitting : WorkoutUiState()
    data class Summary(val workout: WorkoutDto) : WorkoutUiState()
    data class Error(val message: String) : WorkoutUiState()
}

class WorkoutViewModel(
    private val repo: WorkoutRepository
) : ViewModel(), SensorEventListener {

    private val _uiState = MutableStateFlow<WorkoutUiState>(WorkoutUiState.Idle)
    val uiState: StateFlow<WorkoutUiState> = _uiState

    private val WINDOW_SIZE = 40
    private val magnitudes = ArrayDeque<Float>(WINDOW_SIZE)

    private var elapsedSeconds = 0
    private var steps = 0
    private var baseStepCount: Int? = null
    private val activityBreakdown = mutableMapOf(
        "stationary" to 0,
        "walking" to 0,
        "mixed" to 0,
        "running" to 0
    )
    private var currentActivityType = "stationary"
    private var intensitySum = 0f
    private var intensityCount = 0
    private var stepCounterAvailable = false

    private var startedAt: String = ""
    private var timerJob: Job? = null
    private var sensorManager: SensorManager? = null

    init {
        Log.d("VitalIQ", "WorkoutViewModel created")
    }

    fun startWorkout(context: Context) {
        startedAt = DateTimeFormatter.ISO_INSTANT.format(Instant.now())
        elapsedSeconds = 0
        steps = 0
        baseStepCount = null
        magnitudes.clear()
        activityBreakdown.keys.forEach { activityBreakdown[it] = 0 }
        currentActivityType = "stationary"
        intensitySum = 0f
        intensityCount = 0

        registerSensors(context)

        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000L)
                elapsedSeconds++
                if (BuildConfig.SIMULATE_SENSORS) simulateTick()
                emitActiveState()
            }
        }

        emitActiveState()
    }

    // Demo mode: produce a believable workout each second without real sensors.
    // Cycles walking -> mixed -> running so steps climb and activity varies.
    private fun simulateTick() {
        val (type, stepsPerSec, intensity) = when ((elapsedSeconds / 15) % 4) {
            0 -> Triple("walking", 2, 0.25f)
            1 -> Triple("mixed", 3, 0.6f)
            2 -> Triple("running", 4, 1.3f)
            else -> Triple("walking", 2, 0.3f)
        }
        steps += stepsPerSec + (0..1).random()
        currentActivityType = type
        activityBreakdown[type] = (activityBreakdown[type] ?: 0) + 1
        intensitySum += intensity
        intensityCount++
    }

    fun stopWorkout() {
        timerJob?.cancel()
        timerJob = null
        sensorManager?.unregisterListener(this)
        sensorManager = null

        val endedAt = DateTimeFormatter.ISO_INSTANT.format(Instant.now())
        val dominant = activityBreakdown.maxByOrNull { it.value }?.key ?: "stationary"
        val avgIntensity = if (intensityCount > 0) intensitySum / intensityCount else 0f

        val dto = WorkoutDto(
            startedAt = startedAt,
            endedAt = endedAt,
            durationSeconds = elapsedSeconds,
            activityType = dominant,
            steps = steps,
            avgIntensity = avgIntensity,
            activityBreakdown = activityBreakdown.toMap()
        )

        viewModelScope.launch {
            _uiState.value = WorkoutUiState.Submitting
            try {
                val result = repo.createWorkout(dto)
                _uiState.value = WorkoutUiState.Summary(result)
            } catch (e: Exception) {
                _uiState.value = WorkoutUiState.Error(e.message ?: "Failed to save workout")
            }
        }
    }

    fun dismissSummary() {
        _uiState.value = WorkoutUiState.Idle
    }

    private fun emitActiveState() {
        val avgIntensity = if (intensityCount > 0) intensitySum / intensityCount else 0f
        _uiState.value = WorkoutUiState.Active(
            elapsedSeconds = elapsedSeconds,
            steps = steps,
            currentActivityType = currentActivityType,
            activityBreakdown = activityBreakdown.toMap(),
            avgIntensity = avgIntensity,
            stepCounterAvailable = stepCounterAvailable
        )
    }

    private fun registerSensors(context: Context) {
        if (BuildConfig.SIMULATE_SENSORS) {
            stepCounterAvailable = true
            return
        }

        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

        sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.let { sensor ->
            sensorManager?.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME)
        }

        val stepSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        stepCounterAvailable = stepSensor != null
        if (stepSensor != null) {
            sensorManager?.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]
                val mag = abs(sqrt(x * x + y * y + z * z) / SensorManager.GRAVITY_EARTH - 1f)

                if (magnitudes.size >= WINDOW_SIZE) magnitudes.removeFirst()
                magnitudes.addLast(mag)

                val avg = magnitudes.average().toFloat()
                currentActivityType = when {
                    avg < 0.08f -> "stationary"
                    avg < 0.35f -> "walking"
                    avg < 1.0f -> "mixed"
                    else -> "running"
                }
                activityBreakdown[currentActivityType] = (activityBreakdown[currentActivityType] ?: 0) + 1
                intensitySum += avg
                intensityCount++
            }

            Sensor.TYPE_STEP_COUNTER -> {
                val rawCount = event.values[0].toInt()
                if (baseStepCount == null) {
                    baseStepCount = rawCount
                }
                steps = rawCount - (baseStepCount ?: rawCount)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        sensorManager?.unregisterListener(this)
        Log.d("VitalIQ", "WorkoutViewModel cleared")
    }

    companion object {
        // Data wiring is in ServiceLocator; the Context this ViewModel uses is
        // only for SensorManager (passed to startWorkout), never for data access.
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                WorkoutViewModel(ServiceLocator.workoutRepository)
            }
        }
    }
}
