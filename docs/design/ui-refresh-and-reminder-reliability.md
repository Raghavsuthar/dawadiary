# DawaDiary UI Refresh & Reminder Reliability

Target branch: `feat/ui-refresh-and-reminder-reliability`
Base: `main` at `6e26aa7957e766e9b373efcdf100828b17b5395e`

## Product direction

Keep DawaDiary's existing calm teal / mint healthcare identity, but make the interface feel more polished, warm, and intentionally designed. The app is a medication and personal-health companion, so clarity and trust take priority over decoration.

## UI priorities

1. **Today dashboard**
   - Establish a clear visual hierarchy: greeting/profile, today's adherence, next dose, today's timeline, quick actions.
   - Make the next due dose the most actionable item without obscuring dose history.
   - Improve empty states for no medicines, no doses today, and completed schedules.
   - Keep adherence figures derived only from saved dose data; never show demo values.

2. **Medicines**
   - Improve scanability of medicine name, prescribed dose, timing, schedule, and stock.
   - Use consistent status chips with text and icon, never color alone.
   - Make add/edit actions and low-stock information easier to discover.
   - Avoid nested vertical scrolling; use one vertically scrolling container per screen.

3. **Records vault**
   - Make record categories and upload/scan entry points easier to understand.
   - Clarify record date, type, and associated family profile.
   - Preserve encrypted-file handling and private-by-default presentation.

4. **Family**
   - Improve member cards and active-profile selection affordance.
   - Keep emergency information prominent but visually distinct from routine actions.
   - Make profile-specific medicines, records, vitals, and appointments easy to scan.

5. **Shared design system**
   - Standardize page gutters, card radii, elevation, section spacing, icon sizing, and button heights.
   - Respect user font scaling and high-contrast settings.
   - Provide meaningful content descriptions and touch targets of at least 48dp.
   - Check narrow screens and long localized strings (English, Hindi, Gujarati).

## Reminder reliability priorities

- Explain exact-alarm access and clearly communicate when Android will use an inexact fallback.
- Review rescheduling after reboot, time changes, time-zone changes, medicine edits, and app updates.
- Keep PRN medicines free of automatic scheduled alarms.
- Preserve conditional dose-state transitions, snooze semantics, stock decrement transactionality, and notification privacy.
- Add deterministic tests for recurrence rules and permission/fallback behavior.

## Engineering constraints

- Keep Kotlin + Jetpack Compose + Room; do not migrate frameworks.
- Do not replace functional screens with static mockups or hard-coded sample data.
- Do not add a dependency unless it solves a demonstrated problem and is actively maintained with a compatible license.
- Preserve existing database schema unless a migration is necessary and tested.
- Keep changes isolated to this feature branch; do not push directly to `main`.

## Acceptance checklist

- [ ] UI changes implemented in existing Compose screens and shared components.
- [ ] All medicine/dose/record/profile data remains database-driven.
- [ ] English, Hindi, and Gujarati resources remain complete for new user-facing text.
- [ ] Accessibility semantics, font scaling, high contrast, and touch targets checked.
- [ ] Reminder edge cases covered by unit tests.
- [ ] Gradle unit tests and debug build pass in CI.
- [ ] PR includes screenshots or screen recordings for Today, Medicines, Records, and Family.
- [ ] No merge until CI is green and the changes are reviewed.
