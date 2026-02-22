# Plan: Add Adjustable Reference Pitch (A=432-446 Hz)

## MCP Validation

| Lib | Ver | Source | Status |
|-----|-----|--------|--------|
| `android.media.PlaybackParams.setPitch()` | API 23+ | [Android Docs](https://developer.android.com/reference/android/media/PlaybackParams) | Confirmed: multiplicative float, 1.0f = normal |
| `android.media.MediaPlayer.setPlaybackParams()` | API 23+ | [Android Docs](https://developer.android.com/reference/android/media/MediaPlayer) | Confirmed: must call after prepare |
| `SharedPreferences` | API 1+ | Android SDK standard | No validation needed |
| Jetpack Compose Material3 | BOM 2026.02.00 | Already in project | Confirmed in `app/build.gradle:99` |

No third-party dependencies required. All APIs are standard Android SDK.

## Architecture

```
┌──────────────────────────────────────────────────────┐
│                    EarActivity                        │
│  ┌──────────────┐  ┌────────────────────────────┐    │
│  │ SharedPrefs   │  │ Compose State              │    │
│  │ "ref_pitch"   │──│ referencePitch: Int = 440  │    │
│  │ default: 440  │  └─────────┬──────────────────┘    │
│  └──────────────┘            │                        │
│                    ┌─────────▼──────────┐             │
│                    │  PitchControl Row  │             │
│                    │  [-] A=440 [+]     │             │
│                    └─────────┬──────────┘             │
│                              │ pitch / 440f           │
│                    ┌─────────▼──────────┐             │
│                    │   SoundPlayer      │             │
│                    │   .pitchRatio      │             │
│                    │   .playWithLoop()  │             │
│                    │   .onPrepared() →  │             │
│                    │   setPlaybackParams│             │
│                    └────────────────────┘             │
└──────────────────────────────────────────────────────┘
```

## Interface Contract

```kotlin
// SoundPlayer - new/modified signatures
class SoundPlayer(context: Context) {
    var pitchRatio: Float = 1.0f          // NEW field
    fun playWithLoop(index: Int)           // UNCHANGED signature
    // onPrepared now applies pitchRatio after start()
}

// Pure function (testable without Android)
fun calculatePitchRatio(referencePitch: Int): Float = referencePitch / 440f

// SharedPreferences keys
const val PREFS_NAME = "banjen_prefs"
const val KEY_REFERENCE_PITCH = "reference_pitch"
const val DEFAULT_PITCH = 440
const val MIN_PITCH = 432
const val MAX_PITCH = 446
```

---

## M1: SoundPlayer Pitch Support + Unit Tests
┌─────────────────────────────────────────┐
│ ANALOGY: Volume knob on amplifier -     │
│ signal goes through, knob adjusts it    │
│ [🏗️ Fixed pitch]═══[🚀 Adjustable]     │
│ VALUE: SoundPlayer can play at any      │
│ pitch ratio; pure logic unit-tested     │
│ PROGRESS: [████░░░░░░] 40%             │
└─────────────────────────────────────────┘

### Changes

**New file: `app/src/test/java/com/makingiants/android/banjotuner/PitchCalculationTest.kt`**
- Unit tests for `calculatePitchRatio()`:
  - `calculatePitchRatio(440)` returns `1.0f`
  - `calculatePitchRatio(432)` returns `~0.98182f`
  - `calculatePitchRatio(446)` returns `~1.01364f`
  - Boundary values: 432, 440, 446

**Modified: `SoundPlayer.kt`**
- Add `var pitchRatio: Float = 1.0f` field
- Add companion object with `calculatePitchRatio(referencePitch: Int): Float`
- Modify `onPrepared()`: after `mediaPlayer?.start()`, call `mediaPlayer?.playbackParams = PlaybackParams().setPitch(pitchRatio)`
- Import `android.media.PlaybackParams`

### Verification
- `./gradlew test` passes with new unit tests (pure math, no device needed)
- `./gradlew assembleDebug` compiles successfully
- Expected: `calculatePitchRatio(440) == 1.0f`, `calculatePitchRatio(432) == 0.981818..f`

---

## M2: Pitch UI Control + Persistence
┌─────────────────────────────────────────┐
│ ANALOGY: Thermostat - display shows     │
│ current temp, +/- buttons adjust it,    │
│ setting persists across power cycles    │
│ [🏗️ Backend done]═══[🚀 UI + persist]  │
│ VALUE: User can see and change pitch,   │
│ setting survives app restart            │
│ PROGRESS: [████████░░] 80%             │
└─────────────────────────────────────────┘

### Changes

**Modified: `app/src/main/res/values/strings.xml`**
- Add `<string name="reference_pitch_label">Reference pitch</string>`

**Modified: `app/src/main/res/values-es/strings.xml`**
- Add `<string name="reference_pitch_label">Tono de referencia</string>`

**Modified: `app/src/main/res/values-pt/strings.xml`**
- Add `<string name="reference_pitch_label">Tom de referência</string>`

**Modified: `app/src/main/res/values-it/strings.xml`**
- Add `<string name="reference_pitch_label">Tono di riferimento</string>`

**Modified: `EarActivity.kt`**
- Add SharedPreferences read in `onCreate`: load saved pitch, default 440
- Add `referencePitch` as `mutableIntStateOf(savedValue)` in `MainLayout()`
- Add `@Composable PitchControl(referencePitch: MutableState<Int>, onPitchChanged: (Int) -> Unit)`:
  - Row layout: [-] button | "A=440" Text | [+] button
  - Coerce pitch to 432..446 range
  - On change: update state, persist to SharedPreferences, update `player.pitchRatio`
  - If currently playing: restart playback with new pitch
- Place `PitchControl` between the string buttons Column and the AdView
- Wire pitch into button onClick: set `player.pitchRatio = calculatePitchRatio(referencePitch.value)` before `playWithLoop()`

### Verification
- `./gradlew assembleDebug` compiles
- `./gradlew test` still passes
- Visual: pitch control row appears in UI layout
- Functional: changing pitch and restarting app preserves the setting

---

## M3: Localization + Edge Cases + Final Polish
┌─────────────────────────────────────────┐
│ ANALOGY: Quality inspection on assembly │
│ line - everything works, now verify     │
│ edge cases and finish touches           │
│ [🏗️ Feature done]═══[🚀 Polished]      │
│ VALUE: Production-ready, all edge       │
│ cases handled, all locales complete     │
│ PROGRESS: [██████████] 100%            │
└─────────────────────────────────────────┘

### Changes

**Modified: `PitchCalculationTest.kt`**
- Add edge case tests: pitch clamping at boundaries (tapping - at 432 stays 432, + at 446 stays 446)
- Add test: default pitch ratio is exactly 1.0f

**Modified: `EarActivity.kt`**
- Ensure pitch control buttons are disabled/visually muted at boundaries (432 min, 446 max)
- Handle: if user changes pitch while a string is actively playing, apply new pitch immediately (stop + replay with new ratio)
- Ensure PitchControl row uses existing color scheme for visual consistency

### Verification
- `./gradlew assembleDebug` compiles
- `./gradlew test` passes all unit tests
- Full clean build: `./gradlew clean assembleDebug` succeeds
- Boundary behavior: +/- buttons respect 432-446 range

---

## Risk Mitigations Applied

| Risk from Investigation | Mitigation in Plan |
|------------------------|-------------------|
| `setPlaybackParams` after prepare | M1: applied in `onPrepared()` after `start()` |
| UI space tight | M2: compact single row [-] A=440 [+] between buttons and ad |
| SharedPreferences on main thread | M2: read once in `onCreate`, writes via `apply()` (async) |
| Audio artifacts | M1: pitch range 0.98-1.01 is narrow and safe |

## File Change Summary

| File | M1 | M2 | M3 |
|------|:--:|:--:|:--:|
| `SoundPlayer.kt` | Modify | - | - |
| `PitchCalculationTest.kt` | Create | - | Modify |
| `EarActivity.kt` | - | Modify | Modify |
| `values/strings.xml` | - | Modify | - |
| `values-es/strings.xml` | - | Modify | - |
| `values-pt/strings.xml` | - | Modify | - |
| `values-it/strings.xml` | - | Modify | - |
