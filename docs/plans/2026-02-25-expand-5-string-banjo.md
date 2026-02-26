# Expand 5-String Banjo Canvas — Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Make `BanjoStringCanvas` accept `notes: List<Note>` so selecting a 5-string instrument draws 5 strings with correct physics and labels.

**Architecture:** Add `notes: List<Note>` param to `BanjoStringCanvas`; replace the 14 hardcoded arrays with inline computation derived from each note's frequency. Expand the color palette from 4 to 5 entries using an offset formula so 4-string rendering is unchanged. EarActivity gains one new argument at the call site.

**Tech Stack:** Kotlin, Jetpack Compose Canvas, `Animatable`, `LaunchedEffect`

---

### Precheck: understand existing dead code

The following are **dead code** — defined in `EarActivity.kt` but never called. Do NOT remove them in this task (out of scope), but be aware they exist:
- `NoteButton` composable (`EarActivity.kt:842`)
- `buttonsSubtitle` and `buttonsDescription` lists (`EarActivity.kt:156-169`)

The instrumented tests `test_onClick_ifUnselected_playSound`, `test_onClick_ifSelected_stopSound`, `test_buttonsShowSubtitleText`, and `test_buttonsHaveContentDescriptions` rely on this dead code and are currently failing. They are **out of scope** — do not fix them in this task.

---

### Task 1: Add `notes` parameter to `BanjoStringCanvas` + update call site

**Files:**
- Modify: `app/src/main/java/com/makingiants/android/banjotuner/BanjoStringCanvas.kt`
- Modify: `app/src/main/java/com/makingiants/android/banjotuner/EarActivity.kt:371`

**Step 1: Add `notes: List<Note>` as the first parameter of `BanjoStringCanvas`**

In `BanjoStringCanvas.kt`, change the `@Composable` function signature:

```kotlin
// BEFORE
@Composable
fun BanjoStringCanvas(
    selectedString: Int,
    onStringSelected: (index: Int) -> Unit,
    modifier: Modifier = Modifier,
)

// AFTER
@Composable
fun BanjoStringCanvas(
    notes: List<Note>,
    selectedString: Int,
    onStringSelected: (index: Int) -> Unit,
    modifier: Modifier = Modifier,
)
```

The `notes` parameter is now accepted but not yet used — the existing hardcoded `NUM_STRINGS = 4` logic is still in place. The code will compile.

**Step 2: Update the call site in EarActivity**

Find the `BanjoStringCanvas(` call at approximately line 371 in `EarActivity.kt`. Add `notes = currentTuningModel.notes` as the first argument:

```kotlin
// BEFORE
BanjoStringCanvas(
    selectedString = selectedOption.intValue,
    onStringSelected = { index ->

// AFTER
BanjoStringCanvas(
    notes = currentTuningModel.notes,
    selectedString = selectedOption.intValue,
    onStringSelected = { index ->
```

**Step 3: Verify it compiles**

```bash
./gradlew assembleDebug
```

Expected: `BUILD SUCCESSFUL`. No behavior change yet.

**Step 4: Commit**

```bash
git add app/src/main/java/com/makingiants/android/banjotuner/BanjoStringCanvas.kt \
        app/src/main/java/com/makingiants/android/banjotuner/EarActivity.kt
git commit -m "feat: add notes param to BanjoStringCanvas (wiring only)"
```

---

### Task 2: Add helpers and expand color palette

**Files:**
- Modify: `app/src/main/java/com/makingiants/android/banjotuner/BanjoStringCanvas.kt`

This task adds pure helpers and data structures. No behavior change yet.

**Step 1: Add `lerp` helper after the existing imports/constants block**

Add this private function near the bottom of the file (above `lerpColor`):

```kotlin
private fun lerp(start: Float, end: Float, fraction: Float): Float =
    start + (end - start) * fraction.coerceIn(0f, 1f)
```

**Step 2: Add `ordinalSuffix` helper**

Add next to `lerp`:

```kotlin
private fun ordinalSuffix(n: Int): String = when (n) { 1 -> "st"; 2 -> "nd"; 3 -> "rd"; else -> "th" }
```

**Step 3: Add `StringPhysics` data class**

Add before the `BanjoStringCanvas` function:

```kotlin
private data class StringPhysics(
    val idleThickDp: Float,
    val activeThickDp: Float,
    val vibAmpDp: Float,
    val vibFreqHz: Float,
    val vibWavePeaks: Float,
    val springStiffness: Float,
    val springDamping: Float,
    val releaseDurationMs: Int,
    val initialSharpness: Float,
    val sharpnessTransMs: Int,
    val breathingAmplitude: Float,
    val breathingPeriodS: Float,
    val isWound: Boolean,
)

private fun computeStringPhysics(notes: List<Note>): List<StringPhysics> {
    val freqMin = notes.minOf { it.frequency }
    val freqMax = notes.maxOf { it.frequency }
    val golden = 0.6180339887f
    return notes.mapIndexed { i, note ->
        val fn = if (freqMax > freqMin) (note.frequency - freqMin) / (freqMax - freqMin) else 0.5f
        StringPhysics(
            idleThickDp       = lerp(4.0f, 2.0f, fn),
            activeThickDp     = lerp(5.5f, 2.8f, fn),
            vibAmpDp          = lerp(8.0f, 4.0f, fn),
            vibFreqHz         = lerp(3.0f, 6.0f, fn),
            vibWavePeaks      = lerp(2.5f, 4.0f, fn),
            springStiffness   = lerp(300f, 500f, fn),
            springDamping     = lerp(0.50f, 0.65f, fn),
            releaseDurationMs = lerp(400f, 220f, fn).toInt(),
            initialSharpness  = lerp(0.7f, 1.2f, fn),
            sharpnessTransMs  = lerp(250f, 120f, fn).toInt(),
            breathingAmplitude = lerp(0.09f, 0.04f, fn),
            breathingPeriodS  = 1.5f + 1.8f * ((i.toFloat() * golden) % 1.0f),
            isWound           = note.frequency < 220f,
        )
    }
}
```

**Step 4: Expand `stringPalette` to 5 entries**

Replace the existing 4-entry `stringPalette` with a 5-entry version. The new entry at index 0 is for the g4 drone string. Existing entries shift to indices 1–4:

```kotlin
// BEFORE (4 entries, index 0 = D3)
private val stringPalette =
    listOf(
        StringColors(Color(0xFF8C7161), Color(0xFFD4956A), Color(0xFFD4956A), Color(0xFFB89A86)), // D3
        StringColors(Color(0xFF6B8490), Color(0xFF5AAFCB), Color(0xFF5AAFCB), Color(0xFF8EADB8)), // G3
        StringColors(Color(0xFF8C8062), Color(0xFFCBA55A), Color(0xFFCBA55A), Color(0xFFB8A882)), // B3
        StringColors(Color(0xFF907466), Color(0xFFE07850), Color(0xFFE07850), Color(0xFFC09A8A)), // D4
    )

// AFTER (5 entries, index 0 = g4 drone, indices 1-4 = existing D3-D4 colors)
private val stringPalette =
    listOf(
        StringColors(Color(0xFF5C7B58), Color(0xFF78C870), Color(0xFF78C870), Color(0xFF90B082)), // g4 drone
        StringColors(Color(0xFF8C7161), Color(0xFFD4956A), Color(0xFFD4956A), Color(0xFFB89A86)), // D3
        StringColors(Color(0xFF6B8490), Color(0xFF5AAFCB), Color(0xFF5AAFCB), Color(0xFF8EADB8)), // G3
        StringColors(Color(0xFF8C8062), Color(0xFFCBA55A), Color(0xFFCBA55A), Color(0xFFB8A882)), // B3
        StringColors(Color(0xFF907466), Color(0xFFE07850), Color(0xFFE07850), Color(0xFFC09A8A)), // D4
    )
```

**The offset formula** keeps 4-string colors unchanged:
```
paletteIdx = (i + (5 - numStrings)) % 5
// 4-string: i=0→1(D3), i=1→2(G3), i=2→3(B3), i=3→4(D4)  ← unchanged ✅
// 5-string: i=0→0(g4), i=1→1(D3), i=2→2(G3), i=3→3(B3), i=4→4(D4) ✅
```

**Step 5: Verify it compiles**

```bash
./gradlew assembleDebug
```

Expected: `BUILD SUCCESSFUL`. Still no behavior change.

**Step 6: Commit**

```bash
git add app/src/main/java/com/makingiants/android/banjotuner/BanjoStringCanvas.kt
git commit -m "feat: add StringPhysics, lerp, ordinalSuffix helpers; expand palette to 5 entries"
```

---

### Task 3: Derive `numStrings` and wire physics into animation logic

**Files:**
- Modify: `app/src/main/java/com/makingiants/android/banjotuner/BanjoStringCanvas.kt`

This is the main behavioral change. The hardcoded `NUM_STRINGS = 4` arrays are replaced by dynamic computation.

**Step 1: Replace `NUM_STRINGS` constant and derive `numStrings` inside the composable**

Remove the `private const val NUM_STRINGS = 4` line (around line 100).

At the top of the `BanjoStringCanvas` composable body, add:

```kotlin
val numStrings = notes.size
val physics = remember(notes) { computeStringPhysics(notes) }
```

**Step 2: Replace ALL animation `remember` blocks that use `NUM_STRINGS`**

Find these 6 blocks (starting around line 147) and update them. The key change: add `numStrings` as a `remember` key so arrays resize when instrument switches.

```kotlin
// BEFORE
val vibrationAmplitudes = remember { Array(NUM_STRINGS) { Animatable(0f) } }
val colorFactors = remember { Array(NUM_STRINGS) { Animatable(0f) } }
val stringOpacities = remember { Array(NUM_STRINGS) { Animatable(1f) } }
val waveSharpness = remember { Array(NUM_STRINGS) { Animatable(3.0f) } }
val attackProgress = remember { Array(NUM_STRINGS) { Animatable(1f) } }
val revealProgress = remember { Array(NUM_STRINGS) { Animatable(0f) } }
val touchYNorms = remember { FloatArray(NUM_STRINGS) { 0.5f } }

// AFTER
val vibrationAmplitudes = remember(numStrings) { Array(numStrings) { Animatable(0f) } }
val colorFactors = remember(numStrings) { Array(numStrings) { Animatable(0f) } }
val stringOpacities = remember(numStrings) { Array(numStrings) { Animatable(1f) } }
val waveSharpness = remember(numStrings) { Array(numStrings) { Animatable(3.0f) } }
val attackProgress = remember(numStrings) { Array(numStrings) { Animatable(1f) } }
val revealProgress = remember(numStrings) { Array(numStrings) { Animatable(0f) } }
val touchYNorms = remember(numStrings) { FloatArray(numStrings) { 0.5f } }
```

**Step 3: Update the opening reveal `LaunchedEffect`**

Change from `LaunchedEffect(Unit)` to `LaunchedEffect(numStrings)` so the reveal re-runs when switching between 4-string and 5-string instruments. Change the loop bound from `NUM_STRINGS` to `numStrings`:

```kotlin
// BEFORE
LaunchedEffect(Unit) {
    for (i in 0 until NUM_STRINGS) {

// AFTER
LaunchedEffect(numStrings) {
    for (i in 0 until numStrings) {
```

**Step 4: Update the `selectedString` reaction `LaunchedEffect` loops**

Two loops currently use `for (i in 0 until NUM_STRINGS)`. Change both to `numStrings`. The physics arrays `springStiffness[i]`, `springDamping[i]`, etc. are replaced with `physics[i].springStiffness`, `physics[i].springDamping`, etc.:

```kotlin
// BEFORE
for (i in 0 until NUM_STRINGS) {
    LaunchedEffect(selectedString, i) {
        if (selectedString == i) {
            launch { vibrationAmplitudes[i].animateTo(1f, spring(stiffness = springStiffness[i], dampingRatio = springDamping[i])) }
            val sharpnessOffset = when (tapZone.intValue) { 0 -> 0.05f; 2 -> 0.10f; else -> 0f }
            launch {
                waveSharpness[i].snapTo(initialSharpness[i] + sharpnessOffset)
                waveSharpness[i].animateTo(3.0f, tween(sharpnessTransMs[i], easing = EaseOutCubic))
            }
            launch {
                attackProgress[i].snapTo(0f)
                attackProgress[i].animateTo(1f, tween(300, easing = EaseOutCubic))
            }
            colorFactors[i].animateTo(1f, tween(200, easing = EaseOutCubic))
        } else if (selectedString >= 0) {
            val easing = if (i < 2) EaseOutCubic else EaseOutQuad
            vibrationAmplitudes[i].animateTo(0f, tween(releaseDurationMs[i], easing = easing))
            colorFactors[i].animateTo(0f, tween(350, easing = EaseOutCubic))
            stringOpacities[i].animateTo(DIMMED_OPACITY, tween(300, easing = EaseOutCubic))
        } else {
            val easing = if (i < 2) EaseOutCubic else EaseOutQuad
            vibrationAmplitudes[i].animateTo(0f, tween(releaseDurationMs[i], easing = easing))
            colorFactors[i].animateTo(0f, tween(350, easing = EaseOutCubic))
            stringOpacities[i].animateTo(1f, tween(300, easing = EaseOutCubic))
        }
    }

    // Keep active string at full opacity
    LaunchedEffect(selectedString, i) {
        if (selectedString == i) {
            stringOpacities[i].animateTo(1f, tween(200, easing = EaseOutCubic))
        }
    }
}

// AFTER
for (i in 0 until numStrings) {
    val p = physics[i]
    LaunchedEffect(selectedString, i) {
        if (selectedString == i) {
            launch { vibrationAmplitudes[i].animateTo(1f, spring(stiffness = p.springStiffness, dampingRatio = p.springDamping)) }
            val sharpnessOffset = when (tapZone.intValue) { 0 -> 0.05f; 2 -> 0.10f; else -> 0f }
            launch {
                waveSharpness[i].snapTo(p.initialSharpness + sharpnessOffset)
                waveSharpness[i].animateTo(3.0f, tween(p.sharpnessTransMs, easing = EaseOutCubic))
            }
            launch {
                attackProgress[i].snapTo(0f)
                attackProgress[i].animateTo(1f, tween(300, easing = EaseOutCubic))
            }
            colorFactors[i].animateTo(1f, tween(200, easing = EaseOutCubic))
        } else if (selectedString >= 0) {
            val easing = if (i < numStrings / 2) EaseOutCubic else EaseOutQuad
            vibrationAmplitudes[i].animateTo(0f, tween(p.releaseDurationMs, easing = easing))
            colorFactors[i].animateTo(0f, tween(350, easing = EaseOutCubic))
            stringOpacities[i].animateTo(DIMMED_OPACITY, tween(300, easing = EaseOutCubic))
        } else {
            val easing = if (i < numStrings / 2) EaseOutCubic else EaseOutQuad
            vibrationAmplitudes[i].animateTo(0f, tween(p.releaseDurationMs, easing = easing))
            colorFactors[i].animateTo(0f, tween(350, easing = EaseOutCubic))
            stringOpacities[i].animateTo(1f, tween(300, easing = EaseOutCubic))
        }
    }

    // Keep active string at full opacity
    LaunchedEffect(selectedString, i) {
        if (selectedString == i) {
            stringOpacities[i].animateTo(1f, tween(200, easing = EaseOutCubic))
        }
    }
}
```

Note: `if (i < 2)` → `if (i < numStrings / 2)` — preserves the "lower strings use EaseOutCubic, upper strings use EaseOutQuad" split, generalized for any count.

**Step 5: Verify it compiles**

```bash
./gradlew assembleDebug
```

Expected: `BUILD SUCCESSFUL`.

**Step 6: Commit**

```bash
git add app/src/main/java/com/makingiants/android/banjotuner/BanjoStringCanvas.kt
git commit -m "feat: wire numStrings + StringPhysics into animation arrays and LaunchedEffects"
```

---

### Task 4: Update Canvas draw loop — strings, labels, tap detection

**Files:**
- Modify: `app/src/main/java/com/makingiants/android/banjotuner/BanjoStringCanvas.kt`

Replace all remaining `NUM_STRINGS` references inside the `Canvas {}` block and the `pointerInput` lambda.

**Step 1: Fix tap detection in `pointerInput`**

Change the `pointerInput` key and all `NUM_STRINGS` references inside it:

```kotlin
// BEFORE
modifier.pointerInput(selectedString) {
    detectTapGestures { offset ->
        ...
        val bandWidth = availableWidth / NUM_STRINGS
        val relX = offset.x - hPad
        if (relX < 0 || relX > availableWidth) return@detectTapGestures
        val tappedIndex = (relX / bandWidth).toInt().coerceIn(0, NUM_STRINGS - 1)

// AFTER
modifier.pointerInput(selectedString, numStrings) {
    detectTapGestures { offset ->
        ...
        val bandWidth = availableWidth / numStrings
        val relX = offset.x - hPad
        if (relX < 0 || relX > availableWidth) return@detectTapGestures
        val tappedIndex = (relX / bandWidth).toInt().coerceIn(0, numStrings - 1)
```

**Step 2: Fix the string draw loop**

Replace `for (i in 0 until NUM_STRINGS)` inside the `Canvas {}` block with `for (i in 0 until numStrings)`.

Inside the loop, replace all per-string hardcoded array lookups with `physics[i].*` and note-derived values:

```kotlin
// BEFORE (inside Canvas draw loop)
val availableWidth = w - 2 * hPad
val bandWidth = availableWidth / NUM_STRINGS

for (i in 0 until NUM_STRINGS) {
    val centerX = hPad + bandWidth * (i + 0.5f)
    val palette = stringPalette[i]
    ...
    val idleThick = idleThicknessDp[i] * density
    val activeThick = activeThicknessDp[i] * density
    ...
    val maxAmplitudePx = vibrationAmplitudeDp[i] * density * vibAmp
    ...
    val breathFactor = if (isActive) {
        1f + breathingAmplitude[i] * sin(2f * PI.toFloat() * wavePhase / breathingPeriodS[i])
    } else { 1f }
    ...
    drawStringPath(
        ...
        wavePeaks = vibrationWavePeaks[i],
        waveFreqHz = vibrationFrequencyHz[i],
        ...
        isWound = i < 2,
    )
    ...
    drawStringLabel(
        ...
        primary = stringLabels[i],
        secondary = stringOrdinals[i],
        ...
    )

// AFTER (inside Canvas draw loop)
val availableWidth = w - 2 * hPad
val bandWidth = availableWidth / numStrings

for (i in 0 until numStrings) {
    val centerX = hPad + bandWidth * (i + 0.5f)
    val paletteIdx = (i + (5 - numStrings)) % 5
    val palette = stringPalette[paletteIdx]
    val p = physics[i]
    ...
    val idleThick = p.idleThickDp * density
    val activeThick = p.activeThickDp * density
    ...
    val maxAmplitudePx = p.vibAmpDp * density * vibAmp
    ...
    val breathFactor = if (isActive) {
        1f + p.breathingAmplitude * sin(2f * PI.toFloat() * wavePhase / p.breathingPeriodS)
    } else { 1f }
    ...
    drawStringPath(
        ...
        wavePeaks = p.vibWavePeaks,
        waveFreqHz = p.vibFreqHz,
        ...
        isWound = p.isWound,
    )
    ...
    val ordinal = "${numStrings - i}${ordinalSuffix(numStrings - i)}"
    drawStringLabel(
        ...
        primary = notes[i].name,
        secondary = ordinal,
        ...
    )
```

**Step 3: Remove all now-unused hardcoded array declarations**

Delete these lines from the top of the file (the private val arrays that are now replaced by computed physics):

```kotlin
// DELETE these lines:
private val stringLabels = listOf("D3", "G3", "B3", "D4")
private val stringOrdinals = listOf("4th", "3rd", "2nd", "1st")
private val idleThicknessDp = floatArrayOf(4.0f, 3.2f, 2.6f, 2.0f)
private val activeThicknessDp = floatArrayOf(5.5f, 4.5f, 3.6f, 2.8f)
private val vibrationAmplitudeDp = floatArrayOf(8.0f, 6.5f, 5.0f, 4.0f)
private val vibrationFrequencyHz = floatArrayOf(3.0f, 4.0f, 5.5f, 6.0f)
private val vibrationWavePeaks = floatArrayOf(2.5f, 3.0f, 3.5f, 4.0f)
private val springStiffness = floatArrayOf(300f, 350f, 400f, 500f)
private val springDamping = floatArrayOf(0.50f, 0.55f, 0.60f, 0.65f)
private val releaseDurationMs = intArrayOf(400, 350, 300, 220)
private val initialSharpness = floatArrayOf(0.7f, 0.8f, 1.0f, 1.2f)
private val sharpnessTransMs = intArrayOf(250, 200, 160, 120)
private val breathingAmplitude = floatArrayOf(0.09f, 0.08f, 0.05f, 0.04f)
private val breathingPeriodS = floatArrayOf(2.8f, 2.4f, 3.2f, 1.8f)
private const val NUM_STRINGS = 4
```

**Step 4: Run the build**

```bash
./gradlew assembleDebug
```

Expected: `BUILD SUCCESSFUL`. If any compiler errors about missing references, they will be due to a missed `NUM_STRINGS` or array reference — search the file for the old names and fix.

**Step 5: Run ktlint**

```bash
make format
```

**Step 6: Commit**

```bash
git add app/src/main/java/com/makingiants/android/banjotuner/BanjoStringCanvas.kt
git commit -m "feat: BanjoStringCanvas now derives all params from notes — supports any string count"
```

---

### Task 5: Verification

**Step 1: Run release build (final check)**

```bash
make build
```

Expected: `BUILD SUCCESSFUL`

**Step 2: Run unit tests**

```bash
make test
```

Expected: `BUILD SUCCESSFUL` with all tests passing. (The instrumented tests that use `NoteButton` test tags are pre-existing failures and are out of scope.)

**Step 3: Manual test on device**

Install and launch:

```bash
make run
```

Verify the following manually:

| Action | Expected |
|--------|----------|
| Open app — default 4-String Banjo | Canvas shows 4 strings |
| Labels visible on canvas | D3/4th, G3/3rd, B3/2nd, D4/1st |
| Tap each of the 4 strings | String animates + tone plays |
| Open Settings → select 5-String Banjo → Open G | Canvas animates the staggered reveal showing 5 strings |
| Labels visible for 5-string | g4/5th, D3/4th, G3/3rd, B3/2nd, D4/1st |
| g4 (leftmost) string | Amber-green color, thinnest, fastest vibration |
| Tap g4 string | Plays 392 Hz (g4 tone) |
| Tap D3 string (2nd from left) | Plays 146.83 Hz (D3 tone) |
| Switch back to 4-String Banjo | Canvas shows 4 strings again, same colors as before |
| 4-string colors after round-trip | D3 = warm brown, G3 = blue, B3 = gold, D4 = copper (unchanged) |

**Step 4: Commit final state if any formatting fixes were needed**

```bash
git status
# If any changes from make format:
git add -u
git commit -m "style: ktlint formatting after 5-string canvas refactor"
```

---

## Summary of changes

| File | Lines changed | Nature |
|------|:---:|---|
| `BanjoStringCanvas.kt` | ~100 | Remove 14 hardcoded arrays; add `notes` param, `StringPhysics`, `computeStringPhysics`, `lerp`, `ordinalSuffix`; replace all fixed-size logic with `numStrings`-derived logic |
| `EarActivity.kt` | 1 | Add `notes = currentTuningModel.notes` at BanjoStringCanvas call site |

No changes to `TuningModel.kt`, `ToneGenerator.kt`, tests, or any other file.
