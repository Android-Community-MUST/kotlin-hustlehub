# 🚀 HustleHub

<div align="center">

![HustleHub Logo](https://via.placeholder.com/150x150/6C5CE7/FFFFFF?text=HustleHub)

**Your Campus Marketplace**

[![Platform](https://img.shields.io/badge/Platform-Android-brightgreen.svg)](https://www.android.com)
[![Language](https://img.shields.io/badge/Language-Kotlin-purple.svg)](https://kotlinlang.org)
[![Framework](https://img.shields.io/badge/UI-Jetpack%20Compose-blue.svg)](https://developer.android.com/jetpack/compose)
[![Backend](https://img.shields.io/badge/Backend-Spring%20Boot-green.svg)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/License-MIT-orange.svg)](LICENSE)
[![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen.svg)](docs/CONTRIBUTING.md)

*Connecting campus hustlers with customers through trust, convenience, and innovation*

[Features](#-features) • [Tech Stack](#-tech-stack) • [Getting Started](#-getting-started) • [Contributing](#-contributing) • [Roadmap](#-roadmap)

</div>

---

## 📖 About

**HustleHub** is a native Android marketplace app that transforms the informal peer-to-peer service economy at Meru University of Science & Technology. Students offer laundry, salon, tutoring, graphic design, food, and more — but currently rely on chaotic WhatsApp groups and word of mouth.

HustleHub organizes this economy into a structured, trustworthy platform with:

- 🔍 **AI-powered search** — "braids near Gate B under 500"
- 💬 **Safe in-app messaging** — no phone number sharing required
- 🗺️ **Campus map** — real-time provider locations
- ⭐ **Reputation system** — ratings, reviews, and trust scores
- 🎓 **Student-only access** — verified via university email
- 📸 **Portfolio showcase** — providers display their work

---

## ✨ Features

### Core Features (v1.0)

**Authentication**: Student email signup (@must.ac.ke), email OTP verification, Google Sign-In, role selection (Provider / Customer / Both)

**Service Management**: Create and edit listings with portfolio images, category selection, price range, availability toggle (Available / Busy / Offline)

**Discovery**: Browse by category, text search, **AI-powered natural language search** via Gemini, sort by relevance / rating / distance

**Messaging**: Real-time 1-on-1 chat, voice notes with waveform, image sharing, location sharing, Service Request Cards, read receipts, push notifications (FCM)

**Campus Map**: Google Maps with provider pins color-coded by category, distance-based discovery, quick actions from map

**Ratings & Reviews**: 5-star rating, written reviews, moderation, auto-prompt after service

### Future Features (Roadmap)

- 📞 Voice/video calls (WebRTC)
- 💰 M-Pesa payment integration
- 🏆 Gamified Hustle Score, badges, leaderboards
- 🏫 Multi-campus support (Kenyatta, UoN, etc.)
- 📈 Provider analytics dashboard

---

## 🛠️ Tech Stack

### Android Client
- **Language**: Kotlin 2.x
- **UI**: Jetpack Compose + Material 3 Expressive
- **Architecture**: MVVM + Clean Architecture (feature-based)
- **Navigation**: Navigation 3 (`androidx.navigation3`)
- **DI**: Hilt (Dagger)
- **Networking**: Retrofit 3.0.0 + OkHttp 5.x
- **Real-time Chat**: OkHttp WebSocket client → Spring Boot WebSocket/STOMP
- **Local Cache**: Room 2.8.4 + DataStore Preferences (offline-first)
- **Image Loading**: Coil 2.7.0
- **State**: Kotlin Flow + StateFlow

### Backend & Infrastructure
- **REST API**: Spring Boot (Kotlin) — all data at `/api/v1/`
- **Real-time**: Spring WebSocket / STOMP — live chat and presence
- **Authentication**: Firebase Auth (Email/Password + Google Sign-In)
- **Push Notifications**: Firebase Cloud Messaging (FCM)
- **AI Search**: Gemini API (invoked server-side)
- **Maps**: Google Maps SDK for Android

> ⚠️ **No Firestore, no Firebase Realtime Database, no Supabase.** All application data is served by the Spring Boot backend. Firebase is used exclusively for Auth and FCM.

---

## 🏗️ Architecture

```
Android Client
├── Presentation (Compose + ViewModels)
├── Domain (Use Cases + Repository Interfaces)
└── Data (Retrofit APIs + Room Cache + WebSocket)
        │
        ▼ REST /api/v1/ + WebSocket
Spring Boot Backend
├── REST API (services, discovery, chat, users, reviews, media)
├── WebSocket Server (real-time chat)
├── Gemini AI Service (natural language search)
└── Database + File Storage
        │
Firebase (Auth + FCM only)
```

### Package Structure
```
must.kdroiders.hustlehub/
├── core/                    # Shared: ApiClient, AuthInterceptor, theme, utils
├── di/                      # Hilt: NetworkModule, FirebaseModule, RepositoryModule
├── navigation/              # Navigation 3 NavKeys + NavGraph
├── local/                   # Room: DAOs, entities (offline cache)
└── feature/
    ├── auth/                # Firebase Auth → POST /api/v1/auth/register
    ├── profile/             # GET/PUT /api/v1/users/me
    ├── services/            # CRUD /api/v1/services
    ├── discovery/           # /api/v1/discovery/* (browse, search, AI, map pins)
    ├── chat/                # REST history + WebSocket real-time
    ├── map/                 # Google Maps + provider pins
    ├── reviews/             # POST /api/v1/reviews
    ├── notifications/       # FCM service + notification center
    └── media/               # POST /api/v1/media/upload
```

See [ARCHITECTURE.md](docs/dev/ARCHITECTURE.md) for full detail.

---

## 🚀 Getting Started

### Prerequisites

- Android Studio Ladybug (or latest stable)
- JDK 17+
- Firebase account (Auth + FCM only)
- Google Cloud account (Maps API key)
- **HustleHub Spring Boot backend** running locally

### Installation

1. **Clone the Android repo**
   ```bash
   git clone git@github.com:Android-Community-MUST/kotlin-hustlehub.git
   cd kotlin-hustlehub
   ```

2. **Clone and start the Spring Boot backend**
   ```bash
   git clone git@github.com:Android-Community-MUST/hustlehub-backend.git
   cd hustlehub-backend
   ./gradlew bootRun   # starts on localhost:8080
   ```

3. **Set up Firebase (Auth + FCM only)**
   - Create a Firebase project at [console.firebase.google.com](https://console.firebase.google.com)
   - Add Android app with package `must.kdroiders.hustlehub`
   - Download `google-services.json` → place in `app/`
   - Enable **Authentication** (Email/Password + Google Sign-In)
   - Enable **Cloud Messaging** (auto-enabled)
   - ❌ Do NOT enable Firestore, Realtime Database, or Storage

4. **Configure API keys**
   ```bash
   cp keys.properties.template keys.properties
   ```
   Edit `keys.properties`:
   ```properties
   MAPS_API_KEY=AIzaSy...your_maps_key
   BASE_URL=http://10.0.2.2:8080/api/v1/
   WS_BASE_URL=ws://10.0.2.2:8080/ws
   GEMINI_API_KEY=AIzaSy...your_gemini_key
   ```

5. **Build and run**
   ```bash
   ./gradlew installDebug
   ```

See [SETUP.md](docs/dev/SETUP.md) for full setup instructions.

---

## 📱 Screenshots

<div align="center">

| Home / Discovery | Chat | Profile |
|-----------------|------|---------|
| ![Home](https://via.placeholder.com/200x400/6C5CE7/FFFFFF?text=Home) | ![Chat](https://via.placeholder.com/200x400/00B894/FFFFFF?text=Chat) | ![Profile](https://via.placeholder.com/200x400/E17055/FFFFFF?text=Profile) |

*Screenshots coming soon as development progresses*

</div>

---

## 🤝 Contributing

1. Fork the repo and create a feature branch: `git checkout -b feature/amazing-feature`
2. Follow [Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html)
3. Run quality checks before committing:
   ```bash
   ./gradlew ktlintCheck detekt test
   ```
4. Use Conventional Commits: `feat(chat): add typing indicators`
5. Open a Pull Request against `develop`

See [CONTRIBUTING.md](docs/CONTRIBUTING.md) for full guidelines.

---

## 📅 Roadmap

### ✅ Phase 1: MVP (Months 1–3) — In Progress
- [x] Authentication & profile setup
- [x] Design system + shared composables
- [ ] Service creation & discovery feed
- [ ] AI-powered search
- [ ] Real-time messaging (WebSocket)
- [ ] Campus map with provider pins
- [ ] Ratings & reviews
- [ ] Beta launch (500 users)

### 🚧 Phase 2: Growth (Months 4–6)
- [ ] In-app voice/video calls
- [ ] M-Pesa payment integration
- [ ] Gamification (Hustle Score, badges, leaderboards)
- [ ] Provider analytics dashboard

### 🔮 Phase 3: Scale (Months 7–12)
- [ ] Multi-campus support
- [ ] Service swap / barter matching
- [ ] Emergency request broadcasts
- [ ] Web admin panel

---

## 🧪 Testing

```bash
# Unit tests
./gradlew test

# Instrumented tests (requires emulator/device + backend running)
./gradlew connectedAndroidTest

# Code quality
./gradlew ktlintCheck detekt
```

**Test coverage targets**: 70%+ for domain and data layers, critical UI flows covered.

---

## 📄 Documentation

| Document | Description |
|----------|-------------|
| [PRD.md](docs/PRD.md) | Full product requirements |
| [ARCHITECTURE.md](docs/dev/ARCHITECTURE.md) | Codebase structure and patterns |
| [API.md](docs/dev/API.md) | Spring Boot REST + WebSocket API reference |
| [SETUP.md](docs/dev/SETUP.md) | Development environment setup |
| [TROUBLESHOOTING.md](docs/dev/TROUBLESHOOTING.md) | Common issues and solutions |
| [CHANGELOG.md](docs/dev/CHANGELOG.md) | Version history |
| [CONTRIBUTING.md](docs/CONTRIBUTING.md) | Contribution guidelines |

---

## 🔒 Privacy & Security

- **Student verification**: Only @must.ac.ke emails allowed
- **Token security**: All API calls use short-lived Firebase ID tokens (auto-refreshed)
- **No phone number sharing**: In-app messaging only
- **User safety**: Block/report features, content moderation, in-app safety tips
- **Privacy Policy**: [View Policy](PRIVACY.md)

---

## 📜 License

MIT License — see [LICENSE](LICENSE) for details.

---

## 🙏 Acknowledgments

- [Meru University of Science & Technology](https://must.ac.ke) for the inspiration
- [Spring Boot](https://spring.io) + [Kotlin](https://kotlinlang.org) for the backend
- [Jetpack Compose](https://developer.android.com/jetpack/compose) for the UI toolkit
- [Firebase](https://firebase.google.com) for Auth and FCM
- [Google AI](https://ai.google.dev) for Gemini API
- All beta testers and early adopters

---

<div align="center">

**Built with ❤️ by campus hustlers, for campus hustlers**

[⬆ Back to Top](#-hustlehub)

</div>
