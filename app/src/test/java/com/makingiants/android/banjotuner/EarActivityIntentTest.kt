package com.makingiants.android.banjotuner

import org.junit.Test
import kotlin.test.assertEquals

class EarActivityIntentTest {
    @Test
    fun `EXTRA_STRING_INDEX constant value is string_index`() {
        assertEquals("string_index", EarActivity.EXTRA_STRING_INDEX)
    }

    @Test
    fun `validateStringIndex returns valid index for 0`() {
        assertEquals(0, EarActivity.validateStringIndex(0))
    }

    @Test
    fun `validateStringIndex returns valid index for 3`() {
        assertEquals(3, EarActivity.validateStringIndex(3))
    }

    @Test
    fun `validateStringIndex returns valid index for 2`() {
        assertEquals(2, EarActivity.validateStringIndex(2))
    }

    @Test
    fun `validateStringIndex returns -1 for negative index`() {
        assertEquals(-1, EarActivity.validateStringIndex(-5))
    }

    @Test
    fun `validateStringIndex returns -1 for index above 3`() {
        assertEquals(-1, EarActivity.validateStringIndex(4))
    }

    @Test
    fun `validateStringIndex returns -1 for -1`() {
        assertEquals(-1, EarActivity.validateStringIndex(-1))
    }

    @Test
    fun `validateStringIndex returns valid for all string indices`() {
        for (i in 0..3) {
            assertEquals(i, EarActivity.validateStringIndex(i))
        }
    }
}
