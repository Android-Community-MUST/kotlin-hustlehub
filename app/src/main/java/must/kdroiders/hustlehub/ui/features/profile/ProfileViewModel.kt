package must.kdroiders.hustlehub.ui.features.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import must.kdroiders.hustlehub.data.model.Service
import must.kdroiders.hustlehub.data.repository.UserRepository
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth?,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        val userId = firebaseAuth?.currentUser?.uid
        if (userId == null) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    error = "Not signed in"
                )
            }
            return
        }

        viewModelScope.launch {
            userRepository.getUserProfile(userId)
                .onSuccess { user ->
                    _uiState.update {
                        it.copy(
                            user = user,
                            isLoading = false,
                            // Demo data — replace with real
                            // repository calls when backend ready
                            hustleScore = 4.7f,
                            reviewCount = 47,
                            badges = listOf(
                                Badge("Top Rated", BadgeType.GOLD),
                                Badge(
                                    "Fast Responder",
                                    BadgeType.GREEN
                                ),
                                Badge("Verified", BadgeType.BLUE)
                            ),
                            services = sampleServices()
                        )
                    }
                }
                .onFailure { e ->
                    Timber.e(e, "Failed to load profile")
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = e.message
                        )
                    }
                }
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
        _uiState.update {
            it.copy(isLoading = true, error = null)
        }
        loadProfile()
    }

    /**
     * Placeholder services until backend is wired up.
     */
    private fun sampleServices(): List<Service> = listOf(
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
    )
}
