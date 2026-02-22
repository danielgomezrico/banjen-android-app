# Investigation: Home Screen Widget for Instant One-Tap Tuning

## Current State

### Existing Architecture
- **Single Activity**: `EarActivity` is the only activity, set as `MAIN`/`LAUNCHER` in AndroidManifest.xml
- **SoundPlayer**: Plays looped MP3 assets from `assets/b_sounds/` via `MediaPlayer`. Has `playWithLoop(index: Int)` and `stop()` methods
- **4 String Buttons**: Hardcoded list of string resource IDs (`R.string.ear_button_4_text` through `R.string.ear_button_1_text`) mapped to sound indices 0-3
- **Sound files**: `1.mp3` (D3), `2.mp3` (G3), `3.mp3` (B3), `4.mp3` (D4) in `assets/b_sounds/`

### Build Configuration
- Min SDK 23, Target SDK 36
- Compose BOM 2026.02.00
- Kotlin 2.3.10, Java 17
- Uses Groovy DSL for build.gradle

### Key Observation
The `SoundPlayer.playWithLoop(index)` uses a hardcoded `sounds` array (`1.mp3` through `4.mp3`). The widget needs to launch the app and auto-play a specific string index.

## Glance API Research

### Dependencies Required
```groovy
implementation "androidx.glance:glance:1.2.0-beta01"
implementation "androidx.glance:glance-appwidget:1.2.0-beta01"
```

### Core Components
1. **`GlanceAppWidget`** - Defines widget content via a `Content()` composable (uses Glance composables, NOT regular Compose)
2. **`GlanceAppWidgetReceiver`** - BroadcastReceiver subclass that provides the widget instance
3. **`actionStartActivity`** - Launches an activity from a widget button click, supports `ActionParameters` for passing data

### Widget Button -> Activity Launch Flow
```kotlin
val STRING_INDEX_KEY = ActionParameters.Key<Int>("string_index")

Button(
    text = "1 - D",
    onClick = actionStartActivity<EarActivity>(
        actionParametersOf(STRING_INDEX_KEY to 0)
    )
)
```

When using `actionStartActivity` with `ActionParameters`, the parameters are delivered as Intent extras. EarActivity reads them in `onCreate`.

### Widget Metadata XML
Required `res/xml/tuner_widget_info.xml`:
```xml
<appwidget-provider
    android:minWidth="180dp"
    android:minHeight="40dp"
    android:updatePeriodMillis="0"
    android:initialLayout="@layout/widget_loading"
    android:resizeMode="horizontal|vertical"
    android:widgetCategory="home_screen" />
```

Note: Glance widgets still need an `initialLayout` for the brief moment before Glance renders the Compose-based content. A simple loading layout is sufficient.

### Manifest Registration
```xml
<receiver android:name=".TunerWidgetReceiver" android:exported="true">
    <intent-filter>
        <action android:name="android.appwidget.action.APPWIDGET_UPDATE" />
    </intent-filter>
    <meta-data
        android:name="android.appwidget.provider"
        android:resource="@xml/tuner_widget_info" />
</receiver>
```

## Constraints & Risks

1. **Min SDK 23 compatibility**: Glance 1.2.0-beta01 requires minSdk 21, so we're fine with our minSdk 23
2. **No regular Compose in widgets**: Must use Glance-specific composables (`androidx.glance.*`), not `androidx.compose.*`
3. **Widget initial layout**: Need a simple XML layout for the loading state before Glance renders
4. **Intent extras delivery**: `actionStartActivity` with `ActionParameters` delivers data as Intent extras - need to verify the key format
5. **SoundPlayer lifecycle**: EarActivity creates SoundPlayer in `onCreate`. If launched from widget with auto-play intent, need to handle the case where player starts immediately

## Files to Create/Modify

### New Files
1. `app/src/main/java/com/makingiants/android/banjotuner/TunerWidget.kt` - GlanceAppWidget + GlanceAppWidgetReceiver
2. `app/src/main/res/xml/tuner_widget_info.xml` - Widget metadata
3. `app/src/main/res/layout/widget_loading.xml` - Initial loading layout
4. `app/src/test/java/com/makingiants/android/banjotuner/TunerWidgetTest.kt` - Unit tests

### Modified Files
1. `app/build.gradle` - Add Glance dependencies
2. `app/src/main/AndroidManifest.xml` - Register widget receiver
3. `app/src/main/java/com/makingiants/android/banjotuner/EarActivity.kt` - Handle intent extras for auto-play
4. `app/src/main/res/values/strings.xml` (+ es, pt, it) - Widget name/description strings
