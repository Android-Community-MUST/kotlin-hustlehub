# 🚀 Quick Setup Guide

This guide will get you up and running with HustleHub in under 10 minutes.

## Prerequisites

- Android Studio Ladybug (or latest stable)
- JDK 17+
- Git
- Firebase account
- Google Cloud account

## 1. Clone the Repository

```bash
git clone git@github.com:Android-Community-MUST/kotlin-hustlehub.git
cd kotlin-hustlehub
```

## 2. Firebase Setup

### Create Firebase Project

1. Go to [Firebase Console](https://console.firebase.google.com)
2. Click "Add project"
3. Name it "HustleHub" (or your preference)
4. Disable Google Analytics (optional for development)

### Add Android App

1. Click "Add app" → Android icon
2. **Package name**: `com.hustlehub.app` (must match exactly)
3. Download `google-services.json`
4. Place it in `app/` directory

### Enable Firebase Services

In Firebase Console, enable:

- **Authentication**
  - Email/Password provider
  - Google Sign-In provider
  
- **Firestore Database**
  - Start in test mode (for development)
  - Location: Choose closest to Kenya (e.g., `eur3`)

- **Realtime Database**
  - Start in test mode
  
- **Storage**
  - Start in test mode
  
- **Cloud Messaging (FCM)**
  - Automatically enabled

## 3. API Keys Configuration

### Quick Setup

1. **Copy the template**
   ```bash
   cp keys.properties.template keys.properties
   ```

2. **Add your API keys** to `keys.properties`:
   ```properties
   MAPS_API_KEY=AIzaSy...your_actual_maps_key
   GEMINI_API_KEY=AIzaSy...your_actual_gemini_key
   ```

### Get Your API Keys

**Google Maps API**
- Go to [Google Cloud Console](https://console.cloud.google.com)
- Enable "Maps SDK for Android"
- Create API Key → Restrict to Android apps
- Package name: `com.hustlehub.app`

**Gemini API**
- Go to [Google AI Studio](https://makersuite.google.com/app/apikey)
- Create API key

> ⚠️ **Security**: `keys.properties` is gitignored. Never commit API keys to version control!

## 4. Build the Project

### Sync Gradle

```bash
./gradlew build
```

Or in Android Studio: **File → Sync Project with Gradle Files**

### Run the App

```bash
# Debug build
./gradlew installDebug

# Or use Android Studio Run button (Shift+F10)
```

## 5. Verify Setup

### Test Authentication

1. Launch app
2. Sign up with test email: `test@must.ac.ke`
3. Check Firebase Console → Authentication → Users

### Test Firestore

1. Create a service listing
2. Check Firebase Console → Firestore → `services` collection

### Test Maps

1. Navigate to Map tab
2. Verify map loads correctly

## Common Issues

### Build Fails

```bash
# Clean and rebuild
./gradlew clean build
```

### Google Services Error

- Verify `google-services.json` is in `app/` directory
- Check package name matches exactly: `com.hustlehub.app`

### Maps Not Loading

- Verify `MAPS_API_KEY` in `keys.properties`
- Check API key restrictions in Google Cloud Console
- Enable "Maps SDK for Android" in Google Cloud

### Firebase Connection Failed

- Check internet connection
- Verify Firebase project configuration
- Check `google-services.json` is valid

## Next Steps

- Read [ARCHITECTURE.md](ARCHITECTURE.md) to understand the codebase
- Check [API.md](API.md) for API integration details
- See [CONTRIBUTING.md](../CONTRIBUTING.md) for development workflow
- Review [TROUBLESHOOTING.md](TROUBLESHOOTING.md) for common issues

## Quick Commands

```bash
# Run unit tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest

# Generate APK
./gradlew assembleDebug

# Check code style
./gradlew ktlintCheck

# Format code
./gradlew ktlintFormat
```

## Development Environment

### Recommended Android Studio Plugins

- Kotlin
- Jetpack Compose Preview
- Firebase Tools
- GitToolBox

### Code Style

The project uses Kotlin coding conventions. Configure Android Studio:

**Settings → Editor → Code Style → Kotlin → Set from → Kotlin style guide**

---

**Need help?** Check [TROUBLESHOOTING.md](TROUBLESHOOTING.md) or open an issue.
