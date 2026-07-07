# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Banjen is an Android banjo tuner app (package: `com.makingiants.android.banjotuner`). It is an **ear-training tuner**: tap a string and it plays a looping reference tone you match by ear. Tones are **synthesized live as sine waves** (no audio assets). It also has an optional **mic-based pitch check** (YIN algorithm) that shows flat/sharp/in-tune feedback. Single-activity Jetpack Compose UI with a Play-services-ads-lite banner and Firebase Crashlytics.

**Primary user**: Harold M. persona — older beginner who prefers ear-training over visual tuners. Simplicity is the core value proposition: "The simplest way to tune your banjo by ear."

This repo hosts both the original Android app and a newer iOS port (SwiftUI, bundle id `com.banjen.ios`). Most guidance below (product context, personas, pains, features) applies to both platforms; build/architecture sections are split.

## Repository Layout

The Android/Gradle project lives under **`android/`** (Gradle wrapper, `app/`, `baselineprofile/`, `build.gradle`, `settings.gradle`, `fastlane/`, `Gemfile`). The iOS app lives under **`ios/`** (`Banjen.xcodeproj`, `Banjen/` app target, `BanjenCore/` local Swift package). The repo root keeps only docs (`docs/`, `README.md`, `CLAUDE.md`), general scripts (`scripts/`), `Makefile`, and CI (`.github/`). Run Gradle/Fastlane from `android/`, or use the root `make` targets (which `cd android` for you).

## Build Commands

```bash
make build          # Release APK (cd android && ./gradlew assembleRelease)
make run            # Uninstall + install debug + launch (DEVICE=<serial> for multi-device)
make run_release    # Uninstall + install release
make test           # Unit tests (cd android && ./gradlew test) — JVM tests in android/app/src/test/
make format         # Format Kotlin with ktlint 1.5.0 (auto-downloads to .ktlint/)

# Single unit test class / method (run from android/)
(cd android && ./gradlew test --tests "com.makingiants.android.banjotuner.ToneGeneratorTest")
(cd android && ./gradlew test --tests "com.makingiants.android.banjotuner.PitchDetectorTest.detect*")

# Instrumented (Compose UI) tests — requires connected device/emulator
(cd android && ./gradlew connectedAndroidTest)

# Full build with coverage (jacoco runs automatically)
(cd android && ./gradlew build)
```

## iOS Build Commands

```bash
# Run BanjenCore unit tests (pure Swift, no simulator needed)
(cd ios/BanjenCore && swift test)

# Build/test the app target — needs a simulator destination
xcodebuild -project ios/Banjen.xcodeproj -scheme Banjen -destination 'platform=iOS Simulator,name=iPhone 16' build
xcodebuild -project ios/Banjen.xcodeproj -scheme Banjen -destination 'platform=iOS Simulator,name=iPhone 16' test
```

Crashlytics stays disabled until a real `GoogleService-Info.plist` (bundle id `com.banjen.ios`) is dropped into `ios/Banjen/`; Google Mobile Ads uses Google's test app ID until replaced before shipping (see `ios/Banjen/BanjenApp.swift`).

## iOS Architecture

`ios/BanjenCore` is a local Swift package (Swift 6, iOS 17+/macOS 13+) holding all platform-independent logic, mirroring the Android core files 1:1: `AppConstants`, `PitchDetector`, `ToneMath`, `TuningAnimationState`, `TuningModel`. It has its own test target (`BanjenCoreTests`) runnable without a simulator via `swift test`. The `ios/Banjen` app target is SwiftUI and depends on `BanjenCore`:

- **`BanjenApp`** — `@main` entry point; conditionally configures Firebase Crashlytics and starts Google Mobile Ads.
- **`ContentView`** / **`EarView`** / **`EarViewModel`** — top-level screen and its observable view model (Android's `EarActivity` equivalent, split into view + view model).
- **`Audio/ToneGenerator.swift`** / **`Audio/PitchCaptureEngine.swift`** — tone synthesis/looping and mic-based pitch capture, the iOS counterparts of Android's `ToneGenerator` + `AudioRecord` loop.
- **`Views/BanjoStringCanvas.swift`**, **`Views/TuningAnimationView.swift`**, **`Views/SettingsSheet.swift`**, **`Views/AdBannerView.swift`** — SwiftUI equivalents of the Android Canvas views, tuning animation, settings, and banner ad.
- **`Theme/AppColors.swift`** — SwiftUI color theme, kept in sync with Android's Compose theme for UI parity (see recent `feat(ui): unify play/stop into one animated toggle and match iOS to Android`).
- **`Analytics.swift`** — Firebase Analytics wrapper.

When changing shared logic (tone math, pitch detection, tuning models, constants), update both `android/app/src/main/java/.../{ToneGenerator,PitchDetector,TuningModel,AppConstants}.kt` and their `ios/BanjenCore/Sources/BanjenCore/*.swift` counterparts to keep behavior identical across platforms.

## Architecture

Single-module Gradle project (`android/app/`). All production code lives in one package, `com.makingiants.android.banjotuner`. Single activity, no ViewModel, no DI, no navigation — state is held in Compose `mutableStateOf`/`SharedPreferences`. Files:

- **`EarActivity`** (~1100 lines) — The whole UI. `ComponentActivity` that sets Compose content directly. Owns `ToneGenerator`, `PitchDetector`, the `selectedStringIndex`, `pitchCheckMode`, `sessionModeActive` state, the `referencePitch`, and the current instrument/tuning selection. Hosts string buttons, the tuning animation, instrument/tuning dropdowns, reference-pitch (A=) control, banner ad, and the snackbar/volume-low alert. `AudioCaptureEffect` runs the `AudioRecord` → `PitchDetector` loop while pitch-check is active. Reads an autoplay `string_index` intent extra (from the home-screen widget). Tears down audio in `exitSessionMode`/`onPause`.
- **`ToneGenerator`** — Synthesizes and loops a sine tone via `AudioTrack` (`MODE_STATIC`, hardware loop points). No MP3 assets. Key tricks, all to kill click/cold-start noise: a permanent silent **warm-up track** keeps the audio path hot; 200ms fade-in/out; `calculateLoopSampleCount` picks a loop length whose boundary minimizes `1-cos` (full-cycle, not half-cycle) to avoid a phase-flip thump. Coroutine-driven; `play()`/`stop()`/`release()` are main-thread-safe.
- **`PitchDetector`** — Pure-Kotlin YIN pitch detection over a `FloatArray`. `detectPitch` → Hz, `centsFromTarget`, `classifyTuning` → `TuningStatus` (IN_TUNE/CLOSE/SHARP/FLAT/NO_SIGNAL). Also defines `PitchResult` and the `BanjoString` enum.
- **`TuningModel`** — Data model for instruments/tunings: `Note`, `Tuning`, `Instrument`, the `FOUR_STRING_BANJO`/`FIVE_STRING_BANJO` catalogs (DGBD, Irish GDAE, Chicago DGBE, Plectrum CGBD, plus 5-string Open G/Double C/Modal/Drop C/Open D), `noteFrequency(midi)`, and `encodeTuning`/`decodeTuning` for persistence.
- **`AppConstants`** — Reference-pitch range (432–446 Hz, default 440), `SharedPreferences` keys (`banjen_prefs`), session-mode constants (`SECONDS_PER_STRING`, session volume), and small pure helpers (`clampPitch`, `calculatePitchRatio`, `autoAdvanceNextIndex`, `clampVolume`). Heavily unit-tested.
- **`BanjoStringCanvas`** (~730 lines) — Custom-drawn string visualization with per-string semantic (accessibility) buttons overlaid on the Canvas.
- **`TuningAnimation`** — Canvas-based concentric-ring "breathing" animation. **Rive was removed** (APK size); `deriveTuningAnimationState`, `pitchCheckMode`, and cent-deviation are still computed for future reuse but only the Canvas fallback renders.
- **`TunerWidget`** — Glance home-screen app widget (`TunerWidgetReceiver`). Four string buttons that `actionStartActivity<EarActivity>` with a `string_index` extra. Pulls in WorkManager+Room transitively (see ProGuard note below).
- **`AppIcons`** — Hand-inlined vector icons (Remove only). Stop kept as PNG drawable (from design source). `material-icons-extended` was dropped (~10–15 MB) to shrink the APK.

Instrumented tests use a Robot pattern (`EarRobot`/`withEarRobot`) with `AndroidComposeTestRule`. Unit tests now exist and are substantial — `android/app/src/test/` covers `ToneGenerator`, `PitchDetector`, pitch math, `TuningModel`, session mode, widget, and animation state.

## Key Config

- **Build tooling**: AGP **9.0.1**, Kotlin **2.3.10**, Compose BOM **2026.02.00**, Firebase BOM **34.9.0**, JDK 17 source/target. CI builds with JDK 21.
- **`android.enableR8.fullMode=true`** (`gradle.properties`). Release uses `minifyEnabled`/`shrinkResources` + AAB ABI/density/language splits. R8 full mode strips reflective members Room/WorkManager need — `android/app/rules.pro` keeps the Room/WorkManager/Startup surface, and `packaging` must NOT exclude `META-INF/proguard/*.pro` (consumer rules), or `InitializationProvider` crashes on launch.
- **Signing & ads**: read from gradle properties / env (`BANJEN_SIGN_*`, `BANJEN_ADS_UNIT_ID_BANNER`, `BANJEN_ADMOB_APP_ID`); injected as `resValue`. CI reads from GitHub secrets. See `gradle.properties.example`.
- **`dependencies.gradle`**: `setup.targetSdk` (36), `setup.minSdk` (23), `deps.kotlin`.
- **Localization**: en (default), es, pt, it — `res/values-*/strings.xml` (and `resConfigs`).
- **Ads**: `play-services-ads-lite` (not the full SDK). Logging via Timber.
- **Release size/perf hardening** (applies to every release artifact — CI, Fastlane deploy, local `assembleRelease`): `rules.pro` strips Log V/D/I (`-maximumremovedandroidloglevel 4`), Compose trace markers, and Kotlin null-check intrinsics (`-processkotlinnullchecks remove`), plus `-repackageclasses`; `build.gradle` sets release-only Kotlin flags (`jvmDefault NO_COMPATIBILITY`, `-Xno-*-assertions`), disables `dependenciesInfo`/`vcsInfo`, and excludes license/metadata files from packaging. Do NOT add blanket `-keep class androidx.work.**` rules — the AAR consumer rules cover the reflective surface.
- **Baseline Profile**: `:baselineprofile` test module generates an ART profile (committed at `android/app/src/release/generated/baselineProfiles/baseline-prof.txt`, merged into every release build; CI needs no device). Regenerate after major UI/startup changes with a device attached: `(cd android && ./gradlew :app:generateBaselineProfile)`. Benchmark plugin is 1.5.0-alpha (stable 1.4.x rejects AGP 9).

## Release Automation (Fastlane)

`android/fastlane/Fastfile` `deploy` lane drives versioning from **conventional commits** (`analyze_commits`): `feat`→minor, `fix`/`perf`→patch, breaking→major. It patches `appVersionName`/`appVersionCode` in `android/app/build.gradle`, commits, builds the AAB, uploads to Play Store **internal/draft** track, and tags. So commit-message types directly drive the next published version — follow conventional commits strictly. Ruby 3.3.10 (`.ruby-version`). `make deploy-metadata` uploads store metadata only.

## Known Technical Debt

- **`RECORD_AUDIO` is not declared in any manifest**, yet `EarActivity` requests it at runtime for pitch-check — the runtime grant will fail. Add the permission before relying on mic-based tuning.
- Legacy XML resources (styles, dimens, drawables) partly unused.

## Product Context

Full product documentation lives in `docs/product/`:

- **`ux-report.md`** — SUPERSEDED by v3 UX investigation. Historical reference only.
- **`user-personas.md` / `pains.md` / `features.md` (root of docs/product/)** — v2 (Feb 2026). SUPERSEDED by v3. Kept side-by-side for diff/history.
- **`codebase-investigation.md`** — Technical deep-dive: architecture patterns, deprecated APIs, build deps, test gaps.
- **`features/`** — Each planned feature has a `1-investigation.md` and `2-plan.md` (see v3 features.md for current prioritization).

## UX Investigation (v3 — May 2026)

WHY: Hard constraints prevent common pitfalls. Discipline on critical operations prevents irreversible mistakes.

Before building features or making product decisions, agents MUST read these v3 files:

- **User Personas:** `docs/product/v3/user-personas.md` — 5 evidence-grounded personas refined from fresh research + simulated interviews. Harold (primary), Lúcia (cavaquinho), Eileen (Irish tenor GDAE), Wendell (CGBD plectrum, tech 1/5), Marcus (skeptic, deleted Banjen).
- **User Pains:** `docs/product/v3/pains.md` — 49 pains across 8 categories, 10 cross-persona patterns, 5 inviolable constraints. Adds discovery-surface gates (language, instrument label, family install-gate) absent from v2.
- **Feature Opportunities:** `docs/product/v3/features.md` — 21 features across 4 tiers, 6-sprint plan. Top P0: CLS-safe banner ad (off tuning surface). Top growth: pt-BR Play Store listing with literal "Cavaquinho" in title.

**Primary persona:** Harold M. (67, retired beginner, DGBD) — beachhead unchanged. Hidden cognitive-health motivation is now actively-protected clinical disclosure — copy must NOT surface dementia framing.

**Universal dealbreaker:** Any ad, audio interruption, or layout shift on the tuning surface = permanent uninstall across all 5 personas. Marcus named the precise bug (Cumulative Layout Shift on banner ad slot).

**Highest-leverage zero-code opportunity:** pt-BR Play Store listing with "afinador de cavaquinho" in title. Lúcia did not know cavaquinho and banjo share DGBD until interview — the product is already built for her; only metadata is missing.

**Pre-install gates (new in v3):** 3/5 personas reject Banjen at the discovery surface, never reaching the product. Lúcia closes English results before clicking; Eileen closes the tab if GDAE isn't visible in 5 seconds; Wendell cannot install without his daughter Camille.

Every feature built should reference at least one v3 persona and one v3 pain.

## CI/CD

- **Build** (`.github/workflows/build.yaml`): Runs on all branch pushes. Gradle build with JDK 21.
- **Deploy** (`.github/workflows/deploy.yaml`): Runs on push to master (and manual `workflow_dispatch`). Executes Fastlane `deploy` lane: if releasable conventional commits since last tag, bumps version + commits + pushes, builds AAB, uploads to Play internal/draft, tags. Safe no-op otherwise.

## Guidelines

- **Code search**: Always use ast-grep for structural code search instead of grep/ripgrep. Fetch https://ast-grep.github.io/llms.txt for reference when writing ast-grep rules, and use the `ast-grep:ast-grep` skill for guidance.
