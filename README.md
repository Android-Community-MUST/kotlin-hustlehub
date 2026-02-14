# 🚀 HustleHub

<div align="center">

![HustleHub Logo](https://via.placeholder.com/150x150/6C5CE7/FFFFFF?text=HustleHub)

**Your Campus Marketplace**

[![Platform](https://img.shields.io/badge/Platform-Android-brightgreen.svg)](https://www.android.com)
[![Language](https://img.shields.io/badge/Language-Kotlin-purple.svg)](https://kotlinlang.org)
[![Framework](https://img.shields.io/badge/UI-Jetpack%20Compose-blue.svg)](https://developer.android.com/jetpack/compose)
[![License](https://img.shields.io/badge/License-MIT-orange.svg)](LICENSE)
[![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen.svg)](docs/CONTRIBUTING.md)

*Connecting campus hustlers with customers through trust, convenience, and innovation*

[Features](#-features) • [Tech Stack](#-tech-stack) • [Getting Started](#-getting-started) • [Contributing](#-contributing) • [Roadmap](#-roadmap)

</div>

---

## 📖 About

**HustleHub** is a native Android marketplace app that transforms the informal peer-to-peer service economy at Meru University. Students offer diverse services—laundry, salon, tutoring, graphic design, food, and more—but currently rely on chaotic WhatsApp groups and word of mouth.

HustleHub organizes this economy into a structured, trustworthy platform with:

- 🔍 **AI-powered search** - Find services with natural language ("braids near Gate B under 500")
- 💬 **Safe messaging** - Chat without sharing phone numbers
- 🗺️ **Campus map** - See available providers in real-time
- ⭐ **Reputation system** - Ratings, reviews, and trust scores
- 🎓 **Student-only** - Verified via university email
- 📸 **Portfolio showcase** - Providers display their work

---

## ✨ Features

### Core Features (v1.0)

#### 🔐 Authentication
- Student email signup (@must.ac.ke)
- Email OTP verification
- Google Sign-In integration
- Role selection: Provider / Customer / Both

#### 📋 Service Management
- Create and edit service listings
- Portfolio upload (before/after images)
- Category selection (Salon, Laundry, Tutoring, Food, Tech, etc.)
- Availability toggle (Available/Busy/Offline)
- Price range configuration

#### 🔍 Discovery
- Browse by category with filters
- Text search with instant results
- **AI-powered natural language search** via Gemini API
- Sort by relevance, rating, distance
- Service detail pages with portfolio galleries

#### 💬 Messaging
- Real-time 1-on-1 chat
- Voice notes with waveform visualization
- Image sharing
- Location sharing
- Service Request Cards
- Read receipts & typing indicators
- Push notifications (FCM)

#### 🗺️ Campus Map
- Google Maps integration
- Provider location pins (color-coded by category)
- Filter pins by service type
- Distance-based discovery
- Quick actions: View Profile | Chat

#### ⭐ Ratings & Reviews
- 5-star rating system
- Written reviews with moderation
- Auto-prompt after service completion
- Average rating calculation

### Future Features (Roadmap)

- 📞 Voice/video calls (WebRTC)
- 💰 M-Pesa payment integration
- 🏆 Gamified reputation (Hustle Score, badges, leaderboards)
- 🔄 Service swap/barter system
- 🚨 Emergency request broadcasts
- 🏫 Multi-campus support
- 📈 Provider analytics dashboard

---

## 🛠️ Tech Stack

### Frontend
- **Language**: Kotlin 2.1.10
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM + Clean Architecture
- **Navigation**: Compose Navigation
- **DI**: Hilt (Dagger)
- **Networking**: Retrofit 2.9.0 + OkHttp 4.12.0
- **Local DB**: Room 2.8+
- **Image Loading**: Coil 2.x
- **State Management**: Kotlin Flow + StateFlow

### Backend
- **Authentication**: Firebase Auth
- **Database**: Firebase Firestore (users, services, reviews)
- **Real-time Chat**: Firebase Realtime Database
- **File Storage**: Firebase Storage
- **Notifications**: Firebase Cloud Messaging (FCM)
- **Serverless**: Cloud Functions for Firebase
- **Analytics**: Firebase Analytics + Crashlytics

### External APIs
- **AI Search**: Gemini API (Google)
- **Maps**: Google Maps SDK for Android

---

## 🏗️ Architecture

```
┌─────────────────────────────────────┐
│       Presentation Layer            │
│   (Jetpack Compose + ViewModels)   │
└─────────────┬───────────────────────┘
              │
┌─────────────▼───────────────────────┐
│         Domain Layer                │
│  (Use Cases + Repository Interfaces)│
└─────────────┬───────────────────────┘
              │
┌─────────────▼───────────────────────┐
│          Data Layer                 │
│ (Repositories + Data Sources)       │
│  ┌──────────┐  ┌─────────────┐     │
│  │   Room   │  │   Firebase  │     │
│  │ (Cache)  │  │  (Remote)   │     │
│  └──────────┘  └─────────────┘     │
└─────────────────────────────────────┘
```

**Package Structure:**
```
com.hustlehub.app/
├── data/           # Repositories, DAOs, DTOs
├── domain/         # Models, Use Cases, Interfaces
├── presentation/   # Screens, ViewModels, Components
├── di/             # Hilt Modules
├── navigation/     # Nav Graph
└── util/           # Extensions, Constants
```

See [PRD.md](docs/PRD.md) for detailed architecture diagrams and database schema.

---

## 🚀 Getting Started

### Prerequisites

- Android Studio Ladybug (or latest stable)
- JDK 17+
- Android SDK 34
- Firebase account
- Google Cloud account (for Maps & Gemini APIs)

### Installation

1. **Clone the repository**
   ```bash
   git clone git@github.com:Android-Community-MUST/kotlin-hustlehub.git
   cd hustlehub
   ```

2. **Set up Firebase**
   - Create a new Firebase project at [console.firebase.google.com](https://console.firebase.google.com)
   - Add an Android app to your project
   - Download `google-services.json`
   - Place it in the `app/` directory
   - Enable Authentication (Email/Password + Google)
   - Enable Firestore, Realtime Database, Storage, FCM

3. **Configure API Keys**
   
   Copy `keys.properties.template` to `keys.properties` in the project root and add your API keys:
   ```properties
   MAPS_API_KEY=your_google_maps_api_key
   GEMINI_API_KEY=your_gemini_api_key

4. **Build and Run**
   ```bash
   ./gradlew assembleDebug
   # or open in Android Studio and run
   ```

5. **Run Tests**
   ```bash
   # Unit tests
   ./gradlew test
   
   # Instrumented tests (requires emulator/device)
   ./gradlew connectedAndroidTest
   ```

---

## 📱 Screenshots

<div align="center">

| Home / Discovery | Service Detail | Chat |
|-----------------|----------------|------|
| ![Home](https://via.placeholder.com/200x400/6C5CE7/FFFFFF?text=Home) | ![Detail](https://via.placeholder.com/200x400/00CEC9/FFFFFF?text=Detail) | ![Chat](https://via.placeholder.com/200x400/00B894/FFFFFF?text=Chat) |

| Campus Map | Profile | Reviews |
|-----------|---------|---------|
| ![Map](https://via.placeholder.com/200x400/FDCB6E/000000?text=Map) | ![Profile](https://via.placeholder.com/200x400/E17055/FFFFFF?text=Profile) | ![Reviews](https://via.placeholder.com/200x400/A29BFE/FFFFFF?text=Reviews) |

</div>

*Note: Screenshots coming soon as development progresses*

---

## 🤝 Contributing

We welcome contributions from the community! Here's how you can help:

### Ways to Contribute
- 🐛 Report bugs via [GitHub Issues](#)
- 💡 Suggest features or improvements
- 📝 Improve documentation
- 🔧 Submit pull requests

### Development Workflow

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Follow our coding standards:
   - Use Kotlin coding conventions
   - Write meaningful commit messages
   - Add comments for complex logic
   - Write unit tests for new features
4. Commit your changes (`git commit -m 'Add amazing feature'`)
5. Push to the branch (`git push origin feature/amazing-feature`)
6. Open a Pull Request

See [CONTRIBUTING.md](docs/CONTRIBUTING.md) for detailed guidelines.

---

## 📅 Roadmap

### ✅ Phase 1: MVP (Months 1-3) - In Progress
- [x] Authentication & profile setup
- [x] Service creation & management
- [x] Discovery feed with search
- [x] AI-powered search (Gemini API)
- [x] Real-time messaging
- [x] Voice notes & image sharing
- [x] Campus map with provider pins
- [x] Ratings & reviews
- [ ] Beta launch (500 users)

### 🚧 Phase 2: Growth (Months 4-6)
- [ ] In-app voice/video calls
- [ ] M-Pesa payment integration
- [ ] Gamification (Hustle Score, badges, leaderboards)
- [ ] Provider analytics dashboard
- [ ] Push notification optimization

### 🔮 Phase 3: Scale (Months 7-12)
- [ ] Multi-campus support (Kenyatta, UoN, etc.)
- [ ] Service swap/barter matching
- [ ] Emergency request broadcasts
- [ ] Web admin panel
- [ ] iOS app (Flutter/Swift)

See [PRD.md](docs/PRD.md) for detailed feature specifications.

---

## 📊 Project Status

| Metric | Current | Target (3 months) |
|--------|---------|-------------------|
| Registered Users | 0 | 500+ |
| Active Providers | 0 | 100+ |
| Services Listed | 0 | 200+ |
| Conversations | 0 | 1,000+ |
| App Store Rating | N/A | 4.2+ ⭐ |

**Development Progress**: 25% (Sprint 1 of 6 complete)

---

## 🧪 Testing

### Running Tests

```bash
# Unit tests
./gradlew test

# Integration tests with Firebase emulators
firebase emulators:start --only auth,firestore,database,storage
./gradlew connectedAndroidTest

# UI tests
./gradlew connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.hustlehub.app.ChatFlowTest
```

### Test Coverage
- **Unit Tests**: 70%+ (Use Cases, Repositories, ViewModels)
- **UI Tests**: Critical user flows (Auth, Messaging, Discovery)
- **Integration Tests**: Firebase operations

---

## 📄 Documentation

### For Developers
- [Quick Setup Guide](docs/dev/SETUP.md) - Get started in 10 minutes
- [API Reference](docs/dev/API.md) - Firebase, Gemini, and Maps APIs
- [Architecture Guide](docs/dev/ARCHITECTURE.md) - Codebase structure and patterns
- [Troubleshooting](docs/dev/TROUBLESHOOTING.md) - Common issues and solutions
- [Changelog](docs/dev/CHANGELOG.md) - Version history and changes

### Product Documentation
- [Product Requirements Document (PRD)](docs/PRD.md) - Detailed product specifications
- [Contributing Guidelines](docs/CONTRIBUTING.md) - How to contribute
- [Code of Conduct](CODE_OF_CONDUCT.md) - Community standards

---

## 🔒 Privacy & Security

- **Student Verification**: Only @must.ac.ke emails allowed
- **Data Encryption**: All Firebase connections use TLS
- **Privacy Policy**: [View Policy](PRIVACY.md)
- **User Safety**:
  - Block/report features
  - Content moderation
  - In-app safety tips

---

## 📜 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 🙏 Acknowledgments

- [Meru University](https://must.ac.ke) for the inspiration
- [Firebase](https://firebase.google.com) for backend infrastructure
- [Jetpack Compose](https://developer.android.com/jetpack/compose) team for the amazing UI toolkit
- [Google AI](https://ai.google.dev) for Gemini API access
- All beta testers and early adopters

---

## 🌟 Get Involved

- ⭐ Star this repo if you find it useful
- 🐛 Report bugs via [Issues](#)
- 💬 Join discussions in [Discussions](#)
- 🧪 Sign up for beta testing: [Google Form](#)

---

## 🌟 Star History



---

<div align="center">

**Built with ❤️ by campus hustlers, for campus hustlers**

[⬆ Back to Top](#-hustlehub)

</div>