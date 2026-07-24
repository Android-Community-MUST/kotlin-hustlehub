package must.kdroiders.hustlehub.ui.features.auth.data.repository

import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await
import must.kdroiders.hustlehub.core.api.FirebaseAuthErrorMapper
import must.kdroiders.hustlehub.ui.features.auth.domain.repository.AuthRepository
import must.kdroiders.hustlehub.ui.features.auth.domain.repository.LoginResult
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firebase Auth implementation of [AuthRepository].
 *
 * Idiomatic Kotlin implementation using [runCatching] pipelines.
 * All Firebase exceptions are routed through [FirebaseAuthErrorMapper] before being
 * rethrown, while preserving coroutine cancellation.
 */
@Singleton
class AuthRepositoryImpl
    @Inject
    constructor(
        private val firebaseAuth: FirebaseAuth,
    ) : AuthRepository {
        /**
         * Signs in an existing user with email and password.
         *
         * If the email is not yet verified, a fresh verification link is sent
         * automatically and the result still reflects [LoginResult.isEmailVerified] = false
         * so the caller can redirect to the verification screen.
         */
        override suspend fun login(
            email: String,
            password: String,
        ): LoginResult = runCatching {
            val result = firebaseAuth
                .signInWithEmailAndPassword(email, password)
                .await()

            val user = result.user
                ?: throw Exception("Login failed: no user returned")

            // Reload to get the latest emailVerified status from Firebase servers
            user.reload().await()
            // Refresh the ID token cache
            user.getIdToken(true).await()

            // Auto-send a new verification link if the email is still unverified
            if (!user.isEmailVerified) {
                runCatching {
                    user.sendEmailVerification().await()
                    Timber.d("Automatic verification link re-sent to %s", email)
                }.onFailure { e ->
                    if (e is CancellationException) throw e
                    Timber.e(e, "Failed to auto-send verification email on login")
                }
            }

            LoginResult(
                user = user,
                isEmailVerified = user.isEmailVerified,
            )
        }.getOrElse { e ->
            if (e is CancellationException) throw e
            Timber.e(e, "Login failed for %s", email)
            throw Exception(FirebaseAuthErrorMapper.map(e), e)
        }

        /**
         * Creates a new Firebase user and sends a verification email.
         *
         * The Firebase display name is set immediately after account creation.
         * Backend registration is handled separately in [SignUpUseCase] to keep this
         * repository focused on Firebase only.
         */
        override suspend fun signUp(
            name: String,
            email: String,
            password: String,
        ): LoginResult = runCatching {
            val result = firebaseAuth
                .createUserWithEmailAndPassword(email, password)
                .await()

            val user = result.user
                ?: throw Exception("Sign-up failed: no user returned")

            // Set Firebase display name
            runCatching {
                val profileUpdates = UserProfileChangeRequest
                    .Builder()
                    .setDisplayName(name)
                    .build()
                user.updateProfile(profileUpdates).await()
                Timber.d("Firebase profile updated with name: %s", name)
            }.onFailure { e ->
                if (e is CancellationException) throw e
                Timber.e(e, "Failed to update Firebase profile display name")
            }

            // Send the verification email link
            runCatching {
                user.sendEmailVerification().await()
                Timber.d("Verification email sent to %s", email)
            }.onFailure { e ->
                if (e is CancellationException) throw e
                Timber.e(e, "Failed to send verification email on sign-up")
            }

            LoginResult(
                user = user,
                isEmailVerified = user.isEmailVerified,
            )
        }.getOrElse { e ->
            if (e is CancellationException) throw e
            Timber.e(e, "Sign-up failed for %s", email)
            throw Exception(FirebaseAuthErrorMapper.map(e), e)
        }

        /**
         * Signs in with a Google ID token obtained from the Credential Manager flow.
         *
         * Domain validation (@must.ac.ke) is enforced in [LoginViewModel] after this
         * call succeeds, because Firebase does not know our domain restriction.
         */
        override suspend fun signInWithGoogle(idToken: String): LoginResult = runCatching {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = firebaseAuth.signInWithCredential(credential).await()
            val user = result.user
                ?: throw Exception("Google sign-in failed: no user returned")

            LoginResult(
                user = user,
                isEmailVerified = user.isEmailVerified,
            )
        }.getOrElse { e ->
            if (e is CancellationException) throw e
            Timber.e(e, "Google sign-in failed")
            throw Exception(FirebaseAuthErrorMapper.map(e), e)
        }

        /**
         * Sends a Firebase email verification link to the currently signed-in user.
         *
         * Named `sendOtp` to match the [AuthRepository] contract, even though Firebase
         * uses a link rather than a numeric OTP.
         */
        override suspend fun sendOtp(email: String) {
            runCatching {
                val user = firebaseAuth.currentUser
                    ?: throw Exception("No signed-in user to send a verification email to")
                user.sendEmailVerification().await()
                Timber.d("Verification email link sent to %s", email)
            }.onFailure { e ->
                if (e is CancellationException) throw e
                Timber.e(e, "Failed to send verification email link")
                throw Exception(FirebaseAuthErrorMapper.map(e), e)
            }
        }

        /**
         * Checks whether the current Firebase user has verified their email.
         *
         * Reloads the user from Firebase servers to get the latest [FirebaseUser.isEmailVerified]
         * status before checking, so a recently clicked verification link is reflected immediately.
         */
        override suspend fun verifyOtp(
            email: String,
            otp: String,
        ) {
            runCatching {
                val user = firebaseAuth.currentUser
                    ?: throw Exception("No signed-in user to verify")

                user.reload().await()

                if (!user.isEmailVerified) {
                    throw Exception(
                        "Email not yet verified. Please check your inbox and click the " +
                            "verification link, then tap \"Verify\" below.",
                    )
                }
            }.onFailure { e ->
                if (e is CancellationException) throw e
                Timber.e(e, "Email verification check failed")
                throw if (e is com.google.firebase.auth.FirebaseAuthException) {
                    Exception(FirebaseAuthErrorMapper.map(e), e)
                } else {
                    e
                }
            }
        }

        /** Re-sends the verification email. Delegates to [sendOtp]. */
        override suspend fun resendOtp(email: String) {
            sendOtp(email)
        }

        /** Returns the currently signed-in [FirebaseUser], or null if logged out. */
        override fun getCurrentUser(): FirebaseUser? = firebaseAuth.currentUser

        /** Signs the current user out of Firebase Auth. */
        override fun logout() {
            firebaseAuth.signOut()
        }

        /** Sends a password reset email to the given email address. */
        override suspend fun sendPasswordResetEmail(email: String) {
            runCatching {
                firebaseAuth.sendPasswordResetEmail(email).await()
                Timber.d("Password reset email sent to %s", email)
            }.onFailure { e ->
                if (e is CancellationException) throw e
                Timber.e(e, "Failed to send password reset email")
                throw Exception(FirebaseAuthErrorMapper.map(e), e)
            }
        }

        /**
         * Changes the currently signed-in user's password.
         * Re-authenticates the user with their current password before updating to the new password.
         */
        override suspend fun changePassword(
            currentPassword: String,
            newPassword: String,
        ): Result<Unit> = runCatching {
            val user = firebaseAuth.currentUser
                ?: throw Exception("User not logged in")

            val email = user.email
                ?: throw Exception("Email not found for current user")

            if (currentPassword == newPassword) {
                throw Exception("New password cannot be the same as the current password")
            }

            // Create the credential
            val credential = EmailAuthProvider.getCredential(email, currentPassword)

            // Re-authenticate to ensure the session is fresh and the current password is correct
            user.reauthenticate(credential).await()

            // Update to the new password
            user.updatePassword(newPassword).await()
            Unit
        }.recoverCatching { e ->
            if (e is CancellationException) throw e
            when (e) {
                is FirebaseAuthInvalidUserException -> throw Exception("User account is disabled or deleted.")
                is FirebaseAuthInvalidCredentialsException -> throw Exception("Incorrect current password.")
                else -> throw e
            }
        }
    }
