# DawaDiary Visual Refresh — Figma Handoff

Figma concept file: https://www.figma.com/design/nuLQ28cWFx8IeQS5iK0CM5

Branch: `feat/colorful-healthcare-ui`

## Direction
A calm, friendly, modern healthcare experience using an airy off-white background, deep teal primary actions, and mint, coral, lavender, and warm yellow accents. Use color to group content and communicate status while preserving contrast and readability.

## Mobile screen concepts
The Figma file contains four editable 390 × 844 px mobile concepts:
1. **Today** — greeting, daily-care hero with illustration, adherence summary, upcoming dose card, and add-record shortcut.
2. **Medicines** — schedule header, color-coded dose cards, taken/upcoming status chips, stock indicators, low-stock reminder, and a medication-habit tip.
3. **Records** — health-record hero, visual category cards for lab reports, prescriptions, summaries and imaging, plus scan/upload entry point.
4. **Family** — family-care hero illustration, member avatar cards, concise medicine/record summaries, and add-member action.

## Visual system guidance
- Keep backgrounds light and card surfaces white or softly tinted.
- Use rounded cards (roughly 18–24 px) and consistent spacing.
- Use strong heading hierarchy, short supporting copy, and readable touch targets.
- Use simple, consistent line/filled icons; avoid emoji as the only representation of a critical action.
- Illustrations/photos should support the task, not crowd medicine names, doses, or safety information.
- Never use color alone to convey medication status; pair it with text/icon labels.
- Keep medicine name, prescribed dose, timing, taken status, and stock distinct.
- Preserve privacy around health records and family profiles.

## Implementation checklist
- [x] Translate the visual direction into the existing Jetpack Compose screens; keep the UI data-driven rather than replacing screens with static screenshots.
- [x] Reuse the existing theme and shared UI components, including a new reusable `MdVisualHero` illustration banner.
- [ ] Confirm all displayed medicine schedules and examples come from app data, not hard-coded production values.
- [ ] Add appropriate, licensed image/illustration assets to the Android project and document their source/license.
- [ ] Check screen behavior on narrow Android devices and with larger font settings.
- [ ] Run the Gradle build and unit tests in CI; no successful run has been verified yet.
- [ ] Review contrast, accessibility labels, loading/empty/error states, and offline behavior.

## Android implementation status
The visual refresh has now been applied to the Android source on this branch. The Today, Medicines, Records, Family, and Settings screens use the shared colorful hero banner; its artwork uses bundled Material vector icons so it renders crisply offline and does not require remote image downloads. The Figma illustration/photo concepts are still design references rather than exported raster assets in the Android app.

A GitHub Actions workflow (`.github/workflows/android-ui-refresh-ci.yml`) was added to run `assembleDebug` and `testDebugUnitTest` on pushes to this branch and pull requests. At the time of this update, no successful workflow run or green commit status has been verified. The app should not be considered build-verified until CI completes successfully.
