# Banjen iOS Port — Shared Spec & Interface Contracts

Goal: reimplement **every** feature of the Android app (`android/`) on iOS. UI must look
**exactly the same**; behavior must match. SwiftUI + AVFoundation. **SPM only, no CocoaPods.**

## Layout
- `ios/BanjenCore/` — local Swift package. Pure, platform-agnostic logic + unit tests (`swift test`). Already wired into the app target.
- `ios/Banjen/` — the app. **File-system-synchronized group**: any `.swift` added here is auto-compiled. SwiftUI views, audio engine, view model.
- Bundle id `com.banjen.ios.Banjen`, deployment iOS 26.5, Xcode 26.5, Swift 6.

## Build / test commands
```bash
# Pure logic (fast, no simulator):
cd ios/BanjenCore && swift test
# App build:
cd ios && xcodebuild -project Banjen.xcodeproj -scheme Banjen \
  -destination 'platform=iOS Simulator,name=iPhone 17' -derivedDataPath build build CODE_SIGNING_ALLOWED=NO
```

## File ownership (avoid collisions — each file has ONE owner)
| File | Owner task |
|---|---|
| `BanjenCore/Sources/BanjenCore/TuningModel.swift` | logic |
| `BanjenCore/Sources/BanjenCore/AppConstants.swift` | logic |
| `BanjenCore/Sources/BanjenCore/PitchDetector.swift` | logic |
| `BanjenCore/Sources/BanjenCore/ToneMath.swift` | logic |
| `BanjenCore/Sources/BanjenCore/TuningAnimationState.swift` | logic |
| `BanjenCore/Tests/BanjenCoreTests/*` | logic |
| `Banjen/Audio/ToneGenerator.swift` | tone |
| `Banjen/Audio/PitchCaptureEngine.swift` | earview (mic) |
| `Banjen/Views/BanjoStringCanvas.swift` | canvas |
| `Banjen/Views/TuningAnimationView.swift` | canvas |
| `Banjen/Views/EarView.swift` | earview |
| `Banjen/Views/SettingsSheet.swift` | earview |
| `Banjen/Views/AdBannerView.swift` | earview |
| `Banjen/EarViewModel.swift` | earview |
| `Banjen/Theme/AppColors.swift` | earview (create early) |
| `Banjen/ContentView.swift` | earview (replace stub → renders EarView) |
| `Banjen/Localizable.xcstrings` | earview |

`BanjenApp.swift` already exists (renders `ContentView`). Keep `@main`.

---

## EXACT VISUAL CONSTANTS (must match Android pixel-for-pixel)

### Colors (sRGB hex; Compose `Color(0xAARRGGBB)` → SwiftUI `Color(red:green:blue:)` or hex init)
Background vertical gradient stops (BanjoStringCanvas):
`0.00→#1A1210, 0.35→#231A15, 0.65→#1E1512, 1.00→#141010`
Vignette: radial Transparent → Black α0.40, center, radius = max(w,h)*0.85.
Nut/bridge: `#3D322A`, highlight `#5C4A3E` (α0.60 top 1px line). Fret: `#2E2420` α0.35.

String palette (5 entries; idle / active / glow(=active) / label):
- g4 drone: idle `#5C7B58`, active `#78C870`, label `#90B082`
- D3: idle `#8C7161`, active `#D4956A`, label `#B89A86`
- G3: idle `#6B8490`, active `#5AAFCB`, label `#8EADB8`
- B3: idle `#8C8062`, active `#CBA55A`, label `#B8A882`
- D4: idle `#907466`, active `#E07850`, label `#C09A8A`

`paletteIdx = (i + (5 - numStrings)) % 5`. (4-string maps i0..3→1..4; 5-string i0..4→0..4.)

UI chrome:
- Pills / dropdowns bg `#2A1F1A`, border `#5C4A3E` 1px, fully rounded (`Capsule`).
- BANJEN wordmark: 18sp, weight medium, color `#B89A86`, letterSpacing 4sp.
- Dropdown label text color `#B89A86`, weight medium.
- Settings divider: 1px `#2E2420`.
- Accent color (`banjen_accent`) `#6D94A1` — pitch +/- buttons, share icon.
- Pitch label "A=NNN" bold accent.

### Animation constants (BanjoStringCanvas)
- Breathing: period 2400ms, opacity 0.70↔1.00, EaseInOutSine, reverse.
- Shimmer phase: 0→2π over 3200ms linear restart.
- Dimmed opacity 0.35. blur width ×2.0, haze width ×3.5 of core.
- Nut/bridge height 12dp, safe padding 16dp.
- Fret positions (fraction of string length): `[0.18,0.33,0.45,0.55,0.65]`.
- Reveal: stagger `i*150ms`, duration 380ms EaseOutCubic, nut→bridge. Tap interrupts → snap all to full.
- Per-string physics from frequency: see `computeStringPhysics` in BanjoStringCanvas.kt — port exactly:
  - `fn = (freq-min)/(max-min)` (0.5 if equal).
  - idleThick lerp(4→2), activeThick lerp(5.5→2.8), vibAmp lerp(8→4) dp, vibFreqHz lerp(3→6),
    vibWavePeaks lerp(2.5→4), springStiffness lerp(300→500), springDamping lerp(0.50→0.65),
    releaseDuration lerp(400→220)ms, initialSharpness lerp(0.7→1.2), sharpnessTrans lerp(250→120)ms,
    breathingAmplitude lerp(0.09→0.04), breathingPeriodS = `1.5 + 1.8*((i*0.6180339887)%1)`, isWound = freq<220.
  - Path: 80 segments; envelope = sin(π·yNorm); gaussian bias exp(-((y-touchY)²)/(2·0.35²)); attack lerp; sharpness shaping `sign(s)*|s|^(1/sharpness)` (raw when sharpness≥2.9); wound micro-texture `sin(47·y·2π)*0.03*amp` when freq<220; idle shimmer `shimmerAmp·env·sin(2π·y+phase)` (shimmerAmp 0.3dp when idle).
  - On select: vibration spring→1; sharpness snap(initial+zoneOffset)→3 over sharpnessTrans EaseOutCubic; attack snap0→1 300ms; color→1 200ms. Tap zone offsets: nut(y<0.3)=+0.05, bridge(y>0.7)=+0.10, center=0.
  - On other-selected: vib→0 over releaseDuration, color→0 350ms, opacity→0.35 300ms.
  - On none: vib→0, color→0, opacity→1.
- Labels: primary = note name (e.g. "D3"), secondary = ordinal "Nth" where N = `numStrings - i`. Primary 28→36sp (scales with colorFactor) medium, letterSpacing 1.5; secondary 19→26sp normal α·0.8, letterSpacing 0.5. labelY = bridgeY − 76dp.
- A11y: one button per string band, label "`{N}{st/nd/rd/th} string, note {name}. Double tap to play.`", action "Play/Stop {name}".

### TuningAnimationView (concentric rings) — `TuningAnimation.kt`
3 rings + center dot, idle breathe scale 0.97↔1.03 over 2000ms/2 FastOutSlowIn reverse.
Ring fractions of (minDim/2): outer 0.80 (α0.2, stroke 2dp) + bg glow α0.04, middle 0.60 (α0.25, 2.5dp), inner 0.40 (α0.3, 3dp), center dot r=5dp α0.4. Ring color = stringAccentColors[selected] else neutral `#F5E6D3`, cross-fade 300ms. Accent colors: D3 `#A67B5B`, G3 `#6D94A1`, B3 `#C4915A`, D4 `#D4A84B`. State derivation: `deriveTuningAnimationState(selectedOption, pitchCheckMode, pitchResult)` lives in BanjenCore.
NOTE: in current Android UI this view is computed but the **Canvas fallback** (rings) is what renders. The main tuning surface is `BanjoStringCanvas`. Render rings where Android does (it is currently not shown on the primary surface — keep parity: BanjoStringCanvas fills the tuning surface).

### Icons (AppIcons.kt) → SF Symbols
- Stop → `stop.fill`; Remove(−) → `minus`; Add(+) → `plus`; Mic → `mic.fill`; Headphones → `headphones`; VolumeOff → `speaker.slash.fill`; Settings → `gearshape.fill`; Share → `square.and.arrow.up`; ArrowDropDown → `chevron.down`. Tint to match (chrome `#B89A86`, accent `#6D94A1`).

### Strings (localize en + es + pt + it). Keys/values:
| key | en | es | pt | it |
|---|---|---|---|---|
| volume_low_message | Volume is low. Turn it up to hear the tuning tones. | (read android values-es) | (values-pt) | Il volume è basso. Alzalo per sentire i toni di accordatura. |
| reference_pitch_label | Reference pitch | | | Tono di riferimento |
| session_mode_label | Session mode | | | Modalità sessione |
| session_stop | Stop | Detener | | Ferma |
| widget_description | Quick-access banjo tuning buttons | | | Pulsanti di accordatura banjo ad accesso rapido |
| tune_up | Tune Up ▲ | | | Alza ▲ |
| tune_down | Tune Down ▼ | Baja ▼ | | Abbassa ▼ |
| in_tune | In Tune! | | | Accordato! |
| no_signal | Play your string… | | | Suona la corda… |
| share_tuning | Share tuning | | | Condividi accordatura |
| settings_label | Settings | Ajustes | | Impostazioni |
> Read full es/pt from `android/app/src/main/res/values-es/strings.xml` and `values-pt/strings.xml`.

---

## BEHAVIOR SPEC (EarActivity.kt)

State (persist in `UserDefaults`, suite default):
- keys: `reference_pitch` (Int, default 440, range 432–446), `instrument_index` (Int, default 0), `tuning_index` (Int, default 0). Match Android key strings exactly.
- runtime: `selectedStringIndex` (-1 = none), `sessionModeActive` (Bool), `pitchCheckMode` (Bool), `pitchResult`, `isVolumeLow`.

Main screen (top→bottom):
1. `CanvasOverlay` row over the canvas top (status-bar safe area inset): left = Headphones pill (session-mode toggle) — hidden/Spacer when session active; center = "BANJEN" wordmark; right = Settings pill (gear). Row alpha animates to 0.35 when a string is active (300ms).
2. `BanjoStringCanvas` fills the tuning surface (tap a string → play looping reference tone; tap again → stop). Tone freq = `note.frequency * referencePitch/440`.
3. Settings opens a **bottom sheet** (`.sheet`/`.presentationDetents`) with: SelectorRow (instrument dropdown, tuning dropdown, share button) + divider + PitchControl (− A=NNN +).
4. Ad banner (`AdBannerView`) — placeholder matching 320×50 banner slot; real AdMob via SPM optional (see below). **Must NOT cause layout shift on tuning surface** (place off the canvas, reserve fixed height).
5. Volume-low alert: when playing and device volume is low, show snackbar/banner with `volume_low_message`. Use `AVAudioSession.outputVolume` to detect low (< ~0.1). (`isVolumeLow()` Android checks STREAM_MUSIC ≤ 10%.)

Session mode (`SECONDS_PER_STRING = 5`): cycles strings 0→1→2→3 at session volume, auto-advancing every 5s; `autoAdvanceNextIndex(current)` = current+1 if <3 else nil (stops after string index 3). Headphones pill toggles it; while active the left pill is replaced by a Stop affordance per Android (`exitSessionMode`). Tear down audio on background/exit.

Instrument/tuning change: stop tone, reset selection to -1, clear volume-low, persist indices (instrument change also resets tuning_index→0).

Pitch control: − decrements (min 432), + increments (max 446); clamp; buttons disabled at bounds (`canDecreasePitch/canIncreasePitch`). On change, persist + if a string is currently selected, replay it at new effective Hz.

Share tuning: share sheet (`UIActivityViewController` / `ShareLink`) with `encodeTuning(currentTuning)` text.

Deep link / autoplay: Android reads intent extra `string_index` (from widget) and autoplays that string. iOS equiv: handle a URL/userActivity `banjen://play?string=N` (and the Widget intent) → set selectedString N + autoplay.

Mic pitch-check (optional surface, RECORD_AUDIO): `PitchCaptureEngine` taps mic at 44100 mono float, feeds frames to `PitchDetector`, publishes `PitchResult`. Needs `NSMicrophoneUsageDescription` in Info.plist. Drives `TuningAnimationState`. Mirror `AudioCaptureEffect`.

Lifecycle: stop tone + audio capture on scenePhase `.background`/`.inactive`.

---

## SWIFT INTERFACE CONTRACTS (code against these — keep signatures stable)

```swift
// BanjenCore/TuningModel.swift
public struct Note: Equatable, Sendable { public let name: String; public let frequency: Float
  public init(_ name: String, _ frequency: Float) }
public struct Tuning: Equatable, Sendable { public let name: String; public let notes: [Note] }
public struct Instrument: Equatable, Sendable { public let name: String; public let tunings: [Tuning] }
public func noteFrequency(_ midi: Int) -> Float           // 440 * 2^((midi-69)/12)
public func encodeTuning(_ t: Tuning) -> String           // "name|n1:f1,n2:f2,..."
public func decodeTuning(_ s: String) -> Tuning?
public let fourStringBanjo: Instrument                     // FOUR_STRING_BANJO catalog
public let fiveStringBanjo: Instrument                     // FIVE_STRING_BANJO catalog
public let allInstruments: [Instrument]                    // [four, five]

// BanjenCore/AppConstants.swift
public enum AppConstants {
  public static let defaultPitch = 440, minPitch = 432, maxPitch = 446
  public static let prefsName = "banjen_prefs"
  public static let keyReferencePitch = "reference_pitch"
  public static let keyInstrumentIndex = "instrument_index"
  public static let keyTuningIndex = "tuning_index"
  public static let secondsPerString = 5
}
public func calculatePitchRatio(_ ref: Int) -> Float       // ref/440
public func clampPitch(_ p: Int) -> Int                    // 432...446
public func canDecreasePitch(_ p: Int) -> Bool
public func canIncreasePitch(_ p: Int) -> Bool
public func clampVolume(_ v: Float) -> Float               // 0...1
public func autoAdvanceNextIndex(_ current: Int) -> Int?   // current<3 ? +1 : nil

// BanjenCore/PitchDetector.swift
public enum TuningStatus: Sendable { case inTune, close, sharp, flat, noSignal }
public struct PitchResult: Equatable, Sendable {
  public let detectedHz: Double; public let targetHz: Double
  public let centDeviation: Double; public let status: TuningStatus }
public struct PitchDetector: Sendable {
  public init(sampleRate: Int = 44100)
  public func detectPitch(_ samples: [Float]) -> Double            // YIN, -1 if none, threshold 0.15
  public func centsFromTarget(_ detected: Double, _ target: Double) -> Double
  public func classifyTuning(_ cents: Double) -> TuningStatus      // ≤10 inTune, ≤25 close, else sharp/flat
}

// BanjenCore/ToneMath.swift
public let toneSampleRate = 44100
public let amplitudeScale: Float = 0.7
public func generateSineWaveSamples(frequency: Float, sampleRate: Int, numSamples: Int) -> [Int16]
public func calculateLoopSampleCount(frequency: Float, sampleRate: Int) -> Int   // minimize 1-cos at boundary

// BanjenCore/TuningAnimationState.swift
public enum TuningAnimationState: Sendable { case idle, stringSelected, noSignal, flat, sharp, close, inTune }
public func deriveTuningAnimationState(selectedOption: Int, pitchCheckMode: Bool, pitchResult: PitchResult?) -> TuningAnimationState
public func beatFrequencyHz(_ centDeviation: Float) -> Float       // |c|/5 clamped 0.5...6
public func ringAsymmetryOffset(_ centDeviation: Float) -> Float   // c/50 clamped ±0.15

// Banjen/Audio/ToneGenerator.swift  (app target, @MainActor)
@MainActor final class ToneGenerator {
  var isPlaying: Bool { get }
  func play(_ frequency: Float)      // looping sine, 200ms fade-in, replaces current
  func stop()                        // 200ms fade-out
  func setVolume(_ v: Float)         // for session mode level
  func release()
}

// Banjen/EarViewModel.swift
@Observable @MainActor final class EarViewModel {
  var selectedStringIndex: Int       // -1 none
  var instrumentIndex: Int
  var tuningIndex: Int
  var referencePitch: Int
  var sessionModeActive: Bool
  var isVolumeLow: Bool
  var pitchCheckMode: Bool
  var pitchResult: PitchResult?
  var currentInstrument: Instrument { get }
  var currentTuning: Tuning { get }
  func selectString(_ i: Int)        // toggle play/stop, -1 = stop
  func selectInstrument(_ i: Int); func selectTuning(_ i: Int)
  func setReferencePitch(_ p: Int)
  func toggleSessionMode(); func exitSessionMode()
  func shareText() -> String         // encodeTuning(currentTuning)
  func onAutoplay(stringIndex: Int)
}
```

Match Kotlin numeric formulas EXACTLY (lerp, YIN steps, loop-count `1-cos` minimization, gaussian envelope).
When in doubt, open the corresponding `android/.../*.kt` file and translate line-by-line.

---

## ✅ AUTHORITATIVE BanjenCore API (built + 81 tests green — use THESE exact names)
`import BanjenCore`. Names mirror Kotlin (top-level constants, UPPER_SNAKE_CASE):
```swift
// constants (top-level lets):
DEFAULT_PITCH=440, MIN_PITCH=432, MAX_PITCH=446, PREFS_NAME="banjen_prefs",
KEY_REFERENCE_PITCH="reference_pitch", KEY_INSTRUMENT_INDEX="instrument_index",
KEY_TUNING_INDEX="tuning_index", SECONDS_PER_STRING=5, TONE_SAMPLE_RATE=44100, AMPLITUDE_SCALE: Float=0.7
// funcs:
calculatePitchRatio(_:Int)->Float, clampPitch(_:Int)->Int, canDecreasePitch(_:Int)->Bool,
canIncreasePitch(_:Int)->Bool, clampVolume(_:Float)->Float, autoAdvanceNextIndex(_:Int)->Int?,
generateSineWaveSamples(frequency:Float, sampleRate:Int, numSamples:Int)->[Int16],
calculateLoopSampleCount(frequency:Float, sampleRate:Int)->Int,
noteFrequency(_:Int)->Float, encodeTuning(_:Tuning)->String, decodeTuning(_:String)->Tuning?
// types:
enum TuningStatus: Sendable,Equatable { .inTune .close .sharp .flat .noSignal }  // verify cases in file
struct PitchResult { detectedHz:Double; targetHz:Double; centDeviation:Double; status:TuningStatus; init(detectedHz:targetHz:centDeviation:status:) }
final class PitchDetector: Sendable { init(sampleRate:Int=44100); detectPitch(_:[Float])->Double; centsFromTarget(detected:Double,target:Double)->Double; classifyTuning(_:Double)->TuningStatus }
struct Note { name:String; frequency:Float; init(_ name:_ frequency:) }
struct Tuning { name:String; notes:[Note]; init(name:notes:) }
struct Instrument { name:String; tunings:[Tuning]; init(name:tunings:) }
let FOUR_STRING_BANJO: Instrument; let FIVE_STRING_BANJO: Instrument; let ALL_INSTRUMENTS: [Instrument]
enum TuningAnimationState: Sendable,Equatable { .idle .stringSelected .noSignal .flat .sharp .close .inTune }  // verify in file
deriveTuningAnimationState(selectedOption:Int, pitchCheckMode:Bool, pitchResult:PitchResult?)->TuningAnimationState
beatFrequencyHz(_:Float)->Float; ringAsymmetryOffset(_:Float)->Float
```

## SwiftUI VIEW CONTRACTS (Wave-2 agents must match these signatures for integration)
```swift
struct BanjoStringCanvas: View { init(notes: [Note], selectedString: Int, onStringSelected: @escaping (Int)->Void) }
struct TuningAnimationView: View { init(selectedOption: Int, pitchCheckMode: Bool, pitchResult: PitchResult?) }
struct AdBannerView: View { init() }                 // fixed 320x50 placeholder slot
@MainActor final class ToneGenerator {               // class, not view
  var isPlaying: Bool { get }
  func play(_ frequency: Float); func stop(); func setVolume(_ v: Float); func release()
}
```
