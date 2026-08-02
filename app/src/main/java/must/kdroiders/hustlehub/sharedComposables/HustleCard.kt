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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import must.kdroiders.hustlehub.ui.theme.HustleHubTheme
import must.kdroiders.hustlehub.ui.theme.LocalDimensions

enum class HustleCardVariant {
    /** Default surface card — subtle, clean */
    Surface,

    /** Slightly elevated card with a soft shadow */
    Elevated,

    /** Outlined translucent glass card — 30% alpha fill + 20% alpha border */
    Outlined,

    /** Tonal translucent section card */
    Tonal,

    /** Translucent glass card — matches StatCard style */
    Glass,
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HustleCard(
    modifier: Modifier = Modifier,
    variant: HustleCardVariant = HustleCardVariant.Elevated,
    onClick: (() -> Unit)? = null,
    contentPadding: PaddingValues? = null,
    containerColor: Color? = null,
    content: @Composable () -> Unit,
) {
    val dimensions = LocalDimensions.current
    val effectivePadding = contentPadding ?: PaddingValues(dimensions.cardContentPadding)
    val cardShape = RoundedCornerShape(dimensions.cardCornerRadius)
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val motionScheme = MaterialTheme.motionScheme

    val scale by animateFloatAsState(
        targetValue = if (isPressed && onClick != null) 0.985f else 1f,
        animationSpec = motionScheme.fastSpatialSpec(),
        label = "cardScale",
    )

    val cardModifier = modifier
        .fillMaxWidth()
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .semantics(mergeDescendants = true) {
            if (onClick != null) {
                role = Role.Button
            }
        }

    val glassBackground = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
    val glassBorder = BorderStroke(
        width = 1.dp,
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
    )

    when (variant) {
        HustleCardVariant.Surface -> {
            Card(
                onClick = onClick ?: {},
                enabled = onClick != null,
                modifier = cardModifier,
                shape = cardShape,
                interactionSource = interactionSource,
                colors = CardDefaults.cardColors(
                    containerColor = containerColor ?: MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            ) {
                Box(Modifier.padding(effectivePadding)) { content() }
            }
        }

        HustleCardVariant.Elevated -> {
            Card(
                onClick = onClick ?: {},
                enabled = onClick != null,
                modifier = cardModifier,
                shape = cardShape,
                interactionSource = interactionSource,
                colors = CardDefaults.cardColors(
                    containerColor = containerColor ?: MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 2.dp,
                    pressedElevation = 6.dp,
                    hoveredElevation = 4.dp,
                ),
            ) {
                Box(Modifier.padding(effectivePadding)) { content() }
            }
        }

        HustleCardVariant.Outlined -> {
            Card(
                onClick = onClick ?: {},
                enabled = onClick != null,
                modifier = cardModifier,
                shape = cardShape,
                interactionSource = interactionSource,
                border = BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                ),
                colors = CardDefaults.cardColors(
                    containerColor = containerColor ?: MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            ) {
                Box(Modifier.padding(effectivePadding)) { content() }
            }
        }

        HustleCardVariant.Tonal, HustleCardVariant.Glass -> {
            Card(
                onClick = onClick ?: {},
                enabled = onClick != null,
                modifier = cardModifier,
                shape = cardShape,
                interactionSource = interactionSource,
                border = glassBorder,
                colors = CardDefaults.cardColors(
                    containerColor = containerColor ?: glassBackground,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            ) {
                Box(Modifier.padding(effectivePadding)) { content() }
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
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.height(12.dp))
            HustleCard(variant = HustleCardVariant.Outlined) {
                Text("Outlined Card", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                Text(
                    "Clean border, no shadow.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.height(12.dp))
            HustleCard(variant = HustleCardVariant.Tonal) {
                Text("Tonal Card", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                Text(
                    "Uses surfaceVariant for secondary sections.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
