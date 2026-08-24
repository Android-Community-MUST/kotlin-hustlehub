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
 * Orchestrates the complete account deletion flow with crash/network-drop resilience:
 *
 * 1. Writes a [UserPreferences.PENDING_DELETION] flag **before** any network call — ensures
 *    the next app launch can complete local cleanup even if the process is killed mid-flight.
 * 2. Re-authenticates (password users) and deletes identity from Firebase Auth & PostgreSQL via [AuthRepository.deleteAccount].
 * 3. Disconnects active WebSockets via [ChatRepository].
 * 4. Wipes local Room database via [AppDatabase.clearAllTables] — done **before** emitting
 *    the navigation event so HomeScreen never shows stale data.
 * 5. Clears cached credentials from DataStore via [UserPreferences.clearUser].
 * 6. Clears the pending deletion flag.
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
                userPreferences.markPendingDeletion()

                val result = authRepository.deleteAccount(password.takeIf { !it.isNullOrBlank() })
                if (result.isFailure) {
                    throw result.exceptionOrNull() ?: Exception("Failed to delete account")
                }

                runCatching { chatRepository.disconnectWebSocket() }

                withContext(Dispatchers.IO) {
                    appDatabase.clearAllTables()
                }

                userPreferences.clearUser()
                userPreferences.clearPendingDeletion()

                Timber.d("DeleteAccountUseCase: account permanently deleted successfully")
            }
    }
