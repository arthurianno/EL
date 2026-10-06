package com.elta.android.domain.statistic

import com.elta.android.domain.features.statistics.model.glucoseManagementIndicatorPercent
import org.junit.Assert.assertEquals
import org.junit.Test

class GlucoseManagementIndicatorTest {
    @Test
    fun `gmi is calculated as a percentage from mmol per liter`() {
        assertEquals(6.155, glucoseManagementIndicatorPercent(6.6), 0.001)
        assertEquals(7.620, glucoseManagementIndicatorPercent(10.0), 0.001)
    }
}
