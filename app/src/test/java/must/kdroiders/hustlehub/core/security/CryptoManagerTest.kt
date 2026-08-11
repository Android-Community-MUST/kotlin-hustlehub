package must.kdroiders.hustlehub.core.security

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.security.KeyPairGenerator
import javax.crypto.KeyGenerator

@RunWith(RobolectricTestRunner::class)
class CryptoManagerTest {

    private val cryptoManager = CryptoManager()

    @Test
    fun `encrypt and decrypt round trip produces original plaintext`() {
        val keyGen = KeyGenerator.getInstance("AES")
        keyGen.init(256)
        val secretKey = keyGen.generateKey()

        val originalText = "Hello HustleHub E2EE Chat! 🔒"
        val payload = cryptoManager.encrypt(originalText, secretKey)

        assertNotNull(payload.ciphertext)
        assertNotNull(payload.iv)
        assertNotNull(payload.authTag)

        val decryptedText = cryptoManager.decrypt(payload, secretKey)
        assertEquals(originalText, decryptedText)
    }

    @Test
    fun `two encryptions of same text produce different IVs and ciphertexts`() {
        val keyGen = KeyGenerator.getInstance("AES")
        keyGen.init(256)
        val secretKey = keyGen.generateKey()

        val text = "Same secret message"
        val payload1 = cryptoManager.encrypt(text, secretKey)
        val payload2 = cryptoManager.encrypt(text, secretKey)

        assertNotEquals(payload1.iv, payload2.iv)
        assertNotEquals(payload1.ciphertext, payload2.ciphertext)
    }

    @Test(expected = Exception::class)
    fun `decrypt with wrong secret key fails`() {
        val keyGen = KeyGenerator.getInstance("AES")
        keyGen.init(256)
        val secretKey1 = keyGen.generateKey()
        val secretKey2 = keyGen.generateKey()

        val text = "Top Secret"
        val payload = cryptoManager.encrypt(text, secretKey1)

        cryptoManager.decrypt(payload, secretKey2)
    }

    @Test
    fun `ECDH key agreement produces identical shared secrets on both sides`() {
        val ecKeyGen = KeyPairGenerator.getInstance("EC")
        ecKeyGen.initialize(256)

        val aliceKeyPair = ecKeyGen.generateKeyPair()
        val bobKeyPair = ecKeyGen.generateKeyPair()

        val aliceSharedSecret = cryptoManager.deriveSharedSecret(
            privateKey = aliceKeyPair.private,
            peerPublicKey = bobKeyPair.public,
        )

        val bobSharedSecret = cryptoManager.deriveSharedSecret(
            privateKey = bobKeyPair.private,
            peerPublicKey = aliceKeyPair.public,
        )

        assertEquals(
            aliceSharedSecret.encoded.contentToString(),
            bobSharedSecret.encoded.contentToString(),
        )
    }
}
