package must.kdroiders.hustlehub.core.api

import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import okhttp3.Interceptor
import okhttp3.Response
import timber.log.Timber
import java.util.concurrent.TimeUnit

class AuthInterceptor(
    private val firebaseAuth: FirebaseAuth?
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val path = originalRequest.url.encodedPath

        // Skip auth token if endpoint is under /auth/register or doesn't need auth
        if (path.contains("/auth/register")) {
            return chain.proceed(originalRequest)
        }

        val currentUser = firebaseAuth?.currentUser
        if (currentUser == null) {
            return chain.proceed(originalRequest)
        }

        return try {
            // Get ID token synchronously using Tasks.await with a timeout
            val tokenTask = currentUser.getIdToken(false)
            val result = Tasks.await(tokenTask, 5, TimeUnit.SECONDS)
            val token = result.token

            if (!token.isNullOrEmpty()) {
                val newRequest = originalRequest.newBuilder()
                    .header("Authorization", "Bearer $token")
                    .build()
                chain.proceed(newRequest)
            } else {
                chain.proceed(originalRequest)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error fetching Firebase ID token in interceptor")
            chain.proceed(originalRequest)
        }
    }
}
