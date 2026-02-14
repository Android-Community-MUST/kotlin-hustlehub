# 🔧 Troubleshooting

Common issues and solutions for HustleHub development.

## Build Issues

### Gradle Sync Failed

**Problem:** Gradle sync fails with dependency resolution errors

**Solution:**
```bash
# Clear Gradle cache
./gradlew clean
rm -rf ~/.gradle/caches/

# Invalidate Android Studio caches
# File → Invalidate Caches → Invalidate and Restart

# Re-sync
./gradlew build
```

### Duplicate Class Error

**Problem:** `Duplicate class found in modules`

**Solution:**
```kotlin
// Check build.gradle for duplicate dependencies
dependencies {
    // Remove duplicate entries
    // Check transitive dependencies
    implementation("com.example:library:1.0") {
        exclude(group = "com.duplicate", module = "module")
    }
}
```

### Out of Memory Error

**Problem:** `OutOfMemoryError: Java heap space`

**Solution:**
```properties
# gradle.properties
org.gradle.jvmargs=-Xmx4096m -XX:MaxPermSize=1024m
```

## Firebase Issues

### google-services.json Not Found

**Problem:** `File google-services.json is missing`

**Solution:**
1. Download `google-services.json` from Firebase Console
2. Place in `app/` directory (not root)
3. Sync Gradle

### Authentication Failed

**Problem:** `FirebaseAuthException: The email address is badly formatted`

**Solution:**
```kotlin
// Validate email before sending to Firebase
fun isValidEmail(email: String): Boolean {
    return email.endsWith("@must.ac.ke") && 
           android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
}
```

### Firestore Permission Denied

**Problem:** `PERMISSION_DENIED: Missing or insufficient permissions`

**Solution:**
```javascript
// Update Firestore Rules (for development)
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /{document=**} {
      allow read, write: if request.auth != null;
    }
  }
}
```

**⚠️ Warning:** Use proper security rules in production!

### FCM Token Not Generated

**Problem:** FCM token is null

**Solution:**
```kotlin
// Check Google Play Services availability
fun checkPlayServices(context: Context): Boolean {
    val googleApiAvailability = GoogleApiAvailability.getInstance()
    val resultCode = googleApiAvailability.isGooglePlayServicesAvailable(context)
    return resultCode == ConnectionResult.SUCCESS
}

// Request token manually
FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
    if (task.isSuccessful) {
        val token = task.result
        Log.d("FCM", "Token: $token")
    }
}
```

## Google Maps Issues

### Map Not Displaying

**Problem:** Map shows blank/gray screen

**Solutions:**

1. **Check API Key**
```properties
# local.properties
MAPS_API_KEY=AIzaSy...your_actual_key
```

2. **Enable Maps SDK**
- Go to Google Cloud Console
- Enable "Maps SDK for Android"

3. **Check Restrictions**
- API key should be restricted to Android apps
- Package name: `com.hustlehub.app`
- SHA-1 fingerprint added

4. **Get SHA-1 Fingerprint**
```bash
# Debug keystore
keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android

# Release keystore
keytool -list -v -keystore /path/to/release.keystore -alias your_alias
```

### Markers Not Showing

**Problem:** Map loads but markers don't appear

**Solution:**
```kotlin
// Ensure camera is positioned correctly
val cameraPosition = CameraPosition.fromLatLngZoom(
    LatLng(0.0515, 37.6456), // Meru University
    15f // Zoom level
)

// Add markers after map is loaded
LaunchedEffect(services) {
    services.forEach { service ->
        // Add marker
    }
}
```

## Gemini API Issues

### API Key Invalid

**Problem:** `401 Unauthorized` or `API key not valid`

**Solution:**
1. Verify API key in `local.properties`
2. Check API key restrictions in Google Cloud Console
3. Ensure Gemini API is enabled

### Rate Limit Exceeded

**Problem:** `429 Too Many Requests`

**Solution:**
```kotlin
// Implement exponential backoff
suspend fun <T> retryWithBackoff(
    maxRetries: Int = 3,
    initialDelay: Long = 1000,
    block: suspend () -> T
): T {
    var currentDelay = initialDelay
    repeat(maxRetries) { attempt ->
        try {
            return block()
        } catch (e: HttpException) {
            if (e.code() == 429 && attempt < maxRetries - 1) {
                delay(currentDelay)
                currentDelay *= 2
            } else throw e
        }
    }
    throw Exception("Max retries exceeded")
}
```

### Slow Response Times

**Problem:** Gemini API takes too long to respond

**Solution:**
```kotlin
// Implement timeout
withTimeout(5000) { // 5 seconds
    geminiApi.matchServices(query, services)
}

// Show loading state
_uiState.value = UiState.Loading
```

## Runtime Issues

### App Crashes on Startup

**Problem:** App crashes immediately after launch

**Solutions:**

1. **Check Logcat**
```bash
# Filter by app package
adb logcat | grep "com.hustlehub.app"

# Filter by error level
adb logcat *:E
```

2. **Common Causes**
- Missing Firebase initialization
- Hilt setup incorrect
- Compose version mismatch

### Network Timeout

**Problem:** `SocketTimeoutException`

**Solution:**
```kotlin
// Increase timeout in OkHttp
val client = OkHttpClient.Builder()
    .connectTimeout(30, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .writeTimeout(30, TimeUnit.SECONDS)
    .build()
```

### Image Upload Fails

**Problem:** Images fail to upload to Firebase Storage

**Solution:**
```kotlin
// Compress image before upload
fun compressImage(uri: Uri, context: Context): ByteArray {
    val bitmap = MediaStore.Images.Media.getBitmap(
        context.contentResolver, uri
    )
    val outputStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
    return outputStream.toByteArray()
}

// Check file size
val maxSize = 5 * 1024 * 1024 // 5MB
if (fileSize > maxSize) {
    // Compress or reject
}
```

## Testing Issues

### Emulator Not Starting

**Problem:** Android emulator fails to start

**Solution:**
```bash
# Check emulator list
emulator -list-avds

# Start with specific AVD
emulator -avd Pixel_5_API_34

# Enable hardware acceleration
# Tools → AVD Manager → Edit → Advanced → Graphics: Hardware
```

### Tests Failing

**Problem:** Unit tests fail unexpectedly

**Solution:**
```kotlin
// Use test dispatcher for coroutines
@Before
fun setup() {
    Dispatchers.setMain(StandardTestDispatcher())
}

@After
fun tearDown() {
    Dispatchers.resetMain()
}

// Use runTest for suspend functions
@Test
fun testSuspendFunction() = runTest {
    val result = repository.getData()
    assertEquals(expected, result)
}
```

## Performance Issues

### Slow UI Rendering

**Problem:** UI lags or stutters

**Solutions:**

1. **Avoid Recomposition**
```kotlin
// Use remember and derivedStateOf
val filteredList = remember(items, query) {
    items.filter { it.contains(query) }
}
```

2. **Use LazyColumn Properly**
```kotlin
LazyColumn {
    items(
        items = services,
        key = { it.id } // Provide stable keys
    ) { service ->
        ServiceCard(service)
    }
}
```

3. **Profile with Layout Inspector**
- Tools → Layout Inspector
- Check for overdraw
- Optimize nested layouts

### Memory Leaks

**Problem:** App memory usage keeps increasing

**Solution:**
```kotlin
// Cancel coroutines in ViewModel
override fun onCleared() {
    viewModelScope.cancel()
    super.onCleared()
}

// Use weak references for listeners
private val listener = WeakReference(myListener)
```

## Development Tools

### Useful ADB Commands

```bash
# Clear app data
adb shell pm clear com.hustlehub.app

# Uninstall app
adb uninstall com.hustlehub.app

# View shared preferences
adb shell run-as com.hustlehub.app cat /data/data/com.hustlehub.app/shared_prefs/prefs.xml

# Take screenshot
adb shell screencap -p /sdcard/screenshot.png
adb pull /sdcard/screenshot.png

# Record screen
adb shell screenrecord /sdcard/demo.mp4
```

### Debugging Tips

```kotlin
// Enable verbose logging
if (BuildConfig.DEBUG) {
    Timber.plant(Timber.DebugTree())
}

// Log network requests
val loggingInterceptor = HttpLoggingInterceptor().apply {
    level = HttpLoggingInterceptor.Level.BODY
}

// Use breakpoints effectively
// Set conditional breakpoints: Right-click breakpoint → Condition
```

## Getting Help

If you can't find a solution here:

1. **Check Logs**
   - Android Studio Logcat
   - Firebase Console → Crashlytics

2. **Search Issues**
   - [GitHub Issues](https://github.com/Android-Community-MUST/kotlin-hustlehub/issues)
   - Stack Overflow with tag `hustlehub`

3. **Ask for Help**
   - Open a new issue with:
     - Error message
     - Steps to reproduce
     - Environment details
     - Relevant code snippets

4. **Documentation**
   - [Firebase Docs](https://firebase.google.com/docs)
   - [Jetpack Compose Docs](https://developer.android.com/jetpack/compose)
   - [Kotlin Docs](https://kotlinlang.org/docs)

---

**Still stuck?** Open an issue with the `help-wanted` label!
