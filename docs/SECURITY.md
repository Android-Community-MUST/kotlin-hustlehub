# 🔐 Security Architecture & E2EE Documentation

This document outlines the end-to-end security architecture, cryptographic implementations, key management workflows, backend defense layers, and network hardening measures across **HustleHub**.

---

## 1. End-to-End Encryption (E2EE) Chat Architecture

HustleHub implements zero-knowledge, client-side message encryption. All chat messages are encrypted on-device before transmission over WebSocket (STOMP) and decrypted exclusively on the recipient's device. The Spring Boot backend acts strictly as an untrusted relay and stores ciphertext blobs.

### Cryptographic Primitives
- **Key Exchange**: ECDH (Elliptic Curve Diffie-Hellman) over NIST P-256 (`secp256r1`).
- **Key Storage**: Private keys are stored in `AndroidKeyStore` (hardware-backed HSM/TEE). Fallback software keys on API < 31 are encrypted and persisted in `EncryptedSharedPreferences`.
- **Symmetric Cipher**: AES-256-GCM (`AES/GCM/NoPadding`).
  - **IV (Initialization Vector)**: 12-byte cryptographically random nonce generated fresh per message via `SecureRandom`.
  - **Auth Tag**: 128-bit (16-byte) authentication tag guaranteeing confidentiality and integrity.
- **Key Derivation (KDF)**: Raw ECDH shared secret point is digested via SHA-256 with optional `conversationId` domain separation salt to produce uniform 256-bit AES keys.

---

## 2. Key Exchange Workflow & Dual-Tier Architecture

HustleHub employs a **dual-tier key exchange** mechanism to eliminate race conditions and enable asynchronous key discovery:

1. **Tier 1: User Identity Public Key (`/users/me/public-key` & `/users/{userId}/public-key`)**
   - Automatically uploaded upon app launch, user login, or key generation.
   - Tied to the user's account/device, allowing Alice to derive the shared secret even before Bob opens the chat.
2. **Tier 2: Conversation Public Key (`/conversations/{id}/keys`)**
   - Fallback and per-conversation key rotation channel.
   - Stored in the backend `conversations.metadata` JSONB column.

```
User A (Alice)                                Backend Server                                User B (Bob)
      │                                             │                                             │
      ├── 1. Generate ECDH P-256 Key Pair           │                                             │
      ├── 2. POST /users/me/public-key ────────────►│                                             │
      │      (Uploads Alice Identity Key)           │◄──────────── 3. POST /users/me/public-key ──┤
      │                                             │             (Uploads Bob Identity Key)      │
      ├── 4. Open chat: ensureKeysExchanged()       │                                             │
      ├── 5. GET /users/{bobId}/public-key ────────►│                                             │
      │◄─ 6. Returns Bob's Public Key ──────────────┤                                             │
      │                                             │◄──────────── 7. GET /users/{aliceId}/... ───┤
      │                                             ├───────────── 8. Returns Alice's Key ───────►│
      │                                             │                                             │
      ├── 9. Derive Shared Secret (ECDH + SHA-256)  │                                             ├── 10. Derive Shared Secret (ECDH + SHA-256)
      └── 11. Cache in EncryptedSharedPreferences   │                                             └── 12. Cache in EncryptedSharedPreferences
```

### Components
- **`CryptoManager.kt`**: Low-level cryptographic operations (`AndroidKeyStore` key pair generation, ECDH key agreement, AES-256-GCM encrypt/decrypt). Splits the GCM tag for transport and handles JVM unit test fallbacks.
- **`KeyExchangeHandler.kt`**: High-level manager orchestrating key synchronization, dual-tier peer key retrieval, caching shared secrets in `EncryptedSharedPreferences`, and candidate key derivation.
- **`KeyExchangeApiService.kt`**: Retrofit REST endpoints for uploading and fetching public identity and conversation keys.

---

## 3. Decryption Resilience & Candidate Key Strategy

To ensure historical and real-time messages can always be decrypted even across app reinstalls, key rotations, or migration between salted and unsalted secret derivations:

- **`KeyExchangeHandler.getCandidateSecrets()`** evaluates candidate keys in prioritized sequence:
  1. Active cached secret key for the conversation.
  2. Identity key pair + peer identity public key (with `conversationId` salt).
  3. Identity key pair + peer identity public key (unsalted).
  4. Conversation key pair + peer conversation public key (salted & unsalted).
- **`MessageEntity.toDecryptedDomain()`**: Automatically tries each candidate key until authentication tag validation passes. If a candidate key succeeds, the active cached secret is healed and updated.

---

## 4. Real-Time Transport & Wire DTOs

### WebSocket / STOMP Transport
All messages are sent over STOMP WebSocket to `/app/chat.send`:

```json
{
  "conversationId": "550e8400-e29b-41d4-a716-446655440000",
  "type": "TEXT",
  "encryptedContent": "k8Xn2P9q1Z3...",
  "iv": "dGVzdGl2MTIz",
  "authTag": "c2VjdXJldGFnMTI4",
  "content": null
}
```

### Backend Enforcement (`ChatWebSocketHandler.kt`)
- **Mandatory Encryption**: The backend strictly rejects any `TEXT` message lacking `encryptedContent`, `iv`, or `authTag`.
- **Blind Storage**: The database stores ciphertext in `content` and `{"iv":"...","authTag":"...","encrypted":true}` in `metadata`.
- **Push Notification Privacy**: The denormalized conversation preview (`conversations.last_message`) is set to `"[Encrypted message]"` so push notification services (FCM) never receive plaintext.

---

## 5. UI Security Transparency (WhatsApp-Style Notice)

Users receive visual confirmation of encryption at the chronological start of every chat:

- **`ChatEncryptionBanner.kt`**:
  - Centered rounded notice banner pinned at the beginning of the chat message list (anchored at the top of the `reverseLayout` `LazyColumn`).
  - Text: *"Messages are end-to-end encrypted. Only people in this chat can read, listen to, or share them."*.
  - Inline 🔒 lock icon rendered seamlessly with text centering using Compose `InlineTextContent`.
  - Sourced from theme extensions (`MaterialTheme.colorScheme.chatE2EeBannerContainer` and `chatE2EeBannerContent` in `Theme.kt` and `Color.kt`):
    - **Dark Mode**: Charcoal container (`#182229`) with golden amber text (`#FFD56B`).
    - **Light Mode**: Pale amber container (`#FFF4CC`) with deep amber contrast text (`#7A5C00`).

---

## 6. Backend Defense-in-Depth (Lane 10 Hardening)

### Rate Limiting (Token Bucket Algorithm)
- Lock-free thread-safe implementation using `AtomicLong` and atomic `compareAndSet`.
- **General Endpoints (`/api/v1/**`)**: 60 requests/min with refill rate of 1 token/sec.
- **Auth Endpoints (`/api/v1/auth/**`)**: Strict 5 requests/min to mitigate brute-force and credential stuffing.
- **Rejection**: Returns `HTTP 429 Too Many Requests` with `Retry-After: <seconds>`.

### OWASP Input Sanitization
- Service layer cleanses user-generated strings (bios, titles, task descriptions) before database storage via `SanitizationUtils`.
- Strips HTML tags (`<[^>]*>`) and SQL control characters (`['";\\]`).
- Cryptographic ciphertexts, IVs, and AuthTags are strictly exempted to preserve binary integrity.

### HTTP Security Headers
Every backend response includes standard defense headers:
- `X-Content-Type-Options: nosniff` (prevents MIME sniffing).
- `X-Frame-Options: DENY` (prevents clickjacking).
- `Strict-Transport-Security: max-age=31536000; includeSubDomains` (enforces TLS for 1 year).
- `Content-Security-Policy: default-src 'self'` (restricts asset origin).

---

## 7. Network Hardening & Certificate Pinning

### Network Security Configuration (`res/xml/network_security_config.xml`)
- **Production**: Blocks cleartext HTTP app-wide. Only trusts system CAs and pinned server certificates.
- **Debug**: Permits user-installed CAs for local proxy debugging (Charles / mitmproxy).

### Certificate Pinning
Release builds enforce public key pinning in `NetworkModule.kt`:
```kotlin
val certificatePinner = CertificatePinner.Builder()
    .add("api.hustlehub.app", "sha256/...")
    .build()
```

---

## 8. Local Data Security & Storage

- **Local DB (Room v5)**: `MessageEntity` stores `content` as ciphertext, alongside `iv`, `authTag`, and `isEncrypted` columns.
- **Key & Credential Storage**: Sensitive tokens and cached symmetric secrets are stored in Jetpack Security `EncryptedSharedPreferences` backed by `MasterKey` (AES-256-GCM).

---

## 9. Security Unit Tests

Unit tests in `app/src/test/java/must/kdroiders/hustlehub/core/security/`:
- **`CryptoManagerTest.kt`**:
  - Verifies AES-256-GCM encrypt/decrypt round trips.
  - Asserts unique 12-byte IV generation per encryption call.
  - Asserts that tampering with ciphertext or authTag throws `AEADBadTagException`.
  - Validates key derivation agreement between Alice and Bob key pairs.
- **`KeyExchangeHandlerTest.kt`**:
  - Tests user identity public key sync.
  - Validates fallback between identity keys and conversation keys.
  - Tests candidate secret recovery across rotated keys.
