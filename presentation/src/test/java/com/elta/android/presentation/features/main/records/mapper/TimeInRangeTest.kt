package com.elta.android.presentation.features.main.records.mapper

import com.elta.android.domain.features.diary.home.model.GlucoseLevelSettings
import org.junit.Assert.assertEquals
import org.junit.Test

class TimeInRangeTest {
    private val normalRange = GlucoseLevelSettings.defaultRange

    @Test
    fun `single measurement in range shows one hundred percent`() {
        val percent = timeInRangePercent(listOf(7.0), normalRange)

        assertEquals(100, percent)
        assertEquals("100%", formatTimeInRange(percent))
    }

    @Test
    fun `single measurement outside range shows zero percent`() {
        val percent = timeInRangePercent(listOf(2.1), normalRange)

        assertEquals(0, percent)
        assertEquals("0%", formatTimeInRange(percent))
    }

    @Test
    fun `no measurements shows dash`() {
        val percent = timeInRangePercent(emptyList(), normalRange)

        assertEquals(null, percent)
        assertEquals("—", formatTimeInRange(percent))
    }
}
