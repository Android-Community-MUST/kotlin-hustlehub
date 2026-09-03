package must.kdroiders.hustlehub.sharedComposables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Shared back navigation button. Use [HustleBackButtonStyle.Standard] inside a TopAppBar
 * navigationIcon slot, or [HustleBackButtonStyle.Overlay] to float over full-bleed image headers.
 */
@Composable
fun HustleBackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: HustleBackButtonStyle = HustleBackButtonStyle.Standard,
    contentDescription: String = "Navigate back",
) {
    when (style) {
        HustleBackButtonStyle.Standard -> {
            IconButton(
                onClick = onClick,
                modifier = modifier,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = contentDescription,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(24.dp),
                )
            }
        }

        is HustleBackButtonStyle.Overlay -> {
            IconButton(
                onClick = onClick,
                modifier = modifier
                    .size(style.size)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = style.scrimAlpha)),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = contentDescription,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp),
                )
            }
        }
    }
}

sealed class HustleBackButtonStyle {
    /** Plain icon button for TopAppBar navigationIcon slots. */
    data object Standard : HustleBackButtonStyle()

    /**
     * Circular frosted-glass button for floating over full-bleed image content.
     *
     * @param size        Diameter of the button. Defaults to 44dp (meets WCAG 2.2 AA touch target).
     * @param scrimAlpha  Opacity of the dark scrim. Defaults to 0.4f.
     */
    data class Overlay(
        val size: Dp = 44.dp,
        val scrimAlpha: Float = 0.4f,
    ) : HustleBackButtonStyle()
}
