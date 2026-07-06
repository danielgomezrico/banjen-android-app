import Testing
@testable import BanjenCore

@Suite("AppConstants")
struct AppConstantsTests {

    // MARK: clampPitch

    @Test func clampPitch_withinRange_unchanged() {
        #expect(clampPitch(440) == 440)
    }

    @Test func clampPitch_belowMin_clampsToMin() {
        #expect(clampPitch(400) == 432)
    }

    @Test func clampPitch_aboveMax_clampsToMax() {
        #expect(clampPitch(500) == 446)
    }

    @Test func clampPitch_atMin() {
        #expect(clampPitch(432) == 432)
    }

    @Test func clampPitch_atMax() {
        #expect(clampPitch(446) == 446)
    }

    // MARK: canIncreasePitch / canDecreasePitch

    @Test func canIncreasePitch_belowMax() {
        #expect(canIncreasePitch(440) == true)
    }

    @Test func canIncreasePitch_atMax() {
        #expect(canIncreasePitch(446) == false)
    }

    @Test func canDecreasePitch_aboveMin() {
        #expect(canDecreasePitch(440) == true)
    }

    @Test func canDecreasePitch_atMin() {
        #expect(canDecreasePitch(432) == false)
    }

    // MARK: calculatePitchRatio

    @Test func calculatePitchRatio_440_isOne() {
        #expect(abs(calculatePitchRatio(440) - 1.0) < 0.0001)
    }

    @Test func calculatePitchRatio_432() {
        let ratio = calculatePitchRatio(432)
        #expect(abs(ratio - 432.0 / 440.0) < 0.0001)
    }

    // MARK: autoAdvanceNextIndex

    @Test func autoAdvanceNextIndex_0_returns1() {
        #expect(autoAdvanceNextIndex(0) == 1)
    }

    @Test func autoAdvanceNextIndex_2_returns3() {
        #expect(autoAdvanceNextIndex(2) == 3)
    }

    @Test func autoAdvanceNextIndex_3_returnsNil() {
        #expect(autoAdvanceNextIndex(3) == nil)
    }

    // MARK: clampVolume

    @Test func clampVolume_midRange_unchanged() {
        #expect(abs(clampVolume(0.5) - 0.5) < 0.0001)
    }

    @Test func clampVolume_negative_clampsToZero() {
        #expect(clampVolume(-1.0) == 0.0)
    }

    @Test func clampVolume_overOne_clampsToOne() {
        #expect(clampVolume(2.0) == 1.0)
    }

    // MARK: Constants

    @Test func defaultPitch_is440() {
        #expect(DEFAULT_PITCH == 440)
    }

    @Test func minPitch_is432() {
        #expect(MIN_PITCH == 432)
    }

    @Test func maxPitch_is446() {
        #expect(MAX_PITCH == 446)
    }

    @Test func secondsPerString_is5() {
        #expect(SECONDS_PER_STRING == 5)
    }
}
