package must.kdroiders.hustlehub.ui.features.profile

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

@Composable
fun ProfileScreen(
    profileViewModel: ProfileViewModel = hiltViewModel(),
    onEditClick: () -> Unit = {},
    onAddNewServiceClick: () -> Unit = {}
) {
    val state by profileViewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when {
            state.isLoading -> LoadingState()
            state.error != null -> ErrorState(
                message = state.error!!,
                onRetry = profileViewModel::retry
            )
            else -> ProfileContent(
                state = state,
                onEditClick = onEditClick,
                onToggleService = profileViewModel::toggleServiceActive,
                onAddNewServiceClick = onAddNewServiceClick
            )
        }
    }
}

// ─────────────────────────────────────────────────
// Main content — LazyColumn for performance
// ─────────────────────────────────────────────────

@Composable
private fun ProfileContent(
    state: ProfileUiState,
    onEditClick: () -> Unit,
    onToggleService: (String) -> Unit,
    onAddNewServiceClick: () -> Unit
) {
    val user = state.user ?: return

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(
            top = 16.dp,
            bottom = 100.dp
        )
    ) {
        // Header
        item(key = "header") {
            ProfileHeader(onEditClick = onEditClick)
        }

        // Avatar + info
        item(key = "avatar") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ProfileAvatar(
                    photoUrl = user.profilePhotoUrl
                )
                Spacer(Modifier.height(12.dp))
                ProfileInfo(
                    name = user.name,
                    course = user.course,
                    yearOfStudy = user.yearOfStudy,
                    campus = user.campus
                )
            }
        }

        // Stats row
        item(key = "stats") {
            Spacer(Modifier.height(20.dp))
            ProfileStatsRow(
                hustleScore = state.hustleScore,
                serviceCount = state.services.size,
                reviewCount = state.reviewCount,
                modifier = Modifier.padding(
                    horizontal = 16.dp
                )
            )
        }

        // Badges
        item(key = "badges") {
            Spacer(Modifier.height(16.dp))
            ProfileBadges(
                badges = state.badges,
                modifier = Modifier.padding(
                    horizontal = 16.dp
                )
            )
        }

        // Services header
        item(key = "services_header") {
            Spacer(Modifier.height(24.dp))
            ServicesHeader(
                onAddNewServiceClick = onAddNewServiceClick,
                modifier = Modifier.padding(
                    horizontal = 16.dp
                )
            )
            Spacer(Modifier.height(12.dp))
        }

        // Service cards
        items(
            items = state.services,
            key = { it.id }
        ) { service ->
            ServiceCard(
                service = service,
                onToggle = {
                    onToggleService(service.id)
                },
                modifier = Modifier.padding(
                    horizontal = 16.dp,
                    vertical = 6.dp
                )
            )
        }

        // Bottom tabs
        item(key = "bottom_tabs") {
            Spacer(Modifier.height(20.dp))
            ProfileBottomTabs(
                modifier = Modifier.padding(
                    horizontal = 16.dp
                )
            )
        }
    }
}

// ─────────────────────────────────────────────────
// Header — "My Profile" + edit icon
// ─────────────────────────────────────────────────

@Composable
private fun ProfileHeader(
    onEditClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "My Profile",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        IconButton(onClick = onEditClick) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Edit profile",
                tint = MaterialTheme.colorScheme
                    .onSurfaceVariant
            )
        }
    }
}

// ─────────────────────────────────────────────────
// Avatar — circular photo with gradient border
// ─────────────────────────────────────────────────

@Composable
private fun ProfileAvatar(photoUrl: String) {
    val gradientBorder = Brush.linearGradient(
        listOf(HustlePrimary, HustlePrimaryVariant)
    )

    Box(
        modifier = Modifier
            .size(110.dp)
            .border(
                width = 3.dp,
                brush = gradientBorder,
                shape = CircleShape
            )
            .padding(4.dp)
            .clip(CircleShape)
            .background(
                MaterialTheme.colorScheme.surfaceVariant
            ),
        contentAlignment = Alignment.Center
    ) {
        if (photoUrl.isNotBlank()) {
            AsyncImage(
                model = photoUrl,
                contentDescription = "Profile photo",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "No photo",
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme
                    .onSurfaceVariant
            )
        }
    }
}

// ─────────────────────────────────────────────────
// User info — name, course · year, campus
// ─────────────────────────────────────────────────

@Composable
private fun ProfileInfo(
    name: String,
    course: String,
    yearOfStudy: Int,
    campus: String
) {
    Text(
        text = name.ifBlank { "Student" },
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground,
        textAlign = TextAlign.Center
    )
    Spacer(Modifier.height(4.dp))
    Text(
        text = buildString {
            if (course.isNotBlank()) append(course)
            append(" · Year $yearOfStudy")
        },
        fontSize = 14.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
    )
    Spacer(Modifier.height(4.dp))
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = MaterialTheme.colorScheme
                .onSurfaceVariant
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = campus.ifBlank { "Campus" },
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme
                .onSurfaceVariant
        )
    }
}

// ─────────────────────────────────────────────────
// Stats row — Score / Services / Reviews
// ─────────────────────────────────────────────────

@Composable
private fun ProfileStatsRow(
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
private fun StatCard(
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

// ─────────────────────────────────────────────────
// Badges — horizontally scrollable chips
// ─────────────────────────────────────────────────

@Composable
private fun ProfileBadges(
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
private fun BadgeChip(badge: Badge) {
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

// ─────────────────────────────────────────────────
// Services section
// ─────────────────────────────────────────────────

@Composable
private fun ServicesHeader(
    onAddNewServiceClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "My Services",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Add New +",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = HustlePrimary,
            modifier = Modifier.clickable { onAddNewServiceClick() }
        )
    }
}

@Composable
private fun ServiceCard(
    service: Service,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(HustleDarkSurfaceVariant)
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {
                    // Service icon placeholder
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                HustleDarkSurfaceBright
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (
                            service.iconUrl.isNotBlank()
                        ) {
                            AsyncImage(
                                model = service.iconUrl,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(
                                        RoundedCornerShape(
                                            8.dp
                                        )
                                    ),
                                contentScale =
                                    ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector =
                                    Icons.Default
                                        .Description,
                                contentDescription =
                                    null,
                                tint = MaterialTheme
                                    .colorScheme
                                    .onSurfaceVariant,
                                modifier = Modifier
                                    .size(24.dp)
                            )
                        }
                    }

                    Spacer(Modifier.width(12.dp))

                    Column {
                        Text(
                            text = service.title,
                            fontSize = 15.sp,
                            fontWeight =
                                FontWeight.SemiBold,
                            color = MaterialTheme
                                .colorScheme.onSurface
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = service.priceRange,
                            fontSize = 13.sp,
                            color = MaterialTheme
                                .colorScheme
                                .onSurfaceVariant
                        )
                    }
                }

                Column(
                    horizontalAlignment =
                        Alignment.End
                ) {
                    Switch(
                        checked = service.isActive,
                        onCheckedChange = {
                            onToggle()
                        },
                        colors = SwitchDefaults.colors(
                            checkedTrackColor =
                                HustlePrimary,
                            checkedThumbColor =
                                Color.White,
                            uncheckedTrackColor =
                                HustleOfflineGray
                                    .copy(alpha = 0.4f),
                            uncheckedThumbColor =
                                HustleOfflineGray
                        )
                    )
                    Text(
                        text = if (service.isActive)
                            "Active" else "Offline",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (service.isActive)
                            HustleActiveGreen
                        else HustleOfflineGray
                    )
                }
            }

            // Tags
            if (service.tags.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                Row(
                    horizontalArrangement =
                        Arrangement.spacedBy(8.dp)
                ) {
                    service.tags.forEach { tag ->
                        TagChip(label = tag)
                    }
                }
            }
        }
    }
}

@Composable
private fun TagChip(label: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                HustleDarkSurfaceBright
            )
            .padding(
                horizontal = 10.dp,
                vertical = 4.dp
            )
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme
                .onSurfaceVariant
        )
    }
}

// ─────────────────────────────────────────────────
// Analytics / Earnings tabs
// ─────────────────────────────────────────────────

@Composable
private fun ProfileBottomTabs(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TabButton(
            label = "Analytics",
            icon = Icons.Default.BarChart,
            modifier = Modifier.weight(1f)
        )
        TabButton(
            label = "Earnings",
            icon = Icons.Outlined.MonetizationOn,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun TabButton(
    label: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(HustleDarkSurfaceVariant)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme
                    .onSurfaceVariant
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

// ─────────────────────────────────────────────────
// Loading / Error states
// ─────────────────────────────────────────────────

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = HustlePrimary,
            strokeWidth = 3.dp
        )
    }
}

@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Tap to retry",
                color = HustlePrimary,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(HustleDarkSurfaceVariant)
                    .padding(
                        horizontal = 24.dp,
                        vertical = 10.dp
                    )
            )
        }
    }
}
