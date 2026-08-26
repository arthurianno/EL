package com.elta.android.presentation.features.main.records.ui.compose

import kotlin.test.assertEquals
import kotlin.test.assertNull
import org.junit.Test
import org.threeten.bp.LocalDate
import org.threeten.bp.YearMonth

class DetailedChartRangeTest {

    private val august = YearMonth.of(2026, 8)

    @Test
    fun week_startsOnMondayAndContainsSevenDays() {
        val range = DetailedChartRange(
            month = august,
            anchorDate = LocalDate.of(2026, 8, 26),
            period = DetailedChartPeriod.WEEK
        )

        assertEquals(LocalDate.of(2026, 8, 24), range.start)
        assertEquals(LocalDate.of(2026, 8, 30), range.end)
    }

    @Test
    fun twoWeeks_containFourteenCalendarDays() {
        val range = DetailedChartRange(
            month = august,
            anchorDate = LocalDate.of(2026, 8, 26),
            period = DetailedChartPeriod.TWO_WEEKS
        )

        assertEquals(LocalDate.of(2026, 8, 24), range.start)
        assertEquals(LocalDate.of(2026, 9, 6), range.end)
    }

    @Test
    fun month_coversTheWholeSelectedCalendarMonth() {
        val range = DetailedChartRange(
            month = YearMonth.of(2026, 2),
            anchorDate = LocalDate.of(2026, 2, 9),
            period = DetailedChartPeriod.MONTH
        )

        assertEquals(LocalDate.of(2026, 2, 1), range.start)
        assertEquals(LocalDate.of(2026, 2, 28), range.end)
    }

    @Test
    fun navigation_doesNotLeaveSelectedMonth() {
        val range = DetailedChartRange(
            month = august,
            anchorDate = LocalDate.of(2026, 8, 31),
            period = DetailedChartPeriod.DAY
        )

        assertNull(range.moveWithinMonth(1))
        assertEquals(LocalDate.of(2026, 8, 30), range.moveWithinMonth(-1)?.anchorDate)
    }

    @Test
    fun dailyAverages_usesEveryMeasurementFromTheSameDay() {
        val averages = listOf(
            DetailedGlucosePoint("08:00", 4f, LocalDate.of(2026, 8, 24)),
            DetailedGlucosePoint("12:00", 8f, LocalDate.of(2026, 8, 24)),
            DetailedGlucosePoint("08:00", 6f, LocalDate.of(2026, 8, 25))
        ).dailyAverages()

        assertEquals(2, averages.size)
        assertEquals(6f, averages[0].value)
        assertEquals(LocalDate.of(2026, 8, 24), averages[0].date)
        assertEquals(6f, averages[1].value)
    }

    @Test
    fun hourlyAverages_groupsMeasurementsIntoTheirHour() {
        val averages = listOf(
            DetailedGlucosePoint("08:01", 4f, LocalDate.of(2026, 8, 24)),
            DetailedGlucosePoint("08:59", 8f, LocalDate.of(2026, 8, 24)),
            DetailedGlucosePoint("09:10", 6f, LocalDate.of(2026, 8, 24))
        ).hourlyAverages()

        assertEquals(2, averages.size)
        assertEquals("08:30", averages[0].timeLabel)
        assertEquals(6f, averages[0].value)
        assertEquals("09:30", averages[1].timeLabel)
    }
}
