# Opening String Reveal Animation — Design Document
*2026-02-25*

## Problem

The app opens to a static canvas of four banjo strings with no introductory motion. The first impression is functional but cold. Every daily open for Harold (67, cognitive health routine) is a missed opportunity to create a moment of warmth and delight that reinforces his emotional attachment to the app.

## Solution

A Draw-On Reveal animation: each string is drawn progressively from nut to bridge — starting as a point of light and unrolling downward to full length. Staggered left-to-right (D3 → G3 → B3 → D4). Total duration 830ms. Plays on every app open. Immediately interruptible by any tap.

**Persona connection:** Harold M. (daily delight reinforces brain-health routine), Rafael S. (trained musician ear — physical string metaphor resonates), Marcus T. (appreciates craft detail).

**Pain addressed:** Pattern 7 — "First Experience Determines Permanent Adoption." The first 10 seconds of every session set the emotional tone.

**Design principles respected:**
- Time-to-first-tone: animation is <1s and interruptible instantly
- Tuning screen is sacred: animation does not add any delay to actual tuning
- Respect the musician: motion is musical (string-laying metaphor), not decorative tech

---

## Architecture

All changes are confined to `BanjoStringCanvas.kt`. No new files, no new composables.

### 1. `drawStringPath` — add `revealProgress` parameter

```
revealProgress: Float = 1.0f
```

The path drawing loop iterates `segments` steps from nut to bridge. When `revealProgress < 1.0`, the loop terminates at `(revealProgress * segments).toInt()`. Segments beyond that are simply not drawn. When `revealProgress == 1.0` (the steady state after animation completes), behavior is identical to today — zero overhead.

### 2. Per-string `revealProgress` Animatables

```kotlin
val revealProgress = remember { Array(NUM_STRINGS) { Animatable(0f) } }
```

Same pattern as existing `stringOpacities` and `vibrationAmplitudes`.

### 3. Launch effect (triggers on every composition)

```kotlin
LaunchedEffect(Unit) {
    for (i in 0 until NUM_STRINGS) {
        launch {
            delay(i * 150L)
            revealProgress[i].animateTo(
                targetValue = 1f,
                animationSpec = tween(380, easing = EaseOutCubic)
            )
        }
    }
}
```

All four coroutines launch in parallel; the `delay` creates the stagger.

### 4. Interrupt on tap

Inside `detectTapGestures`, before processing the tap:

```kotlin
if (revealProgress.any { it.value < 1f }) {
    revealProgress.forEach { it.snapTo(1f) }
}
```

The user never waits. Tone starts immediately.

### 5. Label alpha tied to revealProgress

The existing `drawStringLabel` call passes `alpha` derived partly from `effectiveAlpha`. During reveal, multiply by `revealProgress[i].value` so labels emerge as the string draws through them.

---

## Timing

| String | Start  | End    | Duration |
|--------|--------|--------|----------|
| D3     | 0ms    | 380ms  | 380ms    |
| G3     | 150ms  | 530ms  | 380ms    |
| B3     | 300ms  | 680ms  | 380ms    |
| D4     | 450ms  | 830ms  | 380ms    |

**Easing:** `EaseOutCubic` — fast start (crisp nut contact), slow finish (soft landing at bridge, emotional tail).

**Total wall-clock:** 830ms. Well within the 3-second time-to-first-tone budget.

---

## After Animation Completes

Once all `revealProgress` values reach `1f`, the canvas transitions seamlessly into the existing idle breathing/shimmer animations. No state machine, no flag needed — `revealProgress == 1f` is the natural steady state and costs nothing.

---

## What Does Not Change

- `EarActivity.kt` — zero changes
- All existing string animations (vibration, breathing, shimmer, spring physics)
- Ad banner behavior
- Session mode
- Any other feature

---

## Out of Scope

- Sound on reveal (no audio cue accompanies the animation — tone only on explicit tap)
- Different reveal directions (top-to-bottom is the only direction; matches string-laying metaphor)
- Per-session skip after N opens (YAGNI — user said "amaze on every open")
