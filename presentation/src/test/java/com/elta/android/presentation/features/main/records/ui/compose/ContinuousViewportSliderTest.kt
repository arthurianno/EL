package com.elta.android.presentation.features.main.records.ui.compose

import org.junit.Assert.assertEquals
import org.junit.Test

class ContinuousViewportSliderTest {
    @Test
    fun `drag reaches both ends of history`() {
        val total = 30L * 24 * 60
        val day = 24L * 60

        assertEquals(0L, sliderViewportStart(0f, 300f, day, total, 24f))
        assertEquals(total - day, sliderViewportStart(300f, 300f, day, total, 24f))
    }

    @Test
    fun `drag works when the track is narrower than the touch target`() {
        val total = 30L * 24 * 60
        val day = 24L * 60

        assertEquals(total - day, sliderViewportStart(20f, 20f, day, total, 24f))
    }
}
