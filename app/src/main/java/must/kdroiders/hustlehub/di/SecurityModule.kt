package must.kdroiders.hustlehub.di

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import must.kdroiders.hustlehub.core.security.CryptoManager
import must.kdroiders.hustlehub.core.security.KeyExchangeHandler
import must.kdroiders.hustlehub.ui.features.chat.data.remote.KeyExchangeApiService
import android.os.Build
import timber.log.Timber
import java.io.File
import javax.inject.Singleton

/** Hilt module for E2EE security dependencies. */
@Suppress("DEPRECATION")
@Module
@InstallIn(SingletonComponent::class)
object SecurityModule {
    private const val PREFS_FILE_NAME = "hustlehub_e2ee_prefs"

    @Provides
    @Singleton
    fun provideMasterKey(
        @ApplicationContext context: Context,
    ): MasterKey =
        MasterKey
            .Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

    @Provides
    @Singleton
    fun provideEncryptedSharedPreferences(
        @ApplicationContext context: Context,
        masterKey: MasterKey,
    ): SharedPreferences {
        return try {
            EncryptedSharedPreferences.create(
                context,
                PREFS_FILE_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
            )
        } catch (e: Exception) {
            Timber.e(e, "Failed to initialize EncryptedSharedPreferences; recovering by clearing corrupted prefs.")
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    context.deleteSharedPreferences(PREFS_FILE_NAME)
                } else {
                    context.getSharedPreferences(PREFS_FILE_NAME, Context.MODE_PRIVATE).edit().clear().commit()
                    val sharedPrefsFile = File(context.filesDir.parent, "shared_prefs/$PREFS_FILE_NAME.xml")
                    if (sharedPrefsFile.exists()) {
                        sharedPrefsFile.delete()
                    }
                }
            } catch (cleanupEx: Exception) {
                Timber.e(cleanupEx, "Failed to delete corrupted SharedPreferences file.")
            }

            EncryptedSharedPreferences.create(
                context,
                PREFS_FILE_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
            )
        }
    }

    @Provides
    @Singleton
    fun provideCryptoManager(): CryptoManager = CryptoManager()

    @Provides
    @Singleton
    fun provideKeyExchangeHandler(
        cryptoManager: CryptoManager,
        keyExchangeApiService: KeyExchangeApiService,
        encryptedPrefs: SharedPreferences,
    ): KeyExchangeHandler = KeyExchangeHandler(cryptoManager, keyExchangeApiService, encryptedPrefs)
}
