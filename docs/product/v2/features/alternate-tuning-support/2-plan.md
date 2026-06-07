# Plan: Alternate Tuning Support (GDAE, CGBD, DGBE)

## MCP Validation

| Lib/API | Ver | Source | Status |
|---------|-----|--------|--------|
| SharedPreferences | API 1+ | Android SDK | Verified |
| MediaPlayer (WAV) | API 1+ | Android SDK | Verified |
| DropdownMenu | Compose Material3 BOM 2026.02 | Already in project | Verified |
| context.assets.openFd | API 1+ | Android SDK | Verified |
| Python wave module | stdlib | Python 3.14 on host | Verified |

## Architecture

```
┌─────────────────────────────────────────────────┐
│                  EarActivity                     │
│  ┌──────────────┐  ┌──────────┐                 │
│  │ TuningSelector│  │ String   │                 │
│  │ (dropdown)   │  │ Buttons  │                 │
│  └──────┬───────┘  └────┬─────┘                 │
│         │               │                        │
│         ▼               ▼                        │
│  ┌──────────────┐  ┌─────────┐                  │
│  │ TuningPreset │  │ Sound   │                  │
│  │ (enum)       │──│ Player  │                  │
│  └──────────────┘  └────┬────┘                  │
│         │               │                        │
│  ┌──────┴───────┐  ┌────┴──────────┐            │
│  │ SharedPrefs  │  │ assets/       │            │
│  │ (persist)    │  │ b_sounds/*.wav│            │
│  └──────────────┘  └───────────────┘            │
└─────────────────────────────────────────────────┘

Tuning Selection Flow:
  User taps dropdown → selects "Irish GDAE" →
  TuningPreset.GDAE loaded → button labels update →
  SoundPlayer.play("b_sounds/2.mp3") for string 4 (G3)
```

## Data Model

```kotlin
enum class TuningPreset(
    val displayName: String,
    val noteNames: List<String>,     // 4 notes, string 4→1 (low to high)
    val assetFiles: List<String>,    // 4 filenames in b_sounds/
) {
    STANDARD("Standard (DGBD)", listOf("D","G","B","D"), listOf("1.mp3","2.mp3","3.mp3","4.mp3")),
    IRISH("Irish (GDAE)",       listOf("G","D","A","E"), listOf("2.mp3","1.mp3","a3.wav","e4.wav")),
    PLECTRUM("Plectrum (CGBD)", listOf("C","G","B","D"), listOf("c3.wav","2.mp3","3.mp3","4.mp3")),
    CHICAGO("Chicago (DGBE)",   listOf("D","G","B","E"), listOf("1.mp3","2.mp3","3.mp3","e4.wav"));

    fun buttonLabel(index: Int): String = "${4 - index} - ${noteNames[index]}"
}
```

---

## M1: TuningPreset Data Model + Unit Tests + Audio Assets

```
┌─────────────────────────────────────────┐
│ ANALOGY: Menu cards for a restaurant    │
│ [📋 One menu]═══[📋📋📋📋 Four menus]  │
│ VALUE: All tuning data defined, assets  │
│        generated, model fully tested    │
│ PROGRESS: [████░░░░░░] 40%             │
└─────────────────────────────────────────┘
```

### Files
- NEW: `app/src/main/java/com/makingiants/android/banjotuner/TuningPreset.kt`
- NEW: `app/src/test/java/com/makingiants/android/banjotuner/TuningPresetTest.kt`
- NEW: `app/src/main/assets/b_sounds/a3.wav` (generated via Python)
- NEW: `app/src/main/assets/b_sounds/c3.wav` (generated via Python)
- NEW: `app/src/main/assets/b_sounds/e4.wav` (generated via Python)

### Implementation

**Generate WAV assets** using Python `wave` module:
- A3 = 220.00 Hz, C3 = 130.81 Hz, E4 = 329.63 Hz
- 44100 Hz sample rate, 16-bit PCM, mono, 3 seconds duration
- Sine wave with gentle fade-in/out to avoid click artifacts

**TuningPreset.kt**: Enum with 4 presets as described in data model above.

**TuningPresetTest.kt**: Unit tests:
- Each preset has exactly 4 notes and 4 asset files
- `buttonLabel(index)` returns correct "N - Note" format
- Default preset is STANDARD
- All asset filenames are non-empty

### Verification
- `./gradlew test` — TuningPreset unit tests pass
- WAV files exist in `assets/b_sounds/`
- `./gradlew assembleDebug` compiles

---

## M2: Parameterize SoundPlayer + Tuning Selector UI + Persistence

```
┌─────────────────────────────────────────┐
│ ANALOGY: Jukebox with selectable albums │
│ [🎵 One album]═══[🎵🎵🎵🎵 Four albums]│
│ VALUE: User can switch tunings, labels  │
│        update, correct tones play,      │
│        selection persists across restart│
│ PROGRESS: [██████████] 100%            │
└─────────────────────────────────────────┘
```

### Files
- EDIT: `app/src/main/java/com/makingiants/android/banjotuner/SoundPlayer.kt`
- EDIT: `app/src/main/java/com/makingiants/android/banjotuner/EarActivity.kt`
- EDIT: `app/src/main/res/values/strings.xml`
- EDIT: `app/src/main/res/values-es/strings.xml`
- EDIT: `app/src/main/res/values-pt/strings.xml`
- EDIT: `app/src/main/res/values-it/strings.xml`

### Implementation

**SoundPlayer.kt changes**:
- Add `playWithLoop(assetFileName: String)` overload that takes a full filename
- Keep existing `playWithLoop(index: Int)` working for backwards compatibility but delegate to the new method

**EarActivity.kt changes**:
1. Add `selectedTuning` state: `mutableStateOf(TuningPreset.STANDARD)`
2. Load from SharedPreferences in `onCreate`
3. Replace hardcoded `buttonsText` with dynamic labels from `selectedTuning.buttonLabel(index)`
4. Pass asset filename to `player.playWithLoop(selectedTuning.assetFiles[index])`
5. Add `TuningSelector` composable at top of `MainLayout` — a `TextButton` that opens `DropdownMenu` with 4 tuning options
6. On tuning change: stop current playback, update state, persist to SharedPreferences

**Strings** (all 4 locales):
- `tuning_selector_label`: "Tuning" / "Afinacion" / "Afinacao" / "Accordatura"
- Tuning preset display names use the English music notation (DGBD, GDAE etc.) which is universal

### Verification
- `./gradlew assembleDebug` compiles
- `./gradlew test` — all unit tests pass
- Tuning selector visible at top of screen
- Selecting each tuning updates button labels
- SharedPreferences persists selection

---

## Implementation Order & Dependencies

```
M1 (TuningPreset + Assets + Tests)
  │
  ▼
M2 (SoundPlayer + UI + Persistence)  ← depends on M1
```

## Risk Mitigations Applied

| Risk | Milestone | Mitigation |
|------|-----------|------------|
| No audio tools | M1 | Python `wave` module generates WAV files |
| Sound quality | M1 | Sine waves with fade-in/out, 44100Hz/16-bit — standard quality |
| Test compatibility | M2 | Default tuning is STANDARD with same "N - Note" labels |
| SharedPrefs key collision | M2 | Use `"banjen_selected_tuning"` dedicated key |
| Button label format | M1 | `buttonLabel()` enforces "N - Note" format |
