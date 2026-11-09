package must.kdroiders.hustlehub.ui.features.miniapp.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import must.kdroiders.hustlehub.ui.features.miniapp.data.remote.MiniAppApiService
import must.kdroiders.hustlehub.ui.features.miniapp.data.remote.dto.CreateMiniAppRequestDto
import javax.inject.Inject

data class SubmitMiniAppUiState(
    val title: String = "",
    val appId: String = "",
    val entryUrl: String = "",
    val description: String = "",
    val category: String = "SERVICES",
    val iconUrl: String = "",
    val coverPhotoUrl: String = "",
    val isAppIdManuallyEdited: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
)

@HiltViewModel
class SubmitMiniAppViewModel
    @Inject
    constructor(
        private val apiService: MiniAppApiService,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(SubmitMiniAppUiState())
        val uiState: StateFlow<SubmitMiniAppUiState> = _uiState.asStateFlow()

        fun onTitleChange(newTitle: String) {
            _uiState.update { current ->
                val updatedAppId = if (!current.isAppIdManuallyEdited) {
                    slugify(newTitle)
                } else {
                    current.appId
                }
                current.copy(
                    title = newTitle,
                    appId = updatedAppId,
                    error = null,
                )
            }
        }

        fun onAppIdChange(newAppId: String) {
            _uiState.update { current ->
                current.copy(
                    appId = newAppId.lowercase().replace(" ", "-"),
                    isAppIdManuallyEdited = true,
                    error = null,
                )
            }
        }

        fun onEntryUrlChange(newUrl: String) {
            _uiState.update { it.copy(entryUrl = newUrl, error = null) }
        }

        fun onDescriptionChange(newDescription: String) {
            _uiState.update { it.copy(description = newDescription, error = null) }
        }

        fun onCategoryChange(newCategory: String) {
            _uiState.update { it.copy(category = newCategory) }
        }

        fun onIconUrlChange(newIconUrl: String) {
            _uiState.update { it.copy(iconUrl = newIconUrl) }
        }

        fun onCoverPhotoUrlChange(newCoverUrl: String) {
            _uiState.update { it.copy(coverPhotoUrl = newCoverUrl) }
        }

        fun clearError() {
            _uiState.update { it.copy(error = null) }
        }

        fun submit() {
            val state = _uiState.value
            val trimmedTitle = state.title.trim()
            val trimmedAppId = state.appId.trim()
            val trimmedUrl = state.entryUrl.trim()
            val trimmedDesc = state.description.trim()

            if (trimmedTitle.length < 2) {
                _uiState.update { it.copy(error = "Title must be at least 2 characters") }
                return
            }

            if (!trimmedAppId.matches(Regex("^[a-z0-9-]+$"))) {
                _uiState.update { it.copy(error = "App ID can only contain lowercase letters, numbers, and hyphens") }
                return
            }

            if (!trimmedUrl.startsWith("https://")) {
                _uiState.update { it.copy(error = "Entry URL must be a valid secure HTTPS link (https://...)") }
                return
            }

            if (trimmedDesc.isBlank()) {
                _uiState.update { it.copy(error = "Description cannot be empty") }
                return
            }

            _uiState.update { it.copy(isLoading = true, error = null) }

            viewModelScope.launch {
                try {
                    val request = CreateMiniAppRequestDto(
                        appId = trimmedAppId,
                        title = trimmedTitle,
                        description = trimmedDesc,
                        entryUrl = trimmedUrl,
                        iconUrl = state.iconUrl.trim().ifBlank { null },
                        coverPhotoUrl = state.coverPhotoUrl.trim().ifBlank { null },
                        category = state.category,
                    )
                    val response = apiService.applyForMiniApp(request)
                    if (response.success && response.data != null) {
                        _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                    } else {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = response.message.ifBlank { "Failed to submit mini-app" },
                            )
                        }
                    }
                } catch (e: Exception) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = e.localizedMessage ?: "Network error submitting application",
                        )
                    }
                }
            }
        }

        private fun slugify(input: String): String {
            return input
                .lowercase()
                .trim()
                .replace(Regex("[^a-z0-9]+"), "-")
                .trim('-')
        }
    }
