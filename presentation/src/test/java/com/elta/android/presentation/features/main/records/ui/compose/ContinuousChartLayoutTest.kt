package com.elta.android.presentation.features.main.records.ui.compose

import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ContinuousChartLayoutTest {
    @Test
    fun `landscape phone fits chart and full summary without scrolling`() {
        val layout = continuousChartLayout(
            screenWidth = 820.dp,
            screenHeight = 360.dp,
            sideMargin = 32.dp,
            topPadding = 24.dp,
            bottomPadding = 24.dp,
            fontScale = 1f
        )

        assertFalse(layout.compactSummary)
        assertFalse(layout.denseMetrics)
        assertEquals(74.dp, layout.summaryHeight)
        assertEquals(198.dp, layout.chartCardHeight)
        assertFalse(layout.scrollContent)
        assertEquals(360.dp, 24.dp + 32.dp + 4.dp + layout.chartCardHeight +
            4.dp + layout.summaryHeight + 24.dp)
    }

    @Test
    fun `taller landscape screen gives spare height to chart`() {
        val layout = continuousChartLayout(
            screenWidth = 1000.dp,
            screenHeight = 500.dp,
            sideMargin = 32.dp,
            topPadding = 24.dp,
            bottomPadding = 24.dp,
            fontScale = 1f
        )

        assertTrue(layout.inlineLegend)
        assertFalse(layout.denseMetrics)
        assertEquals(324.dp, layout.chartCardHeight)
        assertFalse(layout.scrollContent)
    }

    @Test
    fun `narrow screen and large text retain readable cards`() {
        val narrow = continuousChartLayout(600.dp, 360.dp, 32.dp, 24.dp, 24.dp, 1f)
        val largeText = continuousChartLayout(820.dp, 430.dp, 32.dp, 24.dp, 24.dp, 1.5f)

        assertTrue(narrow.compactSummary)
        assertTrue(narrow.scrollContent)
        assertTrue(largeText.compactSummary)
        assertTrue(largeText.scrollContent)
        assertTrue(largeText.chartCardHeight >= 240.dp)
    }
}
