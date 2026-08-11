package must.kdroiders.hustlehub.core.security

import android.content.SharedPreferences
import android.util.Base64
import must.kdroiders.hustlehub.ui.features.chat.data.remote.KeyExchangeApiService
import must.kdroiders.hustlehub.ui.features.chat.data.remote.dto.PublicKeyRequest
import timber.log.Timber
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

                // Derive + cache shared secret
                val peerPublicKey = cryptoManager.decodePublicKey(peerKeyData.publicKey)
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
            return SecretKeySpec(Base64.decode(encoded, Base64.NO_WRAP), "AES")
        }

        private fun cacheSecret(
            conversationId: String,
            secretKey: SecretKey,
        ) {
            val encoded = Base64.encodeToString(secretKey.encoded, Base64.NO_WRAP)
            encryptedPrefs
                .edit()
                .putString("$SECRET_KEY_PREFIX$conversationId", encoded)
                .apply()
        }

        fun clearCachedSecret(conversationId: String) {
            encryptedPrefs
                .edit()
                .remove("$SECRET_KEY_PREFIX$conversationId")
                .apply()
        }
    }
