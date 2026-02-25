# Toolbar Redesign — Instrument Hardware Style
*February 25, 2026*

## Problem

The app's `Scaffold` topBar creates a separate toolbar band at the top of the screen — two plain Material3 `IconButton`s floating in the top-right corner. This looks like a template app and is visually disconnected from the rich animated canvas below it. The canvas fills its content area starting *below* the toolbar, breaking the immersive instrument experience.

## Decision

Remove the `Scaffold` `topBar` entirely. Overlay three elements directly on top of the canvas — no background panel, no toolbar band. The canvas becomes edge-to-edge. Controls float over it as part of the instrument art.

The settings bottom sheet is restyled to match the instrument palette.

**Personas addressed:** Harold M. (recognizable branding), Betty L. (labeled pill buttons, generous touch targets), Marcus T. / Siobhan K. (immersive, no interruption during active tuning).

---

## Design

### Top Overlay Structure

Three elements sit in a `Box` overlaid on the canvas, padded with `windowInsetsPadding(WindowInsets.statusBars)`:

- **Left**: Session mode pill button (headphones icon)
- **Center**: "BANJEN" wordmark
- **Right**: Settings pill button (gear icon)

No background. No panel. They float directly over the canvas gradient.

### "BANJEN" Wordmark

- Typography: matches the string label style in `BanjoStringCanvas` — same warm amber (`Color(0xFFB89A86)`), letter-spacing ~4sp, `FontWeight.Medium`, ~18sp
- Feels engraved on the headstock above the strings
- No glow, no shadow — subtle, like a brand stamp on vintage instrument hardware

### Pill Buttons (Session + Settings)

- Shape: `RoundedCornerShape(50%)` — pill
- Background: `Color(0xFF2A1F1A)` — same dark wood tone as the nut/bridge bars in `BanjoStringCanvas`
- Top edge highlight: 1dp line at `Color(0xFF5C4A3E)` — same `nutBridgeHighlight` already defined in `BanjoStringCanvas`
- Icon tint: warm amber (`Color(0xFFB89A86)`)
- Size: ~40×32dp — large enough for arthritic fingers, small enough to not dominate
- Press feedback: brief scale animation to 0.92f

### Behavior During Playback

When a string is active, the entire overlay (wordmark + both buttons) fades to 35% opacity — same `DIMMED_OPACITY` constant already used for inactive strings in `BanjoStringCanvas`. Uses `tween(300, EaseOutCubic)` for consistency with existing animations.

When idle: full opacity.

This keeps the tuning screen sacred: controls recede, the playing string takes focus.

### Ad Placement

Ad banner stays at `BottomCenter`, hidden during active play (unchanged). Bottom is exclusively the ad zone. Top is exclusively the overlay zone. No overlap, no layout shift risk.

### Settings Bottom Sheet

Restyled to feel like opening a panel inside the instrument:

- `containerColor`: `Color(0xFF1A1210)` — same as canvas background gradient start
- Section dividers: thin 1dp lines at `Color(0xFF2E2420)` — same `fretColor` from `BanjoStringCanvas`
- Dropdown buttons: replace `OutlinedButton` with the same dark pill shape as the toolbar buttons
- Text: warm amber (`Color(0xFFB89A86)`) for labels, `Color(0xFF8EADB8)` for values
- Overall: feels like a panel that belongs to the instrument, not a generic Material bottom sheet

---

## What Changes

| Before | After |
|--------|-------|
| `Scaffold` `topBar` with plain `Row` + `IconButton`s | No `topBar`; overlay `Box` composable directly on canvas |
| Canvas starts below toolbar band | Canvas is edge-to-edge |
| Icons: flat Material tint, no visual treatment | Pill buttons styled as instrument hardware |
| No branding | "BANJEN" wordmark centered, headstock-style |
| Settings sheet: stock Material3 | Settings sheet: instrument palette |

## What Does NOT Change

- Canvas animations, string physics, vibration — untouched
- Ad placement logic (hides during active play) — untouched
- Session mode FAB (stop button) — untouched
- All functional behavior of settings — untouched
- Touch targets for string selection — untouched
