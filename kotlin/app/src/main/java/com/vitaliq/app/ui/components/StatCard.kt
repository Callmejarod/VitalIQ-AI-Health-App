package com.vitaliq.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vitaliq.app.ui.theme.VitalColors
import com.vitaliq.app.ui.theme.VitalRadius
import com.vitaliq.app.ui.theme.VitalSpacing

@Composable
fun StatCard(
    icon: ImageVector,
    label: String,
    value: String,
    caption: String? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(VitalRadius.md))
            .background(VitalColors.card)
            .padding(VitalSpacing.lg)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(VitalRadius.sm))
                .background(VitalColors.accentSoft),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = VitalColors.accent,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(Modifier.height(VitalSpacing.sm))
        Text(
            text = label,
            fontSize = 11.sp,
            color = VitalColors.textMuted,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.5.sp
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = VitalColors.textPrimary
        )
        if (caption != null) {
            Spacer(Modifier.height(2.dp))
            Text(
                text = caption,
                fontSize = 11.sp,
                color = VitalColors.textMuted
            )
        }
    }
}
