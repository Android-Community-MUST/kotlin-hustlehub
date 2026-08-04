## Description

Ensure HustleHub is accessible to users with disabilities by following Android accessibility guidelines and the project's established standards documented in `docs/accesibility/ACCESSIBILITY STANDARDS.md`.

This issue covers a full, structured accessibility pass across all Jetpack Compose screens and components in the codebase. Work is divided across two developers by feature area to allow parallel progress on a single shared branch.

Target branch: `accesibility-audit`

---

## Original Task Requirements

- [x] Add contentDescription to all images
- [x] Add contentDescription to all icon buttons
- [x] Ensure minimum touch target size (48dp)
- [x] Test with TalkBack screen reader
- [x] Add semantic roles to interactive elements
- [x] Ensure proper heading hierarchy
- [x] Minimum contrast ratio 4.5:1 for all text
- [x] Support system font size scaling
- [x] Add clearSemantics() to decorative elements
- [x] Test with large font size (200%)

---

## Semantic Annotations Reference

````kotlin
// Icon button
IconButton(
    onClick = { /* send */ },
    modifier = Modifier.semantics {
        contentDescription = "Send message"
        role = Role.Button
    }
) {
    Icon(Icons.Default.Send, contentDescription = null)
}

// Rating bar
Row(
    modifier = Modifier.semantics(mergeDescendants = true) {
        contentDescription = "Rating: out of 5 stars"
    }
) {
    // Stars
}

// Decorative image
Image(
    painter = ...,
    contentDescription = null,
    modifier = Modifier.semantics { invisibleToUser() }
)
````

---

## Implementation Structure

### Phase 0 — Shared Composables (Complete Before Feature Work Begins)

One developer completes this phase. The other may begin feature work only after Phase 0 is merged or confirmed done on the branch to avoid conflicting changes to shared components.

- [x] `HustleButton.kt` — Verify minimum 48dp touch target. Add disabled state semantics.
- [x] `HustleTextField.kt` — Add visible label semantics, error message announcements, and logical focus traversal between fields.
- [x] `HustleCard.kt` — Apply `mergeDescendants = true` on all clickable card variants.
- [x] `HustleScaffold.kt` — Confirm no accessibility interference at the scaffold level.
- [x] `RatingBar.kt` — Expose a merged contentDescription announcing the rating value (e.g., "Rating: 4 out of 5 stars").
- [x] `SectionHeader.kt` — Mark all header text with `Modifier.semantics { heading() }`.
- [x] `LoadingIndicator.kt` — Announce loading state change to TalkBack when shown or hidden.
- [x] `ErrorView.kt` — Announce error message when displayed. Ensure retry action is labeled.
- [x] `EmptyStateView.kt` — Ensure descriptive content description on empty state illustration.

---

### Developer 1 — Auth, Profile, ProfileSetup, Settings, Chat

#### Feature: Auth

- [x] `LoginScreen.kt`
  - [x] Screen title marked with `heading()`.
  - [x] Email and password fields have visible labels and descriptive error announcements.
  - [x] Password visibility toggle icon describes its action ("Show password" / "Hide password").
  - [x] Login button meets 48dp minimum and has meaningful text.
- [x] `SignUpScreen.kt`
  - [x] All form fields have visible labels and error state announcements.
  - [x] Required fields are identified to assistive technology.
- [x] `ChangePasswordScreen.kt`
  - [x] Field labels and validation errors clearly announced.
  - [x] Success state communicated to TalkBack on completion.
- [x] `EmailVerificationScreen.kt`
  - [x] Verification status changes announced dynamically.
  - [x] Resend action has a clear accessible label.
- [x] `OtpInputField.kt`
  - [x] OTP input fields are navigable in logical order.
  - [x] Current input position communicated clearly.
- [x] `PasswordStrengthIndicator.kt`
  - [x] Strength level announced as a text description, not color alone.

Developer 1 acceptance check for Auth:
- [x] All Auth screens navigable end-to-end with TalkBack without confusion.
- [x] No color-only indicators. All validation states have text equivalents.
- [x] Tested at 200% font size — no clipping or overlapping elements.

---

#### Feature: Profile and ProfileSetup

- [x] `ProfileScreen.kt`
  - [x] Screen title marked with `heading()`.
  - [x] Profile avatar has `contentDescription` ("Profile picture of [name]") or `null` if purely decorative in context.
- [x] `ProviderProfileScreen.kt`
  - [x] Same heading and avatar standards as ProfileScreen.
- [x] `EditProfileScreen.kt`
  - [x] All editable fields have explicit visible labels.
  - [x] Save/submit button has clear label and meets 48dp.
- [x] `ProfileHeader.kt`
  - [x] Header composable does not duplicate announcements already read by parent.
- [x] `ProfileAvatar.kt`
  - [x] Passes correct contentDescription from parent context. Decorative by default — confirm this is correctly suppressed when no action is attached.
- [x] `ProfileBottomTabs.kt`
  - [x] Each tab exposes `Role.Tab` and announces selected/unselected state.
- [x] `ProfileStatsRow.kt`
  - [x] Stat values merged with their labels into single readable units (e.g., "5 reviews" not "5" then "reviews" as separate focus targets).
- [x] `ProfileInfo.kt`
  - [x] Informational text reads in natural order. No duplicate announcements.
- [x] `ProfileBadges.kt`
  - [x] Badges have descriptive labels. Decorative badges suppressed.
- [x] `ServicesSection.kt`
  - [x] Section header marked with `heading()`. Service items use `mergeDescendants = true`.
- [x] `ProfileStates.kt`
  - [x] Loading and error states announced correctly.
- [x] `ProviderOnboardingCard.kt`
  - [x] Card CTA button has clear action label and meets 48dp.

Developer 1 acceptance check for Profile:
- [x] Full profile screen navigable with TalkBack in correct reading order.
- [x] Tab selection state correctly announced.
- [x] Tested at 200% font size — no clipped names or overlapping stats.

---

#### Feature: Settings

- [x] `SettingsScreen.kt`
  - [x] Screen title marked with `heading()`.
  - [x] Each setting row that is tappable exposes `Role.Button`.
  - [x] Toggle switches expose `Role.Switch` and announce on/off state correctly.
  - [x] Grouped settings separated by section headers marked with `heading()`.

Developer 1 acceptance check for Settings:
- [x] Every toggle setting announces its state change when activated.
- [x] No touch target below 48dp.

---

#### Feature: Chat

- [x] `ChatScreen.kt`
  - [x] Screen title (conversation partner name) marked with `heading()`.
  - [x] All icon actions (search, call, more options) have clear descriptive labels.
- [x] `ChatDetailScreen.kt`
  - [x] Consistent heading and icon label standards as ChatScreen.
- [x] `MessageBubble.kt`
  - [x] Sender name, message text, and timestamp merged into a single TalkBack focus unit.
  - [x] Sent vs received state communicated semantically (not by color alone).
- [x] `DateSeparator.kt`
  - [x] Date text either marked as a heading or suppressed if redundant based on reading flow.
- [x] `ChatLocationPickerSheet.kt`
  - [x] Bottom sheet title marked with `heading()`.
  - [x] Location selection controls meet 48dp and have descriptive labels.

Developer 1 acceptance check for Chat:
- [x] A message thread navigable top-to-bottom with TalkBack without confusion.
- [x] Send button and attachment controls clearly labeled.
- [x] Tested at 200% font size — message bubbles do not clip text.

---

### Developer 2 — Home, Service, Map, Notification, Onboarding, Splash

#### Feature: Home

- [x] `HomeScreen.kt`
  - [x] Top-level screen title marked with `heading()`.
- [x] `HomeTopBar.kt`
  - [x] Profile icon and notification icon have clear action descriptions.
- [x] `HomeSearchBar.kt`
  - [x] Search field has a visible accessible label.
  - [x] Clear/cancel action has a descriptive label.
- [x] `CategoryChipRow.kt`
  - [x] Each category chip exposes `Role.Button` or `Role.Tab` as appropriate.
  - [x] Selected chip announces its selected state.
- [x] `ProviderBannerCard.kt`
  - [x] Card content merged via `mergeDescendants = true`.
  - [x] Card action has descriptive label when tappable.
- [x] `TopHustlersRow.kt`
  - [x] Each provider item in the row is a single merged focus unit.
- [x] `AiMatchCard.kt`
  - [x] Card merged. AI match label explained in text, not icon alone.
- [x] `FilterBottomSheet.kt`
  - [x] Sheet title marked with `heading()`.
  - [x] All filter controls meet 48dp. Toggle filters announce their state.
- [x] `ServiceCardShimmer.kt`
  - [x] Shimmer loading state either suppressed from TalkBack or announces "Loading".
- [x] `EmptyServicesView.kt`
  - [x] Illustration suppressed (`contentDescription = null`).
  - [x] Empty state message reads naturally.

Developer 2 acceptance check for Home:
- [x] Full home feed navigable with TalkBack in correct top-to-bottom order.
- [x] Category filter selection state announced correctly.
- [x] Tested at 200% font size — no card content clipped.

---

#### Feature: Service Management

- [x] `CreateServiceScreen.kt`
  - [x] Form section headers marked with `heading()`.
  - [x] All input fields labeled. Required fields identified.
  - [x] Validation errors announced.
- [x] `MyServicesScreen.kt`
  - [x] Screen title marked with `heading()`.
  - [x] Service management actions (edit, delete) clearly labeled.
- [x] `ServiceDetailScreen.kt`
  - [x] Sections (description, portfolio, reviews) each have `heading()` markers.
- [x] `WriteReviewScreen.kt`
  - [x] Rating input announces the selected value as text.
  - [x] Submit review button clearly labeled and meets 48dp.
- [x] `ServiceFormComponents.kt`
  - [x] All reusable form elements follow the same label and error standards as `HustleTextField.kt`.
- [x] `AvailabilityChipSelector.kt`
  - [x] Each availability chip announces selected/unselected state.
- [x] `AvailabilityBadge.kt`
  - [x] Badge text is readable as a semantic unit. Not communicated by color alone.
- [x] `ReviewCard.kt` and `ReviewSummaryCard.kt`
  - [x] Review content merged per card (reviewer name, rating, comment as one unit).
- [x] `PortfolioGallery.kt` and `PortfolioSlots.kt`
  - [x] Each portfolio image has a descriptive `contentDescription`.
  - [x] Empty slots announce "Empty portfolio slot" or are suppressed appropriately.
- [x] `FullScreenImageViewer.kt`
  - [x] Image has `contentDescription` passed from context.
  - [x] Close button clearly labeled and meets 48dp.
- [x] `ServiceLocationCard.kt`
  - [x] Location information readable as a merged unit.
- [x] `ServiceManagementCard.kt`
  - [x] Card merged. Edit and delete actions have distinct descriptive labels.
- [x] `DeleteConfirmDialog.kt`
  - [x] Dialog title marked with `heading()`.
  - [x] Confirm and cancel buttons clearly labeled and meet 48dp.
- [x] `MapLocationPickerModal.kt`
  - [x] Modal title marked with `heading()`.
  - [x] Confirm location action clearly labeled.

Developer 2 acceptance check for Service Management:
- [x] Full service creation flow completable with TalkBack.
- [x] Review submission completable with TalkBack.
- [x] Tested at 200% font size — form fields do not clip labels.

---

#### Feature: Map, Notification, Onboarding, Splash

- [x] Map feature screens
  - [x] Map view has an accessible description of its purpose.
  - [x] All controls overlaid on the map (e.g., locate me, confirm) meet 48dp and are labeled.
- [x] `NotificationScreen.kt`
  - [x] Screen title marked with `heading()`.
  - [x] Each notification item merged into a single TalkBack focus unit (type, sender, preview, time as one announcement).
- [x] Onboarding screens
  - [x] Each onboarding page title marked with `heading()`.
  - [x] Page indicator communicates current step (e.g., "Step 2 of 4").
  - [x] Next and skip buttons meet 48dp and have clear labels.
- [x] Splash screen
  - [x] App logo has a `contentDescription` or is suppressed via `clearAndSetSemantics`.
  - [x] No interactive elements require attention on this screen.

Developer 2 acceptance check for Map, Notification, Onboarding, Splash:
- [x] Notification list fully navigable with TalkBack.
- [x] Onboarding completable without sight using TalkBack.

---

## Acceptance Criteria

- [x] TalkBack reads all elements correctly across all screens.
- [x] All interactive touch targets are at minimum 48dp x 48dp.
- [x] System font scales to 200% without text clipping, hidden buttons, or layout overlap.
- [x] Contrast ratios meet WCAG AA standard (4.5:1 for normal text, 3:1 for large text).
- [x] No duplicate announcements from nested components.
- [x] Decorative elements have `contentDescription = null` or use `clearAndSetSemantics`.
- [x] Each developer verifies their own feature area against the project Accessibility Checklist before requesting review.

## Sprint

Sprint 6 - Week 11 - Day 65

## Estimated Time

16 hours total (2 hours Phase 0, 7 hours Developer 1, 7 hours Developer 2)
