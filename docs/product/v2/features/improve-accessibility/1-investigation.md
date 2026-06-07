# Investigation: Improve Accessibility for Older Users

## Summary
Investigated EarActivity.kt (the single Compose UI file) and all string resource files to assess current accessibility state. Key findings: buttons use `TextButton` with `weight(1f).fillMaxWidth()` which gives generous touch targets already, but no explicit minimum size enforcement; no `contentDescription` or `semantics` blocks exist; font is fixed at 20sp; no string-number/position labels exist beyond "1 - D" format; no onboarding exists.

## Files
| File | Purpose | Relevance |
|------|---------|-----------|
| `app/src/main/java/.../EarActivity.kt` | Single activity with all Compose UI | Primary change target: button composable, labels, semantics |
| `app/src/main/java/.../SoundPlayer.kt` | MediaPlayer wrapper | No changes needed |
| `app/src/main/res/values/strings.xml` | English strings | Add subtitle strings, content descriptions |
| `app/src/main/res/values-es/strings.xml` | Spanish strings | Localize new strings |
| `app/src/main/res/values-pt/strings.xml` | Portuguese strings | Localize new strings |
| `app/src/main/res/values-it/strings.xml` | Italian strings | Localize new strings |
| `app/src/main/res/values/colors.xml` | Color definitions | Reference only, no changes |
| `app/src/androidTest/.../EarActivityTest.kt` | Instrumented UI tests | May need update if button text changes |
| `app/src/androidTest/.../EarRobot.kt` | Test robot pattern | Uses `onNodeWithText("$buttonIndex - ", substring = true)` - robust to subtitle additions |

## Patterns
- **Button layout**: `ColumnScope.Button()` composable at EarActivity.kt:164-270. Uses `TextButton` with `Modifier.weight(1f).fillMaxWidth()`. The weight(1f) in a Column with SpaceEvenly arrangement means buttons already fill available vertical space equally.
- **String references**: `buttonsText` list at EarActivity.kt:70-76 maps indices 0-3 to string resource IDs (`ear_button_4_text` through `ear_button_1_text` -- note reverse order: index 0 = button 4, index 3 = button 1).
- **Button text format**: Currently "N - Note" (e.g., "1 - D", "4 - D"). In Spanish/Portuguese/Italian, note names differ (e.g., "Re", "Si", "Sol").
- **Text rendering**: Uses `getString(text)` (Activity method) instead of `stringResource()` at line 260. Font size is hardcoded at `20.sp`.
- **Test robot**: EarRobot.kt:16 uses `onNodeWithText("$buttonIndex - ", substring = true)` which matches on prefix, so adding subtitle text below the main label won't break tests.
- **Animation**: Selected buttons scale to 3x and shake. The subtitle text should be hidden or omitted when button is selected (scaled up) to avoid visual clutter.

## Constraints
- [x] Material3 accessibility: touch targets must be >= 48dp minimum (M3 guideline)
- [x] Font sizes must use `sp` units (already true at 20.sp) and respect system scaling
- [x] Compose `semantics {}` blocks needed for TalkBack support
- [x] Localization: all user-visible strings must be in all 4 locales (en, es, pt, it)
- [x] No architecture changes - purely Compose UI modifications

## Deps
- `EarActivity.Button()` composable → `buttonsText` list (string resource IDs)
- `EarRobot.click()` → `onNodeWithText("$buttonIndex - ", substring = true)` (substring match)
- Button index in `buttonsText` → `SoundPlayer.playWithLoop(index)` (0-3 maps to sound files 1-4.mp3)

## Tests
- Style: Instrumented Compose UI tests with Robot pattern
- Location: `app/src/androidTest/java/com/makingiants/android/banjotuner/`
- No unit tests exist (only instrumented tests requiring device/emulator)
- Robot uses substring text matching, so adding subtitle text won't break existing tests
- New tests should verify: content descriptions present, subtitle text visible

## Risks
| Risk | Sev | Mitigation |
|------|-----|------------|
| Subtitle text clutters scaled-up (3x) selected button | Medium | Hide subtitle when button is selected (isSelected) |
| Onboarding SharedPreferences adds complexity | Low | Keep onboarding minimal - simple dismissable banner, not a full overlay |
| Changing string format could break tests | Low | Tests use substring match "N - ", so main label stays compatible |
| Large system font sizes could cause overflow | Medium | Test with max accessibility font size; use `maxLines` if needed |

## Recommendations
1. Add subtitle strings to all 4 locales: "String N (thickest/thinnest)" pattern with position context
2. Add a `Column` inside each button with main note text + smaller subtitle text; hide subtitle when selected
3. Add `Modifier.semantics { contentDescription = "..." }` to each button with full description (e.g., "String 1, note D, thickest string. Tap to play reference tone.")
4. Increase main text from 20sp to 24sp for better readability
5. Add `Modifier.defaultMinSize(minHeight = 48.dp)` to enforce Material3 touch target minimum (though current weight-based layout likely exceeds this)
6. Skip onboarding overlay for this S-sized task - the subtitle text itself serves as inline onboarding
7. Use `stringResource()` instead of `getString()` for the button text (Compose idiomatic)

## Verification
- [x] Files verified - all paths exist in worktree
- [x] Patterns confirmed - button composable structure, string format, test robot matching
- [x] Constraints traced - Material3 48dp, sp units, localization requirement
- [x] Deps mapped - button index flow, test robot text matching
- [x] Tests validated - substring matching is robust to subtitle additions
