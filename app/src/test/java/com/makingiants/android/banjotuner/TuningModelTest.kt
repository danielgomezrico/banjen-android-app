package com.makingiants.android.banjotuner

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TuningModelTest {

    @Test
    fun `noteFrequency for A4 returns 440`() {
        assertEquals(440.0f, noteFrequency(69), 0.01f)
    }

    @Test
    fun `noteFrequency for C4 returns 261_63`() {
        assertEquals(261.63f, noteFrequency(60), 0.1f)
    }

    @Test
    fun `noteFrequency for G3 returns 196`() {
        assertEquals(196.0f, noteFrequency(55), 0.1f)
    }

    @Test
    fun `noteFrequency for D3 returns 146_83`() {
        assertEquals(146.83f, noteFrequency(50), 0.1f)
    }

    @Test
    fun `four string banjo has at least 4 tunings`() {
        assertTrue(FOUR_STRING_BANJO.tunings.size >= 4)
    }

    @Test
    fun `five string banjo has at least 4 tunings`() {
        assertTrue(FIVE_STRING_BANJO.tunings.size >= 4)
    }

    @Test
    fun `all four string tunings have 4 notes`() {
        FOUR_STRING_BANJO.tunings.forEach { tuning ->
            assertEquals(4, tuning.notes.size, "Tuning ${tuning.name} should have 4 notes")
        }
    }

    @Test
    fun `all five string tunings have 5 notes`() {
        FIVE_STRING_BANJO.tunings.forEach { tuning ->
            assertEquals(5, tuning.notes.size, "Tuning ${tuning.name} should have 5 notes")
        }
    }

    @Test
    fun `standard 4-string tuning is DGBD`() {
        val standard = FOUR_STRING_BANJO.tunings.first()
        val noteNames = standard.notes.map { it.name }
        assertEquals(listOf("D3", "G3", "B3", "D4"), noteNames)
    }

    @Test
    fun `standard 5-string Open G is gDGBD`() {
        val openG = FIVE_STRING_BANJO.tunings.first()
        val noteNames = openG.notes.map { it.name }
        assertEquals(listOf("g4", "D3", "G3", "B3", "D4"), noteNames)
    }

    @Test
    fun `encodeTuning produces parseable string`() {
        val tuning = Tuning("Test", listOf(Note("A4", 440.0f), Note("B4", 493.88f)))
        val encoded = encodeTuning(tuning)
        assertTrue(encoded.contains("Test"))
        assertTrue(encoded.contains("440.0"))
    }

    @Test
    fun `decodeTuning roundtrips correctly`() {
        val original = Tuning("My Tuning", listOf(Note("A4", 440.0f), Note("C4", 261.63f)))
        val encoded = encodeTuning(original)
        val decoded = decodeTuning(encoded)
        assertNotNull(decoded)
        assertEquals(original.name, decoded.name)
        assertEquals(original.notes.size, decoded.notes.size)
        assertEquals(original.notes[0].name, decoded.notes[0].name)
        assertEquals(original.notes[0].frequency, decoded.notes[0].frequency, 0.01f)
    }

    @Test
    fun `decodeTuning returns null for invalid input`() {
        assertNull(decodeTuning("garbage"))
    }

    @Test
    fun `all instruments are accessible`() {
        val instruments = ALL_INSTRUMENTS
        assertEquals(2, instruments.size)
        assertEquals("4-String Banjo", instruments[0].name)
        assertEquals("5-String Banjo", instruments[1].name)
    }
}
