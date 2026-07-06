import SwiftUI
import AVFoundation
import BanjenCore

@Observable
@MainActor
final class EarViewModel {
    // MARK: - Persisted state
    private(set) var instrumentIndex: Int
    private(set) var tuningIndex: Int
    private(set) var referencePitch: Int

    // MARK: - Runtime state
    var selectedStringIndex: Int = -1
    var sessionModeActive: Bool = false
    var isVolumeLow: Bool = false
    var pitchCheckMode: Bool = false
    var pitchResult: PitchResult? = nil

    // MARK: - Computed
    var currentInstrument: Instrument { ALL_INSTRUMENTS[instrumentIndex] }
    var currentTuning: Tuning { currentInstrument.tunings[tuningIndex] }

    // MARK: - Dependencies
    let toneGenerator = ToneGenerator()
    private var sessionTask: Task<Void, Never>?
    private let prefs: UserDefaults

    init() {
        let prefs = UserDefaults(suiteName: PREFS_NAME) ?? .standard
        self.prefs = prefs

        let savedInstrument = prefs.object(forKey: KEY_INSTRUMENT_INDEX) != nil
            ? prefs.integer(forKey: KEY_INSTRUMENT_INDEX) : 0
        let clampedInstrument = max(0, min(savedInstrument, ALL_INSTRUMENTS.count - 1))
        self.instrumentIndex = clampedInstrument

        let savedTuning = prefs.object(forKey: KEY_TUNING_INDEX) != nil
            ? prefs.integer(forKey: KEY_TUNING_INDEX) : 0
        let clampedTuning = max(0, min(savedTuning, ALL_INSTRUMENTS[clampedInstrument].tunings.count - 1))
        self.tuningIndex = clampedTuning

        let savedPitch = prefs.object(forKey: KEY_REFERENCE_PITCH) != nil
            ? prefs.integer(forKey: KEY_REFERENCE_PITCH) : DEFAULT_PITCH
        self.referencePitch = clampPitch(savedPitch)
    }

    // MARK: - String selection

    func selectString(_ index: Int) {
        guard !sessionModeActive else { return }
        if index == -1 {
            toneGenerator.stop()
            selectedStringIndex = -1
            isVolumeLow = false
        } else {
            guard let note = currentTuning.notes[safe: index] else { return }
            let effectiveHz = note.frequency * calculatePitchRatio(referencePitch)
            toneGenerator.play(effectiveHz)
            selectedStringIndex = index
            updateVolumeLow()
        }
    }

    // MARK: - Instrument / Tuning

    func selectInstrument(_ index: Int) {
        toneGenerator.stop()
        selectedStringIndex = -1
        isVolumeLow = false
        instrumentIndex = index
        tuningIndex = 0
        prefs.set(index, forKey: KEY_INSTRUMENT_INDEX)
        prefs.set(0, forKey: KEY_TUNING_INDEX)
    }

    func selectTuning(_ index: Int) {
        toneGenerator.stop()
        selectedStringIndex = -1
        isVolumeLow = false
        tuningIndex = index
        prefs.set(index, forKey: KEY_TUNING_INDEX)
    }

    // MARK: - Reference pitch

    func setReferencePitch(_ pitch: Int) {
        referencePitch = clampPitch(pitch)
        prefs.set(referencePitch, forKey: KEY_REFERENCE_PITCH)
        if selectedStringIndex >= 0, let note = currentTuning.notes[safe: selectedStringIndex] {
            let effectiveHz = note.frequency * calculatePitchRatio(referencePitch)
            toneGenerator.play(effectiveHz)
        }
    }

    // MARK: - Session mode

    func startSessionMode() {
        guard !sessionModeActive else { return }
        toneGenerator.stop()
        selectedStringIndex = -1
        sessionModeActive = true
        let notes = currentTuning.notes
        sessionTask = Task { @MainActor [weak self] in
            guard let self else { return }
            var index = 0
            while index < notes.count && self.sessionModeActive {
                self.selectedStringIndex = index
                self.toneGenerator.play(notes[index].frequency)
                try? await Task.sleep(for: .seconds(SECONDS_PER_STRING))
                guard self.sessionModeActive else { break }
                if let next = autoAdvanceNextIndex(index) {
                    index = next
                } else {
                    self.exitSessionMode()
                    break
                }
            }
        }
    }

    func exitSessionMode() {
        sessionModeActive = false
        sessionTask?.cancel()
        sessionTask = nil
        toneGenerator.stop()
        selectedStringIndex = -1
    }

    // MARK: - Autoplay (deep link / widget)

    func onAutoplay(stringIndex: Int) {
        guard currentTuning.notes.indices.contains(stringIndex) else { return }
        selectString(stringIndex)
    }

    // MARK: - Share

    func shareText() -> String {
        "Banjen Tuning: \(encodeTuning(currentTuning))"
    }

    // MARK: - Volume

    private func updateVolumeLow() {
        let volume = AVAudioSession.sharedInstance().outputVolume
        isVolumeLow = volume < 0.1
    }

    // MARK: - Lifecycle

    func onBackground() {
        if sessionModeActive { exitSessionMode() }
        toneGenerator.stop()
        pitchCheckMode = false
        pitchResult = nil
    }

    func release() {
        toneGenerator.release()
    }
}

// MARK: - Safe subscript helper

extension Array {
    subscript(safe index: Int) -> Element? {
        indices.contains(index) ? self[index] : nil
    }
}
