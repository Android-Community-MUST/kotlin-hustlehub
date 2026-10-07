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
import must.kdroiders.hustlehub.core.auth.AdminAuthUtils
import must.kdroiders.hustlehub.core.deeplink.InstallReferrerManager
import must.kdroiders.hustlehub.core.security.KeyExchangeHandler
import must.kdroiders.hustlehub.data.local.AppDatabase
import must.kdroiders.hustlehub.datastore.UserPreferences
import must.kdroiders.hustlehub.navigation.DeepLinkAction
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
    data class AccountSuspended(
        val reason: String = "",
        val suspendedUntil: String? = null,
    ) : SplashDestination
}

@HiltViewModel
class SplashViewModel
    @Inject
    constructor(
        private val firebaseAuth: FirebaseAuth?,
        private val userPreferences: UserPreferences,
        private val userRepository: UserRepository,
        private val appDatabase: AppDatabase,
        private val keyExchangeHandler: KeyExchangeHandler,
        private val installReferrerManager: InstallReferrerManager,
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

        private fun syncUserPublicKey() {
            viewModelScope.launch {
                try {
                    keyExchangeHandler.syncUserPublicKey()
                    Timber.d("Successfully triggered identity public key sync on splash")
                } catch (e: Exception) {
                    Timber.w(e, "Failed to sync user public key on splash")
                }
            }
        }

        private fun determineDestination() {
            viewModelScope.launch {
                checkAndStoreDeferredDeepLink()
            }
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
                                try {
                                    currentUser.reload().await()
                                } catch (e: Exception) {
                                    Timber.e(e, "Failed to reload user in splash screen")
                                }

                                val isVerified = currentUser.isEmailVerified || AdminAuthUtils.isAuthorizedAdmin(currentUser.email)
                                if (isVerified) {
                                    val userProfileResult = userRepository.getUserProfile(currentUser.uid)
                                    var targetDestination: SplashDestination = SplashDestination.Home

                                    userProfileResult
                                        .onSuccess { user ->
                                            if (user == null) {
                                                targetDestination = SplashDestination.ProfileSetup
                                            }
                                        }.onFailure { e ->
                                            if (e is retrofit2.HttpException && e.code() == 403) {
                                                var suspendedReason = "Violation of terms of service."
                                                var suspendedUntil: String? = null
                                                try {
                                                    val body = e.response()?.errorBody()?.string() ?: ""
                                                    val reasonMatch = Regex("\"suspendedReason\"\\s*:\\s*\"([^\"]*)\"").find(body)
                                                    val untilMatch = Regex("\"suspendedUntil\"\\s*:\\s*\"([^\"]*)\"").find(body)
                                                    if (reasonMatch != null) suspendedReason = reasonMatch.groupValues[1]
                                                    if (untilMatch != null) suspendedUntil = untilMatch.groupValues[1].takeIf { it.isNotBlank() && it != "null" }
                                                } catch (_: Exception) {
                                                }
                                                targetDestination = SplashDestination.AccountSuspended(
                                                    reason = suspendedReason,
                                                    suspendedUntil = suspendedUntil,
                                                )
                                            } else if (e is retrofit2.HttpException && e.code() == 401) {
                                                firebaseAuth.signOut()
                                                targetDestination = SplashDestination.Login
                                            } else if (e is retrofit2.HttpException && e.code() == 404) {
                                                targetDestination = SplashDestination.ProfileSetup
                                            } else {
                                                Timber.w(e, "SplashViewModel: Transient network error on splash — proceeding with cached session")
                                                targetDestination = SplashDestination.Home
                                            }
                                        }
                                    if (targetDestination == SplashDestination.Home) {
                                        uploadFcmToken()
                                        syncUserPublicKey()
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
                        coroutineContext.ensureActive()
                        Timber.e(
                            e,
                            "Error reading preferences",
                        )
                        SplashDestination.Login
                    }
                }

                minDelayJob.await()
                _destination.value =
                    destinationResult.await()
            }
        }

        private suspend fun checkAndStoreDeferredDeepLink() {
            try {
                if (userPreferences.hasProcessedInstallReferrer.first()) {
                    Timber.tag("SHARE_LINK").d("[SHARE_LINK] Install referrer has already been processed previously; skipping")
                    return
                }
                Timber.tag("SHARE_LINK").d("[SHARE_LINK] Checking install referrer for deferred deep link...")
                val action = installReferrerManager.getDeferredDeepLink()
                if (action != null) {
                    val (target, id) = when (action) {
                        is DeepLinkAction.OpenProviderProfile -> "profile" to action.providerId
                        is DeepLinkAction.OpenServiceDetail -> "service" to action.serviceId
                        else -> null to null
                    }
                    if (target != null && id != null) {
                        userPreferences.savePendingDeepLink(target, id)
                        Timber.tag("SHARE_LINK").d("[SHARE_LINK] Deferred deep link stored in UserPreferences: target=%s, id=%s", target, id)
                    }
                    userPreferences.markInstallReferrerProcessed()
                } else {
                    Timber.tag("SHARE_LINK").d("[SHARE_LINK] No deferred deep link action returned from install referrer")
                    if (!must.kdroiders.hustlehub.BuildConfig.DEBUG) {
                        userPreferences.markInstallReferrerProcessed()
                    }
                }
            } catch (e: Exception) {
                Timber.tag("SHARE_LINK").e(e, "[SHARE_LINK] Failed to check install referrer on splash")
            }
        }

        companion object {
            private const val MIN_SPLASH_DURATION_MS = 2000L
        }
    }
