package com.elta.android.presentation.features.statistic.period.ui.compose

import com.elta.android.domain.features.diary.events.model.glucoseValue
import com.elta.android.domain.features.statistics.model.StatisticByPeriodModel
import com.elta.android.presentation.features.statistic.period.ui.Period
import com.elta.android.presentation.utils.NumberFormatter
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
    val distribution: List<Int> = List(DISTRIBUTION_BUCKETS_COUNT) { 0 }
)

data class HourlyRange(
    val dayLabel: String,
    val statuses: List<HourlyRangeStatus>
)

enum class HourlyRangeStatus {
    LOW,
    IN_RANGE,
    HIGH,
    NO_DATA
}

/** Converts domain data once at the presentation boundary. */
fun StatisticByPeriodModel?.toStatisticsDashboardUiState(
    selectedPeriod: Period
): StatisticsDashboardUiState {
    val model = this ?: return StatisticsDashboardUiState(
        period = selectedPeriod,
        periodTitle = selectedPeriod.displayName
    )
    val glucose = model.glucose
    val glucoseEvents = model.allDays.toSortedMap().flatMap { it.value.glucose.dailyGlucoseModel?.glucoseEvents.orEmpty() }
    val glucoseValues = glucoseEvents.map { it.glucoseValue(glucose.glucoseFormat) }
    val standardDeviation = glucoseValues.standardDeviation(glucose.averageLevel)

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
        hourlyRanges = model.allDays.toSortedMap().map { (date, day) ->
            HourlyRange(
                dayLabel = date.dayOfMonth.toString(),
                statuses = day.glucose.dailyGlucoseModel
                    ?.glucoseEvents
                    .orEmpty()
                    .groupBy { it.additionTime.hour }
                    .toHourlyStatuses(glucose)
            )
        },
        distribution = glucose.toDistributionBuckets()
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

private fun com.elta.android.domain.features.statistics.model.GlucoseStatisticModel.toDistributionBuckets() = listOf(
    eventsLowPercent / 2,
    eventsLowPercent,
    eventsNormalPercent,
    (eventsNormalPercent * NORMAL_SECOND_BUCKET_RATIO).roundToInt(),
    eventsHighPercent,
    eventsHighPercent / 2
)

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
private const val NORMAL_SECOND_BUCKET_RATIO = 0.85f
private const val GMI_BASELINE = 3.31
private const val GMI_AVERAGE_MULTIPLIER = 0.431
private val NIGHT_HYPO_HOURS = 2..4
private val HOURS = 0..23
private val RUSSIAN_MONTH_NAMES = arrayOf(
    "января", "февраля", "марта", "апреля", "мая", "июня",
    "июля", "августа", "сентября", "октября", "ноября", "декабря"
)
