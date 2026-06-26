package com.vitaliq.app.ui.screens.log

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vitaliq.app.ui.components.PrimaryButton
import com.vitaliq.app.ui.theme.VitalColors
import com.vitaliq.app.ui.theme.VitalSpacing
import kotlinx.coroutines.launch

@Composable
fun LogScreen(
    viewModel: LogViewModel = viewModel(factory = LogViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { msg ->
            scope.launch {
                snackbarHostState.showSnackbar(msg)
                viewModel.dismissSnackbar()
            }
        }
    }

    Scaffold(
        containerColor = VitalColors.bg,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(VitalSpacing.lg)
                .verticalScroll(rememberScrollState())
        ) {
            Text("Log Health Data", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = VitalColors.textPrimary)
            Spacer(Modifier.height(4.dp))
            Text("Tap a card to expand and log", fontSize = 13.sp, color = VitalColors.textMuted)
            Spacer(Modifier.height(VitalSpacing.xl))

            val cards = listOf(
                LogCardDef("weight", Icons.Default.MonitorWeight, "Weight", "Track your body weight (lbs)"),
                LogCardDef("bp", Icons.Default.Favorite, "Blood Pressure", "Systolic & diastolic"),
                LogCardDef("hr", Icons.Default.FavoriteBorder, "Heart Rate", "Resting BPM"),
                LogCardDef("medication", Icons.Default.Medication, "Medication", "Name, dosage & frequency"),
                LogCardDef("meal", Icons.Default.Restaurant, "Meal", "Calories & macros"),
                LogCardDef("water", Icons.Default.WaterDrop, "Water", "Hydration intake"),
                LogCardDef("sleep", Icons.Default.Bedtime, "Sleep", "Hours & quality")
            )

            for (row in cards.chunked(2)) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    row.forEachIndexed { idx, card ->
                        LogCard(
                            def = card,
                            expanded = uiState.expandedCard == card.key,
                            submitting = uiState.submitting,
                            onToggle = { viewModel.expandCard(card.key) },
                            viewModel = viewModel,
                            modifier = Modifier
                                .weight(1f)
                                .then(if (idx < row.size - 1) Modifier.padding(end = VitalSpacing.sm) else Modifier)
                        )
                    }
                    if (row.size == 1) Spacer(Modifier.weight(1f))
                }
                Spacer(Modifier.height(VitalSpacing.sm))
            }

            Spacer(Modifier.height(VitalSpacing.xxxl))
        }
    }
}

data class LogCardDef(
    val key: String,
    val icon: ImageVector,
    val title: String,
    val subtitle: String
)

@Composable
private fun LogCard(
    def: LogCardDef,
    expanded: Boolean,
    submitting: Boolean,
    onToggle: () -> Unit,
    viewModel: LogViewModel,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (expanded) VitalColors.accentSoft else VitalColors.card
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggle)
                    .padding(VitalSpacing.lg),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (expanded) VitalColors.accent else VitalColors.bgSecondary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        def.icon,
                        contentDescription = def.title,
                        tint = if (expanded) VitalColors.card else VitalColors.brand,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(def.title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = VitalColors.textPrimary)
                    Text(def.subtitle, fontSize = 11.sp, color = VitalColors.textMuted)
                }
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Box(modifier = Modifier.padding(start = VitalSpacing.lg, end = VitalSpacing.lg, bottom = VitalSpacing.lg)) {
                    LogEntryForm(cardKey = def.key, submitting = submitting, viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
private fun LogEntryForm(
    cardKey: String,
    submitting: Boolean,
    viewModel: LogViewModel
) {
    when (cardKey) {
        "weight" -> WeightForm(submitting, viewModel)
        "bp" -> BloodPressureForm(submitting, viewModel)
        "hr" -> HeartRateForm(submitting, viewModel)
        "medication" -> MedicationForm(submitting, viewModel)
        "meal" -> MealForm(submitting, viewModel)
        "water" -> WaterForm(submitting, viewModel)
        "sleep" -> SleepForm(submitting, viewModel)
    }
}

@Composable
private fun WeightForm(submitting: Boolean, viewModel: LogViewModel) {
    var weight by remember { mutableStateOf("") }
    Column {
        OutlinedTextField(
            value = weight,
            onValueChange = { weight = it },
            label = { Text("Weight (lbs)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(Modifier.height(8.dp))
        PrimaryButton(
            text = "Log Weight",
            onClick = {
                weight.toFloatOrNull()?.let { lbs ->
                    val kg = lbs * 0.453592f
                    viewModel.logWeight(kg)
                }
            },
            loading = submitting,
            enabled = weight.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun BloodPressureForm(submitting: Boolean, viewModel: LogViewModel) {
    var sys by remember { mutableStateOf("") }
    var dia by remember { mutableStateOf("") }
    Column {
        Row {
            OutlinedTextField(
                value = sys,
                onValueChange = { sys = it },
                label = { Text("Systolic") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            Spacer(Modifier.width(8.dp))
            OutlinedTextField(
                value = dia,
                onValueChange = { dia = it },
                label = { Text("Diastolic") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                singleLine = true
            )
        }
        Spacer(Modifier.height(8.dp))
        PrimaryButton(
            text = "Log BP",
            onClick = {
                val s = sys.toIntOrNull()
                val d = dia.toIntOrNull()
                if (s != null && d != null) viewModel.logBloodPressure(s, d)
            },
            loading = submitting,
            enabled = sys.isNotBlank() && dia.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun HeartRateForm(submitting: Boolean, viewModel: LogViewModel) {
    var bpm by remember { mutableStateOf("") }
    Column {
        OutlinedTextField(
            value = bpm,
            onValueChange = { bpm = it },
            label = { Text("Heart Rate (bpm)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(Modifier.height(8.dp))
        PrimaryButton(
            text = "Log HR",
            onClick = { bpm.toIntOrNull()?.let { viewModel.logHeartRate(it) } },
            loading = submitting,
            enabled = bpm.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun MedicationForm(submitting: Boolean, viewModel: LogViewModel) {
    var name by remember { mutableStateOf("") }
    var dosage by remember { mutableStateOf("") }
    var frequency by remember { mutableStateOf("") }
    Column {
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Medication Name") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(value = dosage, onValueChange = { dosage = it }, label = { Text("Dosage (e.g. 10mg)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(value = frequency, onValueChange = { frequency = it }, label = { Text("Frequency (e.g. daily)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Spacer(Modifier.height(8.dp))
        PrimaryButton(
            text = "Log Medication",
            onClick = {
                if (name.isNotBlank() && dosage.isNotBlank() && frequency.isNotBlank())
                    viewModel.logMedication(name, dosage, frequency)
            },
            loading = submitting,
            enabled = name.isNotBlank() && dosage.isNotBlank() && frequency.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun MealForm(submitting: Boolean, viewModel: LogViewModel) {
    var mealName by remember { mutableStateOf("") }
    var calories by remember { mutableStateOf("") }
    var protein by remember { mutableStateOf("") }
    var carbs by remember { mutableStateOf("") }
    var fats by remember { mutableStateOf("") }
    Column {
        OutlinedTextField(value = mealName, onValueChange = { mealName = it }, label = { Text("Meal Name") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(value = calories, onValueChange = { calories = it }, label = { Text("Calories") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth(), singleLine = true)
        Spacer(Modifier.height(6.dp))
        Row {
            OutlinedTextField(value = protein, onValueChange = { protein = it }, label = { Text("Protein (g)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.weight(1f), singleLine = true)
            Spacer(Modifier.width(6.dp))
            OutlinedTextField(value = carbs, onValueChange = { carbs = it }, label = { Text("Carbs (g)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.weight(1f), singleLine = true)
            Spacer(Modifier.width(6.dp))
            OutlinedTextField(value = fats, onValueChange = { fats = it }, label = { Text("Fats (g)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.weight(1f), singleLine = true)
        }
        Spacer(Modifier.height(8.dp))
        PrimaryButton(
            text = "Log Meal",
            onClick = {
                viewModel.logMeal(
                    mealName,
                    calories.toIntOrNull() ?: 0,
                    protein.toFloatOrNull() ?: 0f,
                    carbs.toFloatOrNull() ?: 0f,
                    fats.toFloatOrNull() ?: 0f
                )
            },
            loading = submitting,
            enabled = mealName.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun WaterForm(submitting: Boolean, viewModel: LogViewModel) {
    var water by remember { mutableStateOf("") }
    Column {
        OutlinedTextField(
            value = water,
            onValueChange = { water = it },
            label = { Text("Water (ml)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf(250, 500, 750).forEach { amount ->
                OutlinedButton(
                    onClick = { water = amount.toString() },
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                ) {
                    Text("${amount}ml", fontSize = 12.sp)
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        PrimaryButton(
            text = "Log Water",
            onClick = { water.toIntOrNull()?.let { viewModel.logWater(it) } },
            loading = submitting,
            enabled = water.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun SleepForm(submitting: Boolean, viewModel: LogViewModel) {
    var hours by remember { mutableStateOf("") }
    var quality by remember { mutableStateOf<Int?>(null) }
    Column {
        OutlinedTextField(
            value = hours,
            onValueChange = { hours = it },
            label = { Text("Hours Slept") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(Modifier.height(8.dp))
        Text("Quality (optional)", fontSize = 12.sp, color = VitalColors.textMuted)
        Spacer(Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            (1..5).forEach { q ->
                FilterChip(
                    selected = quality == q,
                    onClick = { quality = if (quality == q) null else q },
                    label = { Text("$q") }
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        PrimaryButton(
            text = "Log Sleep",
            onClick = { hours.toFloatOrNull()?.let { viewModel.logSleep(it, quality) } },
            loading = submitting,
            enabled = hours.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        )
    }
}
