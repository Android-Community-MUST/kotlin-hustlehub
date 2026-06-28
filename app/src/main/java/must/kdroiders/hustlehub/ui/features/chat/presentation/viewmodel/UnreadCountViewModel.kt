package must.kdroiders.hustlehub.ui.features.chat.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import must.kdroiders.hustlehub.ui.features.chat.data.local.dao.ConversationDao
import javax.inject.Inject

/**
 * Lightweight ViewModel scoped to the main shell that exposes the total
 * unread message count across all conversations.
 *
 * Consumed by [MainShellScreen] to drive the bottom navigation badge.
 * Using a dedicated ViewModel (rather than collecting directly in the
 * composable) ensures the Flow survives recomposition and tab switches.
 */
@HiltViewModel
class UnreadCountViewModel
    @Inject
    constructor(
        conversationDao: ConversationDao,
    ) : ViewModel() {
        val totalUnreadCount: StateFlow<Int> =
            conversationDao
                .getTotalUnreadCount()
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
                    initialValue = 0,
                )
    }
