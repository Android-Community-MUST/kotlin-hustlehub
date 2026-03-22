# 🏗️ Architecture Overview

Quick reference for understanding the HustleHub codebase structure.

## Architecture Pattern

HustleHub follows **MVVM** with **Clean Architecture** principles (domain layer planned).

> **Note:** The domain layer (use cases, repository interfaces) is planned but not yet
> implemented. Currently ViewModels call repositories directly.

```
┌─────────────────────────────────────┐
│       Presentation Layer            │  ← UI (Compose) + ViewModels
│  (Jetpack Compose + ViewModels)    │
└─────────────┬───────────────────────┘
              │ observes StateFlow
┌─────────────▼───────────────────────┐
│    Domain Layer  🚧 (planned)       │  ← Use Cases, Interfaces
└─────────────┬───────────────────────┘
              │ implements
┌─────────────▼───────────────────────┐
│          Data Layer                 │  ← Data Sources
│ (Repositories + Data Sources)       │
│  ┌──────────┐  ┌─────────────┐     │
│  │DataStore │  │   Firebase  │     │
│  │  + Room  │  │ + Supabase  │     │
│  └──────────┘  └─────────────┘     │
└─────────────────────────────────────┘
```

## Package Structure

> Items marked with 🚧 are planned but not yet implemented.

```
must.kdroiders.hustlehub/
├── 📁 activities/              # MainActivity
├── 📁 appHilt/                 # Hilt Application class
├── 📁 data/                    # Data Layer
│   ├── model/                 # Domain models (User, Service)
│   └── repository/            # Repository implementations
│       ├── UserRepository.kt
│       └── StorageRepository.kt
│   ├── local/    🚧            # Room DAOs, entities
│   ├── remote/   🚧            # Firebase & API data sources
│   └── dto/      🚧            # Data transfer objects
│
├── 📁 domain/   🚧             # Domain Layer (planned)
│   ├── model/                 # Pure Kotlin domain models
│   ├── repository/            # Repository interfaces
│   └── usecase/               # Business logic
│
├── 📁 datastore/               # DataStore Preferences
│   └── UserPreferences.kt
├── 📁 di/                      # Hilt Modules
│   ├── AppModule.kt
│   └── SupabaseModule.kt
│
├── 📁 navigation/              # Navigation 3
│   ├── HustleHubNavGraph.kt
│   ├── HustleNavKeys.kt
│   ├── BottomNavigationBar.kt
│   └── MainScaffold.kt
│
├── 📁 onboarding/              # First-run carousel
├── 📁 splash/                  # Splash screen + auth-gate
│
├── 📁 sharedComposables/       # Reusable composables
│   ├── EmptyStateView.kt
│   ├── ErrorView.kt
│   ├── HustleButton.kt
│   ├── HustleCard.kt
│   ├── HustleTextField.kt
│   ├── LoadingIndicator.kt
│   └── RatingBar.kt
│
├── 📁 ui/                      # Presentation Layer
│   ├── auth/                  # Sign-up / login
│   │   └── presentation/
│   │       ├── view/
│   │       └── viewmodel/
│   ├── components/            # UI-specific components
│   ├── features/              # Feature screens
│   │   ├── home/
│   │   ├── map/
│   │   ├── chat/
│   │   ├── profile/
│   │   └── profilesetup/
│   ├── portfolio/             # Portfolio upload
│   └── theme/                 # Design system
│       ├── Color.kt
│       ├── Type.kt
│       ├── Shape.kt
│       └── Theme.kt
│
└── 📁 util/                    # Utilities
    └── ImageUtils.kt
```

## Data Flow

### Example: Creating a Service

```
User Action (UI)
    ↓
[CreateServiceScreen]
    ↓ calls
[CreateServiceViewModel]
    ↓ executes
[CreateServiceUseCase]
    ↓ calls
[ServiceRepository] (interface)
    ↓ implements
[ServiceRepositoryImpl]
    ↓ uses
[FirestoreService] + [StorageService]
    ↓
Firebase Cloud
```

**Code Example:**

```kotlin
// 1. User clicks "Create Service" button
CreateServiceScreen(
    onCreateClick = { viewModel.createService(serviceData) }
)

// 2. ViewModel handles UI logic
class CreateServiceViewModel(
    private val createServiceUseCase: CreateServiceUseCase
) : ViewModel() {
    
    fun createService(data: ServiceData) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            
            createServiceUseCase(data).collect { result ->
                _uiState.value = when (result) {
                    is Result.Success -> UiState.Success
                    is Result.Error -> UiState.Error(result.message)
                }
            }
        }
    }
}

// 3. Use Case contains business logic
class CreateServiceUseCase(
    private val repository: ServiceRepository
) {
    operator fun invoke(data: ServiceData): Flow<Result<String>> {
        // Validation
        if (data.title.isBlank()) {
            return flowOf(Result.Error("Title required"))
        }
        
        // Call repository
        return repository.createService(data.toDomain())
    }
}

// 4. Repository coordinates data sources
class ServiceRepositoryImpl(
    private val firestore: FirestoreService,
    private val storage: StorageService,
    private val localDao: ServiceDao
) : ServiceRepository {
    
    override suspend fun createService(service: Service): Result<String> {
        // Upload images first
        val imageUrls = service.portfolio.map { uri ->
            storage.uploadImage(uri)
        }
        
        // Save to Firestore
        val serviceId = firestore.createService(
            service.copy(portfolio = imageUrls)
        )
        
        // Cache locally
        localDao.insert(service.toEntity())
        
        return Result.Success(serviceId)
    }
}
```

## Key Components

### ViewModels

**Purpose:** Manage UI state and handle user interactions

```kotlin
class ServiceDetailViewModel(
    private val getServiceUseCase: GetServiceUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    private val serviceId = savedStateHandle.get<String>("serviceId")!!
    
    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()
    
    init {
        loadService()
    }
    
    private fun loadService() {
        viewModelScope.launch {
            getServiceUseCase(serviceId).collect { result ->
                _uiState.value = result.toUiState()
            }
        }
    }
}
```

### Use Cases

**Purpose:** Encapsulate business logic

```kotlin
class SearchServicesUseCase(
    private val repository: ServiceRepository,
    private val geminiApi: GeminiApiService
) {
    suspend operator fun invoke(
        query: String,
        useAI: Boolean = true
    ): Flow<Result<List<Service>>> = flow {
        
        if (useAI) {
            // AI-powered search
            val services = repository.getAllServices().first()
            val matches = geminiApi.matchServices(query, services)
            emit(Result.Success(matches))
        } else {
            // Simple text search
            repository.searchServices(query).collect { services ->
                emit(Result.Success(services))
            }
        }
    }
}
```

### Repositories

**Purpose:** Abstract data sources

```kotlin
interface ServiceRepository {
    suspend fun createService(service: Service): Result<String>
    fun getServices(): Flow<List<Service>>
    fun searchServices(query: String): Flow<List<Service>>
}

class ServiceRepositoryImpl(
    private val firestore: FirestoreService,
    private val localDao: ServiceDao
) : ServiceRepository {
    
    override fun getServices(): Flow<List<Service>> = flow {
        // Try local cache first
        val cached = localDao.getAll()
        if (cached.isNotEmpty()) {
            emit(cached.map { it.toDomain() })
        }
        
        // Fetch from remote
        val remote = firestore.getServices()
        localDao.insertAll(remote.map { it.toEntity() })
        emit(remote)
    }
}
```

## State Management

### UI State Pattern

```kotlin
sealed interface UiState<out T> {
    object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

// Usage in ViewModel
private val _uiState = MutableStateFlow<UiState<Service>>(UiState.Loading)
val uiState: StateFlow<UiState<Service>> = _uiState.asStateFlow()

// Usage in Composable
when (val state = uiState.collectAsState().value) {
    is UiState.Loading -> LoadingIndicator()
    is UiState.Success -> ServiceContent(state.data)
    is UiState.Error -> ErrorMessage(state.message)
}
```

## Dependency Injection (Hilt)

### Module Structure

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth? {
        return try {
            FirebaseAuth.getInstance()
        } catch (e: IllegalStateException) {
            Timber.w(e, "Firebase not initialized — running without auth")
            null
        }
    }
    
    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore? {
        return try {
            FirebaseFirestore.getInstance()
        } catch (e: IllegalStateException) {
            null
        }
    }

    @Provides
    @Singleton
    fun provideDataStore(
        @ApplicationContext context: Context
    ): DataStore<Preferences> = context.dataStore

    @Provides
    @Singleton
    fun provideUserPreferences(
        dataStore: DataStore<Preferences>
    ): UserPreferences = UserPreferences(dataStore)

    @Provides
    @Singleton
    fun provideUserRepository(
        firestore: FirebaseFirestore?,
        storage: FirebaseStorage?
    ): UserRepository {
        if (firestore != null && storage != null) {
            return UserRepositoryImpl(firestore, storage)
        }
        return NoopUserRepository()
    }
}
```

> **Note:** Firebase instances are provided as nullable (`?`) to allow the app
> to run without Firebase during development. A `NoopUserRepository` is used as
> a fallback when Firebase is unavailable.

### Injection in ViewModels

```kotlin
@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val auth: FirebaseAuth?,
    private val userPreferences: UserPreferences
) : ViewModel() {
    // ViewModel implementation
}
```

## Navigation

HustleHub uses **Navigation 3** (`androidx.navigation3`) with serializable `NavKey` data objects.

### Route Definition

```kotlin
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

// Root-flow keys
@Serializable data object Splash : NavKey
@Serializable data object Onboarding : NavKey
@Serializable data object Login : NavKey
@Serializable data object SignUp : NavKey
@Serializable data object ProfileSetup : NavKey
@Serializable data object MainShell : NavKey

// Bottom-tab keys
@Serializable data object BottomHome : NavKey
@Serializable data object BottomMap : NavKey
@Serializable data object BottomChat : NavKey
@Serializable data object BottomProfile : NavKey

// Detail keys
@Serializable data object PortfolioUpload : NavKey
@Serializable data class ChatDetail(val chatId: String) : NavKey
```

### Navigation Graph

```kotlin
@Composable
fun HustleHubNav() {
    val backstack = rememberNavBackStack(Splash)

    NavDisplay(
        backStack = backstack,
        onBack = { if (backstack.size > 1) backstack.remove(backstack.last()) },
        entryProvider = entryProvider {
            entry<Splash> {
                SplashScreen(
                    onNavigate = { destination ->
                        backstack.clear()
                        backstack.add(/* resolved NavKey */)
                    }
                )
            }

            entry<MainShell> {
                // Inner NavDisplay for bottom-tab destinations
                MainShellScreen(
                    onNavigateToPortfolio = { backstack.add(PortfolioUpload) }
                )
            }

            entry<ChatDetail> { key ->
                ChatDetailScreen(chatId = key.chatId)
            }
        }
    )
}
```

## Testing Strategy

### Unit Tests

```kotlin
class CreateServiceUseCaseTest {
    
    private lateinit var useCase: CreateServiceUseCase
    private lateinit var repository: FakeServiceRepository
    
    @Before
    fun setup() {
        repository = FakeServiceRepository()
        useCase = CreateServiceUseCase(repository)
    }
    
    @Test
    fun `create service with valid data returns success`() = runTest {
        val service = Service(title = "Test Service")
        
        val result = useCase(service).first()
        
        assertTrue(result is Result.Success)
    }
}
```

### UI Tests

```kotlin
@HiltAndroidTest
class LoginScreenTest {
    
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()
    
    @Test
    fun loginWithValidCredentials_navigatesToHome() {
        composeTestRule.setContent {
            LoginScreen()
        }
        
        composeTestRule.onNodeWithTag("emailField")
            .performTextInput("test@must.ac.ke")
        
        composeTestRule.onNodeWithTag("passwordField")
            .performTextInput("password123")
        
        composeTestRule.onNodeWithText("Login")
            .performClick()
        
        // Verify navigation
        composeTestRule.onNodeWithText("Home")
            .assertIsDisplayed()
    }
}
```

## Design Patterns Used

| Pattern | Usage | Example |
|---------|-------|---------|
| **Repository** | Abstract data sources | `ServiceRepository` |
| **Use Case** | Single responsibility business logic | `CreateServiceUseCase` |
| **Observer** | Reactive state updates | `StateFlow`, `Flow` |
| **Factory** | Create complex objects | `ServiceFactory` |
| **Singleton** | Single instance services | Firebase instances |
| **Dependency Injection** | Loose coupling | Hilt modules |

## Best Practices

### ✅ Do

- Keep ViewModels UI-agnostic
- Use sealed classes for state
- Inject dependencies via constructor
- Write tests for use cases
- Use Flow for reactive streams
- Cache data locally with Room

### ❌ Don't

- Put business logic in ViewModels
- Access Firebase directly from UI
- Use LiveData (prefer StateFlow)
- Hardcode strings (use resources)
- Ignore error handling
- Block main thread

---

**See also:**
- [Clean Architecture](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Hilt Documentation](https://dagger.dev/hilt/)
