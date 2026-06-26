package com.vitaliq.app.ui.screens.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vitaliq.app.ui.components.DualLineChart
import com.vitaliq.app.ui.components.LineChart
import com.vitaliq.app.ui.theme.VitalColors
import com.vitaliq.app.ui.theme.VitalSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onBack: () -> Unit,
    viewModel: HistoryViewModel = viewModel(factory = HistoryViewModel.factory(LocalContext.current))
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableIntStateOf(0) }
    var isRefreshing by remember { mutableStateOf(false) }
    val tabs = listOf("Steps", "Weight", "Blood Pressure", "Activity")

    LaunchedEffect(Unit) {
        viewModel.load()
    }

    LaunchedEffect(uiState) {
        if (uiState !is HistoryUiState.Loading) isRefreshing = false
    }

    Scaffold(
        containerColor = VitalColors.bg,
        topBar = {
            TopAppBar(
                title = { Text("History", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = VitalColors.bg)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            // Tab row
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = VitalColors.bg,
                contentColor = VitalColors.brand,
                edgePadding = VitalSpacing.lg,
                divider = {}
            ) {
                tabs.forEachIndexed { idx, title ->
                    Tab(
                        selected = selectedTab == idx,
                        onClick = { selectedTab = idx },
                        text = {
                            Text(
                                title,
                                fontSize = 13.sp,
                                fontWeight = if (selectedTab == idx) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = {
                    isRefreshing = true
                    viewModel.load()
                }
            ) {
                when (val state = uiState) {
                    is HistoryUiState.Loading -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = VitalColors.accent)
                        }
                    }

                    is HistoryUiState.Error -> {
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

                    is HistoryUiState.Success -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(VitalSpacing.lg)
                        ) {
                            when (selectedTab) {
                                0 -> StepsTab(state.data.steps)
                                1 -> WeightTab(state.data.weight)
                                2 -> BpTab(state.data.bpSys, state.data.bpDia)
                                3 -> ActivityTab(state.data.activityCounts)
                            }
                            Spacer(Modifier.height(VitalSpacing.xxxl))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StepsTab(steps: List<Pair<String, Float>>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = VitalColors.card)
    ) {
        Column(modifier = Modifier.padding(VitalSpacing.lg)) {
            Text("Steps per Workout (last 14)", fontWeight = FontWeight.SemiBold, color = VitalColors.textPrimary)
            Spacer(Modifier.height(VitalSpacing.md))
            if (steps.isEmpty()) {
                EmptyState("No workout data available")
            } else {
                LineChart(points = steps, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun WeightTab(weight: List<Pair<String, Float>>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = VitalColors.card)
    ) {
        Column(modifier = Modifier.padding(VitalSpacing.lg)) {
            Text("Weight Over Time (lbs)", fontWeight = FontWeight.SemiBold, color = VitalColors.textPrimary)
            Spacer(Modifier.height(VitalSpacing.md))
            if (weight.isEmpty()) {
                EmptyState("No weight data available")
            } else {
                LineChart(points = weight, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun BpTab(
    bpSys: List<Pair<String, Float>>,
    bpDia: List<Pair<String, Float>>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = VitalColors.card)
    ) {
        Column(modifier = Modifier.padding(VitalSpacing.lg)) {
            Text("Blood Pressure (mmHg)", fontWeight = FontWeight.SemiBold, color = VitalColors.textPrimary)
            Spacer(Modifier.height(4.dp))
            // Legend
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                LegendItem(color = VitalColors.accent, label = "Systolic")
                LegendItem(color = Color(0xFFFFB347), label = "Diastolic")
            }
            Spacer(Modifier.height(VitalSpacing.md))
            if (bpSys.isEmpty()) {
                EmptyState("No blood pressure data available")
            } else {
                DualLineChart(
                    series1 = bpSys,
                    series2 = bpDia,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun ActivityTab(activityCounts: Map<String, Int>) {
    val total = activityCounts.values.sum().coerceAtLeast(1)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = VitalColors.card)
    ) {
        Column(modifier = Modifier.padding(VitalSpacing.lg)) {
            Text("Activity Type Distribution", fontWeight = FontWeight.SemiBold, color = VitalColors.textPrimary)
            Spacer(Modifier.height(VitalSpacing.lg))
            if (total == 1 && activityCounts.values.sum() == 0) {
                EmptyState("No activity data available")
            } else {
                listOf("stationary", "walking", "mixed", "running").forEach { type ->
                    val count = activityCounts[type] ?: 0
                    val fraction = count.toFloat() / total
                    val color = when (type) {
                        "stationary" -> VitalColors.textMuted
                        "walking" -> VitalColors.accent
                        "mixed" -> VitalColors.warning
                        "running" -> VitalColors.error
                        else -> VitalColors.accent
                    }
                    Column(modifier = Modifier.padding(bottom = VitalSpacing.md)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                type.replaceFirstChar { it.uppercase() },
                                fontSize = 14.sp,
                                color = VitalColors.textPrimary
                            )
                            Text(
                                "$count sessions",
                                fontSize = 13.sp,
                                color = VitalColors.textMuted
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { fraction },
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                            color = color,
                            trackColor = VitalColors.bgSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(color))
        Spacer(Modifier.width(4.dp))
        Text(label, fontSize = 11.sp, color = VitalColors.textMuted)
    }
}

@Composable
private fun EmptyState(message: String) {
    Box(
        modifier = Modifier.fillMaxWidth().height(120.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(message, fontSize = 14.sp, color = VitalColors.textMuted, textAlign = TextAlign.Center)
    }
}
