# 🔐 Security Architecture & E2EE Documentation

This document outlines the security architecture, End-to-End Encryption (E2EE) implementation, key management, and network hardening measures in **HustleHub**.

---

## 1. End-to-End Encryption (E2EE) Chat Architecture

HustleHub implements zero-knowledge, client-side message encryption. All chat message contents are encrypted on-device before transmission over WebSocket (STOMP) and decrypted on the receiving device. The Spring Boot backend acts purely as a relay and stores ciphertext.

### Cryptographic Primitives
- **Key Exchange**: ECDH (Elliptic Curve Diffie-Hellman) over P-256 (`secp256r1`).
- **Key Storage**: Private keys are stored in `AndroidKeyStore` (hardware-backed HSM/TEE).
- **Symmetric Cipher**: AES-256-GCM (`AES/GCM/NoPadding`).
  - **IV**: 12-byte cryptographically random Initialization Vector generated fresh per message.
  - **Auth Tag**: 128-bit authentication tag ensuring confidentiality and integrity.
- **Key Derivation**: Raw ECDH shared secret hashed via SHA-256 to produce uniform 256-bit AES keys.

---

## 2. Key Exchange Workflow

```
User A (Alice)                                Backend Server                                User B (Bob)
      │                                             │                                             │
      ├── 1. Generate ECDH P-256 key pair           │                                             │
      ├── 2. POST /conversations/{id}/keys ────────►│                                             │
      │      (Uploads Alice Public Key)             ├── 3. POST /conversations/{id}/keys ─────────┤
      │                                             │      (Uploads Bob Public Key)               │
      ├── 4. GET /conversations/{id}/keys ─────────►│                                             │
      │◄─ 5. Receives Bob Public Key ───────────────┤                                             │
      │                                             │◄─ 6. GET /conversations/{id}/keys ──────────┤
      │                                             ├── 7. Receives Alice Public Key ────────────►│
      │                                             │                                             │
      ├── 8. Derive Shared Secret (ECDH + SHA-256)  │                                             ├── 9. Derive Shared Secret (ECDH + SHA-256)
      └── 10. Cache in EncryptedSharedPreferences   │                                             └── 11. Cache in EncryptedSharedPreferences
```

### Components
- **`CryptoManager.kt`**: Low-level cryptographic operations (`AndroidKeyStore` key pair generation, ECDH key agreement, AES-256-GCM encrypt/decrypt). Includes JVM unit test fallback for non-Android environments.
- **`KeyExchangeHandler.kt`**: High-level manager orchestrating key upload, peer key retrieval, and caching shared secrets in `EncryptedSharedPreferences`.
- **`KeyExchangeApiService.kt`**: Retrofit REST endpoints for uploading and fetching public keys.

---

## 3. Network Hardening & Certificate Pinning

### Network Security Configuration
`res/xml/network_security_config.xml`:
- **Production**: Blocks all cleartext (unencrypted HTTP) traffic app-wide. Only trusts system CAs + pinned certificate.
- **Debug**: Permits user-installed CAs (for development proxying via Charles / mitmproxy).

### Certificate Pinning
Configured in `NetworkModule.kt` for release builds (`!BuildConfig.DEBUG`):
```kotlin
val certificatePinner = CertificatePinner.Builder()
    .add("api.hustlehub.app", "sha256/...")
    .build()
```
*Note: To extract the SHA-256 pin from your production server's TLS certificate, run:*
```bash
openssl s_client -connect api.hustlehub.app:443 </dev/null 2>/dev/null \
  | openssl x509 -pubkey -noout \
  | openssl pkey -pubin -outform der \
  | openssl dgst -sha256 -binary \
  | openssl enc -base64
```

---

## 4. Local Data Security & Storage

- **Local DB (Room v5)**: `MessageEntity` includes `isEncrypted`, `iv`, and `authTag` columns with non-destructive `MIGRATION_4_5`.
- **Shared Preferences**: Sensitive credentials and E2EE shared secrets are stored via Jetpack Security `EncryptedSharedPreferences` backed by `MasterKey` (AES-256-GCM).

---

## 5. Security Unit Tests

Unit tests are located in `app/src/test/java/must/kdroiders/hustlehub/core/security/CryptoManagerTest.kt`:
- Verifies AES-256-GCM encrypt/decrypt round trips.
- Asserts unique 12-byte IV generation per encryption call.
- Validates failure and exception throwing when decrypting with mismatched keys.
- Confirms symmetric shared secret agreement between Alice and Bob key pairs.
