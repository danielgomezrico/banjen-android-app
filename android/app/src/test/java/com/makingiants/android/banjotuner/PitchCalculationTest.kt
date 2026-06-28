package com.makingiants.android.banjotuner

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PitchCalculationTest {
    @Test
    fun `default A440 produces pitch ratio of 1_0`() {
        val ratio = calculatePitchRatio(440)
        assertEquals(1.0f, ratio, 0.0001f)
    }

    @Test
    fun `A432 produces pitch ratio below 1`() {
        val ratio = calculatePitchRatio(432)
        assertEquals(432f / 440f, ratio, 0.0001f)
    }

    @Test
    fun `A446 produces pitch ratio above 1`() {
        val ratio = calculatePitchRatio(446)
        assertEquals(446f / 440f, ratio, 0.0001f)
    }

    @Test
    fun `pitch ratio at lower boundary 432`() {
        val ratio = calculatePitchRatio(432)
        assertEquals(0.98182f, ratio, 0.001f)
    }

    @Test
    fun `pitch ratio at upper boundary 446`() {
        val ratio = calculatePitchRatio(446)
        assertEquals(1.01364f, ratio, 0.001f)
    }

    @Test
    fun `clampPitch coerces below minimum to MIN_PITCH`() {
        assertEquals(MIN_PITCH, clampPitch(430))
    }

    @Test
    fun `clampPitch coerces above maximum to MAX_PITCH`() {
        assertEquals(MAX_PITCH, clampPitch(450))
    }

    @Test
    fun `clampPitch preserves value within range`() {
        assertEquals(440, clampPitch(440))
        assertEquals(432, clampPitch(432))
        assertEquals(446, clampPitch(446))
    }

    @Test
    fun `canDecreasePitch is false at minimum`() {
        assertFalse(canDecreasePitch(MIN_PITCH))
    }

    @Test
    fun `canDecreasePitch is true above minimum`() {
        assertTrue(canDecreasePitch(MIN_PITCH + 1))
    }

    @Test
    fun `canIncreasePitch is false at maximum`() {
        assertFalse(canIncreasePitch(MAX_PITCH))
    }

    @Test
    fun `canIncreasePitch is true below maximum`() {
        assertTrue(canIncreasePitch(MAX_PITCH - 1))
    }
}
