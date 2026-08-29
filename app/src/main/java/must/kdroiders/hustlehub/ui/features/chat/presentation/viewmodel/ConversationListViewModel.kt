package must.kdroiders.hustlehub.ui.features.chat.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import must.kdroiders.hustlehub.ui.features.chat.domain.model.Conversation
import must.kdroiders.hustlehub.ui.features.chat.domain.repository.ChatRepository
import javax.inject.Inject

enum class ConversationFilter(val label: String) {
    ALL("All Chats"),
    UNREAD("Unread"),
    SERVICES("Services"),
    ARCHIVED("Archived"),
}

data class ConversationListUiState(
    val conversations: List<Conversation> = emptyList(),
    val searchQuery: String = "",
    val selectedFilter: ConversationFilter = ConversationFilter.ALL,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
) {
    val filteredConversations: List<Conversation>
        get() = conversations.filter { conversation ->
            // Filter by selected tab category
            val matchesFilter = when (selectedFilter) {
                ConversationFilter.ALL -> !conversation.isArchived
                ConversationFilter.UNREAD -> conversation.unreadCount > 0 && !conversation.isArchived
                ConversationFilter.SERVICES -> !conversation.serviceId.isNullOrBlank() && !conversation.isArchived
                ConversationFilter.ARCHIVED -> conversation.isArchived
            }

            // Filter by search query
            val matchesQuery = if (searchQuery.isBlank()) {
                true
            } else {
                val query = searchQuery.trim().lowercase()
                conversation.otherUserName.lowercase().contains(query) ||
                    (conversation.lastMessage?.lowercase()?.contains(query) == true)
            }

            matchesFilter && matchesQuery
        }
}

@HiltViewModel
class ConversationListViewModel
    @Inject
    constructor(
        private val chatRepository: ChatRepository,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(ConversationListUiState())
        val uiState: StateFlow<ConversationListUiState> = _uiState.asStateFlow()

        init {
            observeConversations()
            refreshConversations()
        }

        private fun observeConversations() {
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true) }
                chatRepository.getConversations().collect { conversations ->
                    _uiState.update { it.copy(conversations = conversations, isLoading = false) }
                }
            }
        }

        fun onSearchQueryChanged(query: String) {
            _uiState.update { it.copy(searchQuery = query) }
        }

        fun onFilterSelected(filter: ConversationFilter) {
            _uiState.update { it.copy(selectedFilter = filter) }
        }

        fun toggleArchiveConversation(conversationId: String) {
            _uiState.update { currentState ->
                val updatedList = currentState.conversations.map { conv ->
                    if (conv.id == conversationId) {
                        conv.copy(isArchived = !conv.isArchived)
                    } else {
                        conv
                    }
                }
                currentState.copy(conversations = updatedList)
            }
        }

        fun refreshConversations() {
            viewModelScope.launch {
                _uiState.update { it.copy(isRefreshing = true, error = null) }
                chatRepository
                    .refreshConversations()
                    .onSuccess {
                        _uiState.update { it.copy(isRefreshing = false) }
                    }.onFailure { error ->
                        _uiState.update { it.copy(isRefreshing = false, error = error.message ?: "Failed to refresh") }
                    }
            }
        }

        fun deleteConversation(conversationId: String) {
            viewModelScope.launch {
                chatRepository
                    .deleteConversation(conversationId)
                    .onFailure { error ->
                        _uiState.update { it.copy(error = error.message ?: "Failed to delete conversation") }
                    }
            }
        }

        public override fun onCleared() {
            super.onCleared()
        }
    }
