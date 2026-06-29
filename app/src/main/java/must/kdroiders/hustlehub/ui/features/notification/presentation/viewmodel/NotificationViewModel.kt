package must.kdroiders.hustlehub.ui.features.notification.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import must.kdroiders.hustlehub.ui.features.notification.domain.model.Notification
import must.kdroiders.hustlehub.ui.features.notification.domain.repository.NotificationRepository
import javax.inject.Inject

data class NotificationUiState(
    val notifications: List<Notification> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val unreadCount: Int = 0
)

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val repository: NotificationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationUiState())
    val uiState: StateFlow<NotificationUiState> = _uiState.asStateFlow()

    init {
        loadNotifications()
    }

    fun loadNotifications(isRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.update { 
                if (isRefresh) it.copy(isRefreshing = true) else it.copy(isLoading = true) 
            }
            repository.getNotifications(page = 0, size = 50)
                .onSuccess { list ->
                    _uiState.update { current ->
                        current.copy(
                            notifications = list,
                            isLoading = false,
                            isRefreshing = false,
                            error = null,
                            unreadCount = list.count { !it.isRead }
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update { current ->
                        current.copy(
                            isLoading = false,
                            isRefreshing = false,
                            error = e.localizedMessage ?: "Failed to load notifications"
                        )
                    }
                }
        }
    }

    fun markAsRead(notificationId: String) {
        val currentList = _uiState.value.notifications
        val target = currentList.find { it.id == notificationId }
        if (target == null) return
        if (target.isRead) return

        // Optimistic update
        val updatedList = currentList.map {
            if (it.id == notificationId) it.copy(isRead = true) else it
        }
        _uiState.update { current ->
            current.copy(
                notifications = updatedList,
                unreadCount = updatedList.count { !it.isRead }
            )
        }

        // Fire network call in background
        viewModelScope.launch {
            repository.markRead(notificationId)
                .onFailure {
                    // Revert on failure
                    _uiState.update { current ->
                        current.copy(
                            notifications = currentList,
                            unreadCount = currentList.count { !it.isRead }
                        )
                    }
                }
        }
    }

    fun markAllAsRead() {
        val currentList = _uiState.value.notifications
        val hasUnread = currentList.any { !it.isRead }
        if (!hasUnread) return

        // Optimistic update
        val updatedList = currentList.map { it.copy(isRead = true) }
        _uiState.update { current ->
            current.copy(
                notifications = updatedList,
                unreadCount = 0
            )
        }

        // Fire network call in background
        viewModelScope.launch {
            repository.markAllRead()
                .onFailure {
                    // Revert on failure
                    _uiState.update { current ->
                        current.copy(
                            notifications = currentList,
                            unreadCount = currentList.count { !it.isRead }
                        )
                    }
                }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
