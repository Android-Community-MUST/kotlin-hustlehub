# Performance & Optimization Guide

This document outlines the performance architecture, memory management, database indexing, and network optimizations implemented in **HustleHub Android**. All developers contributing to the app should follow these practices.

---

## 🎯 Target Device Benchmark & KPIs

HustleHub is optimized for entry-level to mid-range devices commonly used by students (e.g. **2 GB RAM, Snapdragon 665 / Helio P60, Android 8.0+ / Min SDK 24**).

### Target Key Performance Indicators (KPIs)

| Metric | Target Goal | Optimization Strategy |
|--------|-------------|-----------------------|
| **Cold Startup Time** | `< 2.0 seconds` | ART Baseline Profiles + deferred non-critical init |
| **Feed Scrolling Frame Rate** | `60 fps` (smooth, zero stutter) | Stable Compose lambdas + Lazy Grid keys + Coil thumbnail size hints |
| **Peak Heap Memory** | `< 150 MB` | Coil 25% heap cap + explicit `onCleared()` ViewModel job cancellation |
| **Memory Leaks** | `0 leaks` | LeakCanary detection + `NonCancellable` coroutine cleanup |
| **Network Payload Size** | `< 30% original size` | Gzip request compression + OkHttp response caching |

---

## 1. Network & Data Layer Optimizations

### Gzip Request Compression (`GzipRequestInterceptor`)
- **Location**: [`must.kdroiders.hustlehub.core.api.GzipRequestInterceptor`](file:///home/korryr/StudioProjects/kotlin-hustlehub/app/src/main/java/must/kdroiders/hustlehub/core/api/GzipRequestInterceptor.kt)
- **Mechanism**: Intercepts outgoing Retrofit POST/PUT requests with bodies and compresses them using Gzip before sending.
- **Benefits**: Drastically reduces upload bandwidth over campus Wi-Fi and mobile data.
- **Backend Interoperability**: Spring Boot backend automatically decompresses payloads with `Content-Encoding: gzip`.

### Response Caching
- **Location**: Configured in [`NetworkModule.kt`](file:///home/korryr/StudioProjects/kotlin-hustlehub/app/src/main/java/must/kdroiders/hustlehub/di/NetworkModule.kt)
- **Capacity**: 10 MB disk cache at `context.cacheDir/okhttp`.
- **Behavior**: Caches GET responses respecting HTTP cache headers to reduce redundant API round-trips when offline or revisiting screens.

---

## 2. Image Loading & Media Management (Coil & Media3)

### Custom `ImageLoader` Configuration
- **Location**: [`CoilModule.kt`](file:///home/korryr/StudioProjects/kotlin-hustlehub/app/src/main/java/must/kdroiders/hustlehub/di/CoilModule.kt) registered via `ImageLoaderFactory` in [`HustleHubApp.kt`](file:///home/korryr/StudioProjects/kotlin-hustlehub/app/src/main/java/must/kdroiders/hustlehub/appHilt/HustleHubApp.kt).
- **Memory Cache**: 25% of available JVM heap (`maxSizePercent(0.25)`).
- **Disk Cache**: 100 MB at `context.cacheDir/coil`.
- **Transitions**: 300 ms crossfade enabled for smooth image rendering.
- **OkHttpClient Reuse**: Shares the authenticated single `OkHttpClient` instance.

### Image Sizing & Thumbnail Rules
- **Rule**: Never load unconstrained full-resolution images into list cards.
- **Implementation**: Pass explicit resolution hints to Coil `ImageRequest` (e.g. `Size(360, 200)` in [`ServiceCard.kt`](file:///home/korryr/StudioProjects/kotlin-hustlehub/app/src/main/java/must/kdroiders/hustlehub/ui/features/home/presentation/components/ServiceCard.kt)).

### Media Pre-Processing Before Upload
- Compress images client-side before POSTing to `/api/v1/media/upload` using `ImageUtils.kt` (max 1024px width/height, 80% JPEG quality).
- For voice notes, record in AAC format capped at 2 minutes max to constrain storage and memory.
- Release Media3 `ExoPlayer` instances immediately when screens exit or ViewModels are cleared.

---

## 3. Local Storage & Database Performance (Room)

- **Indexed Foreign Keys**: All Room database tables (messages, services, reviews) must index frequently queried foreign keys (`conversationId`, `serviceId`, `createdAt`) to ensure $O(\log N)$ query speed.
- **Pagination**: Use LIMIT/OFFSET pagination or Room Paging sources when reading large tables to keep UI allocations minimal.
- **Transaction Safety**: Wrap multi-table operations (e.g., storing a conversation and its initial message) in `@Transaction` to maintain atomic DB performance.

---

## 4. Memory Leak Prevention & Lifecycle Teardown

### LeakCanary Integration
- Added as `debugImplementation(libs.leakcanary.android)` in [`app/build.gradle.kts`](file:///home/korryr/StudioProjects/kotlin-hustlehub/app/build.gradle.kts).
- Automatically tracks retained activities, fragments, viewmodels, and composables in debug builds.

### ViewModel Teardown Best Practices
- Always cancel active jobs in `onCleared()` (e.g. debounced searches, active WebSocket subscriptions).
- When performing async cleanup on destruction (e.g. WebSocket teardown in `ChatDetailViewModel`), wrap execution in `NonCancellable` so jobs complete even after `viewModelScope` is cancelled.

---

## 5. Jetpack Compose Rendering & Recomposition Rules

1. **Lazy List / Grid Keys**: Always specify unique keys for list items (`key = { item.id }`) to prevent item recreation during scrolling.
2. **Lambda Stabilization**: Wrap callback lambdas passed to child composables in `remember {}` to avoid invalidating child composables on parent recomposition.
3. **`derivedStateOf`**: Use `derivedStateOf` when calculated states depend on frequent scroll or input updates (e.g. `shouldLoadMore` derived from grid scroll position).
4. **State Calculation Caching**: Wrap computed display strings in `remember(key) { ... }` so string parsing/formatting isn't executed on every frame.

---

## 6. Startup Optimization & Baseline Profiles

- **Baseline Profile**: Pre-compiled rules defined in [`baseline-prof.txt`](file:///home/korryr/StudioProjects/kotlin-hustlehub/app/src/main/baseline-prof.txt) instruct ART to compile hot paths (Splash, Home, Detail, Chat) on install.
- **Generator**: Re-generate profiles using [`BaselineProfileGenerator.kt`](file:///home/korryr/StudioProjects/kotlin-hustlehub/app/src/androidTest/java/must/kdroiders/hustlehub/BaselineProfileGenerator.kt) on a connected device/emulator.
- **Deferred Initialization**: Keep `Application.onCreate()` lean. Non-critical SDKs and singletons must be lazily instantiated.

---

## 7. Release Build Optimization & R8 Shrinking

Release builds enable R8 minification and resource shrinking in [`app/build.gradle.kts`](file:///home/korryr/StudioProjects/kotlin-hustlehub/app/build.gradle.kts):

```kotlin
buildTypes {
    release {
        isMinifyEnabled = true
        isShrinkResources = true
        proguardFiles(
            getDefaultProguardFile("proguard-android-optimize.txt"),
            "proguard-rules.pro",
        )
    }
}
```

- **Keep Rules**: Explicit keep rules in [`app/proguard-rules.pro`](file:///home/korryr/StudioProjects/kotlin-hustlehub/app/proguard-rules.pro) preserve Retrofit DTOs, Room DAOs/Entities, Coil, Firebase, and serialization classes while stripping dead code.

---

## 🛠️ Diagnostic Commands for Developers

### Measure Cold Start Time
```bash
adb shell am force-stop must.kdroiders.hustlehub
adb shell am start -W must.kdroiders.hustlehub/.activities.MainActivity
```
*Look for `TotalTime:` in output — target < 2000 ms.*

### Run Unit Tests
```bash
./gradlew test
```

### Run Static Analysis & Formatting Checks
```bash
./gradlew ktlintCheck detekt
```
