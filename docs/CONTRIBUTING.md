# Contributing to HustleHub

## Getting Started

### 1. Clone the repository
```bash
git clone https://github.com/Android-Community-MUST/kotlin-hustlehub.git
cd kotlin-hustlehub
```

### 2. Configure your environment
Copy the properties template to configure your local keys:
```bash
cp keys.properties.template keys.properties
```
Add your required keys (e.g., `MAPS_API_KEY`, `GEMINI_API_KEY`, `GOOGLE_WEB_CLIENT_ID`) to `keys.properties`. Ensure `google-services.json` is placed in your `app/` directory.

### 3. Branching Strategy
Our primary working branch is `dev`. **All Pull Requests must target `dev`**.
When starting new work, always branch off of `dev`:
```bash
git checkout dev
git pull origin dev
git checkout -b feature/issue-#-short-description
```
## Issue Assignment

1. **Browse Issues**: Check the [issues page](https://github.com/Android-Community-MUST/kotlin-hustlehub/issues)
2. **Pick One**: Choose an issue that's:
   - Not assigned to anyone
   - Tagged with `sprint-1` or current sprint
   - Matches your skill level
3. **Assign Yourself**: Click "assign yourself" on the right sidebar
4. **Comment**: Write "Working on this!" so others know
5. **Start Work**: Create a feature branch

## Working on an Issue

### Branch Naming
```bash
git checkout -b feature/issue-#-short-description
# Example: git checkout -b feature/8-splash-screen
```

### Commit Messages
```bash
git commit -m "feat(auth): implement splash screen

- Added SplashScreen composable
- Implemented auth state check
- Added fade-in animation

Closes #8"
```

### Closing Issues
- **You close your own issues** after completing all tasks
- Add "Closes #X" or "Fixes #X" in your commit message
- GitHub will auto-close when merged

### Pull Requests
1. Push your branch
2. Create Pull Request
3. Link to the issue in PR description
4. Wait for review
5. Once merged, issue auto-closes

## Acceptance Criteria
Before closing an issue, ensure:
- ✅ All tasks checked off
- ✅ Acceptance criteria met
- ✅ Code tested
- ✅ No errors/warnings
- ✅ `./gradlew ktlintCheck` passes
- ✅ `./gradlew detekt` passes
