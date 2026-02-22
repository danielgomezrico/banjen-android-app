package com.makingiants.android.banjotuner

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SessionModeTest {
    @Test
    fun `clampVolume at minimum returns 0`() {
        assertEquals(0.0f, clampVolume(0.0f))
    }

    @Test
    fun `clampVolume at maximum returns 1`() {
        assertEquals(1.0f, clampVolume(1.0f))
    }

    @Test
    fun `clampVolume below minimum clamps to 0`() {
        assertEquals(0.0f, clampVolume(-0.5f))
    }

    @Test
    fun `clampVolume above maximum clamps to 1`() {
        assertEquals(1.0f, clampVolume(1.5f))
    }

    @Test
    fun `clampVolume mid-range preserves value`() {
        assertEquals(0.3f, clampVolume(0.3f))
    }

    @Test
    fun `autoAdvanceNextIndex from 0 returns 1`() {
        assertEquals(1, autoAdvanceNextIndex(0))
    }

    @Test
    fun `autoAdvanceNextIndex from 1 returns 2`() {
        assertEquals(2, autoAdvanceNextIndex(1))
    }

    @Test
    fun `autoAdvanceNextIndex from 2 returns 3`() {
        assertEquals(3, autoAdvanceNextIndex(2))
    }

    @Test
    fun `autoAdvanceNextIndex from 3 returns null`() {
        assertNull(autoAdvanceNextIndex(3))
    }
}
