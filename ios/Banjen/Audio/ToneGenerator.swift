import AVFoundation
import BanjenCore

/// iOS ToneGenerator — plays a looping synthesized sine tone via AVAudioEngine.
///
/// Warm-up strategy: the AVAudioEngine is started once in `init()` and kept running
/// until `release()`. A running engine holds the audio hardware open, eliminating
/// cold-start transients without a separate silent player. The Kotlin counterpart
/// writes silent frames through a dedicated `warmTrack` AudioTrack (see ToneGenerator.kt);
/// on iOS, keeping the engine alive achieves the same effect.
@MainActor
final class ToneGenerator {

    private let engine = AVAudioEngine()
    private let playerNode = AVAudioPlayerNode()
    private var fadeTask: Task<Void, Never>?
    private var targetVolume: Float = 1.0

    // MARK: - Public API

    var isPlaying: Bool { playerNode.isPlaying }

    init() {
        configureAudioSession()
        engine.attach(playerNode)
        let format = AVAudioFormat(standardFormatWithSampleRate: Double(TONE_SAMPLE_RATE), channels: 1)!
        engine.connect(playerNode, to: engine.mainMixerNode, format: format)
        try? engine.start()
    }

    /// Start looping a sine tone at `frequency` Hz with a 200ms fade-in.
    /// If already playing, fades out the current tone first then fades in the new one.
    func play(_ frequency: Float) {
        fadeTask?.cancel()
        fadeTask = Task { [weak self] in
            guard let self else { return }
            // Fade out and stop any current tone before starting the replacement.
            if self.playerNode.isPlaying {
                await self.fadeOut()
                self.playerNode.stop()
            }
            guard !Task.isCancelled else { return }

            // Build a buffer of exactly one click-free loop period.
            let loopCount = calculateLoopSampleCount(frequency: frequency, sampleRate: TONE_SAMPLE_RATE)
            let int16Samples = generateSineWaveSamples(
                frequency: frequency,
                sampleRate: TONE_SAMPLE_RATE,
                numSamples: loopCount
            )
            guard let buffer = self.makePCMBuffer(from: int16Samples) else { return }

            // Start at volume 0, schedule with infinite loop, then ramp up.
            //
            // Must use the non-async overload here: the async `scheduleBuffer`
            // only resumes once the scheduled segment finishes playing, and with
            // `.loops` that segment never finishes — awaiting it would hang this
            // task forever, and `playerNode.play()` below would never run.
            self.playerNode.volume = 0
            self.playerNode.scheduleBuffer(buffer, at: nil, options: .loops, completionHandler: nil)
            if !self.engine.isRunning { try? self.engine.start() }
            self.playerNode.play()

            await self.fadeIn()
        }
    }

    /// Fade out and stop the current tone.
    func stop() {
        fadeTask?.cancel()
        fadeTask = Task { [weak self] in
            guard let self else { return }
            await self.fadeOut()
            self.playerNode.stop()
        }
    }

    /// Set the overall output level (used by session mode). Clamped to 0...1.
    func setVolume(_ v: Float) {
        targetVolume = min(max(v, 0), 1)
        if playerNode.isPlaying {
            playerNode.volume = targetVolume
        }
    }

    /// Tear down the engine. Call when this ToneGenerator will no longer be used.
    func release() {
        fadeTask?.cancel()
        playerNode.stop()
        engine.stop()
    }

    // MARK: - Private

    private func configureAudioSession() {
        let session = AVAudioSession.sharedInstance()
        // .playback keeps the tone audible even when the silent switch is engaged —
        // expected behaviour for an instrument tuner.
        try? session.setCategory(.playback, mode: .default)
        try? session.setActive(true)
    }

    /// Convert an [Int16] sample array into a Float32 AVAudioPCMBuffer.
    /// AMPLITUDE_SCALE is already baked into the samples by generateSineWaveSamples,
    /// so the conversion is a straight division by Int16.max.
    private func makePCMBuffer(from samples: [Int16]) -> AVAudioPCMBuffer? {
        let format = AVAudioFormat(standardFormatWithSampleRate: Double(TONE_SAMPLE_RATE), channels: 1)!
        guard let buffer = AVAudioPCMBuffer(pcmFormat: format, frameCapacity: AVAudioFrameCount(samples.count)) else {
            return nil
        }
        buffer.frameLength = buffer.frameCapacity
        guard let floatData = buffer.floatChannelData?[0] else { return nil }
        let scale = 1.0 / Float(Int16.max)
        for i in 0 ..< samples.count {
            floatData[i] = Float(samples[i]) * scale
        }
        return buffer
    }

    // MARK: - Fade helpers
    //
    // Mirrors Kotlin's FADE_DURATION_MS = 200, FADE_STEPS = 20 (10 ms per step).
    // Each step uses Task.sleep, which suspends the task and yields the main thread,
    // keeping the UI responsive during the ramp.

    private static let fadeSteps = 20
    private static let stepDelayNs: UInt64 = 10_000_000 // 10 ms

    private func fadeIn() async {
        for step in 0 ... Self.fadeSteps {
            guard !Task.isCancelled else { break }
            playerNode.volume = Float(step) / Float(Self.fadeSteps) * targetVolume
            if step < Self.fadeSteps {
                try? await Task.sleep(nanoseconds: Self.stepDelayNs)
            }
        }
    }

    private func fadeOut() async {
        let startVolume = playerNode.volume
        for step in stride(from: Self.fadeSteps, through: 0, by: -1) {
            guard !Task.isCancelled else { break }
            playerNode.volume = startVolume * Float(step) / Float(Self.fadeSteps)
            if step > 0 {
                try? await Task.sleep(nanoseconds: Self.stepDelayNs)
            }
        }
    }
}
