# ✅ Android Accessibility Checklist

> Use this checklist during development, code reviews, QA, and before releasing a feature.

---

# Images

- [ ] Every meaningful image has a `contentDescription`.
- [ ] Decorative images use `contentDescription = null`.

---

# Icons

- [ ] Every `IconButton` has a meaningful accessible label.
- [ ] Icons inside buttons use `contentDescription = null`.
- [ ] Icon-only actions clearly describe their purpose.

---

# Buttons

- [ ] Buttons have meaningful text.
- [ ] Buttons are at least 48dp × 48dp.
- [ ] Disabled buttons expose the correct state.

---

# Custom Components

- [ ] Rows acting as buttons expose `Role.Button`.
- [ ] Custom switches expose `Role.Switch`.
- [ ] Custom checkboxes expose `Role.Checkbox`.
- [ ] Custom tabs expose `Role.Tab`.

---

# Headings

- [ ] Screen title marked with `heading()`.
- [ ] Dialog title marked with `heading()`.
- [ ] Bottom Sheet title marked with `heading()`.
- [ ] Long pages divided into logical headings.

---

# Semantics

- [ ] Appropriate semantic roles added.
- [ ] Related content merged where appropriate.
- [ ] Decorative elements hidden from accessibility.
- [ ] No duplicate announcements.
- [ ] State descriptions provided when necessary.

---

# Forms

- [ ] Every input has a visible label.
- [ ] Required fields are identified.
- [ ] Validation errors are descriptive.
- [ ] Error messages are announced.
- [ ] Focus moves logically between fields.

---

# Navigation

- [ ] Reading order matches the visual layout.
- [ ] Keyboard navigation works correctly.
- [ ] D-Pad navigation is logical (if applicable).

---

# Colour & Contrast

- [ ] Colour is not the only indicator of meaning.
- [ ] Text meets WCAG AA contrast recommendations.
- [ ] Icons remain visible in light and dark themes.

---

# Typography

- [ ] App supports 200% font scaling.
- [ ] Text is not clipped.
- [ ] Layout remains usable.
- [ ] Components remain accessible.

---

# Dynamic Content

- [ ] Snackbar messages are announced.
- [ ] Loading states are communicated.
- [ ] Success and error messages are announced.

---

# Material Components

- [ ] Material 3 components are used where possible.
- [ ] Custom components provide equivalent accessibility.

---

# Testing

## TalkBack

- [ ] Screen can be fully navigated.
- [ ] Reading order is correct.
- [ ] Buttons announce correctly.
- [ ] Images announce correctly.
- [ ] Decorative elements are ignored.

---

## Accessibility Scanner

- [ ] No missing labels.
- [ ] No touch target warnings.
- [ ] No contrast warnings.

---

## Manual Testing

- [ ] Tested with 200% font size.
- [ ] Tested in Dark Mode.
- [ ] Tested in Landscape.
- [ ] Tested using keyboard navigation (where applicable).

---

# Pull Request Checklist

Before requesting a review:

- [ ] Accessibility Checklist completed.
- [ ] Tested with TalkBack.
- [ ] Accessibility Scanner run.
- [ ] No accessibility regressions introduced.

---

# Accessibility Review Questions

Before marking a feature as complete, ask yourself:

- [ ] Can a blind user complete this task?
- [ ] Can a low-vision user comfortably use this screen?
- [ ] Can someone using TalkBack understand every action?
- [ ] Can someone with limited dexterity interact with every control?
- [ ] Does the screen remain usable at 200% font size?
- [ ] Does the reading order make sense?
- [ ] Have all meaningful images been described?
- [ ] Have decorative elements been ignored?
- [ ] Have semantic roles been added where required?
- [ ] Would I confidently ship this feature to every user?
