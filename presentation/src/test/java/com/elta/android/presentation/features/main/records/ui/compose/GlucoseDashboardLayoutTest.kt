package com.elta.android.presentation.features.main.records.ui.compose

import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GlucoseDashboardLayoutTest {

    @Test
    fun `keeps the 60 40 working-space composition at the reference heights`() {
        val references = listOf(
            520 to Triple(146f, 333f, 142f),
            644 to Triple(175f, 398f, 169.6f),
            740 to Triple(199f, 458f, 208f),
            894 to Triple(199f, 536.4f, 269.6f)
        )

        references.forEach { (height, expected) ->
            val layout = calculateGlucoseDashboardLayout(375.dp, height.dp)

            assertEquals(expected.first, layout.ringSize.value, 0.01f)
            assertEquals(expected.second, layout.headerHeight.value, 0.01f)
            assertEquals(expected.third, layout.chartHeight.value, 0.01f)
        }
    }

    @Test
    fun `uses free header height to keep lower controls near the gradient edge`() {
        val regular = calculateGlucoseDashboardLayout(375.dp, 740.dp)
        val tall = calculateGlucoseDashboardLayout(375.dp, 894.dp)

        assertEquals(0f, regular.lowerControlsExtraOffset.value, 0.01f)
        assertEquals(78.4f, tall.lowerControlsExtraOffset.value, 0.01f)
    }

    @Test
    fun `scales the ring and glucose value together on a narrow screen`() {
        val regular = calculateGlucoseDashboardLayout(375.dp, 740.dp)
        val narrow = calculateGlucoseDashboardLayout(320.dp, 740.dp)

        assertEquals(199f, regular.ringSize.value, 0.01f)
        assertEquals(167.09f, narrow.ringSize.value, 0.01f)
        assertEquals(75f, glucoseValueFontSize(regular.ringSize), 0.01f)
        assertEquals(63.09f, glucoseValueFontSize(narrow.ringSize), 0.01f)
    }

    @Test
    fun `prioritizes the Figma lower inset when a wide phone has limited height`() {
        val layout = calculateGlucoseDashboardLayout(393.dp, 733.dp)
        val lowerMetricsEnd = layout.navigationTopSpacing +
            33.dp * layout.horizontalScale +
            layout.gaugeTopSpacing +
            glucoseGaugeBaseHeight(layout.ringSize, layout.ringTopOffset) +
            layout.lowerControlsExtraOffset

        assertTrue((layout.headerHeight - lowerMetricsEnd).value >= 15.99f)
        assertTrue(layout.ringSize.value < 207f)
    }

    @Test
    fun `shrinks long glucose values only when they do not fit the disc`() {
        val ringSize = 199.dp
        val discWidth = ringSize * (152f / 185f)

        assertEquals(75f, fittedGlucoseValueFontSize("4,1", ringSize, discWidth), 0.01f)
        assertEquals(59.96f, fittedGlucoseValueFontSize("10,0", ringSize, discWidth), 0.01f)
    }

    @Test
    fun `keeps short TIR values at the Figma size and fits 100 percent`() {
        assertEquals(38f, fittedTirValueFontSize("73%", 1f), 0.01f)
        assertEquals(34f, fittedTirValueFontSize("100%", 1f), 0.01f)
    }

    @Test
    fun `keeps the Figma gap between the outer ring and inner disc`() {
        assertEquals(19.87f, glucoseGaugeDiscTopInset(199.dp).value, 0.01f)
        assertEquals(14.57f, glucoseGaugeDiscTopInset(146.dp).value, 0.01f)
    }
}
