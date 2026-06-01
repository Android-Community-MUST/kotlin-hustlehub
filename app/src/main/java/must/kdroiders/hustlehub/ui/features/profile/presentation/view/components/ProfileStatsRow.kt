package must.kdroiders.hustlehub.ui.features.profile.presentation.view.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import must.kdroiders.hustlehub.ui.theme.HustleWarningAmber

// Stats row — Score / Services / Reviews

@Composable
fun ProfileStatsRow(
    hustleScore: Float,
    serviceCount: Int,
    reviewCount: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            value = hustleScore.toString(),
            label = "HUSTLE\nSCORE",
            icon = Icons.Default.Star,
            iconTint = HustleWarningAmber,
            modifier = Modifier.weight(1f)
        )
        StatCard(
            value = serviceCount.toString(),
            label = "SERVICES",
            modifier = Modifier.weight(1f)
        )
        StatCard(
            value = reviewCount.toString(),
            label = "REVIEWS",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun StatCard(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    iconTint: Color = HustleWarningAmber
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(vertical = 16.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = value,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme
                        .onSurface
                )
                if (icon != null) {
                    Spacer(Modifier.width(4.dp))
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = iconTint
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme
                    .onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 14.sp
            )
        }
    }
}

