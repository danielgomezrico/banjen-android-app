import AVFoundation
import BanjenCore

/// Captures microphone audio, feeds it to PitchDetector, and publishes PitchResult.
/// Mirrors Android AudioCaptureEffect / AudioRecord loop.
@MainActor
final class PitchCaptureEngine {
    private let targetFrequency: Double
    private let onResult: @MainActor (PitchResult) -> Void

    private var captureTask: Task<Void, Never>?
    private let engine = AVAudioEngine()
    private let detector = PitchDetector(sampleRate: 44100)
    private let bufferSize: AVAudioFrameCount = 4096

    init(targetFrequency: Double, onResult: @escaping @MainActor (PitchResult) -> Void) {
        self.targetFrequency = targetFrequency
        self.onResult = onResult
    }

    func start() {
        requestPermissionAndStart()
    }

    func stop() {
        captureTask?.cancel()
        captureTask = nil
        if engine.isRunning { engine.stop() }
        engine.inputNode.removeTap(onBus: 0)
    }

    // MARK: - Private

    private func requestPermissionAndStart() {
        AVAudioApplication.requestRecordPermission { [weak self] granted in
            guard granted, let self else { return }
            Task { @MainActor in
                self.startCapture()
            }
        }
    }

    private func startCapture() {
        let inputNode = engine.inputNode
        let inputFormat = inputNode.inputFormat(forBus: 0)

        // Use native input format; resample to mono 44100 for detector
        let monoFormat = AVAudioFormat(
            commonFormat: .pcmFormatFloat32,
            sampleRate: 44100,
            channels: 1,
            interleaved: false
        )

        let targetFreq = self.targetFrequency

        inputNode.installTap(onBus: 0, bufferSize: bufferSize, format: inputFormat) { [weak self] buffer, _ in
            guard let self else { return }
            let samples = self.extractSamples(buffer: buffer, monoFormat: monoFormat)
            let detectedHz = self.detector.detectPitch(samples)
            let result: PitchResult
            if detectedHz > 0 {
                let cents = self.detector.centsFromTarget(detected: detectedHz, target: targetFreq)
                result = PitchResult(
                    detectedHz: detectedHz,
                    targetHz: targetFreq,
                    centDeviation: cents,
                    status: self.detector.classifyTuning(cents)
                )
            } else {
                result = PitchResult(
                    detectedHz: 0,
                    targetHz: targetFreq,
                    centDeviation: 0,
                    status: .noSignal
                )
            }
            let capturedResult = result
            Task { @MainActor [weak self] in
                self?.onResult(capturedResult)
            }
        }

        do {
            try AVAudioSession.sharedInstance().setCategory(.playAndRecord, options: [.defaultToSpeaker, .mixWithOthers])
            try AVAudioSession.sharedInstance().setActive(true)
            try engine.start()
        } catch {
            // Degrade gracefully — mic unavailable
            engine.inputNode.removeTap(onBus: 0)
        }
    }

    private func extractSamples(buffer: AVAudioPCMBuffer, monoFormat: AVAudioFormat?) -> [Float] {
        guard let channelData = buffer.floatChannelData else { return [] }
        let frameCount = Int(buffer.frameLength)
        let channelCount = Int(buffer.format.channelCount)
        var mono = [Float](repeating: 0, count: frameCount)
        // Average channels to mono
        for frame in 0 ..< frameCount {
            var sum: Float = 0
            for ch in 0 ..< channelCount {
                sum += channelData[ch][frame]
            }
            mono[frame] = sum / Float(max(1, channelCount))
        }
        return mono
    }

    deinit {
        // Task cancel and engine stop must happen from owning context (MainActor)
        // since stop() is @MainActor, callers must invoke stop() before deinit.
    }
}
