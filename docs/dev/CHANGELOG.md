# 📝 Changelog

All notable changes to HustleHub will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- Initial project setup with `must.kdroiders.hustlehub` package
- MVVM architecture with Hilt DI (AppModule, SupabaseModule)
- Firebase integration (Auth, Firestore, Storage, Crashlytics, Perf)
- Supabase Storage integration for file uploads
- Navigation 3 infrastructure with serializable NavKeys
- Bottom navigation bar (Home, Map, Chat, Profile tabs)
- Splash screen with auth-gate routing
- Onboarding carousel for first-run experience
- Sign-up screen with email/password (Firebase Auth)
- Password strength indicator component
- Profile setup wizard (post first-login)
- Profile screen with avatar, badges, stats, and services section
- Portfolio upload screen
- Home, Map, and Chat placeholder screens
- DataStore Preferences for persisting user settings
- Theme system with dark mode, custom colors, shapes, and typography
- Shared composable library (HustleButton, HustleCard, HustleTextField, EmptyStateView, ErrorView, LoadingIndicator, RatingBar)
- Code quality tooling: ktlint + detekt
- Unit tests (SignUpViewModel, StorageRepository, ImageUtils)

### Changed
- N/A

### Deprecated
- N/A

### Removed
- N/A

### Fixed
- N/A

### Security
- N/A

## [0.1.0] - 2026-02-14

### Added
- Project initialization
- Basic project structure
- Firebase configuration
- Dependency setup

---

## Version Guidelines

### Version Format: MAJOR.MINOR.PATCH

- **MAJOR**: Incompatible API changes
- **MINOR**: New features (backward compatible)
- **PATCH**: Bug fixes (backward compatible)

### Change Categories

- **Added**: New features
- **Changed**: Changes in existing functionality
- **Deprecated**: Soon-to-be removed features
- **Removed**: Removed features
- **Fixed**: Bug fixes
- **Security**: Security fixes

### Example Entry

```markdown
## [1.2.0] - 2026-03-15

### Added
- M-Pesa payment integration (#45)
- Voice/video calling feature (#52)
- Provider analytics dashboard (#58)

### Changed
- Improved search algorithm performance (#48)
- Updated UI design for service cards (#50)

### Fixed
- Fixed crash when uploading large images (#47)
- Resolved notification not showing issue (#51)

### Security
- Updated Firebase SDK to fix security vulnerability (#49)
```

---

## Migration Guides

### Migrating to 1.0.0 (Future)

When version 1.0.0 is released, migration guides will be added here.

---

**Note**: This changelog is maintained manually. Please update it when making significant changes.
