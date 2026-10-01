package must.kdroiders.hustlehub.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

data class ReviewMetrics(
    val firstInstallTime: Long = 0L,
    val appOpenCount: Int = 0,
    val significantActionsCount: Int = 0,
    val lastPromptTime: Long = 0L,
    val hasRatedApp: Boolean = false,
)

@Singleton
class ReviewPreferences
    @Inject
    constructor(
        private val dataStore: DataStore<Preferences>,
    ) {
        private companion object {
            val FIRST_INSTALL_TIME = longPreferencesKey("review_first_install_time")
            val APP_OPEN_COUNT = intPreferencesKey("review_app_open_count")
            val SIGNIFICANT_ACTIONS_COUNT = intPreferencesKey("review_significant_actions_count")
            val LAST_PROMPT_TIME = longPreferencesKey("review_last_prompt_time")
            val HAS_RATED_APP = booleanPreferencesKey("review_has_rated_app")
        }

        val reviewMetrics: Flow<ReviewMetrics> =
            dataStore.data
                .catch { e ->
                    if (e is IOException) {
                        Timber.e(e, "Error reading review preferences")
                        emit(emptyPreferences())
                    } else {
                        throw e
                    }
                }.map { prefs ->
                    ReviewMetrics(
                        firstInstallTime = prefs[FIRST_INSTALL_TIME] ?: 0L,
                        appOpenCount = prefs[APP_OPEN_COUNT] ?: 0,
                        significantActionsCount = prefs[SIGNIFICANT_ACTIONS_COUNT] ?: 0,
                        lastPromptTime = prefs[LAST_PROMPT_TIME] ?: 0L,
                        hasRatedApp = prefs[HAS_RATED_APP] ?: false,
                    )
                }

        suspend fun recordAppOpen() {
            try {
                dataStore.edit { prefs ->
                    val currentOpens = prefs[APP_OPEN_COUNT] ?: 0
                    prefs[APP_OPEN_COUNT] = currentOpens + 1
                    if ((prefs[FIRST_INSTALL_TIME] ?: 0L) <= 0L) {
                        prefs[FIRST_INSTALL_TIME] = System.currentTimeMillis()
                    }
                }
            } catch (e: IOException) {
                Timber.e(e, "Error recording app open in review preferences")
            }
        }

        suspend fun recordSignificantAction() {
            try {
                dataStore.edit { prefs ->
                    val current = prefs[SIGNIFICANT_ACTIONS_COUNT] ?: 0
                    prefs[SIGNIFICANT_ACTIONS_COUNT] = current + 1
                }
            } catch (e: IOException) {
                Timber.e(e, "Error recording significant action in review preferences")
            }
        }

        suspend fun recordPromptShown() {
            try {
                dataStore.edit { prefs ->
                    prefs[LAST_PROMPT_TIME] = System.currentTimeMillis()
                }
            } catch (e: IOException) {
                Timber.e(e, "Error recording review prompt shown")
            }
        }

        suspend fun markHasRatedApp() {
            try {
                dataStore.edit { prefs ->
                    prefs[HAS_RATED_APP] = true
                }
            } catch (e: IOException) {
                Timber.e(e, "Error marking user as rated app")
            }
        }

        suspend fun resetForTesting() {
            try {
                dataStore.edit { prefs ->
                    prefs.remove(FIRST_INSTALL_TIME)
                    prefs.remove(APP_OPEN_COUNT)
                    prefs.remove(SIGNIFICANT_ACTIONS_COUNT)
                    prefs.remove(LAST_PROMPT_TIME)
                    prefs.remove(HAS_RATED_APP)
                }
            } catch (e: IOException) {
                Timber.e(e, "Error resetting review preferences")
            }
        }
    }
