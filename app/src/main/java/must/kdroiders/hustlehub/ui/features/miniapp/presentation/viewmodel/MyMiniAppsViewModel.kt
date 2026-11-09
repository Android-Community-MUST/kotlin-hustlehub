package must.kdroiders.hustlehub.ui.features.miniapp.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import must.kdroiders.hustlehub.ui.features.miniapp.data.remote.dto.MiniAppRemoteDto
import must.kdroiders.hustlehub.ui.features.miniapp.domain.repository.MiniAppRepository
import javax.inject.Inject

data class MyMiniAppsUiState(
    val apps: List<MiniAppRemoteDto> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isDeletingId: String? = null,
    val error: String? = null,
    val successMessage: String? = null,
)

@HiltViewModel
class MyMiniAppsViewModel
    @Inject
    constructor(
        private val repository: MiniAppRepository,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(MyMiniAppsUiState())
        val uiState: StateFlow<MyMiniAppsUiState> = _uiState.asStateFlow()

        init {
            loadMyApps()
        }

        fun loadMyApps(isRefresh: Boolean = false) {
            _uiState.update {
                if (isRefresh) {
                    it.copy(isRefreshing = true, error = null)
                } else {
                    it.copy(isLoading = true, error = null)
                }
            }
            viewModelScope.launch {
                repository.getMyMiniApps().fold(
                    onSuccess = { list ->
                        _uiState.update {
                            it.copy(
                                apps = list,
                                isLoading = false,
                                isRefreshing = false,
                            )
                        }
                    },
                    onFailure = { e ->
                        _uiState.update {
                            it.copy(
                                error = e.localizedMessage ?: "Failed to load mini-apps",
                                isLoading = false,
                                isRefreshing = false,
                            )
                        }
                    },
                )
            }
        }

        fun deleteMiniApp(id: String) {
            _uiState.update { it.copy(isDeletingId = id, error = null) }
            viewModelScope.launch {
                repository.deleteMiniApp(id).fold(
                    onSuccess = {
                        _uiState.update {
                            it.copy(
                                isDeletingId = null,
                                successMessage = "Mini-app deleted successfully",
                            )
                        }
                        loadMyApps()
                    },
                    onFailure = { e ->
                        _uiState.update {
                            it.copy(
                                isDeletingId = null,
                                error = e.localizedMessage ?: "Failed to delete mini-app",
                            )
                        }
                    },
                )
            }
        }

        fun clearMessages() {
            _uiState.update { it.copy(error = null, successMessage = null) }
        }
    }
