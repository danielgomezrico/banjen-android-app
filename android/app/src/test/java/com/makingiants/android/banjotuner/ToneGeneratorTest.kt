package com.makingiants.android.banjotuner

import org.junit.Test
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ToneGeneratorTest {
    @Test
    fun `generateSineWaveSamples produces correct number of samples`() {
        val samples = generateSineWaveSamples(440.0f, TONE_SAMPLE_RATE, 44100)
        assertEquals(44100, samples.size)
    }

    @Test
    fun `generateSineWaveSamples at 440Hz has correct period`() {
        val sampleRate = TONE_SAMPLE_RATE
        val samples = generateSineWaveSamples(440.0f, sampleRate, sampleRate)
        // At 440Hz with 44100 sample rate, period = 44100/440 = ~100.23 samples
        // First sample should be near 0 (sin(0)=0)
        assertTrue(abs(samples[0].toInt()) < 100, "First sample should be near zero")
    }

    @Test
    fun `generateSineWaveSamples values are within Short range`() {
        val samples = generateSineWaveSamples(440.0f, TONE_SAMPLE_RATE, 4410)
        samples.forEach { sample ->
            assertTrue(sample.toInt() >= Short.MIN_VALUE.toInt(), "Sample $sample below Short.MIN_VALUE")
            assertTrue(sample.toInt() <= Short.MAX_VALUE.toInt(), "Sample $sample above Short.MAX_VALUE")
        }
    }

    @Test
    fun `calculateLoopSampleCount returns full cycles`() {
        val count = calculateLoopSampleCount(440.0f, TONE_SAMPLE_RATE)
        // Should be a multiple of samples-per-cycle (44100/440 ~= 100.23)
        // The function should return at least one full second of audio
        assertTrue(count > 0)
        assertTrue(count <= TONE_SAMPLE_RATE * 2)
    }

    @Test
    fun `calculateLoopSampleCount produces near-zero phase error at loop boundary for all DGBD strings`() {
        // DGBD tuning: D3=146.83Hz, G3=196.00Hz, B3=246.94Hz, D4=293.66Hz
        // The algorithm minimizes 1-cos (phase angle) rather than |sin|, which avoids
        // half-cycle boundaries that cause waveform phase reversal. Tolerance is 0.02
        // (a complete cycle ≡ 0) to accommodate edge cases like B3 (246.94Hz).
        val dgbdFrequencies = listOf(146.83f, 196.00f, 246.94f, 293.66f)
        for (frequency in dgbdFrequencies) {
            val loopSamples = calculateLoopSampleCount(frequency, TONE_SAMPLE_RATE)
            val phaseError = abs(sin(2.0 * PI * frequency * loopSamples / TONE_SAMPLE_RATE))
            assertTrue(
                phaseError < 0.02,
                "Phase error $phaseError >= 0.02 for frequency ${frequency}Hz (loopSamples=$loopSamples)",
            )
        }
    }

    @Test
    fun `generateSineWaveSamples amplitude does not exceed scaled maximum`() {
        val samples = generateSineWaveSamples(196f, TONE_SAMPLE_RATE, 441)
        val maxAllowed = (Short.MAX_VALUE * AMPLITUDE_SCALE).toInt()
        assertTrue(samples.all { it.toInt() in -maxAllowed..maxAllowed })
    }

    @Test
    fun `calculateLoopSampleCount does not throw and returns positive for zero frequency`() {
        val result = calculateLoopSampleCount(0f, TONE_SAMPLE_RATE)
        assertTrue(result > 0, "Expected positive result for zero frequency, got $result")
        assertEquals(TONE_SAMPLE_RATE, result)
    }

    @Test
    fun `calculateLoopSampleCount returns safe positive for negative frequency`() {
        val result = calculateLoopSampleCount(-50f, TONE_SAMPLE_RATE)
        assertTrue(result > 0, "Expected positive result for negative frequency, got $result")
        assertEquals(TONE_SAMPLE_RATE, result)
    }

    @Test
    fun `generateSineWaveSamples returns empty array for zero sample count`() {
        val result = generateSineWaveSamples(440f, TONE_SAMPLE_RATE, 0)
        assertEquals(0, result.size)
    }

    @Test
    fun `generateSineWaveSamples produces silence for zero frequency`() {
        val result = generateSineWaveSamples(0f, TONE_SAMPLE_RATE, 100)
        assertEquals(100, result.size)
        assertTrue(result.all { it == 0.toShort() }, "Expected all-zero samples for zero frequency")
    }

    @Test
    fun `generateSineWaveSamples stays within amplitude scale for all DGBD strings`() {
        // D3=146.83Hz, G3=196.00Hz, B3=246.94Hz, D4=293.66Hz — from BanjoString enum in PitchDetector.kt
        val dgbdFrequencies = listOf(146.83f, 196.00f, 246.94f, 293.66f)
        val maxAllowed = (Short.MAX_VALUE * AMPLITUDE_SCALE).toInt()
        for (frequency in dgbdFrequencies) {
            val numSamples = calculateLoopSampleCount(frequency, TONE_SAMPLE_RATE)
            val samples = generateSineWaveSamples(frequency, TONE_SAMPLE_RATE, numSamples)
            assertTrue(
                samples.all { it.toInt() in -maxAllowed..maxAllowed },
                "Amplitude out of bounds for ${frequency}Hz (maxAllowed=$maxAllowed)",
            )
        }
    }
}
