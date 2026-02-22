# Investigation: Banjen — Android 4-String Banjo Tuner

## Summary

Exhaustive investigation of the Banjen codebase, a minimal single-activity Android banjo tuner app. The app has only 2 production Kotlin files (`EarActivity.kt`, `SoundPlayer.kt`), uses Jetpack Compose for UI, `MediaPlayer` for audio, AdMob for monetization, and Firebase Crashlytics for crash reporting. Categories investigated: Architecture & Patterns, Dependencies & Integration, State Management, Testing Infrastructure, Performance & Constraints, Error Handling, Domain-Specific (audio playback).

## Files

| File | Purpose | Relevance |
|------|---------|-----------|
| `app/src/main/java/.../EarActivity.kt` | Single activity; Compose UI with 4 tuning string buttons, animations, AdMob banner, volume-low alert | **Core** — all UI logic lives here |
| `app/src/main/java/.../SoundPlayer.kt` | Wraps `MediaPlayer` for looping playback of MP3 assets from `assets/b_sounds/` | **Core** — all audio logic lives here |
| `app/src/androidTest/java/.../EarActivityTest.kt` | Instrumented tests for play/stop per string | **Testing** |
| `app/src/androidTest/java/.../EarRobot.kt` | Robot pattern helper for Compose UI testing | **Testing** |
| `app/build.gradle` | App module build config: plugins, SDK versions, signing, dependencies, ProGuard | **Build** |
| `build.gradle` | Root project: plugin versions (AGP 9.0.1, Kotlin 2.3.10, Firebase Crashlytics 3.0.6) | **Build** |
| `dependencies.gradle` | Shared ext properties: `setup.targetSdk=36`, `setup.minSdk=23`, `deps.kotlin=2.3.10` | **Build** |
| `jacoco.gradle` | JaCoCo code coverage configuration for unit tests | **Build/Quality** |
| `settings.gradle` | Single module `:app`, repository config | **Build** |
| `gradle.properties` | JVM args, AdMob IDs (live values checked in) | **Config** |
| `gradle.properties.example` | Template for required signing and ads config keys | **Config** |
| `.github/workflows/build.yaml` | CI: builds on all branches, JDK 21, `./gradlew build` | **CI/CD** |
| `.github/workflows/deploy.yaml` | CD: builds release AAB on master push, uploads as artifact | **CI/CD** |
| `app/src/main/AndroidManifest.xml` | App manifest: single activity, INTERNET/NETWORK permissions, AdMob meta-data | **Config** |
| `app/src/debug/AndroidManifest.xml` | Debug manifest: adds WRITE_EXTERNAL_STORAGE, DISABLE_KEYGUARD, WAKE_LOCK for testing | **Config** |
| `app/src/main/res/values/strings.xml` | Default (English) strings: button labels (1-D, 2-B, 3-G, 4-D), volume_low_message | **Resources** |
| `app/src/main/res/values-es/strings.xml` | Spanish localization (solfege names: Re, Si, Sol, Re) | **Resources** |
| `app/src/main/res/values-pt/strings.xml` | Portuguese localization | **Resources** |
| `app/src/main/res/values-it/strings.xml` | Italian localization | **Resources** |
| `app/src/main/res/values/colors.xml` | Color palette: dark primary, accent #6d94a1, grays | **Resources** |
| `app/src/main/res/values/styles.xml` | AppTheme (AppCompat.NoActionBar), legacy EarButton style (unused by Compose) | **Resources** |
| `app/src/main/res/values/dimens.xml` | Single dimen `spacing_4dp` (likely unused in Compose code) | **Resources** |
| `app/src/main/res/anim/shake_animation.xml` | XML rotate animation (legacy, loaded but unused — Compose uses its own shake) | **Resources** |
| `app/src/main/res/drawable/button.xml` | Selector drawable for checked/unchecked states (legacy, unused by Compose) | **Resources** |
| `app/src/main/assets/b_sounds/1.mp3` | Audio asset: string 1 reference tone | **Audio** |
| `app/src/main/assets/b_sounds/2.mp3` | Audio asset: string 2 reference tone | **Audio** |
| `app/src/main/assets/b_sounds/3.mp3` | Audio asset: string 3 reference tone | **Audio** |
| `app/src/main/assets/b_sounds/4.mp3` | Audio asset: string 4 reference tone | **Audio** |
| `app/rules.pro` | Release ProGuard: keeps Room, WorkManager, Startup (none used — over-broad) | **Build** |
| `app/rules-test-e2e.pro` | Debug ProGuard: keeps JaCoCo agent, Kotlin metadata | **Build** |
| `Makefile` | Build shortcuts: build, run, test, format (ktlint 1.5.0) | **Tooling** |
| `CHANGELOG.md` | Historical changelog (stops at v1.5.5, current is v1.7.0) | **Docs** |
| `README.md` | Project overview, Play Store link, Apache 2.0 license | **Docs** |
| `docs/PRIVACY_POLICY.md` | Privacy policy document | **Docs** |

## Patterns

### Architecture Patterns
- **Single-Activity Compose**: `EarActivity` extends `AppCompatActivity`, calls `setContent { Contents() }` directly — no ViewModel, no navigation, no fragments @ `EarActivity.kt:83-88`
- **Direct Compose in Activity**: All `@Composable` functions are instance methods of `EarActivity`, not standalone — tightly couples UI to activity lifecycle @ `EarActivity.kt:95-270`
- **Lazy initialization**: `SoundPlayer` and `clickAnimation` use Kotlin `by lazy` delegates @ `EarActivity.kt:68,79`
- **No dependency injection**: Direct instantiation, no Hilt/Dagger/Koin @ `EarActivity.kt:68`

### State Management
- **In-Compose state**: `mutableIntStateOf(-1)` for selected button index, `mutableStateOf(false)` for volume-low flag — all state is local to the composition @ `EarActivity.kt:135-136`
- **No ViewModel**: State is not preserved across config changes (activity recreation) — sound stops on rotation via `onPause()` @ `EarActivity.kt:90-93`
- **Imperative player state**: `SoundPlayer.isPlaying` queries `MediaPlayer` directly; no reactive state flow @ `SoundPlayer.kt:17`

### Audio Playback
- **MediaPlayer lifecycle**: Create new instance per play, mute-stop-reset-release on stop @ `SoundPlayer.kt:38-76`
- **Looping via MediaPlayer**: `isLooping = true` on the `MediaPlayer` instance, not manual restart @ `SoundPlayer.kt:55`
- **Asset file descriptor**: Opens from `assets/b_sounds/{1-4}.mp3` via `context.assets.openFd()` @ `SoundPlayer.kt:43`
- **Stream muting on stop**: Uses deprecated `AudioManager.setStreamMute()` to suppress click artifacts @ `SoundPlayer.kt:66,75`

### UI / Animation
- **Compose scale + shake**: `animateFloatAsState` for 1x-to-3x scale, `infiniteRepeatable` tween for horizontal shake on selected button @ `EarActivity.kt:176-189`
- **Volume-low indicator**: Shows shaking `VolumeOff` icon + snackbar when media volume < 50% @ `EarActivity.kt:191-257`
- **Dark theme by default**: Uses `primary_material_dark` as background, accent `#6d94a1` @ `colors.xml:3-8`

### Testing
- **Robot pattern**: `EarRobot` wraps compose test interactions, `Assert` inner class validates player state @ `EarRobot.kt:11-46`
- **Polling-based assertions**: `waitForPlayer()` busy-waits up to 3s for `MediaPlayer` state to stabilize @ `EarRobot.kt:36-44`
- **`@VisibleForTesting`**: `player` and `clickAnimation` exposed as `internal` for test access @ `EarActivity.kt:67-68,78-81`

### Build & CI
- **Single-module Gradle**: Root `build.gradle` declares plugins, `app/build.gradle` is the sole module @ `settings.gradle:17`
- **Compose BOM**: `androidx.compose:compose-bom:2026.02.00` manages all Compose versions @ `app/build.gradle:99`
- **Firebase BOM**: `com.google.firebase:firebase-bom:34.9.0` manages Crashlytics + Analytics @ `app/build.gradle:95`
- **Config via gradle.properties**: Ad IDs and signing keys resolved via `customProperty()` fallback to env vars @ `app/build.gradle:14-20`
- **No automated Play Store upload**: Deploy workflow builds AAB and uploads as GitHub artifact only @ `deploy.yaml:56-61`

## Constraints

- [x] **Min SDK 23** — limits API surface (e.g., no `AudioManager.adjustStreamVolume` flags added in SDK 28) @ `dependencies.gradle:4`
- [x] **Java 17 source/target** @ `app/build.gradle:75-76`
- [x] **JDK 21 for CI** @ `build.yaml:14`
- [x] **Single activity only** — all logic in one class, no navigation framework @ `AndroidManifest.xml:26-35`
- [x] **No ViewModel** — state loss on config change is by design (sound stops on pause anyway) @ `EarActivity.kt:90-93`
- [x] **4 language locales** — en (default), es, pt, it; `resConfigs` restricts to these only @ `app/build.gradle:39`
- [x] **Live AdMob IDs in gradle.properties** — `ca-app-pub-1572359805006702/...` committed to repo @ `gradle.properties:16-17`

## Deps

```
EarActivity → SoundPlayer (direct instantiation)
EarActivity → Jetpack Compose (Material3, animations, AndroidView)
EarActivity → AdMob (AdView, AdRequest, MobileAds)
SoundPlayer → android.media.MediaPlayer
SoundPlayer → android.media.AudioManager
App → Firebase Crashlytics (plugin + BOM)
App → Firebase Analytics (BOM)
Build → AGP 9.0.1
Build → Kotlin 2.3.10 + Compose Plugin
Build → JaCoCo (unit test coverage)
Build → ktlint 1.5.0 (via Makefile)
```

### Dependency Versions (as of codebase)

| Dependency | Version |
|------------|---------|
| AGP | 9.0.1 |
| Kotlin | 2.3.10 |
| Compose BOM | 2026.02.00 |
| Firebase BOM | 34.9.0 |
| Firebase Crashlytics plugin | 3.0.6 |
| Google Services plugin | 4.4.4 |
| play-services-ads-lite | 24.8.0 |
| appcompat | 1.7.1 |
| activity-compose | 1.12.4 |
| JUnit | 4.13.2 |
| Espresso | 3.7.0 |
| AndroidX Test JUnit | 1.3.0 |
| AndroidX Test Runner | 1.7.0 |
| ktlint | 1.5.0 |

## Tests

- **Style**: Instrumented (Compose UI) + JUnit 4
- **Location**: `app/src/androidTest/java/com/makingiants/android/banjotuner/`
- **Files**: `EarActivityTest.kt` (2 test methods), `EarRobot.kt` (robot helper)
- **Pattern**: Robot pattern — `withEarRobot { click(index) }.assert { checkIsPlaying() }`
- **Coverage**: Tests only play/stop for each of the 4 strings. No unit tests exist in `src/test/`.
- **Framework**: `AndroidComposeTestRule<ActivityScenarioRule<EarActivity>>`, JUnit 4 runner
- **Gaps**: No unit tests for `SoundPlayer` in isolation. No edge-case tests (rapid tap, pause during play, volume change). No UI snapshot/screenshot tests. JaCoCo configured but only for unit tests (which don't exist).

## Risks

| Risk | Sev | Mitigation |
|------|-----|------------|
| **Deprecated `setStreamMute()`** — `AudioManager.setStreamMute()` deprecated since API 23, may mute globally affecting other apps | High | Replace with `AudioManager.adjustStreamVolume(ADJUST_MUTE/ADJUST_UNMUTE)` or use `MediaPlayer.setVolume(0,0)` before stop |
| **`setAudioStreamType()` deprecated** — Deprecated since API 21, replaced by `AudioAttributes` | Medium | Migrate to `MediaPlayer.setAudioAttributes(AudioAttributes.Builder()...)` |
| **No `prepareAsync` error handling** — `prepareAsync()` can fail silently if the asset is corrupt; no `OnErrorListener` set | Medium | Add `setOnErrorListener` to `MediaPlayer` in `SoundPlayer.playWithLoop()` |
| **Live AdMob IDs in version control** — Production ad unit IDs committed to `gradle.properties` | Medium | Move to `local.properties` or environment-only; add to `.gitignore` |
| **Legacy XML resources unused** — `styles.xml:EarButton`, `dimens.xml:spacing_4dp`, `drawable/button.xml`, `anim/shake_animation.xml` are vestigial from pre-Compose UI | Low | Remove unused resources to reduce APK size and confusion |
| **`clickAnimation` loaded but unused** — `EarActivity.clickAnimation` loads XML animation via `AnimationUtils` but no code references it after loading | Low | Remove the `clickAnimation` property entirely |
| **ProGuard rules for unused libraries** — `rules.pro` keeps Room, WorkManager, Startup classes that are not in dependencies | Low | Clean up ProGuard rules to match actual dependencies |
| **CHANGELOG.md stale** — Last entry is v1.5.5, current version is v1.7.0 | Low | Update changelog or remove if not maintained |
| **No unit tests** — JaCoCo coverage runs on `src/test/` which has no tests; only instrumented tests exist | Low | Add unit tests for `SoundPlayer` logic (volume check, play/stop state machine) |
| **`targetSandboxVersion="2"`** — Instant App sandbox attribute in manifest is legacy; instant apps no longer actively supported | Low | Remove `android:targetSandboxVersion="2"` from manifest |

## Recommendations

1. **Remove deprecated API usage**: Replace `setStreamMute()` with `MediaPlayer.setVolume(0f,0f)` before stopping (scoped to the player, not system-wide). Replace `setAudioStreamType()` with `AudioAttributes.Builder().setUsage(USAGE_MEDIA).setContentType(CONTENT_TYPE_MUSIC).build()`.

2. **Clean dead code**: Remove `clickAnimation` property, legacy XML styles (`EarButton`), `dimens.xml:spacing_4dp`, `drawable/button.xml`, `anim/shake_animation.xml`, and instant app `targetSandboxVersion` attribute. Clean ProGuard `rules.pro` to remove Room/WorkManager/Startup keeps.

3. **Improve error resilience**: Add `setOnErrorListener` to `MediaPlayer` instances in `SoundPlayer.playWithLoop()` to catch preparation/playback errors and surface them to the UI or Crashlytics.

4. **Secure config**: Move live AdMob IDs out of `gradle.properties` to `local.properties` (already gitignored) or rely exclusively on environment variables.

5. **Add unit tests**: Create `SoundPlayer` unit tests (mockable `Context` + `AudioManager`) for `isVolumeLow()`, play/stop state transitions. This would give the existing JaCoCo setup actual coverage data to report on.

6. **Consider ViewModel extraction**: While the current no-ViewModel approach works for this simple app, extracting state to a `ViewModel` would enable unit-testable UI logic and proper state preservation if the app grows in complexity.
