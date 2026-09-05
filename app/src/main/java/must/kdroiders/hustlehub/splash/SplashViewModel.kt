package must.kdroiders.hustlehub.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import must.kdroiders.hustlehub.data.local.AppDatabase
import must.kdroiders.hustlehub.datastore.UserPreferences
import must.kdroiders.hustlehub.ui.features.profile.domain.model.User
import must.kdroiders.hustlehub.ui.features.profile.domain.repository.UserRepository
import timber.log.Timber
import javax.inject.Inject

/**
 * Represents the destination the splash screen
 * should navigate to.
 */
sealed interface SplashDestination {
    data object Home : SplashDestination
    data object Login : SplashDestination
    data object Onboarding : SplashDestination
    data object ProfileSetup : SplashDestination
}

@HiltViewModel
class SplashViewModel
    @Inject
    constructor(
        private val firebaseAuth: FirebaseAuth?,
        private val userPreferences: UserPreferences,
        private val userRepository: UserRepository,
        private val appDatabase: AppDatabase,
    ) : ViewModel() {
        private val _destination =
            MutableStateFlow<SplashDestination?>(null)
        val destination: StateFlow<SplashDestination?> =
            _destination.asStateFlow()

        init {
            determineDestination()
        }

        private fun uploadFcmToken() {
            viewModelScope.launch {
                try {
                    @Suppress("DEPRECATION")
                    val token = FirebaseMessaging
                        .getInstance()
                        .token
                        .await()
                    if (token.isNullOrBlank()) return@launch
                    userRepository.updateFcmToken(token)
                    Timber.d("Successfully updated FCM token on splash")
                } catch (e: Exception) {
                    Timber.e(e, "Failed to retrieve/upload FCM token on splash")
                }
            }
        }

        private fun determineDestination() {
            viewModelScope.launch {
                if (userPreferences.hasPendingDeletion.first()) {
                    Timber.w("Pending deletion detected on startup — completing local cleanup")
                    withContext(Dispatchers.IO) { appDatabase.clearAllTables() }
                    userPreferences.clearUser()
                    userPreferences.clearPendingDeletion()
                    firebaseAuth?.signOut()
                    _destination.value = SplashDestination.Onboarding
                    return@launch
                }

                val minDelayJob = async { delay(MIN_SPLASH_DURATION_MS) }

                val destinationResult = async {
                    try {
                        val isFirstLaunch =
                            userPreferences.isFirstLaunch.first()
                        val currentUser =
                            firebaseAuth?.currentUser

                        Timber.d(
                            "Splash — isFirstLaunch: %s, " +
                                "firebaseAuth: %s, user: %s",
                            isFirstLaunch,
                            if (firebaseAuth == null) {
                                "unavailable"
                            } else {
                                "ready"
                            },
                            currentUser?.email ?: "logged out",
                        )

                        when {
                            isFirstLaunch ->
                                SplashDestination.Onboarding

                            currentUser != null -> {
                                // Reload to get latest verified status
                                try {
                                    currentUser.reload().await()
                                } catch (e: Exception) {
                                    Timber.e(e, "Failed to reload user in splash screen")
                                }

                                if (currentUser.isEmailVerified) {
                                    val hasProfileResult = userRepository.hasUserProfile(currentUser.uid)
                                    var targetDestination: SplashDestination = SplashDestination.Home

                                    hasProfileResult
                                        .onSuccess { hasProfile ->
                                            if (!hasProfile) {
                                                val basicUser = User(
                                                    id = currentUser.uid,
                                                    email = currentUser.email ?: "",
                                                    name = currentUser.displayName ?: "Hustler",
                                                )
                                                viewModelScope.launch {
                                                    userRepository.saveUserProfile(basicUser)
                                                }
                                            }
                                        }.onFailure { e ->
                                            if (e is retrofit2.HttpException && (e.code() == 401 || e.code() == 403)) {
                                                firebaseAuth.signOut()
                                                targetDestination = SplashDestination.Login
                                            } else {
                                                // Trigger auto-registration attempt in background for non-auth errors/missing user
                                                val basicUser = User(
                                                    id = currentUser.uid,
                                                    email = currentUser.email ?: "",
                                                    name = currentUser.displayName ?: "Hustler",
                                                )
                                                viewModelScope.launch {
                                                    userRepository.saveUserProfile(basicUser)
                                                }
                                            }
                                        }
                                    if (targetDestination == SplashDestination.Home) {
                                        uploadFcmToken()
                                    }
                                    targetDestination
                                } else {
                                    SplashDestination.Login
                                }
                            }

                            else ->
                                SplashDestination.Login
                        }
                    } catch (e: Exception) {
                        // rethrow cancellation to preserve
                        // structured concurrency
                        coroutineContext.ensureActive()
                        // fall back to Login so the app
                        // never gets stuck on splash
                        Timber.e(
                            e,
                            "Error reading preferences",
                        )
                        SplashDestination.Login
                    }
                }

                // wait for both to complete
                minDelayJob.await()
                _destination.value =
                    destinationResult.await()
            }
        }

        companion object {
            private const val MIN_SPLASH_DURATION_MS = 2000L
        }
    }
