import Testing
import Darwin
@testable import BanjenCore

@Suite("ToneMath")
struct ToneMathTests {

    // MARK: generateSineWaveSamples

    @Test func generateSineWaveSamples_correctLength() {
        let samples = generateSineWaveSamples(frequency: 440, sampleRate: 44100, numSamples: 1024)
        #expect(samples.count == 1024)
    }

    @Test func generateSineWaveSamples_amplitudeBound() {
        let samples = generateSineWaveSamples(frequency: 440, sampleRate: 44100, numSamples: 4096)
        let maxVal = Float(Int16.max) * AMPLITUDE_SCALE
        for s in samples {
            #expect(abs(Float(s)) <= maxVal + 1, "Sample \(s) exceeds amplitude bound \(maxVal)")
        }
    }

    @Test func generateSineWaveSamples_zeroFrequency_allZero() {
        // sin(0) = 0 for all samples
        let samples = generateSineWaveSamples(frequency: 0, sampleRate: 44100, numSamples: 64)
        for s in samples {
            #expect(s == 0)
        }
    }

    @Test func generateSineWaveSamples_nonZeroForNonZeroFreq() {
        let samples = generateSineWaveSamples(frequency: 440, sampleRate: 44100, numSamples: 1024)
        let hasNonZero = samples.contains { $0 != 0 }
        #expect(hasNonZero)
    }

    // MARK: calculateLoopSampleCount

    @Test func calculateLoopSampleCount_positiveFreq_returnsPositive() {
        let count = calculateLoopSampleCount(frequency: 440, sampleRate: 44100)
        #expect(count > 0)
    }

    @Test func calculateLoopSampleCount_zeroFreq_returnsSampleRate() {
        let count = calculateLoopSampleCount(frequency: 0, sampleRate: 44100)
        #expect(count == 44100)
    }

    @Test func calculateLoopSampleCount_nearIntegerCycles() {
        // For 440 Hz at 44100, we expect very close to whole cycles
        let sampleRate = 44100
        let frequency: Float = 440.0
        let count = calculateLoopSampleCount(frequency: frequency, sampleRate: sampleRate)
        let samplesPerCycle = Double(sampleRate) / Double(frequency)
        let numCycles = Double(count) / samplesPerCycle
        // Should be close to an integer
        let fractional = numCycles - numCycles.rounded()
        #expect(abs(fractional) < 0.05, "Expected near-integer cycles, got \(numCycles)")
    }

    @Test func calculateLoopSampleCount_minimizesCosObjective() {
        // Ensure the chosen count minimizes 1-cos(2π*f/fs * n) better than nominal
        let sampleRate = 44100
        let frequency: Float = 220.0
        let chosen = calculateLoopSampleCount(frequency: frequency, sampleRate: sampleRate)
        let twoPiFOverFs = 2.0 * Double.pi * Double(frequency) / Double(sampleRate)
        let chosenCos = 1.0 - cos(twoPiFOverFs * Double(chosen))
        // Nominal is cycles * samplesPerCycle rounded
        let samplesPerCycle = Float(sampleRate) / frequency
        let cycles = Int((Float(sampleRate) / samplesPerCycle).rounded())
        let nominal = Int((Float(cycles) * samplesPerCycle).rounded())
        let nominalCos = 1.0 - cos(twoPiFOverFs * Double(nominal))
        // Chosen should be <= nominal (it's optimal by construction)
        #expect(chosenCos <= nominalCos + 1e-10)
    }

    // MARK: Constants

    @Test func toneSampleRate_is44100() {
        #expect(TONE_SAMPLE_RATE == 44100)
    }

    @Test func amplitudeScale_is0_7() {
        #expect(abs(AMPLITUDE_SCALE - 0.7) < 0.0001)
    }
}
