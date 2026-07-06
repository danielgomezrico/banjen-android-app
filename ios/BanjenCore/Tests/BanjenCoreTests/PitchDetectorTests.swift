import Testing
import Darwin
@testable import BanjenCore

@Suite("PitchDetector")
struct PitchDetectorTests {

    let detector = PitchDetector(sampleRate: 44100)

    // MARK: centsFromTarget

    @Test func centsFromTarget_sameFreq_isZero() {
        let cents = detector.centsFromTarget(detected: 440.0, target: 440.0)
        #expect(abs(cents) < 0.001)
    }

    @Test func centsFromTarget_detectedAboveTarget_isPositive_sharp() {
        // Higher detected frequency → positive cents (sharp)
        let cents = detector.centsFromTarget(detected: 450.0, target: 440.0)
        #expect(cents > 0)
    }

    @Test func centsFromTarget_detectedBelowTarget_isNegative_flat() {
        // Lower detected frequency → negative cents (flat)
        let cents = detector.centsFromTarget(detected: 430.0, target: 440.0)
        #expect(cents < 0)
    }

    @Test func centsFromTarget_oneOctaveUp_is1200() {
        let cents = detector.centsFromTarget(detected: 880.0, target: 440.0)
        #expect(abs(cents - 1200.0) < 0.001)
    }

    @Test func centsFromTarget_zeroDetected_isZero() {
        #expect(detector.centsFromTarget(detected: 0, target: 440.0) == 0.0)
    }

    @Test func centsFromTarget_zeroTarget_isZero() {
        #expect(detector.centsFromTarget(detected: 440.0, target: 0) == 0.0)
    }

    // MARK: classifyTuning

    @Test func classifyTuning_exactly0_isInTune() {
        #expect(detector.classifyTuning(0.0) == .inTune)
    }

    @Test func classifyTuning_10cents_isInTune() {
        #expect(detector.classifyTuning(10.0) == .inTune)
        #expect(detector.classifyTuning(-10.0) == .inTune)
    }

    @Test func classifyTuning_11cents_isClose() {
        #expect(detector.classifyTuning(11.0) == .close)
        #expect(detector.classifyTuning(-11.0) == .close)
    }

    @Test func classifyTuning_25cents_isClose() {
        #expect(detector.classifyTuning(25.0) == .close)
        #expect(detector.classifyTuning(-25.0) == .close)
    }

    @Test func classifyTuning_26centsPositive_isSharp() {
        #expect(detector.classifyTuning(26.0) == .sharp)
    }

    @Test func classifyTuning_26centsNegative_isFlat() {
        #expect(detector.classifyTuning(-26.0) == .flat)
    }

    @Test func classifyTuning_largePosivite_isSharp() {
        #expect(detector.classifyTuning(100.0) == .sharp)
    }

    @Test func classifyTuning_largeNegative_isFlat() {
        #expect(detector.classifyTuning(-100.0) == .flat)
    }

    // MARK: detectPitch

    @Test func detectPitch_emptySamples_returnsNegative() {
        #expect(detector.detectPitch([]) == -1.0)
    }

    @Test func detectPitch_sineTone_detectsFrequency() {
        // Generate a 440 Hz sine wave at 44100 Hz
        let sampleRate = 44100
        let frequency = 440.0
        let numSamples = 4096
        var samples = [Float](repeating: 0, count: numSamples)
        for i in 0 ..< numSamples {
            samples[i] = Float(sin(2.0 * Double.pi * frequency * Double(i) / Double(sampleRate)))
        }
        let detected = detector.detectPitch(samples)
        #expect(detected > 0, "Expected positive frequency, got \(detected)")
        // Within 1% tolerance
        #expect(abs(detected - frequency) / frequency < 0.01, "Expected ~440 Hz, got \(detected)")
    }
}
