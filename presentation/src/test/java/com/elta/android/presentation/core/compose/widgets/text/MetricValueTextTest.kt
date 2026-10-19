package com.elta.android.presentation.core.compose.widgets.text

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MetricValueTextTest {
    @Test fun `empty values and legacy dash variants share one placeholder`() {
        listOf("", " ", "—", "-", "— –", "--").forEach { value ->
            assertTrue(value, value.isMissingOrZeroMetric())
        }
    }

    @Test fun `zero metrics are hidden for all supported number formats and units`() {
        listOf("0", "0%", "0,0 ХЕ", "0.0 Ед.", "0 изм.", "0,0 г (0%)", "0,0 мин", "+0.0", "−0,0").forEach { value ->
            assertTrue(value, value.isMissingOrZeroMetric())
        }
    }

    @Test fun `nonzero values and midnight remain visible`() {
        listOf("100%", "0,1 ХЕ", "−0,1", "6,7%", "00:00", "12:30", "5,2 г (12%)").forEach { value ->
            assertFalse(value, value.isMissingOrZeroMetric())
        }
    }
}
