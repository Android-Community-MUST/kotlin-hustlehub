# 🏗️ Architecture Overview

HustleHub follows **MVVM + Clean Architecture** with a feature-based package structure.

> **Backend**: All data is served by a **Spring Boot (Kotlin) REST API** and a **WebSocket server**.
> Firebase is used **only** for Authentication (ID tokens) and Cloud Messaging (FCM push notifications).
> There is **no Firestore, no Firebase Realtime Database, and no Supabase** in this project.

---

## Architecture Layers

```
┌─────────────────────────────────────┐
│       Presentation Layer            │  ← UI (Compose) + ViewModels
│   (Jetpack Compose + ViewModels)   │
└─────────────┬───────────────────────┘
              │ observes StateFlow / UiState
┌─────────────▼───────────────────────┐
│         Domain Layer                │  ← Use Cases, Repository Interfaces
│   (Pure Kotlin — no Android deps)  │
└─────────────┬───────────────────────┘
              │ implements
┌─────────────▼───────────────────────┐
│          Data Layer                 │  ← Repositories, Data Sources
│  ┌─────────────┐  ┌──────────────┐ │
│  │ Room (cache)│  │ Retrofit API │ │
│  │   + DAOs    │  │ WebSocket    │ │
│  └─────────────┘  └──────────────┘ │
└─────────────────────────────────────┘
```

---

## Package Structure

```
must.kdroiders.hustlehub/
│
├── 📁 activities/
│   └── MainActivity.kt                    # @AndroidEntryPoint, single activity
│
├── 📁 appHilt/
│   └── HustleHubApp.kt                    # @HiltAndroidApp, Firebase init, FCM channels
│
├── 📁 core/                               # Shared across all features
│   ├── 📁 api/
│   │   ├── ApiClient.kt                   # Retrofit + OkHttp singleton
│   │   ├── AuthInterceptor.kt             # Attaches Firebase ID token to every request
│   │   ├── TokenAuthenticator.kt          # Auto-refreshes token on 401
│   │   └── ApiResponse.kt                 # Sealed: Success / HttpError / NetworkError
│   ├── 📁 ui/                             # Shared composables (design system)
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
├── 📁 di/
│   ├── AppModule.kt                       # DataStore, UserPreferences
│   ├── NetworkModule.kt                   # Retrofit, OkHttp, interceptors
│   ├── FirebaseModule.kt                  # FirebaseAuth, FirebaseMessaging
│   └── RepositoryModule.kt               # Binds interfaces to implementations
│
├── 📁 navigation/
│   ├── HustleNavKeys.kt                   # All NavKey data objects/classes
│   ├── HustleHubNavGraph.kt               # Root NavDisplay
│   ├── BottomNavigationBar.kt
│   └── MainScaffold.kt
│
├── 📁 datastore/
│   └── UserPreferences.kt                 # DataStore: first-launch, uid, role
│
├── 📁 local/                              # Room offline cache
│   ├── HustleDatabase.kt
│   ├── 📁 dao/
│   │   ├── ServiceDao.kt
│   │   ├── MessageDao.kt
│   │   └── ConversationDao.kt
│   └── 📁 entity/
│       ├── CachedService.kt
│       ├── CachedMessage.kt
│       └── CachedConversation.kt
│
└── 📁 feature/
    ├── 📁 auth/
    │   ├── 📁 data/
    │   │   ├── remote/AuthApiService.kt   # Retrofit: POST /api/v1/auth/register
    │   │   └── AuthRepositoryImpl.kt
    │   ├── 📁 domain/
    │   │   ├── AuthRepository.kt          # Interface
    │   │   └── usecase/
    │   │       ├── SignUpUseCase.kt
    │   │       ├── LoginUseCase.kt
    │   │       └── VerifyEmailUseCase.kt
    │   └── 📁 presentation/
    │       ├── SignUpScreen.kt
    │       ├── LoginScreen.kt
    │       └── AuthViewModel.kt
    │
    ├── 📁 splash/ & 📁 onboarding/        # (already implemented)
    │
    ├── 📁 profile/
    │   ├── 📁 data/
    │   │   ├── remote/UserApiService.kt   # GET/PUT /api/v1/users/me
    │   │   └── ProfileRepositoryImpl.kt
    │   ├── 📁 domain/
    │   └── 📁 presentation/
    │       ├── ProfileScreen.kt
    │       ├── ProfileSetupScreen.kt
    │       └── ProfileViewModel.kt
    │
    ├── 📁 services/
    │   ├── 📁 data/
    │   │   ├── remote/ServiceApiService.kt  # CRUD /api/v1/services
    │   │   └── ServiceRepositoryImpl.kt
    │   ├── 📁 domain/
    │   └── 📁 presentation/
    │       ├── ServiceListScreen.kt
    │       ├── ServiceDetailScreen.kt
    │       ├── CreateServiceScreen.kt
    │       └── ServiceViewModel.kt
    │
    ├── 📁 discovery/
    │   ├── 📁 data/
    │   │   ├── remote/DiscoveryApiService.kt  # /api/v1/discovery/*
    │   │   └── DiscoveryRepositoryImpl.kt
    │   ├── 📁 domain/
    │   └── 📁 presentation/
    │       ├── HomeScreen.kt
    │       ├── SearchScreen.kt
    │       ├── AiSearchScreen.kt
    │       └── DiscoveryViewModel.kt
    │
    ├── 📁 chat/
    │   ├── 📁 data/
    │   │   ├── remote/
    │   │   │   ├── ConversationApiService.kt  # REST: history, list
    │   │   │   └── ChatWebSocketService.kt    # OkHttp WebSocket / STOMP
    │   │   └── ChatRepositoryImpl.kt
    │   ├── 📁 domain/
    │   └── 📁 presentation/
    │       ├── ConversationListScreen.kt
    │       ├── ChatScreen.kt
    │       └── ChatViewModel.kt
    │
    ├── 📁 map/
    │   ├── 📁 data/
    │   │   └── remote/MapApiService.kt     # GET /api/v1/discovery/map-pins
    │   └── 📁 presentation/
    │       ├── MapScreen.kt
    │       └── MapViewModel.kt
    │
    ├── 📁 reviews/
    │   ├── 📁 data/
    │   │   └── remote/ReviewApiService.kt  # POST /api/v1/reviews
    │   └── 📁 presentation/
    │       ├── WriteReviewScreen.kt
    │       └── ReviewViewModel.kt
    │
    ├── 📁 notifications/
    │   ├── HustleFcmService.kt             # extends FirebaseMessagingService
    │   └── 📁 presentation/
    │       ├── NotificationScreen.kt
    │       └── NotificationViewModel.kt
    │
    └── 📁 media/
        ├── remote/MediaApiService.kt       # POST /api/v1/media/upload
        └── MediaUploadRepository.kt
```

---

## Data Flow Example — Creating a Service

```
1.  User taps "Create Service" in CreateServiceScreen
        │
2.  Screen calls viewModel.createService(formData)
        │
3.  CreateServiceViewModel
        │  ─ sets uiState = Loading
        │  ─ calls createServiceUseCase(data)
        │
4.  CreateServiceUseCase
        │  ─ validates business rules (title required, price range valid, etc.)
        │  ─ calls serviceRepository.createService(service)
        │
5.  ServiceRepositoryImpl
        │  ─ calls MediaApiService to upload portfolio images first
        │  ─ calls ServiceApiService.createService(request)  →  POST /api/v1/services
        │  ─ on success: saves result to Room (CachedService)
        │  ─ returns Result.Success(service)
        │
6.  ViewModel receives Result.Success
        │  ─ sets uiState = Success(service)
        │  ─ emits UiEvent.NavigateToServiceDetail(service.id)
        │
7.  Screen observes UiEvent → navigates to ServiceDetailScreen
```

---

## Key Design Patterns

### UiState (every screen)

```kotlin
sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}
```

### UiEvent (one-time actions: navigation, snackbar)

```kotlin
sealed interface UiEvent {
    data class Navigate(val key: NavKey) : UiEvent
    data class ShowSnackbar(val message: String) : UiEvent
    data object NavigateBack : UiEvent
}
```

### Result (domain layer)

```kotlin
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String, val cause: Throwable? = null) : Result<Nothing>()
}
```

### ViewModel skeleton

```kotlin
@HiltViewModel
class ServiceDetailViewModel @Inject constructor(
    private val getServiceUseCase: GetServiceUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val serviceId = savedStateHandle.get<String>("serviceId")!!

    private val _uiState = MutableStateFlow<UiState<ServiceDetail>>(UiState.Loading)
    val uiState: StateFlow<UiState<ServiceDetail>> = _uiState.asStateFlow()

    private val _events = Channel<UiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init { loadService() }

    private fun loadService() {
        viewModelScope.launch {
            when (val result = getServiceUseCase(serviceId)) {
                is Result.Success -> _uiState.value = UiState.Success(result.data)
                is Result.Error   -> _uiState.value = UiState.Error(result.message)
            }
        }
    }
}
```

---

## Network Layer

### AuthInterceptor

```kotlin
@Singleton
class AuthInterceptor @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runBlocking {
            firebaseAuth.currentUser
                ?.getIdToken(false)
                ?.await()
                ?.token
        }
        val request = chain.request().newBuilder()
            .apply { token?.let { addHeader("Authorization", "Bearer $it") } }
            .build()
        return chain.proceed(request)
    }
}
```

### TokenAuthenticator (auto-refresh on 401)

```kotlin
@Singleton
class TokenAuthenticator @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        if (response.code != 401) return null
        val freshToken = runBlocking {
            firebaseAuth.currentUser
                ?.getIdToken(true)   // force refresh
                ?.await()
                ?.token
        } ?: return null // can't refresh → give up
        return response.request.newBuilder()
            .header("Authorization", "Bearer $freshToken")
            .build()
    }
}
```

---

## Real-Time Chat (WebSocket)

```kotlin
class ChatWebSocketService @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val tokenProvider: TokenProvider
) {
    private var webSocket: WebSocket? = null

    fun connect(conversationId: String, onMessage: (ChatMessage) -> Unit) {
        val token = tokenProvider.getCachedToken()
        val request = Request.Builder()
            .url("${BuildConfig.WS_BASE_URL}?token=$token")
            .build()

        webSocket = okHttpClient.newWebSocket(request, object : WebSocketListener() {
            override fun onMessage(webSocket: WebSocket, text: String) {
                val message = Json.decodeFromString<ChatMessage>(text)
                onMessage(message)
            }
        })
    }

    fun sendMessage(payload: SendMessagePayload) {
        webSocket?.send(Json.encodeToString(payload))
    }

    fun disconnect() {
        webSocket?.close(1000, "User left chat")
        webSocket = null
    }
}
```

---

## Offline-First with Room

- Room is the **single source of truth** for UI.
- Services cached for 10 minutes; messages cached indefinitely.
- Data flow: UI observes Room DAO → Repository fetches from API → updates Room → UI auto-updates.

```kotlin
// Offline-first repository pattern
override fun getServices(category: String): Flow<List<Service>> = flow {
    // 1. Emit cached data immediately
    val cached = serviceDao.getByCategory(category)
    if (cached.isNotEmpty()) emit(cached.map { it.toDomain() })

    // 2. Fetch fresh data from backend
    val response = serviceApiService.getServices(category = category)
    if (response.isSuccessful) {
        val fresh = response.body()!!.content
        serviceDao.upsertAll(fresh.map { it.toEntity() })
        emit(fresh.map { it.toDomain() })
    }
}
```

---

## Dependency Injection

### NetworkModule

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
        tokenAuthenticator: TokenAuthenticator
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .authenticator(tokenAuthenticator)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .apply {
            if (BuildConfig.DEBUG) addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
        }
        .build()

    @Provides @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    @Provides @Singleton
    fun provideServiceApiService(retrofit: Retrofit): ServiceApiService =
        retrofit.create(ServiceApiService::class.java)
    
    // ... repeat for each ApiService
}
```

### FirebaseModule

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    @Provides @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides @Singleton
    fun provideFirebaseMessaging(): FirebaseMessaging = FirebaseMessaging.getInstance()
}
```

---

## Navigation (Navigation 3)

```kotlin
// HustleNavKeys.kt — central route registry
@Serializable data object Splash          : NavKey
@Serializable data object Onboarding      : NavKey
@Serializable data object Login           : NavKey
@Serializable data object SignUp          : NavKey
@Serializable data object ProfileSetup    : NavKey
@Serializable data object MainShell       : NavKey
@Serializable data object HomeScreen      : NavKey
@Serializable data object MapScreen       : NavKey
@Serializable data object ChatListScreen  : NavKey
@Serializable data object ProfileScreen   : NavKey

@Serializable data class ServiceDetailScreen(val serviceId: String)  : NavKey
@Serializable data class ChatDetailScreen(val conversationId: String) : NavKey
@Serializable data class ProviderProfileScreen(val userId: String)    : NavKey
```

---

## Testing Strategy

| Layer | Framework | Approach |
|-------|-----------|----------|
| Use Cases | JUnit 5 + MockK | Fake repositories |
| ViewModels | JUnit 5 + Turbine | TestCoroutineDispatcher |
| Repositories | JUnit 5 + MockWebServer | Mock Retrofit responses |
| Compose UI | Compose Testing | `createComposeRule()` |
| End-to-End | Maestro | YAML flow scripts |

---

**See also:**
- [API Reference](API.md)
- [Setup Guide](SETUP.md)
- [Troubleshooting](TROUBLESHOOTING.md)
