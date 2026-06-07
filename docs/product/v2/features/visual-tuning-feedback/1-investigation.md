# Investigation: Visual Tuning Feedback with Microphone-Based Pitch Detection

## Summary
Investigated the Banjen banjo tuner app to understand how to add an optional microphone-based pitch detection feature ("check my tuning") that shows visual sharp/flat/in-tune feedback. The app is a minimal 2-file Kotlin/Compose project (EarActivity + SoundPlayer) with min SDK 23, making AudioRecord and runtime permissions available. The feature requires a new PitchDetector class, RECORD_AUDIO permission, and additive UI changes to EarActivity.

## Files

| File | Purpose | Relevance |
|------|---------|-----------|
| `app/src/main/java/.../EarActivity.kt` (272 lines) | Single activity with Compose UI, 4 string buttons with animations, AdMob ads | Will add pitch detection UI (indicator composable), permission request, "check my tuning" button |
| `app/src/main/java/.../SoundPlayer.kt` (88 lines) | MediaPlayer wrapper for looping playback from assets | Need to pause/stop playback when listening via mic to avoid feedback; provides `isPlaying` state |
| `app/src/main/AndroidManifest.xml` | App manifest with INTERNET + ACCESS_NETWORK_STATE permissions | Must add RECORD_AUDIO permission |
| `app/src/main/res/values/strings.xml` | English string resources (button labels, volume message) | Add permission rationale, pitch indicator labels |
| `app/src/main/res/values-es/strings.xml` | Spanish strings | Localize new strings |
| `app/src/main/res/values-pt/strings.xml` | Portuguese strings | Localize new strings |
| `app/src/main/res/values-it/strings.xml` | Italian strings | Localize new strings |
| `app/src/main/res/values/colors.xml` | Color definitions (dark theme: primary_material_dark, accent #6d94a1) | May add tuning indicator colors (green/yellow/red) |
| `app/src/androidTest/.../EarActivityTest.kt` | Instrumented UI tests using Robot pattern | Reference for test style; pitch detection tests will be unit tests |
| `app/src/androidTest/.../EarRobot.kt` | Robot pattern helper for Compose testing | Reference for assertion patterns |
| `app/build.gradle` | Build config, dependencies (Compose BOM 2026.02, Kotlin 2.3.10) | No new dependencies needed for AudioRecord/YIN |
| NEW: `app/src/main/java/.../PitchDetector.kt` | Pitch detection engine using YIN algorithm + AudioRecord | Core new file |
| NEW: `app/src/test/.../PitchDetectorTest.kt` | Unit tests for pitch detection logic | Pure Kotlin, runs without device |

## Patterns

### UI Pattern
- Single Activity, direct `setContent { Contents() }` — no ViewModel, no navigation
- State management: `remember { mutableIntStateOf(-1) }` for selection, `mutableStateOf(false)` for boolean states
- Composable functions are methods on `EarActivity` (not standalone), accessing `player` directly
- Animations: `animateFloatAsState` for discrete transitions, `rememberInfiniteTransition` for continuous shake
- Layout: `Column` with `Arrangement.SpaceEvenly`, each button gets `weight(1f)` — ref @ EarActivity.kt:130-143

### Sound Pattern
- `SoundPlayer` wraps `MediaPlayer`, accessed via `player` lazy property on EarActivity
- `player.playWithLoop(index)` starts looping, `player.stop()` stops with mute-unmute to avoid artifacts — ref @ SoundPlayer.kt:38-58, 65-76
- `isPlaying` exposed as computed property — ref @ SoundPlayer.kt:17
- AudioManager already obtained in SoundPlayer for volume checks — ref @ SoundPlayer.kt:18

### Testing Pattern
- Robot pattern: `withEarRobot(rule) { actions }.assert { checks }` — ref @ EarRobot.kt:49-54
- Instrumented tests only (no unit test directory exists currently)
- Tests use `AndroidComposeTestRule<ActivityScenarioRule<EarActivity>, EarActivity>`

### Color/Theme Pattern
- Dark background (`primary_material_dark`), accent color `#6d94a1` (muted teal)
- Colors defined in XML resources, referenced via `colorResource()` in Compose
- `MaterialTheme` with custom `lightColorScheme` (despite visually dark) — ref @ EarActivity.kt:98-111

## Constraints
- [x] Min SDK 23 — AudioRecord and runtime permissions (requestPermissions) both available
- [x] No external dependencies allowed per task spec — YIN algorithm implemented in pure Kotlin
- [x] RECORD_AUDIO is a dangerous permission — requires runtime request on API 23+
- [x] Must not interfere with reference tone playback — stop playback before mic capture to avoid feedback loop
- [x] Target frequencies for standard DGBD tuning: D4=293.66Hz, B3=246.94Hz, G3=196.00Hz, D3=146.83Hz
- [x] Pitch detection accuracy target: +/- 5 cents
- [x] Feature must be optional — "Tap to check" secondary to reference tone, not a replacement

## Dependencies

```
EarActivity → SoundPlayer (player instance, stop before mic capture)
EarActivity → PitchDetector (new, start/stop capture, get pitch results)
PitchDetector → AudioRecord (Android API, no external dep)
PitchDetector → YIN algorithm (pure Kotlin implementation)
EarActivity → Compose UI (pitch indicator composable)
AndroidManifest → RECORD_AUDIO permission declaration
```

## Tests

- **Style**: Unit tests for PitchDetector (pure Kotlin logic, no Android deps needed for algorithm)
- **Location**: `app/src/test/java/com/makingiants/android/banjotuner/PitchDetectorTest.kt` (new)
- **What to test**:
  - YIN algorithm with known synthetic waveforms (sine waves at target frequencies)
  - Cent deviation calculation
  - Tuning status classification (in-tune / sharp / flat)
  - Edge cases: silence, noise, very low amplitude
- **Instrumented tests**: Could add UI tests for permission flow and indicator display but requires device; note in PR

## Risks

| Risk | Severity | Mitigation |
|------|----------|------------|
| Feedback loop: mic picks up speaker output | High | Stop SoundPlayer before starting mic capture; enforce mutual exclusion |
| YIN accuracy on low strings (D3=147Hz) | Medium | Use adequate buffer size (4096+ samples at 44100Hz = ~93ms, enough for 2+ cycles at 147Hz); tune YIN threshold |
| Permission denial by user | Low | Graceful degradation — hide pitch check button, show rationale dialog |
| AudioRecord not available on some devices | Low | Catch initialization errors, disable feature gracefully |
| Harmonics causing octave errors | Medium | YIN algorithm naturally handles harmonics better than autocorrelation; validate with test signals |
| Conflict with other engineers editing EarActivity.kt | Medium | Our changes are additive (new composable, new state), isolated from string button logic |

## Recommendations

1. **Create PitchDetector as a standalone class** with no Android UI dependencies — accepts raw audio samples, returns frequency in Hz. This enables pure unit testing with synthetic waveforms.
2. **Separate AudioCapture from pitch algorithm** — PitchDetector handles math only; a thin wrapper in EarActivity manages AudioRecord lifecycle and feeds samples to PitchDetector.
3. **Implement YIN algorithm** — well-studied, ~100-150 lines of Kotlin, handles harmonics well, no FFT needed. Use sample rate 44100Hz, buffer size 4096 samples.
4. **Mutual exclusion with SoundPlayer** — when user taps "Check Tuning", stop the reference tone first, then start mic capture. Display results. User taps again to return to reference tone mode.
5. **Simple visual indicator** — large colored circle/bar: green (#4CAF50) for in-tune (within +/-10 cents), yellow (#FFC107) for close (+/-10-25 cents), red (#F44336) for far off (>25 cents). Add directional arrow (tune up/down).
6. **Permission handling** — use `rememberLauncherForActivityResult(RequestPermission())` Compose API for clean permission flow.
7. **Add RECORD_AUDIO to AndroidManifest.xml** — simple addition, no impact on existing functionality.
8. **Localize all new strings** across en, es, pt, it.

## Verification
- [x] Files verified — all paths confirmed in worktree
- [x] Patterns confirmed — UI, sound, test patterns documented with line refs
- [x] Constraints traced — SDK level, permission model, frequency ranges
- [x] Dependencies mapped — PitchDetector ↔ EarActivity ↔ SoundPlayer
- [x] Tests validated — no existing unit tests; robot pattern for instrumented tests confirmed
