package must.kdroiders.hustlehub.ui.features.settings.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import must.kdroiders.hustlehub.ui.features.profile.domain.model.User
import must.kdroiders.hustlehub.ui.features.profile.domain.repository.UserRepository
import timber.log.Timber
import javax.inject.Inject

data class BlockedUsersUiState(
    val blockedUsers: List<User> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

@HiltViewModel
class BlockedUsersViewModel
    @Inject
    constructor(
        private val userRepository: UserRepository,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(BlockedUsersUiState())
        val uiState: StateFlow<BlockedUsersUiState> = _uiState.asStateFlow()

        init {
            loadBlockedUsers()
        }

        fun loadBlockedUsers() {
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                userRepository
                    .getBlockedUsers()
                    .onSuccess { users ->
                        _uiState.update { it.copy(blockedUsers = users, isLoading = false) }
                    }.onFailure { e ->
                        Timber.e(e, "Failed to load blocked users")
                        _uiState.update { it.copy(isLoading = false, errorMessage = "Failed to load blocked users") }
                    }
            }
        }

        fun unblockUser(userId: String) {
            viewModelScope.launch {
                userRepository
                    .unblockUser(userId)
                    .onSuccess {
                        _uiState.update { state ->
                            state.copy(blockedUsers = state.blockedUsers.filter { it.uuid != userId && it.id != userId })
                        }
                    }.onFailure { e ->
                        Timber.e(e, "Failed to unblock user $userId")
                    }
            }
        }
    }
