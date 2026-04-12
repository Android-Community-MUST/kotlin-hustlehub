# 🚀 Quick Setup Guide

Get HustleHub up and running in under 15 minutes.

## Prerequisites

- Android Studio Ladybug (or latest stable)
- JDK 17+
- Git
- Firebase account (for Auth + FCM only)
- Google Cloud account (Maps API key)
- **HustleHub Spring Boot backend** running locally or accessible via network

---

## 1. Clone the Android App

```bash
git clone git@github.com:Android-Community-MUST/kotlin-hustlehub.git
cd kotlin-hustlehub
```

---

## 2. Clone & Run the Spring Boot Backend

The Android app has **no direct database access** — all data goes through the backend.

```bash
# Clone the backend repo (separate repository)
git clone git@github.com:Android-Community-MUST/hustlehub-backend.git
cd hustlehub-backend

# Run locally (requires JDK 17+)
./gradlew bootRun
```

The backend starts on `http://localhost:8080`. The Android emulator accesses it via `http://10.0.2.2:8080`.

> **Physical device on the same WiFi**: Set `BASE_URL` to your machine's LAN IP (e.g. `http://192.168.1.5:8080/api/v1/`) in `keys.properties`.

---

## 3. Firebase Setup (Auth + FCM Only)

HustleHub uses Firebase **only** for:
- Email/password and Google Sign-In (Firebase Auth)
- Push notifications (Firebase Cloud Messaging)

No Firestore, no Realtime Database, no Firebase Storage.

### Create Firebase Project

1. Go to [Firebase Console](https://console.firebase.google.com)
2. Click "Add project" → name it "HustleHub"
3. Disable Google Analytics (optional for development)

### Add Android App

1. Click "Add app" → Android icon
2. **Package name**: `must.kdroiders.hustlehub` (must match exactly)
3. Download `google-services.json`
4. Place it in the `app/` directory

### Enable Firebase Services

In Firebase Console, enable **only**:

- **Authentication**
  - Email/Password provider
  - Google Sign-In provider (restrict to `@must.ac.ke` domain in the backend)

- **Cloud Messaging (FCM)** — automatically enabled

> ❌ Do NOT enable Firestore, Realtime Database, or Firebase Storage.

---

## 4. API Keys Configuration

Copy the template and fill in your keys:

```bash
cp keys.properties.template keys.properties
```

Edit `keys.properties`:

```properties
# Google Maps API Key
# Get from: https://console.cloud.google.com → Maps SDK for Android
MAPS_API_KEY=AIzaSy...your_actual_maps_key

# Spring Boot Backend Base URL
# debug: emulator → localhost
BASE_URL=http://10.0.2.2:8080/api/v1/

# WebSocket Base URL
WS_BASE_URL=ws://10.0.2.2:8080/ws

# Gemini API Key (used by backend, but keep here for client-side config if needed)
GEMINI_API_KEY=AIzaSy...your_actual_gemini_key
```

> ⚠️ `keys.properties` is gitignored. **Never commit it.**

### Get Your API Keys

**Google Maps API**
1. Go to [Google Cloud Console](https://console.cloud.google.com)
2. Enable "Maps SDK for Android"
3. Create API Key → Restrict to Android apps
4. Package name: `must.kdroiders.hustlehub`

---

## 5. Build the Project

```bash
# Sync and build
./gradlew build

# Or in Android Studio: File → Sync Project with Gradle Files
```

---

## 6. Run the App

```bash
# Install debug build on emulator/device
./gradlew installDebug

# Or use Android Studio Run button (Shift+F10)
```

Make sure the Spring Boot backend is running before launching the app.

---

## 7. Verify Setup

### Test Authentication
1. Launch app
2. Sign up with `test@must.ac.ke`
3. Check Firebase Console → Authentication → Users (should appear)
4. Check backend logs — should receive `POST /api/v1/auth/register`

### Test API Connectivity
1. Open Logcat in Android Studio
2. Filter by `OkHttp`
3. Navigate the app — you should see REST requests to `10.0.2.2:8080`

### Test Maps
1. Navigate to Map tab
2. Verify the Google Map renders correctly

### Test Real-Time Chat
1. Create two test accounts
2. Start a conversation from one account
3. Send a message — verify it appears in the other account in real time

---

## Build Variants

| Variant | API Base URL | WebSocket | Logging |
|---------|-------------|-----------|---------|
| `debug` | `http://10.0.2.2:8080/api/v1/` | `ws://10.0.2.2:8080/ws` | Verbose |
| `release` | `https://api.hustlehub.app/api/v1/` | `wss://api.hustlehub.app/ws` | Off |

---

## Common Issues

### "Connection refused" on API calls
- Ensure the Spring Boot backend is running on port 8080
- Use `10.0.2.2` (not `localhost`) in the emulator
- For physical device: use your machine's LAN IP

### "google-services.json not found"
- Make sure it's in the `app/` directory (not the root)
- Sync Gradle

### "Maps not loading"
- Verify `MAPS_API_KEY` in `keys.properties`
- Enable "Maps SDK for Android" in Google Cloud Console
- Check SHA-1 fingerprint is added to the API key restrictions

### "401 Unauthorized" on API calls
- Firebase ID token may have expired — the `TokenAuthenticator` will auto-refresh
- Ensure Firebase Auth is initialized before making API calls
- Check that `google-services.json` matches your Firebase project

### WebSocket not connecting
- Backend must have the WebSocket endpoint active
- Check `WS_BASE_URL` in `keys.properties`
- Firebase token is passed as a query param — check backend WebSocket auth filter

---

## Quick Commands

```bash
# Run unit tests
./gradlew test

# Run instrumented tests (requires emulator/device)
./gradlew connectedAndroidTest

# Generate debug APK
./gradlew assembleDebug

# Check code style
./gradlew ktlintCheck

# Auto-format code
./gradlew ktlintFormat

# Run static analysis
./gradlew detekt

# Full quality check (run before every PR)
./gradlew ktlintCheck detekt test
```

---

## Development Environment

### Recommended Android Studio Plugins
- Kotlin
- Jetpack Compose Preview
- Firebase Tools
- GitToolBox
- Rainbow Brackets

### Code Style
The project uses Kotlin coding conventions with ktlint.

**Settings → Editor → Code Style → Kotlin → Set from → Kotlin style guide**

---

## Useful ADB Commands

```bash
# Clear app data (fresh start)
adb shell pm clear must.kdroiders.hustlehub

# View DataStore preferences
adb shell run-as must.kdroiders.hustlehub \
  cat /data/data/must.kdroiders.hustlehub/files/datastore/hustlehub_preferences.preferences_pb

# Take a screenshot
adb shell screencap -p /sdcard/screenshot.png && adb pull /sdcard/screenshot.png

# Record screen
adb shell screenrecord /sdcard/demo.mp4
```

---

**Need help?** Check [TROUBLESHOOTING.md](TROUBLESHOOTING.md) or open a GitHub issue.
