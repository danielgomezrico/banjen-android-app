# Investigation: Add Adjustable Reference Pitch (A=432-446 Hz)

## Summary
Investigated how to add adjustable reference pitch to Banjen banjo tuner. The key finding is that `MediaPlayer.setPlaybackParams(PlaybackParams().setPitch(ratio))` is available since API 23 (matches minSdk 23), allowing pitch-shifting of existing MP3 assets without replacing the audio engine. The implementation requires changes to `SoundPlayer` (apply pitch ratio), `EarActivity` (UI control), persistence via `SharedPreferences`, and localized string resources.

## Files

| File | Purpose | Relevance |
|------|---------|-----------|
| `app/src/main/java/.../SoundPlayer.kt` | MediaPlayer wrapper for looping MP3 playback | **Primary** - must apply pitch ratio via `setPlaybackParams` after prepare |
| `app/src/main/java/.../EarActivity.kt` | Single activity with Compose UI, 4 string buttons | **Primary** - must add pitch adjustment UI control |
| `app/src/main/res/values/strings.xml` | English string resources | Add pitch label strings |
| `app/src/main/res/values-es/strings.xml` | Spanish string resources | Localized pitch label |
| `app/src/main/res/values-pt/strings.xml` | Portuguese string resources | Localized pitch label |
| `app/src/main/res/values-it/strings.xml` | Italian string resources | Localized pitch label |
| `app/src/main/res/values/colors.xml` | Color definitions | Reference only - reuse existing colors |
| `app/src/androidTest/.../EarActivityTest.kt` | Instrumented UI tests (Robot pattern) | May need extension for pitch UI |
| `app/src/androidTest/.../EarRobot.kt` | Robot helper for Compose testing | May need extension for pitch UI |
| `dependencies.gradle` | SDK versions (minSdk 23) | Confirms API 23+ available |

## Patterns

### SoundPlayer Pattern
- `SoundPlayer` at `SoundPlayer.kt:12` - constructor takes `Context`, implements `OnPreparedListener`, `OnCompletionListener`
- `playWithLoop(index: Int)` at `SoundPlayer.kt:38` - creates new `MediaPlayer`, loads asset by index, calls `prepareAsync()`, sets `isLooping = true`
- `onPrepared()` at `SoundPlayer.kt:80` - calls `mediaPlayer?.start()` after async prepare completes
- `stop()` at `SoundPlayer.kt:65` - mutes stream, stops/resets/releases player, unmutes
- Sound files: `assets/b_sounds/1.mp3` through `4.mp3` (four ~101KB MP3 files)

### Compose UI Pattern
- `EarActivity.Contents()` at `EarActivity.kt:97` - root composable with MaterialTheme
- `MainLayout()` at `EarActivity.kt:118` - Scaffold with Column of 4 buttons + AdView
- `Button()` at `EarActivity.kt:165` - individual string button with scale+shake animation
- State management: `mutableIntStateOf(-1)` for selectedOption, `mutableStateOf(false)` for isVolumeLow
- Player accessed via `player` lazy property at `EarActivity.kt:68`

### Test Pattern (Robot)
- `EarRobot` at `EarRobot.kt:11` - Robot pattern with `click(buttonIndex)` and nested `Assert` class
- `withEarRobot()` at `EarRobot.kt:49` - DSL builder function
- Uses `AndroidComposeTestRule` with `createAndroidComposeRule<EarActivity>()`
- No unit tests exist (only instrumented/androidTest)

### Localization Pattern
- 4 locales: en (default), es, pt, it
- Note names localized: D/B/G → Re/Si/Sol (es, pt, it)
- All string files follow same structure

## Constraints

- [x] Min SDK 23 - `PlaybackParams.setPitch()` requires API 23+, confirmed compatible
- [x] `MediaPlayer.setPlaybackParams()` must be called AFTER `prepare()`/`onPrepared()` - the current code uses `prepareAsync()` so pitch must be applied in `onPrepared()` callback
- [x] Pitch ratio calculation: `ratio = targetHz / 440.0f` where target is A=432 to A=446
- [x] Valid pitch range: 432/440 = 0.98182 to 446/440 = 1.01364 - well within MediaPlayer's supported range
- [x] No new dependencies needed - `PlaybackParams` is in `android.media` (standard library)
- [x] SharedPreferences for persistence - no DataStore dependency exists, SharedPreferences is simpler and sufficient
- [x] The `playWithLoop` currently takes only `index: Int` - needs a pitch ratio parameter or access to stored pitch value

## Dependencies

```
EarActivity → SoundPlayer (plays sounds)
EarActivity → SharedPreferences (will store pitch setting)
SoundPlayer → MediaPlayer → PlaybackParams (will set pitch)
EarActivity.Button → SoundPlayer.playWithLoop (click handler)
```

## Tests

- Style: Instrumented (androidTest) only; no unit tests exist
- Location: `app/src/androidTest/java/com/makingiants/android/banjotuner/`
- Pattern: Robot (`EarRobot`/`withEarRobot`) with compose test rule
- Gap: No unit test directory at `app/src/test/` - **need to create for SoundPlayer pitch logic unit tests**
- Pitch ratio calculation is pure math and can be unit tested without a device

## Risks

| Risk | Severity | Mitigation |
|------|----------|------------|
| `setPlaybackParams` may cause audio artifacts on some devices | Low | Pitch range is very narrow (0.98-1.01), well within safe bounds |
| `setPlaybackParams` must be called after prepare, not before | Medium | Apply in `onPrepared()` callback, store ratio as field |
| UI space is tight (4 buttons + ad banner fill screen) | Medium | Use compact design: single row with "A=440" text and +/- buttons, placed above or below buttons area |
| SharedPreferences blocking main thread | Low | Reads are fast for single int value; writes use `apply()` (async) |

## Recommendations

1. **SoundPlayer changes**: Add a `pitchRatio: Float` field (default 1.0f). Modify `playWithLoop()` to accept or read the pitch ratio. Apply `setPlaybackParams(PlaybackParams().setPitch(pitchRatio))` in `onPrepared()` after `start()`.

2. **Pitch calculation**: Create a small utility/helper or inline calculation: `pitchRatio = referencePitch / 440f`. This is pure math suitable for unit testing.

3. **UI control**: Add a compact pitch control row between the string buttons area and the ad banner. Show "A=440" with "-" and "+" buttons for 1Hz increments. Use existing color scheme (`banjen_accent` for text, matching button style).

4. **Persistence**: Use `SharedPreferences` with key `"reference_pitch"`, default `440`. Read on activity creation, write on change with `apply()`.

5. **State flow**: Store pitch as `mutableIntStateOf(440)` in Compose. Pass to `SoundPlayer` when playing. If already playing when pitch changes, restart playback with new pitch.

6. **Unit tests**: Create `app/src/test/java/com/makingiants/android/banjotuner/` directory. Add unit tests for pitch ratio calculation and bounds validation (432-446 range).

7. **Localization**: The "A=440" display is a musical notation standard and does not need translation. Only the label/description text needs localization (e.g., "Reference pitch" / "Tono de referencia" / etc.).
