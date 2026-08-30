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
            private const val USER_PUBLIC_KEY_SYNCED = "user_identity_public_key_synced"
        }

        /**
         * Ensures this device's public identity key is uploaded to the backend.
         * Safe to call on app startup, login, or before messaging.
         */
        suspend fun syncUserPublicKey() {
            try {
                val identityKeyPair = cryptoManager.getOrCreateUserIdentityKeyPair()
                val encodedPublicKey = cryptoManager.encodePublicKey(identityKeyPair.public)
                val isSynced = encryptedPrefs.getString(USER_PUBLIC_KEY_SYNCED, null) == encodedPublicKey
                if (!isSynced) {
                    keyExchangeApiService.uploadUserPublicKey(PublicKeyRequest(publicKey = encodedPublicKey))
                    encryptedPrefs.edit().putString(USER_PUBLIC_KEY_SYNCED, encodedPublicKey).apply()
                    Timber.d("User public identity key synchronized with backend")
                }
            } catch (e: Exception) {
                Timber.w(e, "Failed to synchronize user public identity key")
            }
        }

        /** Returns cached or freshly-derived [SecretKey], or null if peer key missing. */
        suspend fun ensureKeysExchanged(
            conversationId: String,
            otherUserId: String? = null,
        ): SecretKey? {
            getCachedSecret(conversationId)?.let { return it }

            return try {
                val ourIdentityKeyPair = cryptoManager.getOrCreateUserIdentityKeyPair()
                val ourKeyPair = cryptoManager.getOrCreateKeyPair(conversationId)

                // 1. Upload our conversation public key
                val encodedPublicKey = cryptoManager.encodePublicKey(ourKeyPair.public)
                try {
                    keyExchangeApiService.uploadPublicKey(
                        conversationId = conversationId,
                        request = PublicKeyRequest(publicKey = encodedPublicKey),
                    )
                } catch (e: Exception) {
                    Timber.w(e, "Could not upload conversation key for: %s", conversationId)
                }

                // 2. Ensure our user identity key is synchronized
                syncUserPublicKey()

                // 3. Fetch peer's public key (try conversation key first, fallback to user identity key)
                var rawPeerKey: String? = null
                try {
                    val peerResponse = keyExchangeApiService.getPeerPublicKey(conversationId)
                    rawPeerKey = peerResponse.data?.publicKey
                } catch (e: Exception) {
                    Timber.d("Conversation peer key not available: %s", e.message)
                }

                if (rawPeerKey.isNullOrBlank() && !otherUserId.isNullOrBlank()) {
                    try {
                        val userPeerResponse = keyExchangeApiService.getUserPublicKey(otherUserId)
                        rawPeerKey = userPeerResponse.data?.publicKey
                    } catch (e: Exception) {
                        Timber.d("User peer key not available for user %s: %s", otherUserId, e.message)
                    }
                }

                if (rawPeerKey.isNullOrBlank()) {
                    Timber.d("Peer key not available yet for conversation: %s, otherUser: %s", conversationId, otherUserId)
                    return null
                }

                // 4. Derive + cache shared secret
                val peerPublicKey = cryptoManager.decodePublicKey(rawPeerKey)
                val sharedSecret = try {
                    cryptoManager.deriveSharedSecret(
                        privateKey = ourKeyPair.private,
                        peerPublicKey = peerPublicKey,
                    )
                } catch (e: Exception) {
                    cryptoManager.deriveSharedSecret(
                        privateKey = ourIdentityKeyPair.private,
                        peerPublicKey = peerPublicKey,
                    )
                }

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

        /** Returns local master device secret for local key management. */
        fun getOrGenerateLocalSecret(conversationId: String): SecretKey {
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
