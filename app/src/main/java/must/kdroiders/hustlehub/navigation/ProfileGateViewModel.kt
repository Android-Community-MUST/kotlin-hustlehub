package must.kdroiders.hustlehub.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import must.kdroiders.hustlehub.datastore.UserPreferences
import must.kdroiders.hustlehub.ui.features.profile.domain.model.User
import must.kdroiders.hustlehub.ui.features.profile.domain.repository.UserRepository
import javax.inject.Inject

@HiltViewModel
class ProfileGateViewModel
    @Inject
    constructor(
        private val userPreferences: UserPreferences,
        private val userRepository: UserRepository,
    ) : ViewModel() {
        val cachedUser: StateFlow<User> = userPreferences.cachedUser.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = User(),
        )

        suspend fun saveLocation(
            campusLocation: String,
            phone: String,
            bio: String,
        ): Result<User> {
            val user = cachedUser.value
            val currentName = user.name.ifBlank {
                userRepository.getUserProfile(user.id).getOrNull()?.name.orEmpty()
            }
            return userRepository
                .updateProfile(
                    name = currentName,
                    bio = bio.ifBlank { user.bio },
                    phone = phone.ifBlank { user.phone },
                    campusLocation = campusLocation,
                    avatarUrl = user.profilePhotoUrl.takeIf { it.isNotBlank() },
                    allowCalls = user.allowCalls,
                ).also { result ->
                    result.onSuccess { userPreferences.writeUser(it) }
                }
        }
    }
