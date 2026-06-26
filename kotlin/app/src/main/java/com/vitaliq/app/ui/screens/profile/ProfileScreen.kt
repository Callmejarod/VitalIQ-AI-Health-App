package com.vitaliq.app.ui.screens.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vitaliq.app.data.model.ProfileDto
import com.vitaliq.app.ui.components.PrimaryButton
import com.vitaliq.app.ui.components.ButtonVariant
import com.vitaliq.app.ui.components.SectionHeader
import com.vitaliq.app.ui.theme.VitalColors
import com.vitaliq.app.ui.theme.VitalSpacing
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = viewModel(factory = ProfileViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            viewModel.load()
        }
    }

    val filePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.importFile(context, it) }
    }

    LaunchedEffect(uiState) {
        val msg = (uiState as? ProfileUiState.Success)?.snackbarMessage
        msg?.let {
            scope.launch {
                snackbarHostState.showSnackbar(it)
                viewModel.dismissSnackbar()
            }
        }
    }

    Scaffold(
        containerColor = VitalColors.bg,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        when (val state = uiState) {
            is ProfileUiState.Loading -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = VitalColors.accent)
                }
            }

            is ProfileUiState.Error -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = VitalColors.error)
                        Spacer(Modifier.height(8.dp))
                        Text(state.message, color = VitalColors.error)
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = { viewModel.load() }) { Text("Retry") }
                    }
                }
            }

            is ProfileUiState.Success -> {
                ProfileContent(
                    state = state,
                    modifier = Modifier.padding(padding),
                    onSave = { viewModel.saveProfile(it) },
                    onImport = { filePicker.launch(arrayOf("application/json", "text/csv", "*/*")) }
                )
            }
        }
    }
}

@Composable
private fun ProfileContent(
    state: ProfileUiState.Success,
    modifier: Modifier = Modifier,
    onSave: (ProfileDto) -> Unit,
    onImport: () -> Unit
) {
    val profile = state.profile

    var name by remember(profile.name) { mutableStateOf(profile.name) }
    var age by remember(profile.age) { mutableStateOf(profile.age?.toString() ?: "") }
    var heightCm by remember(profile.heightCm) { mutableStateOf(profile.heightCm?.toString() ?: "") }
    var weightKg by remember(profile.weightKg) {
        mutableStateOf(profile.weightKg?.let { "%.1f".format(it * 2.20462f) } ?: "")
    }
    var fitnessGoal by remember(profile.fitnessGoal) { mutableStateOf(profile.fitnessGoal ?: "maintain") }
    var dailyStepGoal by remember(profile.dailyStepGoal) { mutableStateOf(profile.dailyStepGoal.toString()) }
    var dailyWaterGoal by remember(profile.dailyWaterGoalMl) { mutableStateOf(profile.dailyWaterGoalMl.toString()) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(VitalColors.bg)
            .verticalScroll(rememberScrollState())
            .padding(VitalSpacing.lg)
    ) {
        Text("Profile", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = VitalColors.textPrimary)
        Spacer(Modifier.height(4.dp))
        Text("Manage your health profile", fontSize = 13.sp, color = VitalColors.textMuted)
        Spacer(Modifier.height(VitalSpacing.xl))

        // Personal Info section
        SectionHeader(title = "Personal Info")
        Spacer(Modifier.height(VitalSpacing.md))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = VitalColors.card)
        ) {
            Column(modifier = Modifier.padding(VitalSpacing.lg)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(Modifier.height(VitalSpacing.md))
                Row {
                    OutlinedTextField(
                        value = age,
                        onValueChange = { age = it },
                        label = { Text("Age") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    Spacer(Modifier.width(VitalSpacing.sm))
                    OutlinedTextField(
                        value = heightCm,
                        onValueChange = { heightCm = it },
                        label = { Text("Height (cm)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
                Spacer(Modifier.height(VitalSpacing.md))
                OutlinedTextField(
                    value = weightKg,
                    onValueChange = { weightKg = it },
                    label = { Text("Weight (lbs)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        }

        Spacer(Modifier.height(VitalSpacing.xl))

        // Fitness Goal
        SectionHeader(title = "Fitness Goal")
        Spacer(Modifier.height(VitalSpacing.md))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = VitalColors.card)
        ) {
            Row(
                modifier = Modifier.padding(VitalSpacing.lg),
                horizontalArrangement = Arrangement.spacedBy(VitalSpacing.sm)
            ) {
                listOf("lose", "maintain", "gain", "endurance").forEach { goal ->
                    FilterChip(
                        selected = fitnessGoal == goal,
                        onClick = { fitnessGoal = goal },
                        label = { Text(goal.replaceFirstChar { it.uppercase() }, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = VitalColors.brand,
                            selectedLabelColor = androidx.compose.ui.graphics.Color.White
                        )
                    )
                }
            }
        }

        Spacer(Modifier.height(VitalSpacing.xl))

        // Goals
        SectionHeader(title = "Daily Goals")
        Spacer(Modifier.height(VitalSpacing.md))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = VitalColors.card)
        ) {
            Column(modifier = Modifier.padding(VitalSpacing.lg)) {
                OutlinedTextField(
                    value = dailyStepGoal,
                    onValueChange = { dailyStepGoal = it },
                    label = { Text("Daily Step Goal") },
                    leadingIcon = { Icon(Icons.AutoMirrored.Filled.DirectionsWalk, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(Modifier.height(VitalSpacing.md))
                OutlinedTextField(
                    value = dailyWaterGoal,
                    onValueChange = { dailyWaterGoal = it },
                    label = { Text("Daily Water Goal (ml)") },
                    leadingIcon = { Icon(Icons.Default.WaterDrop, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        }

        Spacer(Modifier.height(VitalSpacing.xl))

        PrimaryButton(
            text = "Save Profile",
            onClick = {
                onSave(
                    profile.copy(
                        name = name,
                        age = age.toIntOrNull(),
                        heightCm = heightCm.toFloatOrNull(),
                        weightKg = weightKg.toFloatOrNull()?.let { it * 0.453592f },
                        fitnessGoal = fitnessGoal,
                        dailyStepGoal = dailyStepGoal.toIntOrNull() ?: 8000,
                        dailyWaterGoalMl = dailyWaterGoal.toIntOrNull() ?: 2000
                    )
                )
            },
            loading = state.saving,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(VitalSpacing.md))

        PrimaryButton(
            text = if (state.importing) "Importing..." else "Import Data (JSON / CSV)",
            onClick = onImport,
            loading = state.importing,
            variant = ButtonVariant.Secondary,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(VitalSpacing.xxxl))
    }
}
