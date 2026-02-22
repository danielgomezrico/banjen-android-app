# Plan: Visual Tuning Feedback with Microphone-Based Pitch Detection

## MCP Validation

| Lib/API | Ver | Source | Status |
|---------|-----|--------|--------|
| AudioRecord | API 23+ | Android SDK (min SDK 23) | Verified |
| ActivityResultContracts.RequestPermission | androidx.activity:activity-compose:1.12.4 | context7 | Verified |
| rememberLauncherForActivityResult | Compose BOM 2026.02 | context7 | Verified |
| MediaPlayer.setVolume | API 1+ | Android SDK | Verified |
| YIN pitch detection | N/A | Pure Kotlin impl, no dep | N/A |

## Architecture

```
┌─────────────────────────────────────────────────┐
│                  EarActivity                     │
│  ┌──────────┐  ┌──────────────┐  ┌───────────┐ │
│  │ String   │  │ TuningCheck  │  │ Pitch     │ │
│  │ Buttons  │  │ Indicator    │  │ Check Btn │ │
│  │ (exist)  │  │ (new)        │  │ (new)     │ │
│  └────┬─────┘  └──────┬───────┘  └─────┬─────┘ │
│       │               │                │        │
│       ▼               ▼                ▼        │
│  ┌─────────┐   ┌──────────────┐                 │
│  │ Sound   │   │ PitchDetector│  ← Pure Kotlin  │
│  │ Player  │   │ (YIN algo)   │    no Android   │
│  └─────────┘   └──────┬───────┘    deps in algo │
│                       │                          │
│                ┌──────┴───────┐                  │
│                │ AudioRecord  │  ← Android API   │
│                │ (mic capture)│                   │
│                └──────────────┘                   │
└─────────────────────────────────────────────────┘

Flow: Tap string → ref tone plays → Tap "Check" →
      tone stops → mic captures → PitchDetector →
      indicator shows sharp/flat/in-tune
```

## Data Model

```kotlin
// Target frequencies for DGBD standard tuning
enum class BanjoString(val noteName: String, val frequencyHz: Double) {
    D4("D", 293.66),
    B3("B", 246.94),
    G3("G", 196.00),
    D3("D", 146.83)
}

// Result from pitch detection
data class PitchResult(
    val detectedHz: Double,      // 0.0 if no pitch detected
    val targetHz: Double,
    val centDeviation: Double,   // negative=flat, positive=sharp
    val status: TuningStatus
)

enum class TuningStatus {
    IN_TUNE,    // abs(cents) <= 10
    CLOSE,      // abs(cents) <= 25
    SHARP,      // cents > 25
    FLAT,       // cents < -25
    NO_SIGNAL   // too quiet or no pitch
}
```

## Interfaces

```kotlin
// PitchDetector — pure Kotlin, unit testable
class PitchDetector(private val sampleRate: Int = 44100) {
    fun detectPitch(samples: FloatArray): Double  // returns Hz, -1.0 if none
    fun centsFromTarget(detected: Double, target: Double): Double
    fun classifyTuning(cents: Double): TuningStatus
}

// AudioCaptureManager — thin Android wrapper, lives in EarActivity
// Manages AudioRecord lifecycle, feeds samples to PitchDetector
// Runs on coroutine, emits PitchResult via callback
```

---

## M1: PitchDetector — Pure Kotlin YIN Algorithm + Unit Tests

```
┌─────────────────────────────────────────┐
│ ANALOGY: Tuning fork + frequency meter  │
│ [🎵 Sound wave]═══[📊 Frequency Hz]    │
│ VALUE: Core pitch detection, fully      │
│        testable without Android device  │
│ PROGRESS: [████░░░░░░] 40%             │
└─────────────────────────────────────────┘
```

### Files
- NEW: `app/src/main/java/com/makingiants/android/banjotuner/PitchDetector.kt`
- NEW: `app/src/test/java/com/makingiants/android/banjotuner/PitchDetectorTest.kt`

### Implementation

**PitchDetector.kt** (~120 lines):
1. `detectPitch(samples: FloatArray): Double` — YIN algorithm:
   - Step 1: Difference function `d(tau)` — squared difference of signal with shifted version
   - Step 2: Cumulative mean normalized difference `d'(tau)` — normalize to remove amplitude dependence
   - Step 3: Absolute threshold — find first `tau` where `d'(tau) < threshold` (0.15)
   - Step 4: Parabolic interpolation — refine tau estimate for sub-sample accuracy
   - Step 5: Convert tau to frequency: `sampleRate / tau`
   - Returns -1.0 if no pitch detected (all values above threshold)
2. `centsFromTarget(detected: Double, target: Double): Double` — `1200 * log2(detected / target)`
3. `classifyTuning(cents: Double): TuningStatus` — threshold-based classification

**PitchDetectorTest.kt** — unit tests with synthetic sine waves:
- Generate sine wave at known frequency: `sin(2 * PI * freq * i / sampleRate)`
- Test each banjo string frequency (D3, G3, B3, D4)
- Test cent deviation calculation: exact match = 0 cents, known offsets
- Test classification thresholds
- Test edge cases: silence (all zeros), very low amplitude, DC offset

### Verification
- `./gradlew test` — all PitchDetector unit tests pass
- Accuracy: detected frequency within +/- 1Hz of input sine wave
- Cent calculation: `centsFromTarget(440.0, 440.0) == 0.0`
- Classification: `classifyTuning(0.0) == IN_TUNE`, `classifyTuning(30.0) == SHARP`

---

## M2: Permission + AudioRecord + Mic Capture Integration

```
┌─────────────────────────────────────────┐
│ ANALOGY: Building a microphone booth    │
│ [🎤 Mic]═══[🔌 Wiring]═══[📊 Meter]   │
│ VALUE: App can listen via mic, request  │
│        permission, feed audio to M1     │
│ PROGRESS: [███████░░░] 70%             │
└─────────────────────────────────────────┘
```

### Files
- EDIT: `app/src/main/AndroidManifest.xml` (add RECORD_AUDIO permission)
- EDIT: `app/src/main/java/com/makingiants/android/banjotuner/EarActivity.kt` (add AudioRecord lifecycle, permission request)
- EDIT: `app/src/main/res/values/strings.xml` (permission rationale string)
- EDIT: `app/src/main/res/values-es/strings.xml`
- EDIT: `app/src/main/res/values-pt/strings.xml`
- EDIT: `app/src/main/res/values-it/strings.xml`

### Implementation

**AndroidManifest.xml**: Add `<uses-permission android:name="android.permission.RECORD_AUDIO" />`

**EarActivity.kt** — add audio capture management:
1. Add state: `pitchCheckMode: MutableState<Boolean>` — whether currently listening
2. Add state: `pitchResult: MutableState<PitchResult?>` — latest detection result
3. Add `PitchDetector` instance (lazy, like SoundPlayer)
4. Add permission launcher:
   ```kotlin
   val permissionLauncher = rememberLauncherForActivityResult(
       ActivityResultContracts.RequestPermission()
   ) { granted -> if (granted) startPitchCheck(targetString) }
   ```
5. Add `startPitchCheck(targetString: BanjoString)`:
   - Stop SoundPlayer (mutual exclusion)
   - Create AudioRecord (MONO, 44100Hz, buffer = 4096)
   - Launch coroutine: read loop → feed to PitchDetector → update pitchResult state
6. Add `stopPitchCheck()`:
   - Stop and release AudioRecord
   - Reset pitchCheckMode

**Strings**: Add `mic_permission_rationale` in all 4 locales

### Verification
- `./gradlew assembleDebug` compiles
- Permission declared in merged manifest
- Strings present in all locales

---

## M3: Visual Tuning Indicator UI

```
┌─────────────────────────────────────────┐
│ ANALOGY: Traffic light for your tuning  │
│ [🔴 Flat]═══[🟡 Close]═══[🟢 In Tune] │
│ VALUE: User sees clear visual feedback  │
│        with color + direction arrow     │
│ PROGRESS: [██████████] 100%            │
└─────────────────────────────────────────┘
```

### Files
- EDIT: `app/src/main/java/com/makingiants/android/banjotuner/EarActivity.kt` (indicator composable, check button)
- EDIT: `app/src/main/res/values/strings.xml` (UI labels)
- EDIT: `app/src/main/res/values-es/strings.xml`
- EDIT: `app/src/main/res/values-pt/strings.xml`
- EDIT: `app/src/main/res/values-it/strings.xml`

### Implementation

**TuningIndicator composable** (in EarActivity.kt, follows existing pattern of methods on Activity):
1. Shows when `pitchCheckMode == true` and a string was selected
2. Layout:
   ```
   ┌──────────────────────────────────┐
   │         ▲ Tune Up               │  ← arrow + text (if sharp)
   │                                  │
   │    ●●●●●●●●●●●●●●●●●●●●●●●     │  ← colored bar
   │    [green/yellow/red]            │
   │                                  │
   │    -15 cents                     │  ← deviation text
   │                                  │
   │         ▼ Tune Down             │  ← arrow + text (if flat)
   └──────────────────────────────────┘
   ```
3. Colors (hardcoded Compose Color, consistent with dark theme):
   - `IN_TUNE`: Green `Color(0xFF4CAF50)`
   - `CLOSE`: Yellow `Color(0xFFFFC107)`
   - `SHARP`/`FLAT`: Red `Color(0xFFF44336)`
   - `NO_SIGNAL`: Gray (existing `banjen_gray`)
4. Direction arrow: Unicode arrow `\u25B2` (up) / `\u25BC` (down)

**"Check My Tuning" button**:
1. Appears below the selected string button (or as a small icon button)
2. Only visible when a string is actively selected (playing)
3. On tap:
   - If permission not granted → launch permission request
   - If granted → stop tone, start mic capture, show indicator
4. Tap again (or tap string button) → stop capture, return to reference tone mode

**Strings** (all 4 locales):
- `check_tuning_button`: "Check My Tuning" / "Verificar Afinacion" / "Verificar Afinacao" / "Controlla Accordatura"
- `tune_up`: "Tune Up" / "Sube" / "Suba" / "Alza"
- `tune_down`: "Tune Down" / "Baja" / "Baixe" / "Abbassa"
- `in_tune`: "In Tune!" / "Afinado!" / "Afinado!" / "Accordato!"
- `no_signal`: "Play your string..." / "Toca la cuerda..." / "Toque a corda..." / "Suona la corda..."

### Verification
- `./gradlew assembleDebug` compiles
- `./gradlew test` — all unit tests still pass
- Visual inspection: indicator shows correct colors and arrows
- Mutual exclusion: tapping "Check" stops the reference tone
- Returning to tone: tapping string button stops capture and plays tone

---

## Implementation Order & Dependencies

```
M1 (PitchDetector + tests)
  │
  ▼
M2 (Permission + AudioRecord)  ← depends on M1
  │
  ▼
M3 (Visual UI)                  ← depends on M2
```

## Risk Mitigations Applied

| Risk | Milestone | Mitigation |
|------|-----------|------------|
| Feedback loop | M2 | `player.stop()` before `AudioRecord.startRecording()` |
| Low freq accuracy | M1 | Buffer size 4096 @ 44100Hz; unit tests verify D3 (147Hz) |
| Permission denied | M2 | Graceful: button hidden until permission granted |
| AudioRecord init fail | M2 | Try-catch around initialization, disable feature on failure |
| Harmonics/octave error | M1 | YIN threshold 0.15; parabolic interpolation; unit tests |
