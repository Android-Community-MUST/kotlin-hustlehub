package must.kdroiders.hustlehub.ui.features.auth.domain.repository

import com.google.firebase.auth.FirebaseUser

data class LoginResult(
    val user: FirebaseUser,
    val isEmailVerified: Boolean,
)

interface AuthRepository {
    suspend fun login(
        email: String,
        password: String,
    ): LoginResult
    suspend fun signUp(
        name: String,
        email: String,
        password: String,
    ): LoginResult
    suspend fun signInWithGoogle(idToken: String): LoginResult
    suspend fun sendOtp(email: String)
    suspend fun verifyOtp(
        email: String,
        otp: String,
    )
    suspend fun resendOtp(email: String)
    suspend fun sendPasswordResetEmail(email: String)
    suspend fun changePassword(
        currentPassword: String,
        newPassword: String,
    ): Result<Unit>
    fun getCurrentUser(): FirebaseUser?
    suspend fun logout()
    suspend fun deleteAccount(password: String? = null): Result<Unit>
}
