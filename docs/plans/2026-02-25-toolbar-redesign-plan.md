# Toolbar Redesign — Instrument Hardware Style Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Remove the Material3 topBar entirely, float "BANJEN" wordmark + two pill-shaped hardware-style buttons directly over the edge-to-edge canvas, and restyle the settings bottom sheet to match the instrument palette.

**Architecture:** All changes are in `EarActivity.kt`. The `Scaffold` `topBar` is removed; a new `CanvasOverlay` composable is layered inside the existing content `Box` on top of `BanjoStringCanvas`. A new `PillIconButton` composable replaces the plain `IconButton`s. The settings `ModalBottomSheet` gets an instrument-palette coat of paint.

**Tech Stack:** Jetpack Compose, Material3, `animateFloatAsState`, `MutableInteractionSource` for press scale.

---

### Task 1: Add `PillIconButton` composable

**Files:**
- Modify: `app/src/main/java/com/makingiants/android/banjotuner/EarActivity.kt`
- Test: `app/src/androidTest/java/com/makingiants/android/banjotuner/EarActivityTest.kt`

**Step 1: Write the failing instrumented test**

Add to `EarActivityTest`:

```kotlin
@Test
fun test_settingsButtonExists() {
    composeTestRule
        .onNode(hasContentDescription("Settings"))
        .assertExists()
}
```

Run: `./gradlew connectedAndroidTest --tests "*.EarActivityTest.test_settingsButtonExists"`
Expected: **FAIL** — the existing `IconButton` uses content description from `R.string.settings_label`.
*(It may actually pass with the old button — confirm the string value matches "Settings" via `app/src/main/res/values/strings.xml` before proceeding.)*

**Step 2: Add new imports to `EarActivity.kt`**

After the existing import block, add:

```kotlin
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
```

**Step 3: Add `PillIconButton` composable**

Add this private composable at the bottom of `EarActivity.kt`, before the closing `}` of the class:

```kotlin
@Composable
private fun PillIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.92f else 1f,
        animationSpec = tween(100),
        label = "pill-scale",
    )

    Box(
        modifier =
            modifier
                .graphicsLayer(scaleX = scale, scaleY = scale)
                .clip(RoundedCornerShape(50))
                .background(Color(0xFF2A1F1A))
                .border(1.dp, Color(0xFF5C4A3E), RoundedCornerShape(50)),
        contentAlignment = Alignment.Center,
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier.size(40.dp),
            interactionSource = interactionSource,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = Color(0xFFB89A86),
                modifier = Modifier.size(20.dp),
            )
        }
    }
}
```

**Step 4: Run test to verify it passes**

Run: `./gradlew connectedAndroidTest --tests "*.EarActivityTest.test_settingsButtonExists"`
Expected: **PASS**

**Step 5: Commit**

```bash
git add app/src/main/java/com/makingiants/android/banjotuner/EarActivity.kt
git add app/src/androidTest/java/com/makingiants/android/banjotuner/EarActivityTest.kt
git commit -m "feat(UI): add PillIconButton composable — instrument hardware style"
```

---

### Task 2: Add `CanvasOverlay` composable and replace `topBar`

**Files:**
- Modify: `app/src/main/java/com/makingiants/android/banjotuner/EarActivity.kt`
- Test: `app/src/androidTest/java/com/makingiants/android/banjotuner/EarActivityTest.kt`

**Step 1: Write the failing tests**

Add to `EarActivityTest`:

```kotlin
@Test
fun test_wordmarkExists() {
    composeTestRule
        .onNodeWithText("BANJEN")
        .assertExists()
}

@Test
fun test_sessionButtonExists() {
    composeTestRule
        .onNode(hasContentDescription("Session mode"))
        .assertExists()
}
```

Run: `./gradlew connectedAndroidTest --tests "*.EarActivityTest.test_wordmarkExists"`
Expected: **FAIL** — no "BANJEN" text in the current UI

*(Verify `R.string.session_mode_label` resolves to `"Session mode"` from strings.xml before using that string in the test.)*

**Step 2: Add `CanvasOverlay` composable**

Add after `PillIconButton`, still inside the `EarActivity` class:

```kotlin
@Composable
private fun CanvasOverlay(
    isSessionActive: Boolean,
    isStringActive: Boolean,
    onSessionClick: () -> Unit,
    onSettingsClick: () -> Unit,
) {
    val overlayAlpha by animateFloatAsState(
        targetValue = if (isStringActive) 0.35f else 1f,
        animationSpec = tween(300),
        label = "overlay-alpha",
    )

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .graphicsLayer(alpha = overlayAlpha),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (!isSessionActive) {
            PillIconButton(
                icon = Icons.Filled.Headphones,
                contentDescription = stringResource(id = R.string.session_mode_label),
                onClick = onSessionClick,
            )
        } else {
            Spacer(modifier = Modifier.size(40.dp))
        }

        Text(
            text = "BANJEN",
            style =
                androidx.compose.ui.text.TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFB89A86),
                    letterSpacing = 4.sp,
                ),
        )

        PillIconButton(
            icon = Icons.Default.Settings,
            contentDescription = stringResource(id = R.string.settings_label),
            onClick = onSettingsClick,
        )
    }
}
```

**Step 3: Replace `topBar` with `CanvasOverlay` in `NormalLayout`**

In `NormalLayout`, find the `Scaffold(...)` call.

**Remove** the entire `topBar = { ... }` block:

```kotlin
// DELETE this block:
topBar = {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (!sessionModeActive.value) {
            IconButton(onClick = {
                toneGenerator.stop()
                selectedOption.intValue = -1
                sessionModeActive.value = true
            }) {
                Icon(
                    imageVector = Icons.Filled.Headphones,
                    contentDescription = stringResource(id = R.string.session_mode_label),
                    tint = colorResource(id = R.color.banjen_accent),
                )
            }
        }
        IconButton(onClick = { showSettings = true }) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = stringResource(id = R.string.settings_label),
                tint = colorResource(id = R.color.banjen_accent),
            )
        }
    }
},
```

Inside the Scaffold content `Box`, add `CanvasOverlay` as the last child (so it renders on top):

```kotlin
) { paddingValues ->
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(paddingValues),
    ) {
        // ... existing BanjoStringCanvas ...
        // ... existing ad banner Box ...

        // ADD: overlay floats over canvas
        CanvasOverlay(
            isSessionActive = sessionModeActive.value,
            isStringActive = selectedOption.intValue >= 0,
            onSessionClick = {
                toneGenerator.stop()
                selectedOption.intValue = -1
                sessionModeActive.value = true
            },
            onSettingsClick = { showSettings = true },
        )
    }
}
```

**Step 4: Build and verify no compile errors**

Run: `./gradlew assembleDebug`
Expected: **BUILD SUCCESSFUL**

**Step 5: Run all tests**

Run: `./gradlew connectedAndroidTest --tests "*.EarActivityTest"`
Expected: **All tests PASS**, including `test_wordmarkExists` and `test_sessionButtonExists`

**Step 6: Commit**

```bash
git add app/src/main/java/com/makingiants/android/banjotuner/EarActivity.kt
git add app/src/androidTest/java/com/makingiants/android/banjotuner/EarActivityTest.kt
git commit -m "feat(UI): remove topBar, overlay BANJEN wordmark + pill buttons on canvas"
```

---

### Task 3: Restyle settings bottom sheet

**Files:**
- Modify: `app/src/main/java/com/makingiants/android/banjotuner/EarActivity.kt`
- Test: `app/src/androidTest/java/com/makingiants/android/banjotuner/EarActivityTest.kt`

**Step 1: Write the failing test**

Add to `EarActivityTest`:

```kotlin
@Test
fun test_settingsSheetOpens() {
    composeTestRule
        .onNode(hasContentDescription("Settings"))
        .performClick()
    composeTestRule.waitForIdle()
    // Instrument selector is visible in the sheet
    composeTestRule
        .onNodeWithText("Banjo")
        .assertExists()
}
```

*(Replace `"Banjo"` with whatever `currentInstrument.name` resolves to on first launch — check `AppConstants.kt` or `TuningModel.kt` for the default instrument name.)*

Run: `./gradlew connectedAndroidTest --tests "*.EarActivityTest.test_settingsSheetOpens"`
Expected: **PASS** (this verifies the sheet still opens after the topBar removal; confirms the wiring is correct before restyling)

**Step 2: Restyle `ModalBottomSheet` container color**

In `NormalLayout`, find the `ModalBottomSheet(...)` call. Change `containerColor`:

```kotlin
// BEFORE:
containerColor = colorResource(id = R.color.banjen_background),

// AFTER:
containerColor = Color(0xFF1A1210),
```

**Step 3: Add a fret-line divider between instrument selectors and pitch control**

In the `Column` inside `ModalBottomSheet`, between `Spacer(modifier = Modifier.height(16.dp))` and `PitchControl(...)`:

```kotlin
// ADD before PitchControl:
Box(
    modifier =
        Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(Color(0xFF2E2420)),
)
Spacer(modifier = Modifier.height(16.dp))
```

**Step 4: Restyle `DropdownSelector` — replace `OutlinedButton` with dark pill**

In the `DropdownSelector` composable, replace the `OutlinedButton(...)` block:

```kotlin
// BEFORE:
OutlinedButton(onClick = { expanded = true }) {
    Text(
        text = label,
        style =
            MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Medium,
                color = colorResource(id = R.color.banjen_accent),
            ),
    )
    Icon(
        imageVector = Icons.Default.ArrowDropDown,
        contentDescription = null,
        tint = colorResource(id = R.color.banjen_accent),
    )
}
```

```kotlin
// AFTER:
Box(
    modifier =
        Modifier
            .clip(RoundedCornerShape(50))
            .background(Color(0xFF2A1F1A))
            .border(1.dp, Color(0xFF5C4A3E), RoundedCornerShape(50))
            .clickable { expanded = true }
            .padding(horizontal = 16.dp, vertical = 10.dp),
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = label,
            style =
                MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFB89A86),
                ),
        )
        Icon(
            imageVector = Icons.Default.ArrowDropDown,
            contentDescription = null,
            tint = Color(0xFFB89A86),
        )
    }
}
```

Add the missing import for `clickable`:

```kotlin
import androidx.compose.foundation.clickable
```

**Step 5: Build and run all tests**

Run: `./gradlew assembleDebug`
Expected: **BUILD SUCCESSFUL**

Run: `./gradlew connectedAndroidTest --tests "*.EarActivityTest"`
Expected: **All tests PASS**

**Step 6: Commit**

```bash
git add app/src/main/java/com/makingiants/android/banjotuner/EarActivity.kt
git commit -m "feat(UI): restyle settings sheet — instrument palette, dark pill dropdowns, fret divider"
```

---

## Manual Verification Checklist

After all tasks are done, install on a device and verify:

- [ ] Canvas fills edge-to-edge — no toolbar band visible at top
- [ ] "BANJEN" wordmark centered at top, warm amber color
- [ ] Session (headphones) pill on left, Settings (gear) pill on right
- [ ] Tapping a string: overlay fades to 35% opacity
- [ ] Releasing string: overlay returns to full opacity
- [ ] Session mode active: headphones button replaced by spacer (gap)
- [ ] Session mode FAB (stop) still appears bottom-right
- [ ] Settings pill opens bottom sheet
- [ ] Bottom sheet: dark wood background, fret divider line, amber pill dropdowns
- [ ] Ad banner still appears at bottom when idle, disappears when a string is active
- [ ] All existing EarActivityTest tests pass
