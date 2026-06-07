# Investigation: Alternate Tuning Support (GDAE, CGBD, DGBE)

## Summary
Investigated the Banjen banjo tuner to add support for 3 additional tuning presets beyond the current DGBD standard. The app uses hardcoded MP3 assets (1.mp3-4.mp3 mapping to D3, G3, B3, D4) and hardcoded string resource button labels. Adding alternate tunings requires: parameterizing SoundPlayer to play by filename, creating a tuning data model, generating 3 new MP3 tone assets (A3, C3, E4), adding a tuning selector UI, and persisting selection via SharedPreferences.

## Files

| File | Purpose | Relevance |
|------|---------|-----------|
| `app/src/main/java/.../SoundPlayer.kt` (88 lines) | MediaPlayer wrapper, hardcoded `sounds = arrayOf("1.mp3"..."4.mp3")` | Must parameterize to accept arbitrary filenames per tuning |
| `app/src/main/java/.../EarActivity.kt` (271 lines) | Single activity, hardcoded `buttonsText` list of string resource IDs | Must make button labels dynamic based on selected tuning, add tuning selector |
| `app/src/main/assets/b_sounds/` | 4 MP3 files: 1.mp3(D3), 2.mp3(G3), 3.mp3(B3), 4.mp3(D4) | Add 3 new: A3, C3, E4 |
| `app/src/main/res/values/strings.xml` | Button labels "1 - D", "2 - B", etc. | Add tuning names, keep note labels dynamic |
| `app/src/main/res/values-es/strings.xml` | Spanish strings | Localize tuning names |
| `app/src/main/res/values-pt/strings.xml` | Portuguese strings | Localize tuning names |
| `app/src/main/res/values-it/strings.xml` | Italian strings | Localize tuning names |
| `app/src/androidTest/.../EarActivityTest.kt` | UI tests using Robot pattern | Tests click button by text "N - ", need to work with default tuning |
| `app/src/androidTest/.../EarRobot.kt` | Robot helper: `onNodeWithText("$buttonIndex - ", substring = true)` | Pattern still works if labels follow "N - X" format |
| NEW: `app/src/main/java/.../TuningPreset.kt` | Tuning data model | Core new file |
| NEW: `app/src/test/.../TuningPresetTest.kt` | Unit tests for tuning model | Pure Kotlin, runs without device |

## Patterns

### Sound Asset Pattern
- Files in `assets/b_sounds/`, loaded via `context.assets.openFd("b_sounds/${sounds[index]}")` — ref @ SoundPlayer.kt:43
- Hardcoded array: `sounds = arrayOf("1.mp3", "2.mp3", "3.mp3", "4.mp3")` — ref @ SoundPlayer.kt:27
- `playWithLoop(index: Int)` uses index into array — ref @ SoundPlayer.kt:38

### Button Label Pattern
- `buttonsText` is a hardcoded list of string resource IDs — ref @ EarActivity.kt:70-76
- Labels rendered via `getString(text)` — ref @ EarActivity.kt:260
- Format: "N - NoteName" (e.g., "1 - D", "3 - G")

### State Pattern
- Selection state: `mutableIntStateOf(-1)` stored as string resource ID — ref @ EarActivity.kt:135
- No persistence across restarts (selection resets on app restart)

### Test Pattern
- Robot clicks by text: `onNodeWithText("$buttonIndex - ", substring = true)` — ref @ EarRobot.kt:16
- Tests iterate 1..4 for all buttons — ref @ EarActivityTest.kt:33

## Current Sound File Mapping
```
Button Index  →  buttonsText        →  sounds[]   →  Note
0             →  ear_button_4_text  →  1.mp3      →  D3 (146.83 Hz)
1             →  ear_button_3_text  →  2.mp3      →  G3 (196.00 Hz)
2             →  ear_button_2_text  →  3.mp3      →  B3 (246.94 Hz)
3             →  ear_button_1_text  →  4.mp3      →  D4 (293.66 Hz)
```

## Required Tuning Presets
```
Standard DGBD (default): D3,  G3,  B3,  D4   → 1.mp3, 2.mp3, 3.mp3, 4.mp3
Irish GDAE:               G3,  D3,  A3,  E4   → 2.mp3, 1.mp3, a3.mp3, e4.mp3
Plectrum CGBD:            C3,  G3,  B3,  D4   → c3.mp3, 2.mp3, 3.mp3, 4.mp3
Chicago DGBE:             D3,  G3,  B3,  E4   → 1.mp3, 2.mp3, 3.mp3, e4.mp3
```

## New Assets Needed
- `a3.mp3` — A3 (220.00 Hz)
- `c3.mp3` — C3 (130.81 Hz)
- `e4.mp3` — E4 (329.63 Hz)

Note: Existing assets are real instrument recordings. New assets can be generated as pure sine tones using SoX or ffmpeg command line, or we can use a simple approach of generating WAV data programmatically and converting. For consistency, synthesized sine tones at the target frequencies are acceptable since the app is a reference tone tuner.

## Constraints
- [x] No external dependencies — tuning model is pure Kotlin data class/enum
- [x] SharedPreferences available on all SDK levels (min 23)
- [x] Button labels must remain in "N - NoteName" format for test compatibility
- [x] SoundPlayer `playWithLoop` API must support tuning-specific asset paths
- [x] Default tuning must be DGBD (backwards compatible)
- [x] Tuning names need localization (note names differ: en D/es Re/pt Re/it Re)

## Dependencies
```
EarActivity → TuningPreset (new, provides note names + asset paths)
EarActivity → SharedPreferences (persist selected tuning)
EarActivity → SoundPlayer (parameterized play)
SoundPlayer → assets/b_sounds/*.mp3 (new + existing files)
TuningPreset → pure Kotlin (no Android deps for model)
```

## Tests
- **Style**: Unit tests for TuningPreset data model (pure Kotlin)
- **Location**: `app/src/test/java/com/makingiants/android/banjotuner/TuningPresetTest.kt` (new)
- **What to test**: Preset definitions, asset path correctness, note name arrays, default preset
- **Existing tests**: `EarActivityTest` will continue to work as-is since default tuning is DGBD with same button labels

## Risks

| Risk | Severity | Mitigation |
|------|----------|------------|
| Sound quality mismatch (synth vs recorded) | Medium | Generate high-quality sine tones at correct frequencies; acceptable for reference tuning |
| Generating MP3 assets without audio tools | Medium | Use ffmpeg/sox if available, or embed raw audio bytes; generate at build time |
| SharedPreferences key collision | Low | Use dedicated preference key with clear namespace |
| Button label localization complexity | Low | Use Compose dynamic strings instead of XML resource IDs for note names |
| EarRobot test compatibility | Low | Keep "N - NoteName" format; tests only check default tuning |

## Recommendations

1. **Create `TuningPreset` enum** with 4 presets, each defining: display name, 4 note names, 4 asset filenames. Pure Kotlin, unit testable.
2. **Parameterize `SoundPlayer.playWithLoop`** to accept a filename string instead of an index. Add new overload or change signature.
3. **Generate MP3 assets** using ffmpeg (if available) or create programmatically. Sine waves at A3=220Hz, C3=130.81Hz, E4=329.63Hz.
4. **Add tuning selector** as a `DropdownMenu` or row of `FilterChip` at the top of the main layout, above the string buttons.
5. **Persist selection** via `SharedPreferences` using `getSharedPreferences("banjen_prefs", MODE_PRIVATE)`.
6. **Make button labels dynamic** — derive from selected TuningPreset instead of hardcoded string resource IDs.
7. **Localize tuning preset names** (not individual note names — note names in music are universal enough, but tuning preset display names need localization).

## Verification
- [x] Files verified — all paths confirmed in worktree
- [x] Patterns confirmed — sound loading, button labels, state management documented with line refs
- [x] Constraints traced — SDK, file format, test compatibility
- [x] Dependencies mapped — TuningPreset ↔ EarActivity ↔ SoundPlayer
- [x] Tests validated — existing instrumented tests, new unit tests for model
