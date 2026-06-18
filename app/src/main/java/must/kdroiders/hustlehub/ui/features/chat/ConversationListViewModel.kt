package must.kdroiders.hustlehub.ui.features.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import must.kdroiders.hustlehub.data.model.Conversation
import must.kdroiders.hustlehub.domain.repository.ChatRepository
import javax.inject.Inject

data class ConversationListUiState(
    val conversations: List<Conversation> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class ConversationListViewModel @Inject constructor(
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

    fun refreshConversations() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, error = null) }
            chatRepository.refreshConversations()
                .onSuccess {
                    _uiState.update { it.copy(isRefreshing = false) }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isRefreshing = false, error = error.message ?: "Failed to refresh") }
                }
        }
    }
}
