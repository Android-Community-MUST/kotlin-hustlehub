package must.kdroiders.hustlehub.ui.features.profile.presentation.view.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import must.kdroiders.hustlehub.R
import must.kdroiders.hustlehub.ui.theme.HustleWarningAmber

/** Unified stats row displaying hustle score, service count, and reviews. */
@Composable
fun ProfileStatsRow(
    hustleScore: Float,
    serviceCount: Int,
    reviewCount: Int,
    modifier: Modifier = Modifier,
    onReviewsClick: (() -> Unit)? = null,
    onServicesClick: (() -> Unit)? = null,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                shape = RoundedCornerShape(20.dp),
            ).padding(vertical = 16.dp, horizontal = 8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StatColumn(
                value = if (hustleScore > 0f) String.format("%.1f", hustleScore) else "0.0",
                label = stringResource(R.string.profile_stat_hustle_score),
                icon = Icons.Default.Star,
                iconTint = HustleWarningAmber,
                modifier = Modifier.weight(1f),
            )

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(30.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)),
            )

            StatColumn(
                value = serviceCount.toString(),
                label = stringResource(R.string.profile_stat_services),
                onClick = onServicesClick,
                modifier = Modifier.weight(1f),
            )

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(30.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)),
            )

            StatColumn(
                value = reviewCount.toString(),
                label = stringResource(R.string.profile_stat_reviews),
                onClick = onReviewsClick,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun StatColumn(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    iconTint: Color = HustleWarningAmber,
    onClick: (() -> Unit)? = null,
) {
    val cleanLabel = label.replace("\n", " ")
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(vertical = 4.dp, horizontal = 2.dp)
            .defaultMinSize(minHeight = 48.dp)
            .semantics(mergeDescendants = true) {
                contentDescription = "$value $cleanLabel"
                if (onClick != null) {
                    role = androidx.compose.ui.semantics.Role.Button
                }
            },
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (icon != null) {
                Spacer(Modifier.width(4.dp))
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
        Spacer(Modifier.height(3.dp))
        Text(
            text = cleanLabel,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.3.sp,
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun StatCard(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    iconTint: Color = HustleWarningAmber,
    onClick: (() -> Unit)? = null,
) {
    StatColumn(
        value = value,
        label = label,
        modifier = modifier,
        icon = icon,
        iconTint = iconTint,
        onClick = onClick,
    )
}
