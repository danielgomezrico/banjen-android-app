package com.makingiants.android.banjotuner

import org.junit.Test
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PitchDetectorTest {
    private val sampleRate = 44100
    private val detector = PitchDetector(sampleRate)

    private fun generateSineWave(
        frequencyHz: Double,
        numSamples: Int = 4096,
    ): FloatArray =
        FloatArray(numSamples) { i ->
            sin(2.0 * PI * frequencyHz * i / sampleRate).toFloat()
        }

    @Test
    fun `detectPitch returns correct frequency for A4 440Hz sine wave`() {
        val samples = generateSineWave(440.0)
        val detected = detector.detectPitch(samples)
        assertTrue(detected > 0, "Expected positive frequency, got $detected")
        assertTrue(abs(detected - 440.0) < 5.0, "Expected ~440Hz, got $detected")
    }

    @Test
    fun `detectPitch returns correct frequency for D4 293Hz banjo string`() {
        val samples = generateSineWave(293.66)
        val detected = detector.detectPitch(samples)
        assertTrue(abs(detected - 293.66) < 3.0, "Expected ~293.66Hz, got $detected")
    }

    @Test
    fun `detectPitch returns correct frequency for B3 247Hz banjo string`() {
        val samples = generateSineWave(246.94)
        val detected = detector.detectPitch(samples)
        assertTrue(abs(detected - 246.94) < 3.0, "Expected ~246.94Hz, got $detected")
    }

    @Test
    fun `detectPitch returns correct frequency for G3 196Hz banjo string`() {
        val samples = generateSineWave(196.00)
        val detected = detector.detectPitch(samples)
        assertTrue(abs(detected - 196.00) < 3.0, "Expected ~196.00Hz, got $detected")
    }

    @Test
    fun `detectPitch returns correct frequency for D3 147Hz banjo string`() {
        val samples = generateSineWave(146.83, numSamples = 8192)
        val detected = detector.detectPitch(samples)
        assertTrue(abs(detected - 146.83) < 3.0, "Expected ~146.83Hz, got $detected")
    }

    @Test
    fun `detectPitch returns negative for silence`() {
        val samples = FloatArray(4096) { 0.0f }
        val detected = detector.detectPitch(samples)
        assertTrue(detected < 0, "Expected no pitch for silence, got $detected")
    }

    @Test
    fun `centsFromTarget returns zero for exact match`() {
        val cents = detector.centsFromTarget(440.0, 440.0)
        assertEquals(0.0, cents, 0.001)
    }

    @Test
    fun `centsFromTarget returns positive for sharp`() {
        val cents = detector.centsFromTarget(450.0, 440.0)
        assertTrue(cents > 0, "Expected positive cents for sharp, got $cents")
    }

    @Test
    fun `centsFromTarget returns negative for flat`() {
        val cents = detector.centsFromTarget(430.0, 440.0)
        assertTrue(cents < 0, "Expected negative cents for flat, got $cents")
    }

    @Test
    fun `classifyTuning returns IN_TUNE within 10 cents`() {
        assertEquals(TuningStatus.IN_TUNE, detector.classifyTuning(0.0))
        assertEquals(TuningStatus.IN_TUNE, detector.classifyTuning(5.0))
        assertEquals(TuningStatus.IN_TUNE, detector.classifyTuning(-10.0))
    }

    @Test
    fun `classifyTuning returns CLOSE within 25 cents`() {
        assertEquals(TuningStatus.CLOSE, detector.classifyTuning(15.0))
        assertEquals(TuningStatus.CLOSE, detector.classifyTuning(-20.0))
    }

    @Test
    fun `classifyTuning returns SHARP beyond 25 cents positive`() {
        assertEquals(TuningStatus.SHARP, detector.classifyTuning(30.0))
        assertEquals(TuningStatus.SHARP, detector.classifyTuning(50.0))
    }

    @Test
    fun `classifyTuning returns FLAT beyond 25 cents negative`() {
        assertEquals(TuningStatus.FLAT, detector.classifyTuning(-30.0))
        assertEquals(TuningStatus.FLAT, detector.classifyTuning(-50.0))
    }
}
