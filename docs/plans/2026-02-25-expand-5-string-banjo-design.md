# Design: Expand 5-String Banjo Canvas

*February 25, 2026*

## Problem

`FIVE_STRING_BANJO` data and audio playback work correctly — selecting the instrument plays 5 notes. But `BanjoStringCanvas` hardcodes `NUM_STRINGS = 4` with 14 fixed-size arrays. Selecting a 5-string banjo always draws 4 strings.

## Solution

Pass `notes: List<Note>` into `BanjoStringCanvas`. Derive string count, labels, physics params, and animation array sizes from the notes list. The EarActivity call site requires one added parameter.

---

## Architecture

### Signature Change

```kotlin
fun BanjoStringCanvas(
    notes: List<Note>,
    selectedString: Int,
    onStringSelected: (index: Int) -> Unit,
    modifier: Modifier = Modifier,
)
```

`val numStrings = notes.size` replaces `NUM_STRINGS` everywhere in the file.

### EarActivity Call Site

One line added:

```kotlin
BanjoStringCanvas(
    notes = currentTuningModel.notes,  // NEW
    selectedString = selectedOption.intValue,
    onStringSelected = { ... },
    modifier = ...,
)
```

---

## Per-String Physics (Frequency-Based)

Each string's visual physics are derived from its normalized frequency position within the tuning:

```kotlin
val freqMin = notes.minOf { it.frequency }
val freqMax = notes.maxOf { it.frequency }
val fn = (notes[i].frequency - freqMin) / (freqMax - freqMin)
// fn = 0.0 → lowest pitch (thickest string), fn = 1.0 → highest pitch (thinnest)
```

| Parameter | fn=0 (lowest) | fn=1 (highest) | Current 4-string range |
|-----------|--------------|----------------|------------------------|
| idleThickDp | 4.0 | 2.0 | 4.0 → 2.0 |
| activeThickDp | 5.5 | 2.8 | 5.5 → 2.8 |
| vibAmpDp | 8.0 | 4.0 | 8.0 → 4.0 |
| vibFreqHz | 3.0 | 6.0 | 3.0 → 6.0 |
| vibWavePeaks | 2.5 | 4.0 | 2.5 → 4.0 |
| springStiffness | 300 | 500 | 300 → 500 |
| springDamping | 0.50 | 0.65 | 0.50 → 0.65 |
| releaseDurationMs | 400 | 220 | 400 → 220 |
| initialSharpness | 0.7 | 1.2 | 0.7 → 1.2 |
| sharpnessTransMs | 250 | 120 | 250 → 120 |
| breathingAmplitude | 0.09 | 0.04 | 0.09 → 0.04 |

For `breathingPeriodS` (incommensurate by design), use golden-ratio index stepping:
`1.5f + 1.8f * ((i.toFloat() * 0.6180339887f) % 1.0f)`

For 4-string DGBD, computed values are within ~5% of current hand-tuned values — visually indistinguishable.

**Wound string texture**: replace hardcoded `isWound = i < 2` with `notes[i].frequency < 220f`. D3 (146 Hz) and G3 (196 Hz) get wound-string micro-texture; higher strings do not.

---

## Color Palette

Expand from 4 to 5 entries. Add a warm amber-green for the g4 drone string (index 0):

```kotlin
private val stringPalette = listOf(
    StringColors(Color(0xFF5C7B58), Color(0xFF78C870), Color(0xFF78C870), Color(0xFF90B082)), // g4 drone
    StringColors(Color(0xFF8C7161), Color(0xFFD4956A), Color(0xFFD4956A), Color(0xFFB89A86)), // D3
    StringColors(Color(0xFF6B8490), Color(0xFF5AAFCB), Color(0xFF5AAFCB), Color(0xFF8EADB8)), // G3
    StringColors(Color(0xFF8C8062), Color(0xFFCBA55A), Color(0xFFCBA55A), Color(0xFFB8A882)), // B3
    StringColors(Color(0xFF907466), Color(0xFFE07850), Color(0xFFE07850), Color(0xFFC09A8A)), // D4
)
```

**Palette indexing** uses a count-based offset so 4-string colors are unchanged:

```kotlin
val paletteIdx = (i + (5 - numStrings)) % 5
```

Verification:
- 4-string: i=0→palette[1](D3 brown), i=1→palette[2](G3 blue), i=2→palette[3](B3 gold), i=3→palette[4](D4 copper) ✅
- 5-string: i=0→palette[0](g4 green), i=1→palette[1](D3 brown), ..., i=4→palette[4](D4 copper) ✅

---

## Labels and Ordinals

- **Primary label**: `notes[i].name` — no hardcoded list
- **Secondary ordinal**: `"${numStrings - i}${ordinalSuffix(numStrings - i)}"`
  - 4-string → "4th", "3rd", "2nd", "1st"
  - 5-string → "5th", "4th", "3rd", "2nd", "1st"
- Helper: `fun ordinalSuffix(n: Int) = when (n) { 1 -> "st"; 2 -> "nd"; 3 -> "rd"; else -> "th" }`

---

## Animation Arrays

All `remember { Array(NUM_STRINGS) { ... } }` become `remember(numStrings) { Array(numStrings) { ... } }`. When the instrument switches (numStrings changes), arrays are recreated and animations reset to initial state.

`touchYNorms`: `remember(numStrings) { FloatArray(numStrings) { 0.5f } }`

---

## Tap Detection

- Key: `.pointerInput(selectedString, numStrings)` — add `numStrings` so the lambda captures the updated band width
- Band width: `availableWidth / numStrings`
- Clamp: `tappedIndex.coerceIn(0, numStrings - 1)`

---

## Opening Animation

Change `LaunchedEffect(Unit)` (reveal) to `LaunchedEffect(notes)` — re-runs the staggered nut-to-bridge reveal when the instrument changes.

---

## Files Changed

| File | Change |
|------|--------|
| `BanjoStringCanvas.kt` | Major: parameterize by `notes`, replace all hardcoded array usage |
| `EarActivity.kt` | Minor: add `notes = currentTuningModel.notes` to call site |

No changes to `TuningModel.kt`, `ToneGenerator.kt`, or any test files.

---

## Success Criteria

- Selecting "5-String Banjo" shows 5 strings on the canvas
- Selecting "4-String Banjo" shows 4 strings with visually identical rendering to current
- Tapping each string plays the correct frequency
- Switching instruments triggers the staggered reveal animation
- g4 drone string is visually distinct (thinnest, fastest vibration, amber-green color)
