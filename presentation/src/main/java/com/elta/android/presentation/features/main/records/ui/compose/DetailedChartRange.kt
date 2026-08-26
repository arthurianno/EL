package com.elta.android.presentation.features.main.records.ui.compose

import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalDate
import org.threeten.bp.YearMonth
import org.threeten.bp.temporal.TemporalAdjusters
import java.util.Locale

internal enum class DetailedChartPeriod(
    val label: String,
    private val navigationStepDays: Long
) {
    DAY(label = "День", navigationStepDays = 1),
    WEEK(label = "Неделя", navigationStepDays = 7),
    TWO_WEEKS(label = "2 недели", navigationStepDays = 14),
    MONTH(label = "Месяц", navigationStepDays = 0);

    fun moveAnchor(anchor: LocalDate, direction: Int): LocalDate = when (this) {
        MONTH -> anchor.withDayOfMonth(1).plusMonths(direction.toLong())
        else -> anchor.plusDays(navigationStepDays * direction)
    }
}

internal data class DetailedChartRange(
    val month: YearMonth,
    val anchorDate: LocalDate,
    val period: DetailedChartPeriod
) {
    init {
        require(YearMonth.from(anchorDate) == month) {
            "The anchor date must belong to the selected month"
        }
    }

    val start: LocalDate
        get() = when (period) {
            DetailedChartPeriod.DAY -> anchorDate
            DetailedChartPeriod.WEEK,
            DetailedChartPeriod.TWO_WEEKS -> anchorDate.with(
                TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)
            )
            DetailedChartPeriod.MONTH -> month.atDay(1)
        }

    val end: LocalDate
        get() = when (period) {
            DetailedChartPeriod.DAY -> start
            DetailedChartPeriod.WEEK -> start.plusDays(6)
            DetailedChartPeriod.TWO_WEEKS -> start.plusDays(13)
            DetailedChartPeriod.MONTH -> month.atEndOfMonth()
        }
}

internal fun DetailedChartRange.withPeriod(period: DetailedChartPeriod): DetailedChartRange =
    copy(period = period)

internal fun DetailedChartRange.moveWithinMonth(direction: Int): DetailedChartRange? {
    require(direction == -1 || direction == 1)

    if (period == DetailedChartPeriod.MONTH) return null

    val candidate = period.moveAnchor(anchorDate, direction)
    return candidate.takeIf { YearMonth.from(it) == month }?.let { copy(anchorDate = it) }
}

internal fun DetailedChartRange.withMonth(month: YearMonth): DetailedChartRange {
    val day = anchorDate.dayOfMonth.coerceAtMost(month.lengthOfMonth())
    return copy(month = month, anchorDate = month.atDay(day))
}

internal fun List<DetailedGlucosePoint>.dailyAverages(): List<DetailedGlucosePoint> =
    asSequence()
        .filter { it.date != null }
        .groupBy { requireNotNull(it.date) }
        .toSortedMap()
        .map { (date, points) ->
            DetailedGlucosePoint(
                timeLabel = "12:00",
                value = points.map { it.value }.average().toFloat(),
                date = date,
                trendText = "среднее за день"
            )
        }
        .toList()

internal fun List<DetailedGlucosePoint>.hourlyAverages(): List<DetailedGlucosePoint> =
    asSequence()
        .filter { it.date != null }
        .groupBy { point -> requireNotNull(point.date) to point.hourOfDay() }
        .entries
        .sortedWith(compareBy({ it.key.first }, { it.key.second }))
        .map { (key, points) ->
            DetailedGlucosePoint(
                timeLabel = String.format(Locale.US, "%02d:30", key.second),
                value = points.map { it.value }.average().toFloat(),
                date = key.first,
                trendText = "среднее за час"
            )
        }

private fun DetailedGlucosePoint.hourOfDay(): Int =
    timeLabel.substringBefore(':').toIntOrNull()?.coerceIn(0, 23) ?: 0
