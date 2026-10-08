package com.elta.android.presentation.features.main.records.ui.compose

import com.elta.android.domain.features.diary.home.model.GlucoseLevelSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ContinuousStatisticsTest {

    @Test
    fun `uses configured glucose thresholds for visible points`() {
        val settings = GlucoseLevelSettings.fromNormalValues(5.0, 8.0)
        val points = listOf(
            DetailedGlucosePoint("08:00", 4f),
            DetailedGlucosePoint("09:00", 6f),
            DetailedGlucosePoint("10:00", 9f)
        )

        val statistics = ContinuousStatistics.from(points, settings)

        assertEquals(3, statistics.count)
        assertEquals(1, statistics.low)
        assertEquals(1, statistics.normal)
        assertEquals(1, statistics.high)
    }

    @Test
    fun `empty viewport has no derived metrics`() {
        val statistics = ContinuousStatistics.from(emptyList(), null)

        assertEquals(0, statistics.count)
        assertNull(statistics.average)
        assertNull(statistics.sd)
        assertNull(statistics.cv)
        assertNull(statistics.gmi)
    }
}
