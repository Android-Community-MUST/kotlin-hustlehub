package must.kdroiders.hustlehub.core.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import timber.log.Timber
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.MessageDigest
import java.security.PrivateKey
import java.security.PublicKey
import java.security.spec.ECGenParameterSpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import javax.crypto.KeyAgreement
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton

/** All fields are Base64-encoded. */
data class EncryptedPayload(
    val ciphertext: String,
    val iv: String,
    val authTag: String,
)

/**
 * ECDH P-256 key generation + AES-256-GCM encrypt/decrypt.
 * Private keys stored in Android KeyStore (hardware-backed).
 */
@Singleton
class CryptoManager @Inject constructor() {

    companion object {
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val KEY_ALIAS_PREFIX = "hustlehub_e2ee_"
        private const val EC_CURVE = "secp256r1"
        private const val KEY_AGREEMENT_ALGORITHM = "ECDH"
        private const val CIPHER_TRANSFORMATION = "AES/GCM/NoPadding"
        private const val GCM_TAG_LENGTH_BITS = 128
        private const val AES_KEY_LENGTH_BYTES = 32
    }

    private val keyStore: KeyStore? by lazy {
        try {
            KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        } catch (e: Exception) {
            Timber.w("AndroidKeyStore not available in current environment")
            null
        }
    }

    /** Returns existing key pair or generates a new ECDH P-256 pair. */
    fun getOrCreateKeyPair(conversationId: String): KeyPair {
        val alias = "$KEY_ALIAS_PREFIX$conversationId"
        val ks = keyStore
        return if (ks != null && ks.containsAlias(alias)) {
            val privateKey = ks.getKey(alias, null) as PrivateKey
            val publicKey = ks.getCertificate(alias).publicKey
            KeyPair(publicKey, privateKey)
        } else {
            generateKeyPair(alias)
        }
    }

    private fun generateKeyPair(alias: String): KeyPair {
        return if (keyStore != null) {
            val parameterSpec = KeyGenParameterSpec.Builder(
                alias,
                KeyProperties.PURPOSE_AGREE_KEY,
            ).setAlgorithmParameterSpec(ECGenParameterSpec(EC_CURVE))
                .build()

            val keyPairGenerator = KeyPairGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_EC,
                ANDROID_KEYSTORE,
            )
            keyPairGenerator.initialize(parameterSpec)
            keyPairGenerator.generateKeyPair().also {
                Timber.d("Generated AndroidKeyStore ECDH key pair for alias: %s", alias)
            }
        } else {
            // Fallback for JVM unit test environment
            val keyPairGenerator = KeyPairGenerator.getInstance("EC")
            keyPairGenerator.initialize(ECGenParameterSpec(EC_CURVE))
            keyPairGenerator.generateKeyPair().also {
                Timber.d("Generated in-memory ECDH key pair for alias: %s", alias)
            }
        }
    }

    /** Encodes a public key to Base64 for the backend. */
    fun encodePublicKey(publicKey: PublicKey): String =
        Base64.encodeToString(publicKey.encoded, Base64.NO_WRAP)

    /** Decodes a Base64-encoded public key from the backend. */
    fun decodePublicKey(base64Key: String): PublicKey {
        val keyBytes = Base64.decode(base64Key, Base64.NO_WRAP)
        val keySpec = X509EncodedKeySpec(keyBytes)
        return KeyFactory.getInstance("EC").generatePublic(keySpec)
    }

    /** Derives a 256-bit AES key via ECDH + SHA-256. */
    fun deriveSharedSecret(
        privateKey: PrivateKey,
        peerPublicKey: PublicKey,
    ): SecretKey {
        val keyAgreement = KeyAgreement.getInstance(KEY_AGREEMENT_ALGORITHM)
        keyAgreement.init(privateKey)
        keyAgreement.doPhase(peerPublicKey, true)

        val rawSecret = keyAgreement.generateSecret()
        val hashedSecret = MessageDigest.getInstance("SHA-256").digest(rawSecret)

        return SecretKeySpec(hashedSecret.copyOf(AES_KEY_LENGTH_BYTES), "AES")
    }

    /** Encrypts plaintext with AES-256-GCM. Fresh 12-byte IV per call. */
    fun encrypt(plaintext: String, secretKey: SecretKey): EncryptedPayload {
        val cipher = Cipher.getInstance(CIPHER_TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)

        val iv = cipher.iv
        val ciphertextWithTag =
            cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))

        // GCM appends auth tag to ciphertext — split for the DTO
        val tagLen = GCM_TAG_LENGTH_BITS / 8
        val ciphertextOnly =
            ciphertextWithTag.copyOfRange(0, ciphertextWithTag.size - tagLen)
        val authTag = ciphertextWithTag.copyOfRange(
            ciphertextWithTag.size - tagLen,
            ciphertextWithTag.size,
        )

        return EncryptedPayload(
            ciphertext = Base64.encodeToString(ciphertextOnly, Base64.NO_WRAP),
            iv = Base64.encodeToString(iv, Base64.NO_WRAP),
            authTag = Base64.encodeToString(authTag, Base64.NO_WRAP),
        )
    }

    /** Decrypts an [EncryptedPayload]. Throws AEADBadTagException on tamper. */
    fun decrypt(payload: EncryptedPayload, secretKey: SecretKey): String {
        val iv = Base64.decode(payload.iv, Base64.NO_WRAP)
        val ciphertextOnly = Base64.decode(payload.ciphertext, Base64.NO_WRAP)
        val authTag = Base64.decode(payload.authTag, Base64.NO_WRAP)

        val ciphertextWithTag = ciphertextOnly + authTag

        val cipher = Cipher.getInstance(CIPHER_TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv))

        return String(cipher.doFinal(ciphertextWithTag), Charsets.UTF_8)
    }

    fun hasKeyPair(conversationId: String): Boolean =
        keyStore?.containsAlias("$KEY_ALIAS_PREFIX$conversationId") ?: false

    fun deleteKeyPair(conversationId: String) {
        val alias = "$KEY_ALIAS_PREFIX$conversationId"
        if (keyStore?.containsAlias(alias) == true) {
            keyStore?.deleteEntry(alias)
            Timber.d("Deleted ECDH key pair for alias: %s", alias)
        }
    }
}
