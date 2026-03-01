package must.kdroiders.hustlehub.ui.features.profilesetup.presentation.viewModel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import must.kdroiders.hustlehub.data.model.User
import must.kdroiders.hustlehub.data.model.UserRole
import must.kdroiders.hustlehub.data.repository.UserRepository
import timber.log.Timber
import javax.inject.Inject

/**
 * Holds the state of every field on the
 * profile setup form.
 */
data class ProfileSetupState(
    val name: String = "",
    val course: String = "",
    val yearOfStudy: Int = 1,
    val hostel: String = "",
    val role: UserRole = UserRole.CUSTOMER,
    val photoUri: Uri? = null,
    val photoUrl: String = "",
    val isUploadingPhoto: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null
)

/**
 * One-shot events that shouldn't survive recomposition.
 * Using SharedFlow instead of state so the event is
 * consumed exactly once.
 */
sealed interface ProfileSetupEvent {
    data object ProfileSaved : ProfileSetupEvent
}

@HiltViewModel
class ProfileSetupViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth?,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileSetupState())
    val state: StateFlow<ProfileSetupState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<ProfileSetupEvent>()
    val events: SharedFlow<ProfileSetupEvent> = _events.asSharedFlow()

    init {
        // Pre-fill name from Firebase Auth if available
        val currentUser = firebaseAuth?.currentUser
        _state.update {
            it.copy(
                name = currentUser?.displayName ?: ""
            )
        }
    }

    // ---- form field updates ----

    fun onNameChange(value: String) {
        _state.update { it.copy(name = value) }
    }

    fun onCourseChange(value: String) {
        _state.update { it.copy(course = value) }
    }

    fun onYearChange(year: Int) {
        _state.update { it.copy(yearOfStudy = year) }
    }

    fun onHostelChange(value: String) {
        _state.update { it.copy(hostel = value) }
    }

    fun onRoleChange(role: UserRole) {
        _state.update { it.copy(role = role) }
    }

    // ---- photo handling ----

    /**
     * Called when user picks a photo from camera or gallery.
     * Immediately starts uploading in the background.
     */
    fun onPhotoSelected(uri: Uri) {
        val userId = firebaseAuth?.currentUser?.uid ?: return

        _state.update {
            it.copy(
                photoUri = uri,
                isUploadingPhoto = true,
                errorMessage = null
            )
        }

        viewModelScope.launch {
            userRepository.uploadProfilePhoto(userId, uri)
                .onSuccess { downloadUrl ->
                    _state.update {
                        it.copy(
                            photoUrl = downloadUrl,
                            isUploadingPhoto = false
                        )
                    }
                    Timber.d("Photo uploaded: $downloadUrl")
                }
                .onFailure { e ->
                    _state.update {
                        it.copy(
                            isUploadingPhoto = false,
                            errorMessage =
                                "Photo upload failed: ${e.message}"
                        )
                    }
                }
        }
    }

    // ---- save profile ----

    fun saveProfile() {
        val currentState = _state.value
        val userId = firebaseAuth?.currentUser?.uid
        val email = firebaseAuth?.currentUser?.email ?: ""

        // Validation
        if (userId == null) {
            _state.update {
                it.copy(errorMessage = "You must be logged in")
            }
            return
        }
        if (currentState.name.isBlank()) {
            _state.update {
                it.copy(errorMessage = "Name is required")
            }
            return
        }
        if (currentState.course.isBlank()) {
            _state.update {
                it.copy(errorMessage = "Please select a course")
            }
            return
        }
        if (currentState.hostel.isBlank()) {
            _state.update {
                it.copy(
                    errorMessage = "Hostel/Residence is required"
                )
            }
            return
        }
        if (currentState.isUploadingPhoto) {
            _state.update {
                it.copy(
                    errorMessage = "Please wait for photo upload"
                )
            }
            return
        }

        _state.update {
            it.copy(isSaving = true, errorMessage = null)
        }

        val user = User(
            id = userId,
            name = currentState.name,
            email = email,
            course = currentState.course,
            yearOfStudy = currentState.yearOfStudy,
            hostel = currentState.hostel,
            role = currentState.role,
            profilePhotoUrl = currentState.photoUrl
        )

        viewModelScope.launch {
            userRepository.saveUserProfile(user)
                .onSuccess {
                    _state.update { it.copy(isSaving = false) }
                    _events.emit(ProfileSetupEvent.ProfileSaved)
                }
                .onFailure { e ->
                    _state.update {
                        it.copy(
                            isSaving = false,
                            errorMessage =
                                "Save failed: ${e.message}"
                        )
                    }
                }
        }
    }

    fun clearError() {
        _state.update { it.copy(errorMessage = null) }
    }
}
