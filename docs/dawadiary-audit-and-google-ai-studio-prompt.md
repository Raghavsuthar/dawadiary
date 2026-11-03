# DawaDiary app review and Google AI Studio prompt

**Review date:** 4 November 2026
**Repository reviewed:** [Raghavsuthar/dawadiary](https://github.com/Raghavsuthar/dawadiary)
**Baseline:** `72652fa` (`origin/main`) plus the uncommitted fixes on this review branch.
**Review method:** source-level review of the Android/Jetpack Compose app, Room data model, reminders, privacy/security, backups, resources and tests. No emulator/device visual or alarm-delivery test was available during this review.

## Executive summary

DawaDiary is an offline-first Android medicine and health-record app built with Kotlin, Jetpack Compose/Material 3 and Room. It includes family profiles, medicine schedules, dose logs, notifications, health records with encrypted attachment files, vitals, appointments, a PIN/biometric lock, export/restore and English/Hindi/Gujarati resources. The recent Stitch-inspired visual direction is a calm pale canvas with rounded cards and teal accents.

The most serious functional issues found were in medication reminders: snoozed doses could become stuck, and the scheduler originally created alarms only for the current day. Populated screens also nested vertical `LazyColumn`s inside vertical-scroll containers, which can crash at runtime. There were additional correctness and accessibility gaps: fabricated appointment timestamps, silently accepted invalid reminder times, app font preferences overriding Android’s accessibility font scale, and a tab not surviving recreation.

The current review branch contains fixes for these items plus backup-field preservation, safer dose-stock accounting, PIN masking, translation-key parity and data-erasure cleanup. These changes still require the repository’s GitHub Actions test/build run before they can be considered verified.

## Changes implemented on this review branch

- **Snooze lifecycle:** alarm delivery accepts both `PENDING` and `SNOOZED`; taking, snoozing or skipping can transition an active dose atomically. Dose stock decrement now uses the medicine ID stored on that dose, not a separately supplied ID. Regression coverage checks taking a snoozed dose, repeated actions, and stock decrement.
- **Reminder continuity:** schedule generation now plans today plus seven following days, serializes concurrent syncs in-process, and refreshes the horizon when a reminder fires, after reboot/date rollover, and after app replacement. Future snoozes are restored from their saved action time; terminal-dose alarms are canceled. Editing a medicine first cancels its active reminders and pending/snoozed dose rows while retaining completed history. The planner has recurrence/horizon unit tests.
- **Reminder-time validation:** a shared validator rejects malformed/out-of-range times such as `24:00` or `12:60`, normalizes valid times, and no longer silently substitutes an 08:00 alarm. Boundary and invalid-input tests were added.
- **Scrollable screens:** removed same-axis nested vertical lazy lists from Medicines, Records and Family when embedded in the screens’ existing vertical scrollers. This avoids the Compose infinite-height measurement crash. Rows in those sections are now composed in a regular `Column`; a future large-list optimization should make the whole screen one parent `LazyColumn` rather than reintroducing nested vertical scrolling.
- **Appointments:** users can now select a real date and time; saving a past appointment is rejected. The previous “tomorrow at the current time” placeholder is removed.
- **Accessibility/state:** the app-specific text scale now multiplies, rather than replaces, Android’s system font scale; the selected tab uses saveable state; PIN entry is masked and uses a numeric-password keyboard. White text on all three teal gradient stops now has WCAG AA contrast against the solid stop colors: approximately **5.00:1**, **6.02:1** and **6.54:1**.
- **Privacy/data correctness:** erase-all also removes orphaned tag rows and immediately clears cached scan/share images. Backups now include and restore `linkedMedicineName` and `linkedCondition`, with optional reads for older backups. English, Hindi and Gujarati resource keys are back in parity, including SOS Card and appointment/schedule validation labels.

## Important issues still needing deliberate work

These are not claimed as fixed by this branch; several are high risk and should be addressed before making strong privacy or reliability claims.

1. **Health database is not encrypted at the SQLite layer.** Room stores profiles, allergies/conditions, medicines, dose history, vitals, appointments and notes in a normal app-private SQLite database. Attachment files use encryption, but that does not mean the entire health dataset is encrypted at rest. Reconcile this with consent/settings copy before claiming “all data is encrypted.” Evaluate a Keystore-backed database-encryption migration with a tested rollback and recovery plan; do not risk existing records with an untested database swap.
2. **Restore is not atomic across database and attachment files.** A failure copying an attachment after the database transaction can leave the restored DB pointing at missing files while the previous dataset has already been replaced. Stage, verify and coordinate DB/file replacement with rollback; test injected failures.
3. **Keystore fallback can make files unreadable.** `CryptoManager` catches broad Keystore errors and can fall back to an ephemeral in-memory key. Fail closed for existing ciphertext and surface a clear error; never silently substitute a key.
4. **Existing installs may retain old seeded/sample rows.** Removing a database seeding callback does not remove rows from already-created version-1 databases. Establish provenance and use an exact, fixture-tested migration if cleanup is appropriate; never delete rows based on a person’s name alone.
5. **Language selection is still cosmetic.** The setting persists a preference but does not currently apply a per-app locale, and many labels/notifications remain hard-coded English. Implement real locale switching and complete user-facing resource coverage, or remove the nonfunctional selector until it is ready.
6. **Multiple schedules per medicine need a defined policy.** The editor displays/edits only the first schedule although the data model and reminder manager support multiple schedules. Either support editing all schedules or deliberately migrate/enforce one schedule; test backup/restore and existing multi-schedule data.
7. **Dose-log uniqueness should be enforced in the database.** The in-process mutex reduces concurrent duplicate creation from this scheduler, but a unique `(medicineId, scheduledDateTime)` constraint and a duplicate-reconciling migration would provide a durable invariant.
8. **Notification permission denial needs an explanation and recovery path.** On Android 13+, an alarm may fire while notifications are blocked. Surface permission state in onboarding/settings and link users to the appropriate system settings.
9. **Sensitive-data erasure is not secure erasure.** Clearing rows/files/cache is useful cleanup but does not securely erase SQLite pages from flash storage or destroy all cryptographic keys. Avoid promising forensic-proof erasure until encrypted storage/key lifecycle is designed and tested.
10. **Broader test gaps remain.** Add receiver/integration tests for actual alarm delivery, snooze/reboot, notification permission states, schedule edits with multiple schedules, timezone/DST behavior, data migration, and restore failure. Add Compose tests that render populated lists and verify accessibility scaling/navigation restoration.

## Single prompt to paste into Google AI Studio

```text
You are a senior Android engineer, Jetpack Compose UI designer, accessibility reviewer, and privacy-conscious health-app reviewer. Work on the existing DawaDiary Android app in this repository:
https://github.com/Raghavsuthar/dawadiary

GOAL
Improve the existing app’s UI and implementation without replacing the app, changing its product identity, losing user data, or inventing medical functionality. Review the current repository first, then make focused, tested changes directly in the existing Kotlin/Jetpack Compose project. Do not return only mockups or a generic design proposal.

If you cannot access the GitHub repository or its source files, say exactly what files/project archive you need me to provide. Do not pretend you reviewed or edited files you could not access.

PRODUCT AND TECHNICAL CONTEXT
- DawaDiary is an offline-first Android health organizer built in Kotlin with Jetpack Compose, Material 3, Room and Android notifications/alarms.
- Existing features include family profiles, medicines and recurring dose schedules, dose actions, medicine stock, health records and attachments, vitals, appointments, backup/restore, PIN/biometric lock and English/Hindi/Gujarati resources.
- Keep health and family data local unless I explicitly ask otherwise. Do not add Firebase, analytics, advertising, cloud sync, sign-in, external APIs or new data collection.
- The UI direction is the existing Stitch-inspired design: calm pale/greenish background, white rounded cards, deep teal, restrained purple accents, clear hierarchy and generous spacing. Keep the current brand and Material 3 architecture; do not replace it with a web app or a different framework.
- Intended users may be older adults or caregivers. Make controls easy to see and operate, with at least 48dp touch targets, readable text, strong focus/selected states, accessible content descriptions, TalkBack semantics and support for large Android system font settings.
- This is a record-keeping/reminder tool, not a clinician. Never recommend, change or infer a medication dose, diagnosis, treatment, or emergency decision. Do not create demo patient/medicine data or present sample data as real.

REVIEW THE CURRENT SOURCE BEFORE EDITING
1. Inspect the whole repository structure, Gradle configuration, manifest, entities/DAOs/migrations, reminder receivers, ViewModels, screens, theme, resources, backup/restore and tests. Check the latest branch/commit so you do not duplicate or overwrite recent changes.
2. Identify each defect by exact file and behavior, distinguish proven bugs from risks, and rank by patient/data safety, data-loss risk, correctness, accessibility, then visual polish.
3. Preserve existing data and APIs where possible. Never delete or migrate user health records without an explicit, reversible, fixture-tested plan. Do not seed fake personal/medical data.
4. Work in small coherent changes. Show a brief implementation plan, then make the changes. Do not stop after writing a design critique.

PRIORITY IMPLEMENTATION REQUIREMENTS
A. Medication/reminder correctness
- Maintain a clear, atomic state machine for each dose. A snoozed dose must be eligible to notify again, and the user must be able to take or snooze it after the snooze. Repeated/stale actions must not double-decrement stock or change a terminal dose. Use the dose row’s own medicine ID when changing stock.
- Ensure recurring alarms continue when the app is not opened every day. Use an idempotent rolling horizon or a well-tested next-occurrence strategy, restore alarms on boot/date rollover/app replacement, and define how schedule edits cancel/reconcile old pending and snoozed doses/alarms.
- Prevent duplicate dose logs under concurrent startup/sync. Prefer a database-enforced uniqueness invariant with a safe migration; reconcile any pre-existing duplicates without losing completed dose history.
- Validate reminder times as actual 24-hour times (00:00–23:59). Reject malformed values rather than silently dropping them or substituting a default alarm. Test boundary and invalid values.
- Preserve selected recurrence rules (daily, selected weekdays, interval, tapering, as-needed) and verify start/end dates. Handle local time, timezone changes and daylight-saving transitions explicitly; do not create duplicate or shifted alerts after a time-zone change.
- If a medicine supports multiple schedules in the database, either make all schedules visible/editable or explicitly reconcile/migrate to one schedule; never leave hidden active reminders.
- Explain denied notification permission and provide a settings recovery path. Keep exact-alarm and notification permission states distinct.

B. UI, design and accessibility
- Fix same-axis nested scrolling safely. Do not put a vertical LazyColumn inside a verticalScroll container. For long datasets use one parent LazyColumn with headers/sections as items; do not replace scalable lists with huge eagerly composed lists as a permanent shortcut.
- Use one consistent spacing, typography, shape, elevation and icon system based on the existing theme. Keep the design calm and visually coherent across Today, Medicines, Records, Family and Settings.
- Verify accessible color contrast for every text/gradient/status combination in normal and high-contrast modes. White text requires at least 4.5:1 for ordinary text; choose dark-enough gradient stops or another text color.
- Preserve Android system font scaling when applying any app-level font-size preference; test large system font size plus each app font-size choice.
- Persist the current navigation destination across activity recreation/process restoration.
- Replace invented appointment timestamps with real date and time selection, validate future appointments, and render the saved local date/time accurately.
- Use minimum 48dp interactive targets, clear labels, and no color-only meaning. Check dialogs, chips, tab navigation, icons, destructive actions and forms.

C. Privacy, backup and data integrity
- Audit all privacy/consent claims against what is actually encrypted. Room currently appears to store health rows in ordinary SQLite while attachment files have separate encryption. Do not claim all health data is encrypted unless that is actually implemented and migration/recovery is tested.
- Treat a database-encryption upgrade as a sensitive migration: design, back up, validate and test rollback before changing production data.
- Make backup/restore preserve every entity field and attachment. A failed attachment copy/decryption must not destroy the previous database or leave references to missing files. Stage and validate before committing; add failure-injection tests.
- Do not silently fall back to ephemeral encryption keys when Keystore fails. Fail closed and explain recovery options without overwriting ciphertext.
- Make erase-all remove rows, tags, cached plaintext scans, encrypted attachments and relevant key material as appropriate; report the limitations of flash/storage deletion honestly. Keep DB operations transactional.
- Ensure old backup formats remain readable when adding optional fields. Test round trips with real records, linked fields, tags and attachments.
- If an old app version seeded sample rows, use a narrowly scoped migration based on exact fingerprints and a realistic v1 database fixture. Never delete by a generic patient name.

D. Localization and finish
- Make English/Hindi/Gujarati selection actually change the app locale, or remove/disable the selector until it works. Move visible labels, errors, confirmations and notification text into resources; keep translation keys complete across languages.
- Avoid static text, sample metrics, fake health data, TODO placeholders, misleading buttons, silent fallbacks and inaccurate “encrypted/atomic” claims.
- Make empty/loading/error/success states clear and non-alarming. Destructive actions need a clear confirmation and accurate impact statement.

TESTING AND ACCEPTANCE CRITERIA
Add or update tests with each behavior change, not just screenshot checks:
1. Pending → snoozed → snooze notification eligible → taken; stock decremented exactly once. Repeated take/skip/snooze and terminal statuses are safely ignored.
2. Multiple schedule syncs produce one dose per medicine/time. A rolling horizon contains future reminders and extends without opening the UI; reboot/date rollover restores the next alarms.
3. Editing a schedule removes obsolete pending/snoozed doses and alarms but retains taken/skipped history; test multiple schedules explicitly.
4. Valid times 00:00 and 23:59 pass; invalid 24:00, 12:60, malformed and blank entries are rejected.
5. Appointment date/time entered is exactly what is saved; past times are rejected.
6. Render populated Medicines, Records, Family vitals and appointments without a Compose nested-scroll crash.
7. Verify app/system text scaling compose rather than replace each other; selected tab survives recreation.
8. Verify contrast ratios, 48dp touch sizes, TalkBack semantics and real locale changes.
9. Backup/restore round-trip preserves all health data and files; injected attachment failure leaves old data intact.
10. Test production-database migrations with realistic fixtures and verify unrelated user data is unchanged.
11. Run the repository’s full unit-test task and debug/release build using the project’s existing CI/toolchain. If any task cannot run, state the exact reason and do not claim it passed.

DELIVERABLES
- Apply the focused changes to the existing source, not a standalone sample app.
- Provide a concise list of files changed, bugs fixed, remaining risks and any migration/privacy decisions that still need owner approval.
- Report exact test/build commands and pass/fail output. Do not claim emulator, visual, alarm-delivery or security testing unless it was actually performed.
- Provide a clean diff/patch and keep unrelated files/formatting untouched.
- Before concluding, verify no user data is being reset, no reminder action was weakened, all new resource keys exist in all offered locales, and every claim in UI/consent copy is accurate.
```

## Verification status

Source hygiene (`git diff --check`), English/Hindi/Gujarati string-key parity and gradient-stop contrast calculations passed locally. This sandbox has no configured Android SDK and the checked-out Gradle wrapper is not executable directly, so local Android compilation/tests were not run. The branch’s GitHub Actions workflow includes unit tests and Android debug/release build tasks; use its result as the authoritative compilation/test check before merging.