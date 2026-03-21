package must.kdroiders.hustlehub.ui.features.profile.presentation.view.components

import must.kdroiders.hustlehub.ui.features.profile.presentation.viewmodel.ProfileViewModel
import must.kdroiders.hustlehub.ui.features.profile.presentation.viewmodel.ProfileUiState
import must.kdroiders.hustlehub.ui.features.profile.presentation.viewmodel.Badge
import must.kdroiders.hustlehub.ui.features.profile.presentation.viewmodel.BadgeType
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.MonetizationOn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import must.kdroiders.hustlehub.data.model.Service
import must.kdroiders.hustlehub.ui.theme.HustleActiveGreen
import must.kdroiders.hustlehub.ui.theme.HustleBadgeBlue
import must.kdroiders.hustlehub.ui.theme.HustleBadgeGold
import must.kdroiders.hustlehub.ui.theme.HustleBadgeGreen
import must.kdroiders.hustlehub.ui.theme.HustleDarkSurfaceBright
import must.kdroiders.hustlehub.ui.theme.HustleDarkSurfaceVariant
import must.kdroiders.hustlehub.ui.theme.HustleOfflineGray
import must.kdroiders.hustlehub.ui.theme.HustlePrimary
import must.kdroiders.hustlehub.ui.theme.HustlePrimaryVariant
import must.kdroiders.hustlehub.ui.theme.HustleWarningAmber
import androidx.compose.material3.*

// ─────────────────────────────────────────────────
// Stats row — Score / Services / Reviews
// ─────────────────────────────────────────────────

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
            .background(HustleDarkSurfaceVariant)
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

