package must.kdroiders.hustlehub.core.api

import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import timber.log.Timber
import java.util.concurrent.TimeUnit

class TokenAuthenticator(
    private val firebaseAuth: FirebaseAuth?
) : Authenticator {
    override fun authenticate(route: Route?, response: Response): Request? {
        val currentUser = firebaseAuth?.currentUser ?: return null

        // Avoid infinite loops if the request has already been retried
        if (responseCount(response) >= 2) {
            return null
        }

        return try {
            // Force a network refresh — this runs after a 401, so a cached token was already rejected.
            val tokenTask = currentUser.getIdToken(true)
            val result = Tasks.await(tokenTask, 3, TimeUnit.SECONDS)
            val token = result.token

            if (!token.isNullOrEmpty()) {
                response.request.newBuilder()
                    .header("Authorization", "Bearer $token")
                    .build()
            } else {
                null
            }
        } catch (e: Exception) {
            Timber.e(e, "Error force-refreshing Firebase ID token")
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
