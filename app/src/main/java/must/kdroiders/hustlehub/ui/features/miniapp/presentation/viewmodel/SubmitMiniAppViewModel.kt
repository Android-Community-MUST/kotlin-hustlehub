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
import must.kdroiders.hustlehub.ui.features.miniapp.data.remote.dto.UpdateMiniAppRequestDto
import javax.inject.Inject

data class SubmitMiniAppUiState(
    val title: String = "",
    val titleError: String? = null,
    val appId: String = "",
    val appIdError: String? = null,
    val entryUrl: String = "",
    val entryUrlError: String? = null,
    val description: String = "",
    val descriptionError: String? = null,
    val category: String = "SERVICES",
    val iconUrl: String = "",
    val iconUrlError: String? = null,
    val coverPhotoUrl: String = "",
    val coverPhotoUrlError: String? = null,
    val isAppIdManuallyEdited: Boolean = false,
    val isEditMode: Boolean = false,
    val editingId: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
) {
    val isFormValid: Boolean
        get() = title.trim().length >= 2 &&
            titleError == null &&
            appId.trim().matches(Regex("^[a-z0-9-]+$")) &&
            appIdError == null &&
            entryUrl.trim().startsWith("https://") &&
            entryUrlError == null &&
            description.trim().length >= 10 &&
            descriptionError == null &&
            iconUrlError == null &&
            coverPhotoUrlError == null
}

@HiltViewModel
class SubmitMiniAppViewModel
    @Inject
    constructor(
        private val apiService: MiniAppApiService,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(SubmitMiniAppUiState())
        val uiState: StateFlow<SubmitMiniAppUiState> = _uiState.asStateFlow()

        fun loadForEdit(appId: String) {
            _uiState.update { it.copy(isLoading = true, isEditMode = true, error = null) }
            viewModelScope.launch {
                try {
                    val response = apiService.getMiniApp(appId)
                    val data = response.data
                    if (response.success && data != null) {
                        _uiState.update {
                            it.copy(
                                title = data.title,
                                appId = data.appId,
                                entryUrl = data.entryUrl,
                                description = data.description,
                                category = data.category,
                                iconUrl = data.iconUrl.orEmpty(),
                                coverPhotoUrl = data.coverPhotoUrl.orEmpty(),
                                isAppIdManuallyEdited = true,
                                isEditMode = true,
                                editingId = data.id,
                                isLoading = false,
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = response.message.ifBlank { "Could not load mini-app" },
                            )
                        }
                    }
                } catch (e: Exception) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = e.localizedMessage ?: "Network error loading mini-app",
                        )
                    }
                }
            }
        }

        fun onTitleChange(newTitle: String) {
            if (newTitle.length > 50) return
            _uiState.update { current ->
                val updatedAppId = if (!current.isAppIdManuallyEdited) {
                    slugify(newTitle)
                } else {
                    current.appId
                }
                val titleErr = when {
                    newTitle.isBlank() -> null
                    newTitle.trim().length < 2 -> "Name must be at least 2 characters"
                    else -> null
                }
                val appIdErr = if (!current.isAppIdManuallyEdited && updatedAppId.isNotEmpty()) {
                    validateAppId(updatedAppId)
                } else {
                    current.appIdError
                }
                current.copy(
                    title = newTitle,
                    titleError = titleErr,
                    appId = updatedAppId,
                    appIdError = appIdErr,
                    error = null,
                )
            }
        }

        fun onAppIdChange(newAppId: String) {
            if (_uiState.value.isEditMode || newAppId.length > 40) return
            val sanitized = newAppId.lowercase().replace(" ", "-")
            _uiState.update { current ->
                current.copy(
                    appId = sanitized,
                    appIdError = validateAppId(sanitized),
                    isAppIdManuallyEdited = true,
                    error = null,
                )
            }
        }

        fun onEntryUrlChange(newUrl: String) {
            _uiState.update { current ->
                val urlErr = when {
                    newUrl.isBlank() -> null
                    !newUrl.startsWith("https://") -> "Must be a secure HTTPS URL (https://...)"
                    newUrl.length < 10 -> "Please enter a valid URL"
                    else -> null
                }
                current.copy(
                    entryUrl = newUrl,
                    entryUrlError = urlErr,
                    error = null,
                )
            }
        }

        fun onDescriptionChange(newDescription: String) {
            if (newDescription.length > 300) return
            _uiState.update { current ->
                val descErr = when {
                    newDescription.isBlank() -> null
                    newDescription.trim().length < 10 -> "Description must be at least 10 characters"
                    else -> null
                }
                current.copy(
                    description = newDescription,
                    descriptionError = descErr,
                    error = null,
                )
            }
        }

        fun onCategoryChange(newCategory: String) {
            _uiState.update { it.copy(category = newCategory) }
        }

        fun onIconUrlChange(newIconUrl: String) {
            _uiState.update { current ->
                val iconErr = when {
                    newIconUrl.isBlank() -> null
                    !newIconUrl.startsWith("http://") && !newIconUrl.startsWith("https://") -> "Must be a valid image URL"
                    else -> null
                }
                current.copy(
                    iconUrl = newIconUrl,
                    iconUrlError = iconErr,
                )
            }
        }

        fun onCoverPhotoUrlChange(newCoverUrl: String) {
            _uiState.update { current ->
                val coverErr = when {
                    newCoverUrl.isBlank() -> null
                    !newCoverUrl.startsWith("http://") && !newCoverUrl.startsWith("https://") -> "Must be a valid image URL"
                    else -> null
                }
                current.copy(
                    coverPhotoUrl = newCoverUrl,
                    coverPhotoUrlError = coverErr,
                )
            }
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

            val titleErr = when {
                trimmedTitle.isBlank() -> "Mini-app name is required"
                trimmedTitle.length < 2 -> "Name must be at least 2 characters"
                else -> null
            }
            val appIdErr = validateAppId(trimmedAppId)
            val urlErr = when {
                trimmedUrl.isBlank() -> "Website URL is required"
                !trimmedUrl.startsWith("https://") -> "Must be a secure HTTPS URL (https://...)"
                trimmedUrl.length < 10 -> "Please enter a valid URL"
                else -> null
            }
            val descErr = when {
                trimmedDesc.isBlank() -> "Description is required"
                trimmedDesc.length < 10 -> "Description must be at least 10 characters"
                else -> null
            }

            if (titleErr != null || appIdErr != null || urlErr != null || descErr != null) {
                _uiState.update {
                    it.copy(
                        titleError = titleErr,
                        appIdError = appIdErr,
                        entryUrlError = urlErr,
                        descriptionError = descErr,
                    )
                }
                return
            }

            _uiState.update { it.copy(isLoading = true, error = null) }

            viewModelScope.launch {
                try {
                    if (state.isEditMode && state.editingId != null) {
                        val updateRequest = UpdateMiniAppRequestDto(
                            title = trimmedTitle,
                            description = trimmedDesc,
                            entryUrl = trimmedUrl,
                            iconUrl = state.iconUrl.trim().ifBlank { null },
                            coverPhotoUrl = state.coverPhotoUrl.trim().ifBlank { null },
                            category = state.category,
                        )
                        val response = apiService.updateMiniApp(state.editingId, updateRequest)
                        if (response.success && response.data != null) {
                            _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                        } else {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    error = response.message.ifBlank { "Failed to update mini-app" },
                                )
                            }
                        }
                    } else {
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

        private fun validateAppId(id: String): String? {
            return when {
                id.isBlank() -> "Unique App ID is required"
                id.length < 2 -> "App ID must be at least 2 characters"
                !id.matches(Regex("^[a-z0-9-]+$")) -> "Only lowercase letters, numbers, and hyphens allowed"
                else -> null
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
