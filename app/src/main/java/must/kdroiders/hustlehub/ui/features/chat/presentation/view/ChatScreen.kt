package must.kdroiders.hustlehub.ui.features.chat.presentation.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import must.kdroiders.hustlehub.sharedComposables.EmptyStateView
import must.kdroiders.hustlehub.sharedComposables.HustleScaffold
import must.kdroiders.hustlehub.ui.features.chat.domain.model.Conversation
import must.kdroiders.hustlehub.ui.features.chat.presentation.viewmodel.ConversationListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    onNavigateToChatDetail: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ConversationListViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    HustleScaffold(
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets(0, 0, 0, 0),
                title = {
                    Text(
                        text = "Messages",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                ),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier,
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            when {
                state.isLoading && !state.isRefreshing -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.primary,
                    )
                }

                state.conversations.isEmpty() -> {
                    EmptyStateView(
                        title = "No messages yet",
                        description = "Start a conversation with a service provider to chat about tasks.",
                        icon = Icons.AutoMirrored.Filled.Chat,
                        modifier = Modifier.align(Alignment.Center),
                    )
                }

                else -> {
                    val pullToRefreshState = rememberPullToRefreshState()
                    PullToRefreshBox(
                        isRefreshing = state.isRefreshing,
                        onRefresh = viewModel::refreshConversations,
                        modifier = Modifier.fillMaxSize(),
                        state = pullToRefreshState,
                        indicator = {
                            PullToRefreshDefaults.Indicator(
                                modifier = Modifier.align(Alignment.TopCenter),
                                isRefreshing = state.isRefreshing,
                                state = pullToRefreshState,
                                color = MaterialTheme.colorScheme.primary,
                                containerColor = MaterialTheme.colorScheme.surface,
                            )
                        },
                    ) {
                        LazyColumn(
                            contentPadding = PaddingValues(vertical = 8.dp),
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            items(
                                items = state.conversations,
                                key = { it.id },
                            ) { conversation ->
                                val dismissState = rememberSwipeToDismissBoxState(
                                    confirmValueChange = { dismissValue ->
                                        if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                                            viewModel.deleteConversation(conversation.id)
                                            true
                                        } else {
                                            false
                                        }
                                    },
                                )

                                SwipeToDismissBox(
                                    state = dismissState,
                                    enableDismissFromStartToEnd = false,
                                    enableDismissFromEndToStart = true,
                                    backgroundContent = {
                                        val color = when (dismissState.dismissDirection) {
                                            SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.errorContainer
                                            else -> androidx.compose.ui.graphics.Color.Transparent
                                        }
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(color)
                                                .padding(horizontal = 24.dp),
                                            contentAlignment = Alignment.CenterEnd,
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete Conversation",
                                                tint = MaterialTheme.colorScheme.onErrorContainer,
                                            )
                                        }
                                    },
                                    content = {
                                        Box(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
                                            ConversationItem(
                                                conversation = conversation,
                                                onClick = { onNavigateToChatDetail(conversation.id) },
                                            )
                                        }
                                    },
                                )
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
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
private fun ConversationItem(
    conversation: Conversation,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Avatar
        val avatarUrl = conversation.otherUserAvatar
        if (!avatarUrl.isNullOrBlank()) {
            AsyncImage(
                model = avatarUrl,
                contentDescription = "User Avatar",
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop,
            )
        } else {
            // Letter avatar with a modern gradient-like fallback background
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                val firstLetter = conversation.otherUserName.firstOrNull()?.uppercase() ?: "?"
                Text(
                    text = firstLetter,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Name and Message
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = conversation.otherUserName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(4.dp))
            val messageText = when (conversation.lastMessageType) {
                "VOICE" -> "Voice note"
                "IMAGE" -> "Photo"
                "LOCATION" -> "Location"
                "SERVICE_CARD" -> "Service shared"
                else -> conversation.lastMessage ?: "No messages yet"
            }
            val icon = when (conversation.lastMessageType) {
                "VOICE" -> Icons.Default.Mic
                "IMAGE" -> Icons.Default.Image
                "LOCATION" -> Icons.Default.Place
                "SERVICE_CARD" -> Icons.Default.Work
                else -> null
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start,
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = if (conversation.unreadCount > 0) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }
                Text(
                    text = messageText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (conversation.unreadCount > 0) {
                        MaterialTheme.colorScheme.onBackground
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    fontWeight = if (conversation.unreadCount > 0) FontWeight.Bold else FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Time and Badge
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.Center,
        ) {
            val formattedTime = formatTimestamp(conversation.lastMessageAt)
            Text(
                text = formattedTime,
                style = MaterialTheme.typography.bodySmall,
                color = if (conversation.unreadCount > 0) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                fontWeight = if (conversation.unreadCount > 0) FontWeight.Bold else FontWeight.Normal,
            )
            if (conversation.unreadCount > 0) {
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = conversation.unreadCount.toString(),
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

private fun formatTimestamp(isoString: String?): String {
    if (isoString == null) return ""
    return try {
        val parts = isoString.split("T")
        if (parts.size >= 2) {
            val time = parts[1].substring(0, 5) // "HH:MM"
            time
        } else {
            isoString
        }
    } catch (e: Exception) {
        isoString
    }
}
