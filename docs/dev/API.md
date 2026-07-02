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

## Response Envelope & Pagination

### Successful Response Wrapper
Every successful REST request returns a generic `ApiResponse` envelope:

```json
{
  "success": true,
  "message": "Operation description",
  "data": { ... }
}
```

The specific models detailed below will always reside within the `"data"` field of this envelope.

### Paginated Response Wrapper
Endpoints returning lists of items wrap their contents in a custom `PageResponse` object inside the `data` block:

```json
{
  "content": [ ... ],
  "page": 0,
  "size": 20,
  "totalElements": 87,
  "totalPages": 5
}
```

*Note: The page index parameter is named `"page"` (0-indexed).*

### Error Response Format
All errors return a consistent body mapping to standard HTTP status codes:

```json
{
  "timestamp": "2026-02-14T14:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Email must end with @must.ac.ke",
  "path": "/api/v1/auth/register"
}
```

---

## Auth Endpoints

### Register / Sync User Profile
`POST /api/v1/auth/register` (No authentication header required)

Called after Firebase sign-up to persist the user details on the backend.

**Request:**
```json
{
  "firebaseUid": "user_abc123",
  "email": "john.kamau@must.ac.ke",
  "name": "John Kamau",
  "bio": "Optional user bio",
  "avatarUrl": "https://lh3.googleusercontent.com/.../photo.jpg",
  "phone": "0712345678",
  "campusLocation": "Hostel B, Room 204"
}
```

**Response `201 Created`:**
```json
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "id": "78e9069d-210d-45be-91c6-1c88c75dfb3f",
    "firebaseUid": "user_abc123",
    "email": "john.kamau@must.ac.ke",
    "name": "John Kamau",
    "role": "CUSTOMER",
    "bio": "Optional user bio",
    "avatarUrl": "https://lh3.googleusercontent.com/.../photo.jpg",
    "phone": "0712345678",
    "campusLocation": "Hostel B, Room 204",
    "lat": null,
    "lng": null,
    "isVerified": true,
    "isActive": true,
    "createdAt": "2026-02-14T10:00:00Z",
    "updatedAt": "2026-02-14T10:00:00Z"
  }
}

```

---

### Update FCM Token
`PUT /api/v1/users/fcm-token`

Saves or updates a user's active device FCM token for push notifications. Caps at the 5 most-recent unique tokens.

**Request:**
```json
{
  "token": "new_fcm_token_here"
}
```

**Response `204 No Content`** (Empty body).

---

## User / Profile Endpoints

### Get Current User Profile
`GET /api/v1/users/me`

**Response `200 OK`:**
```json
{
  "success": true,
  "message": "Profile fetched successfully",
  "data": {
    "id": "78e9069d-210d-45be-91c6-1c88c75dfb3f",
    "firebaseUid": "user_abc123",
    "email": "john.kamau@must.ac.ke",
    "name": "John Kamau",
    "role": "CUSTOMER",
    "bio": "Quality laundry services with free pickup",
    "avatarUrl": "https://...",
    "phone": "0712345678",
    "campusLocation": "Hostel B, Room 204",
    "lat": -0.0515,
    "lng": 37.6456,
    "isVerified": true,
    "isActive": true,
    "createdAt": "2026-02-01T10:00:00Z",
    "updatedAt": "2026-02-14T14:30:00Z"
  }
}

```

---

### Update User Profile
`PUT /api/v1/users/me`

Updates the profile fields of the currently authenticated user.

**Request:**
```json
{
  "name": "John Kamau",
  "bio": "Updated bio details",
  "avatarUrl": "https://...",
  "phone": "0712345678",
  "campusLocation": "Hostel C"
}
```

**Response `200 OK`:** Returns the updated user profile in `UserResponse` format.

---

### Get User Profile by ID
`GET /api/v1/users/{userId}`

Fetches public profile details of any user by their primary PostgreSQL UUID.

**Response `200 OK`:** Returns the user profile in `UserResponse` format.

---

### Update Online Status
`PUT /api/v1/users/me/status`

**Request:**
```json
{ 
  "isOnline": true 
}
```

**Response `204 No Content`** (Empty body).

---

### Update User Location
`PUT /api/v1/users/me/location`

Updates the geographic latitude and longitude coordinates of the currently authenticated user in the database.

**Request:**
```json
{
  "lat": -0.0515,
  "lng": 37.6456
}
```

**Response `204 No Content`** (Empty body).

---

### Get Nearby Providers (PostGIS)
`GET /api/v1/users/nearby`

Fetches service providers located within a specified distance using PostGIS spatial queries.

**Query Parameters:**
- `lat` (Double, Required): Center latitude.
- `lng` (Double, Required): Center longitude.
- `radiusMeters` (Double, Optional, default `1000.0`): Distance limit in meters.

**Response `200 OK`:**
```json
{
  "success": true,
  "message": "Nearby providers fetched successfully",
  "data": [
    {
      "id": "78e9069d-210d-45be-91c6-1c88c75dfb3f",
      "firebaseUid": "user_abc123",
      "email": "jane.kamau@must.ac.ke",
      "name": "Jane Kamau",
      "role": "PROVIDER",
      "bio": "Professional Hair Styling",
      "avatarUrl": "https://...",
      "phone": "0712345678",
      "campusLocation": "Hostel C",
      "lat": -0.0524,
      "lng": 37.6456,
      "isVerified": true,
      "isActive": true,
      "createdAt": "2026-02-14T10:00:00Z",
      "updatedAt": "2026-02-14T10:00:00Z"
    }
  ]
}
```

---


### Block User
`POST /api/v1/users/{userId}/block`

Blocks a user by their UUID. Blocked users cannot see each other's profiles, services, or send chat messages.

**Response `204 No Content`** (Empty body).

---

### Unblock User
`DELETE /api/v1/users/{userId}/block`

**Response `204 No Content`** (Empty body).

---

### Get Blocked Users
`GET /api/v1/users/me/blocked`

**Response `200 OK`:** Returns a list of blocked users:
```json
{
  "success": true,
  "message": "Blocked users fetched successfully",
  "data": [
    {
      "id": "89fa88c2-321a-45be-12c6-3d88c75dfb3f",
      "firebaseUid": "user_xyz789",
      "email": "jane.wanjiku@must.ac.ke",
      ...
    }
  ]
}
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
*Categories:* `SALON`, `LAUNDRY`, `TUTORING`, `FOOD`, `TECH`, `FASHION`, `PHOTOGRAPHY`, `DESIGN`, `OTHER`

**Response `201 Created`:**
```json
{
  "success": true,
  "message": "Service created successfully",
  "data": {
    "serviceId": "aa1c969d-210d-45be-91c6-1c88c75dfb3f",
    "providerId": "78e9069d-210d-45be-91c6-1c88c75dfb3f",
    "title": "Professional Braiding Services",
    "category": "SALON",
    "description": "All styles — box braids, cornrows, twists, and more.",
    "priceRange": "300 - 800 KSh",
    "portfolioImages": [],
    "tags": ["braids", "hair", "salon", "beauty"],
    "availability": "AVAILABLE",
    "avgRating": 0.0,
    "reviewCount": 0,
    "location": {
      "lat": 0.0515,
      "lng": 37.6456,
      "label": "Hostel C"
    },
    "openToBarter": true,
    "createdAt": "2026-02-14T10:00:00Z",
    "updatedAt": "2026-02-14T10:00:00Z"
  }
}
```

---

### Get Service by ID
`GET /api/v1/services/{serviceId}`

**Response `200 OK`:** Service details wrapped in `ApiResponse`.

---

### Update Service
`PUT /api/v1/services/{serviceId}`

**Request:** Same optional fields as `CreateService`.

**Response `200 OK`:** Updated service details wrapped in `ApiResponse`.

---

### Delete Service
`DELETE /api/v1/services/{serviceId}`

**Response `204 No Content`** (Empty body).

---

### Toggle Availability
`PUT /api/v1/services/{serviceId}/availability`

**Request:**
```json
{ 
  "availability": "BUSY" 
}
```
*Allowed values:* `AVAILABLE`, `BUSY`, `OFFLINE`

**Response `200 OK`:** Availability update message wrapped in `ApiResponse`.

---

### Get My Services
`GET /api/v1/services/me`

**Response `200 OK`:** List of user's own services wrapped in `ApiResponse`.

---

## Discovery Endpoints

### Browse Services (Paginated)
`GET /api/v1/discovery/services`

**Query Parameters:**

| Param | Type | Example |
|-------|------|---------|
| `category` | String | `SALON` |
| `availability` | String | `AVAILABLE` |
| `minRating` | Double | `4.0` |
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
  "success": true,
  "message": "Services fetched",
  "data": {
    "content": [
      {
        "serviceId": "aa1c969d-210d-45be-91c6-1c88c75dfb3f",
        "providerId": "78e9069d-210d-45be-91c6-1c88c75dfb3f",
        "title": "Professional Braiding Services",
        "category": "SALON",
        "description": "All styles — box braids, cornrows, twists, and more.",
        "priceRange": "300 - 800 KSh",
        "portfolioImages": [],
        "tags": ["braids", "hair", "salon", "beauty"],
        "availability": "AVAILABLE",
        "avgRating": 4.8,
        "reviewCount": 23,
        "location": {
          "lat": 0.0515,
          "lng": 37.6456,
          "label": "Hostel C"
        },
        "openToBarter": true,
        "distanceMeters": 180.2,
        "createdAt": "2026-02-14T10:00:00Z",
        "updatedAt": "2026-02-14T10:00:00Z"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 1,
    "totalPages": 1
  }
}
```

---

### Text Search
`GET /api/v1/discovery/search?q=braids&page=0&size=20`

Performs a keyword search on title, description, and tags using `ILIKE` queries.

**Response `200 OK`:** Paginated search results formatted as `PageResponse<ServiceResponse>` wrapped in `ApiResponse`.

---

### AI-Powered Natural Language Search
`POST /api/v1/discovery/ai-search`

Takes a natural language search query, parses parameters via Gemini, and queries the database.

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
  "success": true,
  "message": "AI search results",
  "data": {
    "matches": [
      {
        "serviceId": "aa1c969d-210d-45be-91c6-1c88c75dfb3f",
        "providerId": "78e9069d-210d-45be-91c6-1c88c75dfb3f",
        "title": "Professional Braiding Services",
        "category": "SALON",
        "priceRange": "300 - 800 KSh",
        "relevanceScore": 0.95,
        "matchReason": "Offers box braiding, within 200m of Hostel C, price range 300–800 KSh",
        "distanceMeters": 180.2
      }
    ],
    "queryUnderstanding": {
      "service": "box braids",
      "location": "Hostel C",
      "maxPrice": 500,
      "category": "SALON"
    }
  }
}
```

---

### Get Map Pins (Nearby Providers)
`GET /api/v1/discovery/map-pins?lat=0.0515&lng=37.6456&radiusKm=2.0&category=SALON`

**Response `200 OK`:**
```json
{
  "success": true,
  "message": "Map pins fetched",
  "data": [
    {
      "serviceId": "aa1c969d-210d-45be-91c6-1c88c75dfb3f",
      "providerId": "78e9069d-210d-45be-91c6-1c88c75dfb3f",
      "providerName": "John Kamau",
      "providerPhotoUrl": "https://...",
      "title": "Professional Braiding Services",
      "category": "SALON",
      "availability": "AVAILABLE",
      "averageRating": 4.8,
      "lat": 0.0515,
      "lng": 37.6456
    }
  ]
}
```

---

## Messaging Endpoints

### Get Conversation List
`GET /api/v1/conversations?page=0&size=20`

Returns a sorted list of conversations (newest activity first).

**Response `200 OK`:**
```json
{
  "success": true,
  "message": "OK",
  "data": {
    "content": [
      {
        "id": "bb4c969d-210d-45be-91c6-1c88c75dfb3f",
        "otherUserId": "89fa88c2-321a-45be-12c6-3d88c75dfb3f",
        "otherUserName": "Jane Wanjiku",
        "otherUserAvatar": "https://...",
        "serviceId": "aa1c969d-210d-45be-91c6-1c88c75dfb3f",
        "lastMessage": "I'm available tomorrow at 2pm",
        "lastMessageType": "TEXT",
        "lastMessageAt": "2026-02-14T14:30:00Z",
        "unreadCount": 2,
        "createdAt": "2026-02-14T10:00:00Z"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 1,
    "totalPages": 1
  }
}
```

---

### Get or Create Conversation
`POST /api/v1/conversations`

**Request:**
```json
{
  "otherUserId": "89fa88c2-321a-45be-12c6-3d88c75dfb3f",
  "serviceId": "aa1c969d-210d-45be-91c6-1c88c75dfb3f"
}
```

**Response `200 OK`:**
```json
{
  "success": true,
  "message": "OK",
  "data": {
    "id": "bb4c969d-210d-45be-91c6-1c88c75dfb3f",
    "otherUserId": "89fa88c2-321a-45be-12c6-3d88c75dfb3f",
    "otherUserName": "Jane Wanjiku",
    "otherUserAvatar": "https://...",
    "serviceId": "aa1c969d-210d-45be-91c6-1c88c75dfb3f",
    "lastMessage": null,
    "lastMessageType": null,
    "lastMessageAt": null,
    "unreadCount": 0,
    "createdAt": "2026-02-14T10:00:00Z"
  }
}
```

---

### Get Message History
`GET /api/v1/conversations/{conversationId}/messages?page=0&size=50`

**Response `200 OK`:**
```json
{
  "success": true,
  "message": "OK",
  "data": {
    "content": [
      {
        "id": "cc5c969d-210d-45be-91c6-1c88c75dfb3f",
        "conversationId": "bb4c969d-210d-45be-91c6-1c88c75dfb3f",
        "senderId": "78e9069d-210d-45be-91c6-1c88c75dfb3f",
        "type": "TEXT",
        "content": "I'm available tomorrow at 2pm",
        "mediaUrl": null,
        "thumbnailUrl": null,
        "metadata": null,
        "timestamp": "2026-02-14T14:30:00Z",
        "deliveredAt": "2026-02-14T14:30:05Z",
        "readAt": null
      }
    ],
    "page": 0,
    "size": 50,
    "totalElements": 1,
    "totalPages": 1
  }
}
```

---

### Mark Conversation as Read
`PUT /api/v1/conversations/{conversationId}/read`

**Response `200 OK`:**
```json
{
  "success": true,
  "message": "Conversation marked as read"
}
```

---

### Delete Message for Me
`DELETE /api/v1/conversations/messages/{messageId}/me`

Removes a message from the current user's message history.

**Response `200 OK`:**
```json
{
  "success": true,
  "message": "Message deleted for me successfully"
}
```

---

### Delete Message for Everyone
`DELETE /api/v1/conversations/messages/{messageId}/everyone`

Deletes a message for all participants in the conversation, replacing its content with a deletion placeholder.

**Response `200 OK`:**
```json
{
  "success": true,
  "message": "Message deleted for everyone successfully"
}
```

---

## Real-Time Chat — WebSocket (STOMP Protocol)

Connect to: `ws://10.0.2.2:8080/ws` (debug) / `wss://api.hustlehub.app/ws` (release)

Include Firebase ID token as a handshake query parameter or STOMP connect header:
```
/ws?token=<firebase_id_token>
```

### Send a Message
**Destination:** `/app/chat.send`

**Stomp frame body:**
```json
{
  "conversationId": "bb4c969d-210d-45be-91c6-1c88c75dfb3f",
  "type": "TEXT",
  "content": "I'm available tomorrow at 2pm",
  "mediaUrl": null,
  "thumbnailUrl": null,
  "metadata": null
}
```
*Message Types:* `TEXT`, `VOICE`, `IMAGE`, `LOCATION`, `SERVICE_CARD`

---

### Receive Messages
**Subscribe destination:** `/topic/conversation/{conversationId}`

**Payload received:** Returns a single `MessageResponse` JSON structure:
```json
{
  "id": "cc5c969d-210d-45be-91c6-1c88c75dfb3f",
  "conversationId": "bb4c969d-210d-45be-91c6-1c88c75dfb3f",
  "senderId": "78e9069d-210d-45be-91c6-1c88c75dfb3f",
  "type": "TEXT",
  "content": "I'm available tomorrow at 2pm",
  "mediaUrl": null,
  "thumbnailUrl": null,
  "metadata": null,
  "timestamp": "2026-02-14T14:30:00Z",
  "deliveredAt": "2026-02-14T14:30:05Z",
  "readAt": null
}
```

---

### Typing Indicators
**Publish destination:** `/app/chat.typing`
```json
{
  "conversationId": "bb4c969d-210d-45be-91c6-1c88c75dfb3f",
  "senderId": "78e9069d-210d-45be-91c6-1c88c75dfb3f",
  "isTyping": true
}
```

**Subscribe destination:** `/topic/conversation/{conversationId}/typing`

**Payload received:** Returns the typing indicator JSON structure above.

---

### Presence (Online/Offline)
**Subscribe destination:** `/topic/user/{userId}/presence`

**Payload received:** Returns `OnlineStatusRequest` format.

---

## Reviews Endpoints

### Submit Review
`POST /api/v1/reviews`

**Request:**
```json
{
  "serviceId": "aa1c969d-210d-45be-91c6-1c88c75dfb3f",
  "rating": 5,
  "comment": "Amazing braids! Very professional.",
  "isAnonymous": false
}
```

**Response `200 OK`:** Submit confirmation wrapped in `ApiResponse`.

---

### Get Reviews for a Service
`GET /api/v1/services/{serviceId}/reviews?page=0&size=10`

**Response `200 OK`:** Paginated reviews list formatted as `PageResponse<ReviewResponse>` wrapped in `ApiResponse`.

---

### Report a Review
`POST /api/v1/reviews/{reviewId}/report`

**Request:**
```json
{ 
  "reason": "Inappropriate content" 
}
```

**Response `200 OK`:** Report confirmation wrapped in `ApiResponse`.

---

## Media Upload Endpoints

### Upload Image
`POST /api/v1/media/upload`

**Content-Type:** `multipart/form-data`

**Form fields:**
- `file` — the image file (JPEG, PNG)
- `type` — `PROFILE_PHOTO` / `PORTFOLIO` / `CHAT_IMAGE`
- `entityId` — service UUID or conversation UUID (optional)

**Response `200 OK`:**
```json
{
  "success": true,
  "message": "Image uploaded successfully",
  "data": {
    "mediaId": "media_abc123",
    "url": "https://api.hustlehub.app/media/services/xyz789/img1.jpg",
    "thumbnailUrl": "https://api.hustlehub.app/media/services/xyz789/img1_thumb.jpg",
    "type": "PORTFOLIO"
  }
}
```

---

### Upload Voice Note
`POST /api/v1/media/upload/voice`

**Content-Type:** `multipart/form-data`

**Form fields:**
- `file` — audio file (AAC/M4A)
- `conversationId` — conversation UUID this voice note belongs to

**Response `200 OK`:**
```json
{
  "success": true,
  "message": "Voice note uploaded successfully",
  "data": {
    "mediaId": "media_voice_123",
    "url": "https://api.hustlehub.app/media/voice/abc123.m4a",
    "durationSeconds": 12,
    "type": "VOICE_NOTE"
  }
}
```

---

## Notifications Endpoints

### Get Notification History
`GET /api/v1/notifications?page=0&size=20`

**Response `200 OK`:** Paginated history list formatted as `PageResponse<NotificationResponse>` wrapped in `ApiResponse`.

---

### Mark Notification as Read
`PUT /api/v1/notifications/{notificationId}/read`

**Response `200 OK`:** Confirmation wrapped in `ApiResponse`.

---

### Mark All as Read
`PUT /api/v1/notifications/read-all`

**Response `200 OK`:** Confirmation wrapped in `ApiResponse`.
