package must.kdroiders.hustlehub.ui.features.profilesetup.presentation.viewModel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import must.kdroiders.hustlehub.data.repository.MediaRepository
import javax.inject.Inject

// You will need to create this state class if it doesn't exist
data class ProfileSetupState(
    val name: String = "",
    val phone: String = "",
    val bio: String = "",
    val campusLocation: String = "",
    val photoUri: Uri? = null,
    val isUploadingPhoto: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null
)

sealed class ProfileSetupEvent {
    object ProfileSaved : ProfileSetupEvent()
}

@HiltViewModel
class ProfileSetupViewModel @Inject constructor(
    private val mediaRepository: MediaRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileSetupState())
    val state = _state.asStateFlow()

    private val _events = MutableSharedFlow<ProfileSetupEvent>()
    val events = _events.asSharedFlow()

    fun onPhotoSelected(uri: Uri) {
        viewModelScope.launch {
            _state.update { it.copy(isUploadingPhoto = true, errorMessage = null) }
            try {
                val url = mediaRepository.uploadUserMedia(uri, "profile.jpg")
                _state.update { it.copy(photoUri = Uri.parse(url), isUploadingPhoto = false) }
            } catch (e: Exception) {
                _state.update { it.copy(isUploadingPhoto = false, errorMessage = e.message) }
            }
        }
    }

    fun onNameChange(newName: String) { _state.update { it.copy(name = newName) } }
    fun onPhoneChange(newPhone: String) { _state.update { it.copy(phone = newPhone) } }
    fun onBioChange(newBio: String) { _state.update { it.copy(bio = newBio) } }
    fun onCampusLocationChange(newLocation: String) { _state.update { it.copy(campusLocation = newLocation) } }

    fun saveProfile() {
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            // Add your API save logic here
            _state.update { it.copy(isSaving = false) }
            _events.emit(ProfileSetupEvent.ProfileSaved)
        }
    }
}
