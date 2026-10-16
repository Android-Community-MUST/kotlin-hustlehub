package must.kdroiders.hustlehub.ui.features.chat.presentation.audio

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import android.os.PowerManager
import android.os.SystemClock
import timber.log.Timber

class ProximityAudioRouter(
    context: Context,
    private val onEarpieceModeChanged: (isEarpiece: Boolean) -> Unit = {},
) {
    private val appContext = context.applicationContext

    private val sensorManager = appContext.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val audioManager = appContext.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    private val powerManager = appContext.getSystemService(Context.POWER_SERVICE) as? PowerManager

    private val proximitySensor: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_PROXIMITY)

    private var wakeLock: PowerManager.WakeLock? = null
    private var isListening = false
    private var lastSwitchTimestamp = 0L

    var isEarpieceActive = false
        private set

    init {
        initWakeLock()
    }

    private fun initWakeLock() {
        try {
            val pm = powerManager ?: return
            if (pm.isWakeLockLevelSupported(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK)) {
                wakeLock =
                    pm
                        .newWakeLock(
                            PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK,
                            "HustleHub:ProximityAudioScreenOff",
                        ).apply {
                            setReferenceCounted(false)
                        }
            }
        } catch (e: Throwable) {
            Timber.w(e, "ProximityAudioRouter: Failed to initialize proximity wake lock")
        }
    }

    private val sensorListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            if (event == null || event.sensor.type != Sensor.TYPE_PROXIMITY) return

            if (isHeadsetOrBluetoothConnected()) {
                if (isEarpieceActive) {
                    switchToSpeaker()
                }
                releaseWakeLock()
                return
            }

            val distance = event.values.firstOrNull() ?: return
            val maxRange = proximitySensor?.maximumRange ?: PROXIMITY_THRESHOLD_CM
            val isNear = if (maxRange > 0f) {
                distance < maxRange && distance < PROXIMITY_THRESHOLD_CM
            } else {
                distance < PROXIMITY_THRESHOLD_CM
            }

            if (isNear == isEarpieceActive) return

            val now = SystemClock.elapsedRealtime()
            if (now - lastSwitchTimestamp < SWITCH_DEBOUNCE_MS) return
            lastSwitchTimestamp = now

            if (isNear) {
                acquireWakeLock()
                switchToEarpiece()
            } else {
                switchToSpeaker()
            }
        }

        override fun onAccuracyChanged(
            sensor: Sensor?,
            accuracy: Int,
        ) = Unit
    }

    fun start() {
        if (isListening || proximitySensor == null) return
        try {
            isListening = sensorManager?.registerListener(
                sensorListener,
                proximitySensor,
                SensorManager.SENSOR_DELAY_NORMAL,
            ) == true
            if (isListening && !isHeadsetOrBluetoothConnected()) {
                acquireWakeLock()
            }
        } catch (e: Throwable) {
            Timber.w(e, "ProximityAudioRouter: Failed to register sensor listener")
        }
    }

    fun stop() {
        if (isListening) {
            try {
                sensorManager?.unregisterListener(sensorListener)
            } catch (e: Throwable) {
                Timber.w(e, "ProximityAudioRouter: Failed to unregister sensor listener")
            }
            isListening = false
        }

        if (isEarpieceActive) {
            switchToSpeaker()
        } else {
            resetAudioMode()
        }
        releaseWakeLock()
        lastSwitchTimestamp = 0L
    }

    fun release() {
        stop()
        wakeLock = null
    }

    private fun switchToEarpiece() {
        if (isEarpieceActive) return
        isEarpieceActive = true

        val am = audioManager
        try {
            if (am != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    am.mode = AudioManager.MODE_IN_COMMUNICATION
                    val earpieceDevice = am.availableCommunicationDevices.firstOrNull {
                        it.type == AudioDeviceInfo.TYPE_BUILTIN_EARPIECE
                    }
                    if (earpieceDevice != null) {
                        am.setCommunicationDevice(earpieceDevice)
                    }
                } else {
                    am.mode = AudioManager.MODE_IN_COMMUNICATION
                    @Suppress("DEPRECATION")
                    am.isSpeakerphoneOn = false
                }
            }
        } catch (e: Throwable) {
            Timber.w(e, "ProximityAudioRouter: Failed to route audio to earpiece")
        }

        onEarpieceModeChanged(true)
    }

    private fun switchToSpeaker() {
        if (!isEarpieceActive) return
        isEarpieceActive = false

        resetAudioMode()
        onEarpieceModeChanged(false)
    }

    private fun resetAudioMode() {
        val am = audioManager ?: return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                am.clearCommunicationDevice()
            }
            am.mode = AudioManager.MODE_NORMAL
            @Suppress("DEPRECATION")
            am.isSpeakerphoneOn = false
        } catch (e: Throwable) {
            Timber.w(e, "ProximityAudioRouter: Failed to restore speaker mode")
        }
    }

    private fun acquireWakeLock() {
        try {
            if (wakeLock?.isHeld == false) {
                wakeLock?.acquire(WAKELOCK_TIMEOUT_MS)
            }
        } catch (e: Throwable) {
            Timber.w(e, "ProximityAudioRouter: Failed to acquire wake lock")
        }
    }

    private fun releaseWakeLock() {
        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
        } catch (e: Throwable) {
            Timber.w(e, "ProximityAudioRouter: Failed to release wake lock")
        }
    }

    fun isHeadsetOrBluetoothConnected(): Boolean {
        val am = audioManager ?: return false
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val devices = am.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
                devices.any { device ->
                    when (device.type) {
                        AudioDeviceInfo.TYPE_WIRED_HEADSET,
                        AudioDeviceInfo.TYPE_WIRED_HEADPHONES,
                        AudioDeviceInfo.TYPE_BLUETOOTH_A2DP,
                        AudioDeviceInfo.TYPE_BLUETOOTH_SCO,
                        AudioDeviceInfo.TYPE_USB_HEADSET,
                        AudioDeviceInfo.TYPE_USB_DEVICE,
                        -> true
                        else -> false
                    }
                }
            } else {
                @Suppress("DEPRECATION")
                am.isWiredHeadsetOn || am.isBluetoothA2dpOn || am.isBluetoothScoOn
            }
        } catch (e: Throwable) {
            false
        }
    }

    companion object {
        private const val PROXIMITY_THRESHOLD_CM = 5.0f
        private const val SWITCH_DEBOUNCE_MS = 300L
        private const val WAKELOCK_TIMEOUT_MS = 10 * 60 * 1000L
    }
}
