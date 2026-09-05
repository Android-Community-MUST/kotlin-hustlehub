package must.kdroiders.hustlehub.ui.features.notification.presentation.view

import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import must.kdroiders.hustlehub.navigation.DeepLinkAction
import must.kdroiders.hustlehub.navigation.MainNavigationViewModel
import must.kdroiders.hustlehub.sharedComposables.HustleBackButton
import must.kdroiders.hustlehub.ui.features.chat.presentation.viewmodel.UnreadCountViewModel
import must.kdroiders.hustlehub.ui.features.notification.domain.model.Notification
import must.kdroiders.hustlehub.ui.features.notification.domain.model.NotificationType
import must.kdroiders.hustlehub.ui.features.notification.presentation.viewmodel.NotificationViewModel
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NotificationViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val activity = context as? ComponentActivity
    val mainNavigationViewModel: MainNavigationViewModel? = if (activity != null) {
        hiltViewModel<MainNavigationViewModel>(viewModelStoreOwner = activity)
    } else {
        null
    }
    val unreadCountViewModel: UnreadCountViewModel? = if (activity != null) {
        hiltViewModel<UnreadCountViewModel>(viewModelStoreOwner = activity)
    } else {
        null
    }

    LaunchedEffect(Unit) {
        viewModel.markAllAsRead()
        unreadCountViewModel?.clearNotificationsBadge()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar
            NotificationHeader(
                unreadCount = state.unreadCount,
                onBack = onBack,
                onMarkAllRead = { viewModel.markAllAsRead() },
            )

            // Notifications Content with Date Grouping & Pull-to-refresh
            PullToRefreshBox(
                isRefreshing = state.isRefreshing,
                onRefresh = { viewModel.loadNotifications(isRefresh = true) },
                modifier = Modifier.fillMaxSize(),
            ) {
                if (state.notifications.isEmpty() && !state.isLoading) {
                    NotificationEmptyState()
                } else {
                    val grouped = groupNotificationsByDate(state.notifications)
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        grouped.forEach { (dateGroup, notificationsInGroup) ->
                            item(key = "header_$dateGroup") {
                                DateHeaderGroup(text = dateGroup)
                            }
                            items(
                                items = notificationsInGroup,
                                key = { it.id },
                            ) { notification ->
                                SwipeableNotificationItem(
                                    notification = notification,
                                    onClick = {
                                        viewModel.markAsRead(notification.id)
                                        handleNotificationTap(notification, onBack, mainNavigationViewModel)
                                    },
                                    onDelete = {
                                        viewModel.deleteNotification(notification.id)
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationHeader(
    unreadCount: Int,
    onBack: () -> Unit,
    onMarkAllRead: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 8.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HustleBackButton(onClick = onBack)
        Text(
            text = "Notifications",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .padding(start = 8.dp)
                .semantics { heading() },
        )
        if (unreadCount > 0) {
            Box(
                modifier = Modifier
                    .padding(start = 8.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(horizontal = 8.dp, vertical = 2.dp),
            ) {
                Text(
                    text = "$unreadCount new",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        if (unreadCount > 0) {
            TextButton(
                onClick = onMarkAllRead,
                contentPadding = PaddingValues(horizontal = 12.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.DoneAll,
                    contentDescription = "Mark all as read",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Mark all read",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
fun DateHeaderGroup(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .padding(start = 16.dp, top = 16.dp, bottom = 4.dp)
            .semantics { heading() },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeableNotificationItem(
    notification: Notification,
    onClick: () -> Unit,
    onDelete: () -> Unit,
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            if (dismissValue == SwipeToDismissBoxValue.EndToStart || dismissValue == SwipeToDismissBoxValue.StartToEnd) {
                onDelete()
                true
            } else {
                false
            }
        },
    )

    SwipeToDismissBox(
        state = dismissState,
        modifier = Modifier.padding(horizontal = 16.dp),
        backgroundContent = {
            val color = when (dismissState.targetValue) {
                SwipeToDismissBoxValue.EndToStart, SwipeToDismissBoxValue.StartToEnd -> MaterialTheme.colorScheme.errorContainer
                else -> Color.Transparent
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
                    .background(color)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete notification",
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                )
            }
        },
    ) {
        NotificationItem(notification = notification, onClick = onClick)
    }
}

@Composable
fun NotificationItem(
    notification: Notification,
    onClick: () -> Unit,
) {
    val containerColor = if (notification.isRead) {
        MaterialTheme.colorScheme.surface
    } else {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
    }

    val iconInfo = when (notification.type) {
        NotificationType.NEW_MESSAGE -> Triple(
            Icons.AutoMirrored.Filled.Chat,
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.primaryContainer,
        )
        NotificationType.NEW_REVIEW -> Triple(
            Icons.Default.Star,
            MaterialTheme.colorScheme.tertiary,
            MaterialTheme.colorScheme.tertiaryContainer,
        )
        NotificationType.SERVICE_INQUIRY -> Triple(
            Icons.Default.Work,
            MaterialTheme.colorScheme.secondary,
            MaterialTheme.colorScheme.secondaryContainer,
        )
        NotificationType.SYSTEM -> Triple(
            Icons.Default.Info,
            MaterialTheme.colorScheme.outline,
            MaterialTheme.colorScheme.surfaceVariant,
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .semantics(mergeDescendants = true) {}
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(iconInfo.third),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = iconInfo.first,
                    contentDescription = null,
                    tint = iconInfo.second,
                    modifier = Modifier.size(20.dp),
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = notification.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (notification.isRead) FontWeight.Medium else FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = notification.body,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = formatRelativeTime(notification.sentAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                )
            }

            if (!notification.isRead) {
                Spacer(modifier = Modifier.width(12.dp))
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                )
            }
        }
    }
}

@Composable
fun NotificationEmptyState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Default.NotificationsNone,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
            modifier = Modifier.size(80.dp),
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "No notifications yet",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.semantics { heading() },
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "We will notify you here when you receive new messages, reviews, or inquiries.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

fun groupNotificationsByDate(notifications: List<Notification>): Map<String, List<Notification>> {
    val today = LocalDate.now(ZoneId.systemDefault())
    val yesterday = today.minusDays(1)
    val weekAgo = today.minusDays(7)

    return notifications.groupBy { notification ->
        val date = try {
            Instant
                .parse(notification.sentAt)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
        } catch (e: Exception) {
            today
        }
        when {
            date.isEqual(today) -> "Today"
            date.isEqual(yesterday) -> "Yesterday"
            date.isAfter(weekAgo) -> "This Week"
            else -> "Older"
        }
    }
}

fun formatRelativeTime(dateString: String): String {
    return try {
        val instant = Instant.parse(dateString)
        val now = Instant.now()
        val duration = Duration.between(instant, now)
        val diffSeconds = duration.seconds
        when {
            diffSeconds < 60 -> "Just now"
            diffSeconds < 3600 -> "${diffSeconds / 60}m ago"
            diffSeconds < 86400 -> "${diffSeconds / 3600}h ago"
            else -> "${diffSeconds / 86400}d ago"
        }
    } catch (e: Exception) {
        "Just now"
    }
}

private fun handleNotificationTap(
    notification: Notification,
    onBack: () -> Unit,
    mainNavigationViewModel: MainNavigationViewModel?,
) {
    if (mainNavigationViewModel == null) return

    when (notification.type) {
        NotificationType.NEW_MESSAGE -> {
            val conversationId = notification.data?.get("conversationId")
            if (!conversationId.isNullOrBlank()) {
                onBack()
                mainNavigationViewModel.triggerDeepLink(DeepLinkAction.OpenChat(conversationId))
            }
        }
        NotificationType.NEW_REVIEW -> {
            onBack()
            mainNavigationViewModel.triggerDeepLink(DeepLinkAction.OpenProfile)
        }
        NotificationType.SERVICE_INQUIRY -> {
            onBack()
            mainNavigationViewModel.triggerDeepLink(DeepLinkAction.OpenChatList)
        }
        NotificationType.SYSTEM -> {
            // No navigation needed for system notifications
        }
    }
}
