package must.kdroiders.hustlehub.ui.features.chat.presentation.audio

import android.content.Context
import android.hardware.SensorManager
import android.media.AudioManager
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class ProximityAudioRouterTest {
    private lateinit var context: Context
    private lateinit var audioManager: AudioManager
    private lateinit var sensorManager: SensorManager

    @Before
    fun setUp() {
        context = RuntimeEnvironment.getApplication()
        audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }

    @After
    fun tearDown() {
        audioManager.mode = AudioManager.MODE_NORMAL
    }

    @Test
    fun `router initializes in non-earpiece mode`() {
        val router = ProximityAudioRouter(context)
        assertFalse(router.isEarpieceActive)
    }

    @Test
    fun `stop guarantees audioManager mode is reset to MODE_NORMAL`() {
        audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
        val router = ProximityAudioRouter(context)

        router.stop()

        assertEquals(AudioManager.MODE_NORMAL, audioManager.mode)
        assertFalse(router.isEarpieceActive)
    }

    @Test
    fun `release safely stops router and clears state`() {
        val router = ProximityAudioRouter(context)
        router.start()
        router.release()

        assertFalse(router.isEarpieceActive)
        assertEquals(AudioManager.MODE_NORMAL, audioManager.mode)
    }

    @Test
    fun `isHeadsetOrBluetoothConnected returns false by default in test environment`() {
        val router = ProximityAudioRouter(context)
        val connected = router.isHeadsetOrBluetoothConnected()
        assertFalse(connected)
    }
}
