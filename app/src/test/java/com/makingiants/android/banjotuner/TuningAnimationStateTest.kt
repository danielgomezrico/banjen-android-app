package com.makingiants.android.banjotuner

import org.junit.Test
import kotlin.test.assertEquals

/**
 * Unit tests for TuningAnimation pure functions: state derivation, beat frequency, and ring asymmetry.
 * These test the logic defined in the animation design spec (synthesis.md Section 6).
 */
class TuningAnimationStateTest {

    // --- State derivation tests ---

    @Test
    fun deriveState_idle_whenNoStringSelected() {
        val state = deriveTuningAnimationState(
            selectedOption = -1,
            pitchCheckMode = false,
            pitchResult = null,
        )
        assertEquals(TuningAnimationState.IDLE, state)
    }

    @Test
    fun deriveState_idle_ignoresPitchResultWhenNoStringSelected() {
        val state = deriveTuningAnimationState(
            selectedOption = -1,
            pitchCheckMode = true,
            pitchResult = PitchResult(
                detectedHz = 147.0,
                targetHz = 146.83,
                centDeviation = 5.0,
                status = TuningStatus.IN_TUNE,
            ),
        )
        assertEquals(TuningAnimationState.IDLE, state)
    }

    @Test
    fun deriveState_stringSelected_whenStringActiveAndNotPitchCheck() {
        for (stringIndex in 0..3) {
            val state = deriveTuningAnimationState(
                selectedOption = stringIndex,
                pitchCheckMode = false,
                pitchResult = null,
            )
            assertEquals(
                TuningAnimationState.STRING_SELECTED,
                state,
                "Expected STRING_SELECTED for string index $stringIndex",
            )
        }
    }

    @Test
    fun deriveState_noSignal_whenPitchCheckWithNoSignal() {
        val state = deriveTuningAnimationState(
            selectedOption = 0,
            pitchCheckMode = true,
            pitchResult = PitchResult(
                detectedHz = 0.0,
                targetHz = 146.83,
                centDeviation = 0.0,
                status = TuningStatus.NO_SIGNAL,
            ),
        )
        assertEquals(TuningAnimationState.NO_SIGNAL, state)
    }

    @Test
    fun deriveState_noSignal_whenPitchCheckWithNullResult() {
        val state = deriveTuningAnimationState(
            selectedOption = 0,
            pitchCheckMode = true,
            pitchResult = null,
        )
        assertEquals(TuningAnimationState.NO_SIGNAL, state)
    }

    @Test
    fun deriveState_flat_whenStatusFlat() {
        val state = deriveTuningAnimationState(
            selectedOption = 0,
            pitchCheckMode = true,
            pitchResult = PitchResult(
                detectedHz = 130.0,
                targetHz = 146.83,
                centDeviation = -30.0,
                status = TuningStatus.FLAT,
            ),
        )
        assertEquals(TuningAnimationState.FLAT, state)
    }

    @Test
    fun deriveState_sharp_whenStatusSharp() {
        val state = deriveTuningAnimationState(
            selectedOption = 0,
            pitchCheckMode = true,
            pitchResult = PitchResult(
                detectedHz = 165.0,
                targetHz = 146.83,
                centDeviation = 30.0,
                status = TuningStatus.SHARP,
            ),
        )
        assertEquals(TuningAnimationState.SHARP, state)
    }

    @Test
    fun deriveState_close_whenStatusClose() {
        val state = deriveTuningAnimationState(
            selectedOption = 0,
            pitchCheckMode = true,
            pitchResult = PitchResult(
                detectedHz = 149.0,
                targetHz = 146.83,
                centDeviation = 15.0,
                status = TuningStatus.CLOSE,
            ),
        )
        assertEquals(TuningAnimationState.CLOSE, state)
    }

    @Test
    fun deriveState_inTune_whenStatusInTune() {
        val state = deriveTuningAnimationState(
            selectedOption = 0,
            pitchCheckMode = true,
            pitchResult = PitchResult(
                detectedHz = 147.0,
                targetHz = 146.83,
                centDeviation = 5.0,
                status = TuningStatus.IN_TUNE,
            ),
        )
        assertEquals(TuningAnimationState.IN_TUNE, state)
    }

    // --- Beat frequency calculation tests ---
    // Formula: beatHz = (|centDeviation| / 5f).coerceIn(0.5f, 6f)

    @Test
    fun beatFrequency_at25cents_returns5Hz() {
        assertEquals(5f, beatFrequencyHz(25f), 0.01f)
    }

    @Test
    fun beatFrequency_at10cents_returns2Hz() {
        assertEquals(2f, beatFrequencyHz(10f), 0.01f)
    }

    @Test
    fun beatFrequency_at15cents_returns3Hz() {
        assertEquals(3f, beatFrequencyHz(15f), 0.01f)
    }

    @Test
    fun beatFrequency_at0cents_returnsMinimum() {
        assertEquals(0.5f, beatFrequencyHz(0f), 0.01f)
    }

    @Test
    fun beatFrequency_at1cent_returnsMinimum() {
        // 1/5 = 0.2, below min 0.5
        assertEquals(0.5f, beatFrequencyHz(1f), 0.01f)
    }

    @Test
    fun beatFrequency_at50cents_returnsCappedAt6Hz() {
        // 50/5 = 10, capped at 6
        assertEquals(6f, beatFrequencyHz(50f), 0.01f)
    }

    @Test
    fun beatFrequency_negativeDeviation_usesAbsoluteValue() {
        assertEquals(5f, beatFrequencyHz(-25f), 0.01f)
    }

    @Test
    fun beatFrequency_negativeLargeDeviation_cappedAt6Hz() {
        assertEquals(6f, beatFrequencyHz(-40f), 0.01f)
    }

    // --- Ring asymmetry offset tests ---
    // Formula: (centDeviation / 50f).coerceIn(-0.15f, 0.15f)

    @Test
    fun ringAsymmetry_zero_returnsZero() {
        assertEquals(0f, ringAsymmetryOffset(0f), 0.001f)
    }

    @Test
    fun ringAsymmetry_5cents_returnsProportional() {
        // 5/50 = 0.1
        assertEquals(0.1f, ringAsymmetryOffset(5f), 0.001f)
    }

    @Test
    fun ringAsymmetry_negative5cents_returnsNegativeProportional() {
        assertEquals(-0.1f, ringAsymmetryOffset(-5f), 0.001f)
    }

    @Test
    fun ringAsymmetry_7point5cents_returnsExactClamp() {
        // 7.5/50 = 0.15 — exactly at clamp boundary
        assertEquals(0.15f, ringAsymmetryOffset(7.5f), 0.001f)
    }

    @Test
    fun ringAsymmetry_positive50cents_clampedToMax() {
        assertEquals(0.15f, ringAsymmetryOffset(50f), 0.001f)
    }

    @Test
    fun ringAsymmetry_negative50cents_clampedToMin() {
        assertEquals(-0.15f, ringAsymmetryOffset(-50f), 0.001f)
    }

    @Test
    fun ringAsymmetry_25cents_clamped() {
        // 25/50 = 0.5, clamped to 0.15
        assertEquals(0.15f, ringAsymmetryOffset(25f), 0.001f)
    }
}
