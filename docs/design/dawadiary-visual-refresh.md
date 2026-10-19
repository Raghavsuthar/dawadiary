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
- [ ] Translate approved concepts into existing Jetpack Compose screens; do not replace the current app with static screenshots.
- [ ] Reuse the existing theme and shared UI components where possible.
- [ ] Confirm all displayed medicine schedules and examples come from app data, not hard-coded production values.
- [ ] Add appropriate, licensed image/illustration assets to the Android project and document their source/license.
- [ ] Check screen behavior on narrow Android devices and with larger font settings.
- [ ] Run the available Gradle build and tests; record actual results before claiming verification.
- [ ] Review contrast, accessibility labels, loading/empty/error states, and offline behavior.

## Scope note
This file records the design handoff. Creating or editing the Figma file does not automatically modify Android source code. The app implementation and its build/test verification must be completed separately.
