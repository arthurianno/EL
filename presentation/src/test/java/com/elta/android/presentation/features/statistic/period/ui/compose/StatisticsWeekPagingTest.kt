package com.elta.android.presentation.features.statistic.period.ui.compose

import org.junit.Assert.assertEquals
import org.junit.Test
import org.threeten.bp.LocalDate

class StatisticsWeekPagingTest {
    @Test
    fun `weekly windows cover complete period without skipping days`() {
        val days = (1..16).map { day ->
            HourlyRange(LocalDate.of(2026, 10, day), "", emptyList())
        }

        assertEquals(2, days.lastWeekOffset)
        assertEquals((10..16).toList(), days.weekFromLatest(0).map { it.date.dayOfMonth })
        assertEquals((3..9).toList(), days.weekFromLatest(1).map { it.date.dayOfMonth })
        assertEquals((1..2).toList(), days.weekFromLatest(2).map { it.date.dayOfMonth })
    }

    @Test
    fun `single week has no previous page`() {
        val days = listOf(HourlyRange(LocalDate.of(2026, 10, 6), "", emptyList()))

        assertEquals(0, days.lastWeekOffset)
        assertEquals(days, days.weekFromLatest(0))
        assertEquals(0, emptyList<HourlyRange>().lastWeekOffset)
    }
}
