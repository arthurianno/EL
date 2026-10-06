package com.elta.android.presentation.features.main.records.ui.adapter.items

import com.elta.android.domain.features.diary.home.model.GlucoseLevelSettings
import org.junit.Assert.assertEquals
import org.junit.Test

class TimeInRangeTest {
    private val normalRange = GlucoseLevelSettings.defaultRange

    @Test
    fun `single measurement in range shows one hundred percent`() {
        assertEquals("100%", formatTimeInRange(listOf(7.0), normalRange))
    }

    @Test
    fun `single measurement outside range shows zero percent`() {
        assertEquals("0%", formatTimeInRange(listOf(2.1), normalRange))
    }

    @Test
    fun `no measurements shows dash`() {
        assertEquals("—", formatTimeInRange(emptyList(), normalRange))
    }
}
