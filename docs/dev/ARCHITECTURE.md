# 🏗️ Architecture Overview

Quick reference for understanding the HustleHub codebase structure.

## Architecture Pattern

HustleHub follows **Clean Architecture** with **MVVM** pattern.

```
┌─────────────────────────────────────┐
│       Presentation Layer            │  ← UI (Compose) + ViewModels
│   (Jetpack Compose + ViewModels)   │
└─────────────┬───────────────────────┘
              │ observes StateFlow
┌─────────────▼───────────────────────┐
│         Domain Layer                │  ← Business Logic
│  (Use Cases + Repository Interfaces)│
└─────────────┬───────────────────────┘
              │ implements
┌─────────────▼───────────────────────┐
│          Data Layer                 │  ← Data Sources
│ (Repositories + Data Sources)       │
│  ┌──────────┐  ┌─────────────┐     │
│  │   Room   │  │   Firebase  │     │
│  │ (Cache)  │  │  (Remote)   │     │
│  └──────────┘  └─────────────┘     │
└─────────────────────────────────────┘
```

## Package Structure

```
com.hustlehub.app/
├── 📁 data/                    # Data Layer
│   ├── local/                  # Room database
│   │   ├── dao/               # Data Access Objects
│   │   ├── entity/            # Room entities
│   │   └── AppDatabase.kt
│   ├── remote/                # Remote data sources
│   │   ├── firebase/          # Firebase services
│   │   └── api/               # Retrofit APIs
│   ├── repository/            # Repository implementations
│   └── dto/                   # Data Transfer Objects
│
├── 📁 domain/                  # Domain Layer
│   ├── model/                 # Domain models (pure Kotlin)
│   ├── repository/            # Repository interfaces
│   └── usecase/               # Business logic
│       ├── auth/
│       ├── service/
│       ├── messaging/
│       └── discovery/
│
├── 📁 presentation/            # Presentation Layer
│   ├── auth/                  # Auth screens
│   │   ├── login/
│   │   │   ├── LoginScreen.kt
│   │   │   └── LoginViewModel.kt
│   │   └── signup/
│   ├── discovery/             # Discovery screens
│   ├── messaging/             # Chat screens
│   ├── map/                   # Map screen
│   ├── profile/               # Profile screens
│   ├── components/            # Reusable composables
│   └── theme/                 # Design system
│
├── 📁 di/                      # Dependency Injection
│   ├── AppModule.kt
│   ├── DataModule.kt
│   └── RepositoryModule.kt
│
├── 📁 navigation/              # Navigation
│   ├── NavGraph.kt
│   └── Routes.kt
│
└── 📁 util/                    # Utilities
    ├── Constants.kt
    ├── Extensions.kt
    └── ValidationUtils.kt
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
object DataModule {
    
    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore {
        return Firebase.firestore
    }
    
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "hustlehub_db"
        ).build()
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    
    @Binds
    @Singleton
    abstract fun bindServiceRepository(
        impl: ServiceRepositoryImpl
    ): ServiceRepository
}
```

### Injection in ViewModels

```kotlin
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val signInUseCase: SignInUseCase,
    private val validateEmailUseCase: ValidateEmailUseCase
) : ViewModel() {
    // ViewModel implementation
}
```

## Navigation

### Route Definition

```kotlin
sealed class Routes(val route: String) {
    object Home : Routes("home")
    object Login : Routes("login")
    object ServiceDetail : Routes("service/{serviceId}") {
        fun createRoute(serviceId: String) = "service/$serviceId"
    }
}
```

### Navigation Graph

```kotlin
@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Routes.Home.route) {
            HomeScreen(
                onServiceClick = { serviceId ->
                    navController.navigate(
                        Routes.ServiceDetail.createRoute(serviceId)
                    )
                }
            )
        }
        
        composable(
            route = Routes.ServiceDetail.route,
            arguments = listOf(
                navArgument("serviceId") { type = NavType.StringType }
            )
        ) {
            ServiceDetailScreen()
        }
    }
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
