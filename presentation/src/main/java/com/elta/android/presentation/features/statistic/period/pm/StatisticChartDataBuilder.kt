package com.elta.android.presentation.features.statistic.period.pm

import com.elta.android.common.utils.atStartOfDay
import com.elta.android.common.utils.toStringWithFormat
import com.elta.android.domain.features.statistics.model.GlucoseStatisticModel
import com.elta.android.domain.features.statistics.model.Periods
import com.elta.android.domain.features.statistics.model.StatisticByPeriodModel
import com.elta.android.domain.features.statistics.model.StatisticPeriod
import com.elta.android.domain.features.statistics.model.daily.DailyStatisticModel
import com.elta.android.presentation.utils.daysTo
import com.elta.android.presentation.widgets.charts.statistics.models.DateModel
import com.elta.android.presentation.widgets.charts.statistics.models.StatisticsChartDataModel
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import java.util.TreeMap

private const val VALUES_COUNT = 5
private const val DEFAULT_MIN_LEVEL = 0.6
private const val DEFAULT_MAX_LEVEL = 35.0
private const val STEP_SEVEN_DAYS = 7
private const val STEP_SIX_DAYS = 6
private const val DATE_FORMAT = "dd.MM"
private val stubDate = DateModel(null, null, false, true)

fun StatisticByPeriodModel.toChartModel(selectedDate: LocalDate?): StatisticsChartDataModel {
    var minLevel = dayWithMinLevel?.glucose.minLevel()
    var maxLevel = dayWithMaxLevel?.glucose.maxLevel()

    if (minLevel == maxLevel) {
        val settings = glucose.settings
        val value = minLevel
        when (value) {
            in settings.normal -> {
                minLevel = settings.normal.start
                maxLevel = settings.normal.end
            }
            in settings.low -> {
                minLevel = value
                maxLevel = settings.normal.end
            }
            in settings.high -> {
                minLevel = settings.normal.start
                maxLevel = value
            }
        }
    }

    val values = buildValues(minLevel, maxLevel)
    val modelsMap = TreeMap<DateModel, GlucoseStatisticModel?>()
    modelsMap[stubDate] = null
    var date = period.start
    while (!date.isAfter(period.end)) {
        modelsMap[date.toDateModel(period)] = allDays[date.toLocalDate()].glucose()
        date = date.plusDays(1)
    }
    return StatisticsChartDataModel(
        maxValue = maxLevel,
        minValue = minLevel,
        values = values,
        statisticsPerDate = modelsMap,
        selectedDate = selectedDate,
        period = period
    )
}

private fun buildValues(min: Double, max: Double): List<Double> {
    val step = (max - min) / VALUES_COUNT
    val resultList = arrayListOf<Double>()
    for (i in 0..VALUES_COUNT) {
        resultList.add(min + step * i)
    }
    resultList.sort()
    return resultList
}

private fun LocalDateTime.toDateModel(period: StatisticPeriod) =
    DateModel(
        date = this,
        formattedDate = this.toStringWithFormat(DATE_FORMAT),
        needDrawDateTile = when {
            period is Periods.SevenDays -> true
            this == period.start || this == period.end.atStartOfDay() -> true
            (period.start daysTo this) % period.datesStep() == 0L -> true
            else -> false
        }
    )

private fun StatisticPeriod.datesStep() = when {
    this is Periods.FourteenDays -> STEP_SEVEN_DAYS
    else -> STEP_SIX_DAYS
}

private fun GlucoseStatisticModel?.minLevel(): Double {
    this?.let {
        return if (it.eventsCount > 0) minLevel else DEFAULT_MIN_LEVEL
    }
    return DEFAULT_MIN_LEVEL
}

private fun GlucoseStatisticModel?.maxLevel(): Double {
    this?.let {
        return if (it.eventsCount > 0) maxLevel else DEFAULT_MAX_LEVEL
    }
    return DEFAULT_MAX_LEVEL
}

private fun DailyStatisticModel?.glucose(): GlucoseStatisticModel? =
    when {
        this != null && glucose.eventsCount > 0 -> glucose
        else -> null
    }