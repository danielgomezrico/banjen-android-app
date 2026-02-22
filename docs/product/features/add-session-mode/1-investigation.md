# Investigation: Add Session Mode with Earbud-Optimized Discreet Tuning

## Summary
Investigated how to add a "session mode" to Banjen for discreet, speed-focused tuning through earbuds. The key components are: (1) headphone detection via `AudioManager.getDevices(AudioDeviceInfo.GET_DEVICES_OUTPUTS)`, (2) per-player volume control via `MediaPlayer.setVolume(float, float)`, (3) auto-advance sequential playback using Compose `LaunchedEffect` with `delay()`, and (4) a dark-themed minimal UI toggled by a single button. All APIs are available at minSdk 23. No new dependencies needed.

## Files

| File | Purpose | Relevance |
|------|---------|-----------|
| `app/src/main/java/.../EarActivity.kt` | Single activity with Compose UI | **Primary** - add session mode toggle, dark UI, auto-advance flow |
| `app/src/main/java/.../SoundPlayer.kt` | MediaPlayer wrapper | **Primary** - add adjustable volume via `setVolume()`, play-by-index for auto-advance |
| `app/src/main/res/values/strings.xml` | English strings | Add session mode labels |
| `app/src/main/res/values-es/strings.xml` | Spanish strings | Localized labels |
| `app/src/main/res/values-pt/strings.xml` | Portuguese strings | Localized labels |
| `app/src/main/res/values-it/strings.xml` | Italian strings | Localized labels |
| `app/src/main/res/values/colors.xml` | Color definitions | May need dark theme colors |

## Patterns

### SoundPlayer Pattern
- `SoundPlayer` at `SoundPlayer.kt:12` - uses `MediaPlayer`, wraps `prepareAsync()` + `isLooping = true`
- Volume already set at `SoundPlayer.kt:48`: `setVolume(1.0f, 1.0f)` - hardcoded to max
- `sounds` array at `SoundPlayer.kt:27` - 4 MP3 files indexed 0-3
- `playWithLoop(index: Int)` at `SoundPlayer.kt:38` - stops previous, starts new

### Compose UI Pattern
- Single `MainLayout()` composable with `Scaffold` + `Column`
- State: `mutableIntStateOf(-1)` for selectedOption, `mutableStateOf(false)` for isVolumeLow
- Buttons rendered via `buttonsText.forEachIndexed`
- AdView at bottom of Column

### Headphone Detection (Android API)
- `AudioManager.getDevices(AudioDeviceInfo.GET_DEVICES_OUTPUTS)` - API 23+
- Check for `AudioDeviceInfo.TYPE_WIRED_HEADSET`, `TYPE_WIRED_HEADPHONES`, `TYPE_BLUETOOTH_A2DP`, `TYPE_BLE_HEADSET`, `TYPE_USB_HEADSET`
- `AudioManager.registerAudioDeviceCallback()` - API 23+ for live detection

## Constraints

- [x] Min SDK 23 - all needed APIs available (`AudioDeviceInfo`, `MediaPlayer.setVolume()`)
- [x] `MediaPlayer.setVolume(left, right)` operates on 0.0f to 1.0f scale, independent of system volume
- [x] No new permissions needed - headphone detection doesn't require any special permission
- [x] Auto-advance timer should be cancellable (user taps to stop/skip)
- [x] Dark UI in session mode must coexist with normal theme (conditional theming)
- [x] Single-activity architecture - session mode is a UI state within EarActivity, not a separate activity

## Dependencies

```
EarActivity → SoundPlayer (plays sounds)
EarActivity → AudioManager (headphone detection)
SoundPlayer → MediaPlayer.setVolume() (adjustable volume)
Session mode UI → LaunchedEffect + delay() (auto-advance timer)
Session mode UI → darkColorScheme (dark theme)
```

## Tests

- Style: No existing unit tests in this worktree (baseline master)
- Instrumented tests: `EarActivityTest.kt` uses Robot pattern
- Testable logic: auto-advance sequence timing, headphone detection helper, volume clamping
- Unit testable: `isHeadphoneConnected()` is hard to unit test (needs AudioManager mock), but the auto-advance state machine logic can be tested as pure functions

## Risks

| Risk | Severity | Mitigation |
|------|----------|------------|
| Auto-advance timing feels wrong | Medium | Default 5 seconds per string, clearly communicated in UI |
| Headphone detection unreliable on some devices | Low | Show warning but don't block usage; Bluetooth detection may lag |
| Dark theme clashes with existing UI | Low | Use standard Material3 darkColorScheme, consistent with accent colors |
| Session mode complexity bloats EarActivity | Medium | Keep session mode as a distinct Composable section, conditionally rendered |
| User confused by two modes | Low | Clear toggle with descriptive label, snackbar explanation on first use |

## Recommendations

1. **SoundPlayer changes**: Add `var volume: Float = 1.0f` field. Apply in `playWithLoop()` when setting up MediaPlayer. Add helper `fun isHeadphoneConnected(): Boolean` using `AudioManager.getDevices()`.

2. **Session mode state**: Add `sessionModeActive: MutableState<Boolean>` to MainLayout. When active, replace normal UI with session mode UI.

3. **Session mode UI**: Dark background, large string labels showing current string being tuned, countdown/progress indicator, tap-to-skip functionality. Hide ad banner in session mode.

4. **Auto-advance**: Use `LaunchedEffect(sessionModeActive)` with a loop: play string 0 for N seconds, advance to string 1, etc. Stop after string 3 or when user taps stop.

5. **Volume control**: Add a slider (0.1 to 1.0) in session mode for playback volume. Default to 0.3 (quiet for earbuds).

6. **Headphone detection**: Check on session mode activation. If no headphones detected, show snackbar warning but allow proceeding (user may have speaker they want to use quietly).

7. **Persistence**: Save session volume preference in SharedPreferences. Don't persist session mode active state (always start in normal mode).
