package must.kdroiders.hustlehub.data.repository

import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

data class LoginResult(
    val user: FirebaseUser,
    val isEmailVerified: Boolean
)

interface AuthRepository {
    suspend fun login(email: String, password: String): LoginResult
    suspend fun sendOtp(email: String)
    suspend fun verifyOtp(email: String, otp: String)
    suspend fun resendOtp(email: String)
    fun getCurrentUser(): FirebaseUser?
    fun logout()
}

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : AuthRepository {

    override suspend fun login(email: String, password: String): LoginResult {
        return try {
            val result = firebaseAuth
                .signInWithEmailAndPassword(email, password)
                .await()

            val user = result.user
                ?: throw Exception("Login failed: no user returned")

            // Reload to get latest emailVerified status
            user.reload().await()

            // If email not verified, send OTP automatically
            if (!user.isEmailVerified) {
                sendOtp(email)
            }

            LoginResult(
                user = user,
                isEmailVerified = user.isEmailVerified
            )
        } catch (e: Exception) {
            Timber.e(e, "Login failed")
            throw e
        }
    }

    override suspend fun sendOtp(email: String) {
        try {
            // Firebase sends a 6-digit OTP code to the user's email
            firebaseAuth.sendSignInLinkToEmail(
                email,
                com.google.firebase.auth.ActionCodeSettings.newBuilder()
                    .setHandleCodeInApp(true)
                    .build()
            ).await()
            Timber.d("OTP sent to $email")
        } catch (e: Exception) {
            Timber.e(e, "Failed to send OTP")
            throw e
        }
    }

    override suspend fun verifyOtp(email: String, otp: String) {
        try {
            // Re-authenticate the current user using email + OTP code
            val credential = EmailAuthProvider.getCredential(email, otp)
            val user = firebaseAuth.currentUser
                ?: throw Exception("No logged-in user to verify")

            user.reauthenticate(credential).await()
            user.reload().await()

            if (!user.isEmailVerified) {
                throw Exception("Email not yet verified. Please check your code.")
            }

            Timber.d("OTP verified for $email")
        } catch (e: Exception) {
            Timber.e(e, "OTP verification failed")
            throw e
        }
    }

    override suspend fun resendOtp(email: String) {
        sendOtp(email)
    }

    override fun getCurrentUser(): FirebaseUser? {
        return firebaseAuth.currentUser
    }

    override fun logout() {
        firebaseAuth.signOut()
    }
}
