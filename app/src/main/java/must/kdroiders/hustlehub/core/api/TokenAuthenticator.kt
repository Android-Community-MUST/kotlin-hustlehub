package must.kdroiders.hustlehub.core.api

import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import must.kdroiders.hustlehub.core.auth.AuthManager
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * OkHttp [Authenticator] that handles 401 Unauthorized responses by force-refreshing
 * the Firebase ID token and retrying the request with a fresh `Authorization` header.
 *
 * If the token refresh fails (expired Firebase session, revoked token, no network),
 * it signs the user out via [AuthManager] so the [AuthState] flow emits
 * [must.kdroiders.hustlehub.ui.features.auth.domain.repository.AuthState.Unauthenticated]
 * and the NavGraph automatically navigates to the Login screen.
 *
 * Auto-logout on token expiration:
 * - Firebase tokens expire after 1 hour.
 * - [AuthInterceptor] proactively refreshes the token before each request.
 * - If a 401 slips through (race condition or revocation), this authenticator attempts
 *   a force-refresh. On failure, it calls [AuthManager.signOut] which triggers
 *   [FirebaseAuth.signOut] → [FirebaseAuth.AuthStateListener] fires
 *   → [AuthManager.authState] emits [AuthState.Unauthenticated] → NavGraph pops to Login.
 */
class TokenAuthenticator(
    private val firebaseAuth: FirebaseAuth?,
    private val authManager: AuthManager
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        val currentUser = firebaseAuth?.currentUser ?: return null

        // Prevent infinite retry loops — stop after 2 attempts
        if (responseCount(response) >= 2) {
            Timber.w("TokenAuthenticator: 2 retry attempts exhausted — signing out")
            authManager.signOut()
            return null
        }

        return try {
            // Force a network token refresh — the cached token was already rejected by the backend
            val result = Tasks.await(currentUser.getIdToken(true), 10, TimeUnit.SECONDS)
            val token = result.token

            if (!token.isNullOrEmpty()) {
                Timber.d("TokenAuthenticator: token refreshed successfully")
                response.request.newBuilder()
                    .header("Authorization", "Bearer $token")
                    .build()
            } else {
                // Refresh succeeded but returned an empty token — treat as unrecoverable
                Timber.e("TokenAuthenticator: force-refresh returned empty token — signing out")
                authManager.signOut()
                null
            }
        } catch (e: Exception) {
            Timber.e(e, "TokenAuthenticator: force-refresh failed — signing out")
            authManager.signOut()
            null
        }
    }

    private fun responseCount(response: Response): Int {
        var result = 1
        var priorResponse = response.priorResponse
        while (priorResponse != null) {
            result++
            priorResponse = priorResponse.priorResponse
        }
        return result
    }
}
