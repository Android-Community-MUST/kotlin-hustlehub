package must.kdroiders.hustlehub.core.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import must.kdroiders.hustlehub.ui.features.auth.domain.repository.AuthState
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Singleton that exposes the current Firebase authentication state as a [Flow].
 *
 * Wraps [FirebaseAuth.addAuthStateListener] using [callbackFlow] so the rest of the app
 * can observe auth changes reactively without holding direct Firebase references.
 *
 * Lifecycle:
 * - The listener is attached when the first subscriber collects [authState].
 * - The listener is automatically removed when the last subscriber cancels.
 *
 * Usage:
 * ```kotlin
 * // In a ViewModel or Composable (via LaunchedEffect):
 * authManager.authState.collect { state ->
 *     when (state) {
 *         is AuthState.Authenticated -> // user logged in
 *         AuthState.Unauthenticated  -> // navigate to login
 *         AuthState.Loading          -> // show spinner
 *         is AuthState.Error         -> // show error
 *     }
 * }
 * ```
 */
@Singleton
class AuthManager
    @Inject
    constructor(
        private val firebaseAuth: FirebaseAuth?,
    ) {
        /**
         * Cold flow of [AuthState] backed by [FirebaseAuth.addAuthStateListener].
         *
         * Emits [AuthState.Loading] immediately, then [AuthState.Authenticated] or
         * [AuthState.Unauthenticated] as Firebase notifies the listener.
         *
         * Uses [distinctUntilChanged] to avoid duplicate emissions when the same
         * auth state fires multiple times (e.g., token refresh).
         */
        val authState: Flow<AuthState> = callbackFlow {
            // Firebase not available (CI / tests without google-services.json)
            if (firebaseAuth == null) {
                trySend(AuthState.Unauthenticated)
                awaitClose()
                return@callbackFlow
            }

            // Send Loading until the first listener callback arrives
            trySend(AuthState.Loading)

            val listener = FirebaseAuth.AuthStateListener { auth ->
                val user: FirebaseUser? = auth.currentUser
                val newState: AuthState = if (user != null) {
                    Timber.d("AuthManager: user signed in — uid=%s", user.uid)
                    AuthState.Authenticated(user)
                } else {
                    Timber.d("AuthManager: no current user — Unauthenticated")
                    AuthState.Unauthenticated
                }
                trySend(newState)
            }

            firebaseAuth.addAuthStateListener(listener)

            // Remove the listener when the collector is cancelled
            awaitClose {
                Timber.d("AuthManager: removing auth state listener")
                firebaseAuth.removeAuthStateListener(listener)
            }
        }.distinctUntilChanged()

        /**
         * Returns the currently signed-in [FirebaseUser], or null if the user is
         * not authenticated. This is a synchronous snapshot — prefer [authState]
         * for reactive observation.
         */
        fun currentUser(): FirebaseUser? = firebaseAuth?.currentUser

        /**
         * Returns true if there is a currently authenticated Firebase user.
         *
         * Convenience helper for one-off checks (e.g., deciding whether to show
         * a "Login" button).
         */
        fun isAuthenticated(): Boolean = firebaseAuth?.currentUser != null

        /**
         * Signs the current user out of Firebase Auth.
         *
         * Prefer calling [SignOutUseCase] from ViewModels — this method is provided
         * for cases where the sign-out must happen outside the ViewModel (e.g.,
         * [TokenAuthenticator] after an unrecoverable 401).
         */
        fun signOut() {
            Timber.d("AuthManager: signing out")
            firebaseAuth?.signOut()
        }
    }
