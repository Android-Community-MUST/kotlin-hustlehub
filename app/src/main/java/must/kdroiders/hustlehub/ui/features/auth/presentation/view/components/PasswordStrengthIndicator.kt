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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import must.kdroiders.hustlehub.R
import must.kdroiders.hustlehub.ui.features.auth.presentation.viewmodel.PasswordStrength
import must.kdroiders.hustlehub.ui.theme.HustleActiveGreen
import must.kdroiders.hustlehub.ui.theme.HustleTertiaryTeal
import must.kdroiders.hustlehub.ui.theme.HustleWarningAmber

@Composable
fun PasswordStrengthIndicator(strength: PasswordStrength) {
    val strengthText = when (strength) {
        PasswordStrength.NONE -> stringResource(R.string.auth_strength_very_weak)
        PasswordStrength.WEAK -> stringResource(R.string.auth_strength_weak)
        PasswordStrength.MEDIUM -> stringResource(R.string.auth_strength_medium)
        PasswordStrength.STRONG -> stringResource(R.string.auth_strength_strong)
        PasswordStrength.VERY_STRONG -> stringResource(R.string.auth_strength_very_strong)
    }

    val errorColor = MaterialTheme.colorScheme.error
    val warningColor = HustleWarningAmber
    val infoColor = HustleTertiaryTeal
    val successColor = HustleActiveGreen
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

    val cd = stringResource(R.string.cd_password_strength_format, strengthText)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 8.dp)
            .semantics(mergeDescendants = true) {
                contentDescription = cd
            },
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.auth_password_strength_label),
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
