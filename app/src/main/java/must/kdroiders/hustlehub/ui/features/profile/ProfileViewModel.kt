package must.kdroiders.hustlehub.ui.features.profile

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import must.kdroiders.hustlehub.data.model.Service
import must.kdroiders.hustlehub.data.model.User
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadMockProfile()
    }

    // Mock data — swap with real repo calls when backend is ready
    private fun loadMockProfile() {
        _uiState.update {
            it.copy(
                user = User(
                    id = "mock_1",
                    name = "Wanjiku M.",
                    course = "Computer Sci",
                    yearOfStudy = 3,
                    campus = "Nairobi Campus",
                    profilePhotoUrl = "",
                    isVerified = true
                ),
                hustleScore = 4.7f,
                reviewCount = 47,
                badges = listOf(
                    Badge("Top Rated", BadgeType.GOLD),
                    Badge("Fast Responder", BadgeType.GREEN),
                    Badge("Verified", BadgeType.BLUE)
                ),
                services = listOf(
                    Service(
                        id = "1",
                        title = "Professional Braiding",
                        priceRange = "KES 800 - 1500",
                        isActive = true,
                        tags = listOf("Beauty", "On-Campus")
                    ),
                    Service(
                        id = "2",
                        title = "Hostel Laundry Service",
                        priceRange = "KES 500/load",
                        isActive = false,
                        tags = listOf("Cleaning", "Pickup")
                    ),
                    Service(
                        id = "3",
                        title = "Notes Transcription",
                        priceRange = "KES 200/page",
                        isActive = true,
                        tags = listOf("Academic", "Remote")
                    )
                ),
                isLoading = false,
                error = null
            )
        }
    }

    fun toggleServiceActive(serviceId: String) {
        _uiState.update { state ->
            state.copy(
                services = state.services.map { svc ->
                    if (svc.id == serviceId) {
                        svc.copy(isActive = !svc.isActive)
                    } else {
                        svc
                    }
                }
            )
        }
    }

    fun retry() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        loadMockProfile()
    }
}
