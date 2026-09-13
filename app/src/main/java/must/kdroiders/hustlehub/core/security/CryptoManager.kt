package must.kdroiders.hustlehub.core.security

import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.annotation.Keep
import timber.log.Timber
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.MessageDigest
import java.security.PrivateKey
import java.security.PublicKey
import java.security.spec.ECGenParameterSpec
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import javax.crypto.KeyAgreement
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton

/** All fields are Base64-encoded. */
@Keep
data class EncryptedPayload(
    val ciphertext: String,
    val iv: String,
    val authTag: String,
)

object Base64Util {
    fun encodeToString(bytes: ByteArray): String {
        return try {
            android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
        } catch (e: Throwable) {
            java.util.Base64
                .getEncoder()
                .encodeToString(bytes)
        }
    }

    fun decode(str: String): ByteArray {
        return try {
            android.util.Base64.decode(str, android.util.Base64.NO_WRAP)
        } catch (e: Throwable) {
            java.util.Base64
                .getDecoder()
                .decode(str)
        }
    }
}

/**
 * ECDH P-256 key generation + AES-256-GCM encrypt/decrypt.
 * Private keys stored in Android KeyStore (hardware-backed).
 */
@Singleton
class CryptoManager
    @Inject
    constructor(
        private val encryptedPrefs: SharedPreferences? = null,
    ) {
        companion object {
            private const val ANDROID_KEYSTORE = "AndroidKeyStore"
            private const val KEY_ALIAS_PREFIX = "hustlehub_e2ee_"
            private const val IDENTITY_KEY_ALIAS = "hustlehub_e2ee_identity_key"
            private const val EC_CURVE = "secp256r1"
            private const val KEY_AGREEMENT_ALGORITHM = "ECDH"
            private const val CIPHER_TRANSFORMATION = "AES/GCM/NoPadding"
            private const val GCM_TAG_LENGTH_BITS = 128
            private const val AES_KEY_LENGTH_BYTES = 32
            private const val PREF_SUFFIX_PRIVATE = "_priv"
            private const val PREF_SUFFIX_PUBLIC = "_pub"
        }

        private val keyStore: KeyStore? by lazy {
            try {
                KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
            } catch (e: Exception) {
                Timber.w("AndroidKeyStore not available in current environment")
                null
            }
        }

        /** Returns existing user identity key pair or generates a new ECDH P-256 identity key pair. */
        fun getOrCreateUserIdentityKeyPair(): KeyPair {
            val ks = keyStore
            return try {
                if (ks != null && ks.containsAlias(IDENTITY_KEY_ALIAS)) {
                    val privateKey = ks.getKey(IDENTITY_KEY_ALIAS, null) as? PrivateKey
                    val publicKey = ks.getCertificate(IDENTITY_KEY_ALIAS)?.publicKey
                    if (privateKey != null && publicKey != null) {
                        KeyPair(publicKey, privateKey)
                    } else {
                        loadKeyPairFromPrefs(IDENTITY_KEY_ALIAS) ?: generateKeyPair(IDENTITY_KEY_ALIAS)
                    }
                } else {
                    loadKeyPairFromPrefs(IDENTITY_KEY_ALIAS) ?: generateKeyPair(IDENTITY_KEY_ALIAS)
                }
            } catch (e: Exception) {
                Timber.w(e, "Error retrieving user identity key pair from KeyStore")
                loadKeyPairFromPrefs(IDENTITY_KEY_ALIAS) ?: generateKeyPair(IDENTITY_KEY_ALIAS)
            }
        }

        /** Returns existing key pair or generates a new ECDH P-256 pair. */
        fun getOrCreateKeyPair(conversationId: String): KeyPair {
            val alias = "$KEY_ALIAS_PREFIX$conversationId"
            val ks = keyStore
            return try {
                if (ks != null && ks.containsAlias(alias)) {
                    val privateKey = ks.getKey(alias, null) as? PrivateKey
                    val publicKey = ks.getCertificate(alias)?.publicKey
                    if (privateKey != null && publicKey != null) {
                        KeyPair(publicKey, privateKey)
                    } else {
                        loadKeyPairFromPrefs(alias) ?: generateKeyPair(alias)
                    }
                } else {
                    loadKeyPairFromPrefs(alias) ?: generateKeyPair(alias)
                }
            } catch (e: Exception) {
                Timber.w(e, "Error retrieving key pair from KeyStore for alias: %s", alias)
                loadKeyPairFromPrefs(alias) ?: generateKeyPair(alias)
            }
        }

        private fun generateKeyPair(alias: String): KeyPair {
            if (keyStore != null && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                try {
                    val parameterSpec = KeyGenParameterSpec
                        .Builder(
                            alias,
                            KeyProperties.PURPOSE_AGREE_KEY,
                        ).setAlgorithmParameterSpec(ECGenParameterSpec(EC_CURVE))
                        .build()

                    val keyPairGenerator = KeyPairGenerator.getInstance(
                        KeyProperties.KEY_ALGORITHM_EC,
                        ANDROID_KEYSTORE,
                    )
                    keyPairGenerator.initialize(parameterSpec)
                    return keyPairGenerator.generateKeyPair().also {
                        Timber.tag("CHAT_CRYPTO").d("[CHAT_KEY_GEN] Generated AndroidKeyStore ECDH key pair for alias: %s", alias)
                    }
                } catch (e: Exception) {
                    Timber.w(e, "Failed to generate AndroidKeyStore key pair for alias %s, falling back to standard EC generator", alias)
                }
            }

            // Fallback: persist key pair in EncryptedSharedPreferences so it survives restarts on API < 31.
            val keyPairGenerator = KeyPairGenerator.getInstance("EC")
            keyPairGenerator.initialize(ECGenParameterSpec(EC_CURVE))
            val keyPair = keyPairGenerator.generateKeyPair()
            Timber.tag("CHAT_CRYPTO").d("[CHAT_KEY_GEN] Generated standard ECDH key pair for alias: %s", alias)

            val prefs = encryptedPrefs
            if (prefs != null) {
                val privB64 = Base64Util.encodeToString(keyPair.private.encoded)
                val pubB64 = Base64Util.encodeToString(keyPair.public.encoded)
                prefs
                    .edit()
                    .putString("$alias$PREF_SUFFIX_PRIVATE", privB64)
                    .putString("$alias$PREF_SUFFIX_PUBLIC", pubB64)
                    .apply()
                Timber.tag("CHAT_CRYPTO").d("[CHAT_KEY_GEN] Persisted software EC key pair to EncryptedPrefs for alias: %s", alias)
            } else {
                Timber.tag("CHAT_CRYPTO").w("[CHAT_KEY_GEN] No EncryptedPrefs available — software EC key pair for alias %s is ephemeral (will be lost on restart)", alias)
            }
            return keyPair
        }

        private fun loadKeyPairFromPrefs(alias: String): KeyPair? {
            val prefs = encryptedPrefs ?: return null
            val privB64 = prefs.getString("$alias$PREF_SUFFIX_PRIVATE", null) ?: return null
            val pubB64 = prefs.getString("$alias$PREF_SUFFIX_PUBLIC", null) ?: return null
            return try {
                val kf = KeyFactory.getInstance("EC")
                val privateKey = kf.generatePrivate(PKCS8EncodedKeySpec(Base64Util.decode(privB64)))
                val publicKey = kf.generatePublic(X509EncodedKeySpec(Base64Util.decode(pubB64)))
                Timber.tag("CHAT_CRYPTO").d("[CHAT_KEY_GEN] Reloaded persisted software EC key pair from EncryptedPrefs for alias: %s", alias)
                KeyPair(publicKey, privateKey)
            } catch (e: Exception) {
                Timber.tag("CHAT_CRYPTO").w(e, "[CHAT_KEY_GEN] Failed to reload persisted key pair for alias %s — will regenerate", alias)
                null
            }
        }

        /** Encodes a public key to Base64 for the backend. */
        fun encodePublicKey(publicKey: PublicKey): String = Base64Util.encodeToString(publicKey.encoded)

        /** Decodes a Base64-encoded public key from the backend. */
        fun decodePublicKey(base64Key: String): PublicKey {
            val keyBytes = Base64Util.decode(base64Key)
            val keySpec = X509EncodedKeySpec(keyBytes)
            return KeyFactory.getInstance("EC").generatePublic(keySpec)
        }

        /** Derives a 256-bit AES key via ECDH + SHA-256 (optionally salted with conversationId). */
        fun deriveSharedSecret(
            privateKey: PrivateKey,
            peerPublicKey: PublicKey,
            conversationId: String? = null,
        ): SecretKey {
            val peerKeyBase64 = encodePublicKey(peerPublicKey)
            Timber.tag("CHAT_CRYPTO").d(
                "[CHAT_KEY_DERIVE] Deriving shared secret. PeerPublicKey: %s, conversationId salt: %s",
                peerKeyBase64,
                conversationId,
            )
            val keyAgreement = KeyAgreement.getInstance(KEY_AGREEMENT_ALGORITHM)
            keyAgreement.init(privateKey)
            keyAgreement.doPhase(peerPublicKey, true)

            val rawSecret = keyAgreement.generateSecret()
            val md = MessageDigest.getInstance("SHA-256")
            md.update(rawSecret)
            if (!conversationId.isNullOrBlank()) {
                md.update(conversationId.toByteArray(Charsets.UTF_8))
            }
            val hashedSecret = md.digest()

            val secretKey = SecretKeySpec(hashedSecret.copyOf(AES_KEY_LENGTH_BYTES), "AES")
            Timber.tag("CHAT_CRYPTO").d(
                "[CHAT_KEY_DERIVE] Derived AES SecretKey (Base64): %s",
                Base64Util.encodeToString(secretKey.encoded),
            )
            return secretKey
        }

        /** Encrypts plaintext with AES-256-GCM. Fresh 12-byte IV per call. */
        fun encrypt(
            plaintext: String,
            secretKey: SecretKey,
        ): EncryptedPayload {
            val keyBase64 = Base64Util.encodeToString(secretKey.encoded)
            Timber.tag("CHAT_CRYPTO").d(
                "[CHAT_ENCRYPT] Encrypting plaintext (length=%d) with SecretKey: %s",
                plaintext.length,
                keyBase64,
            )
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

            val payload = EncryptedPayload(
                ciphertext = Base64Util.encodeToString(ciphertextOnly),
                iv = Base64Util.encodeToString(iv),
                authTag = Base64Util.encodeToString(authTag),
            )
            Timber.tag("CHAT_CRYPTO").d(
                "[CHAT_ENCRYPT] Encryption success. Ciphertext: %s, IV: %s, AuthTag: %s",
                payload.ciphertext,
                payload.iv,
                payload.authTag,
            )
            return payload
        }

        /** Decrypts an [EncryptedPayload]. Throws AEADBadTagException on tamper. */
        fun decrypt(
            payload: EncryptedPayload,
            secretKey: SecretKey,
        ): String {
            val keyBase64 = runCatching { Base64Util.encodeToString(secretKey.encoded) }.getOrDefault("<unreadable>")
            Timber.tag("CHAT_CRYPTO").d(
                "[CHAT_DECRYPT] Attempting decryption. SecretKey: %s, IV: %s, AuthTag: %s, Ciphertext: %s",
                keyBase64,
                payload.iv,
                payload.authTag,
                payload.ciphertext,
            )
            return try {
                val iv = Base64Util.decode(payload.iv)
                val ciphertextOnly = Base64Util.decode(payload.ciphertext)
                val authTag = Base64Util.decode(payload.authTag)

                val ciphertextWithTag = ciphertextOnly + authTag

                val cipher = Cipher.getInstance(CIPHER_TRANSFORMATION)
                cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv))

                val decrypted = String(cipher.doFinal(ciphertextWithTag), Charsets.UTF_8)
                Timber.tag("CHAT_CRYPTO").d(
                    "[CHAT_DECRYPT_SUCCESS] Decrypted message successfully: '%s'",
                    decrypted,
                )
                decrypted
            } catch (e: Exception) {
                Timber.tag("CHAT_CRYPTO").e(
                    e,
                    "[CHAT_DECRYPT_FAILURE] Decryption failed! Reason: %s (Exception: %s). SecretKey used: %s, IV: %s, AuthTag: %s, Ciphertext: %s",
                    e.message,
                    e.javaClass.simpleName,
                    keyBase64,
                    payload.iv,
                    payload.authTag,
                    payload.ciphertext,
                )
                throw e
            }
        }

        fun hasKeyPair(conversationId: String): Boolean = keyStore?.containsAlias("$KEY_ALIAS_PREFIX$conversationId") ?: false

        fun deleteKeyPair(conversationId: String) {
            val alias = "$KEY_ALIAS_PREFIX$conversationId"
            if (keyStore?.containsAlias(alias) == true) {
                keyStore?.deleteEntry(alias)
                Timber.d("Deleted ECDH key pair for alias: %s", alias)
            }
        }
    }
