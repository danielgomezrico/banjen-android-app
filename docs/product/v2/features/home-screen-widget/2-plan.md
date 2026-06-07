# Plan: Home Screen Widget for Instant One-Tap Tuning

## Milestones

### M1: Widget Infrastructure (TunerWidget + metadata + dependencies)
**Goal**: Create a Glance-based widget that displays 4 string buttons and launches EarActivity when tapped.

**Files**:
- NEW: `app/src/main/java/com/makingiants/android/banjotuner/TunerWidget.kt`
- NEW: `app/src/main/res/xml/tuner_widget_info.xml`
- NEW: `app/src/main/res/layout/widget_loading.xml`
- MODIFY: `app/build.gradle` (add Glance dependencies)
- MODIFY: `app/src/main/AndroidManifest.xml` (register receiver)
- MODIFY: `app/src/main/res/values/strings.xml` (+ es, pt, it) - widget name/description

**Tests** (unit tests for the widget helper logic):
- `TunerWidgetTest.kt`:
  - Verify STRING_INDEX_KEY constant name
  - Verify string labels for all 4 buttons
  - Verify button count matches expected strings

### M2: Auto-Play on Launch (EarActivity intent handling)
**Goal**: When EarActivity is launched with a `string_index` intent extra, automatically start playing that string's tone.

**Files**:
- MODIFY: `app/src/main/java/com/makingiants/android/banjotuner/EarActivity.kt`

**Tests**:
- `EarActivityIntentTest.kt`:
  - Verify `EXTRA_STRING_INDEX` constant value
  - Verify `parseStringIndex` returns valid index from intent
  - Verify `parseStringIndex` returns -1 for missing extra
  - Verify `parseStringIndex` returns -1 for out-of-range values

## Implementation Details

### M1 Details

**TunerWidget.kt**:
```kotlin
class TunerWidget : GlanceAppWidget() {
    companion object {
        val STRING_INDEX_KEY = ActionParameters.Key<Int>("string_index")
        val STRING_LABELS = listOf("4 - D", "3 - G", "2 - B", "1 - D")
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent { Content() }
    }

    @Composable
    private fun Content() {
        // Row of 4 buttons, each with actionStartActivity<EarActivity>
        // passing STRING_INDEX_KEY to the index
    }
}

class TunerWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = TunerWidget()
}
```

**tuner_widget_info.xml**:
- minWidth: 250dp (fits 4 buttons in a row)
- minHeight: 40dp (single row)
- description: string resource for widget picker
- previewLayout or previewImage for widget picker

**widget_loading.xml**:
- Simple TextView with "Banjen" text, centered

**build.gradle additions**:
```groovy
implementation "androidx.glance:glance:1.2.0-beta01"
implementation "androidx.glance:glance-appwidget:1.2.0-beta01"
```

### M2 Details

**EarActivity.kt changes**:
```kotlin
companion object {
    const val EXTRA_STRING_INDEX = "string_index"
}

override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    MobileAds.initialize(this)

    val autoPlayIndex = intent?.getIntExtra(EXTRA_STRING_INDEX, -1) ?: -1
    setContent { Contents(autoPlayIndex) }
}
```

Pass `autoPlayIndex` through to Contents/MainLayout so that if it's valid (0-3), the selected button is pre-set and playback starts automatically.

## Verification Criteria
- `make test` passes (unit tests for widget constants and intent parsing)
- `make build` succeeds (Glance dependency resolves, manifest is valid)
- Widget metadata XML is well-formed
- No ktlint errors (beyond pre-existing Compose PascalCase warnings)
