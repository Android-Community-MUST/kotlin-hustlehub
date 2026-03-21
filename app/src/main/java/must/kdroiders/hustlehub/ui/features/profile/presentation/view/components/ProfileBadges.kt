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
// Badges — horizontally scrollable chips
// ─────────────────────────────────────────────────

@Composable
fun ProfileBadges(
    badges: List<Badge>,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(horizontal = 0.dp)
    ) {
        items(
            items = badges,
            key = { it.label }
        ) { badge ->
            BadgeChip(badge = badge)
        }
    }
}

@Composable
fun BadgeChip(badge: Badge) {
    val (bgColor, textColor) = when (badge.type) {
        BadgeType.GOLD -> HustleBadgeGold to HustleWarningAmber
        BadgeType.GREEN -> HustleBadgeGreen to HustleActiveGreen
        BadgeType.BLUE -> HustleBadgeBlue to HustlePrimaryVariant
        BadgeType.DEFAULT -> HustleDarkSurfaceVariant to
            MaterialTheme.colorScheme.onSurfaceVariant
    }

    val icon = when (badge.type) {
        BadgeType.GOLD -> Icons.Default.Star
        BadgeType.GREEN -> Icons.Default.Star
        BadgeType.BLUE -> Icons.Default.Star
        BadgeType.DEFAULT -> Icons.Default.Star
    }

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .border(
                width = 1.dp,
                color = textColor.copy(alpha = 0.3f),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(
                horizontal = 14.dp,
                vertical = 8.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = textColor
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = badge.label,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = textColor
        )
    }
}

