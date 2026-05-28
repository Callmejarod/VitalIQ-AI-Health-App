package com.vitaliq.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vitaliq.app.ui.theme.VitalColors

@Composable
fun HealthRing(
    score: Int,
    size: Dp = 160.dp,
    strokeWidth: Dp = 12.dp,
    trackColor: Color = VitalColors.border,
    fillColor: Color = VitalColors.accent,
    textColor: Color = VitalColors.textPrimary
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(size)
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val strokePx = strokeWidth.toPx()
            val padding = strokePx / 2f
            val arcSize = Size(
                this.size.width - strokePx,
                this.size.height - strokePx
            )
            val topLeft = Offset(padding, padding)

            // Start angle: 135° (bottom-left), sweep max = 270°
            val startAngle = 135f
            val maxSweep = 270f
            val sweepAngle = (score.coerceIn(0, 100) / 100f) * maxSweep

            // Track arc
            drawArc(
                color = trackColor,
                startAngle = startAngle,
                sweepAngle = maxSweep,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )

            // Progress arc
            if (sweepAngle > 0f) {
                drawArc(
                    color = fillColor,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )
            }
        }

        Text(
            text = score.toString(),
            fontWeight = FontWeight.Bold,
            fontSize = (size.value * 0.22f).sp,
            color = textColor
        )
    }
}
