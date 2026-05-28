package com.vitaliq.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.vitaliq.app.ui.theme.VitalColors
import com.vitaliq.app.ui.theme.VitalRadius

enum class ButtonVariant { Primary, Secondary, Ghost }

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    loading: Boolean = false,
    enabled: Boolean = true,
    variant: ButtonVariant = ButtonVariant.Primary
) {
    val shape = RoundedCornerShape(VitalRadius.md)

    when (variant) {
        ButtonVariant.Primary -> Button(
            onClick = onClick,
            modifier = modifier.height(48.dp),
            enabled = enabled && !loading,
            shape = shape,
            colors = ButtonDefaults.buttonColors(
                containerColor = VitalColors.brand,
                contentColor = Color.White,
                disabledContainerColor = VitalColors.brand.copy(alpha = 0.5f)
            )
        ) {
            ButtonContent(text, loading)
        }

        ButtonVariant.Secondary -> OutlinedButton(
            onClick = onClick,
            modifier = modifier.height(48.dp),
            enabled = enabled && !loading,
            shape = shape,
            border = BorderStroke(1.5.dp, VitalColors.brand),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = VitalColors.brand
            )
        ) {
            ButtonContent(text, loading, contentColor = VitalColors.brand)
        }

        ButtonVariant.Ghost -> TextButton(
            onClick = onClick,
            modifier = modifier.height(48.dp),
            enabled = enabled && !loading,
            shape = shape,
            colors = ButtonDefaults.textButtonColors(
                contentColor = VitalColors.brand
            )
        ) {
            ButtonContent(text, loading, contentColor = VitalColors.brand)
        }
    }
}

@Composable
private fun ButtonContent(
    text: String,
    loading: Boolean,
    contentColor: Color = Color.White
) {
    if (loading) {
        CircularProgressIndicator(
            modifier = Modifier.size(20.dp),
            color = contentColor,
            strokeWidth = 2.dp
        )
        Spacer(Modifier.width(8.dp))
    }
    Text(text)
}
