package com.elta.android.presentation.features.main.records.ui.compose

import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Test

class GlucoseDashboardLayoutTest {

    @Test
    fun `matches the four Figma height reference points`() {
        val references = listOf(
            592 to Triple(146f, 312f, 142f),
            716 to Triple(175f, 381f, 164f),
            812 to Triple(199f, 440f, 201f),
            966 to Triple(199f, 506f, 272f)
        )

        references.forEach { (height, expected) ->
            val layout = calculateGlucoseDashboardLayout(375.dp, height.dp)

            assertEquals(expected.first, layout.ringSize.value, 0.01f)
            assertEquals(expected.second, layout.headerHeight.value, 0.01f)
            assertEquals(expected.third, layout.chartHeight.value, 0.01f)
        }
    }

    @Test
    fun `scales the ring and glucose value together on a narrow screen`() {
        val regular = calculateGlucoseDashboardLayout(375.dp, 812.dp)
        val narrow = calculateGlucoseDashboardLayout(320.dp, 812.dp)

        assertEquals(199f, regular.ringSize.value, 0.01f)
        assertEquals(167.09f, narrow.ringSize.value, 0.01f)
        assertEquals(75f, glucoseValueFontSize(regular.ringSize), 0.01f)
        assertEquals(63.09f, glucoseValueFontSize(narrow.ringSize), 0.01f)
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
