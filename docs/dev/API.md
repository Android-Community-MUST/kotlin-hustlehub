# 📡 API Reference

All data operations go through the **HustleHub Spring Boot backend** at `/api/v1/`.
Firebase is used **only** for Authentication (ID tokens) and Cloud Messaging (FCM push).

---

## Base URLs

| Build Variant | Base URL |
|---------------|----------|
| `debug` | `http://10.0.2.2:8080/api/v1/` |
| `release` | `https://api.hustlehub.app/api/v1/` |

> **Emulator note**: `10.0.2.2` maps to `localhost` on the host machine when running on the Android emulator.

---

## Authentication

Every request (except `/auth/**`) must include a Firebase ID Token in the `Authorization` header:

```
Authorization: Bearer <firebase_id_token>
```

The `AuthInterceptor` fetches this token automatically from `FirebaseAuth.currentUser?.getIdToken(false)`.
A `401 Unauthorized` response triggers an automatic token refresh via OkHttp's `Authenticator`.

---

## Auth Endpoints

### Register / Sync User Profile
`POST /api/v1/auth/register`

Called after Firebase sign-up to persist the user on the backend.

**Request:**
```json
{
  "firebaseUid": "user_abc123",
  "name": "John Kamau",
  "email": "john.kamau@must.ac.ke",
  "studentId": "COM/0234/2023",
  "campus": "Meru University",
  "course": "Computer Science",
  "yearOfStudy": 3,
  "hostel": "Hostel B, Room 204",
  "role": "BOTH",
  "profilePhotoUrl": "https://api.hustlehub.app/media/users/abc123/profile.jpg",
  "fcmToken": "fcm_token_here"
}
```

**Response `201 Created`:**
```json
{
  "userId": "user_abc123",
  "name": "John Kamau",
  "email": "john.kamau@must.ac.ke",
  "role": "BOTH",
  "isVerified": true,
  "createdAt": "2026-02-14T10:00:00Z"
}
```

---

### Update FCM Token
`PUT /api/v1/users/fcm-token`

**Request:**
```json
{
  "fcmToken": "new_fcm_token_here"
}
```

**Response `200 OK`:**
```json
{ "message": "FCM token updated" }
```

---

## User / Profile Endpoints

### Get Current User Profile
`GET /api/v1/users/me`

**Response `200 OK`:**
```json
{
  "userId": "user_abc123",
  "name": "John Kamau",
  "email": "john.kamau@must.ac.ke",
  "studentId": "COM/0234/2023",
  "campus": "Meru University",
  "course": "Computer Science",
  "yearOfStudy": 3,
  "hostel": "Hostel B, Room 204",
  "role": "BOTH",
  "profilePhotoUrl": "https://...",
  "bio": "Quality laundry services with free pickup",
  "hustleScore": 4.7,
  "badges": ["TOP_RATED", "FAST_RESPONDER"],
  "isVerified": true,
  "isOnline": true,
  "createdAt": "2026-02-01T10:00:00Z",
  "lastSeen": "2026-02-14T14:30:00Z"
}
```

---

### Update User Profile
`PUT /api/v1/users/me`

**Request:**
```json
{
  "name": "John Kamau",
  "bio": "Updated bio",
  "hostel": "Hostel C",
  "yearOfStudy": 4
}
```

---

### Get User Profile by ID
`GET /api/v1/users/{userId}`

---

### Update Online Status
`PUT /api/v1/users/me/status`

**Request:**
```json
{ "isOnline": true }
```

---

## Services Endpoints

### Create Service
`POST /api/v1/services`

**Request:**
```json
{
  "title": "Professional Braiding Services",
  "category": "SALON",
  "description": "All styles — box braids, cornrows, twists, and more.",
  "minPrice": 300,
  "maxPrice": 800,
  "tags": ["braids", "hair", "salon", "beauty"],
  "location": {
    "lat": 0.0515,
    "lng": 37.6456,
    "label": "Hostel C"
  },
  "openToBarter": true
}
```

**Response `201 Created`:** Full service object.

---

### Get Service by ID
`GET /api/v1/services/{serviceId}`

---

### Update Service
`PUT /api/v1/services/{serviceId}`

---

### Delete Service
`DELETE /api/v1/services/{serviceId}`

---

### Toggle Availability
`PUT /api/v1/services/{serviceId}/availability`

**Request:**
```json
{ "availability": "BUSY" }
```
Allowed values: `AVAILABLE`, `BUSY`, `OFFLINE`

---

### Get My Services
`GET /api/v1/services/me`

---

## Discovery Endpoints

### Browse Services (Paginated)
`GET /api/v1/discovery/services`

**Query Parameters:**

| Param | Type | Example |
|-------|------|---------|
| `category` | String | `SALON` |
| `availability` | String | `AVAILABLE` |
| `minRating` | Float | `4.0` |
| `maxPrice` | Int | `800` |
| `lat` | Double | `0.0515` |
| `lng` | Double | `37.6456` |
| `radiusKm` | Double | `1.0` |
| `sortBy` | String | `RATING` / `DISTANCE` / `NEWEST` |
| `page` | Int | `0` |
| `size` | Int | `20` |

**Response `200 OK`:**
```json
{
  "content": [
    {
      "serviceId": "service_xyz789",
      "providerId": "user_abc123",
      "providerName": "Jane Wanjiku",
      "providerPhotoUrl": "https://...",
      "title": "Professional Braiding Services",
      "category": "SALON",
      "priceRange": "300 - 800",
      "portfolioImages": ["https://..."],
      "averageRating": 4.8,
      "reviewCount": 23,
      "availability": "AVAILABLE",
      "distanceMeters": 180,
      "location": { "lat": 0.0515, "lng": 37.6456, "label": "Hostel C" }
    }
  ],
  "totalElements": 87,
  "totalPages": 5,
  "number": 0,
  "size": 20
}
```

---

### Text Search
`GET /api/v1/discovery/search?q=braids&page=0&size=20`

---

### AI-Powered Natural Language Search
`POST /api/v1/discovery/ai-search`

**Request:**
```json
{
  "query": "I need someone to do box braids near Hostel C under 500",
  "userLocation": {
    "lat": 0.0515,
    "lng": 37.6456
  },
  "maxResults": 10
}
```

**Response `200 OK`:**
```json
{
  "matches": [
    {
      "serviceId": "service_xyz789",
      "providerId": "user_abc123",
      "relevanceScore": 0.95,
      "matchReason": "Offers box braiding, within 200m of Hostel C, price range 300–800 KSh",
      "distance": 180,
      "priceRange": "300 - 800"
    }
  ],
  "queryUnderstanding": {
    "service": "box braids",
    "location": "Hostel C",
    "maxPrice": 500,
    "category": "SALON"
  }
}
```

---

### Get Map Pins (Nearby Providers)
`GET /api/v1/discovery/map-pins?lat=0.0515&lng=37.6456&radiusKm=2.0&category=SALON`

**Response `200 OK`:**
```json
[
  {
    "serviceId": "service_xyz789",
    "providerId": "user_abc123",
    "providerName": "Jane Wanjiku",
    "providerPhotoUrl": "https://...",
    "title": "Professional Braiding",
    "category": "SALON",
    "availability": "AVAILABLE",
    "averageRating": 4.8,
    "location": { "lat": 0.0515, "lng": 37.6456 }
  }
]
```

---

## Messaging Endpoints

### Get Conversation List
`GET /api/v1/conversations?page=0&size=20`

**Response `200 OK`:**
```json
{
  "content": [
    {
      "conversationId": "conv_123abc",
      "otherParticipant": {
        "userId": "user_xyz789",
        "name": "Jane Wanjiku",
        "photoUrl": "https://..."
      },
      "serviceId": "service_xyz789",
      "serviceTitle": "Professional Braiding",
      "lastMessage": "I'm available tomorrow at 2pm",
      "lastMessageType": "TEXT",
      "lastMessageAt": "2026-02-14T14:30:00Z",
      "unreadCount": 2
    }
  ]
}
```

---

### Get or Create Conversation
`POST /api/v1/conversations`

**Request:**
```json
{
  "otherUserId": "user_xyz789",
  "serviceId": "service_xyz789"
}
```

**Response `200 OK` or `201 Created`:** Conversation object.

---

### Get Message History
`GET /api/v1/conversations/{conversationId}/messages?page=0&size=50`

**Response `200 OK`:** Paginated list of messages (newest first).

---

### Mark Conversation as Read
`PUT /api/v1/conversations/{conversationId}/read`

---

## Real-Time Chat — WebSocket (STOMP over SockJS)

Connect to: `ws://10.0.2.2:8080/ws` (debug) / `wss://api.hustlehub.app/ws` (release)

Include Firebase token as a query parameter on connect:
```
/ws?token=<firebase_id_token>
```

### Send a Message
**Destination:** `/app/chat.send`

```json
{
  "conversationId": "conv_123abc",
  "type": "TEXT",
  "content": "I'm available tomorrow at 2pm",
  "mediaUrl": null,
  "metadata": null
}
```

**Message Types:** `TEXT`, `VOICE`, `IMAGE`, `LOCATION`, `SERVICE_CARD`

### Receive Messages
**Subscribe to:** `/topic/conversation/{conversationId}`

**Payload received:**
```json
{
  "messageId": "msg_456def",
  "conversationId": "conv_123abc",
  "senderId": "user_abc123",
  "type": "TEXT",
  "content": "I'm available tomorrow at 2pm",
  "mediaUrl": null,
  "metadata": null,
  "timestamp": "2026-02-14T14:30:00Z",
  "deliveredAt": "2026-02-14T14:30:05Z",
  "readAt": null
}
```

### Typing Indicators
**Send:** `/app/chat.typing`
```json
{ "conversationId": "conv_123abc", "isTyping": true }
```
**Receive:** `/topic/conversation/{conversationId}/typing`

### Presence (Online/Offline)
**Subscribe to:** `/topic/user/{userId}/presence`

---

## Reviews Endpoints

### Submit Review
`POST /api/v1/reviews`

**Request:**
```json
{
  "serviceId": "service_xyz789",
  "rating": 5,
  "comment": "Amazing braids! Very professional.",
  "isAnonymous": false
}
```

---

### Get Reviews for a Service
`GET /api/v1/services/{serviceId}/reviews?page=0&size=10`

---

### Report a Review
`POST /api/v1/reviews/{reviewId}/report`

**Request:**
```json
{ "reason": "Inappropriate content" }
```

---

## Media Upload Endpoints

### Upload Image
`POST /api/v1/media/upload`

**Content-Type:** `multipart/form-data`

**Form fields:**
- `file` — the image file (JPEG, PNG)
- `type` — `PROFILE_PHOTO` / `PORTFOLIO` / `CHAT_IMAGE`
- `entityId` — service ID or conversation ID (optional)

**Response `200 OK`:**
```json
{
  "mediaId": "media_abc123",
  "url": "https://api.hustlehub.app/media/services/xyz789/img1.jpg",
  "thumbnailUrl": "https://api.hustlehub.app/media/services/xyz789/img1_thumb.jpg",
  "type": "PORTFOLIO"
}
```

---

### Upload Voice Note
`POST /api/v1/media/upload/voice`

**Content-Type:** `multipart/form-data`

**Form fields:**
- `file` — audio file (AAC/M4A)
- `conversationId` — conversation this voice note belongs to

**Response `200 OK`:**
```json
{
  "mediaId": "media_voice_123",
  "url": "https://api.hustlehub.app/media/voice/abc123.m4a",
  "durationSeconds": 12,
  "type": "VOICE_NOTE"
}
```

---

## Notifications Endpoints

### Get Notification History
`GET /api/v1/notifications?page=0&size=20`

---

### Mark Notification as Read
`PUT /api/v1/notifications/{notificationId}/read`

---

### Mark All as Read
`PUT /api/v1/notifications/read-all`

---

## Error Handling

All errors follow a consistent structure:

```json
{
  "timestamp": "2026-02-14T14:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Email must end with @must.ac.ke",
  "path": "/api/v1/auth/register"
}
```

### Common HTTP Status Codes

| Code | Meaning |
|------|---------|
| `200` | OK |
| `201` | Created |
| `400` | Bad Request (validation error) |
| `401` | Unauthorized (invalid/expired Firebase token) |
| `403` | Forbidden (action not allowed for this user) |
| `404` | Resource not found |
| `409` | Conflict (duplicate entry) |
| `429` | Too Many Requests (rate limited) |
| `500` | Internal Server Error |

### Kotlin Handling Example

```kotlin
// Repository pattern with sealed Result
suspend fun getServices(): Result<List<Service>> {
    return try {
        val response = apiService.getServices()
        if (response.isSuccessful) {
            Result.Success(response.body()!!.content)
        } else {
            Result.Error("Server error: ${response.code()}")
        }
    } catch (e: IOException) {
        Result.Error("No internet connection")
    } catch (e: HttpException) {
        Result.Error("HTTP ${e.code()}: ${e.message()}")
    }
}
```

---

## Rate Limits

| Endpoint group | Limit |
|----------------|-------|
| Auth endpoints | 10 req/min per IP |
| Discovery / Search | 60 req/min per user |
| AI Search | 20 req/min per user |
| Media upload | 30 uploads/hour per user |
| Chat messages | 120 messages/min per conversation |
| General API | 300 req/min per user |

---

## Testing Against Local Backend

```bash
# Start Spring Boot backend locally
./gradlew bootRun   # in backend repo

# Android emulator connects via 10.0.2.2:8080
# Physical device on same network: use your machine's LAN IP
```

### Using Firebase Auth Emulator (optional for local dev)
```kotlin
// In HustleHubApp.onCreate() — debug builds only
if (BuildConfig.DEBUG) {
    FirebaseAuth.getInstance().useEmulator("10.0.2.2", 9099)
}
```

---

**See also:**
- [Architecture Guide](ARCHITECTURE.md)
- [Setup Guide](SETUP.md)
- [Spring Boot Backend Repo](https://github.com/Android-Community-MUST/hustlehub-backend)
