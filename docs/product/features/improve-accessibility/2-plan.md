# Plan: Improve Accessibility for Older Users

## MCP Validation
| Lib | Ver | Source | Status |
|-----|-----|--------|--------|
| Jetpack Compose Material3 | In project | build.gradle | Already in use |
| Compose semantics API | In project | Compose UI | Already in use |
| Android string resources | Platform | Android SDK | Already in use |

No new dependencies. All APIs are already available in the project.

## Architecture

```
Before:
  Button
  └── Row
      ├── [VolumeIcon] (conditional)
      └── Text("1 - D", 20sp)

After:
  Button (+ semantics contentDescription)
  └── Row
      ├── [VolumeIcon] (conditional)
      └── Column
          ├── Text("1 - D", 24sp)        ← larger main text
          └── Text("String 1 (thickest)", 14sp) ← subtitle, hidden when selected
```

## M1: Add Subtitle Strings and Content Descriptions to Resources

```
┌─────────────────────────────────────────┐
│ ANALOGY: Adding aisle signs to a store  │
│ [🏗️ Bare shelves]═══[🚀 Labeled aisles]│
│ VALUE: String data ready for UI         │
│ PROGRESS: [████░░░░░░] 40%             │
└─────────────────────────────────────────┘
```

### Changes
1. **`values/strings.xml`**: Add 4 subtitle strings (`ear_button_N_subtitle`) and 4 content description strings (`ear_button_N_description`)
   - Subtitles: "String 1 (thinnest)" through "String 4 (thickest)"
   - Content descriptions: "String 4, note D, thickest. Tap to play reference tone."
2. **`values-es/strings.xml`**: Spanish translations
3. **`values-pt/strings.xml`**: Portuguese translations
4. **`values-it/strings.xml`**: Italian translations

### String design
| Key | EN | ES | PT | IT |
|-----|----|----|----|----|
| `ear_button_1_subtitle` | String 1 (thinnest) | Cuerda 1 (la mas fina) | Corda 1 (a mais fina) | Corda 1 (la piu sottile) |
| `ear_button_2_subtitle` | String 2 | Cuerda 2 | Corda 2 | Corda 2 |
| `ear_button_3_subtitle` | String 3 | Cuerda 3 | Corda 3 | Corda 3 |
| `ear_button_4_subtitle` | String 4 (thickest) | Cuerda 4 (la mas gruesa) | Corda 4 (a mais grossa) | Corda 4 (la piu spessa) |
| `ear_button_N_description` | String N, note X. Tap to play. | Cuerda N, nota X. Toca para reproducir. | Corda N, nota X. Toque para reproduzir. | Corda N, nota X. Tocca per riprodurre. |

### Verification
- `./gradlew assembleDebug` compiles successfully
- All 4 strings.xml files have matching keys

## M2: Update Button Composable with Subtitles, Larger Text, and Semantics

```
┌─────────────────────────────────────────┐
│ ANALOGY: Adding name tags to guests     │
│ [🏗️ Note names only]═══[🚀 Full IDs]   │
│ VALUE: Harold can understand buttons    │
│ PROGRESS: [██████████] 100%             │
└─────────────────────────────────────────┘
```

### Changes to `EarActivity.kt`
1. **Data structure**: Add parallel `buttonsSubtitle` and `buttonsDescription` lists mapping to new string resource IDs
2. **Button composable signature**: Add `subtitle: Int` and `description: Int` params
3. **Semantics**: Add `Modifier.semantics { contentDescription = stringResource(description) }` to TextButton
4. **Main text**: Increase font from 20sp to 24sp; switch from `getString(text)` to `stringResource(text)`
5. **Subtitle text**: Add second `Text` composable below main text in a `Column`; 14sp, slightly dimmer color; hidden via `AnimatedVisibility` when `isSelected`
6. **Touch target**: Add `Modifier.defaultMinSize(minHeight = 48.dp)` (belt-and-suspenders with existing weight)

### Key implementation detail
- The `Row` content inside `TextButton` gets wrapped: `Row { [icon] Column { Text(main) Text(subtitle) } }`
- Subtitle uses `AnimatedVisibility(!isSelected)` for smooth show/hide
- `clearAndSetSemantics` on TextButton to provide single merged content description for TalkBack

### Verification
- `./gradlew assembleDebug` compiles
- Visual: 4 buttons each show note name + subtitle
- TalkBack: each button announces full content description
- Existing tests pass (substring match "N - " unaffected)
- `./gradlew test` passes (unit tests)

## Risks & Mitigations
| Risk | Mitigation |
|------|------------|
| Subtitle visible during 3x scale animation looks cluttered | Hide subtitle when isSelected via AnimatedVisibility |
| Test robot broken | Robot uses substring match - verified safe |
| Oversized text at max system font | Use maxLines=1 on both text elements |

## Scope decisions
- **IN**: Subtitles, larger text, semantics/contentDescription, localization
- **OUT**: Onboarding overlay (subtitles serve as inline onboarding), SharedPreferences
- **RATIONALE**: The subtitle text ("String 1 (thinnest)") IS the onboarding -- Harold immediately understands without a dismissable overlay. Keeping scope to S.
