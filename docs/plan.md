# HustleHub Android — Development Lanes Plan

> **Stack**: Jetpack Compose · Kotlin 2.x · Hilt · Navigation 3 · Retrofit 2 · Room · OkHttp 4 · Firebase Auth · Material 3 Expressive
> **Branching**: `lane/feature-branch` → `working` → `dev` → `main`

---

## Overview

The Android frontend is organized into **development lanes** matching the backend's vertical slices. Each lane owns: Retrofit API services, Room entities/DAOs, repository implementations, use cases, ViewModels, Compose screens, and tests for that domain.

Lanes 1–7 cover the MVP features (auth, discovery, chat, reviews, media, notifications). This document defines the **Phase 2 lanes** that correspond to the backend's Lanes 8, 9, and 10.

```
Lane 8: Monetization & M-Pesa UI         ← Depends on backend Lane 8 APIs being deployed
Lane 9: Reporting & Pro Badge UI         ← Depends on backend Lanes 8 + 9
Lane 10: Security Hardening & E2EE Chat  ← Depends on backend Lane 10 (key exchange endpoints)
```

> **Parallelism**: Lanes 8, 9, and 10 can be developed in parallel by different Android engineers. Each lane is self-contained within its feature package. Lane 10 touches shared chat code (Lane 4) so coordinate with that lane owner.

---

## Lane 8 — Monetization & M-Pesa UI

**Branch**: `lane/monetization-ui`
**Depends on**: Backend Lane 8 deployed (STK push + subscription endpoints live)
**Backend APIs consumed**:
- `POST /api/v1/payments/stk-push`
- `GET /api/v1/payments/status/{checkoutRequestId}`
- `GET /api/v1/subscriptions/me`

### Context

Students upgrade to **HustleHub Pro** (~150 KES/month) or purchase **Featured Listing Boosts** (~50 KES/3 days) via M-Pesa Express (STK Push). The Android client triggers the STK push, polls for payment status, and updates the local user state upon successful payment. Pro users see expanded upload limits and a Verified Pro Badge across the app.

### Deliverables

#### Navigation
- Add `NavKey.Subscription` to `HustleNavKeys.kt` — navigable from Profile screen and discovery feed upgrade prompts
- Add `NavKey.PaymentStatus(checkoutRequestId: String)` — navigable after STK push trigger

#### Retrofit API Service
- **`PaymentApiService.kt`** (`feature/monetization/data/remote/`):
  ```kotlin
  @POST("payments/stk-push")
  suspend fun initiateStkPush(@Body request: StkPushRequest): ApiResponse<StkPushResponse>

  @GET("payments/status/{checkoutRequestId}")
  suspend fun getPaymentStatus(@Path("checkoutRequestId") id: String): ApiResponse<PaymentStatusResponse>

  @GET("subscriptions/me")
  suspend fun getMySubscription(): ApiResponse<SubscriptionResponse?>
  ```

#### DTOs (`feature/monetization/data/model/`)
- `StkPushRequest.kt` — `phoneNumber: String`, `planType: String` (`PRO` / `FEATURED`), `serviceId: String?`
- `StkPushResponse.kt` — `checkoutRequestId: String`, `responseDescription: String`
- `PaymentStatusResponse.kt` — `status: String` (`PENDING`, `COMPLETED`, `FAILED`), `mpesaReceiptNumber: String?`
- `SubscriptionResponse.kt` — `planType: String`, `status: String`, `startDate: String`, `endDate: String`, `isActive: Boolean`

#### Repository
- **`PaymentRepository.kt`** (`feature/monetization/domain/`):
  ```kotlin
  interface PaymentRepository {
      suspend fun initiateStkPush(phoneNumber: String, planType: String, serviceId: String?): Result<StkPushResponse>
      suspend fun getPaymentStatus(checkoutRequestId: String): Result<PaymentStatusResponse>
      suspend fun getMySubscription(): Result<SubscriptionResponse?>
  }
  ```
- **`PaymentRepositoryImpl.kt`** (`feature/monetization/data/`) — calls `PaymentApiService`, maps API responses to `Result<T>`

#### Use Cases (`feature/monetization/domain/usecase/`)
- `InitiateStkPushUseCase.kt` — validates phone number format (`07XX` → `254XX` conversion), calls repository
- `PollPaymentStatusUseCase.kt` — polls `getPaymentStatus()` every 3 seconds up to 10 attempts, emits `Flow<PaymentPollState>` with states: `Waiting`, `Completed(receipt)`, `Failed(reason)`, `Timeout`
- `GetSubscriptionUseCase.kt` — fetches current subscription, returns `null` if none active

#### ViewModel
- **`MonetizationViewModel.kt`** (`feature/monetization/presentation/`):
  ```kotlin
  @HiltViewModel
  class MonetizationViewModel @Inject constructor(
      private val initiateStkPush: InitiateStkPushUseCase,
      private val pollPaymentStatus: PollPaymentStatusUseCase,
      private val getSubscription: GetSubscriptionUseCase
  ) : ViewModel() {
      val subscriptionState: StateFlow<UiState<SubscriptionResponse?>>
      val paymentState: StateFlow<PaymentUiState>  // Idle, Prompting, Polling, Success, Failed

      fun triggerPayment(phoneNumber: String, planType: String, serviceId: String? = null)
      fun loadSubscription()
  }
  ```

#### Compose Screens (`feature/monetization/presentation/`)
- **`SubscriptionScreen.kt`**:
  - Hero section showing HustleHub Pro benefits (badge, priority ranking, 15 photos, auto-replies, analytics)
  - Side-by-side comparison card: Free Tier vs Pro Tier
  - Phone number input field (pre-filled from user profile, Kenyan format validation: `07XX...`)
  - "Upgrade to Pro — 150 KES/month" Material 3 FilledTonalButton
  - "Boost This Service — 50 KES/3 days" secondary button (when navigated from a specific service)
  - Bottom sheet with M-Pesa terms and payment info
- **`PaymentStatusScreen.kt`**:
  - Animated waiting state: "Check your phone for the M-Pesa PIN prompt..."
  - Polling indicator with step counter ("Verifying payment... attempt 3/10")
  - Success state: confetti animation + receipt number + "You're now a Pro Hustler!" message
  - Failed state: error message + "Try Again" button
  - Timeout state: "Payment not confirmed. Check your M-Pesa messages and contact support."

#### Pro Badge Integration (modify existing screens)
- **`HustleCard.kt`** (`sharedComposables/`):
  - Add `isVerifiedPro: Boolean` parameter
  - Render a Material 3 `AssistChip` with star icon and "PRO" label next to provider name when `true`
  - Badge color: `MaterialTheme.colorScheme.tertiary` with subtle glow effect
- **`ProfileScreen.kt`**:
  - Show "HustleHub Pro" status card with expiration date if user has active subscription
  - Show "Upgrade to Pro" CTA button if no active subscription
- **`ServiceDetailScreen.kt`**:
  - Display Pro badge next to provider name
  - For Pro providers: show "Featured" chip if `featuredUntil` is in the future
- **`CreateServiceScreen.kt`**:
  - Enforce portfolio photo limits: max 3 for Free, max 15 for Pro
  - Show upgrade prompt when Free user hits the 3-photo limit
  - Enable video pitch upload button only for Pro users

#### DataStore Updates
- **`UserPreferences.kt`** (`datastore/`):
  - Add `isProUser: Boolean` and `proExpiresAt: String?` fields
  - Update after subscription fetch to avoid redundant network calls

#### Hilt Module
- **`MonetizationModule.kt`** (`di/`):
  - Provide `PaymentApiService` via Retrofit
  - Bind `PaymentRepository` to `PaymentRepositoryImpl`

#### Tests
- `InitiateStkPushUseCaseTest` — phone format conversion, valid/invalid phone rejection
- `PollPaymentStatusUseCaseTest` — success after 3 polls, failure, timeout after 10 polls
- `MonetizationViewModelTest` — trigger payment flow, verify state transitions (Idle → Prompting → Polling → Success)

---

## Lane 9 — Reporting & Pro Badge UI

**Branch**: `lane/reporting-ui`
**Depends on**: Backend Lane 9 deployed (report submission + admin APIs live)
**Backend APIs consumed**:
- `POST /api/v1/reports`

### Context

Any user can report fraudulent or inappropriate services, providers, or chat behavior. The Android client provides a reporting flow accessible from service detail screens, user profiles, and chat screens. This lane also handles rendering admin-applied badges (verified, suspended indicators) surfaced via existing user/service API responses.

### Deliverables

#### Retrofit API Service
- **`ReportApiService.kt`** (`feature/reporting/data/remote/`):
  ```kotlin
  @POST("reports")
  suspend fun submitReport(@Body request: SubmitReportRequest): ApiResponse<Unit>
  ```

#### DTOs (`feature/reporting/data/model/`)
- `SubmitReportRequest.kt` — `reportedUserId: String?`, `reportedServiceId: String?`, `reason: String`, `details: String?`
- Report reasons enum: `FRAUD`, `HARASSMENT`, `SPAM`, `INAPPROPRIATE_CONTENT`, `UNFULFILLED_SERVICE`, `OTHER`

#### Repository
- **`ReportRepository.kt`** (`feature/reporting/domain/`) — interface
- **`ReportRepositoryImpl.kt`** (`feature/reporting/data/`) — calls `ReportApiService`

#### Use Cases
- `SubmitReportUseCase.kt` — validates at least one target (user or service) is provided, calls repository

#### ViewModel
- **`ReportViewModel.kt`** (`feature/reporting/presentation/`):
  - `val reportState: StateFlow<UiState<Unit>>` — Idle, Loading, Success, Error
  - `fun submitReport(reportedUserId: String?, reportedServiceId: String?, reason: String, details: String?)`

#### Compose UI (`feature/reporting/presentation/`)
- **`ReportBottomSheet.kt`**:
  - Material 3 `ModalBottomSheet` with:
    - Radio button group for report reason (Fraud, Harassment, Spam, Inappropriate Content, Unfulfilled Service, Other)
    - Optional details `OutlinedTextField` (max 500 chars)
    - "Submit Report" button
    - Success confirmation with "We'll review this within 24 hours" message
  - Launchable from:
    - `ServiceDetailScreen.kt` — "Report this service" menu item in top app bar overflow menu
    - `ProfileScreen.kt` — "Report this user" menu item (only on other users' profiles)
    - `ChatScreen.kt` — "Report this conversation" menu item

#### Suspension State Handling
- **`ApiClient.kt`** (modify `core/api/`):
  - Handle `403` responses with body containing `"Account suspended"` — navigate user to a `SuspendedScreen`
- **`SuspendedScreen.kt`** (`feature/auth/presentation/`):
  - Full-screen overlay: "Your account has been suspended"
  - Display suspension reason from API response
  - "Contact Support" button linking to email
  - Sign Out button

#### Model Updates (modify existing)
- **`User.kt`** (`data/model/`):
  - Add `isVerifiedPro: Boolean`, `role: String`, `isSuspended: Boolean` fields
- **`Service.kt`** (`data/model/`):
  - Add `featuredUntil: String?`, `providerIsVerifiedPro: Boolean` fields

#### Hilt Module
- **`ReportingModule.kt`** (`di/`) — provide `ReportApiService`, bind `ReportRepository`

#### Tests
- `SubmitReportUseCaseTest` — valid report submission, reject report with no target
- `ReportViewModelTest` — verify state transitions, error handling

---

## Lane 10 — Security Hardening & E2EE Chat [COMPLETED]

**Branch**: `lane/security-e2ee`
**Depends on**: Backend Lane 10 deployed (key exchange endpoints + encrypted message relay live)

### Context

This lane implements **client-side AES-256-GCM encryption** for all chat messages. Messages are encrypted on-device before being sent over WebSocket and decrypted on the receiving device. The backend stores only ciphertext and acts as a zero-knowledge relay. Key exchange uses **ECDH (Elliptic Curve Diffie-Hellman)** with keys stored in the **Android KeyStore**.

### Deliverables

#### Cryptography Layer (`core/security/`)
- **`CryptoManager.kt`**:
  - **Key generation**:
    - `generateKeyPair(): KeyPair` — generates ECDH P-256 key pair stored in Android KeyStore
    - `getOrCreateKeyPair(conversationId: String): KeyPair` — lazy initialization per conversation
  - **Key agreement**:
    - `deriveSharedSecret(privateKey: PrivateKey, peerPublicKey: PublicKey): SecretKey` — ECDH key agreement → HKDF to derive 256-bit AES key
  - **Encryption/Decryption**:
    ```kotlin
    data class EncryptedPayload(
        val ciphertext: String,  // Base64-encoded
        val iv: String,          // Base64-encoded 12-byte IV
        val authTag: String      // Base64-encoded (included in GCM ciphertext)
    )

    fun encrypt(plaintext: String, secretKey: SecretKey): EncryptedPayload
    fun decrypt(payload: EncryptedPayload, secretKey: SecretKey): String
    ```
  - **Cipher spec**: `AES/GCM/NoPadding`, 256-bit key, 12-byte random IV per message, 128-bit auth tag
  - All keys stored in `AndroidKeyStore` — never exposed to application memory in plaintext
- **`KeyExchangeHandler.kt`**:
  - Automatically exchanges ECDH public keys when a conversation is first opened
  - Caches the derived shared secret per conversation in an encrypted `SharedPreferences` (via EncryptedSharedPreferences from Jetpack Security)
  - Handles re-keying if a user reinstalls the app (detect missing local key → re-exchange)

#### Retrofit API Service (Key Exchange)
- **`KeyExchangeApiService.kt`** (`feature/chat/data/remote/`):
  ```kotlin
  @POST("conversations/{id}/keys")
  suspend fun uploadPublicKey(@Path("id") conversationId: String, @Body key: PublicKeyRequest): ApiResponse<Unit>

  @GET("conversations/{id}/keys")
  suspend fun getPeerPublicKey(@Path("id") conversationId: String): ApiResponse<PeerKeyResponse>
  ```

#### DTOs
- `PublicKeyRequest.kt` — `publicKey: String` (Base64-encoded ECDH public key)
- `PeerKeyResponse.kt` — `publicKey: String`, `userId: String`
- `EncryptedMessagePayload.kt` — `encryptedContent: String`, `iv: String`, `authTag: String`, `type: String`

#### Chat Integration (modify Lane 4 code)
- **`ChatWebSocketService.kt`** (`feature/chat/data/remote/`):
  - Before sending: call `CryptoManager.encrypt()` → send `EncryptedMessagePayload` over STOMP
  - On receive: parse `EncryptedMessagePayload` from STOMP frame → call `CryptoManager.decrypt()` → emit plaintext to ViewModel
- **`ChatViewModel.kt`** (`feature/chat/presentation/`):
  - On conversation open: call `KeyExchangeHandler.ensureKeysExchanged(conversationId)`
  - If key exchange fails (peer hasn't uploaded key yet): show "Waiting for encryption setup..." state
  - All message encryption/decryption happens transparently — Compose UI only ever sees plaintext
- **`ChatRepositoryImpl.kt`** (`feature/chat/data/`):
  - Room `CachedMessage` entity: store `encryptedContent` in local DB, decrypt on read
  - Alternatively: store plaintext locally (since device is already trusted), encrypt only for transit

#### Room Entity Update
- **`CachedMessage.kt`** (`local/entity/`):
  - Add `isEncrypted: Boolean` field
  - Add `iv: String?` and `authTag: String?` fields
  - Migration: `Room.databaseBuilder().addMigrations(MIGRATION_X_Y)` to add new columns

#### OkHttp Certificate Pinning (`core/api/`)
- **`ApiClient.kt`** — add `CertificatePinner` for the production API domain:
  ```kotlin
  val certificatePinner = CertificatePinner.Builder()
      .add("api.hustlehub.app", "sha256/AAAA...")  // pin production cert
      .build()
  ```
  - Only enable in release builds (`BuildConfig.DEBUG` check)
  - Document pin rotation procedure in `CONTRIBUTING.md`

#### Network Security Config
- **`network_security_config.xml`** (`res/xml/`):
  - Production: only trust system CAs + pinned certificates
  - Debug: additionally trust user-installed CAs (for Charles Proxy / mitmproxy during development)

#### DataStore Encryption
- **`UserPreferences.kt`** (`datastore/`):
  - Migrate from `Preferences DataStore` to `EncryptedSharedPreferences` for sensitive fields: `firebaseUid`, `authToken`, `proExpiresAt`
  - Non-sensitive fields (theme preference, onboarding complete) remain in regular DataStore

#### Hilt Module Updates
- **`SecurityModule.kt`** (`di/`):
  - Provide `CryptoManager` as singleton
  - Provide `KeyExchangeHandler` with `KeyExchangeApiService` dependency
  - Provide `EncryptedSharedPreferences` instance

#### Tests
- `CryptoManagerTest` — encrypt/decrypt round-trip, verify different IV per message, verify auth tag validation (tampered ciphertext → exception)
- `KeyExchangeHandlerTest` — mock API, verify key upload, verify shared secret derivation, verify re-key on missing local key
- `ChatViewModelTest` — verify messages are encrypted before send, decrypted on receive, key exchange failure shows waiting state

---

## Shared Infrastructure Decisions

### Phone Number Formatting (Lane 8)
Kenyan M-Pesa phone numbers must be in `254XXXXXXXXX` format (12 digits). The Android client must convert common user input formats:
- `07XX...` → `2547XX...`
- `+254XX...` → `254XX...`
- `01XX...` → `2541XX...`

Implement this in `InitiateStkPushUseCase` and unit test all format variations.

### Pro Status Caching
After fetching subscription status from the backend, cache `isProUser` in `UserPreferences` DataStore. Use this cached value to:
- Show/hide Pro badges immediately without network calls
- Enforce upload limits on the client side (defense-in-depth; backend also enforces)
- Refresh on app launch and after successful payment

### E2EE Key Lifecycle
- Keys are generated per-conversation, not per-user
- If a user reinstalls the app, local keys are lost. The `KeyExchangeHandler` detects this and initiates a re-key exchange
- Old messages encrypted with the previous key cannot be decrypted after reinstall — this is a known trade-off of true E2EE. Display "[Message encrypted with a previous key]" for unrecoverable messages

### Reporting UX
The report bottom sheet should be accessible from **3 touchpoints**:
1. Service detail screen → overflow menu → "Report Service"
2. User profile screen → overflow menu → "Report User"
3. Chat screen → overflow menu → "Report Conversation"

Each touchpoint passes the appropriate `reportedUserId` and/or `reportedServiceId` to the `ReportViewModel`.

---

## Package Structure (Phase 2 Additions)

```
must.kdroiders.hustlehub/
├── core/
│   ├── api/
│   │   └── ApiClient.kt              ← Modified Lane 10 (cert pinning)
│   ├── security/                      ← NEW Lane 10
│   │   ├── CryptoManager.kt
│   │   └── KeyExchangeHandler.kt
│   └── ui/
│       └── HustleCard.kt             ← Modified Lane 8 (Pro badge)
├── di/
│   ├── MonetizationModule.kt          ← NEW Lane 8
│   ├── ReportingModule.kt            ← NEW Lane 9
│   └── SecurityModule.kt             ← NEW Lane 10
├── ui/features/
│   ├── monetization/                  ← NEW Lane 8
│   │   ├── data/
│   │   │   ├── remote/
│   │   │   │   └── PaymentApiService.kt
│   │   │   ├── model/
│   │   │   │   ├── StkPushRequest.kt
│   │   │   │   ├── StkPushResponse.kt
│   │   │   │   ├── PaymentStatusResponse.kt
│   │   │   │   └── SubscriptionResponse.kt
│   │   │   └── PaymentRepositoryImpl.kt
│   │   ├── domain/
│   │   │   ├── PaymentRepository.kt
│   │   │   └── usecase/
│   │   │       ├── InitiateStkPushUseCase.kt
│   │   │       ├── PollPaymentStatusUseCase.kt
│   │   │       └── GetSubscriptionUseCase.kt
│   │   └── presentation/
│   │       ├── MonetizationViewModel.kt
│   │       ├── SubscriptionScreen.kt
│   │       └── PaymentStatusScreen.kt
│   ├── reporting/                     ← NEW Lane 9
│   │   ├── data/
│   │   │   ├── remote/
│   │   │   │   └── ReportApiService.kt
│   │   │   ├── model/
│   │   │   │   └── SubmitReportRequest.kt
│   │   │   └── ReportRepositoryImpl.kt
│   │   ├── domain/
│   │   │   ├── ReportRepository.kt
│   │   │   └── usecase/
│   │   │       └── SubmitReportUseCase.kt
│   │   └── presentation/
│   │       ├── ReportViewModel.kt
│   │       └── ReportBottomSheet.kt
│   ├── chat/                          ← Modified Lane 10
│   │   └── data/remote/
│   │       └── KeyExchangeApiService.kt  ← NEW
│   ├── auth/                          ← Modified Lane 9
│   │   └── presentation/
│   │       └── SuspendedScreen.kt        ← NEW
│   └── ...existing features...
└── local/
    └── entity/
        └── CachedMessage.kt           ← Modified Lane 10 (encryption fields)
```

---

## Branching Strategy

```
main          ← Production. Protected. Only receives merges from dev after full QA.
  └── dev     ← Integration branch. Receives merges from working after review.
        └── working  ← Active development. Lanes merge here when stable.
              └── lane/monetization-ui     (Lane 8)  ← NEW
              └── lane/reporting-ui        (Lane 9)  ← NEW
              └── lane/security-e2ee       (Lane 10) ← NEW
```

**Merge flow** (same as backend):
1. Developer works on `lane/<name>`
2. Opens PR: `lane/<name>` → `working`
3. One other team member reviews
4. After review + CI green → merge to `working`
5. Sprint milestone complete → `working` → `dev` (full integration test)
6. `dev` is stable + tested → `dev` → `main`

---

## Commit Convention (Conventional Commits)

```
feat(monetization): add SubscriptionScreen with Pro tier comparison
feat(monetization): implement M-Pesa STK push polling flow
feat(reporting): add ReportBottomSheet with reason picker
feat(security): implement AES-256-GCM CryptoManager
feat(chat): integrate E2EE encryption into WebSocket handler
fix(monetization): handle phone number format edge cases
test(security): add CryptoManager round-trip encryption tests
chore(deps): add jetpack-security-crypto dependency
```

Format: `<type>(<scope>): <short description>`
Types: `feat`, `fix`, `chore`, `test`, `docs`, `refactor`

---

*Last updated: 2026-07-23 · HustleHub Android Team · Android Community MUST*
*Phase 2 lanes (Monetization, Reporting, Security/E2EE) defined.*
