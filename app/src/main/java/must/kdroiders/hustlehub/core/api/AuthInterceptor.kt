package must.kdroiders.hustlehub.core.api

import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import okhttp3.Interceptor
import okhttp3.Response
import timber.log.Timber
import java.util.concurrent.TimeUnit

class AuthInterceptor(
    private val firebaseAuth: FirebaseAuth?,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val path = originalRequest.url.encodedPath

        // Skip auth token only for exactly /auth/register — strict match to avoid
        // accidentally exempting future /auth/register-* paths
        val isRegisterEndpoint = path.trimEnd('/').endsWith("/auth/register")
        if (isRegisterEndpoint) {
            return chain.proceed(originalRequest)
        }

        val currentUser = firebaseAuth?.currentUser
        if (currentUser == null) {
            return chain.proceed(originalRequest)
        }

        val token = fetchTokenWithFallback()

        return if (!token.isNullOrEmpty()) {
            val newRequest = originalRequest
                .newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
            chain.proceed(newRequest)
        } else {
            chain.proceed(originalRequest)
        }
    }

    /**
     * Attempts to fetch a cached Firebase ID token first (fast path).
     * If that times out, it retries with a forced network refresh (slow path).
     * Returns null only if both attempts fail.
     */
    private fun fetchTokenWithFallback(): String? {
        val currentUser = firebaseAuth?.currentUser ?: return null
        return try {
            // Fast path: use cached token (usually instant)
            val result = Tasks.await(currentUser.getIdToken(false), 10, TimeUnit.SECONDS)
            result.token
        } catch (e: Exception) {
            Timber.w(e, "Cached token fetch timed out — retrying with force refresh")
            try {
                // Slow path: force a network token refresh
                val result = Tasks.await(currentUser.getIdToken(true), 15, TimeUnit.SECONDS)
                result.token
            } catch (e2: Exception) {
                Timber.e(e2, "Force-refresh token fetch also failed — proceeding without token")
                null
            }
        }
    }
}
