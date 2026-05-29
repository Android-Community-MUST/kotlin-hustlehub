package must.kdroiders.hustlehub.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import must.kdroiders.hustlehub.data.model.User
import must.kdroiders.hustlehub.data.model.UserRole
import must.kdroiders.hustlehub.data.repository.UserRepository
import must.kdroiders.hustlehub.datastore.UserPreferences
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
class SplashViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth?,
    private val userPreferences: UserPreferences,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _destination =
        MutableStateFlow<SplashDestination?>(null)
    val destination: StateFlow<SplashDestination?> =
        _destination.asStateFlow()

    init {
        determineDestination()
    }

    private fun determineDestination() {
        viewModelScope.launch {
            // run minimum delay and auth check in parallel
            val minDelayJob = async {
                delay(MIN_SPLASH_DURATION_MS)
            }

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
                        if (firebaseAuth == null)
                            "unavailable"
                        else "ready",
                        currentUser?.email ?: "logged out"
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

                                hasProfileResult.onSuccess { hasProfile ->
                                    if (!hasProfile) {
                                        val defaultUser = User(
                                            id = currentUser.uid,
                                            name = currentUser.displayName ?: "Student",
                                            email = currentUser.email ?: "",
                                            phone = "",
                                            campusLocation = "",
                                            role = UserRole.CUSTOMER,
                                            profilePhotoUrl = currentUser.photoUrl?.toString()
                                                ?: "",
                                            bio = "",
                                            isVerified = false,
                                            isOnline = true
                                        )
                                        val saveResult = userRepository.saveUserProfile(defaultUser)
                                        saveResult.onFailure { saveException ->
                                            if (saveException is retrofit2.HttpException) {
                                                firebaseAuth.signOut()
                                                targetDestination = SplashDestination.Login
                                            } else {
                                                targetDestination = SplashDestination.Home
                                            }
                                        }
                                    }
                                }.onFailure { e ->
                                    if (e is retrofit2.HttpException) {
                                        if (e.code() == 404 || e.code() == 403 || e.code() == 401) {
                                            val defaultUser = User(
                                                id = currentUser.uid,
                                                name = currentUser.displayName ?: "Student",
                                                email = currentUser.email ?: "",
                                                phone = "",
                                                campusLocation = "",
                                                role = UserRole.CUSTOMER,
                                                profilePhotoUrl = currentUser.photoUrl?.toString() ?: "",
                                                bio = "",
                                                isVerified = false,
                                                isOnline = true
                                            )
                                            val saveResult = userRepository.saveUserProfile(defaultUser)
                                            saveResult.onFailure {
                                                firebaseAuth.signOut()
                                                targetDestination = SplashDestination.Login
                                            }
                                        } else {
                                            firebaseAuth.signOut()
                                            targetDestination = SplashDestination.Login
                                        }
                                    } else {
                                        targetDestination = SplashDestination.Home
                                    }
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
                        "Error reading preferences"
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
