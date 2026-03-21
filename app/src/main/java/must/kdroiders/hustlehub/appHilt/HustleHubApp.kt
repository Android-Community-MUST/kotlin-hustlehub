package must.kdroiders.hustlehub.appHilt

import android.app.Application
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp
import must.kdroiders.hustlehub.BuildConfig
import timber.log.Timber

@HiltAndroidApp
class HustleHubApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialize Firebase
        try {
            FirebaseApp.initializeApp(this)
        } catch (e: Exception) {
            Timber.e(e, "Firebase initialization failed")
        }

        // Initialize Timber for logging (optional)
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}
