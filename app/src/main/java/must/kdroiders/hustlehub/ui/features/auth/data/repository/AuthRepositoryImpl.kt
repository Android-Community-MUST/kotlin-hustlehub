package must.kdroiders.hustlehub.ui.features.auth.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await
import must.kdroiders.hustlehub.ui.features.auth.domain.repository.AuthRepository
import must.kdroiders.hustlehub.ui.features.auth.domain.repository.LoginResult
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

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
            user.getIdToken(true).await()

            // If email not verified, send verification link automatically
            if (!user.isEmailVerified) {
                try {
                    user.sendEmailVerification().await()
                    Timber.d("Automatic verification link sent to $email")
                } catch (e: Exception) {
                    Timber.e(e, "Failed to send automatic verification email on login")
                }
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

    override suspend fun signUp(name: String, email: String, password: String): LoginResult {
        return try {
            val result = firebaseAuth
                .createUserWithEmailAndPassword(email, password)
                .await()

            val user = result.user
                ?: throw Exception("Sign-up failed: no user returned")

            // Set the Firebase display name
            try {
                val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                    .setDisplayName(name)
                    .build()
                user.updateProfile(profileUpdates).await()
                Timber.d("Firebase profile updated with name: $name")
            } catch (e: Exception) {
                Timber.e(e, "Failed to update Firebase profile display name")
            }

            // Automatically send standard verification link
            try {
                user.sendEmailVerification().await()
                Timber.d("Verification email sent to $email")
            } catch (e: Exception) {
                Timber.e(e, "Failed to send verification email on sign-up")
            }

            LoginResult(
                user = user,
                isEmailVerified = user.isEmailVerified
            )
        } catch (e: Exception) {
            Timber.e(e, "Sign-up failed")
            throw e
        }
    }

    override suspend fun signInWithGoogle(idToken: String): LoginResult {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = firebaseAuth.signInWithCredential(credential).await()
            val user = result.user
                ?: throw Exception("Google sign-in failed: no user returned")

            LoginResult(
                user = user,
                isEmailVerified = user.isEmailVerified
            )
        } catch (e: Exception) {
            Timber.e(e, "Google sign-in failed")
            throw e
        }
    }

    override suspend fun sendOtp(email: String) {
        try {
            val user = firebaseAuth.currentUser
                ?: throw Exception("No logged-in user to send verification email to")
            user.sendEmailVerification().await()
            Timber.d("Verification email link sent to $email")
        } catch (e: Exception) {
            Timber.e(e, "Failed to send verification email link")
            throw e
        }
    }

    override suspend fun verifyOtp(email: String, otp: String) {
        try {
            val user = firebaseAuth.currentUser
                ?: throw Exception("No logged-in user to verify")

            user.reload().await()

            if (!user.isEmailVerified) {
                throw Exception("Email not yet verified. Please check your inbox and click the verification link, then tap \"Verify\" below.")
            }
        } catch (e: Exception) {
            Timber.e(e, "Email verification failed")
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
