package must.kdroiders.hustlehub.core.api

import com.google.firebase.auth.FirebaseAuthException

/**
 * Translates Firebase Auth error codes into user-friendly messages.
 *
 * All Firebase Auth operations in [AuthRepositoryImpl] route their exceptions
 * through this mapper before rethrowing, so the ViewModel layer always receives
 * a presentable string rather than a raw SDK message.
 *
 * Error codes: https://firebase.google.com/docs/auth/admin/errors
 */
object FirebaseAuthErrorMapper {
    /**
     * Maps a [Throwable] to a human-friendly message.
     *
     * If the exception is a [FirebaseAuthException] its error code is matched
     * first. For any other exception the message is returned as-is (or a
     * generic fallback is used).
     */
    fun map(e: Throwable): String {
        if (e is FirebaseAuthException) {
            return when (e.errorCode) {
                // Sign-up errors
                "ERROR_EMAIL_ALREADY_IN_USE" ->
                    "An account with this email already exists. Try logging in instead."

                "ERROR_INVALID_EMAIL" ->
                    "Please enter a valid email address."

                "ERROR_WEAK_PASSWORD" ->
                    "Password is too weak. Use at least 8 characters with uppercase letters and numbers."

                "ERROR_OPERATION_NOT_ALLOWED" ->
                    "Email/password sign-in is not enabled. Please contact support."

                // Sign-in errors
                "ERROR_USER_NOT_FOUND" ->
                    "No account found with this email. Please sign up first."

                "ERROR_WRONG_PASSWORD",
                "ERROR_INVALID_CREDENTIAL",
                ->
                    "Incorrect password. Please try again or reset your password."

                "ERROR_USER_DISABLED" ->
                    "This account has been disabled. Please contact support."

                "ERROR_USER_MISMATCH" ->
                    "The credentials do not match the currently signed-in user."

                // Rate limiting / network
                "ERROR_TOO_MANY_REQUESTS" ->
                    "Too many failed attempts. Please wait a moment and try again."

                "ERROR_NETWORK_REQUEST_FAILED" ->
                    "Network error. Check your internet connection and try again."

                // Token / session errors
                "ERROR_USER_TOKEN_EXPIRED",
                "ERROR_INVALID_USER_TOKEN",
                ->
                    "Your session has expired. Please log in again."

                "ERROR_REQUIRES_RECENT_LOGIN" ->
                    "This action requires you to log in again for security."

                // Google sign-in specific
                "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL" ->
                    "An account already exists with a different sign-in method for this email."

                "ERROR_CREDENTIAL_ALREADY_IN_USE" ->
                    "This Google account is already linked to another user."

                // Generic Firebase fallback
                else ->
                    e.message
                        ?.takeIf { it.isNotBlank() }
                        ?: "Authentication failed. Please try again."
            }
        }

        // Non-Firebase exceptions (network timeouts, etc.)
        return e.message
            ?.takeIf { it.isNotBlank() }
            ?: "Something went wrong. Please try again."
    }
}
