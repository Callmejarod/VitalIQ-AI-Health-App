package com.vitaliq.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

object VitalColors {
    val bg = Color(0xFFF8F9F6)
    val bgSecondary = Color(0xFFF0F2EE)
    val workoutActive = Color(0xFF1A2F24)
    val textPrimary = Color(0xFF1C2B22)
    val textSecondary = Color(0xFF4A5E52)
    val textMuted = Color(0xFF8FA396)
    val brand = Color(0xFF2C4C3B)
    val accent = Color(0xFF4CAF7D)
    val accentSoft = Color(0xFFE8F5EE)
    val success = Color(0xFF4CAF7D)
    val warning = Color(0xFFFFB347)
    val error = Color(0xFFE57373)
    val card = Color(0xFFFFFFFF)
    val border = Color(0xFFE0E5E1)
}

object VitalSpacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 20.dp
    val xxl = 24.dp
    val xxxl = 32.dp
}

object VitalRadius {
    val sm = 8.dp
    val md = 16.dp
    val lg = 24.dp
    val pill = 999.dp
}

private val VitalColorScheme = lightColorScheme(
    primary = VitalColors.brand,
    secondary = VitalColors.accent,
    background = VitalColors.bg,
    surface = VitalColors.card,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = VitalColors.textPrimary,
    onSurface = VitalColors.textPrimary,
    error = VitalColors.error,
    outline = VitalColors.border
)

@Composable
fun VitalIQTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = VitalColorScheme,
        typography = VitalTypography,
        content = content
    )
}
