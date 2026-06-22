package must.kdroiders.hustlehub.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import must.kdroiders.hustlehub.ui.features.profile.domain.model.User
import must.kdroiders.hustlehub.ui.features.profile.domain.model.UserRole
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/** Extension property to create / retrieve the DataStore singleton on [Context]. */
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "hustlehub_preferences",
)

/**
 * Lightweight DataStore wrapper for app-wide user preferences.
 *
 * Stores:
 * - First-launch flag (controls onboarding).
 * - Lightweight user identity fields (uid, name, email, role, avatar) so the
 *   app can show profile info offline without hitting the network.
 *
 * Use [writeUser] after successful login / sign-up and [clearUser] on logout.
 */
@Singleton
class UserPreferences
    @Inject
    constructor(
        private val dataStore: DataStore<Preferences>,
    ) {
        // Keys

        private companion object {
            val IS_FIRST_LAUNCH = booleanPreferencesKey("is_first_launch")
            val USER_ID = stringPreferencesKey("user_id")
            val USER_NAME = stringPreferencesKey("user_name")
            val USER_EMAIL = stringPreferencesKey("user_email")
            val USER_ROLE = stringPreferencesKey("user_role")
            val USER_AVATAR_URL = stringPreferencesKey("user_avatar_url")
            val USER_UUID = stringPreferencesKey("user_uuid")
            val RECENT_SEARCHES = stringSetPreferencesKey("recent_searches")
            /** Maximum number of recent searches to persist. Oldest entry is evicted when full. */
            const val MAX_RECENT_SEARCHES = 10
        }

        // Reads

        /**
         * Emits true on first launch (or on DataStore read error so onboarding is
         * never accidentally skipped due to a transient error).
         */
        val isFirstLaunch: Flow<Boolean> = dataStore.data
            .catch { e ->
                if (e is IOException) {
                    Timber.e(e, "Error reading preferences")
                    emit(emptyPreferences())
                } else {
                    throw e
                }
            }.map { prefs -> prefs[IS_FIRST_LAUNCH] ?: true }

        /**
         * Emits the cached [User] fields stored after the last successful auth.
         * Returns a default (empty) [User] if no data is persisted yet.
         */
        val cachedUser: Flow<User> = dataStore.data
            .catch { e ->
                if (e is IOException) {
                    Timber.e(e, "Error reading cached user")
                    emit(emptyPreferences())
                } else {
                    throw e
                }
            }.map { prefs ->
                User(
                    id = prefs[USER_ID] ?: "",
                    uuid = prefs[USER_UUID] ?: "",
                    name = prefs[USER_NAME] ?: "",
                    email = prefs[USER_EMAIL] ?: "",
                    role = prefs[USER_ROLE]
                        ?.let { runCatching { UserRole.valueOf(it) }.getOrDefault(UserRole.CUSTOMER) }
                        ?: UserRole.CUSTOMER,
                    profilePhotoUrl = prefs[USER_AVATAR_URL] ?: "",
                )
            }

        // Writes

        /** Marks onboarding as seen. */
        suspend fun setFirstLaunchComplete() {
            try {
                dataStore.edit { prefs ->
                    prefs[IS_FIRST_LAUNCH] = false
                }
            } catch (e: IOException) {
                Timber.e(e, "Error writing is_first_launch")
            }
        }

        /**
         * Persists lightweight user identity fields to DataStore.
         *
         * Call this after every successful login or sign-up so the app can show
         * the user's name and avatar without a network round-trip.
         */
        suspend fun writeUser(user: User) {
            try {
                dataStore.edit { prefs ->
                    prefs[USER_ID] = user.id
                    prefs[USER_UUID] = user.uuid
                    prefs[USER_NAME] = user.name
                    prefs[USER_EMAIL] = user.email
                    prefs[USER_ROLE] = user.role.name
                    prefs[USER_AVATAR_URL] = user.profilePhotoUrl
                }
                Timber.d("User written to DataStore: uid=%s", user.id)
            } catch (e: IOException) {
                Timber.e(e, "Error writing user to DataStore")
            }
        }

        /**
         * Removes all stored user fields from DataStore.
         *
         * Call this on logout so stale identity is not shown to the next session.
         */
        suspend fun clearUser() {
            try {
                dataStore.edit { prefs ->
                    prefs.remove(USER_ID)
                    prefs.remove(USER_UUID)
                    prefs.remove(USER_NAME)
                    prefs.remove(USER_EMAIL)
                    prefs.remove(USER_ROLE)
                    prefs.remove(USER_AVATAR_URL)
                }
                Timber.d("User cleared from DataStore")
            } catch (e: IOException) {
                Timber.e(e, "Error clearing user from DataStore")
            }
        }

        /**
         * Emits the list of the user's recent search queries, most recent first.
         * Returns an empty list if no history is stored.
         */
        val recentSearches: Flow<List<String>> = dataStore.data
            .catch { e ->
                if (e is IOException) {
                    Timber.e(e, "Error reading recent searches")
                    emit(emptyPreferences())
                } else {
                    throw e
                }
            }.map { prefs ->
                // StringSet has no guaranteed order; we encode insertion order as
                // "index:value" so we can restore recency after reading from DataStore.
                prefs[RECENT_SEARCHES]
                    ?.mapNotNull { entry ->
                        val idx = entry.substringBefore(':').toIntOrNull() ?: return@mapNotNull null
                        val value = entry.substringAfter(':')
                        idx to value
                    }
                    ?.sortedByDescending { it.first }
                    ?.map { it.second }
                    ?: emptyList()
            }

        /**
         * Saves [query] to recent searches, evicting the oldest entry when the list
         * exceeds [MAX_RECENT_SEARCHES]. Duplicate entries are deduplicated (existing
         * entry is removed and the query is re-inserted at the front).
         */
        suspend fun addRecentSearch(query: String) {
            if (query.isBlank()) return
            try {
                dataStore.edit { prefs ->
                    val existing = prefs[RECENT_SEARCHES]?.toMutableSet() ?: mutableSetOf()
                    // Parse current entries: "idx:value"
                    val parsed = existing
                        .mapNotNull { entry ->
                            val idx = entry.substringBefore(':').toIntOrNull() ?: return@mapNotNull null
                            idx to entry.substringAfter(':')
                        }
                        .toMutableList()

                    // Remove any existing entry for this query (dedup)
                    parsed.removeAll { it.second == query }

                    // Assign a new index higher than the current max
                    val nextIdx = (parsed.maxOfOrNull { it.first } ?: 0) + 1
                    parsed.add(nextIdx to query)

                    // Evict oldest entries beyond the cap
                    val trimmed = parsed.sortedByDescending { it.first }.take(MAX_RECENT_SEARCHES)

                    prefs[RECENT_SEARCHES] = trimmed.map { "${it.first}:${it.second}" }.toSet()
                }
            } catch (e: IOException) {
                Timber.e(e, "Error saving recent search")
            }
        }

        /** Clears all recent search history. */
        suspend fun clearRecentSearches() {
            try {
                dataStore.edit { prefs -> prefs.remove(RECENT_SEARCHES) }
            } catch (e: IOException) {
                Timber.e(e, "Error clearing recent searches")
            }
        }
    }
