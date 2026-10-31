# DawaDiary Core Functionality and CI Implementation Plan

> **For agentic workers:** Execute task-by-task with tests before implementation and verify the complete branch before merge.

**Goal:** Close confirmed profile-edit and dose-snooze correctness gaps, and make CI execute unit tests before producing the debug APK.

**Architecture:** Preserve the existing Room/Compose architecture. Use the existing ProfileDao update API and existing conditional dose-status DAO query; do not introduce new abstractions or schema migrations. Extend the current Robolectric/Room test suite and the existing GitHub Actions workflow.

**Tech Stack:** Kotlin, Jetpack Compose, Room, Coroutines, JUnit, Robolectric, Gradle, GitHub Actions.

**Spec:** Findings from the DawaDiary code audit and docs/design/dawadiary-visual-refresh.md.

## Global Constraints
- Keep the app's existing Material 3 / Compose UI patterns.
- Preserve existing profile IDs and createdAt values when editing.
- A dose action may transition only from PENDING; repeated/stale notification actions must not overwrite terminal states.
- Do not commit signing keys, passwords, or credentials.
- Do not claim tests pass until the workflow reports success.

## Review Focus
- Editing a profile must update the existing row rather than create a duplicate.
- Profile edit cancellation must not mutate the profile.
- A snooze action arriving after TAKEN/SKIPPED must not overwrite the terminal status or schedule a snooze alarm.
- Repeated snooze actions must not create additional scheduled reminders.
- CI must fail when unit tests fail and must still upload the debug APK after tests pass.

---

### Task 1: Profile editing

**Files:**
- Modify: `app/src/main/java/com/example/ui/screens/FamilyScreen.kt`
- Modify: `app/src/main/java/com/example/ui/MainViewModel.kt`

**Interfaces:**
- Existing UI callback: `onSaveProfile(ProfileEntity)`
- Existing DAO API: `ProfileDao.updateProfile(ProfileEntity): Int`

- [ ] Add an edit action to each profile card that sets `editingProfile` and opens the existing dialog.
- [ ] Ensure dialog dismissal clears edit state.
- [ ] Update `MainViewModel.saveProfile`: insert when id == 0L; update when id > 0L; do not change active-profile selection during an edit.
- [ ] Review resulting diff for preserved profile ID and createdAt.

### Task 2: Guard snooze state transitions

**Files:**
- Modify: `app/src/main/java/com/example/ui/MainViewModel.kt`
- Modify: `app/src/main/java/com/example/reminder/NotificationActionReceiver.kt`
- Test: `app/src/test/java/com/example/DataIntegrityTest.kt`

**Interfaces:**
- Existing DAO API: `DoseLogDao.updateDoseStatusConditional(id, expectedStatus, newStatus, actionTime, notes): Int`

- [ ] Add a Room regression test proving only PENDING can transition to SNOOZED and a second transition affects zero rows.
- [ ] Run the targeted test and confirm it fails against the unconditional update behavior.
- [ ] Replace unconditional snooze updates with PENDING-conditional updates.
- [ ] Schedule the snooze alarm only when exactly one row was updated.
- [ ] Verify the targeted regression test passes.

### Task 3: CI test gate

**Files:**
- Modify: `.github/workflows/android.yml`

- [ ] Add a Gradle unit-test step using `bash ./gradlew testDebugUnitTest --no-daemon`.
- [ ] Keep APK assembly and artifact upload after the test step so failures block publishing.
- [ ] Verify the GitHub Actions run executes tests and assembles the debug APK.

### Task 4: Release signing safety follow-up

**Files:**
- Review: `app/build.gradle.kts`

- [ ] Keep release signing out of scope for this patch until a secure keystore and GitHub secret names are agreed.
- [ ] Do not use the debug keystore for a production release; document the release blocker in the PR.

### Task 5: Final verification

- [ ] Inspect the complete branch diff.
- [ ] Confirm no secrets or generated binaries are included.
- [ ] Open a pull request; merge only after CI passes.
- [ ] State explicitly that physical-device verification still requires installing the generated APK on an Android device.
