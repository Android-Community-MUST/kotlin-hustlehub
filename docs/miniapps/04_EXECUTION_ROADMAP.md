# 04. Phased Execution Roadmap & Milestones

This document establishes the step-by-step engineering roadmap to transition HustleHub into a production-grade Super App platform without disrupting the existing campus marketplace features.

---

## 🗺️ High-Level Roadmap Overview

```
┌────────────────────────────────────────────────────────────────────────┐
│                        ENGINEERING ROADMAP                             │
├───────────────┬────────────────┬───────────────┬───────────────────────┤
│    Phase 1    │    Phase 2     │    Phase 3    │        Phase 4        │
│   In-App PoC  │  Native Bridge │  Multi-Module │  Catalog & Developer  │
│  (Container)  │ (M-Pesa & Auth)│  Refactoring  │       Ecosystem       │
├───────────────┼────────────────┼───────────────┼───────────────────────┤
│ - Compose     │ - Wire M-Pesa  │ - Extract     │ - Spring Boot Catalog │
│   WebView     │   Bottom Sheet │   :core:mini- │   API (/api/v1/apps)  │
│ - BackHandler │ - Token-free   │   contract    │ - Mini-App Manifest   │
│ - Pilot Web   │   Identity     │ - Extract     │ - Campus Developer    │
│   Page (HTML) │ - Native Toast │   :feature:   │   Portal & SDK doc    │
│               │   & Navigation │   runtime     │                       │
└───────────────┴────────────────┴───────────────┴───────────────────────┘
```

---

## 🚀 Phase 1: Proof of Concept Container (Estimated: 3–5 Days)

**Objective**: Validate the WebView container inside the current Navigation 3 stack without refactoring the whole build.

### Tasks
- [ ] Create `must.kdroiders.hustlehub.ui.features.miniapp.presentation.MiniAppContainerScreen.kt`
- [ ] Add `MiniAppNavKey(val appId: String, val title: String, val url: String)` to [HustleNavKeys.kt](file:///home/korryr/StudioProjects/kotlin-hustlehub/app/src/main/java/must/kdroiders/hustlehub/navigation/HustleNavKeys.kt)
- [ ] Implement Compose `BackHandler` so pressing Android's back gesture navigates back through the webview's internal history (`webView.goBack()`) before popping the screen.
- [ ] Implement basic URL scheme handling in `WebViewClient` (prevent crashes on `tel:`, `mailto:`, and `intent:`).
- [ ] Deploy a test 1-page HTML/JS mini-app on GitHub Pages (e.g. "MUST Exam Timetable" or "Canteen Menu").
- [ ] Add a test entry button on `HomeScreen` to launch the test mini-app.

### Acceptance Criteria
* The web app renders inside HustleHub with a native top bar and back button.
* Pressing the hardware back button navigates web pages within the mini-app without exiting to the home screen prematurely.
* No memory leaks observed when entering and exiting the screen 10 times in Android Studio Profiler.

---

## 💳 Phase 2: Native Bridge & Payment Integration (Estimated: 1–2 Weeks)

**Objective**: Connect the JavaScript environment to HustleHub's existing M-Pesa rail and student profile.

### Tasks
- [ ] Create `HustleBridge.kt` with `@JavascriptInterface` methods (`getStudentProfile`, `requestPayment`, `close`).
- [ ] Inject `HustleBridge` into the WebView container under the global namespace `window.HustleBridge`.
- [ ] Connect `requestPayment` to the native Compose payment confirmation sheet:
  - Mini-app calls `requestPayment(amount, memo)`.
  - Native Compose sheet pops up showing the vendor name and amount.
  - On user tap, call the existing [InitiateStkPushUseCase.kt](file:///home/korryr/StudioProjects/kotlin-hustlehub/app/src/main/java/must/kdroiders/hustlehub/ui/features/monetization/domain/usecase/InitiateStkPushUseCase.kt).
  - Observe [PollPaymentStatusUseCase.kt](file:///home/korryr/StudioProjects/kotlin-hustlehub/app/src/main/java/must/kdroiders/hustlehub/ui/features/monetization/domain/usecase/PollPaymentStatusUseCase.kt) and dispatch `HustlePaymentComplete` event back to the webview.
- [ ] Publish the lightweight JavaScript client library `hustlehub-sdk.js`.

### Acceptance Criteria
* A test web mini-app can trigger an M-Pesa STK push via `HustleHub.payWithMpesa(10, "Test Order")`.
* Safaricom Daraja STK prompt arrives on the device.
* Upon entering the M-Pesa PIN, the webview receives the receipt number automatically without a page reload.

---

## 📦 Phase 3: Multi-Module Extraction (Estimated: 2 Weeks)

**Objective**: Enforce architectural boundaries by extracting the mini-app runtime and contracts into isolated Gradle modules.

### Tasks
- [ ] Update [settings.gradle.kts](file:///home/korryr/StudioProjects/kotlin-hustlehub/settings.gradle.kts) to include:
  ```kotlin
  include(":core:miniapp-contract")
  include(":feature:miniapp-runtime")
  ```
- [ ] Move data models (`MiniAppManifest`, `MiniAppProfile`, `PaymentRequest`) into `:core:miniapp-contract`.
- [ ] Move `MiniAppContainerScreen`, `HustleBridgeImpl`, and `SecureMiniAppWebViewClient` into `:feature:miniapp-runtime`.
- [ ] Configure Hilt DI bindings for the new modules.
- [ ] Verify that `:feature:miniapp-runtime` has zero dependencies on internal feature packages like `chat` or `admin`.

### Acceptance Criteria
* `./gradlew assembleDebug` completes with multi-module parallel compilation.
* Circular dependencies between features and runtime are strictly eliminated.

---

## 🌐 Phase 4: Dynamic Catalog & Developer Ecosystem (Estimated: 2–3 Weeks)

**Objective**: Allow dynamic discovery of mini-apps and open the platform to student creators.

### Tasks
- [ ] Add Backend Spring Boot API endpoint: `GET /api/v1/miniapps`:
  ```json
  [
    {
      "id": "must-mess",
      "name": "Mess & Canteen",
      "iconUrl": "https://cdn.hustlehub.must.ac.ke/icons/mess.png",
      "entryUrl": "https://mess.must.ac.ke",
      "category": "FOOD",
      "version": "1.0.0"
    }
  ]
  ```
- [ ] Build a "Campus Services / Mini Apps" grid on `HomeScreen` that renders apps dynamically from the backend response.
- [ ] Write a developer guide and starter template (HTML + Preact) for MUST student developers.
- [ ] Establish an audit and verification process for campus mini-apps before publishing them to the catalog.

---

## 📈 Long-Term Evolution: Offline Bundling (Future Milestone)

Once the web-hosted mini-apps are stable, introduce **Offline Bundle Caching**:
1. Mini-apps are compiled into static zip bundles (`bundle.zip` containing `index.html`, `bundle.js`, `style.css`).
2. When the user taps a mini-app for the first time, HustleHub downloads and extracts the zip file into the app's encrypted internal cache.
3. Subsequent opens load instantly via Android's `WebViewAssetLoader`, eliminating network latency and working even when campus cellular data is slow.
