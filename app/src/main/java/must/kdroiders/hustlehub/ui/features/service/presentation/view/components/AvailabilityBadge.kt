package must.kdroiders.hustlehub.ui.features.service.presentation.view.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import must.kdroiders.hustlehub.ui.features.service.domain.model.ServiceAvailability

@Composable
fun AvailabilityBadge(
    availability: ServiceAvailability,
    modifier: Modifier = Modifier,
) {
    val (dotColor, label) = when (availability) {
        ServiceAvailability.AVAILABLE -> Color(0xFF22C55E) to "Available"
        ServiceAvailability.BUSY -> Color(0xFFF59E0B) to "Busy"
        ServiceAvailability.OFFLINE -> Color(0xFF9CA3AF) to "Offline"
    }

    Surface(
        shape = RoundedCornerShape(50),
        color = dotColor.copy(alpha = 0.12f),
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(dotColor, CircleShape),
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = dotColor,
            )
        }
    }
}
