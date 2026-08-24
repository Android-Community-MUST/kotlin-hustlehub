package must.kdroiders.hustlehub.ui.features.auth.domain.usecase

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import must.kdroiders.hustlehub.data.local.AppDatabase
import must.kdroiders.hustlehub.datastore.UserPreferences
import must.kdroiders.hustlehub.ui.features.auth.domain.repository.AuthRepository
import must.kdroiders.hustlehub.ui.features.chat.domain.repository.ChatRepository
import timber.log.Timber
import javax.inject.Inject

/**
 * Orchestrates the complete account deletion flow:
 * 1. Re-authenticates the user with [password] and deletes their identity from Firebase Auth & PostgreSQL backend via [AuthRepository.deleteAccount].
 * 2. Disconnects active WebSockets via [ChatRepository].
 * 3. Clears cached user credentials from DataStore via [UserPreferences].
 * 4. Clears all cached tables from Room database via [AppDatabase].
 */
class DeleteAccountUseCase
    @Inject
    constructor(
        private val authRepository: AuthRepository,
        private val chatRepository: ChatRepository,
        private val userPreferences: UserPreferences,
        private val appDatabase: AppDatabase,
    ) {
        suspend operator fun invoke(password: String? = null): Result<Unit> =
            runCatching {
                // 1. Delete account from Auth & Backend (re-authenticating with password if provided)
                val result = authRepository.deleteAccount(password.takeIf { !it.isNullOrBlank() })
                if (result.isFailure) {
                    throw result.exceptionOrNull() ?: Exception("Failed to delete account")
                }

                // 2. Disconnect chat WebSockets
                runCatching { chatRepository.disconnectWebSocket() }

                // 3. Clear local identity
                userPreferences.clearUser()

                // 4. Wipe local Room database
                withContext(Dispatchers.IO) {
                    appDatabase.clearAllTables()
                }

                Timber.d("DeleteAccountUseCase: account permanently deleted successfully")
            }
    }
