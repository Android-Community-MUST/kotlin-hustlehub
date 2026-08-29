package must.kdroiders.hustlehub.ui.features.chat.presentation.view

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import must.kdroiders.hustlehub.sharedComposables.EmptyStateView
import must.kdroiders.hustlehub.sharedComposables.HustlePullToRefreshBox
import must.kdroiders.hustlehub.sharedComposables.HustleScaffold
import must.kdroiders.hustlehub.sharedComposables.HustleSearchBar
import must.kdroiders.hustlehub.ui.features.chat.domain.model.Conversation
import must.kdroiders.hustlehub.ui.features.chat.presentation.viewmodel.ConversationFilter
import must.kdroiders.hustlehub.ui.features.chat.presentation.viewmodel.ConversationListViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ChatScreen(
    onNavigateToChatDetail: (String) -> Unit,
    modifier: Modifier = Modifier,
    conversationListViewModel: ConversationListViewModel = hiltViewModel(),
) {
    val state by conversationListViewModel.uiState.collectAsState()

    HustleScaffold(
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets(0, 0, 0, 0),
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = "Messages",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.semantics { heading() },
                        )
                    }
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            // 1. Search Bar Header (Using shared HustleSearchBar composable)
            HustleSearchBar(
                query = state.searchQuery,
                onQueryChanged = conversationListViewModel::onSearchQueryChanged,
                placeholder = "Search messages or services...",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
            )

            // 2. Category Filter Chips Row (All Chats, Unread, Services, Archived)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Spacer(modifier = Modifier.width(8.dp))

                ConversationFilter.entries.forEach { filter ->
                    val isSelected = state.selectedFilter == filter
                    val count = when (filter) {
                        ConversationFilter.UNREAD -> state.conversations.count { it.unreadCount > 0 }
                        else -> 0
                    }

                    FilterChip(
                        selected = isSelected,
                        onClick = { conversationListViewModel.onFilterSelected(filter) },
                        label = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Text(
                                    text = filter.label,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                )
                                if (filter == ConversationFilter.UNREAD && count > 0) {
                                    Box(
                                        modifier = Modifier
                                            .size(18.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                                            ),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text(
                                            text = if (count > 99) "99+" else count.toString(),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onPrimary,
                                        )
                                    }
                                }
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                            selectedBorderColor = Color.Transparent,
                            borderWidth = 1.dp,
                            selectedBorderWidth = 0.dp,
                        ),
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))
            }

            Spacer(modifier = Modifier.height(4.dp))

            // 3. Conversation List & Contextual Empty States
            Box(modifier = Modifier.fillMaxSize()) {
                HustlePullToRefreshBox(
                    isRefreshing = state.isRefreshing,
                    onRefresh = conversationListViewModel::refreshConversations,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    val filteredList = state.filteredConversations

                    when {
                        state.isLoading && !state.isRefreshing -> {
                            CircularWavyProgressIndicator(
                                modifier = Modifier.align(Alignment.Center),
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }

                        filteredList.isEmpty() -> {
                            val (title, description) = when {
                                state.searchQuery.isNotBlank() ->
                                    "No results found" to "No conversations match \"${state.searchQuery}\""

                                state.selectedFilter == ConversationFilter.UNREAD ->
                                    "No unread messages" to "You're all caught up! No unread conversations."

                                state.selectedFilter == ConversationFilter.SERVICES ->
                                    "No service chats" to "Start a conversation on a service to see it here."

                                state.selectedFilter == ConversationFilter.ARCHIVED ->
                                    "No archived chats" to "Archived conversations will appear here."

                                else ->
                                    "No messages yet" to "Start a conversation with a service provider to chat about tasks."
                            }

                            EmptyStateView(
                                title = title,
                                description = description,
                                icon = Icons.AutoMirrored.Filled.Chat,
                                modifier = Modifier.align(Alignment.Center),
                            )
                        }

                        else -> {
                            LazyColumn(
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxSize(),
                            ) {
                                items(
                                    items = filteredList,
                                    key = { it.id },
                                ) { conversation ->
                                    val dismissState = rememberSwipeToDismissBoxState(
                                        confirmValueChange = { dismissValue ->
                                            when (dismissValue) {
                                                SwipeToDismissBoxValue.EndToStart -> {
                                                    conversationListViewModel.deleteConversation(conversation.id)
                                                    true
                                                }
                                                SwipeToDismissBoxValue.StartToEnd -> {
                                                    conversationListViewModel.toggleArchiveConversation(conversation.id)
                                                    true
                                                }
                                                else -> false
                                            }
                                        },
                                    )

                                    SwipeToDismissBox(
                                        state = dismissState,
                                        enableDismissFromStartToEnd = true,
                                        enableDismissFromEndToStart = true,
                                        backgroundContent = {
                                            val (color, icon, align) = when (dismissState.dismissDirection) {
                                                SwipeToDismissBoxValue.EndToStart -> Triple(
                                                    MaterialTheme.colorScheme.errorContainer,
                                                    Icons.Default.Delete,
                                                    Alignment.CenterEnd,
                                                )
                                                SwipeToDismissBoxValue.StartToEnd -> Triple(
                                                    MaterialTheme.colorScheme.primaryContainer,
                                                    if (conversation.isArchived) Icons.Default.Unarchive else Icons.Default.Archive,
                                                    Alignment.CenterStart,
                                                )
                                                else -> Triple(Color.Transparent, Icons.Default.Delete, Alignment.CenterEnd)
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .clip(RoundedCornerShape(16.dp))
                                                    .background(color)
                                                    .padding(horizontal = 24.dp),
                                                contentAlignment = align,
                                            ) {
                                                Icon(
                                                    imageVector = icon,
                                                    contentDescription = "Swipe Action",
                                                    tint = if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) {
                                                        MaterialTheme.colorScheme.onErrorContainer
                                                    } else {
                                                        MaterialTheme.colorScheme.onPrimaryContainer
                                                    },
                                                )
                                            }
                                        },
                                        content = {
                                            ConversationItem(
                                                conversation = conversation,
                                                onClick = { onNavigateToChatDetail(conversation.id) },
                                            )
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
}

@Composable
private fun ConversationItem(
    conversation: Conversation,
    onClick: () -> Unit,
) {
    val hasUnread = conversation.unreadCount > 0

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .semantics(mergeDescendants = true) {
                role = Role.Button
            }.clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Avatar with optional unread border ring & online dot
        val avatarUrl = conversation.otherUserAvatar
        Box {
            val avatarModifier = Modifier
                .size(56.dp)
                .then(
                    if (hasUnread) {
                        Modifier
                            .border(
                                width = 2.dp,
                                color = MaterialTheme.colorScheme.primary,
                                shape = CircleShape,
                            ).padding(2.dp)
                    } else {
                        Modifier
                    },
                ).clip(CircleShape)

            if (!avatarUrl.isNullOrBlank()) {
                AsyncImage(
                    model = avatarUrl,
                    contentDescription = null,
                    modifier = avatarModifier,
                    contentScale = ContentScale.Crop,
                )
            } else {
                Box(
                    modifier = avatarModifier
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .clearAndSetSemantics { },
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
        }

        Spacer(modifier = Modifier.width(14.dp))

        // Name, optional service tag, and Message Preview
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = conversation.otherUserName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (hasUnread) FontWeight.Bold else FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Subtitle Line: Last message or service context
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
                        modifier = Modifier.size(15.dp),
                        tint = if (hasUnread) {
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
                    color = if (hasUnread) {
                        MaterialTheme.colorScheme.onBackground
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    fontWeight = if (hasUnread) FontWeight.Bold else FontWeight.Normal,
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
            val formattedTime = formatSmartTimestamp(conversation.lastMessageAt)
            Text(
                text = formattedTime,
                style = MaterialTheme.typography.bodySmall,
                color = if (hasUnread) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                fontWeight = if (hasUnread) FontWeight.Bold else FontWeight.Normal,
            )
            if (hasUnread) {
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .heightIn(min = 20.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .animateContentSize(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMedium,
                            ),
                        ).padding(horizontal = 7.dp, vertical = 2.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = if (conversation.unreadCount > 99) "99+" else conversation.unreadCount.toString(),
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

/**
 * Smart timestamp formatting for conversation list:
 * - Same day: "14:30"
 * - Yesterday: "Yesterday"
 * - Same week: "Mon", "Tue", etc.
 * - Older: "Jun 15" or "12/25/25"
 */
private fun formatSmartTimestamp(isoString: String?): String {
    if (isoString.isNullOrBlank()) return ""
    return try {
        val instant = Instant.parse(isoString)
        val zonedDateTime = instant.atZone(ZoneId.systemDefault())
        val now = Instant.now().atZone(ZoneId.systemDefault())
        val daysBetween = ChronoUnit.DAYS.between(zonedDateTime.toLocalDate(), now.toLocalDate())

        when {
            daysBetween == 0L -> zonedDateTime.format(DateTimeFormatter.ofPattern("HH:mm"))
            daysBetween == 1L -> "Yesterday"
            daysBetween < 7L -> zonedDateTime.format(DateTimeFormatter.ofPattern("EEE"))
            else -> zonedDateTime.format(DateTimeFormatter.ofPattern("MMM dd"))
        }
    } catch (e: Exception) {
        try {
            val parts = isoString.split("T")
            if (parts.size >= 2) parts[1].substring(0, 5) else isoString
        } catch (_: Exception) {
            isoString
        }
    }
}
