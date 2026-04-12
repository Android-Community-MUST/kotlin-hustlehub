# 📝 Changelog

All notable changes to HustleHub will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [Unreleased]

### Changed
- **Backend migration**: Removed all Supabase Storage and Firebase Firestore/Realtime Database dependencies. All data operations now go through the HustleHub Spring Boot (Kotlin) REST API (`/api/v1/`). Firebase is retained only for Authentication (ID tokens) and Cloud Messaging (FCM).
- **Networking**: Replaced direct Firebase SDK data calls with Retrofit 3 + OkHttp 5 HTTP client. Added `AuthInterceptor` (attaches Firebase ID token) and `TokenAuthenticator` (auto-refreshes on 401).
- **Real-time chat**: Replaced Firebase Realtime Database with OkHttp WebSocket client connecting to the Spring Boot WebSocket/STOMP endpoint.
- **File uploads**: Replaced Supabase Storage SDK with a multipart `POST /api/v1/media/upload` endpoint via Retrofit.
- **Architecture**: Migrated `SupabaseModule` and direct Firestore repository calls to `NetworkModule` + feature-level Retrofit `ApiService` interfaces.
- Updated `docs/dev/API.md` to reflect Spring Boot REST + WebSocket endpoint reference.
- Updated `docs/dev/ARCHITECTURE.md` to reflect the new network + offline-first data layer.
- Updated `docs/dev/SETUP.md` to include Spring Boot backend setup instructions.
- Updated `README.md` tech stack section.
- Updated `keys.properties.template`: replaced Supabase keys with `BASE_URL` and `WS_BASE_URL`.

### Removed
- `io.github.jan-tennert.supabase:bom` and all Supabase SDK dependencies from `build.gradle.kts`
- `SupabaseModule.kt` (DI module for Supabase client)
- `StorageRepository.kt` (Supabase-based portfolio upload — replaced by `MediaUploadRepository.kt`)
- Direct Firestore / Firebase Realtime Database SDK usage from all repositories
- `SUPABASE_URL` and `SUPABASE_KEY` from `keys.properties.template`
- `ktor-client-android` and `ktor-client-okhttp` dependencies (were only needed by Supabase SDK)

### Added
- `core/api/ApiClient.kt` — Retrofit singleton with OkHttp, interceptors, and build-variant base URL
- `core/api/AuthInterceptor.kt` — injects Firebase ID token into every request header
- `core/api/TokenAuthenticator.kt` — auto-refreshes Firebase token on `401 Unauthorized`
- `core/api/ApiResponse.kt` — sealed wrapper: `Success<T>` / `HttpError` / `NetworkError`
- `di/NetworkModule.kt` — provides Retrofit, OkHttp, and all `ApiService` instances
- `di/FirebaseModule.kt` — provides `FirebaseAuth` and `FirebaseMessaging`
- Feature-level `ApiService` interfaces: `AuthApiService`, `UserApiService`, `ServiceApiService`, `DiscoveryApiService`, `ConversationApiService`, `ReviewApiService`, `MediaApiService`
- `feature/chat/data/remote/ChatWebSocketService.kt` — OkHttp WebSocket wrapper for real-time messaging
- `BASE_URL` and `WS_BASE_URL` added to `keys.properties.template`

---

## [0.2.0] — Sprint 1 Complete

### Added
- Initial project setup with `must.kdroiders.hustlehub` package
- MVVM architecture with Hilt DI (`AppModule`)
- Firebase Auth integration (Email/Password + Google Sign-In)
- Navigation 3 infrastructure with serializable NavKeys
- Bottom navigation bar (Home, Map, Chat, Profile tabs)
- Splash screen with auth-gate routing and animated entrance
- Onboarding carousel (3 slides, pager, skip/back)
- Sign-up screen with `@must.ac.ke` email validation + password strength indicator
- Profile setup wizard (photo, course, year, hostel, role selection)
- Profile screen with avatar, badges, stats row, service cards, toggle
- Portfolio upload screen with Supabase Storage (⚠️ **replaced in [Unreleased]** — see above)
- Home, Map, Chat placeholder screens
- DataStore Preferences for first-launch and user settings persistence
- Theme system: dark mode, custom colors (HustleHub dark palette), shapes, typography
- Shared composable library: `HustleButton`, `HustleCard`, `HustleTextField`, `EmptyStateView`, `ErrorView`, `LoadingIndicator`, `RatingBar`
- Code quality tooling: ktlint + detekt with project-specific rules
- Unit tests: `SignUpViewModelTest`, `StorageRepositoryTest`, `ImageUtilsTest`

---

## [0.1.0] — 2026-02-14

### Added
- Project initialization
- Basic package structure
- Firebase configuration (`google-services.json`)
- Gradle dependency setup (BOM versions, version catalog in `libs.versions.toml`)

---

## Version Guidelines

### Version Format: MAJOR.MINOR.PATCH

- **MAJOR**: Breaking changes or complete feature milestone
- **MINOR**: New features (backward compatible)
- **PATCH**: Bug fixes (backward compatible)

### Change Categories

- **Added**: New features or files
- **Changed**: Changes in existing functionality or docs
- **Deprecated**: Soon-to-be removed features
- **Removed**: Removed features or dependencies
- **Fixed**: Bug fixes
- **Security**: Security improvements

---

**Note**: Update this file in every PR. Use the correct category and reference issue numbers where applicable.
