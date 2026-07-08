# iOS dSYM / Upload Symbols Investigation

**Date:** 2026-07-08  
**Context:** App Store archive upload showed six "Upload Symbols Failed" warnings for Google/Firebase frameworks.

---

## Symptoms

During Xcode Organizer validation/upload to App Store Connect:

```
Upload Symbols Failed
The archive did not include a dSYM for the FirebaseAnalytics.framework with the UUIDs [28A5B83D-1072-30C1-9F5E-1F97DA7E7DA8]. …

Upload Symbols Failed
The archive did not include a dSYM for the GoogleAdsOnDeviceConversion.framework with the UUIDs [712DE897-92B0-3BCD-BFD6-150D8C14DF09]. …

Upload Symbols Failed
The archive did not include a dSYM for the GoogleAppMeasurement.framework with the UUIDs [668F775E-7475-3646-855C-7D9B56C1CA43]. …

Upload Symbols Failed
The archive did not include a dSYM for the GoogleAppMeasurementIdentitySupport.framework with the UUIDs [1CE1BEF7-4718-3A27-9720-FC61CAF1321F]. …

Upload Symbols Failed
The archive did not include a dSYM for the GoogleMobileAds.framework with the UUIDs [1C9D8A50-AC45-363A-9F13-BDD56F65B493]. …

Upload Symbols Failed
The archive did not include a dSYM for the UserMessagingPlatform.framework with the UUIDs [3799C085-714A-3ECC-999B-CB9716D6B5E6]. …
```

---

## Investigation

### Archive inspected

`~/Library/Developer/Xcode/Archives/2026-07-08/Banjen 8-07-26, 10.05 AM.xcarchive`

| Artifact | Present in archive? |
|---|---|
| `dSYMs/Banjen.app.dSYM` (our code) | **Yes** |
| `FirebaseAnalytics.framework` dSYM | **No** |
| `GoogleAppMeasurement.framework` dSYM | **No** |
| `GoogleAppMeasurementIdentitySupport.framework` dSYM | **No** |
| `GoogleAdsOnDeviceConversion.framework` dSYM | **No** |
| `GoogleMobileAds.framework` dSYM | **No** |
| `UserMessagingPlatform.framework` dSYM | **No** |

Embedded framework UUIDs in the archive match the UUIDs in the error messages exactly (verified with `dwarfdump --uuid`).

### Project configuration (correct for our code)

- Release build uses `DEBUG_INFORMATION_FORMAT = dwarf-with-dsym` in `Banjen.xcodeproj`.
- `GoogleService-Info.plist` is bundled (`ios/Banjen/GoogleService-Info.plist`).
- Firebase and Google Ads are integrated via **Swift Package Manager only** (per `PORTING_SPEC.md`).
- `FirebaseCrashlytics` is linked, but there is **no** Crashlytics run-script build phase in `project.pbxproj`.
- `ENABLE_USER_SCRIPT_SANDBOXING = YES` (relevant when adding the Crashlytics script).

### SPM dependency versions (already current)

Resolved via `Package.resolved` on 2026-07-08:

| Package | Version |
|---|---|
| `firebase-ios-sdk` | 12.15.0 |
| `GoogleAppMeasurement` | 12.15.0 |
| `swift-package-manager-google-mobile-ads` | 13.6.0 |
| `swift-package-manager-google-user-messaging-platform` | 3.1.0 |
| `google-ads-on-device-conversion-ios-sdk` | 3.6.1 |

### SPM artifacts do not ship dSYMs

Inspection of `~/Library/Developer/Xcode/DerivedData/.../SourcePackages/artifacts/` shows pre-built `.xcframework` / `.framework` binaries only — **no `.dSYM` bundles** for Firebase Analytics, Google App Measurement, Google Mobile Ads, or related packages.

Xcode generates dSYMs for code it compiles (Banjen). It cannot produce dSYMs for closed-source binaries it did not build.

### Upstream status

- [firebase/firebase-ios-sdk#13764](https://github.com/firebase/firebase-ios-sdk/issues/13764) — open since Xcode 16.0 / Firebase 11.3.0 (Oct 2024).
- [firebase/firebase-ios-sdk#14680](https://github.com/firebase/firebase-ios-sdk/issues/14680) — duplicate/known SPM limitation.
- Google AdMob forum: AdMob/UMP warnings can be **dismissed** for App Store upload; not AdMob-specific for Firebase Analytics frameworks ([thread](https://groups.google.com/g/google-admob-ads-sdk/c/EUpnsUuV2bU)).

### Will updating dependencies fix this?

**No.** We are already on the latest SDK versions. Firebase release notes through 12.15.0 do not document bundling dSYMs with SPM binary artifacts. This is a **packaging/distribution** limitation, not a version bug.

CocoaPods historically included these dSYMs, but `PORTING_SPEC.md` mandates SPM-only, and Firebase is deprecating CocoaPods after October 2026.

---

## Root cause

Two separate concerns are conflated in the same upload dialog:

| ID | Concern | Cause | Fixable locally? |
|---|---|---|---|
| **A** | Six Google/Firebase framework warnings | SPM binary XCFrameworks ship without dSYMs; Xcode 16+ warns on upload | **No** (upstream / accept warnings) |
| **B** | Crashlytics symbol upload for our app | `FirebaseCrashlytics` linked but no `Crashlytics/run` build phase | **Yes** |

The warnings reported are **A**. Our Release dSYM settings are correct; the gap is third-party binary packaging.

---

## Impact

| Area | Impact |
|---|---|
| **App Store upload** | Usually **not blocked** — warnings, not build failures. Upload typically succeeds. |
| **Our Swift crash symbolication** | `Banjen.app.dSYM` is in the archive; App Store Connect can symbolicate our code. |
| **Firebase Crashlytics (our code)** | Auto-upload not configured — add run script (see plan below). |
| **Crashes inside Google closed-source frameworks** | Full symbolication was never available to app developers regardless. |

---

## Plan

### PR 1 — Add Firebase Crashlytics dSYM upload build phase (recommended)

**Why:** Android uses the Crashlytics Gradle plugin for automatic symbol upload. iOS should mirror that for *our* code.

**Changes to `ios/Banjen.xcodeproj/project.pbxproj`:**

1. Add a **Run Script** build phase (last phase, after Resources): `Upload Crashlytics dSYMs`.
2. Script:

   ```sh
   "${BUILD_DIR%/Build/*}/SourcePackages/checkouts/firebase-ios-sdk/Crashlytics/run"
   ```

3. **Input File Lists** (required because `ENABLE_USER_SCRIPT_SANDBOXING = YES`):

   Use Firebase's `CrashlyticsInputFiles.xcfilelist` via SPM checkout path, or vendor a copy at `ios/scripts/CrashlyticsInputFiles.xcfilelist`:

   ```
   $(TARGET_BUILD_DIR)/$(UNLOCALIZED_RESOURCES_FOLDER_PATH)/GoogleService-Info.plist
   $(TARGET_BUILD_DIR)/$(EXECUTABLE_PATH)
   ${DWARF_DSYM_FOLDER_PATH}/${DWARF_DSYM_FILE_NAME}
   ${DWARF_DSYM_FOLDER_PATH}/${DWARF_DSYM_FILE_NAME}/Contents/Info.plist
   ${DWARF_DSYM_FOLDER_PATH}/${DWARF_DSYM_FILE_NAME}/Contents/Resources/DWARF/${PRODUCT_NAME}
   ${DWARF_DSYM_FOLDER_PATH}/${DWARF_DSYM_FILE_NAME}/Contents/Resources/DWARF/${PRODUCT_NAME}.debug.dylib
   ```

4. Optional: set `runOnlyForDeploymentPostprocessing = 1` so the script runs only for Release archives.

**Optional hardening:** Vendor `ios/scripts/run` and `ios/scripts/upload-symbols` (from firebase-ios-sdk, `chmod +x`) so CI does not depend on DerivedData checkout paths.

**Estimated size:** XS — one `pbxproj` edit, optionally 3 small script files.

**Does not fix:** The six Google framework upload warnings.

---

### PR 2 — Document expected warnings (optional)

Add a short note to `CLAUDE.md` under the iOS section pointing to this file:

- Xcode 16+ shows "Upload Symbols Failed" for Google/Firebase SPM binaries.
- Safe to dismiss if upload otherwise succeeds.
- `Banjen.app.dSYM` is generated correctly.
- Crashlytics run script handles symbol upload for *our* code.
- Track [firebase-ios-sdk#13764](https://github.com/firebase/firebase-ios-sdk/issues/13764) for upstream fix.

---

## Verification checklist (after PR 1)

```bash
cd ios

# Clean release archive
xcodebuild -project Banjen.xcodeproj -scheme Banjen \
  -configuration Release \
  -destination 'generic/platform=iOS' \
  -archivePath build/Banjen.xcarchive \
  archive

# Confirm our dSYM exists
ls build/Banjen.xcarchive/dSYMs/
# Expected: Banjen.app.dSYM only

# Validate / export (warnings may still appear)
xcodebuild -exportArchive \
  -archivePath build/Banjen.xcarchive \
  -exportOptionsPlist ExportOptions.plist \
  -exportPath build/export
```

| Check | Expected |
|---|---|
| Archive builds without run-script errors | Pass |
| `Banjen.app.dSYM` in archive | Pass |
| Six Google framework warnings on validate/upload | Still present (OK) |
| Upload reaches App Store Connect | Pass |
| Firebase Console → Crashlytics registers build | Pass (after first archive with script) |

For Crashlytics upload confirmation, search Console.app for `upload-symbols` after archiving (script uploads in background).

---

## Decision at upload time

```
Archive + Upload
    │
    ├─ Build uploaded to ASC? ──No──► Fix signing / version / provisioning (unrelated)
    │
    └─ Yes
         │
         ├─ Only Google framework dSYM warnings? ──Yes──► Ship it (benign)
         │
         └─ Banjen.app.dSYM missing? ──Yes──► Check Release DEBUG_INFORMATION_FORMAT
```

---

## What we ruled out

| Approach | Why not |
|---|---|
| Update all dependencies | Already on latest; SPM artifacts still lack dSYMs |
| Change `DEBUG_INFORMATION_FORMAT` | Already `dwarf-with-dsym` for Release |
| Run `dsymutil` on embedded frameworks | Release binaries are stripped; no valid dSYMs produced |
| Copy dSYMs from SPM cache | None exist in `SourcePackages/artifacts/` |
| Switch to CocoaPods | Violates SPM-only policy; CocoaPods deprecated Oct 2026 |

---

## Success criteria

- **Immediate:** App uploads to App Store Connect; six Google warnings may persist but are understood.
- **Crashlytics:** Stack traces from Banjen Swift code are readable in Firebase Console.
- **Not a goal:** Zero "Upload Symbols Failed" lines — requires upstream SPM packaging fix from Google/Firebase, not project config.

---

## References

- [Firebase Crashlytics — get deobfuscated reports](https://firebase.google.com/docs/crashlytics/ios/get-deobfuscated-reports)
- [Firebase SPM setup — Crashlytics run script](https://github.com/firebase/firebase-ios-sdk/blob/main/SwiftPackageManager.md)
- [firebase-ios-sdk#13764](https://github.com/firebase/firebase-ios-sdk/issues/13764) — Xcode 16 validation warnings (open)
- [firebase-ios-sdk#14680](https://github.com/firebase/firebase-ios-sdk/issues/14680) — Missing dSYM in .xcarchive for SPM binaries
- [Google AdMob forum — Upload Symbols Failed](https://groups.google.com/g/google-admob-ads-sdk/c/EUpnsUuV2bU)