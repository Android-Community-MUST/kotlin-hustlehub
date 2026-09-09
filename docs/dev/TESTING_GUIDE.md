# 🧪 HustleHub Testing Guide

This guide covers running unit tests, Compose UI integration tests with Firebase Emulator Suite, and end-to-end Maestro flows.

---

## 1. Firebase Emulator Suite

HustleHub uses the Firebase Emulator Suite for authentication testing during CI and local development.

### Configuration (`firebase.json`)

The emulator ports are configured as follows:
- **Authentication**: `localhost:9099`
- **Cloud Firestore**: `localhost:8085` (non-conflicting with Spring Boot on 8080)
- **Cloud Storage**: `localhost:9199`
- **Emulator UI**: `http://localhost:4000`

### Starting the Emulators

```bash
# Start emulators with persistent data or clean state
firebase emulators:start
```

To run tests automatically inside the emulator lifecycle:
```bash
firebase emulators:exec "./gradlew connectedAndroidTest"
```

---

## 2. Compose UI & Integration Tests

The project includes instrumented Compose UI tests covering the 5 critical user flows under `app/src/androidTest/java/must/kdroiders/hustlehub/`:

| Test Suite | Package | Covered Flow |
|------------|---------|--------------|
| `AuthFlowTest` | `ui.auth` | Sign Up → Email Verification → Profile Setup |
| `DiscoveryFlowTest` | `ui.home` | Browse Feed → Search Bar → Service Detail |
| `CreateServiceFlowTest` | `ui.service` | Create Service → Fill Details → Publish |
| `ChatFlowTest` | `ui.chat` | Open Conversation → Type Message → Send → Verify Bubble |
| `ReviewFlowTest` | `ui.service` | Select Star Rating → Write Comment → Submit |

### Running Tests

#### Run all instrumented tests
```bash
./gradlew connectedAndroidTest
```

#### Run a specific test suite
```bash
./gradlew connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=must.kdroiders.hustlehub.ui.auth.AuthFlowTest
```

### Physical Device vs Emulator Port Forwarding

- **Android Emulator**: Automatically accesses host services via `10.0.2.2`.
- **Physical USB Device**: `10.0.2.2` does not work. Route the emulator and backend ports to your connected device using `adb reverse`:

```bash
# Forward Firebase Auth emulator
adb reverse tcp:9099 tcp:9099

# Forward Spring Boot backend
adb reverse tcp:8080 tcp:8080

# Forward Firebase Storage emulator
adb reverse tcp:9199 tcp:9199
```

`FirebaseEmulatorHelper.getEmulatorHost()` automatically detects physical devices vs emulators and selects `127.0.0.1` or `10.0.2.2`.

---

## 3. Maestro E2E Flows

Maestro test definitions are stored in `.maestro/`:
- `.maestro/signup_and_create_service.yaml`
- `.maestro/customer_chat_and_review.yaml`

### Prerequisites

Install the Maestro CLI:
```bash
curl -FsSL "https://get.mobile.dev" | bash
```

### Running Maestro Flows

Ensure your device/emulator is connected (`adb devices`) and the debug build is installed:
```bash
./gradlew installDebug

# Run sign up and service creation flow
maestro test .maestro/signup_and_create_service.yaml

# Run customer chat and review flow
maestro test .maestro/customer_chat_and_review.yaml
```

---

## 4. Continuous Integration (CI)

In GitHub Actions, Compose UI and emulator tests can run headlessly using an Android Emulator matrix:

```yaml
- name: Start Firebase Emulators
  run: npx -y firebase-tools@latest emulators:start --only auth &

- name: Run Connected Tests
  uses: reactivecircus/android-emulator-runner@v2
  with:
    api-level: 34
    script: ./gradlew connectedAndroidTest
```
