package com.elta.android.domain.features.statistics.interactor

import com.elta.android.domain.features.diary.events.model.Event
import com.elta.android.domain.features.diary.events.model.EventType
import com.elta.android.domain.features.diary.home.interactor.buildDailyGlucoseModel
import com.elta.android.domain.features.diary.home.model.GlucoseLevelSettings
import com.elta.android.domain.features.statistics.model.ActivityStatisticModel
import com.elta.android.domain.features.statistics.model.BreadStatisticModelByPeriod
import com.elta.android.domain.features.statistics.model.GlucoseStatisticModel
import com.elta.android.domain.features.statistics.model.InsulinStatisticModelByPeriod
import com.elta.android.domain.features.statistics.model.StatisticByPeriodModel
import com.elta.android.domain.features.statistics.model.StatisticPeriod
import com.elta.android.domain.features.statistics.model.daily.DailyStatisticModel
import com.elta.android.domain.features.user.model.GlucoseFormat
import org.threeten.bp.LocalDate
import timber.log.Timber

fun buildStatisticModel(
    period: StatisticPeriod,
    events: List<Event>,
    settings: GlucoseLevelSettings,
    glucoseFormat: GlucoseFormat
): StatisticByPeriodModel {
    val eventsContainer = events.toEventsContainer()

    val eventsByType = eventsContainer.byType
    val eventsByTypePerDay = eventsContainer.byTypePerDay

    val statisticByDay = mutableMapOf<LocalDate, DailyStatisticModel>()

    var dayWithMaxLevel: DailyStatisticModel? = null
    var dayWithMinLevel: DailyStatisticModel? = null

    eventsByTypePerDay.entries.forEach { entry ->
        val day = entry.key
        val eventsPerDay = entry.value
        val dayStatistic = buildDailyStatisticModel(
            date = day,
            eventsPerDay = eventsPerDay,
            settings = settings,
            glucoseFormat = glucoseFormat
        )

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
        glucose = buildGlucoseStatisticModel(
            eventsByType[EventType.GLUCOSE],
            settings,
            glucoseFormat
        ),
        insulin = buildInsulinStatisticModelByPeriod(eventsByType[EventType.INSULIN]),
        bread = buildBreadStatisticModelByPeriod(eventsByType[EventType.BREAD]),
        activity = buildActivityStatisticModel(eventsByType[EventType.ACTIVITY])
    )
}

@Suppress("LongMethod")
fun buildGlucoseStatisticModel(
    glucoseEventsPerPeriod: List<Event>?,
    settings: GlucoseLevelSettings,
    glucoseFormat: GlucoseFormat,
    forPeriod: Boolean = true
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
    val eventsLowPercent =
        if (eventsLowCount != 0) 100 - eventsHighPercent - eventsNormalPercent else 0

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

        dailyGlucoseModel = when (forPeriod) {
            true -> null
            else -> glucoseEventsPerPeriod?.let {
                buildDailyGlucoseModel(
                    list = it,
                    glucoseLevelSettings = settings,
                    glucoseFormat = glucoseFormat
                )
            }
        }
    )
}

fun buildInsulinStatisticModelByPeriod(insulinEventsPerPeriod: List<Event>?): InsulinStatisticModelByPeriod {
    var totalBolusLevel = 0.0
    var totalBasalLevel = 0.0
    var totalLevel = 0.0

    val daysWithBolusEvents = mutableSetOf<LocalDate>()
    val daysWithBasalEvents = mutableSetOf<LocalDate>()
    val daysWithEvents = mutableSetOf<LocalDate>()

    insulinEventsPerPeriod?.forEach { event ->
        val value = event.value
        if (value != null && value != 0.0) {
            if (event.isBolusInsulin()) {
                totalBolusLevel += value
                daysWithBolusEvents.add(event.additionTime.toLocalDate())
            }

            if (event.isBasalInsulin()) {
                totalBasalLevel += value
                daysWithBasalEvents.add(event.additionTime.toLocalDate())
            }

            if (event.isNotMixedInsulin()) {
                totalLevel += value
                daysWithEvents.add(event.additionTime.toLocalDate())
            }
        }
    }

    return InsulinStatisticModelByPeriod(
        averageBolusLevel = totalBolusLevel.average(daysWithBolusEvents.size),
        averageBasalLevel = totalBasalLevel.average(daysWithBasalEvents.size),
        averageLevel = totalLevel.average(daysWithEvents.size)
    )
}

fun buildBreadStatisticModelByPeriod(breadEventsPerPeriod: List<Event>?): BreadStatisticModelByPeriod {
    var totalLevel = 0.0
    val daysWithEvents = mutableSetOf<LocalDate>()

    breadEventsPerPeriod?.forEach { event ->
        val value = event.value
        if (value != null && value != 0.0) {
            totalLevel += value
            daysWithEvents.add(event.additionTime.toLocalDate())
        }
    }

    return BreadStatisticModelByPeriod(
        averageLevel = totalLevel.average(daysWithEvents.size)
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
