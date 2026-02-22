package com.makingiants.android.banjotuner

import org.junit.Test
import kotlin.test.assertEquals

class TunerWidgetTest {
    @Test
    fun `string labels has 4 entries`() {
        assertEquals(4, TunerWidget.STRING_LABELS.size)
    }

    @Test
    fun `string labels match standard tuning DGBD`() {
        assertEquals("4 - D", TunerWidget.STRING_LABELS[0])
        assertEquals("3 - G", TunerWidget.STRING_LABELS[1])
        assertEquals("2 - B", TunerWidget.STRING_LABELS[2])
        assertEquals("1 - D", TunerWidget.STRING_LABELS[3])
    }

    @Test
    fun `STRING_INDEX_KEY name is string_index`() {
        assertEquals("string_index", TunerWidget.STRING_INDEX_KEY.name)
    }
}
