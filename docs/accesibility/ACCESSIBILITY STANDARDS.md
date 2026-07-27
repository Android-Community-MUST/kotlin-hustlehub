# ♿ Accessibility Standards

> This document defines the accessibility standards that all Android features in this project should follow.
>
> Accessibility is **not a separate feature** or a task to complete at the end of development. It should be considered during design, development, testing, and code review.

---

# Purpose

The goal of this guide is to ensure every screen is usable by as many people as possible, including users who:

- Are blind or have low vision
- Are Deaf or hard of hearing
- Have limited mobility or dexterity
- Have cognitive or learning disabilities
- Use TalkBack
- Use Voice Access
- Use Switch Access
- Increase system font size
- Require higher colour contrast

---

# Accessibility Standards

## 1. Content Descriptions

### Images

- Every meaningful image must include a `contentDescription`.
- Decorative images should use `contentDescription = null`.

Example

```kotlin
Image(
    painter = painterResource(...),
    contentDescription = "Profile picture"
)
```

Decorative

```kotlin
Image(
    painter = painterResource(...),
    contentDescription = null
)
```

---

### Icon Buttons

Every icon button must describe its action.

Good examples

- Search
- Delete message
- Save
- Share event
- Open settings

Example

```kotlin
IconButton(
    onClick = { },
    modifier = Modifier.semantics {
        contentDescription = "Search"
    }
) {
    Icon(
        Icons.Default.Search,
        contentDescription = null
    )
}
```

---

# 2. Touch Targets

Interactive components should be at least **48dp × 48dp**.

This applies to:

- Buttons
- IconButtons
- Chips
- Cards
- Checkboxes
- Radio Buttons
- Switches
- Floating Action Buttons

---

# 3. Headings

Large screens should expose headings so TalkBack users can navigate quickly.

Use headings for:

- Screen titles
- Dialog titles
- Bottom Sheet titles
- Section titles

Example

```kotlin
Text(
    text = "Settings",
    modifier = Modifier.semantics {
        heading()
    }
)
```

---

# 4. Semantic Roles

Custom interactive components should expose their role.

Example

```kotlin
Row(
    modifier = Modifier
        .clickable { }
        .semantics {
            role = Role.Button
        }
)
```

Common roles

- Button
- Switch
- Checkbox
- Tab
- Image

---

# 5. Decorative Elements

Decorative elements should not be announced.

Example

```kotlin
Modifier.clearAndSetSemantics { }
```

or

```kotlin
contentDescription = null
```

---

# 6. Reading Order

The focus order should match the visual order.

Users navigating with TalkBack should experience the interface naturally from top to bottom and left to right.

---

# 7. Merge Semantics

Cards or grouped components should be announced as a single element where appropriate.

Example

```kotlin
Modifier.semantics(
    mergeDescendants = true
) {}
```

---

# 8. Forms

Every form should:

- Have visible labels
- Clearly identify required fields
- Display descriptive error messages
- Announce validation errors
- Keep a logical focus order

---

# 9. Colour & Contrast

Do not rely on colour alone to communicate information.

Good

✔ Success

✖ Error

Bad

🟢 Success

🔴 Error

Text should meet WCAG AA contrast recommendations.

---

# 10. Font Scaling

Support Android font scaling.

The UI should remain usable at **200%** system font size.

Avoid:

- Clipped text
- Hidden buttons
- Overlapping components

---

# 11. Keyboard & D-Pad Navigation

Interactive components should be reachable using keyboard or D-Pad navigation where applicable.

Focus should move logically through the screen.

---

# 12. Dynamic Content

Important changes should be announced.

Examples

- Snackbar messages
- Loading complete
- Upload successful
- Download failed

---

# 13. Material Components

Whenever possible, use Material 3 components instead of custom implementations.

Material components already include many accessibility improvements.

Examples

- Button
- Checkbox
- Switch
- Slider
- TextField
- AlertDialog
- NavigationBar

---

# 14. Accessibility Testing

Every feature should be tested using:

- TalkBack
- Accessibility Scanner
- Large font sizes (200%)
- Dark Mode
- Landscape Orientation
- Keyboard navigation (where applicable)

---

# Definition of Done

A feature should not be considered complete until accessibility has been reviewed.

Every Pull Request should reference the Accessibility Checklist before approval.
