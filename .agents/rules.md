# HustleHub Android — Project Rules & AI Instructions

> These rules govern how the AI assistant (Antigravity/MCP) builds and maintains the HustleHub Android frontend.
> All decisions, code patterns, and structures must align with these standards before any code is written.

---

## 🎯 Project Identity

- **App**: HustleHub — Campus peer-to-peer service marketplace for Meru University of Science & Technology
- **Package**: `must.kdroiders.hustlehub`
- **Platform**: Android (native, Kotlin-only — no Flutter, no XML layouts)
- **Min SDK**: 26 | **Target/Compile SDK**: 36
- **Domain restriction**: Only `@must.ac.ke` emails allowed

---

## 🛠️ Tech Stack — Non-Negotiable

| Area | Technology | Version |
|------|-----------|---------|
| **Language** | Kotlin | 2.x (latest stable) |
| **UI** | Jetpack Compose + Material 3 Expressive | Latest |
| **Architecture** | MVVM + Clean Architecture (feature-based) | — |
| **Navigation** | Navigation 3 (`androidx.navigation3`) | Latest |
| **DI** | Hilt (Dagger) | Latest |
| **Networking** | Retrofit 3.0.0 + OkHttp 5.x | As specified |
| **Local DB** | Room 2.8.4 | As specified |
| **Preferences** | DataStore Preferences | Latest |
| **Image Loading** | Coil 2.7.0 | As specified |
| **State** | Kotlin Flow + StateFlow | — |
| **Auth** | Firebase Auth (Email + Google Sign-In) | Latest |
| **Backend API** | Retrofit → Spring Boot backend | `/api/v1/` |
| **Real-time** | WebSocket client (OkHttp) → Spring Boot | — |
| **Notifications** | Firebase Cloud Messaging (FCM) | Latest |
| **Maps** | Google Maps SDK for Android | Latest |
| **AI Search** | Via backend `/api/v1/discovery/ai-search` | — |
| **Build** | Gradle with Kotlin DSL (`.kts`) | — |

> ⚠️ **No Supabase, no Firestore, no Realtime Database** — the Spring Boot backend replaces all of them.
> ⚠️ **No LiveData** — use `StateFlow` / `Flow` exclusively.
> ⚠️ **No XML layouts** — Jetpack Compose only.

---

## 🗂️ Architecture — Feature-Based MVVM + Clean Architecture

### Core Principle
Each feature is a fully self-contained vertical slice: its own screens, ViewModel, use cases, repository, data sources, and models. Features do **not** import from each other directly.

### Package Structure

```
must.kdroiders.hustlehub/
│
├── 📁 activities/                   # Single entry point
│   └── MainActivity.kt              # Hosts NavDisplay
│
├── 📁 appHilt/
│   └── HustleHubApp.kt              # @HiltAndroidApp
│
├── 📁 core/                         # Shared across all features
│   ├── 📁 api/                      # Retrofit instance, interceptors, auth header
│   │   ├── ApiClient.kt
│   │   ├── AuthInterceptor.kt       # Attaches Firebase token to every request
│   │   └── ApiResponse.kt           # Sealed wrapper for API results
│   ├── 📁 ui/                       # Shared composables (design system)
│   │   ├── HustleButton.kt
│   │   ├── HustleCard.kt
│   │   ├── HustleTextField.kt
│   │   ├── LoadingIndicator.kt
│   │   ├── EmptyStateView.kt
│   │   ├── ErrorView.kt
│   │   └── RatingBar.kt
│   ├── 📁 theme/
│   │   ├── Color.kt
│   │   ├── Type.kt
│   │   ├── Shape.kt
│   │   └── Theme.kt
│   └── 📁 util/
│       ├── Extensions.kt
│       └── ImageUtils.kt
│
├── 📁 di/                           # Hilt DI modules
│   ├── AppModule.kt
│   ├── NetworkModule.kt             # Retrofit, OkHttp
│   ├── FirebaseModule.kt            # Firebase Auth, FCM
│   └── RepositoryModule.kt
│
├── 📁 navigation/                   # Navigation 3
│   ├── HustleNavKeys.kt             # All NavKey sealed/data objects
│   ├── HustleHubNavGraph.kt         # Root NavDisplay
│   ├── BottomNavigationBar.kt
│   └── MainScaffold.kt
│
├── 📁 datastore/
│   └── UserPreferences.kt           # DataStore: auth state, user prefs
│
├── 📁 feature/                      # All features live here
│   ├── 📁 auth/
│   │   ├── 📁 data/
│   │   │   ├── remote/AuthApiService.kt
│   │   │   └── AuthRepositoryImpl.kt
│   │   ├── 📁 domain/
│   │   │   ├── AuthRepository.kt    # Interface
│   │   │   └── usecase/
│   │   │       ├── SignUpUseCase.kt
│   │   │       ├── LoginUseCase.kt
│   │   │       └── VerifyOtpUseCase.kt
│   │   └── 📁 presentation/
│   │       ├── SignUpScreen.kt
│   │       ├── LoginScreen.kt
│   │       ├── OtpVerificationScreen.kt
│   │       └── AuthViewModel.kt
│   │
│   ├── 📁 onboarding/
│   │   ├── OnboardingScreen.kt
│   │   └── OnboardingViewModel.kt
│   │
│   ├── 📁 splash/
│   │   ├── SplashScreen.kt
│   │   └── SplashViewModel.kt
│   │
│   ├── 📁 profile/
│   │   ├── 📁 data/
│   │   ├── 📁 domain/
│   │   └── 📁 presentation/
│   │       ├── ProfileScreen.kt
│   │       ├── ProfileSetupScreen.kt
│   │       └── ProfileViewModel.kt
│   │
│   ├── 📁 services/                 # Service listings (create, edit, view)
│   │   ├── 📁 data/
│   │   ├── 📁 domain/
│   │   └── 📁 presentation/
│   │       ├── ServiceListScreen.kt
│   │       ├── ServiceDetailScreen.kt
│   │       ├── CreateServiceScreen.kt
│   │       └── ServiceViewModel.kt
│   │
│   ├── 📁 discovery/                # Browse, search, AI search
│   │   ├── 📁 data/
│   │   ├── 📁 domain/
│   │   └── 📁 presentation/
│   │       ├── HomeScreen.kt
│   │       ├── SearchScreen.kt
│   │       ├── AiSearchScreen.kt
│   │       └── DiscoveryViewModel.kt
│   │
│   ├── 📁 chat/                     # Real-time messaging (WebSocket)
│   │   ├── 📁 data/
│   │   │   ├── remote/ChatWebSocketService.kt
│   │   │   └── ChatRepositoryImpl.kt
│   │   ├── 📁 domain/
│   │   └── 📁 presentation/
│   │       ├── ConversationListScreen.kt
│   │       ├── ChatScreen.kt
│   │       └── ChatViewModel.kt
│   │
│   ├── 📁 map/
│   │   ├── 📁 data/
│   │   ├── 📁 domain/
│   │   └── 📁 presentation/
│   │       ├── MapScreen.kt
│   │       └── MapViewModel.kt
│   │
│   ├── 📁 reviews/
│   │   ├── 📁 data/
│   │   ├── 📁 domain/
│   │   └── 📁 presentation/
│   │       ├── WriteReviewScreen.kt
│   │       └── ReviewViewModel.kt
│   │
│   ├── 📁 notifications/
│   │   ├── FcmService.kt            # Extends FirebaseMessagingService
│   │   └── 📁 presentation/
│   │       ├── NotificationScreen.kt
│   │       └── NotificationViewModel.kt
│   │
│   └── 📁 media/                    # Upload images, voice notes to backend
│       ├── 📁 data/
│       └── MediaUploadService.kt
│
└── 📁 local/                        # Room database (offline cache)
    ├── HustleDatabase.kt
    ├── 📁 dao/
    │   ├── ServiceDao.kt
    │   ├── MessageDao.kt
    │   └── ConversationDao.kt
    └── 📁 entity/
        ├── CachedService.kt
        ├── CachedMessage.kt
        └── CachedConversation.kt
```

---

## 📐 MVVM Layer Rules

### ✅ Presentation Layer (Screen + ViewModel)
- Screens are `@Composable` functions — pure UI, zero business logic.
- Every screen has exactly **one ViewModel** injected via `hiltViewModel()`.
- State is exposed as `StateFlow<UiState<T>>`, never `LiveData`.
- User actions are sent to the ViewModel as one-off functions (`onSubmit()`, `onQueryChanged()`).
- One-time events (navigation, toast) use `Channel<UiEvent>` consumed via `LaunchedEffect`.

```kotlin
// Standard UiState pattern — use this everywhere
sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}
```

### ✅ Domain Layer (Use Cases + Repository Interfaces)
- Every business action = one use case class with a single `operator fun invoke()`.
- Use cases talk to repository **interfaces** only, never implementations.
- Return type: `Flow<Result<T>>` or `suspend fun` returning `Result<T>`.
- Use cases live in `feature/<name>/domain/usecase/`.

```kotlin
// Standard Result pattern
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String, val cause: Throwable? = null) : Result<Nothing>()
}
```

### ✅ Data Layer (Repositories + Data Sources)
- Repository implementations coordinate between **remote API** and **Room local cache**.
- Offline-first: always emit cached data first, then fetch from remote and update cache.
- Remote data sources use Retrofit API services.
- Local data sources use Room DAOs.
- Repository implementations live in `feature/<name>/data/`.

---

## 🔐 Authentication Rules

- Firebase Auth handles sign-in (email/password + Google Sign-In).
- Only `@must.ac.ke` emails are allowed — validate at the UI before calling backend.
- After sign-in, retrieve the **Firebase ID Token** and attach it to every API call via `AuthInterceptor`.
- `AuthInterceptor` calls `FirebaseAuth.currentUser?.getIdToken(false)` and adds `Authorization: Bearer <token>`.
- Refresh token automatically when `401 Unauthorized` is returned by backend (OkHttp Authenticator).
- `SplashViewModel` checks auth state on launch → routes to Onboarding/Login or Home.
- Store lightweight user prefs (uid, name, role) in DataStore, not in memory.

---

## 🌐 Networking Rules — Retrofit + Spring Boot Backend

- **Base URL**: Configured via `BuildConfig.BASE_URL` (different per build variant).
- All API calls go through the Spring Boot backend (no direct Firebase/Supabase calls for data).
- Use `sealed class ApiResponse<T>` to wrap Retrofit responses (Success / HttpError / NetworkError).
- Handle errors with a global `ResponseCallAdapterFactory` or `try/catch` in repositories.
- Never expose raw `Response<T>` or Retrofit exceptions to the ViewModel.
- Timeouts: connect 15s, read 30s, write 30s.
- Log requests in `DEBUG` only — never in production.

```kotlin
// Build flavors for API base URL
// debug: http://10.0.2.2:8080/api/v1/   (local emulator → localhost)
// release: https://api.hustlehub.app/api/v1/
```

---

## 💬 Real-Time Chat — WebSocket

- Use **OkHttp WebSocket** client connected to the Spring Boot STOMP endpoint.
- `ChatWebSocketService` manages connection lifecycle within the chat feature.
- Connect on `ChatScreen` entry, disconnect on exit — use `DisposableEffect`.
- Send messages as JSON over `/app/chat.send`.
- Receive messages from `/topic/conversation/{conversationId}`.
- All received messages are persisted to Room (`MessageDao`) immediately.
- Show optimistic UI: add message to local state before server ack.

---

## 🗺️ Navigation Rules — Navigation 3

- **Only Navigation 3** (`androidx.navigation3`) — no legacy `NavController`.
- All routes are `NavKey` — `@Serializable data object` (no args) or `@Serializable data class` (with args).
- All NavKeys live in `HustleNavKeys.kt` — one central file.
- `backStack` managed with `rememberNavBackStack(initialKey)`.
- Never pass complex objects as NavKey arguments — pass IDs only, load data in the destination ViewModel.
- Bottom navigation tabs use a nested `NavDisplay` inside `MainScaffold`.

```kotlin
// NavKey examples — always follow this pattern
@Serializable data object HomeScreen : NavKey
@Serializable data object ChatListScreen : NavKey
@Serializable data class ChatDetailScreen(val conversationId: String) : NavKey
@Serializable data class ServiceDetailScreen(val serviceId: String) : NavKey
```

---

## 💉 Dependency Injection Rules — Hilt

- Every ViewModel uses `@HiltViewModel` + `@Inject constructor(...)`.
- Every module uses `@Module` + `@InstallIn(SingletonComponent::class)` for singletons.
- Inject at the ViewModel level, never directly into Composables.
- Use `@ApplicationContext` for Context injection — never use Activity context in DI.
- Provide `FirebaseAuth` as nullable (`FirebaseAuth?`) with fallback to allow running without Firebase in tests.

---

## 🎨 UI & Design Rules — Jetpack Compose + Material 3

- **Material 3 Expressive** design system — use `MaterialTheme` tokens everywhere.
- All colors come from `Color.kt` via `MaterialTheme.colorScheme` — no hardcoded hex values in composables.
- All typography comes from `Type.kt` via `MaterialTheme.typography`.
- All shapes come from `Shape.kt` via `MaterialTheme.shapes`.
- Shared reusable composables live in `core/ui/` (e.g., `HustleButton`, `HustleCard`).
- Feature-specific composables live inside their own `presentation/` folder.
- Every interactive element has a `Modifier.testTag("unique_tag")` for UI testing.
- Support dark mode — all colors defined with `darkColorScheme` / `lightColorScheme`.
- Accessibility: every image has `contentDescription`, every icon button has `contentDescription`.
- Use `LazyColumn` / `LazyRow` for lists — never `Column` with `forEach` for large datasets.
- Animations: use `AnimatedVisibility`, `animateContentSize`, `Crossfade` — no raw `alpha` hacks.

---

## 🖼️ Media & File Handling

- File uploads (images, voice) go to the **backend** (`/api/v1/media/upload`) — not direct to GCS.
- Use `ActivityResultContracts` for image picking and camera capture.
- Compress images before upload (max 1024px, 80% quality) using `ImageUtils.kt`.
- Display images with **Coil 2.7.0** using `AsyncImage` composable.
- Voice recording: use `MediaRecorder`, max 2 minutes, save to app cache before uploading.
- Always show upload progress to the user.

---

## 🏬 Offline-First & Room Cache Rules

- Room is the **single source of truth** for the UI. ViewModels observe DAOs, not network calls directly.
- Data flow: UI → ViewModel → UseCase → Repository → (Room DAO first, then Retrofit, update Room).
- Cache expiry: services cached for 10 minutes, messages kept indefinitely until explicitly deleted.
- Room migrations use explicit `Migration` objects — never `fallbackToDestructiveMigration()` in production.
- Use `@Entity` with `@PrimaryKey` as UUID string — matches backend IDs.

---

## 🧪 Testing Rules

- **Unit tests**: Every `UseCase` has a test. Use **JUnit 5** + **MockK** for mocking.
- **ViewModel tests**: Use `TestCoroutineDispatcher` + `Turbine` for StateFlow testing.
- **UI tests**: Every screen's critical flow has a Compose UI test using `createComposeRule()`.
- **Fake repositories**: Use `Fake*Repository` implementations — never mock repositories directly.
- Test naming: `` `should_doSomething_when_condition` `` backtick style.
- All tests run with `@OptIn(ExperimentalCoroutinesApi::class)` where needed.

---

## 📐 Code Quality Rules — KISS & DRY

- **KISS**: Every composable does one thing. Split if it exceeds ~150 lines.
- **DRY**: Shared UI goes to `core/ui/`. Shared logic goes to `core/util/`. Never copy-paste.
- **No business logic in Composables** — only UI rendering and event forwarding.
- **No business logic in ViewModels** — that belongs to Use Cases.
- **No Android context in Use Cases or Repositories** — use `@ApplicationContext` only where unavoidable.
- Use Kotlin idiomatic patterns: `let`, `run`, `apply`, `also`, `?.`, `?:` appropriately.
- Use `data class` for models and DTOs.
- Use `sealed class` / `sealed interface` for state and events.
- Named constants only: no magic strings or numbers in code. Use `companion object` constants or `object Constants`.
- `viewModelScope.launch` for async — never `GlobalScope`.
- Always handle loading, success, and error states in every screen.

---

## 📂 Resource Rules

- **Strings**: All user-facing strings in `strings.xml` — no hardcoded strings in Kotlin.
- **Drawables**: Vector drawables (`.xml`) for icons. Use Material Icons where possible.
- **Dimensions**: Define in `dimens.xml` for spacing tokens used across multiple screens.
- Feature-specific assets in `res/drawable/<feature>_*` naming convention.

---

## 🔔 Push Notifications — FCM

- `FcmService` extends `FirebaseMessagingService`, lives in `feature/notifications/`.
- On new token, send it to the backend via `POST /api/v1/users/fcm-token`.
- Notification taps must deep-link to the relevant screen via the Navigation 3 backstack.
- All notification channels registered in `HustleHubApp.onCreate()`.

---

## 📚 Documentation Rules

- Every public function/class has a KDoc comment.
- Complex logic has inline comments explaining **why**, not **what**.
- Every screen file starts with a header comment specifying its NavKey and purpose.
- Update `docs/dev/CHANGELOG.md` on every meaningful change.

---

## 🔀 Git & Branching Rules

- **Branch strategy**: `main` → `develop` → `feature/<feature-name>`
- **Commit convention**: Conventional Commits
  - `feat(auth):` — new feature in auth
  - `fix(chat):` — bug fix in chat
  - `refactor(core):` — restructure shared code
  - `test(discovery):` — adding tests
  - `docs:` — documentation only
  - `chore:` — dependency updates, gradle changes
- **No direct pushes to `main`** — all changes go through `develop` via PR.
- PR naming: `[FEATURE] Add AI search screen` / `[FIX] Chat scroll position bug`

---

## ⚙️ Build Variants

| Variant | API Base URL | Logging | Notes |
|---------|-------------|---------|-------|
| `debug` | `http://10.0.2.2:8080/api/v1/` | Verbose | Emulator → localhost backend |
| `release` | `https://api.hustlehub.app/api/v1/` | Off | Production backend |

- All API keys in `keys.properties` (gitignored) — never in source code.
- Use `BuildConfig` fields to access keys at runtime.

---

> **Remember**: This app serves real students with real money concerns. Build fast, build clean, build trustworthy.
> Every screen must handle **loading → success → error** gracefully. Never leave a user staring at a blank screen. 🚀
