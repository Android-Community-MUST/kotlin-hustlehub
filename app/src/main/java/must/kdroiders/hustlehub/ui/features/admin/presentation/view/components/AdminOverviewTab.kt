package must.kdroiders.hustlehub.ui.features.admin.presentation.view.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import must.kdroiders.hustlehub.ui.features.admin.domain.model.AdminAnalytics
import java.text.NumberFormat
import java.util.Locale

@Composable
fun AdminOverviewTab(
    analytics: AdminAnalytics,
    modifier: Modifier = Modifier,
) {
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("en", "KE")).apply {
        maximumFractionDigits = 0
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Text(
                text = "Campus Operations Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "Real-time metrics aggregated across all active MUST students & services.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                MetricCard(
                    title = "Total Students",
                    value = analytics.totalUsers.toString(),
                    icon = Icons.Default.People,
                    tint = Color(0xFF2196F3),
                    modifier = Modifier.weight(1f),
                )
                MetricCard(
                    title = "Live Services",
                    value = analytics.totalServices.toString(),
                    icon = Icons.Default.Work,
                    tint = Color(0xFF00C853),
                    modifier = Modifier.weight(1f),
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                MetricCard(
                    title = "Pro Verified",
                    value = analytics.totalProSubscribers.toString(),
                    icon = Icons.Default.Star,
                    tint = Color(0xFFFFB300),
                    modifier = Modifier.weight(1f),
                )
                MetricCard(
                    title = "Open Reports",
                    value = analytics.openReportsCount.toString(),
                    icon = Icons.Default.Flag,
                    tint = if (analytics.openReportsCount > 0) Color(0xFFFF1744) else Color(0xFF757575),
                    modifier = Modifier.weight(1f),
                )
            }
        }

        item {
            MetricCard(
                title = "Monthly M-Pesa Volume",
                value = "KES ${analytics.monthlyRevenue}",
                icon = Icons.Default.AttachMoney,
                tint = Color(0xFF00BFA5),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    tint: Color,
    modifier: Modifier = Modifier,
) {
    ElevatedCard(
        modifier = modifier,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint,
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}
