package must.kdroiders.hustlehub.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import must.kdroiders.hustlehub.sharedPrefs.UserPreferences
import timber.log.Timber
import javax.inject.Inject

/**
 * Represents the destination the splash screen should navigate to.
 */
sealed interface SplashDestination {
    data object Home : SplashDestination
    data object Login : SplashDestination
    data object Onboarding : SplashDestination
}

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth?,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _destination = MutableStateFlow<SplashDestination?>(null)
    val destination: StateFlow<SplashDestination?> = _destination.asStateFlow()

    init {
        determineDestination()
    }

    private fun determineDestination() {
        viewModelScope.launch {
            // Run minimum delay and auth check in parallel
            val minDelayJob = async { delay(MIN_SPLASH_DURATION_MS) }

            val destinationResult = async {
                val isFirstLaunch = userPreferences.isFirstLaunch.first()
                val currentUser = firebaseAuth?.currentUser

                Timber.d(
                    "Splash check — isFirstLaunch: %s, currentUser: %s",
                    isFirstLaunch,
                    currentUser?.email ?: "null (Firebase unavailable or logged out)"
                )

                when {
                    isFirstLaunch -> {
                        userPreferences.setFirstLaunchComplete()
                        SplashDestination.Onboarding
                    }
                    currentUser != null -> SplashDestination.Home
                    else -> SplashDestination.Login
                }
            }

            // Wait for both to complete
            minDelayJob.await()
            _destination.value = destinationResult.await()
        }
    }

    companion object {
        private const val MIN_SPLASH_DURATION_MS = 2000L
    }
}
