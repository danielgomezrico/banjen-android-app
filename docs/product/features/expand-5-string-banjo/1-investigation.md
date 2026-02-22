# Investigation: Expand to 5-String Banjo with Alternate Tunings and Sharing

## Summary
Investigated the full scope of expanding Banjen from a fixed 4-string tuner to a multi-instrument, multi-tuning app supporting both 4-string and 5-string banjos with custom tuning creation and sharing. The key technical enabler is replacing MP3 assets with AudioTrack sine wave synthesis, allowing any note at any frequency to be generated on-the-fly. The tuning data model, UI refactoring, and persistence are all achievable with zero new dependencies.

## Files

| File | Purpose | Relevance |
|------|---------|-----------|
| `SoundPlayer.kt` | MediaPlayer wrapper, hardcoded 4 MP3s | **Replace** with ToneGenerator or augment with frequency-based playback |
| `EarActivity.kt` | Hardcoded 4 buttons, single tuning | **Refactor** to dynamic string count + tuning/instrument selector |
| `values/strings.xml` | 4 button labels | Add instrument names, tuning names, custom tuning UI |
| `values-es/strings.xml` | Spanish | Localize new strings |
| `values-pt/strings.xml` | Portuguese | Localize new strings |
| `values-it/strings.xml` | Italian | Localize new strings |
| NEW: `ToneGenerator.kt` | AudioTrack sine wave synthesis | Core enabler - generates any frequency |
| NEW: `TuningModel.kt` | Data classes for Instrument, Tuning, Note | Domain model |

## Key Technical Decisions

### AudioTrack Sine Wave Synthesis
- `AudioTrack` with `AudioFormat.ENCODING_PCM_16BIT`, 44100 Hz sample rate
- Sine wave: `sample = sin(2 * PI * frequency * t / sampleRate)`
- Looping: write a full cycle buffer, set `LOOP_INFINITE` via `setLoopPoints()`
- Volume: `AudioTrack.setVolume(float)`
- This replaces the need for MP3 files entirely for new tunings
- Keep backward compatibility: existing 4-string DGBD tuning can still use MP3s or migrate to generated tones

### Note Frequencies (A4 = 440 Hz, Equal Temperament)
- Formula: `freq = 440 * 2^((midiNote - 69) / 12)`
- Common banjo notes and their frequencies:
  - g4 (high G, 5th string drone): 392.00 Hz
  - D3: 146.83 Hz, G3: 196.00 Hz, B3: 246.94 Hz, D4: 293.66 Hz
  - C3: 130.81 Hz, C4: 261.63 Hz, F#3: 185.00 Hz, A3: 220.00 Hz

### Tuning Presets
**4-String:**
- Standard DGBD (existing)
- Irish GDAE
- Chicago DGBE
- Plectrum CGBD

**5-String:**
- Open G: gDGBD (standard)
- Double C: gCGCD
- Modal/Sawmill: gDGCD
- Drop C: gCGBD
- Open D: f#DF#AD

### SharedPreferences for Persistence
- Selected instrument type (4-string/5-string)
- Selected tuning index
- Custom tunings as JSON array

### Share Format
- Simple text: "Banjen Tuning: Open G | gDGBD | 392.0,146.83,196.0,246.94,293.66"
- Android share intent - no backend needed
- Import: parse from clipboard or share intent

## Constraints
- [x] AudioTrack available since API 1 (no minSdk concern)
- [x] Must keep existing simplicity for Harold/Betty personas
- [x] No new dependencies
- [x] JSON serialization via `org.json` (Android built-in)
- [x] Must handle 4 or 5 buttons dynamically in Compose layout

## Risks

| Risk | Severity | Mitigation |
|------|----------|------------|
| Sine wave sounds harsh compared to MP3 samples | Medium | Use short fade-in/fade-out envelope; consider adding slight harmonic content |
| UI gets cluttered with tuning selector | Medium | Minimal dropdown/selector; default to simplest view |
| Custom tuning JSON corruption | Low | Validation on parse; fallback to empty list |
| AudioTrack threading | Low | Write on background thread, manage lifecycle carefully |

## Recommendations
1. Create `ToneGenerator` as a standalone class that can play any frequency
2. Create `TuningModel.kt` with data classes: `Note`, `Tuning`, `Instrument`
3. Refactor EarActivity to use a tuning-driven dynamic button list
4. Add instrument selector (compact dropdown) at top
5. Add custom tuning creation via a simple dialog
6. Share via Android share intent with encoded text format
