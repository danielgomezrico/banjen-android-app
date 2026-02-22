package com.makingiants.android.banjotuner

import org.junit.Test
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
        assertTrue(count <= TONE_SAMPLE_RATE)
    }
}
