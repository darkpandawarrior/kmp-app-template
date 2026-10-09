# kmp-app-template DESIGN.md (starter)

> Inherits: house design standard (AgentHarness skill `design-md`). This file wins on conflict.
> Also inherits the shared token layer: kmp-toolkit `DESIGN.md` (present in `external/kmp-toolkit`
> once its pin includes it). Spacing, sizes, shapes, elevation, motion and the adaptive ladder
> already come from `external/kmp-toolkit/designsystem`.
> This is a stub for a new app to fill in. Replace every "TBD" before shipping UI.
> Dial: ENERGY TBD / RHYTHM TBD / MOTION TBD

## Overview

The template ships no brand. `App()` in
`cmp-shared/src/composeMain/kotlin/com/siddharth/apptemplate/shared/App.kt` wraps the screens in a
stock `MaterialTheme { }` (the Material 3 default scheme, which does not switch to dark by itself)
over a Splash, Login, Home flow. A real app should state here, in two or three sentences, what it
is, who uses it, and one specific visual reference. Avoid "modern, clean, premium".

TBD (product, audience, reference).

## Colors

Currently the stock Material 3 baseline scheme; there are no custom colours in the template.
Define the app palette as a `ColorScheme` in a theme file (for example
`cmp-shared/src/composeMain/kotlin/<package>/theme/Theme.kt`) and fill this table from it with real
values only:

| M3 role | Light | Dark |
|---|---|---|
| primary | TBD | TBD |
| background | TBD | TBD |
| surface | TBD | TBD |
| error | TBD | TBD |

Rules to write once the palette exists: what primary is for (one main action per screen), what is
forbidden, and the contrast target (WCAG AA 4.5:1). Toolkit primitives read only
`MaterialTheme.colorScheme` roles, so the palette flows through them with no extra work.

## Typography

Stock `MaterialTheme.typography` today. TBD (families, which roles use which, any bundled fonts).
Toolkit primitives use headlineSmall, titleMedium, bodyMedium, bodySmall and labelSmall.

## Layout

From the toolkit: `DesignTokens.Spacing` (4dp scale), `DesignTokens.Size`, and the adaptive ladder
(`AdaptiveTheme`, `byWindow`, `byFormFactor`). Targets are Android, Desktop, iOS and Web from one
shared Compose UI. TBD for app-specific breakpoints and density.

## Elevation & Depth

Toolkit default: flat, 1dp outlined cards, floating layers only for menus and sheets. TBD if the app
departs from that.

## Shapes

Toolkit `DesignTokens.Shape` (8 to 24dp). TBD if the app defines its own `Shapes`.

## Components

Start from the toolkit primitives (`SectionCard`, `PageHeader`, `TagChip`, `LoadingState`,
`ErrorState`, `EmptyState`, `AdaptiveNavigationShell`, `ThemeController`) and the shell screens in
`App.kt`. List app-specific components here as they appear.

## Motion

Toolkit motion buckets (90, 160, 240, 360 ms) and `screenEnter` / `screenExit`. TBD for anything
beyond that.

## Do's and Don'ts

- Do define the theme once, in one file, and have this file point at it.
- Do take spacing, radius and duration from the toolkit tokens.
- Do add a dark scheme and wire `ThemeController`; the stock theme does not follow the system.
- Don't copy hex values into this file by hand without reading them from the theme file.
- Don't ship the template's stock look: replace it with a real direction.

## Agent notes

- This file is a starter. When a project is created from the template, rewrite it first, then build.
- The code wins on any value here. If a hex differs, fix this file.
- Run the `antislop` skill as the filter on any UI diff and report its Delivery Gate result.

## Changelog

| Date | Change | Why | Source |
|---|---|---|---|
| 2026-10-09 | Initial version, distilled from the code token files and existing design docs | Establish design direction for agents | DESIGN.md rollout |

## Open questions

- TBD: product, audience and visual reference.
- TBD: dial values for ENERGY, RHYTHM and MOTION.
- TBD: primary, background, surface and error colour values.
- TBD: typography families, role mapping and bundled fonts.
- TBD: app-specific breakpoints and density.
- TBD: card and floating-layer policy if the app departs from the toolkit default.
- TBD: shapes if the app defines its own Shapes.
- TBD: motion beyond the toolkit buckets.

## Evolving this file

Agents: when you change UI and find this file wrong or silent, fix it in the same change and add a Changelog row. Code token files win over this file; when they disagree, correct the doc. A user correction of a visual choice with a stated reason becomes a rule here immediately. Lessons that apply beyond this repo go to the LEARNINGS log of the `design-md` skill in AgentHarness.
