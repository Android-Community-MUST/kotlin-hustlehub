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
            private const val PEER_PUBLIC_KEY_PREFIX = "peer_public_key_"
        }

        fun getOurIdentityPublicKey(): String {
            val keyPair = cryptoManager.getOrCreateUserIdentityKeyPair()
            return cryptoManager.encodePublicKey(keyPair.public)
        }

        fun getOurConversationPublicKey(conversationId: String): String {
            val keyPair = cryptoManager.getOrCreateKeyPair(conversationId)
            return cryptoManager.encodePublicKey(keyPair.public)
        }

        fun getCachedPeerPublicKey(conversationId: String): String? {
            return encryptedPrefs.getString("$PEER_PUBLIC_KEY_PREFIX$conversationId", null)
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
                Timber.tag("CHAT_KEY_EXCHANGE").d(
                    "[CHAT_KEY_EXCHANGE] syncUserPublicKey: Identity PublicKey: %s, Already synced: %b",
                    encodedPublicKey,
                    isSynced,
                )
                if (!isSynced) {
                    keyExchangeApiService.uploadUserPublicKey(PublicKeyRequest(publicKey = encodedPublicKey))
                    encryptedPrefs.edit().putString(USER_PUBLIC_KEY_SYNCED, encodedPublicKey).apply()
                    Timber.tag("CHAT_KEY_EXCHANGE").d("[CHAT_KEY_EXCHANGE] User public identity key uploaded to backend successfully")
                }
            } catch (e: Exception) {
                Timber.tag("CHAT_KEY_EXCHANGE").w(e, "[CHAT_KEY_EXCHANGE] Failed to synchronize user public identity key")
            }
        }

        /** Returns cached or freshly-derived [SecretKey], or null if peer key missing. */
        suspend fun ensureKeysExchanged(
            conversationId: String,
            otherUserId: String? = null,
        ): SecretKey? {
            Timber.tag("CHAT_KEY_EXCHANGE").d(
                "[CHAT_KEY_EXCHANGE] ensureKeysExchanged initiated for convId=%s, otherUserId=%s",
                conversationId,
                otherUserId,
            )
            getCachedSecret(conversationId)?.let {
                val encoded = runCatching { Base64Util.encodeToString(it.encoded) }.getOrDefault("<unknown>")
                Timber.tag("CHAT_KEY_EXCHANGE").d(
                    "[CHAT_KEY_EXCHANGE] Found existing cached SecretKey for convId=%s: %s",
                    conversationId,
                    encoded,
                )
                return it
            }

            return try {
                val ourIdentityKeyPair = cryptoManager.getOrCreateUserIdentityKeyPair()
                val ourKeyPair = cryptoManager.getOrCreateKeyPair(conversationId)

                val ourIdentityPubKey = cryptoManager.encodePublicKey(ourIdentityKeyPair.public)
                val ourConvPubKey = cryptoManager.encodePublicKey(ourKeyPair.public)
                Timber.tag("CHAT_KEY_EXCHANGE").d(
                    "[CHAT_KEY_EXCHANGE] Our Identity PublicKey: %s, Our Conversation PublicKey: %s",
                    ourIdentityPubKey,
                    ourConvPubKey,
                )

                // 1. Ensure our user identity key is synchronized
                syncUserPublicKey()

                // 2. Also upload our conversation public key for backward-compatibility
                try {
                    keyExchangeApiService.uploadPublicKey(
                        conversationId = conversationId,
                        request = PublicKeyRequest(publicKey = ourConvPubKey),
                    )
                    Timber.tag("CHAT_KEY_EXCHANGE").d(
                        "[CHAT_KEY_EXCHANGE] Uploaded conversation public key for convId=%s",
                        conversationId,
                    )
                } catch (e: Exception) {
                    Timber.tag("CHAT_KEY_EXCHANGE").w(e, "[CHAT_KEY_EXCHANGE] Could not upload conversation key for convId=%s", conversationId)
                }

                // 3. Fetch peer key: If otherUserId is known, prioritize the deterministic user identity key.
                var rawPeerKey: String? = null
                var isIdentityKey = false

                if (!otherUserId.isNullOrBlank()) {
                    try {
                        Timber.tag("CHAT_KEY_EXCHANGE").d(
                            "[CHAT_KEY_EXCHANGE] Fetching peer identity public key for otherUserId=%s...",
                            otherUserId,
                        )
                        val userPeerResponse = keyExchangeApiService.getUserPublicKey(otherUserId)
                        val userKey = userPeerResponse.data?.publicKey
                        if (!userKey.isNullOrBlank()) {
                            rawPeerKey = userKey
                            isIdentityKey = true
                            Timber.tag("CHAT_KEY_EXCHANGE").d(
                                "[CHAT_KEY_EXCHANGE] Fetched peer user identity public key for user %s: %s",
                                otherUserId,
                                rawPeerKey,
                            )
                        } else {
                            Timber.tag("CHAT_KEY_EXCHANGE").d(
                                "[CHAT_KEY_EXCHANGE] Peer user identity public key is empty for user %s",
                                otherUserId,
                            )
                        }
                    } catch (e: Exception) {
                        Timber.tag("CHAT_KEY_EXCHANGE").d(
                            "[CHAT_KEY_EXCHANGE] Peer user identity key not available for user %s: %s",
                            otherUserId,
                            e.message,
                        )
                    }
                }

                // Fallback to conversation peer key if identity key wasn't retrieved
                if (rawPeerKey.isNullOrBlank()) {
                    try {
                        Timber.tag("CHAT_KEY_EXCHANGE").d(
                            "[CHAT_KEY_EXCHANGE] Fetching conversation peer public key for convId=%s...",
                            conversationId,
                        )
                        val peerResponse = keyExchangeApiService.getPeerPublicKey(conversationId)
                        val convKey = peerResponse.data?.publicKey
                        if (!convKey.isNullOrBlank()) {
                            rawPeerKey = convKey
                            isIdentityKey = false
                            Timber.tag("CHAT_KEY_EXCHANGE").d(
                                "[CHAT_KEY_EXCHANGE] Fetched conversation peer public key: %s",
                                rawPeerKey,
                            )
                        } else {
                            Timber.tag("CHAT_KEY_EXCHANGE").d(
                                "[CHAT_KEY_EXCHANGE] Conversation peer public key is empty for convId=%s",
                                conversationId,
                            )
                        }
                    } catch (e: Exception) {
                        Timber.tag("CHAT_KEY_EXCHANGE").d(
                            "[CHAT_KEY_EXCHANGE] Conversation peer key not available for convId=%s: %s",
                            conversationId,
                            e.message,
                        )
                    }
                }

                if (rawPeerKey.isNullOrBlank()) {
                    Timber.tag("CHAT_KEY_EXCHANGE").w(
                        "[CHAT_KEY_EXCHANGE] Peer public key NOT available yet for convId=%s, otherUser=%s. Key exchange cannot proceed.",
                        conversationId,
                        otherUserId,
                    )
                    return null
                }

                // Cache peer public key for inspection & logging
                encryptedPrefs.edit().putString("$PEER_PUBLIC_KEY_PREFIX$conversationId", rawPeerKey).apply()

                // 4. Derive + cache shared secret using the MATCHING private key
                val peerPublicKey = cryptoManager.decodePublicKey(rawPeerKey)
                val privateKeyToUse = if (isIdentityKey) {
                    ourIdentityKeyPair.private
                } else {
                    ourKeyPair.private
                }

                Timber.tag("CHAT_KEY_EXCHANGE").d(
                    "[CHAT_KEY_EXCHANGE] Deriving shared secret using %s private key and peer public key (%s)",
                    if (isIdentityKey) "IDENTITY" else "CONVERSATION",
                    rawPeerKey,
                )

                val sharedSecret = cryptoManager.deriveSharedSecret(
                    privateKey = privateKeyToUse,
                    peerPublicKey = peerPublicKey,
                    conversationId = conversationId,
                )

                cacheSecret(conversationId, sharedSecret)
                Timber.tag("CHAT_KEY_EXCHANGE").d(
                    "[CHAT_KEY_EXCHANGE] Key exchange SUCCESS for convId=%s. SecretKey: %s (isIdentityKey=%b)",
                    conversationId,
                    Base64Util.encodeToString(sharedSecret.encoded),
                    isIdentityKey,
                )
                sharedSecret
            } catch (e: Exception) {
                Timber.tag("CHAT_KEY_EXCHANGE").e(
                    e,
                    "[CHAT_KEY_EXCHANGE] Key exchange FAILED for convId=%s, otherUser=%s: %s",
                    conversationId,
                    otherUserId,
                    e.message,
                )
                null
            }
        }

        fun getCachedSecret(conversationId: String): SecretKey? {
            val encoded = encryptedPrefs.getString(
                "$SECRET_KEY_PREFIX$conversationId",
                null,
            )
            return if (encoded != null) {
                SecretKeySpec(Base64Util.decode(encoded), "AES")
            } else {
                null
            }
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
                .remove("$PEER_PUBLIC_KEY_PREFIX$conversationId")
                .apply()
        }
    }
