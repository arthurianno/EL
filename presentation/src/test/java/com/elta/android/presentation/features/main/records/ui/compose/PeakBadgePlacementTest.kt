package com.elta.android.presentation.features.main.records.ui.compose

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PeakBadgePlacementTest {

    @Test
    fun `places extrema inside graph when points are at vertical edges`() {
        val chartSize = IntSize(300, 160)
        val badgeSize = IntSize(80, 24)

        val placements = calculatePeakBadgePlacements(
            chartSize = chartSize,
            minPoint = Offset(240f, 154f),
            maxPoint = Offset(60f, 6f),
            minBadgeSize = badgeSize,
            maxBadgeSize = badgeSize,
            edgePx = 4,
            gapPx = 8
        )

        assertNotNull(placements.min)
        assertNotNull(placements.max)
        assertEquals(122, placements.min!!.y)
        assertEquals(14, placements.max!!.y)
        assertInside(placements.min!!, badgeSize, chartSize)
        assertInside(placements.max!!, badgeSize, chartSize)
    }

    @Test
    fun `clamps a badge at the horizontal edge of the graph`() {
        val chartSize = IntSize(300, 160)
        val badgeSize = IntSize(80, 24)

        val placements = calculatePeakBadgePlacements(
            chartSize = chartSize,
            minPoint = Offset(298f, 154f),
            maxPoint = null,
            minBadgeSize = badgeSize,
            maxBadgeSize = IntSize.Zero,
            edgePx = 4,
            gapPx = 8
        )

        assertEquals(216, placements.min!!.x)
        assertInside(placements.min!!, badgeSize, chartSize)
    }

    private fun assertInside(placement: PeakBadgePlacement, badgeSize: IntSize, chartSize: IntSize) {
        assertTrue(placement.x >= 4)
        assertTrue(placement.y >= 4)
        assertTrue(placement.x + badgeSize.width <= chartSize.width - 4)
        assertTrue(placement.y + badgeSize.height <= chartSize.height - 4)
    }
}
