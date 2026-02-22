# Plan: Add Session Mode with Earbud-Optimized Discreet Tuning

## MCP Validation

| Lib | Ver | Source | Status |
|-----|-----|--------|--------|
| `MediaPlayer.setVolume(float, float)` | API 1+ | Android SDK | Confirmed: 0.0f-1.0f per-player volume |
| `AudioManager.getDevices()` | API 23+ | Android SDK | Confirmed: returns AudioDeviceInfo[] |
| `AudioDeviceInfo.TYPE_*` | API 23+ | Android SDK | Confirmed: headphone/BT type constants |
| Jetpack Compose `LaunchedEffect` | Compose 1.0+ | BOM 2026.02.00 in project | Confirmed |
| Material3 `darkColorScheme` | Material3 1.0+ | BOM 2026.02.00 in project | Confirmed |

No third-party dependencies needed.

## Architecture

```
┌──────────────────────────────────────────────────────────┐
│                    EarActivity                            │
│  ┌─────────────────────────────────────────────────────┐ │
│  │ sessionModeActive: MutableState<Boolean> = false    │ │
│  └──────────────┬──────────────────────────────────────┘ │
│                 │                                        │
│     ┌───────────▼────────────┐                           │
│     │  if (!sessionMode)     │                           │
│     │  NormalLayout()        │ ← existing 4 buttons + ad │
│     └────────────────────────┘                           │
│                                                          │
│     ┌───────────▼────────────┐                           │
│     │  if (sessionMode)      │                           │
│     │  SessionModeLayout()   │ ← dark, auto-advance     │
│     │  ┌──────────────────┐  │                           │
│     │  │ Volume slider    │  │                           │
│     │  │ Current string   │  │                           │
│     │  │ Progress dots    │  │                           │
│     │  │ Stop button      │  │                           │
│     │  └──────────────────┘  │                           │
│     └────────────────────────┘                           │
│                                                          │
│  ┌────────────────────────────┐                          │
│  │ SoundPlayer                │                          │
│  │ .volume = 0.3f (session)   │                          │
│  │ .playWithLoop(index)       │                          │
│  └────────────────────────────┘                          │
└──────────────────────────────────────────────────────────┘
```

## Interface Contract

```kotlin
// SoundPlayer additions
class SoundPlayer(context: Context) {
    var volume: Float = 1.0f              // NEW: per-player volume
    fun playWithLoop(index: Int)           // EXISTING: now applies volume
    fun isHeadphoneConnected(): Boolean    // NEW: checks AudioDeviceInfo
}

// Auto-advance state
data class SessionState(
    val currentStringIndex: Int,    // 0-3, which string is playing
    val isRunning: Boolean,         // auto-advance active
    val secondsPerString: Int = 5,  // configurable duration
)

// SharedPreferences
const val KEY_SESSION_VOLUME = "session_volume"
const val DEFAULT_SESSION_VOLUME = 0.3f
```

---

## M1: SoundPlayer Volume Control + Headphone Detection + Tests
┌─────────────────────────────────────────┐
│ ANALOGY: Volume dial on headphone amp   │
│ [🏗️ Fixed volume]═══[🚀 Adjustable]    │
│ VALUE: SoundPlayer supports per-player  │
│ volume and can detect headphones        │
│ PROGRESS: [████░░░░░░] 35%             │
└─────────────────────────────────────────┘

### Changes

**New file: `app/src/test/java/com/makingiants/android/banjotuner/SessionModeTest.kt`**
- Test `clampVolume()`: 0.0f -> 0.0f, 1.0f -> 1.0f, -0.5f -> 0.0f, 1.5f -> 1.0f
- Test `autoAdvanceNextIndex()`: 0->1, 1->2, 2->3, 3->null (finished)
- Test `formatStringLabel()`: index 0-3 maps to correct string names

**Modified: `SoundPlayer.kt`**
- Add `var volume: Float = 1.0f` field
- Modify `playWithLoop()`: change `setVolume(1.0f, 1.0f)` to `setVolume(volume, volume)`
- Add `fun isHeadphoneConnected(): Boolean` using `audioManager.getDevices(AudioDeviceInfo.GET_DEVICES_OUTPUTS)` checking for headphone/BT types

**New top-level functions in SoundPlayer.kt:**
- `fun clampVolume(v: Float): Float = v.coerceIn(0.0f, 1.0f)`
- `fun autoAdvanceNextIndex(current: Int): Int? = if (current < 3) current + 1 else null`

### Verification
- `./gradlew test` passes with new unit tests
- `./gradlew assembleDebug` compiles

---

## M2: Session Mode UI + Auto-Advance
┌─────────────────────────────────────────┐
│ ANALOGY: Concert mode on phone - dark,  │
│ minimal, auto-pilot for the performance │
│ [🏗️ Backend done]═══[🚀 Full session]  │
│ VALUE: User activates session mode,     │
│ gets dark UI with auto-advance tuning   │
│ PROGRESS: [████████░░] 75%             │
└─────────────────────────────────────────┘

### Changes

**Modified: `app/src/main/res/values/strings.xml`**
- `session_mode_label` = "Session mode"
- `session_mode_description` = "Discreet tuning through earbuds"
- `session_stop` = "Stop"
- `session_no_headphones` = "No headphones detected. Plug in earbuds for discreet tuning."
- `session_tuning_string` = "Tuning string %d"

**Modified: localized strings (es, pt, it)**
- Add translations for all new strings

**Modified: `EarActivity.kt`**
- Add `sessionModeActive` state to `MainLayout`
- Add session mode toggle button (headphone icon) at top of normal layout
- Add `SessionModeLayout` composable:
  - Dark background (Color.Black)
  - Current string label (large, white text)
  - 4 progress dots showing which string
  - Volume slider (0.0 to 1.0)
  - Stop/exit button
- Add `LaunchedEffect` for auto-advance:
  - Loop: set currentStringIndex, play sound, delay(N seconds), advance
  - Cancel on stop or mode exit
- Check headphones on activation, show snackbar warning if none
- Save/restore session volume via SharedPreferences

### Verification
- `./gradlew assembleDebug` compiles
- `./gradlew test` passes
- Visual: session mode toggle appears, session UI renders

---

## M3: Polish + Edge Cases
┌─────────────────────────────────────────┐
│ ANALOGY: Final sound check before       │
│ the gig starts                          │
│ [🏗️ Feature done]═══[🚀 Stage-ready]   │
│ VALUE: Production-ready, all edge       │
│ cases handled                           │
│ PROGRESS: [██████████] 100%            │
└─────────────────────────────────────────┘

### Changes

**Modified: `SessionModeTest.kt`**
- Additional edge cases for auto-advance boundary

**Modified: `EarActivity.kt`**
- Handle: exiting session mode stops playback and returns to normal mode cleanly
- Handle: `onPause()` exits session mode
- Ensure pressing back while in session mode exits session mode (not the activity)

### Verification
- `./gradlew clean assembleDebug` passes
- `./gradlew test` passes all unit tests
- Edge cases: back button in session mode, onPause during session

---

## File Change Summary

| File | M1 | M2 | M3 |
|------|:--:|:--:|:--:|
| `SoundPlayer.kt` | Modify | - | - |
| `SessionModeTest.kt` | Create | - | Modify |
| `EarActivity.kt` | - | Modify | Modify |
| `values/strings.xml` | - | Modify | - |
| `values-es/strings.xml` | - | Modify | - |
| `values-pt/strings.xml` | - | Modify | - |
| `values-it/strings.xml` | - | Modify | - |
