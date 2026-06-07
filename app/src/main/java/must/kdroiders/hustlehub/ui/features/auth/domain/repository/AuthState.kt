package must.kdroiders.hustlehub.ui.features.auth.domain.repository

import com.google.firebase.auth.FirebaseUser

/**
 * Represents the current Firebase authentication state observed by [AuthManager].
 *
 * Consumers (NavGraph, screens) should handle all four states:
 * - [Loading] — initial state while Firebase checks persisted credentials.
 * - [Authenticated] — a Firebase user is signed in and (optionally) email-verified.
 * - [Unauthenticated] — no Firebase user; navigate to the Login screen.
 * - [Error] — an unexpected error occurred reading auth state.
 */
sealed interface AuthState {
    /** Auth check is in progress (shown briefly on cold start). */
    data object Loading : AuthState

    /**
     * A user is currently signed in to Firebase Auth.
     *
     * @param user The active [FirebaseUser]. Note: [FirebaseUser.isEmailVerified] may be
     *   false for email/password users who haven't clicked the verification link yet.
     */
    data class Authenticated(val user: FirebaseUser) : AuthState

    /** No user is signed in — the app should navigate to the Login screen. */
    data object Unauthenticated : AuthState

    /**
     * An unexpected error prevented reading the auth state.
     *
     * @param message A user-readable description of the problem.
     */
    data class Error(val message: String) : AuthState
}
