import Testing
@testable import BanjenCore

@Suite("TuningAnimationState")
struct TuningAnimationStateTests {

    // MARK: deriveTuningAnimationState

    @Test func derive_negativeSelected_isIdle() {
        let state = deriveTuningAnimationState(selectedOption: -1, pitchCheckMode: false, pitchResult: nil)
        #expect(state == .idle)
    }

    @Test func derive_selectedNoPitchCheck_isStringSelected() {
        let state = deriveTuningAnimationState(selectedOption: 0, pitchCheckMode: false, pitchResult: nil)
        #expect(state == .stringSelected)
    }

    @Test func derive_pitchCheckNilResult_isNoSignal() {
        let state = deriveTuningAnimationState(selectedOption: 0, pitchCheckMode: true, pitchResult: nil)
        #expect(state == .noSignal)
    }

    @Test func derive_pitchCheckNoSignalStatus_isNoSignal() {
        let result = PitchResult(detectedHz: -1, targetHz: 440, centDeviation: 0, status: .noSignal)
        let state = deriveTuningAnimationState(selectedOption: 0, pitchCheckMode: true, pitchResult: result)
        #expect(state == .noSignal)
    }

    @Test func derive_flat_isFlat() {
        let result = PitchResult(detectedHz: 430, targetHz: 440, centDeviation: -39, status: .flat)
        let state = deriveTuningAnimationState(selectedOption: 0, pitchCheckMode: true, pitchResult: result)
        #expect(state == .flat)
    }

    @Test func derive_sharp_isSharp() {
        let result = PitchResult(detectedHz: 450, targetHz: 440, centDeviation: 39, status: .sharp)
        let state = deriveTuningAnimationState(selectedOption: 0, pitchCheckMode: true, pitchResult: result)
        #expect(state == .sharp)
    }

    @Test func derive_close_isClose() {
        let result = PitchResult(detectedHz: 442, targetHz: 440, centDeviation: 8, status: .close)
        let state = deriveTuningAnimationState(selectedOption: 0, pitchCheckMode: true, pitchResult: result)
        #expect(state == .close)
    }

    @Test func derive_inTune_isInTune() {
        let result = PitchResult(detectedHz: 440, targetHz: 440, centDeviation: 0, status: .inTune)
        let state = deriveTuningAnimationState(selectedOption: 0, pitchCheckMode: true, pitchResult: result)
        #expect(state == .inTune)
    }

    // MARK: beatFrequencyHz

    @Test func beatFrequencyHz_smallDeviation_clampsToMin() {
        // 0 cents / 5 = 0, clamps to 0.5
        let hz = beatFrequencyHz(0)
        #expect(abs(hz - 0.5) < 0.001)
    }

    @Test func beatFrequencyHz_largeDeviation_clampsToMax() {
        // 100 cents / 5 = 20, clamps to 6.0
        let hz = beatFrequencyHz(100)
        #expect(abs(hz - 6.0) < 0.001)
    }

    @Test func beatFrequencyHz_midDeviation_proportional() {
        // 25 cents / 5 = 5.0 Hz
        let hz = beatFrequencyHz(25)
        #expect(abs(hz - 5.0) < 0.001)
    }

    @Test func beatFrequencyHz_negativeDeviation_usesAbsValue() {
        let hz = beatFrequencyHz(-25)
        #expect(abs(hz - 5.0) < 0.001)
    }

    // MARK: ringAsymmetryOffset

    @Test func ringAsymmetryOffset_zeroDeviation_isZero() {
        #expect(abs(ringAsymmetryOffset(0)) < 0.0001)
    }

    @Test func ringAsymmetryOffset_50cents_is1_0_clampedTo0_15() {
        // 50 / 50 = 1.0, clamps to 0.15
        let offset = ringAsymmetryOffset(50)
        #expect(abs(offset - 0.15) < 0.001)
    }

    @Test func ringAsymmetryOffset_negative50cents_clampedToNeg0_15() {
        let offset = ringAsymmetryOffset(-50)
        #expect(abs(offset - (-0.15)) < 0.001)
    }

    @Test func ringAsymmetryOffset_withinRange() {
        // 5 cents: 5/50 = 0.1, within [-0.15, 0.15] so no clamping
        let offset = ringAsymmetryOffset(5)
        #expect(abs(offset - (5.0 / 50.0)) < 0.001)
    }

    @Test func ringAsymmetryOffset_clampsNegative() {
        let offset = ringAsymmetryOffset(-100)
        #expect(offset >= -0.15)
    }

    @Test func ringAsymmetryOffset_clampsPositive() {
        let offset = ringAsymmetryOffset(100)
        #expect(offset <= 0.15)
    }
}
