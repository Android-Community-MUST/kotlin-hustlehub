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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
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
import must.kdroiders.hustlehub.ui.theme.success
import must.kdroiders.hustlehub.ui.theme.successContainer
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.launch

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

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var selectedPaymentReceipt by remember { mutableStateOf<Notification?>(null) }

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
                                        if (notification.type == NotificationType.PAYMENT_SUCCESS ||
                                            notification.type == NotificationType.PAYMENT_FAILED
                                        ) {
                                            selectedPaymentReceipt = notification
                                        } else {
                                            handleNotificationTap(notification, onBack, mainNavigationViewModel)
                                        }
                                    },
                                    onDelete = {
                                        val deletedItem = notification
                                        viewModel.deleteNotification(deletedItem.id)
                                        coroutineScope.launch {
                                            snackbarHostState.currentSnackbarData?.dismiss()
                                            val result = snackbarHostState.showSnackbar(
                                                message = "Notification deleted",
                                                actionLabel = "Undo",
                                                duration = SnackbarDuration.Short,
                                            )
                                            if (result == SnackbarResult.ActionPerformed) {
                                                viewModel.restoreNotification(deletedItem)
                                            }
                                        }
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp),
        )

        selectedPaymentReceipt?.let { receipt ->
            PaymentReceiptDialog(
                notification = receipt,
                onDismiss = { selectedPaymentReceipt = null },
                onNavigateToService = { serviceId ->
                    onBack()
                    mainNavigationViewModel?.triggerDeepLink(DeepLinkAction.OpenServiceDetail(serviceId))
                },
                onNavigateToSubscription = {
                    onBack()
                    mainNavigationViewModel?.triggerDeepLink(DeepLinkAction.OpenSubscription())
                },
            )
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
            if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
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
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            val isSwiping = dismissState.targetValue != SwipeToDismissBoxValue.Settled ||
                dismissState.currentValue != SwipeToDismissBoxValue.Settled

            val color = if (isSwiping) {
                MaterialTheme.colorScheme.errorContainer
            } else {
                Color.Transparent
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
                    .background(color)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd,
            ) {
                if (isSwiping) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete notification",
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                    )
                }
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
    val surfaceColor = MaterialTheme.colorScheme.surface
    val containerColor = if (notification.isRead) {
        surfaceColor
    } else {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.12f).compositeOver(surfaceColor)
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
        NotificationType.PAYMENT_SUCCESS -> Triple(
            Icons.Default.CheckCircle,
            MaterialTheme.colorScheme.success,
            MaterialTheme.colorScheme.successContainer,
        )
        NotificationType.PAYMENT_FAILED -> Triple(
            Icons.Default.Error,
            MaterialTheme.colorScheme.error,
            MaterialTheme.colorScheme.errorContainer,
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
        NotificationType.PAYMENT_SUCCESS, NotificationType.PAYMENT_FAILED -> {
            onBack()
            mainNavigationViewModel.triggerDeepLink(DeepLinkAction.OpenProfile)
        }
        NotificationType.SYSTEM -> {
            // No navigation needed for system notifications
        }
    }
}

@Composable
fun PaymentReceiptDialog(
    notification: Notification,
    onDismiss: () -> Unit,
    onNavigateToService: (String) -> Unit,
    onNavigateToSubscription: () -> Unit,
) {
    val isSuccess = notification.type == NotificationType.PAYMENT_SUCCESS

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSuccess) MaterialTheme.colorScheme.successContainer
                        else MaterialTheme.colorScheme.errorContainer,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Error,
                    contentDescription = null,
                    tint = if (isSuccess) MaterialTheme.colorScheme.success else MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(32.dp),
                )
            }
        },
        title = {
            Text(
                text = if (isSuccess) "Payment Receipt" else "Payment Failed",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = notification.body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    ),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        val receiptNumber = notification.data?.get("receiptNumber")
                        if (!receiptNumber.isNullOrBlank()) {
                            ReceiptRow(label = "M-Pesa Receipt", value = receiptNumber)
                        }
                        val amount = notification.data?.get("amount")
                        if (!amount.isNullOrBlank()) {
                            ReceiptRow(label = "Amount", value = "KES $amount")
                        }
                        val statusText = if (isSuccess) "Completed" else "Failed"
                        ReceiptRow(label = "Status", value = statusText)
                        ReceiptRow(label = "Date", value = formatRelativeTime(notification.sentAt))
                    }
                }
            }
        },
        confirmButton = {
            val serviceId = notification.data?.get("serviceId")
            if (isSuccess && !serviceId.isNullOrBlank()) {
                Button(
                    onClick = {
                        onDismiss()
                        onNavigateToService(serviceId)
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("View Featured Service")
                }
            } else if (isSuccess) {
                Button(
                    onClick = {
                        onDismiss()
                        onNavigateToSubscription()
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("View Subscriptions")
                }
            } else {
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Dismiss")
                }
            }
        },
        dismissButton = {
            if (isSuccess) {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Close")
                }
            }
        },
    )
}

@Composable
private fun ReceiptRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

