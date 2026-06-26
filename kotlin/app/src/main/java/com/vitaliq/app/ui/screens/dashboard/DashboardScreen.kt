package com.vitaliq.app.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vitaliq.app.ui.components.HealthRing
import com.vitaliq.app.ui.components.SectionHeader
import com.vitaliq.app.ui.components.StatCard
import com.vitaliq.app.ui.theme.VitalColors
import com.vitaliq.app.ui.theme.VitalSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToWorkout: () -> Unit,
    onNavigateToLog: () -> Unit,
    onNavigateToInsights: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToHistory: () -> Unit,
    viewModel: DashboardViewModel = viewModel(factory = DashboardViewModel.factory(LocalContext.current))
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    var isRefreshing by remember { mutableStateOf(false) }

    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            viewModel.load()
        }
    }

    LaunchedEffect(uiState) {
        if (uiState !is DashboardUiState.Loading) isRefreshing = false
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = {
            isRefreshing = true
            viewModel.load()
        }
    ) {
        when (val state = uiState) {
            is DashboardUiState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = VitalColors.accent)
                }
            }

            is DashboardUiState.Error -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = VitalColors.error)
                        Spacer(Modifier.height(8.dp))
                        Text(state.message, color = VitalColors.error)
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = { viewModel.load() }) { Text("Retry") }
                    }
                }
            }

            is DashboardUiState.Success -> {
                val summary = state.summary
                val profile = state.profile

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(VitalColors.bg)
                        .verticalScroll(rememberScrollState())
                        .padding(VitalSpacing.lg)
                ) {
                    // Top bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                "Good morning",
                                fontSize = 13.sp,
                                color = VitalColors.textMuted
                            )
                            Text(
                                profile.name.ifBlank { "Friend" },
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = VitalColors.textPrimary
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(VitalColors.accentSoft)
                                .clickable { onNavigateToProfile() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.AccountCircle,
                                contentDescription = "Profile",
                                tint = VitalColors.brand,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(VitalSpacing.xl))

                    // Health Ring section
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = VitalColors.card),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(VitalSpacing.xxl),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "Health Score",
                                fontSize = 13.sp,
                                color = VitalColors.textMuted,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(Modifier.height(VitalSpacing.lg))
                            Box(contentAlignment = Alignment.Center) {
                                HealthRing(score = summary.healthScore, size = 160.dp)
                                if (summary.healthScore == 0) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.padding(top = 48.dp)
                                    ) {
                                        Spacer(Modifier.height(32.dp))
                                        TextButton(onClick = onNavigateToInsights) {
                                            Text(
                                                "Get Insights",
                                                color = VitalColors.accent,
                                                fontSize = 12.sp
                                            )
                                        }
                                    }
                                }
                            }
                            Spacer(Modifier.height(VitalSpacing.sm))
                            Text(
                                "out of 100",
                                fontSize = 12.sp,
                                color = VitalColors.textMuted
                            )
                        }
                    }

                    Spacer(Modifier.height(VitalSpacing.xl))

                    // Stats section
                    SectionHeader(
                        title = "Today's Stats",
                        action = {
                            TextButton(onClick = onNavigateToHistory) {
                                Text("View History", color = VitalColors.accent, fontSize = 13.sp)
                            }
                        }
                    )
                    Spacer(Modifier.height(VitalSpacing.md))

                    val columns = 2
                    val stats = listOf(
                        Triple(Icons.Default.DirectionsWalk, "Steps", "${summary.stepsToday}"),
                        Triple(Icons.Default.WaterDrop, "Water", "${summary.waterMlToday} ml"),
                        Triple(Icons.Default.Bedtime, "Sleep", "${summary.lastSleepHours ?: "--"} hrs"),
                        Triple(Icons.Default.Favorite, "Heart Rate", "${summary.lastHr ?: "--"} bpm"),
                        Triple(Icons.Default.FitnessCenter, "Workouts", "${summary.workoutsToday}"),
                        Triple(Icons.Default.Timer, "Active Min", "${summary.workoutMinutesToday} min")
                    )

                    for (row in stats.chunked(columns)) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            row.forEachIndexed { idx, (icon, label, value) ->
                                StatCard(
                                    icon = icon,
                                    label = label,
                                    value = value,
                                    modifier = Modifier
                                        .weight(1f)
                                        .then(if (idx < row.size - 1) Modifier.padding(end = VitalSpacing.sm) else Modifier)
                                )
                            }
                            if (row.size < columns) {
                                Spacer(Modifier.weight(1f))
                            }
                        }
                        Spacer(Modifier.height(VitalSpacing.sm))
                    }

                    Spacer(Modifier.height(VitalSpacing.xl))

                    // Quick Actions
                    SectionHeader(title = "Quick Actions")
                    Spacer(Modifier.height(VitalSpacing.md))

                    val quickActions = listOf(
                        Triple(Icons.Default.FitnessCenter, "Start Workout", onNavigateToWorkout),
                        Triple(Icons.Default.AddCircle, "Log Health", onNavigateToLog),
                        Triple(Icons.Default.AutoAwesome, "AI Insights", onNavigateToInsights)
                    )

                    Row(modifier = Modifier.fillMaxWidth()) {
                        quickActions.forEachIndexed { idx, (icon, label, action) ->
                            QuickActionCard(
                                icon = icon,
                                label = label,
                                onClick = action,
                                modifier = Modifier
                                    .weight(1f)
                                    .then(if (idx < quickActions.size - 1) Modifier.padding(end = VitalSpacing.sm) else Modifier)
                            )
                        }
                    }

                    Spacer(Modifier.height(VitalSpacing.xxxl))
                }
            }
        }
    }
}

@Composable
private fun QuickActionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(80.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = VitalColors.accentSoft),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(VitalSpacing.sm),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = label, tint = VitalColors.brand, modifier = Modifier.size(22.dp))
            Spacer(Modifier.height(4.dp))
            Text(label, fontSize = 11.sp, color = VitalColors.brand, fontWeight = FontWeight.Medium)
        }
    }
}
