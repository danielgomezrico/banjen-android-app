# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Banjen is an Android 4-string banjo tuner app (package: `com.makingiants.android.banjotuner`). It plays looping reference tones for each string (D, B, G, D). Single-activity Jetpack Compose UI with AdMob ads and Firebase Crashlytics.

## Build Commands

```bash
make build          # Release APK (./gradlew assembleRelease)
make run            # Install debug on connected device
make run_release    # Install release on connected device
make test           # Unit tests (./gradlew test)
make format         # Format Kotlin with ktlint 1.5.0

# Instrumented tests (requires connected device/emulator)
./gradlew connectedAndroidTest

# Full build with coverage (jacoco runs automatically)
./gradlew build
```

## Architecture

Single-module Gradle project (`app/`). Only two source files:

- **`EarActivity`** — Single activity, sets Compose content directly. Four string buttons with scale+shake animations. Tapping a button plays/stops the corresponding tone via `SoundPlayer`. AdMob banner at the bottom.
- **`SoundPlayer`** — Wraps `MediaPlayer` for looping playback of MP3 assets from `assets/b_sounds/`. Handles prepare/completion lifecycle, mutes stream during stop to avoid audio artifacts.

Instrumented tests use a Robot pattern (`EarRobot`/`withEarRobot`) for Compose UI testing with `AndroidComposeTestRule`.

## Key Config

- **Signing & ads config**: Set in `gradle.properties` (see `gradle.properties.example` for keys). CI reads from GitHub secrets.
- **`dependencies.gradle`**: Defines `setup.targetSdk`, `setup.minSdk`, and Kotlin version shared across build files.
- **Localization**: en (default), es, pt, it — string resources in `app/src/main/res/values-*/strings.xml`
- **Min SDK**: 23 | **Target SDK**: 36 | **Java**: 17

## CI/CD

- **Build** (`.github/workflows/build.yaml`): Runs on all branch pushes. Gradle build with JDK 21.
- **Deploy** (`.github/workflows/deploy.yaml`): Runs on master push. Builds release AAB, uploads as artifact.

## Guidelines

- **Code search**: Always use ast-grep for structural code search instead of grep/ripgrep. Fetch https://ast-grep.github.io/llms.txt for reference when writing ast-grep rules, and use the `ast-grep:ast-grep` skill for guidance.
