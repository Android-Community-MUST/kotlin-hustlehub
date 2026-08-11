package must.kdroiders.hustlehub.sharedComposables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * Provider name row used on discovery service cards and service detail screens.
 * Renders the provider's name alongside the [ProBadge] when [isVerifiedPro] is true.
 *
 * Usage:
 * ```
 * ServiceProviderBadge(name = "John Doe", isVerifiedPro = true)
 * ```
 */
@Composable
fun ServiceProviderBadge(
    name: String,
    isVerifiedPro: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false),
        )
        ProBadge(
            isVisible = isVerifiedPro,
            modifier = Modifier.padding(top = 1.dp),
        )
    }
}
