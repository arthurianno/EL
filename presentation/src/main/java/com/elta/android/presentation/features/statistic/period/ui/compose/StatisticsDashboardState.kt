package com.elta.android.presentation.features.statistic.period.ui.compose

import com.elta.android.domain.features.diary.events.model.glucoseValue
import com.elta.android.domain.features.statistics.model.StatisticByPeriodModel
import com.elta.android.presentation.features.statistic.period.ui.Period
import com.elta.android.presentation.utils.NumberFormatter
import org.threeten.bp.LocalDate
import kotlin.math.roundToInt
import kotlin.math.sqrt

/**
 * Presentation-only state for the statistics dashboard.
 *
 * It deliberately contains formatted strings and chart-ready values so composables remain
 * stateless and do not know about domain models, glucose settings or formatting rules.
 */
data class StatisticsDashboardUiState(
    val period: Period,
    val periodTitle: String,
    val average: String = "—",
    val unit: String = "ммоль/л",
    val lowPercent: Int = 0,
    val inRangePercent: Int = 0,
    val highPercent: Int = 0,
    val lowCount: Int = 0,
    val inRangeCount: Int = 0,
    val highCount: Int = 0,
    val minLabel: String = "—",
    val maxLabel: String = "—",
    val coefficientOfVariation: String = "—",
    val standardDeviation: String = "—",
    val gmi: String = "—",
    val nightHypoEpisodes: Int = 0,
    val hourlyRanges: List<HourlyRange> = emptyList(),
    val dailyRangeTitle: String = "",
    val distribution: List<Int> = List(DISTRIBUTION_BUCKETS_COUNT) { 0 },
    val dailyEpisodes: List<DailyEpisodeCount> = emptyList(),
    val comparison: ComparisonUiState? = null
)

data class HourlyRange(
    val date: LocalDate,
    val dayLabel: String,
    val statuses: List<HourlyRangeStatus>
)

data class DailyEpisodeCount(
    val date: LocalDate,
    val low: Int,
    val high: Int
)

data class ComparisonUiState(
    val currentTir: Int,
    val previousTir: Int,
    val currentAverage: String,
    val previousAverage: String,
    val currentAverageValue: Double,
    val previousAverageValue: Double,
    val normalStart: Double,
    val normalEnd: Double,
    val currentHypoEpisodes: Int,
    val previousHypoEpisodes: Int,
    val axisDates: List<LocalDate>,
    val currentSeries: List<DailyGlucosePoint>,
    val previousSeries: List<DailyGlucosePoint>
)

data class DailyGlucosePoint(
    val date: LocalDate,
    val value: Double?,
    /** Position on the complete selected-period timeline, from 0 to 1. */
    val position: Float = 0f
)

enum class HourlyRangeStatus {
    LOW,
    IN_RANGE,
    HIGH,
    NO_DATA
}

/** Converts domain data once at the presentation boundary. */
fun StatisticByPeriodModel?.toStatisticsDashboardUiState(
    selectedPeriod: Period,
    previous: StatisticByPeriodModel? = null
): StatisticsDashboardUiState {
    val model = this ?: return StatisticsDashboardUiState(
        period = selectedPeriod,
        periodTitle = selectedPeriod.displayName
    )
    val glucose = model.glucose
    val glucoseEvents = model.allDays.toSortedMap().flatMap { it.value.glucose.dailyGlucoseModel?.glucoseEvents.orEmpty() }
    val glucoseValues = glucoseEvents.map { it.glucoseValue(glucose.glucoseFormat) }
    val standardDeviation = glucoseValues.standardDeviation(glucose.averageLevel)

    val allDates = model.period.dates()
    val hourlyRanges = allDates.map { date ->
        val glucoseEventsForDay = model.allDays[date]
            ?.glucose
            ?.dailyGlucoseModel
            ?.glucoseEvents
            .orEmpty()
        HourlyRange(
            date = date,
            dayLabel = date.toShortWeekday(),
            statuses = glucoseEventsForDay
                .groupBy { it.additionTime.hour }
                .toHourlyStatuses(glucose)
        )
    }
    val dailyEpisodes = allDates.map { date ->
        val values = model.allDays[date]
            ?.glucose
            ?.dailyGlucoseModel
            ?.glucoseEvents
            .orEmpty()
            .map { it.glucoseValue(glucose.glucoseFormat) }
        DailyEpisodeCount(
            date = date,
            low = values.count { it in glucose.settings.low },
            high = values.count { it in glucose.settings.high }
        )
    }

    return StatisticsDashboardUiState(
        period = selectedPeriod,
        periodTitle = model.period.toDisplayTitle(),
        average = NumberFormatter.format(glucose.averageLevel),
        lowPercent = glucose.eventsLowPercent,
        inRangePercent = glucose.eventsNormalPercent,
        highPercent = glucose.eventsHighPercent,
        lowCount = glucose.eventsLowCount,
        inRangeCount = glucose.eventsNormalCount,
        highCount = glucose.eventsHighCount,
        minLabel = NumberFormatter.format(glucose.minLevel),
        maxLabel = NumberFormatter.format(glucose.maxLevel),
        coefficientOfVariation = glucose.averageLevel.coefficientOfVariation(standardDeviation),
        standardDeviation = NumberFormatter.format(standardDeviation),
        gmi = NumberFormatter.format(GMI_BASELINE + glucose.averageLevel * GMI_AVERAGE_MULTIPLIER),
        nightHypoEpisodes = glucoseEvents.count { event ->
            event.additionTime.hour in NIGHT_HYPO_HOURS &&
                event.glucoseValue(glucose.glucoseFormat) in glucose.settings.low
        },
        hourlyRanges = hourlyRanges,
        dailyRangeTitle = hourlyRanges.takeLast(DAYS_IN_HEATMAP).toDateRangeTitle(),
        distribution = glucoseEvents.toDistributionBuckets(glucose.glucoseFormat),
        dailyEpisodes = dailyEpisodes,
        comparison = previous
            ?.takeIf { it.glucose.eventsCount > 0 }
            ?.toComparisonUiState(model)
    )
}

private fun List<Double>.standardDeviation(average: Double): Double =
    takeIf { isNotEmpty() }
        ?.map { value -> (value - average) * (value - average) }
        ?.average()
        ?.let(::sqrt)
        ?: 0.0

private fun Double.coefficientOfVariation(standardDeviation: Double): String =
    if (this <= 0.0) "—" else "${(standardDeviation / this * 100).roundToInt()}%"

private fun Map<Int, List<com.elta.android.domain.features.diary.events.model.EventV2>>.toHourlyStatuses(
    glucose: com.elta.android.domain.features.statistics.model.GlucoseStatisticModel
): List<HourlyRangeStatus> = HOURS.map { hour ->
    this[hour]?.lastOrNull()?.glucoseValue(glucose.glucoseFormat)?.let { value ->
        when {
            value in glucose.settings.low -> HourlyRangeStatus.LOW
            value in glucose.settings.high -> HourlyRangeStatus.HIGH
            else -> HourlyRangeStatus.IN_RANGE
        }
    } ?: HourlyRangeStatus.NO_DATA
}

private fun List<com.elta.android.domain.features.diary.events.model.EventV2>.toDistributionBuckets(
    format: com.elta.android.domain.features.user.model.GlucoseFormat
): List<Int> {
    val buckets = MutableList(DISTRIBUTION_BUCKETS_COUNT) { 0 }
    forEach { event ->
        when (event.glucoseValue(format)) {
            in Double.NEGATIVE_INFINITY..<3.0 -> buckets[0]++
            in 3.0..<4.0 -> buckets[1]++
            in 4.0..<7.0 -> buckets[2]++
            in 7.0..<10.0 -> buckets[3]++
            in 10.0..<12.0 -> buckets[4]++
            else -> buckets[5]++
        }
    }
    return buckets
}

private fun StatisticByPeriodModel.toComparisonUiState(
    current: StatisticByPeriodModel
): ComparisonUiState = ComparisonUiState(
    currentTir = current.glucose.eventsNormalPercent,
    previousTir = glucose.eventsNormalPercent,
    currentAverage = NumberFormatter.format(current.glucose.averageLevel),
    previousAverage = NumberFormatter.format(glucose.averageLevel),
    currentAverageValue = current.glucose.averageLevel,
    previousAverageValue = glucose.averageLevel,
    normalStart = current.glucose.settings.normal.start,
    normalEnd = current.glucose.settings.normal.end,
    currentHypoEpisodes = current.glucose.eventsLowCount,
    previousHypoEpisodes = glucose.eventsLowCount,
    axisDates = current.period.dates(),
    currentSeries = current.toDailyGlucosePoints(),
    previousSeries = toDailyGlucosePoints()
)

private fun StatisticByPeriodModel.toDailyGlucosePoints(): List<DailyGlucosePoint> {
    val dates = period.dates()
    val daysCount = dates.size.coerceAtLeast(1)
    return dates.mapIndexed { dayIndex, date ->
        val values = allDays[date]
            ?.glucose
            ?.dailyGlucoseModel
            ?.glucoseEvents
            .orEmpty()
            .map { event -> event.glucoseValue(glucose.glucoseFormat) }
        DailyGlucosePoint(
            date = date,
            value = values.takeIf { it.isNotEmpty() }?.average(),
            position = (dayIndex + 0.5f) / daysCount
        )
    }
}

private fun com.elta.android.domain.features.statistics.model.StatisticPeriod.dates(): List<LocalDate> {
    val startDate = start.toLocalDate()
    val endDate = end.toLocalDate()
    return generateSequence(startDate) { date ->
        date.plusDays(1).takeIf { it <= endDate }
    }.toList()
}

private fun List<HourlyRange>.toDateRangeTitle(): String {
    val week = takeLast(DAYS_IN_HEATMAP)
    val first = week.firstOrNull()?.date ?: return ""
    val last = week.lastOrNull()?.date ?: return ""
    return "${first.dayOfMonth} ${first.monthValue.toRussianMonthName()} – ${last.dayOfMonth} ${last.monthValue.toRussianMonthName()}"
}

private fun LocalDate.toShortWeekday(): String = when (dayOfWeek.value) {
    1 -> "ПН"
    2 -> "ВТ"
    3 -> "СР"
    4 -> "ЧТ"
    5 -> "ПТ"
    6 -> "СБ"
    else -> "ВС"
}

private fun com.elta.android.domain.features.statistics.model.StatisticPeriod.toDisplayTitle(): String =
    "${start.dayOfMonth} ${start.monthValue.toRussianMonthName()} – ${end.dayOfMonth} ${end.monthValue.toRussianMonthName()}"

internal val Period.displayName: String
    get() = when (this) {
        Period.SEVEN -> "Последние 7 дней"
        Period.FOURTEEN -> "Последние 14 дней"
        Period.THIRTY -> "Последние 30 дней"
        Period.NINETY -> "Последние 90 дней"
    }

private fun Int.toRussianMonthName() = RUSSIAN_MONTH_NAMES[this - 1]

private const val DISTRIBUTION_BUCKETS_COUNT = 6
private const val DAYS_IN_HEATMAP = 7
private const val GMI_BASELINE = 3.31
private const val GMI_AVERAGE_MULTIPLIER = 0.431
private val NIGHT_HYPO_HOURS = 2..4
private val HOURS = 0..23
private val RUSSIAN_MONTH_NAMES = arrayOf(
    "января", "февраля", "марта", "апреля", "мая", "июня",
    "июля", "августа", "сентября", "октября", "ноября", "декабря"
)
