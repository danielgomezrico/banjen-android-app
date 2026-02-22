package com.makingiants.android.banjotuner

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TuningPresetTest {
    @Test
    fun `each preset has exactly 4 notes`() {
        TuningPreset.entries.forEach { preset ->
            assertEquals(4, preset.noteNames.size, "${preset.name} should have 4 notes")
        }
    }

    @Test
    fun `each preset has exactly 4 asset files`() {
        TuningPreset.entries.forEach { preset ->
            assertEquals(4, preset.assetFiles.size, "${preset.name} should have 4 asset files")
        }
    }

    @Test
    fun `all asset filenames are non-empty`() {
        TuningPreset.entries.forEach { preset ->
            preset.assetFiles.forEach { file ->
                assertTrue(file.isNotEmpty(), "${preset.name} has empty asset filename")
            }
        }
    }

    @Test
    fun `standard tuning is DGBD`() {
        val standard = TuningPreset.STANDARD
        assertEquals(listOf("D", "G", "B", "D"), standard.noteNames)
    }

    @Test
    fun `irish tuning is GDAE`() {
        val irish = TuningPreset.IRISH
        assertEquals(listOf("G", "D", "A", "E"), irish.noteNames)
    }

    @Test
    fun `plectrum tuning is CGBD`() {
        val plectrum = TuningPreset.PLECTRUM
        assertEquals(listOf("C", "G", "B", "D"), plectrum.noteNames)
    }

    @Test
    fun `chicago tuning is DGBE`() {
        val chicago = TuningPreset.CHICAGO
        assertEquals(listOf("D", "G", "B", "E"), chicago.noteNames)
    }

    @Test
    fun `buttonLabel returns correct format for each index`() {
        val standard = TuningPreset.STANDARD
        assertEquals("4 - D", standard.buttonLabel(0))
        assertEquals("3 - G", standard.buttonLabel(1))
        assertEquals("2 - B", standard.buttonLabel(2))
        assertEquals("1 - D", standard.buttonLabel(3))
    }

    @Test
    fun `buttonLabel works for irish tuning`() {
        val irish = TuningPreset.IRISH
        assertEquals("4 - G", irish.buttonLabel(0))
        assertEquals("3 - D", irish.buttonLabel(1))
        assertEquals("2 - A", irish.buttonLabel(2))
        assertEquals("1 - E", irish.buttonLabel(3))
    }

    @Test
    fun `standard tuning uses existing mp3 files`() {
        val standard = TuningPreset.STANDARD
        assertEquals(listOf("1.mp3", "2.mp3", "3.mp3", "4.mp3"), standard.assetFiles)
    }

    @Test
    fun `there are exactly 4 tuning presets`() {
        assertEquals(4, TuningPreset.entries.size)
    }
}
