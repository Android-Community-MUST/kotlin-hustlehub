package must.kdroiders.hustlehub.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

// extension to create DataStore instance
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "hustlehub_preferences"
)

@Singleton
class UserPreferences @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private companion object {
        val IS_FIRST_LAUNCH = booleanPreferencesKey(
            "is_first_launch"
        )
    }

    // defaults to true on read failure so onboarding shows
    val isFirstLaunch: Flow<Boolean> = dataStore.data
        .catch { e ->
            if (e is IOException) {
                Timber.e(e, "Error reading preferences")
                emit(emptyPreferences())
            } else {
                throw e
            }
        }
        .map { prefs ->
            prefs[IS_FIRST_LAUNCH] ?: true
        }

    // catches IOException so a write failure
    // doesn't crash the app
    suspend fun setFirstLaunchComplete() {
        try {
            dataStore.edit { prefs ->
                prefs[IS_FIRST_LAUNCH] = false
            }
        } catch (e: IOException) {
            Timber.e(e, "Error writing preferences")
        }
    }
}
