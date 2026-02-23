# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Banjen is an Android 4-string banjo tuner app (package: `com.makingiants.android.banjotuner`). It plays looping reference tones for each string (DGBD tuning). Single-activity Jetpack Compose UI with AdMob ads and Firebase Crashlytics.

**Primary user**: Harold M. persona — older beginner who prefers ear-training over visual tuners. Simplicity is the core value proposition: "The simplest way to tune your banjo by ear."

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

Single-module Gradle project (`app/`). Only two production source files:

- **`EarActivity`** — Single activity, sets Compose content directly (no ViewModel, no navigation). Four hardcoded string buttons (DGBD) with scale+shake animations. `mutableIntStateOf(-1)` tracks selected button. AdMob banner at the bottom. Volume-low alert with shaking icon + snackbar.
- **`SoundPlayer`** — Wraps `MediaPlayer` for looping playback of MP3 assets from `assets/b_sounds/` (files `1.mp3`–`4.mp3` = D3, G3, B3, D4). `stop()` mutes stream before stopping to avoid audio artifacts. `isVolumeLow()` checks if media volume < 50%.

Instrumented tests use a Robot pattern (`EarRobot`/`withEarRobot`) for Compose UI testing with `AndroidComposeTestRule`. No unit tests exist (JaCoCo is configured but `src/test/` is absent).

## Key Config

- **Signing & ads config**: Set in `gradle.properties` (see `gradle.properties.example`). CI reads from GitHub secrets.
- **`dependencies.gradle`**: Defines `setup.targetSdk`, `setup.minSdk`, and Kotlin version.
- **Localization**: en (default), es, pt, it — `app/src/main/res/values-*/strings.xml`
- **Min SDK**: 23 | **Target SDK**: 36 | **Java**: 17

## Known Technical Debt

- `AudioManager.setStreamMute()` is deprecated since API 23 (HIGH severity)
- `setAudioStreamType()` is deprecated; no `prepareAsync` error handling (MEDIUM)
- Live AdMob IDs committed to version control (MEDIUM)
- Legacy XML resources (styles, dimens, drawables) exist but are unused

## Product Context

Full product documentation lives in `docs/product/`:

- **`ux-report.md`** — SUPERSEDED by the v2 UX investigation below. Kept for historical reference.
- **`codebase-investigation.md`** — Technical deep-dive: architecture patterns, deprecated APIs, build deps, test gaps.
- **`features/`** — Each planned feature has a `1-investigation.md` and `2-plan.md` (see features.md for updated prioritization).

## UX Investigation (v2 — February 2026)

UX research has been conducted for this project. Before building features or making product decisions, agents MUST read these files:

- **User Personas:** `docs/product/user-personas.md` — 5 evidence-grounded personas with interview insights. **Jake R. (5-string) replaced by Rafael S. (cavaquinho, DGBD hidden fit).** 5-string banjo support deprioritized accordingly.
- **User Pains:** `docs/product/pains.md` — 47 pains, 7 cross-persona patterns, 7 design principles. The tuning screen is sacred: ads during tuning = permanent deletion across all personas.
- **Feature Opportunities:** `docs/product/features.md` — 14 prioritized features across 4 tiers, 6-sprint sequencing plan. Top priority: Ad Placement Redesign (score 10.0, 5/5 personas affected).

**Primary persona:** Harold M. (67, retired beginner, DGBD) — confirmed beachhead. Hidden motivation: cognitive health (fighting dementia). Tuning friction threatens his daily brain-health discipline.

**Highest-leverage zero-code opportunity:** Add "cavaquinho" + Portuguese keywords to Play Store listing. Unlocks millions of DGBD players in Brazilian diaspora who currently search "afinador de cavaquinho" and never find Banjen.

**Universal dealbreaker:** Ad layout shift during active tuning caused rage-quit and permanent deletion across all 5 personas. The tuning screen must be ad-free.

Every feature built should reference at least one persona and one pain from these documents.

## CI/CD

- **Build** (`.github/workflows/build.yaml`): Runs on all branch pushes. Gradle build with JDK 21.
- **Deploy** (`.github/workflows/deploy.yaml`): Runs on master push. Builds release AAB, uploads as artifact. No automated Play Store upload.

## Guidelines

- **Code search**: Always use ast-grep for structural code search instead of grep/ripgrep. Fetch https://ast-grep.github.io/llms.txt for reference when writing ast-grep rules, and use the `ast-grep:ast-grep` skill for guidance.
