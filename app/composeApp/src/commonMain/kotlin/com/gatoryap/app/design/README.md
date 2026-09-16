# Design system

Seeded from the brand identity in `assets/design.pen`. That file is the source of
truth: when the identity board changes, update these tokens to match.

## Usage

Wrap UI in `GatorYapTheme` (already applied at the app root in `App.kt`), then read
tokens from `MaterialTheme` as usual:

```kotlin
Text("Yap", style = MaterialTheme.typography.headlineSmall)
Column(Modifier.padding(GatorYapTheme.spacing.medium)) { ... }
```

Colors, typography and shapes live on `MaterialTheme`. Spacing has no Material slot,
so it comes from `GatorYapTheme.spacing`.

Prefer scheme roles (`colorScheme.primary`) over `BrandColors`, which is internal —
roles are what adapt across light and dark.

## What came from the board, and what did not

Colors, the type scale's faces and tracking, and the corner radii are taken from
the identity board. Two things are derived, and are marked as such in the source:

- **The dark scheme.** The board only documents light surfaces.
- **`MutedText`.** Brand Muted (`#6B7C75`) sits at 3.9:1 on Bone, under WCAG AA;
  body text uses a darkened `#5A6B64` at 5.0:1. Brand Muted is still used for
  `outline`, where it is decorative rather than text.

`ColorContrastTest` enforces AA on the foreground/background pairs, so a mis-paired
role fails the build instead of shipping unreadable.

## Fonts

Outfit (display) and Inter (body) are bundled as variable fonts under
`composeResources/font/`, both SIL OFL 1.1 — see `app/composeApp/licenses/`.
