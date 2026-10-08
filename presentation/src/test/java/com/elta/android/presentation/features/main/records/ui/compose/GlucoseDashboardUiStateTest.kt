package com.elta.android.presentation.features.main.records.ui.compose

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GlucoseDashboardUiStateTest {
    @Test
    fun `measurement availability uses numeric data instead of display text`() {
        assertFalse(GlucoseDashboardUiState(glucoseValue = "6,7").hasMeasurements)
        assertTrue(
            GlucoseDashboardUiState(
                glucoseValue = "—",
                glucoseValueNumber = 6.7
            ).hasMeasurements
        )
    }
}
