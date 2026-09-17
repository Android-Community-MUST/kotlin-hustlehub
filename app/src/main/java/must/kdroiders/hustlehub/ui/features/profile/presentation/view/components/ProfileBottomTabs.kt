package must.kdroiders.hustlehub.ui.features.profile.presentation.view.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.outlined.MonetizationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import must.kdroiders.hustlehub.R

/** Segmented tab bar for switching between services, badges, and insights. */
@Composable
fun ProfileSegmentedTabs(
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    serviceCount: Int,
    badgeCount: Int,
    modifier: Modifier = Modifier,
) {
    val servicesLabel = if (serviceCount > 0) {
        stringResource(R.string.profile_services_count_format, serviceCount)
    } else {
        stringResource(R.string.profile_tab_services)
    }
    val badgesLabel = if (badgeCount > 0) {
        stringResource(R.string.profile_badges_count_format, badgeCount)
    } else {
        stringResource(R.string.profile_tab_badges)
    }
    val insightsLabel = stringResource(R.string.profile_tab_insights)

    val tabTitles = listOf(servicesLabel, badgesLabel, insightsLabel)

    TabRow(
        selectedTabIndex = selectedIndex,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f),
                shape = RoundedCornerShape(16.dp),
            ),
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onSurface,
        indicator = { tabPositions ->
            if (selectedIndex < tabPositions.size) {
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedIndex]),
                    height = 3.dp,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        },
        divider = {},
    ) {
        tabTitles.forEachIndexed { index, title ->
            val selected = selectedIndex == index
            Tab(
                selected = selected,
                onClick = { onTabSelected(index) },
                modifier = Modifier.defaultMinSize(minHeight = 48.dp),
                text = {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        ),
                        color = if (selected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        maxLines = 2,
                    )
                },
            )
        }
    }
}

/** Action buttons for navigating to analytics and earnings details. */
@Composable
fun ProfileBottomTabs(
    modifier: Modifier = Modifier,
    onAnalyticsClick: () -> Unit = {},
    onEarningsClick: () -> Unit = {},
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        TabButton(
            label = stringResource(R.string.profile_tab_analytics),
            icon = Icons.Default.BarChart,
            modifier = Modifier.weight(1f),
            onClick = onAnalyticsClick,
        )
        TabButton(
            label = stringResource(R.string.profile_tab_earnings),
            icon = Icons.Outlined.MonetizationOn,
            modifier = Modifier.weight(1f),
            onClick = onEarningsClick,
        )
    }
}

@Composable
fun TabButton(
    label: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    val tabCd = stringResource(R.string.cd_tab_format, label)

    Box(
        modifier = modifier
            .defaultMinSize(minHeight = 48.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                shape = RoundedCornerShape(24.dp),
            ).clickable(onClick = onClick)
            .padding(vertical = 18.dp, horizontal = 20.dp)
            .semantics {
                role = androidx.compose.ui.semantics.Role.Button
                contentDescription = tabCd
            },
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
