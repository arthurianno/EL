package com.elta.android.domain.features.statistics.interactor

import com.elta.android.domain.features.diary.events.model.Event
import com.elta.android.domain.features.diary.events.model.EventType
import com.elta.android.domain.features.diary.home.model.GlucoseLevelSettings
import com.elta.android.domain.features.statistics.model.ActivityStatisticModel
import com.elta.android.domain.features.statistics.model.BreadStatisticModelByPeriod
import com.elta.android.domain.features.statistics.model.GlucoseStatisticModel
import com.elta.android.domain.features.statistics.model.InsulinStatisticModelByPeriod
import com.elta.android.domain.features.statistics.model.StatisticByPeriodModel
import com.elta.android.domain.features.statistics.model.StatisticPeriod
import com.elta.android.domain.features.statistics.model.daily.DailyStatisticModel
import timber.log.Timber
import java.util.Date

fun buildStatisticModel(
    period: StatisticPeriod,
    events: List<Event>,
    settings: GlucoseLevelSettings
): StatisticByPeriodModel {
    val eventsContainer = events.toEventsContainer()

    val eventsByType = eventsContainer.byType
    val eventsByTypePerDay = eventsContainer.byTypePerDay

    val statisticByDay = mutableMapOf<Date, DailyStatisticModel>()

    var dayWithMaxLevel: DailyStatisticModel? = null
    var dayWithMinLevel: DailyStatisticModel? = null

    eventsByTypePerDay.entries.forEach { entry ->
        val day = entry.key
        val eventsPerDay = entry.value
        val dayStatistic = buildDailyStatisticModel(day, eventsPerDay, settings)

        dayWithMaxLevel = dayWithMaxLevel?.let { dayStatistic.checkMax(it) } ?: dayStatistic
        dayWithMinLevel = dayWithMinLevel?.let { dayStatistic.checkMin(it) } ?: dayStatistic

        statisticByDay[day] = dayStatistic
    }

    // 0(N)
    return StatisticByPeriodModel(
        period = period,
        dayWithMaxLevel = dayWithMaxLevel,
        dayWithMinLevel = dayWithMinLevel,
        allDays = statisticByDay,
        glucose = buildGlucoseStatisticModel(eventsByType[EventType.GLUCOSE], settings),
        insulin = buildInsulinStatisticModelByPeriod(eventsByType[EventType.INSULIN]),
        bread = buildBreadStatisticModelByPeriod(eventsByType[EventType.BREAD]),
        activity = buildActivityStatisticModel(eventsByType[EventType.ACTIVITY])
    )
}

@Suppress("LongMethod")
fun buildGlucoseStatisticModel(
    glucoseEventsPerPeriod: List<Event>?,
    settings: GlucoseLevelSettings
): GlucoseStatisticModel {
    val count = glucoseEventsPerPeriod?.size ?: 0
    var totalLevel = 0.0

    var maxLevel = 0.0
    var minLevel = Double.MAX_VALUE

    var maxHighLevel = 0.0
    var minHighLevel = Double.MAX_VALUE

    var maxNormalLevel = 0.0
    var minNormalLevel = Double.MAX_VALUE

    var maxLowLevel = 0.0
    var minLowLevel = Double.MAX_VALUE

    var eventsHighCount = 0
    var eventsNormalCount = 0
    var eventsLowCount = 0

    glucoseEventsPerPeriod?.forEach { event ->
        event.value?.let { value ->
            totalLevel += value

            maxLevel = value.checkMax(maxLevel)
            minLevel = value.checkMin(minLevel)

            when (value) {
                in settings.high -> {
                    eventsHighCount++
                    maxHighLevel = value.checkMax(maxHighLevel)
                    minHighLevel = value.checkMin(minHighLevel)
                }
                in settings.normal -> {
                    eventsNormalCount++
                    maxNormalLevel = value.checkMax(maxNormalLevel)
                    minNormalLevel = value.checkMin(minNormalLevel)
                }
                in settings.low -> {
                    eventsLowCount++
                    maxLowLevel = value.checkMax(maxLowLevel)
                    minLowLevel = value.checkMin(minLowLevel)
                }
                else -> Timber.w("$value doesn't enter in any diapason")
            }
        }
    }

    val eventsHighPercent = eventsHighCount.percent(count)
    val eventsNormalPercent = eventsNormalCount.percent(count)
    val eventsLowPercent = if (eventsLowCount != 0) 100 - eventsHighPercent - eventsNormalPercent else 0

    return GlucoseStatisticModel(
        settings = settings,

        averageLevel = totalLevel.average(count),

        maxLevel = maxLevel,
        minLevel = minLevel,

        maxHighLevel = maxHighLevel,
        minHighLevel = minHighLevel,

        maxNormalLevel = maxNormalLevel,
        minNormalLevel = minNormalLevel,

        maxLowLevel = maxLowLevel,
        minLowLevel = minLowLevel,

        eventsCount = count,
        eventsHighCount = eventsHighCount,
        eventsNormalCount = eventsNormalCount,
        eventsLowCount = eventsLowCount,

        eventsHighPercent = eventsHighPercent,
        eventsNormalPercent = eventsNormalPercent,
        eventsLowPercent = eventsLowPercent,

        glucoseEvents = glucoseEventsPerPeriod
    )
}

fun buildInsulinStatisticModelByPeriod(insulinEventsPerPeriod: List<Event>?): InsulinStatisticModelByPeriod {
    var totalBolusLevel = 0.0
    var totalBasalLevel = 0.0
    var totalLevel = 0.0

    var bolusCount = 0
    var basalCount = 0
    var count = 0

    insulinEventsPerPeriod?.forEach { event ->
        val value = event.value
        if (value != null && value != 0.0) {
            if (event.isBolusInsulin()) {
                totalBolusLevel += value
                bolusCount++
            }

            if (event.isBasalInsulin()) {
                totalBasalLevel += value
                basalCount++
            }

            if (event.isNotMixedInsulin()) {
                totalLevel += value
                count++
            }
        }
    }

    return InsulinStatisticModelByPeriod(
        averageBolusLevel = totalBolusLevel.average(bolusCount),
        averageBasalLevel = totalBasalLevel.average(basalCount),
        averageLevel = totalLevel.average(count)
    )
}

fun buildBreadStatisticModelByPeriod(breadEventsPerPeriod: List<Event>?): BreadStatisticModelByPeriod {
    var totalLevel = 0.0
    var count = 0

    breadEventsPerPeriod?.forEach { event ->
        val value = event.value
        if (value != null && value != 0.0) {
            totalLevel += value
            count++
        }
    }

    return BreadStatisticModelByPeriod(
        averageLevel = totalLevel.average(count)
    )
}

fun buildActivityStatisticModel(activityEventsPerPeriod: List<Event>?): ActivityStatisticModel {
    var totalDuration = 0L
    var count = 0

    activityEventsPerPeriod?.forEach { event ->
        event.duration?.let { duration ->
            totalDuration += duration
            count++
        }
    }

    return ActivityStatisticModel(
        eventsCount = count,
        averageDuration = totalDuration.average(count)
    )
}