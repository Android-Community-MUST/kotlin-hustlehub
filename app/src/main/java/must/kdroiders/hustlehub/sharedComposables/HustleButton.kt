package must.kdroiders.hustlehub.sharedComposables

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import must.kdroiders.hustlehub.ui.theme.HustleHubTheme

enum class HustleButtonVariant {
    Primary,
    Secondary,
    Outlined
}

private val ButtonShape = RoundedCornerShape(10.dp)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HustleButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: HustleButtonVariant = HustleButtonVariant.Primary,
    enabled: Boolean = true,
    loading: Boolean = false,
    icon: ImageVector? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val motionScheme = MaterialTheme.motionScheme

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = motionScheme.fastSpatialSpec(),
        label = "scale"
    )

    val isActive = enabled && !loading

    when (variant) {
        HustleButtonVariant.Primary -> {
            Button(
                onClick = onClick,
                modifier = modifier
                    .height(52.dp)
                    .graphicsLayer { scaleX = scale; scaleY = scale },
                enabled = isActive,
                shape = ButtonShape,
                interactionSource = interactionSource,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 0.dp
                )
            ) {
                ButtonContent(text = text, loading = loading, icon = icon,
                    loadingColor = { MaterialTheme.colorScheme.onPrimary })
            }
        }

        HustleButtonVariant.Secondary -> {
            Button(
                onClick = onClick,
                modifier = modifier
                    .height(52.dp)
                    .graphicsLayer { scaleX = scale; scaleY = scale },
                enabled = isActive,
                shape = ButtonShape,
                interactionSource = interactionSource,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 0.dp
                )
            ) {
                ButtonContent(text = text, loading = loading, icon = icon,
                    loadingColor = { MaterialTheme.colorScheme.onSecondaryContainer })
            }
        }

        HustleButtonVariant.Outlined -> {
            OutlinedButton(
                onClick = onClick,
                modifier = modifier
                    .height(52.dp)
                    .graphicsLayer { scaleX = scale; scaleY = scale },
                enabled = isActive,
                shape = ButtonShape,
                interactionSource = interactionSource,
                border = BorderStroke(
                    width = 1.5.dp,
                    color = if (isActive) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary,
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                ButtonContent(text = text, loading = loading, icon = icon,
                    loadingColor = { MaterialTheme.colorScheme.primary })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ButtonContent(
    text: String,
    loading: Boolean,
    icon: ImageVector?,
    loadingColor: @Composable () -> androidx.compose.ui.graphics.Color
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (loading) {
            CircularWavyProgressIndicator(
                modifier = Modifier.size(18.dp),
                color = loadingColor(),
                trackColor = loadingColor().copy(alpha = 0.2f)
            )
            Spacer(Modifier.width(10.dp))
        } else if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
        }
        Text(
            text = text,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.3.sp,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F5F5)
@Composable
fun HustleButtonPreview() {
    HustleHubTheme {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            HustleButton(text = "Primary", onClick = {})
            HustleButton(text = "Secondary", onClick = {}, variant = HustleButtonVariant.Secondary)
            HustleButton(text = "Outlined", onClick = {}, variant = HustleButtonVariant.Outlined)
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F5F5)
@Composable
fun HustleButtonLoadingPreview() {
    HustleHubTheme {
        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            HustleButton(text = "Loading...", onClick = {}, loading = true)
            HustleButton(
                text = "With Icon",
                onClick = {},
                icon = Icons.Default.ArrowForward,
                variant = HustleButtonVariant.Outlined
            )
        }
    }
}
