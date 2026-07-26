package must.kdroiders.hustlehub.ui.features.profile.presentation.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import must.kdroiders.hustlehub.ui.features.auth.domain.repository.AuthRepository
import must.kdroiders.hustlehub.ui.features.profile.domain.repository.UserRepository
import must.kdroiders.hustlehub.ui.features.profile.domain.usecase.UpdateProfileUseCase
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class EditProfileViewModel
    @Inject
    constructor(
        private val authRepository: AuthRepository,
        private val userRepository: UserRepository,
        private val updateProfileUseCase: UpdateProfileUseCase,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(EditProfileUiState())
        val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

        init {
            loadCurrentUser()
        }

        private fun loadCurrentUser() {
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true) }
                val uid = authRepository.getCurrentUser()?.uid ?: run {
                    _uiState.update { it.copy(isLoading = false, error = "Not logged in") }
                    return@launch
                }

                userRepository
                    .getUserProfile(uid)
                    .onSuccess { user ->
                        _uiState.update {
                            it.copy(
                                user = user,
                                name = user?.name ?: "",
                                bio = user?.bio ?: "",
                                phone = user?.phone ?: "",
                                campusLocation = user?.campusLocation ?: "",
                                allowCalls = user?.allowCalls ?: false,
                                isLoading = false,
                            )
                        }
                    }.onFailure { e ->
                        Timber.e(e, "EditProfileViewModel: failed to load user")
                        _uiState.update { it.copy(isLoading = false, error = "Failed to load profile.") }
                    }
            }
        }

        fun onNameChanged(value: String) = _uiState.update { it.copy(name = value) }
        fun onBioChanged(value: String) = _uiState.update { it.copy(bio = value) }
        fun onPhoneChanged(value: String) = _uiState.update { it.copy(phone = value) }
        fun onCampusLocationChanged(value: String) = _uiState.update { it.copy(campusLocation = value) }
        fun onAllowCallsChanged(value: Boolean) = _uiState.update { it.copy(allowCalls = value) }
        fun onAvatarPicked(uri: Uri) = _uiState.update { it.copy(pendingAvatarUri = uri.toString()) }
        fun clearError() = _uiState.update { it.copy(error = null) }

        fun save() {
            val state = _uiState.value
            if (state.name.isBlank()) {
                _uiState.update { it.copy(error = "Name cannot be empty.") }
                return
            }

            viewModelScope.launch {
                _uiState.update { it.copy(isSaving = true, error = null) }

                // Upload photo first if a new one was picked
                var newAvatarUrl: String? = null
                val pendingUri = state.pendingAvatarUri
                if (pendingUri != null) {
                    val uid = authRepository.getCurrentUser()?.uid ?: ""
                    userRepository
                        .uploadProfilePhoto(uid, Uri.parse(pendingUri))
                        .onSuccess { url -> newAvatarUrl = url }
                        .onFailure { e ->
                            Timber.e(e, "EditProfileViewModel: photo upload failed")
                            _uiState.update { it.copy(isSaving = false, error = "Photo upload failed. Please try again.") }
                            return@launch
                        }
                }

                updateProfileUseCase(
                    name = state.name.trim(),
                    bio = state.bio.trim(),
                    phone = state.phone.trim(),
                    campusLocation = state.campusLocation.trim(),
                    avatarUrl = newAvatarUrl,
                    allowCalls = state.allowCalls,
                ).onSuccess {
                    _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
                }.onFailure { e ->
                    Timber.e(e, "EditProfileViewModel: save failed")
                    _uiState.update { it.copy(isSaving = false, error = "Failed to save profile. Please try again.") }
                }
            }
        }
    }
