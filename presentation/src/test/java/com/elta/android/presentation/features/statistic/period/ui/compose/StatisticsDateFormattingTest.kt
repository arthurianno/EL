package com.elta.android.presentation.features.statistic.period.ui.compose

import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Test
import org.threeten.bp.LocalDate

class StatisticsDateFormattingTest {
    @Test
    fun `date range follows selected locale and compact style`() {
        val start = LocalDate.of(2026, 7, 15)
        val end = LocalDate.of(2026, 7, 28)

        assertEquals("15 июля – 28 июля", formatDateRange(start, end, Locale.forLanguageTag("ru")))
        assertEquals("15 июл. – 28 июл.", formatDateRange(start, end, Locale.forLanguageTag("ru"), compact = true))
        assertEquals("15 July – 28 July", formatDateRange(start, end, Locale.ENGLISH))
    }
}
