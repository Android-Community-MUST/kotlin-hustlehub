package must.kdroiders.hustlehub.ui.features.auth.domain.usecase

import must.kdroiders.hustlehub.ui.features.profile.domain.model.User
import must.kdroiders.hustlehub.ui.features.profile.domain.model.UserRole
import must.kdroiders.hustlehub.ui.features.auth.domain.repository.AuthRepository
import must.kdroiders.hustlehub.ui.features.auth.domain.repository.LoginResult
import must.kdroiders.hustlehub.ui.features.profile.domain.repository.UserRepository
import timber.log.Timber
import javax.inject.Inject

/**
 * Creates a new Firebase user and registers them in the HustleHub backend.
 *
 * Steps:
 * 1. Delegates to [AuthRepository.signUp] → Firebase creates the account and sends a
 *    verification email.
 * 2. Builds a minimal [User] from the returned [FirebaseUser] and calls
 *    [UserRepository.saveUserProfile] → `POST /auth/register` on the Spring Boot backend.
 *    A 409 Conflict (user already registered) is treated as success by [UserRepository].
 * 3. Returns [Result.success] carrying the [LoginResult] from step 1 so the caller can
 *    navigate to the email-verification screen.
 *
 * @returns [Result.failure] with a user-friendly message if Firebase account creation fails.
 *          Backend registration failure is logged but does not fail the use case.
 */
class SignUpUseCase
    @Inject
    constructor(
        private val authRepository: AuthRepository,
        private val userRepository: UserRepository,
    ) {
        suspend operator fun invoke(
            name: String,
            email: String,
            password: String,
        ): Result<LoginResult> =
            runCatching {
                // Step 1 — Firebase account creation
                val loginResult = authRepository.signUp(name, email, password)

                // Step 2 — Backend registration (non-blocking for the happy path)
                val firebaseUser = loginResult.user
                val newUser = User(
                    id = firebaseUser.uid,
                    name = name,
                    email = email,
                    role = UserRole.CUSTOMER,
                    profilePhotoUrl = firebaseUser.photoUrl?.toString() ?: "",
                    isVerified = false,
                )
                val saveResult = userRepository.saveUserProfile(newUser)
                saveResult.onFailure { e ->
                    // Log but don't fail sign-up — the user is created in Firebase.
                    // Backend registration can be retried on next splash load.
                    Timber.e(e, "Backend registration failed for uid=%s — will retry on next launch", firebaseUser.uid)
                }

                loginResult
            }
    }
