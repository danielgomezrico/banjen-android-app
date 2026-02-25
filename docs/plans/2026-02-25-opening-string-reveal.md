# Opening String Reveal Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Each banjo string draws itself from nut to bridge on every app open, staggered 150ms apart (D3→G3→B3→D4), completing in 830ms total, with instant interrupt-on-tap.

**Architecture:** All changes confined to `BanjoStringCanvas.kt`. Add `revealProgress: Float = 1.0f` to `drawStringPath` which clips path drawing at `revealProgress * segments`. Add a per-string `Animatable(0f)` array, animate to `1f` staggered via `LaunchedEffect(Unit)`. On any tap while revealing, `snapTo(1f)` all strings via a captured `CoroutineScope` so tone starts without delay.

**Tech Stack:** Jetpack Compose Canvas, `Animatable`, `EaseOutCubic`, `rememberCoroutineScope`, Kotlin coroutines.

---

### Task 0: Set up git worktree

**Files:** none yet — worktree setup only.

**Step 1: Create the worktree from the repo root**

```bash
git worktree add ../banjen-string-reveal -b feat/opening-string-reveal
```

**Step 2: All subsequent tasks run inside the worktree**

```bash
cd ../banjen-string-reveal
```

---

### Task 1: Add `revealProgress` parameter to `drawStringPath`

**Files:**
- Modify: `app/src/main/java/com/makingiants/android/banjotuner/BanjoStringCanvas.kt`

This is a pure refactor. Default `1.0f` preserves existing behavior exactly — no visual change before the Animatables are wired up.

**Step 1: Locate the `drawStringPath` signature** (line ~469)

Current last optional params:
```kotlin
    attackProgress: Float = 1.0f,
    isWound: Boolean = false,
```

**Step 2: Add `revealProgress` between them**

```kotlin
    attackProgress: Float = 1.0f,
    revealProgress: Float = 1.0f,
    isWound: Boolean = false,
```

**Step 3: Clip the path drawing loop**

Find the loop (line ~493):
```kotlin
    for (s in 0..segments) {
```

Replace with:
```kotlin
    val revealSegments = (revealProgress * segments).toInt().coerceAtLeast(1)
    for (s in 0..revealSegments) {
```

`coerceAtLeast(1)` ensures `moveTo` is always called, preventing an empty-path edge case.

**Step 4: Build to confirm no compile errors**

```bash
./gradlew assembleDebug
```

Expected: `BUILD SUCCESSFUL`. No visual change — all three `drawStringPath` call sites pass no `revealProgress` so it defaults to `1.0f`.

**Step 5: Commit**

```bash
git add app/src/main/java/com/makingiants/android/banjotuner/BanjoStringCanvas.kt
git commit -m "refactor(BanjoStringCanvas): add revealProgress param to drawStringPath"
```

---

### Task 2: Add per-string `revealProgress` Animatables and launch stagger

**Files:**
- Modify: `app/src/main/java/com/makingiants/android/banjotuner/BanjoStringCanvas.kt`

**Step 1: Add the Animatable array**

Inside `BanjoStringCanvas`, after the existing `attackProgress` array (~line 167):

```kotlin
    // Per-string attack progress (0=biased envelope, 1=symmetric)
    val attackProgress = remember { Array(NUM_STRINGS) { Animatable(1f) } }
```

Add immediately after:

```kotlin
    // Per-string reveal progress (0=hidden, 1=fully drawn) — opening animation
    val revealProgress = remember { Array(NUM_STRINGS) { Animatable(0f) } }
```

**Step 2: Add the staggered launch effect**

After the existing `LaunchedEffect(Unit)` block for `wavePhase` (~line 180):

```kotlin
    // Opening animation: reveal each string nut-to-bridge, staggered left-to-right
    LaunchedEffect(Unit) {
        for (i in 0 until NUM_STRINGS) {
            launch {
                delay(i * 150L)
                revealProgress[i].animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 380, easing = EaseOutCubic),
                )
            }
        }
    }
```

**Step 3: Pass `revealProgress[i].value` to all three `drawStringPath` calls**

Inside the Canvas `for (i in 0 until NUM_STRINGS)` loop, there are three `drawStringPath` call sites (haze, blur, core). Add the named parameter to each:

```kotlin
                revealProgress = revealProgress[i].value,
```

Place it after `waveFreqHz = ...` and before `color = ...` in each call.

**Step 4: Verify imports at top of file**

Ensure these are present (add if missing):

```kotlin
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.tween
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
```

**Step 5: Build and install on device**

```bash
./gradlew assembleDebug && adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Open the app. You should see D3 draw itself from nut to bridge, then G3 150ms later, then B3, then D4. Each string's reveal takes 380ms with `EaseOutCubic` (fast start, soft landing at the bridge).

**Step 6: Commit**

```bash
git add app/src/main/java/com/makingiants/android/banjotuner/BanjoStringCanvas.kt
git commit -m "feat(BanjoStringCanvas): add opening string reveal animation"
```

---

### Task 3: Tie label alpha to `revealProgress`

**Files:**
- Modify: `app/src/main/java/com/makingiants/android/banjotuner/BanjoStringCanvas.kt`

Labels should emerge in sync with the string drawing through them — not appear separately.

**Step 1: Find `labelAlpha`** (inside the Canvas `for` loop, ~line 451):

```kotlin
            val labelAlpha = if (isActive) 1f else effectiveAlpha * 0.65f
```

**Step 2: Multiply by reveal progress**

```kotlin
            val labelAlpha = (if (isActive) 1f else effectiveAlpha * 0.65f) * revealProgress[i].value
```

**Step 3: Build and run on device**

Labels should now fade in in sync with their string, not flash in all at once.

**Step 4: Commit**

```bash
git add app/src/main/java/com/makingiants/android/banjotuner/BanjoStringCanvas.kt
git commit -m "feat(BanjoStringCanvas): fade labels in with string reveal progress"
```

---

### Task 4: Interrupt reveal on tap

**Files:**
- Modify: `app/src/main/java/com/makingiants/android/banjotuner/BanjoStringCanvas.kt`

If the user taps during the reveal, all strings snap to fully drawn instantly and the tap is processed normally. Harold never waits.

**Step 1: Add `rememberCoroutineScope()` at the top of `BanjoStringCanvas`**

Immediately after `val textMeasurer = rememberTextMeasurer()`:

```kotlin
    val textMeasurer = rememberTextMeasurer()
    val coroutineScope = rememberCoroutineScope()
    val infiniteTransition = rememberInfiniteTransition(label = "strings-breathe")
```

**Step 2: Snap all revealProgress values at the top of the tap handler**

Inside `detectTapGestures { offset -> }`, add as the very first lines:

```kotlin
                    detectTapGestures { offset ->
                        // Interrupt opening animation so tone starts immediately
                        if (revealProgress.any { it.value < 1f }) {
                            coroutineScope.launch {
                                revealProgress.forEach { it.snapTo(1f) }
                            }
                        }
                        // ... all existing tap logic continues unchanged below
```

**Step 3: Build and test on device — interrupt verification**

Open the app and immediately tap any string within the first 500ms. The reveal should snap to complete and the tone should start. There must be zero perceptible delay between tap and sound.

**Step 4: Commit**

```bash
git add app/src/main/java/com/makingiants/android/banjotuner/BanjoStringCanvas.kt
git commit -m "feat(BanjoStringCanvas): interrupt string reveal on tap for instant playback"
```

---

### Task 5: Run existing test suite

**Step 1: Run instrumented tests on a connected device or emulator**

```bash
./gradlew connectedAndroidTest
```

Expected: all existing tests pass. The `revealProgress` defaults to `1.0f` in all draw calls, preserving existing behavior for test assertions.

**Step 2: If any test fails, check for a naming collision**

The local `revealProgress` array inside `BanjoStringCanvas` must not shadow the parameter name in `drawStringPath`. Verify the call sites pass it as a named argument (`revealProgress = revealProgress[i].value`) to avoid ambiguity.

---

### Task 6: Merge worktree back to master and clean up

**Step 1: Switch back to the main repo**

```bash
cd ../banjen
```

**Step 2: Merge the feature branch**

```bash
git merge feat/opening-string-reveal --no-ff -m "feat: opening string reveal animation"
```

**Step 3: Remove the worktree and branch**

```bash
git worktree remove ../banjen-string-reveal
git branch -d feat/opening-string-reveal
```

**Step 4: Verify clean state**

```bash
git status
git log --oneline -6
```

Expected: clean working tree, recent commits show the four feature commits followed by the merge commit.
