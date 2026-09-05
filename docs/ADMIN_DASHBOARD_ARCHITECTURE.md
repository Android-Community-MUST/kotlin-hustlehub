# HustleHub In-App Admin Dashboard Architecture & Implementation Guide

## 📌 1. Overview & Motivation

The **HustleHub In-App Admin Dashboard** was designed to provide designated campus administrators with native mobile moderation and operational controls directly inside the Android application (`kotlin-hustlehub`).

### The Challenge
- The standalone web admin dashboard was an early-stage frontend prototype with placeholder pages (`<div>page</div>`) and mock login states.
- The Spring Boot backend (`hustlehub-backend`) already had production-ready REST API endpoints under `/api/v1/admin/*` with full database support for analytics, report resolution, user suspension, and service delisting.
- Student campus administrators needed an immediate, reliable way to manage reports, ban abusive accounts, grant verified badges, and monitor real-time platform metrics from their mobile devices.

### The Solution
Drawing from the proven control center design in **Campus Connect**, we integrated a native Jetpack Compose **Admin Center** into `kotlin-hustlehub` coupled with automatic role assignment on `hustlehub-backend`.

---

## 🔐 2. Security & Access Control Model

A **defense-in-depth** model ensures administrative features remain inaccessible to regular students:

```
┌────────────────────────────────────────────────────────┐
│ 1. Client-Side Email Gating (AdminAuthUtils)           │
│    Only verified admin emails see the Admin Center btn │
└──────────────────────────┬─────────────────────────────┘
                           │ Firebase ID Token
                           ▼
┌────────────────────────────────────────────────────────┐
│ 2. Backend Filter (FirebaseJwtFilter)                  │
│    Extracts verified email from Firebase Auth token    │
│    Assigns ROLE_ADMIN authority in Spring Security     │
└──────────────────────────┬─────────────────────────────┘
                           │ Authorized HTTP Request
                           ▼
┌────────────────────────────────────────────────────────┐
│ 3. RBAC Route Protection (SecurityConfig)              │
│    /api/v1/admin/** strictly requires ROLE_ADMIN       │
└────────────────────────────────────────────────────────┘
```

### Authorized Administrator Allowlist:
- `kipyegonaldo@gmail.com`
- `vertigoproject.lab@gmail.com`
- `jumaderick89@gmail.com`

---

## 📱 3. Mobile Client Architecture (`kotlin-hustlehub`)

The feature is built using **Clean Architecture** and **Unidirectional Data Flow (UDF)** under `must.kdroiders.hustlehub.ui.features.admin`:

```
ui.features.admin/
├── data/
│   ├── remote/
│   │   ├── AdminApiService.kt         # Retrofit REST interface
│   │   └── dto/AdminDto.kt            # Data transfer objects
│   └── repository/
│       └── AdminRepositoryImpl.kt     # Concrete repository implementation
├── domain/
│   ├── model/AdminModels.kt           # Domain entities (Analytics, Users, Reports, Logs)
│   └── repository/AdminRepository.kt  # Repository interface
└── presentation/
    ├── viewmodel/
    │   ├── AdminUiState.kt            # Immutable UI state & action target models
    │   └── AdminViewModel.kt          # State management & coroutine execution
    └── view/
        ├── AdminDashboardScreen.kt    # Main Scaffold & Tab host
        └── components/
            ├── AdminOverviewTab.kt    # Real-time KPI metrics
            ├── AdminReportsTab.kt     # Student report inspection & resolution
            ├── AdminUsersTab.kt       # Search, suspend/unsuspend, PRO verification
            ├── AdminServicesTab.kt    # Quick delisting/relisting of spam services
            ├── AdminAuditLogsTab.kt   # Chronological moderation timeline
            └── AdminActionDialog.kt   # Confirmation dialog with reason logging
```

---

## 🧭 4. Navigation & Entry Point

### Profile Screen Button
On `ProfileScreen.kt`, when `AdminAuthUtils.isAuthorizedAdmin(user.email, user.role.name)` is `true`, a prominent **🛡️ Admin Center** button is rendered below the profile header.

### Navigation 3 Integration
- **`HustleNavKeys.kt`**: Added `@Serializable data object AdminDashboard : NavKey`.
- **`HustleHubNavGraph.kt`**: Registered `entry<AdminDashboard>` destination.
- **`MainScaffold.kt`**: Forwarded `onNavigateToAdminDashboard` from `MainShellScreen` to `ProfileScreen`.

---

## ⚡ 5. Backend Integration (`hustlehub-backend`)

### Endpoints Connected:
| Endpoint | Method | Action |
|---|---|---|
| `/api/v1/admin/analytics` | `GET` | Fetches platform KPIs (Users, Services, Pro Subscribers, Open Reports, Revenue) |
| `/api/v1/admin/users` | `GET` | Paginated user directory with search and status |
| `/api/v1/admin/users/{id}/suspend` | `POST` | Suspends user account with required reason note |
| `/api/v1/admin/users/{id}/unsuspend` | `POST` | Restores account access |
| `/api/v1/admin/users/{id}/verify-pro` | `POST` | Grants official PRO verification badge |
| `/api/v1/admin/users/{id}/revoke-pro` | `POST` | Revokes PRO verification badge |
| `/api/v1/admin/services/{id}/delist` | `POST` | Immediately hides service from discovery & search |
| `/api/v1/admin/services/{id}/relist` | `POST` | Restores service to discovery & search |
| `/api/v1/admin/reports` | `GET` | Lists student reports filtered by status (`OPEN`, `RESOLVED`, `DISMISSED`) |
| `/api/v1/admin/reports/{id}/resolve` | `POST` | Marks report as resolved with admin note |
| `/api/v1/admin/reports/{id}/dismiss` | `POST` | Dismisses report |
| `/api/v1/admin/audit-logs` | `GET` | Retrieves audit trail of moderation actions |

---

## 🛡️ 6. Pull Request & Secret Safety

To ensure sensitive credentials and development configurations never leak into GitHub Pull Requests:
1. `app/google-services.json`, `local.properties`, and `keys.properties` are listed in `.gitignore`.
2. Verified with `git status --ignored` and `git log` that only the clean Kotlin source files are committed on the branch.
