package must.kdroiders.hustlehub.sharedComposables

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

/**
 * Compact PRO badge chip shown next to provider names on discovery cards,
 * service details, and profile screens.
 *
 * Only renders when [isVisible] is true — callers can pass the Pro flag directly
 * without wrapping in an if/else.
 */
@Composable
fun ProBadge(
    isVisible: Boolean,
    modifier: Modifier = Modifier,
) {
    if (!isVisible) return
    AssistChip(
        onClick = {},
        enabled = false,
        modifier = modifier.semantics { contentDescription = "HustleHub Pro member" },
        label = {
            Text(
                text = "PRO",
                style = MaterialTheme.typography.labelSmall,
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                modifier = Modifier.padding(start = 4.dp),
            )
        },
        colors = AssistChipDefaults.assistChipColors(
            disabledContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
            disabledLabelColor = MaterialTheme.colorScheme.onTertiaryContainer,
            disabledLeadingIconContentColor = MaterialTheme.colorScheme.tertiary,
        ),
        border = AssistChipDefaults.assistChipBorder(
            enabled = false,
            borderColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.4f),
            disabledBorderColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.4f),
        ),
    )
}
