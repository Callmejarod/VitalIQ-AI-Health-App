package com.vitaliq.app.ui.screens.insights

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.style.TextAlign
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
import com.vitaliq.app.ui.theme.VitalColors
import com.vitaliq.app.ui.theme.VitalSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(
    viewModel: InsightsViewModel = viewModel(factory = InsightsViewModel.factory(LocalContext.current))
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
        if (uiState !is InsightsUiState.Loading) isRefreshing = false
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = {
            isRefreshing = true
            viewModel.load()
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(VitalColors.bg)
                .verticalScroll(rememberScrollState())
                .padding(VitalSpacing.lg)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("AI Insights", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = VitalColors.textPrimary)
                val generating = (uiState as? InsightsUiState.Success)?.generating == true
                Button(
                    onClick = { viewModel.generateInsights() },
                    enabled = !generating && uiState !is InsightsUiState.Loading,
                    colors = ButtonDefaults.buttonColors(containerColor = VitalColors.brand),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (generating) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                        Spacer(Modifier.width(6.dp))
                    } else {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                    }
                    Text(if (generating) "Generating..." else "Generate", fontSize = 13.sp)
                }
            }
            Spacer(Modifier.height(VitalSpacing.xl))

            when (val state = uiState) {
                is InsightsUiState.Loading -> {
                    InsightsSkeleton()
                }

                is InsightsUiState.Empty -> {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(300.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = VitalColors.textMuted, modifier = Modifier.size(56.dp))
                            Spacer(Modifier.height(12.dp))
                            Text("No insights yet", fontWeight = FontWeight.SemiBold, fontSize = 18.sp, color = VitalColors.textPrimary)
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Tap Generate to analyze your health data with AI",
                                fontSize = 14.sp,
                                color = VitalColors.textMuted,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 24.dp)
                            )
                        }
                    }
                }

                is InsightsUiState.Error -> {
                    Box(Modifier.fillMaxWidth().height(240.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = VitalColors.error)
                            Spacer(Modifier.height(8.dp))
                            Text(state.message, color = VitalColors.error, textAlign = TextAlign.Center)
                            Spacer(Modifier.height(12.dp))
                            Button(onClick = { viewModel.load() }) { Text("Retry") }
                        }
                    }
                }

                is InsightsUiState.Success -> {
                    val insight = state.insight

                    if (insight.usedFallback) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1))
                        ) {
                            Row(modifier = Modifier.padding(VitalSpacing.md), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = VitalColors.warning, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("AI unavailable — local estimate", fontSize = 12.sp, color = Color(0xFF795548))
                            }
                        }
                        Spacer(Modifier.height(VitalSpacing.md))
                    }

                    // Overall score ring
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = VitalColors.card),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(VitalSpacing.xxl),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Overall Health Score", fontSize = 13.sp, color = VitalColors.textMuted)
                            Spacer(Modifier.height(VitalSpacing.md))
                            HealthRing(score = insight.overallScore, size = 140.dp)
                            if (insight.trendSummary.isNotBlank()) {
                                Spacer(Modifier.height(VitalSpacing.md))
                                Text(
                                    insight.trendSummary,
                                    fontSize = 14.sp,
                                    color = VitalColors.textSecondary,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(VitalSpacing.xl))

                    // Category scores
                    if (insight.categoryScores.isNotEmpty()) {
                        SectionHeader(title = "Category Scores")
                        Spacer(Modifier.height(VitalSpacing.md))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = VitalColors.card)
                        ) {
                            Column(modifier = Modifier.padding(VitalSpacing.lg)) {
                                val categoryOrder = listOf(
                                    "cardiovascular", "activity", "nutrition",
                                    "hydration", "medication_adherence", "recovery"
                                )
                                categoryOrder.forEach { key ->
                                    insight.categoryScores[key]?.let { score ->
                                        CategoryScoreRow(
                                            label = key.replace("_", " ").replaceFirstChar { it.uppercase() },
                                            score = score
                                        )
                                        Spacer(Modifier.height(VitalSpacing.md))
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(VitalSpacing.xl))
                    }

                    // Snapshot
                    if (insight.snapshot.isNotEmpty()) {
                        SectionHeader(title = "Sensor Snapshot")
                        Spacer(Modifier.height(VitalSpacing.md))

                        // Sensor summary sub-section (nested object from sensor_summary key)
                        @Suppress("UNCHECKED_CAST")
                        val sensorSummary = insight.snapshot["sensor_summary"] as? Map<String, Any>
                        if (sensorSummary != null && sensorSummary.isNotEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = VitalColors.card)
                            ) {
                                Column(modifier = Modifier.padding(VitalSpacing.lg)) {
                                    Text(
                                        "Workout & Activity",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = VitalColors.textMuted,
                                        letterSpacing = 1.5.sp
                                    )
                                    Spacer(Modifier.height(10.dp))

                                    val workoutCount = (sensorSummary["workout_count"] as? Number)?.toInt() ?: 0
                                    val totalSteps = (sensorSummary["total_steps"] as? Number)?.toInt() ?: 0
                                    val totalMinutes = (sensorSummary["total_minutes"] as? Number)?.toInt() ?: 0
                                    val avgIntensity = (sensorSummary["avg_intensity"] as? Number)?.toFloat() ?: 0f
                                    val dominantActivity = (sensorSummary["dominant_activity"] as? String) ?: "—"

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceEvenly
                                    ) {
                                        SensorStatItem(label = "Workouts", value = workoutCount.toString())
                                        SensorStatItem(label = "Steps", value = totalSteps.toString())
                                        SensorStatItem(label = "Active Min", value = totalMinutes.toString())
                                    }
                                    Spacer(Modifier.height(10.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceEvenly
                                    ) {
                                        SensorStatItem(label = "Avg Intensity", value = "${(avgIntensity * 100).toInt()}%")
                                        SensorStatItem(label = "Dominant", value = dominantActivity.replaceFirstChar { it.uppercase() })
                                    }

                                    @Suppress("UNCHECKED_CAST")
                                    val breakdownPct = sensorSummary["activity_breakdown_pct"] as? Map<String, Any>
                                    if (breakdownPct != null && breakdownPct.isNotEmpty()) {
                                        Spacer(Modifier.height(14.dp))
                                        Text(
                                            "Activity Mix",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = VitalColors.textMuted,
                                            letterSpacing = 1.5.sp
                                        )
                                        Spacer(Modifier.height(8.dp))
                                        breakdownPct.entries.sortedByDescending { (it.value as? Number)?.toFloat() ?: 0f }.forEach { (activity, pct) ->
                                            val pctFloat = (pct as? Number)?.toFloat() ?: 0f
                                            Row(
                                                modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Text(
                                                    activity.replaceFirstChar { it.uppercase() },
                                                    fontSize = 12.sp,
                                                    color = VitalColors.textSecondary,
                                                    modifier = Modifier.width(90.dp)
                                                )
                                                LinearProgressIndicator(
                                                    progress = { pctFloat / 100f },
                                                    modifier = Modifier.weight(1f).height(6.dp).clip(RoundedCornerShape(3.dp)),
                                                    color = VitalColors.brand,
                                                    trackColor = VitalColors.bgSecondary
                                                )
                                                Text(
                                                    "${pctFloat.toInt()}%",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = VitalColors.textPrimary,
                                                    modifier = Modifier.width(36.dp),
                                                    textAlign = TextAlign.End
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            Spacer(Modifier.height(VitalSpacing.md))
                        }

                        // Remaining top-level snapshot fields (exclude sensor_summary nested object)
                        val topLevelEntries = insight.snapshot.entries.filter { (k, v) ->
                            k != "sensor_summary" && v !is Map<*, *>
                        }
                        if (topLevelEntries.isNotEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = VitalColors.card)
                            ) {
                                Column(modifier = Modifier.padding(VitalSpacing.lg)) {
                                    topLevelEntries.forEach { (k, v) ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                k.replace("_", " ").replaceFirstChar { it.uppercase() },
                                                fontSize = 13.sp,
                                                color = VitalColors.textSecondary
                                            )
                                            Text(v.toString(), fontSize = 13.sp, fontWeight = FontWeight.Medium, color = VitalColors.textPrimary)
                                        }
                                        HorizontalDivider(color = VitalColors.border, thickness = 0.5.dp)
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(VitalSpacing.xl))
                    }

                    // Suggestions
                    if (insight.suggestions.isNotEmpty()) {
                        SectionHeader(title = "Suggestions")
                        Spacer(Modifier.height(VitalSpacing.md))
                        insight.suggestions.sortedBy { it.priority }.forEach { suggestion ->
                            SuggestionCard(
                                title = suggestion.title,
                                detail = suggestion.detail,
                                priority = suggestion.priority,
                                category = suggestion.category
                            )
                            Spacer(Modifier.height(VitalSpacing.sm))
                        }
                    }

                    Spacer(Modifier.height(VitalSpacing.xxxl))
                }
            }
        }
    }
}

@Composable
private fun CategoryScoreRow(label: String, score: Int) {
    val color = when {
        score >= 75 -> VitalColors.success
        score >= 50 -> VitalColors.warning
        else -> VitalColors.error
    }
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, fontSize = 13.sp, color = VitalColors.textSecondary)
            Text("$score%", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = color)
        }
        Spacer(Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { score / 100f },
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = VitalColors.bgSecondary
        )
    }
}

@Composable
private fun SuggestionCard(
    title: String,
    detail: String,
    priority: Int,
    category: String
) {
    val priorityColor = when (priority) {
        1 -> VitalColors.error
        2 -> VitalColors.warning
        else -> VitalColors.accent
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = VitalColors.card),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(modifier = Modifier.padding(VitalSpacing.lg)) {
            Box(
                modifier = Modifier.size(8.dp).clip(RoundedCornerShape(4.dp)).background(priorityColor).padding(top = 6.dp)
            )
            Spacer(Modifier.width(10.dp))
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = VitalColors.textPrimary, modifier = Modifier.weight(1f))
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = VitalColors.accentSoft
                    ) {
                        Text(
                            category,
                            fontSize = 10.sp,
                            color = VitalColors.brand,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(detail, fontSize = 13.sp, color = VitalColors.textSecondary)
            }
        }
    }
}

@Composable
private fun SensorStatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = VitalColors.textPrimary)
        Text(label, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = VitalColors.textMuted, letterSpacing = 0.5.sp)
    }
}

@Composable
private fun InsightsSkeleton() {
    Column {
        repeat(3) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(VitalColors.bgSecondary)
            )
            Spacer(Modifier.height(12.dp))
        }
    }
}
