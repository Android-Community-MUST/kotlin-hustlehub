package must.kdroiders.hustlehub.core.security

import android.content.Context
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import must.kdroiders.hustlehub.core.api.ApiResponse
import must.kdroiders.hustlehub.ui.features.chat.data.remote.KeyExchangeApiService
import must.kdroiders.hustlehub.ui.features.chat.data.remote.dto.PeerKeyResponse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import java.security.KeyPairGenerator

@RunWith(RobolectricTestRunner::class)
class KeyExchangeHandlerTest {
    private val cryptoManager = CryptoManager()
    private val keyExchangeApiService = mockk<KeyExchangeApiService>()
    private lateinit var keyExchangeHandler: KeyExchangeHandler

    @Before
    fun setUp() {
        val context = RuntimeEnvironment.getApplication()
        val prefs = context.getSharedPreferences("test_e2ee_prefs", Context.MODE_PRIVATE)
        prefs.edit().clear().commit()

        keyExchangeHandler = KeyExchangeHandler(
            cryptoManager = cryptoManager,
            keyExchangeApiService = keyExchangeApiService,
            encryptedPrefs = prefs,
        )
    }

    @Test
    fun `ensureKeysExchanged derives shared secret and caches it when peer key exists`() =
        runTest {
            val conversationId = "conv_123"

            val ecKeyGen = KeyPairGenerator.getInstance("EC")
            ecKeyGen.initialize(256)
            val peerKeyPair = ecKeyGen.generateKeyPair()
            val peerEncodedKey = cryptoManager.encodePublicKey(peerKeyPair.public)

            coEvery {
                keyExchangeApiService.uploadPublicKey(conversationId, any())
            } returns ApiResponse(success = true, data = Unit, message = "Success")

            coEvery {
                keyExchangeApiService.getPeerPublicKey(conversationId)
            } returns ApiResponse(
                success = true,
                data = PeerKeyResponse(publicKey = peerEncodedKey, userId = "peer_user_1"),
                message = "Success",
            )

            val secretKey = keyExchangeHandler.ensureKeysExchanged(conversationId)

            assertNotNull(secretKey)
            assertEquals("AES", secretKey?.algorithm)

            coVerify(exactly = 1) { keyExchangeApiService.uploadPublicKey(conversationId, any()) }
            coVerify(exactly = 1) { keyExchangeApiService.getPeerPublicKey(conversationId) }

            val cachedSecret = keyExchangeHandler.getCachedSecret(conversationId)
            assertNotNull(cachedSecret)
            assertEquals(secretKey?.encoded?.contentToString(), cachedSecret?.encoded?.contentToString())
        }

    @Test
    fun `ensureKeysExchanged returns null when peer key is not uploaded yet`() =
        runTest {
            val conversationId = "conv_no_peer_key"

            coEvery {
                keyExchangeApiService.uploadPublicKey(conversationId, any())
            } returns ApiResponse(success = true, data = Unit, message = "Success")

            coEvery {
                keyExchangeApiService.getPeerPublicKey(conversationId)
            } returns ApiResponse(
                success = true,
                data = PeerKeyResponse(publicKey = null, userId = null),
                message = "Success",
            )

            val secretKey = keyExchangeHandler.ensureKeysExchanged(conversationId)

            assertNull(secretKey)
            assertNull(keyExchangeHandler.getCachedSecret(conversationId))
        }

    @Test
    fun `clearCachedSecret removes stored AES secret key`() =
        runTest {
            val conversationId = "conv_to_clear"

            val ecKeyGen = KeyPairGenerator.getInstance("EC")
            ecKeyGen.initialize(256)
            val peerKeyPair = ecKeyGen.generateKeyPair()
            val peerEncodedKey = cryptoManager.encodePublicKey(peerKeyPair.public)

            coEvery { keyExchangeApiService.uploadPublicKey(any(), any()) } returns ApiResponse(true, "Success", Unit)
            coEvery { keyExchangeApiService.getPeerPublicKey(conversationId) } returns ApiResponse(
                success = true,
                data = PeerKeyResponse(publicKey = peerEncodedKey, userId = "peer_user"),
                message = "Success",
            )

            val secretKey = keyExchangeHandler.ensureKeysExchanged(conversationId)
            assertNotNull(secretKey)

            keyExchangeHandler.clearCachedSecret(conversationId)
            assertNull(keyExchangeHandler.getCachedSecret(conversationId))
        }
}
