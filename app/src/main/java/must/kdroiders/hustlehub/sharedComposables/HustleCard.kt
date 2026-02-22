package must.kdroiders.hustlehub.sharedComposables

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import must.kdroiders.hustlehub.ui.theme.HustleHubTheme

private val CardShape = RoundedCornerShape(12.dp)

enum class HustleCardVariant {
    /** Default surface card — subtle, clean */
    Surface,

    /** Slightly elevated card with a soft shadow */
    Elevated,

    /** Outlined card — no elevation, defined by a border */
    Outlined,

    /** Filled with surfaceVariant — useful for secondary sections */
    Tonal,
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HustleCard(
    modifier: Modifier = Modifier,
    variant: HustleCardVariant = HustleCardVariant.Elevated,
    onClick: (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(20.dp),
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val motionScheme = MaterialTheme.motionScheme

    val scale by animateFloatAsState(
        targetValue = if (isPressed && onClick != null) 0.985f else 1f,
        animationSpec = motionScheme.fastSpatialSpec(),
        label = "cardScale"
    )

    val cardModifier = modifier
        .fillMaxWidth()
        .graphicsLayer { scaleX = scale; scaleY = scale }

    when (variant) {
        HustleCardVariant.Surface -> {
            Card(
                onClick = onClick ?: {},
                enabled = onClick != null,
                modifier = cardModifier,
                shape = CardShape,
                interactionSource = interactionSource,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Box(Modifier.padding(contentPadding)) { content() }
            }
        }

        HustleCardVariant.Elevated -> {
            Card(
                onClick = onClick ?: {},
                enabled = onClick != null,
                modifier = cardModifier,
                shape = CardShape,
                interactionSource = interactionSource,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 2.dp,
                    pressedElevation = 6.dp,
                    hoveredElevation = 4.dp
                )
            ) {
                Box(Modifier.padding(contentPadding)) { content() }
            }
        }

        HustleCardVariant.Outlined -> {
            Card(
                onClick = onClick ?: {},
                enabled = onClick != null,
                modifier = cardModifier,
                shape = CardShape,
                interactionSource = interactionSource,
                border = BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                ),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Box(Modifier.padding(contentPadding)) { content() }
            }
        }

        HustleCardVariant.Tonal -> {
            Card(
                onClick = onClick ?: {},
                enabled = onClick != null,
                modifier = cardModifier,
                shape = CardShape,
                interactionSource = interactionSource,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Box(Modifier.padding(contentPadding)) { content() }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF4F4F4)
@Composable
fun HustleCardPreview() {
    HustleHubTheme {
        Column(modifier = Modifier.padding(20.dp)) {
            HustleCard(variant = HustleCardVariant.Elevated, onClick = {}) {
                Text("Elevated Card", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                Text(
                    "Clickable with press scale animation.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(12.dp))
            HustleCard(variant = HustleCardVariant.Outlined) {
                Text("Outlined Card", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                Text(
                    "Clean border, no shadow.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(12.dp))
            HustleCard(variant = HustleCardVariant.Tonal) {
                Text("Tonal Card", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                Text(
                    "Uses surfaceVariant for secondary sections.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
