package must.kdroiders.hustlehub.core.security

import android.content.SharedPreferences
import must.kdroiders.hustlehub.ui.features.chat.data.remote.KeyExchangeApiService
import must.kdroiders.hustlehub.ui.features.chat.data.remote.dto.PublicKeyRequest
import timber.log.Timber
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Orchestrates ECDH key exchange for a conversation:
 * upload our key → fetch peer key → derive shared AES secret → cache it.
 * Returns null if the peer hasn't uploaded their key yet.
 */
@Singleton
class KeyExchangeHandler
    @Inject
    constructor(
        private val cryptoManager: CryptoManager,
        private val keyExchangeApiService: KeyExchangeApiService,
        private val encryptedPrefs: SharedPreferences,
    ) {
        companion object {
            private const val SECRET_KEY_PREFIX = "shared_secret_"
            private const val MASTER_DEVICE_KEY_PREFIX = "master_device_secret_"
        }

        /** Returns cached or freshly-derived [SecretKey], or null if peer key missing. */
        suspend fun ensureKeysExchanged(conversationId: String): SecretKey? {
            getCachedSecret(conversationId)?.let { return it }

            return try {
                val ourKeyPair = cryptoManager.getOrCreateKeyPair(conversationId)

                // Upload our public key
                val encodedPublicKey = cryptoManager.encodePublicKey(ourKeyPair.public)
                keyExchangeApiService.uploadPublicKey(
                    conversationId = conversationId,
                    request = PublicKeyRequest(publicKey = encodedPublicKey),
                )

                // Fetch peer's public key
                val peerResponse = keyExchangeApiService.getPeerPublicKey(conversationId)
                val peerKeyData = peerResponse.data ?: run {
                    Timber.d("Peer key not available yet for: %s", conversationId)
                    return null
                }
                val rawPeerKey = peerKeyData.publicKey ?: run {
                    Timber.d("Peer public key string is null for: %s", conversationId)
                    return null
                }

                // Derive + cache shared secret
                val peerPublicKey = cryptoManager.decodePublicKey(rawPeerKey)
                val sharedSecret = cryptoManager.deriveSharedSecret(
                    privateKey = ourKeyPair.private,
                    peerPublicKey = peerPublicKey,
                )
                cacheSecret(conversationId, sharedSecret)
                Timber.d("Key exchange complete for: %s", conversationId)
                sharedSecret
            } catch (e: Exception) {
                Timber.e(e, "Key exchange failed for: %s", conversationId)
                null
            }
        }

        fun getCachedSecret(conversationId: String): SecretKey? {
            val encoded = encryptedPrefs.getString(
                "$SECRET_KEY_PREFIX$conversationId",
                null,
            ) ?: return null
            return SecretKeySpec(Base64Util.decode(encoded), "AES")
        }

        /** Returns cached shared secret OR master device secret for local disk encryption fallback. */
        fun getOrGenerateLocalSecret(conversationId: String): SecretKey {
            getCachedSecret(conversationId)?.let { return it }

            val masterAlias = "$MASTER_DEVICE_KEY_PREFIX$conversationId"
            val encoded = encryptedPrefs.getString(masterAlias, null)
            if (encoded != null) {
                return SecretKeySpec(Base64Util.decode(encoded), "AES")
            }

            val keyGen = KeyGenerator.getInstance("AES")
            keyGen.init(256)
            val secretKey = keyGen.generateKey()
            val newEncoded = Base64Util.encodeToString(secretKey.encoded)
            encryptedPrefs.edit().putString(masterAlias, newEncoded).apply()
            return secretKey
        }

        private fun cacheSecret(
            conversationId: String,
            secretKey: SecretKey,
        ) {
            val encoded = Base64Util.encodeToString(secretKey.encoded)
            encryptedPrefs
                .edit()
                .putString("$SECRET_KEY_PREFIX$conversationId", encoded)
                .apply()
        }

        fun clearCachedSecret(conversationId: String) {
            encryptedPrefs
                .edit()
                .remove("$SECRET_KEY_PREFIX$conversationId")
                .remove("$MASTER_DEVICE_KEY_PREFIX$conversationId")
                .apply()
        }
    }
