package must.kdroiders.hustlehub.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.google.firebase.auth.FirebaseAuth
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import must.kdroiders.hustlehub.sharedPrefs.UserPreferences
import must.kdroiders.hustlehub.sharedPrefs.dataStore
import timber.log.Timber
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth? {
        return try {
            FirebaseAuth.getInstance()
        } catch (e: IllegalStateException) {
            Timber.w("Firebase not initialized — running without auth")
            null
        }
    }

    @Provides
    @Singleton
    fun provideDataStore(
        @ApplicationContext context: Context
    ): DataStore<Preferences> = context.dataStore

    @Provides
    @Singleton
    fun provideUserPreferences(
        dataStore: DataStore<Preferences>
    ): UserPreferences = UserPreferences(dataStore)
}
