# 🚀 HustleHub Google Play Production & Release Guide

This guide details the standard operating procedure (SOP) for versioning, signing, bundling, and releasing **HustleHub** on the Google Play Store. Any developer on the team can follow this workflow to prepare, test, and deploy releases.

---

## 📋 1. Release Lifecycle & Requirements

HustleHub follows Google Play's standard deployment lifecycle:

```
┌──────────────────────────┐      20 testers / 14 days     ┌──────────────────────────┐
│   Closed Testing Track   ├──────────────────────────────►│ Apply for Production     │
│   (Alpha / Beta testing) │                               │ (One-time policy check)  │
└────────────┬─────────────┘                               └────────────┬─────────────┘
             │                                                          │
             │ Subsequent updates                                       │ Approved by Google
             ▼                                                          ▼
┌─────────────────────────────────────────────────────────────────────────────────────┐
│                              Production Rollout Track                               │
│              (Staged: 10% → 20% → 50% → 100% or Direct 100% Rollout)                │
└─────────────────────────────────────────────────────────────────────────────────────┘
```

### Initial Production Access Requirements (Google Play Policy)
For personal developer accounts created after November 2023:
1. **Closed Testing Track**:
   - Maintain at least 20 opted-in testers active continuously for at least 14 days.
   - Gather feedback and iterate on reported bugs during the testing window.
2. **Production Access Application**:
   - Complete the Play Console questionnaire covering tester recruitment, feedback collected, and changes made.
   - Google typically reviews the application within 2 to 7 days.
3. **Subsequent App Updates**:
   - Once production access is granted to the account, subsequent version updates do **not** require the 14-day testing period again. They can be tested internally and promoted directly to production.

---

## 🔢 2. Versioning Procedure

Before creating any release build, bump the version in `app/build.gradle.kts`:

```kotlin
defaultConfig {
    // ...
    versionCode = 8          // Increment monotonically by +1 for every release
    versionName = "1.0.4"    // Semantic version: MAJOR.MINOR.PATCH
}
```

- **`versionCode`**: An integer that strictly increases with every build uploaded to Google Play.
- **`versionName`**: Human-readable version string shown to users in app details and Play Store listing.
- Update `docs/dev/CHANGELOG.md` with the release notes and fixes associated with the new version.

---

## 🔑 3. Release Signing & Keystore Configuration

HustleHub uses Google Play App Signing with upload keys configured via `keys.properties`.

### Environment Setup (`keys.properties`)

Ensure your local `keys.properties` file contains the release keystore details:

```properties
# Release Keystore Configuration
KEYSTORE_FILE=/path/to/your/upload-release.keystore
KEYSTORE_PASSWORD=your_keystore_password
KEY_ALIAS=your_upload_key_alias
KEY_PASSWORD=your_key_password

# API Configuration (Production URLs)
MAPS_API_KEY=AIzaSy...your_production_maps_key
BASE_URL=https://api.hustlehub.app/api/v1/
WS_BASE_URL=wss://api.hustlehub.app/ws
GOOGLE_WEB_CLIENT_ID=your_google_web_client_id.apps.googleusercontent.com
```

> ⚠️ **Never commit `keys.properties` or keystore files to git.** Keep secure offline backups of your keystore and passwords.

---

## 📦 4. Building the Production Release Bundle

Google Play requires Android App Bundles (`.aab`) for store releases.

### Step 1: Run Quality Checks
Ensure all tests and static analysis pass before compiling the release artifact:
```bash
./gradlew ktlintCheck detekt test
```

### Step 2: Build the Signed Bundle
```bash
./gradlew clean bundleRelease
```

The compiled and signed bundle will be generated at:
```
app/build/outputs/bundle/release/app-release.aab
```

---

## 🚀 5. Google Play Console Deployment Workflow

### Method A: Promoting from Closed Testing (Recommended for major releases)
1. Navigate to **Google Play Console** → Select **HustleHub**.
2. Go to **Testing** → **Closed testing** (or **Releases overview**).
3. Select the release you wish to promote.
4. Click **Promote release** → **Production**.
5. Review the release notes and target rollout percentage.
6. Click **Save** → **Review release** → **Start rollout to Production**.

### Method B: Creating a Direct Production Release (For minor patches/hotfixes)
1. Go to **Release** → **Production** in Google Play Console.
2. Click **Create new release** in the top right corner.
3. Upload `app/build/outputs/bundle/release/app-release.aab`.
4. Enter user-facing release notes under **Release notes**.
5. Save and proceed to **Review release**.
6. Choose a rollout strategy and confirm rollout.

---

## 🛡️ 6. Rollout Strategies & Safety

### Staged Rollout (Best Practice)
For substantial releases, use a staged rollout to mitigate risk:
- **Day 1**: 10% – 20% rollout. Monitor Crashlytics and Play Console Vitals for unexpected crashes or device-specific regressions.
- **Day 2**: 50% rollout if crash rates remain within acceptable limits.
- **Day 3+**: 100% full rollout.
- If a critical bug is discovered, the rollout can be halted immediately in the console while a fix is prepared.

---

## 📊 7. Post-Launch Monitoring & Health Checks

- **Google Play Android Vitals**:
  - **Crash Rate**: Must remain below Google's bad behavior threshold (< 1.09%).
  - **User-Perceived ANR Rate**: Must remain below Google's threshold (< 0.47%).
- **Firebase Crashlytics**:
  - Monitor real-time crash reports and filter by `screen` and `backend_version` custom keys.
- **Backend Service Compatibility**:
  - Verify that the production backend API (`BASE_URL`) and WebSocket gateway (`WS_BASE_URL`) handle incoming traffic, authentication tokens, and real-time STOMP connections properly.
