package must.kdroiders.hustlehub.ui.features.service.presentation.view.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import must.kdroiders.hustlehub.data.model.ServiceAvailability
import must.kdroiders.hustlehub.ui.theme.HustleActiveGreen
import must.kdroiders.hustlehub.ui.theme.HustleOfflineGray
import must.kdroiders.hustlehub.ui.theme.HustleWarningAmber

/**
 * 3-state availability selector chip row.
 * 🟢 Available  🟡 Busy  🔴 Offline
 */
@Composable
fun AvailabilityChipSelector(
    current: ServiceAvailability,
    onSelect: (ServiceAvailability) -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AvailabilityChip(
            label = "Available",
            dotColor = HustleActiveGreen,
            selected = current == ServiceAvailability.AVAILABLE,
            enabled = enabled,
            onClick = { onSelect(ServiceAvailability.AVAILABLE) }
        )
        AvailabilityChip(
            label = "Busy",
            dotColor = HustleWarningAmber,
            selected = current == ServiceAvailability.BUSY,
            enabled = enabled,
            onClick = { onSelect(ServiceAvailability.BUSY) }
        )
        AvailabilityChip(
            label = "Offline",
            dotColor = HustleOfflineGray,
            selected = current == ServiceAvailability.OFFLINE,
            enabled = enabled,
            onClick = { onSelect(ServiceAvailability.OFFLINE) }
        )
    }
}

@Composable
private fun AvailabilityChip(
    label: String,
    dotColor: Color,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val bgColor = if (selected)
        dotColor.copy(alpha = 0.15f)
    else
        MaterialTheme.colorScheme.surfaceVariant

    val borderColor = if (selected) dotColor else Color.Transparent

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(20.dp))
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Dot indicator
        androidx.compose.foundation.Canvas(modifier = Modifier.size(8.dp)) {
            drawCircle(color = dotColor)
        }
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) dotColor else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
