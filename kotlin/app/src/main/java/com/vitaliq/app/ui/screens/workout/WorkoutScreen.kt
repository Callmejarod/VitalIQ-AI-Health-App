package com.vitaliq.app.ui.screens.workout

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vitaliq.app.data.model.WorkoutDto
import com.vitaliq.app.ui.theme.VitalColors
import com.vitaliq.app.ui.theme.VitalSpacing

@Composable
fun WorkoutScreen(
    viewModel: WorkoutViewModel = viewModel(factory = WorkoutViewModel.factory(LocalContext.current))
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) viewModel.startWorkout(context)
    }

    val isActive = uiState is WorkoutUiState.Active
    val bgColor = if (isActive) VitalColors.workoutActive else VitalColors.bg

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        when (val state = uiState) {
            is WorkoutUiState.Idle -> {
                IdleWorkoutContent(
                    onStart = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            permissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
                        } else {
                            viewModel.startWorkout(context)
                        }
                    }
                )
            }

            is WorkoutUiState.Active -> {
                ActiveWorkoutContent(
                    state = state,
                    onStop = { viewModel.stopWorkout() }
                )
            }

            is WorkoutUiState.Submitting -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = VitalColors.accent)
                        Spacer(Modifier.height(12.dp))
                        Text("Saving workout...", color = Color.White)
                    }
                }
            }

            is WorkoutUiState.Summary -> {
                WorkoutSummaryDialog(
                    workout = state.workout,
                    onDismiss = { viewModel.dismissSummary() }
                )
                IdleWorkoutContent(onStart = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        permissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
                    } else {
                        viewModel.startWorkout(context)
                    }
                })
            }

            is WorkoutUiState.Error -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = VitalColors.error, modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(12.dp))
                        Text(state.message, color = VitalColors.error, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = { viewModel.dismissSummary() }) { Text("OK") }
                    }
                }
            }
        }
    }
}

@Composable
private fun IdleWorkoutContent(onStart: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(VitalSpacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(VitalColors.accentSoft),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.FitnessCenter,
                contentDescription = "Workout",
                tint = VitalColors.brand,
                modifier = Modifier.size(56.dp)
            )
        }
        Spacer(Modifier.height(VitalSpacing.xxl))
        Text("Ready to Work Out?", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = VitalColors.textPrimary)
        Spacer(Modifier.height(8.dp))
        Text(
            "Track your activity with live sensor data",
            fontSize = 14.sp,
            color = VitalColors.textMuted,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(VitalSpacing.xxxl))
        Button(
            onClick = onStart,
            modifier = Modifier.size(72.dp),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = VitalColors.accent)
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = "Start", modifier = Modifier.size(36.dp))
        }
        Spacer(Modifier.height(12.dp))
        Text("Tap to Start", fontSize = 13.sp, color = VitalColors.textMuted)
    }
}

@Composable
private fun ActiveWorkoutContent(
    state: WorkoutUiState.Active,
    onStop: () -> Unit
) {
    val elapsed = state.elapsedSeconds
    val hours = elapsed / 3600
    val minutes = (elapsed % 3600) / 60
    val seconds = elapsed % 60
    val timeStr = if (hours > 0)
        "%02d:%02d:%02d".format(hours, minutes, seconds)
    else
        "%02d:%02d".format(minutes, seconds)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(VitalSpacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(VitalSpacing.xxl))

        Text("WORKOUT ACTIVE", fontSize = 11.sp, color = Color(0xFF4CAF7D), letterSpacing = 2.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(VitalSpacing.lg))

        Text(timeStr, fontSize = 52.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Spacer(Modifier.height(4.dp))
        Text("Duration", fontSize = 12.sp, color = Color(0xFF8FA396))

        Spacer(Modifier.height(VitalSpacing.xxl))

        // Stats row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            WorkoutStatItem(
                value = "${state.steps}",
                label = "Steps",
                color = Color.White,
                warning = if (!state.stepCounterAvailable) "No sensor" else null
            )
            WorkoutStatItem(
                value = "${(state.avgIntensity * 100).toInt()}%",
                label = "Intensity",
                color = Color.White
            )
            WorkoutStatItem(
                value = state.currentActivityType.replaceFirstChar { it.uppercase() },
                label = "Activity",
                color = VitalColors.accent
            )
        }

        Spacer(Modifier.height(VitalSpacing.xxl))

        // Activity breakdown
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF243D2F))
        ) {
            Column(modifier = Modifier.padding(VitalSpacing.lg)) {
                Text("Activity Breakdown", fontSize = 13.sp, color = Color(0xFF8FA396), fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(VitalSpacing.md))
                val total = state.activityBreakdown.values.sum().coerceAtLeast(1)
                listOf("stationary", "walking", "mixed", "running").forEach { type ->
                    val count = state.activityBreakdown[type] ?: 0
                    val fraction = count.toFloat() / total
                    ActivityBreakdownRow(type = type, fraction = fraction)
                    Spacer(Modifier.height(8.dp))
                }
            }
        }

        Spacer(Modifier.height(VitalSpacing.xxxl))

        Button(
            onClick = onStop,
            modifier = Modifier.size(72.dp),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = VitalColors.error)
        ) {
            Icon(Icons.Default.Stop, contentDescription = "Stop", modifier = Modifier.size(36.dp))
        }
        Spacer(Modifier.height(12.dp))
        Text("Tap to Stop", fontSize = 13.sp, color = Color(0xFF8FA396))
        Spacer(Modifier.height(VitalSpacing.xxl))
    }
}

@Composable
private fun WorkoutStatItem(
    value: String,
    label: String,
    color: Color,
    warning: String? = null
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = color)
        Text(label, fontSize = 12.sp, color = Color(0xFF8FA396))
        if (warning != null) {
            Text(warning, fontSize = 10.sp, color = VitalColors.warning)
        }
    }
}

@Composable
private fun ActivityBreakdownRow(type: String, fraction: Float) {
    val color = when (type) {
        "stationary" -> Color(0xFF8FA396)
        "walking" -> VitalColors.accent
        "mixed" -> VitalColors.warning
        "running" -> Color(0xFFE57373)
        else -> VitalColors.accent
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            type.replaceFirstChar { it.uppercase() },
            modifier = Modifier.width(80.dp),
            fontSize = 12.sp,
            color = Color.White
        )
        Spacer(Modifier.width(8.dp))
        LinearProgressIndicator(
            progress = { fraction },
            modifier = Modifier.weight(1f).height(6.dp).clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = Color(0xFF1A2F24)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            "${(fraction * 100).toInt()}%",
            fontSize = 11.sp,
            color = Color(0xFF8FA396),
            modifier = Modifier.width(32.dp),
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun WorkoutSummaryDialog(
    workout: WorkoutDto,
    onDismiss: () -> Unit
) {
    val elapsed = workout.durationSeconds
    val minutes = elapsed / 60
    val seconds = elapsed % 60
    val total = workout.activityBreakdown.values.sum().coerceAtLeast(1)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Workout Complete!", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                    SummaryItem("Duration", "%dm %ds".format(minutes, seconds))
                    SummaryItem("Steps", "${workout.steps}")
                    SummaryItem("Intensity", "${(workout.avgIntensity * 100).toInt()}%")
                }
                Spacer(Modifier.height(16.dp))
                Text("Activity Breakdown", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Spacer(Modifier.height(8.dp))
                listOf("stationary", "walking", "mixed", "running").forEach { type ->
                    val count = workout.activityBreakdown[type] ?: 0
                    val fraction = count.toFloat() / total
                    val color = when (type) {
                        "stationary" -> VitalColors.textMuted
                        "walking" -> VitalColors.accent
                        "mixed" -> VitalColors.warning
                        "running" -> VitalColors.error
                        else -> VitalColors.accent
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            type.replaceFirstChar { it.uppercase() },
                            modifier = Modifier.width(80.dp),
                            fontSize = 13.sp
                        )
                        LinearProgressIndicator(
                            progress = { fraction },
                            modifier = Modifier.weight(1f).height(8.dp).clip(RoundedCornerShape(4.dp)),
                            color = color,
                            trackColor = VitalColors.bgSecondary
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("${(fraction * 100).toInt()}%", fontSize = 12.sp, color = VitalColors.textMuted)
                    }
                    Spacer(Modifier.height(6.dp))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Done", color = VitalColors.brand) }
        }
    )
}

@Composable
private fun SummaryItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = VitalColors.textPrimary)
        Text(label, fontSize = 12.sp, color = VitalColors.textMuted)
    }
}
