# 📡 API Reference

Quick reference for all APIs used in HustleHub.

## Firebase APIs

### Authentication

#### Email/Password Sign Up

```kotlin
// AuthRepository.kt
suspend fun signUpWithEmail(email: String, password: String): Result<User>
```

**Example:**
```kotlin
val result = authRepository.signUpWithEmail(
    email = "student@must.ac.ke",
    password = "SecurePass123!"
)
```

#### Google Sign-In

```kotlin
suspend fun signInWithGoogle(idToken: String): Result<User>
```

### Firestore

#### Collections Structure

```
users/{userId}
services/{serviceId}
reviews/{reviewId}
conversations/{conversationId}
  └── messages/{messageId}
```

#### Create Service

```kotlin
// ServiceRepository.kt
suspend fun createService(service: Service): Result<String>
```

**Firestore Document:**
```json
{
  "serviceId": "service_xyz789",
  "providerId": "user_abc123",
  "title": "Professional Braiding Services",
  "category": "salon",
  "description": "All styles — box braids, cornrows, twists",
  "priceRange": "300-800",
  "portfolio": ["gs://path/to/image1.jpg"],
  "availability": "available",
  "averageRating": 4.8,
  "reviewCount": 23,
  "tags": ["braids", "hair", "salon"],
  "location": {
    "lat": 0.0515,
    "lng": 37.6456
  },
  "createdAt": "2026-02-14T10:00:00Z"
}
```

#### Query Services

```kotlin
// Get all services in a category
suspend fun getServicesByCategory(category: String): Flow<List<Service>>

// Search services
suspend fun searchServices(query: String): Flow<List<Service>>
```

**Firestore Query:**
```kotlin
firestore.collection("services")
    .whereEqualTo("category", "salon")
    .whereEqualTo("availability", "available")
    .orderBy("averageRating", Query.Direction.DESCENDING)
    .limit(20)
```

### Realtime Database

#### Message Structure

```
conversations/
  {conversationId}/
    messages/
      {messageId}/
        senderId: "user_abc123"
        type: "text"
        content: "Hello!"
        timestamp: 1707912345000
        readAt: null
```

#### Send Message

```kotlin
// MessageRepository.kt
suspend fun sendMessage(
    conversationId: String,
    message: Message
): Result<String>
```

**Example:**
```kotlin
val message = Message(
    senderId = currentUserId,
    type = MessageType.TEXT,
    content = "Hello!",
    timestamp = System.currentTimeMillis()
)
messageRepository.sendMessage(conversationId, message)
```

#### Listen to Messages

```kotlin
fun observeMessages(conversationId: String): Flow<List<Message>>
```

**Realtime Database Listener:**
```kotlin
database.reference
    .child("conversations/$conversationId/messages")
    .orderByChild("timestamp")
    .limitToLast(50)
    .addValueEventListener(...)
```

### Storage

#### Upload Image

```kotlin
// StorageRepository.kt
suspend fun uploadImage(
    uri: Uri,
    path: String
): Result<String> // Returns download URL
```

**Example:**
```kotlin
val downloadUrl = storageRepository.uploadImage(
    uri = imageUri,
    path = "services/${serviceId}/portfolio/${UUID.randomUUID()}.jpg"
)
```

**Storage Paths:**
```
users/{userId}/profile.jpg
services/{serviceId}/portfolio/{imageId}.jpg
messages/{conversationId}/{messageId}.jpg
```

### Cloud Messaging (FCM)

#### Send Notification

```kotlin
// NotificationRepository.kt
suspend fun sendNotification(
    userId: String,
    title: String,
    body: String,
    data: Map<String, String>
): Result<Unit>
```

**FCM Payload:**
```json
{
  "to": "fcm_token_here",
  "notification": {
    "title": "New Message",
    "body": "Jane sent you a message"
  },
  "data": {
    "type": "message",
    "conversationId": "conv_123abc",
    "senderId": "user_xyz789"
  }
}
```

## External APIs

### Gemini API

#### AI-Powered Search

**Endpoint:** `https://generativelanguage.googleapis.com/v1/models/gemini-pro:generateContent`

**Request:**
```kotlin
// GeminiApiService.kt
suspend fun matchServices(
    query: String,
    services: List<Service>
): Result<List<ServiceMatch>>
```

**Example Request Body:**
```json
{
  "contents": [{
    "parts": [{
      "text": "Match this query to services: 'braids near Gate B under 500'\n\nAvailable services:\n1. Professional Braiding - 300-800 KSh - Hostel C\n2. Quick Laundry - 200-400 KSh - Gate B\n\nReturn JSON with relevance scores."
    }]
  }]
}
```

**Response:**
```json
{
  "candidates": [{
    "content": {
      "parts": [{
        "text": "{\n  \"matches\": [\n    {\n      \"serviceId\": \"service_xyz789\",\n      \"relevanceScore\": 0.95,\n      \"matchReason\": \"Offers braiding, within budget, near location\"\n    }\n  ]\n}"
      }]
    }
  }]
}
```

**Implementation:**
```kotlin
val prompt = buildString {
    append("Match this query to services: '$query'\n\n")
    append("Available services:\n")
    services.forEachIndexed { index, service ->
        append("${index + 1}. ${service.title} - ${service.priceRange} KSh - ${service.location}\n")
    }
    append("\nReturn JSON with relevance scores.")
}

val response = geminiApi.generateContent(prompt)
val matches = parseMatches(response)
```

### Google Maps API

#### Maps SDK

**Add to build.gradle:**
```kotlin
dependencies {
    implementation("com.google.android.gms:play-services-maps:20.0.0")
    implementation("com.google.maps.android:maps-compose:8.1.0")
}
```

**Display Map:**
```kotlin
GoogleMap(
    modifier = Modifier.fillMaxSize(),
    cameraPositionState = cameraPositionState,
    properties = MapProperties(
        isMyLocationEnabled = true
    )
) {
    // Add markers
    services.forEach { service ->
        Marker(
            state = MarkerState(
                position = LatLng(
                    service.location.lat,
                    service.location.lng
                )
            ),
            title = service.title,
            snippet = service.category
        )
    }
}
```

#### Geocoding API

**Get Location from Address:**
```kotlin
// LocationRepository.kt
suspend fun geocodeAddress(address: String): Result<LatLng>
```

**Example:**
```kotlin
val location = locationRepository.geocodeAddress("Meru University, Gate B")
// Returns: LatLng(0.0515, 37.6456)
```

## Rate Limits & Quotas

### Firebase

| Service | Free Tier | Limit |
|---------|-----------|-------|
| Firestore Reads | 50K/day | After: $0.06/100K |
| Firestore Writes | 20K/day | After: $0.18/100K |
| Realtime DB | 1GB storage | After: $5/GB |
| Storage | 5GB | After: $0.026/GB |
| FCM | Unlimited | - |

### Gemini API

- **Free Tier**: 60 requests/minute
- **Paid**: Higher limits available

### Google Maps

- **Free Tier**: 28,000 map loads/month
- **After**: $7/1000 loads

## Error Handling

### Firebase Errors

```kotlin
try {
    val result = firestore.collection("services").get().await()
} catch (e: FirebaseException) {
    when (e) {
        is FirebaseNetworkException -> {
            // No internet connection
            Result.Error("No internet connection")
        }
        is FirebaseAuthException -> {
            // Authentication error
            Result.Error("Please sign in again")
        }
        else -> {
            // Generic error
            Result.Error("Something went wrong")
        }
    }
}
```

### API Errors

```kotlin
sealed class ApiError : Exception() {
    object NetworkError : ApiError()
    object Unauthorized : ApiError()
    data class ServerError(val code: Int) : ApiError()
    data class Unknown(val message: String) : ApiError()
}
```

## Testing

### Firebase Emulators

```bash
# Start emulators
firebase emulators:start --only auth,firestore,database,storage

# Connect to emulators in code
FirebaseFirestore.getInstance().useEmulator("10.0.2.2", 8080)
FirebaseAuth.getInstance().useEmulator("10.0.2.2", 9099)
```

### Mock API Responses

```kotlin
// Test doubles
class FakeServiceRepository : ServiceRepository {
    override suspend fun getServices() = flow {
        emit(listOf(
            Service(id = "1", title = "Test Service")
        ))
    }
}
```

---

**See also:**
- [Firebase Documentation](https://firebase.google.com/docs)
- [Gemini API Docs](https://ai.google.dev/docs)
- [Google Maps Platform](https://developers.google.com/maps)
