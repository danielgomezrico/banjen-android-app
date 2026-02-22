# Plan: Expand to 5-String Banjo with Alternate Tunings and Sharing

## Pragmatic Scope (per team lead)
Focus on 5-string tuning model extension. Community sharing = local Android share intent, not a backend.

## M1: TuningModel + ToneGenerator + Unit Tests
┌─────────────────────────────────────────┐
│ ANALOGY: Building the engine before     │
│ the car body - core sound generation    │
│ and data model                          │
│ PROGRESS: [████░░░░░░] 30%             │
└─────────────────────────────────────────┘

### New: `TuningModel.kt`
- `data class Note(val name: String, val frequency: Float)`
- `data class Tuning(val name: String, val notes: List<Note>)`
- `data class Instrument(val name: String, val tunings: List<Tuning>)`
- `fun noteFrequency(midiNote: Int): Float` - equal temperament formula
- Preset data: FOUR_STRING_BANJO, FIVE_STRING_BANJO with all tunings
- `fun encodeTuning(tuning: Tuning): String` and `fun decodeTuning(encoded: String): Tuning?`

### New: `ToneGenerator.kt`
- Uses `AudioTrack` with PCM 16-bit, 44100 Hz
- `fun play(frequency: Float)` - generates sine wave, starts looping playback
- `fun stop()` - stops and releases AudioTrack
- `val isPlaying: Boolean`
- Short fade envelope to avoid clicks

### Tests: `TuningModelTest.kt`
- Note frequency calculation (A4=440, C4=261.63, etc.)
- Tuning encode/decode roundtrip
- Preset tuning counts and string counts
- Instrument validation

### Tests: `ToneGeneratorTest.kt`
- Sine wave sample generation (pure math, testable)
- Buffer size calculation

## M2: Dynamic UI + Instrument/Tuning Selector
┌─────────────────────────────────────────┐
│ ANALOGY: Dashboard that adapts to the   │
│ vehicle - same controls, different      │
│ configurations                          │
│ PROGRESS: [███████░░░] 65%             │
└─────────────────────────────────────────┘

### Modified: `EarActivity.kt`
- Replace hardcoded `buttonsText` with tuning-driven dynamic list
- Add instrument selector dropdown (4-string / 5-string)
- Add tuning selector dropdown below instrument
- Buttons rendered from `currentTuning.notes`
- Replace `SoundPlayer.playWithLoop(index)` with `ToneGenerator.play(frequency)`
- Persist selected instrument + tuning in SharedPreferences

### Modified: string resources (all locales)
- Instrument names, tuning names

## M3: Custom Tunings + Sharing
┌─────────────────────────────────────────┐
│ ANALOGY: Recipe book - save your own    │
│ recipes, share with friends via text    │
│ PROGRESS: [██████████] 100%            │
└─────────────────────────────────────────┘

### Modified: `EarActivity.kt`
- "Custom" entry in tuning selector opens creation dialog
- Dialog: name field + note pickers (one per string)
- Save to SharedPreferences as JSON
- Share button: Android share intent with encoded tuning text
- Import: paste encoded text to add custom tuning

## File Change Summary

| File | M1 | M2 | M3 |
|------|:--:|:--:|:--:|
| `TuningModel.kt` | Create | - | Modify |
| `ToneGenerator.kt` | Create | - | - |
| `TuningModelTest.kt` | Create | - | Modify |
| `ToneGeneratorTest.kt` | Create | - | - |
| `EarActivity.kt` | - | Modify | Modify |
| `SoundPlayer.kt` | - | - | - (kept for backward compat, ToneGenerator used instead) |
| `values/strings.xml` | - | Modify | Modify |
| `values-es/strings.xml` | - | Modify | - |
| `values-pt/strings.xml` | - | Modify | - |
| `values-it/strings.xml` | - | Modify | - |
