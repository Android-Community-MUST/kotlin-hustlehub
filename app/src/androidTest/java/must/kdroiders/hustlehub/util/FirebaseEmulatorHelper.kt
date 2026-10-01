package must.kdroiders.hustlehub.util

import android.os.Build
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import timber.log.Timber

/**
 * Utility for configuring Firebase Emulator Suite in instrumented tests.
 */
object FirebaseEmulatorHelper {
    private var isConfigured = false

    /**
     * Determines whether the running environment is an Android Emulator or a physical device.
     * On physical devices, port forwarding via `adb reverse` routes localhost to the development host.
     */
    fun getEmulatorHost(): String {
        val isEmulator = Build.FINGERPRINT.startsWith("generic") ||
            Build.FINGERPRINT.startsWith("unknown") ||
            Build.MODEL.contains("google_sdk") ||
            Build.MODEL.contains("Emulator") ||
            Build.MODEL.contains("Android SDK built for x86") ||
            Build.HARDWARE.contains("goldfish") ||
            Build.HARDWARE.contains("ranchu")

        return if (isEmulator) "10.0.2.2" else "127.0.0.1"
    }

    /**
     * Points Firebase services (Auth, Firestore, Storage) to the local Firebase Emulator Suite.
     */
    fun setupEmulators(host: String = getEmulatorHost()) {
        if (isConfigured) return
        try {
            Firebase.auth.useEmulator(host, 9099)
            Firebase.firestore.useEmulator(host, 8085)
            Firebase.storage.useEmulator(host, 9199)
            isConfigured = true
            Timber.i("Firebase Emulator Suite connected on host: $host")
        } catch (e: IllegalStateException) {
            Timber.w(e, "Firebase emulator already initialized")
            isConfigured = true
        } catch (e: Exception) {
            Timber.e(e, "Failed to connect to Firebase Emulator Suite")
        }
    }
}
