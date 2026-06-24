package must.kdroiders.hustlehub.ui.features.service.presentation.view.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import must.kdroiders.hustlehub.ui.features.service.domain.model.ServiceAvailability
import must.kdroiders.hustlehub.ui.features.service.domain.model.ServiceCategory

@Composable
fun AvailabilityBadge(
    availability: ServiceAvailability,
    modifier: Modifier = Modifier,
) {
    val (dotColor, label) = when (availability) {
        ServiceAvailability.AVAILABLE -> Color(0xFF10B981) to "Available Now"
        ServiceAvailability.BUSY -> Color(0xFFF59E0B) to "Busy"
        ServiceAvailability.OFFLINE -> Color(0xFF9CA3AF) to "Offline"
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), RoundedCornerShape(50))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(dotColor, CircleShape),
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = dotColor,
        )
    }
}

@Composable
fun CategoryBadge(
    category: ServiceCategory,
    modifier: Modifier = Modifier,
) {
    val emoji = when (category) {
        ServiceCategory.ALL -> "🔍"
        ServiceCategory.SALON -> "🏆"
        ServiceCategory.LAUNDRY -> "👕"
        ServiceCategory.TUTORING -> "📚"
        ServiceCategory.FOOD -> "🍔"
        ServiceCategory.TECH -> "💻"
        ServiceCategory.FASHION -> "👗"
        ServiceCategory.PHOTOGRAPHY -> "📸"
        ServiceCategory.DESIGN -> "🎨"
        ServiceCategory.OTHER -> "✨"
    }

    val displayLabel = category.label

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), RoundedCornerShape(50))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = emoji, style = MaterialTheme.typography.labelMedium)
        Spacer(Modifier.width(4.dp))
        Text(
            text = displayLabel,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
