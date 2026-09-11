# 🚀 HustleHub Beta Distribution & Feedback Operations Guide

This guide details the end-to-end process for building, distributing, and collecting structured feedback from campus testers for HustleHub using **Firebase App Distribution**.

---

## 1. Build Variants Configuration

HustleHub defines three primary build variants in `app/build.gradle.kts`:

| Variant | Purpose | Minification (R8) | Shrink Resources | Version Name Suffix | Signing |
|---|---|---|---|---|---|
| **`debug`** | Local development & unit tests | Disabled | Disabled | None | Android Debug Keystore |
| **`beta`** | Campus beta testing distribution | **Enabled** | **Enabled** | `-beta` (e.g. `1.0-beta`) | Automatic (Release if available, otherwise Debug) |
| **`release`** | Google Play Store production release | **Enabled** | **Enabled** | None (e.g. `1.0`) | Dedicated Release Keystore |

### Building the Beta APK Locally

To compile and package the minified, optimized beta APK:
```bash
./gradlew assembleBeta
```

The resulting signed APK will be located at:
```
app/build/outputs/apk/beta/app-beta.apk
```

---

## 2. Firebase App Distribution Setup

### Step 1: Firebase Console Configuration
1. Open the [Firebase Console](https://console.firebase.google.com/) and navigate to your HustleHub project.
2. Under **Release & Monitor**, click **App Distribution**.
3. Accept the Terms of Service if prompted.

### Step 2: Create Tester Groups
Navigate to the **Testers & Groups** tab and create two dedicated tester cohorts:

1. **`providers`** (10 students offering services on campus):
   - Add student email addresses (e.g. barbers, tech repair, tutors, graphic designers).
2. **`customers`** (10 students seeking services on campus):
   - Add student email addresses representing buyers looking to request services.
3. **`beta-testers`** (Unified group containing all 20 participants).

### Step 3: Distribution Methods

#### Method A: Manual Upload via Firebase Console
1. Run `./gradlew assembleBeta`.
2. Drag and drop `app/build/outputs/apk/beta/app-beta.apk` into the App Distribution dashboard.
3. Select the groups: `providers`, `customers`.
4. Enter release notes highlighting new features or areas needing focused testing.
5. Click **Distribute to X testers**.

#### Method B: Automated CI/CD Distribution (GitHub Actions)
Pushing to `main` (or clicking **Run workflow** in GitHub Actions under the **Beta Distribution** tab) automatically triggers `.github/workflows/beta_distribution.yml`.

Required GitHub Repository Secrets:
* `FIREBASE_APP_ID`: Your Android Mobile App ID from Firebase Settings.
* `FIREBASE_SERVICE_ACCOUNT`: Service account private key JSON with *Firebase App Distribution Admin* permissions.
* `RELEASE_KEYSTORE_BASE64` (Optional): Base64 string of release keystore for signing.

---

## 3. Structured Feedback Collection (Google Form Schema)

A standardized Google Form ensures feedback is quantifiable and categorized. Replicate the following structure:

### Section 1: Tester Identity & Context
1. **Your Email**: (Short text)
2. **Your Campus / Hostel**: (Short text)
3. **Which role did you test primarily?** (Multiple choice)
   - Service Provider (offering services)
   - Customer (looking to hire / book services)
   - Both
4. **Phone Model & Android Version**: (e.g., Tecno Spark 10, Android 13)

### Section 2: Core Flow Usability (Rating 1 to 5)
1. **Sign Up & Account Setup**: (1 = Confusing/Broken, 5 = Smooth & Fast)
2. **Browsing & Searching Services**: (1 = Hard to find items, 5 = Instant & clear)
3. **Creating & Publishing a Service (Providers)**: (1 = Frustrating, 5 = Seamless)
4. **In-App Chat & Communication**: (1 = Laggy/Messages delayed, 5 = Instant & responsive)
5. **Writing & Reading Reviews**: (1 = Difficult, 5 = Straightforward)

### Section 3: Performance & Reliability
1. **Did the app ever freeze, crash, or stop responding?** (Yes / No)
   - *If yes*: Describe what you were doing right before the crash.
2. **How was the app speed on campus Wi-Fi / mobile data?** (1 = Very slow, 5 = Lightning fast)

### Section 4: Qualitative Insights
1. **What was your favorite feature?** (Paragraph)
2. **What was the most frustrating part of using the app?** (Paragraph)
3. **What is ONE feature missing that you need to use this every day?** (Paragraph)
4. **Overall App Rating**: (⭐⭐⭐⭐⭐ scale from 1 to 5)

---

## 4. WhatsApp Group Coordination Protocol

Real-time chat encourages engagement and immediate bug surfacing that users might otherwise not report on a form.

### Rules of Engagement
* **Title**: `HustleHub Campus Beta Testers 🚀`
* **Description**: Official beta testing community for HustleHub. Report bugs, suggest features, and share screenshots directly.

### Daily Testing Prompts

| Day | Target Flow | Provider Action | Customer Action |
|---|---|---|---|
| **Day 1** | Onboarding & Profile | Sign up with student email, add skills, bio, and campus location. | Sign up, complete profile, and bookmark 3 services. |
| **Day 2** | Service Marketplace | Create 2 real services with photos, descriptions, and price tiers. | Search for services using search bar and category filters. |
| **Day 3** | Communication | Respond to inquiries within 10 minutes. | Initiate chat with 2 providers asking service questions. |
| **Day 4** | Transactions & Reviews | Confirm order mock completion. | Submit 5-star ratings and written reviews on completed services. |
| **Day 5** | Deep Dive & Stress Test | Test in low network areas, test dark/light theme switching. | Test push notifications on incoming messages. |

---

## 5. 30-Minute 1-on-1 Feedback Interview Protocol

Schedule video or in-person sessions with 2 providers and 2 customers.

### Agenda
* **00:00 - 05:00**: Welcome, rapport building, context.
* **05:00 - 15:00**: User screen shares while performing a live task:
  - *Provider*: "Show me how you would update your service pricing."
  - *Customer*: "Find a tech repair service near your hostel and message them."
* **15:00 - 25:00**: Guided questions:
  - "Where did you hesitate or feel unsure what to do next?"
  - "Would you trust paying through the app based on your current experience?"
* **25:00 - 30:00**: Wrap up and incentive / thank you acknowledgment.

---

## 6. Bug Triage & Severity Matrix

Every bug reported via the Google Form, WhatsApp group, or GitHub Issue Template must be tagged with a severity level:

| Severity | Definition | SLA / Action | Examples |
|---|---|---|---|
| **Critical** | App crashes on launch, data loss, payment failure, authentication blocked. | **Fix immediately before Play Store submission**. | Crash on splash screen, signup fails completely, inability to view orders. |
| **Major** | Core user journey impaired, but a workaround exists. | **Fix before Play Store submission or in 1st update**. | Chat message order out of sequence, search filter returns incorrect results, image upload takes over 1 minute. |
| **Minor** | Visual defect, typography misalignment, minor UX friction. | **Address in post-launch maintenance**. | Button padding inconsistency, dark mode icon color mismatch, minor typo in dialog. |
