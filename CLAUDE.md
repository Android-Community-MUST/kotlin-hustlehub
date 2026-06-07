# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

---

## Project

**HustleHub** — native Android campus peer-to-peer service marketplace for Meru University of Science & Technology (MUST). Only `@must.ac.ke` emails are permitted.

---

## Build & Development Commands

```bash
# Build and install debug APK on connected device/emulator
./gradlew installDebug

# Assemble without installing
./gradlew assembleDebug

# Unit tests
./gradlew test

# Single test class
./gradlew test --tests "must.kdroiders.hustlehub.ui.auth.SignUpViewModelTest"

# Instrumented tests (requires running emulator + backend on localhost:8080)
./gradlew connectedAndroidTest

# Lint — ktlint (style) + detekt (static analysis)
./gradlew ktlintCheck detekt

# Auto-fix ktlint formatting
./gradlew ktlintFormat
```

**Required before first build:**
```bash
cp keys.properties.template keys.properties
# Edit keys.properties with real values:
# MAPS_API_KEY, GEMINI_API_KEY, GOOGLE_WEB_CLIENT_ID
# Place google-services.json in app/
```

The Spring Boot backend must be running locally (`./gradlew bootRun` in the backend repo) for the app to function. The emulator reaches it at `http://10.0.2.2:8080/api/v1/`.

---

## Architecture

### Layers

**Clean Architecture + MVVM**, feature-sliced:

```
Composable Screen  →  ViewModel  →  UseCase  →  Repository interface
                                                       ↓
                                            RepositoryImpl
                                            ├── Retrofit (remote)
                                            └── Room DAO (cache)
```

- **Screens** are `@Composable` functions — zero business logic.
- **ViewModels** expose `StateFlow<UiState<T>>`; one-time events via `Channel<UiEvent>`.
- **Use cases** are single-`invoke` classes returning `Flow<Result<T>>` or `suspend fun: Result<T>`.
- **Repositories** coordinate remote + local; Room is the single source of truth.
- No `LiveData` anywhere — `StateFlow`/`Flow` only.

### Standard patterns

```kotlin
sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String, val cause: Throwable? = null) : Result<Nothing>()
}
```

Every screen must handle all three `UiState` branches.

### Package layout (current)

```
must.kdroiders.hustlehub/
├── activities/          # MainActivity — hosts HustleHubNav
├── appHilt/             # HustleHubApp (@HiltAndroidApp)
├── data/                # Shared models + repository interfaces/impls
│   ├── model/           # User, Service data classes
│   └── repository/      # AuthRepository, UserRepository, StorageRepository
├── datastore/           # UserPreferences (DataStore)
├── di/                  # Hilt modules (AppModule, SupabaseModule)
├── navigation/          # Navigation 3 — NavKeys + NavGraph + MainScaffold
├── onboarding/          # OnboardingScreen + OnboardingViewModel
├── sharedComposables/   # Design-system components (HustleButton, HustleCard, …)
├── splash/              # SplashScreen + SplashViewModel (auth gate)
├── ui/
│   ├── auth/            # Login, SignUp, EmailVerification screens + ViewModels
│   └── features/        # chat, home, map, profile, profilesetup
└── util/                # ImageUtils
```

> The target architecture in `.agents/rules.md` places all features under `feature/<name>/` with full `data/domain/presentation` sub-splits. Currently only `auth` and `profile` have partial domain layers; other features will migrate to this structure as they are built out.

---

## Navigation

Uses **Navigation 3** (`androidx.navigation3`) exclusively — no legacy `NavController`.

- All routes are `@Serializable` `NavKey` objects defined in `navigation/HustleNavKeys.kt` — add every new destination there.
- Root backstack starts at `Splash`; `SplashViewModel` gates auth and routes to `Onboarding`, `Login`, `ProfileSetup`, or `MainShell`.
- `MainShell` hosts a nested `NavDisplay` for the four bottom-tab destinations (`BottomHome`, `BottomMap`, `BottomChat`, `BottomProfile`).
- Never pass complex objects as NavKey arguments — pass IDs only, load data in the destination ViewModel.

---

## Dependency Injection (Hilt)

- Every ViewModel: `@HiltViewModel` + `@Inject constructor(…)`.
- Firebase dependencies (`FirebaseAuth`, `FirebaseFirestore`, `FirebaseStorage`) are provided as **nullable**; the app has `Noop*` fallbacks so it runs without Firebase (useful in CI / tests).
- Inject at ViewModel level only — never directly into Composables.

---

## Authentication

- Firebase Auth (email/password + Google Sign-In) for identity only.
- After sign-in, Firebase ID token is attached to every API request via `AuthInterceptor` (`Authorization: Bearer <token>`).
- **All application data** goes through the Spring Boot backend REST API (`/api/v1/`) — Firebase Firestore and Realtime Database are **not** used for app data.
- Supabase is used only for file/media storage.

---

## UI & Design

- **Material 3 Expressive** (`material3 = "1.5.0-alpha15"`) — use `@OptIn(ExperimentalMaterial3ExpressiveApi::class)` where needed.
- All colors via `MaterialTheme.colorScheme`, typography via `MaterialTheme.typography`, shapes via `MaterialTheme.shapes` — no hardcoded hex values in Composables.
- Shared reusable composables live in `sharedComposables/`.
- Support both light and dark mode.
- Composables should stay under ~150 lines; split if larger.

---

## Code Quality

- **ktlint** enforces style; **detekt** (`detekt.yml`) enforces structure (LongMethod ≤ 60 lines, LargeClass ≤ 600 lines). Both are configured with `ignoreFailures = true` so they report but don't fail the build during development.
- Use `Timber` for all logging — never `Log.*` directly. Log at `DEBUG` only for network calls; never log in release.
- All user-facing strings must be in `strings.xml`.
- Named constants only — no magic strings or numbers. Use `companion object` or top-level `object`.

---

## Git Conventions

- **Branches**: `main` → `develop` → `feature/<name>`  
- **All PRs target `develop`**, never `main` directly.
- **Conventional Commits**: `feat(auth):`, `fix(chat):`, `refactor(core):`, `test(discovery):`, `docs:`, `chore:`
- **PR naming**: `[FEATURE] Add AI search screen` / `[FIX] Chat scroll position bug`
- Run `./gradlew ktlintCheck detekt test` before opening a PR.