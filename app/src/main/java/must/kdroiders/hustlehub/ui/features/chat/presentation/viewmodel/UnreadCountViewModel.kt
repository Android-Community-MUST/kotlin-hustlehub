package must.kdroiders.hustlehub.ui.features.chat.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import must.kdroiders.hustlehub.core.notification.AppBadgeHelper
import must.kdroiders.hustlehub.ui.features.chat.data.local.dao.ConversationDao
import must.kdroiders.hustlehub.ui.features.notification.domain.repository.NotificationRepository
import javax.inject.Inject

/**
 * Lightweight ViewModel scoped to the main shell that exposes unread counts
 * for Chat messages, Notifications, and total unread launcher icon badge.
 */
@HiltViewModel
class UnreadCountViewModel
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        conversationDao: ConversationDao,
        private val notificationRepository: NotificationRepository,
    ) : ViewModel() {
        val unreadMessageCount: StateFlow<Int> =
            conversationDao
                .getTotalUnreadCount()
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
                    initialValue = 0,
                )

        private val _unreadNotificationCount = MutableStateFlow(0)
        val unreadNotificationCount: StateFlow<Int> = _unreadNotificationCount.asStateFlow()

        val totalUnreadCount: StateFlow<Int> =
            combine(unreadMessageCount, _unreadNotificationCount) { messages, notifications ->
                messages + notifications
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
                initialValue = 0,
            )

        init {
            refreshUnreadNotifications()
            viewModelScope.launch {
                totalUnreadCount.collect { count ->
                    AppBadgeHelper.applyBadgeCount(context, count)
                }
            }
        }

        fun refreshUnreadNotifications() {
            viewModelScope.launch {
                notificationRepository
                    .getUnreadCount()
                    .onSuccess { count ->
                        _unreadNotificationCount.value = count
                    }
            }
        }

        fun clearNotificationsBadge() {
            viewModelScope.launch {
                notificationRepository.markAllRead()
                _unreadNotificationCount.value = 0
            }
        }
    }
