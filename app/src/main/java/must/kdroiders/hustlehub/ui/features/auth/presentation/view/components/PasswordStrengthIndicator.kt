package must.kdroiders.hustlehub.ui.features.auth.presentation.view.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import must.kdroiders.hustlehub.ui.features.auth.presentation.viewmodel.PasswordStrength

@Composable
fun PasswordStrengthIndicator(strength: PasswordStrength) {
    val strengthText = when (strength) {
        PasswordStrength.NONE -> "Very Weak"
        PasswordStrength.WEAK -> "Weak"
        PasswordStrength.MEDIUM -> "Medium"
        PasswordStrength.STRONG -> "Strong"
        PasswordStrength.VERY_STRONG -> "Very Strong"
    }

    val errorColor = MaterialTheme.colorScheme.error
    val warningColor = Color(0xFFFFA500) // Orange
    val infoColor = Color(0xFF03A9F4) // Light Blue
    val successColor = Color(0xFF4CAF50) // Green
    val primaryColor = MaterialTheme.colorScheme.primary

    val strengthColor = when (strength) {
        PasswordStrength.NONE -> errorColor
        PasswordStrength.WEAK -> warningColor
        PasswordStrength.MEDIUM -> infoColor
        PasswordStrength.STRONG -> successColor
        PasswordStrength.VERY_STRONG -> primaryColor
    }

    val targetFillPercentage = when (strength) {
        PasswordStrength.NONE -> 0.2f
        PasswordStrength.WEAK -> 0.4f
        PasswordStrength.MEDIUM -> 0.6f
        PasswordStrength.STRONG -> 0.8f
        PasswordStrength.VERY_STRONG -> 1f
    }

    // Animate the progress bar width smoothly
    val animatedFillPercentage by animateFloatAsState(
        targetValue = targetFillPercentage,
        animationSpec = tween(durationMillis = 300),
        label = "PasswordStrengthProgress",
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Password Strength",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Text(
                text = strengthText,
                style = MaterialTheme.typography.bodySmall,
                color = strengthColor,
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .padding(top = 4.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedFillPercentage)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = when (strength) {
                                PasswordStrength.NONE -> listOf(errorColor, errorColor)
                                PasswordStrength.WEAK -> listOf(errorColor, warningColor)
                                PasswordStrength.MEDIUM -> listOf(warningColor, infoColor)
                                PasswordStrength.STRONG -> listOf(infoColor, successColor)
                                PasswordStrength.VERY_STRONG -> listOf(successColor, primaryColor)
                            },
                        ),
                    ),
            )
        }
    }
}
