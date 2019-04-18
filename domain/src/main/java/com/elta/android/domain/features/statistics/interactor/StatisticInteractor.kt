package com.elta.android.domain.features.statistics.interactor

import com.elta.android.domain.features.diary.events.model.Event
import com.elta.android.domain.features.diary.events.model.EventType
import com.elta.android.domain.features.diary.events.model.InsulinType
import com.elta.android.domain.features.diary.home.model.GlucoseLevelSettings
import com.elta.android.domain.features.statistics.model.ActivityStatisticModel
import com.elta.android.domain.features.statistics.model.BreadStatisticModel
import com.elta.android.domain.features.statistics.model.GlucoseStatisticModel
import com.elta.android.domain.features.statistics.model.InsulinStatisticModel
import com.elta.android.domain.features.statistics.model.StatisticByDateModel
import com.elta.android.domain.features.statistics.model.StatisticByPeriodModel
import com.elta.android.domain.features.statistics.model.StatisticPeriod
import com.nullgr.core.date.withoutTime
import timber.log.Timber
import java.util.Date

// O(6N)
fun buildStatisticModel(period: StatisticPeriod, events: List<Event>, settings: GlucoseLevelSettings): StatisticByPeriodModel {
    val eventsContainer = events.toEventsContainer() // O(N)

    val eventsByDay = eventsContainer.byDate
    val eventsByType = eventsContainer.byType

    val statisticByDay = mutableMapOf<Date, StatisticByDateModel>()

    var dayWithMaxLevel: StatisticByDateModel? = null
    var dayWithMinLevel: StatisticByDateModel? = null

    eventsByDay.entries.forEach { entry ->
        val day = entry.key
        val eventsPerDay = entry.value
        val dayStatistic = buildDayStatisticModel(day, eventsPerDay, settings) // O(4N)

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
        insulin = buildInsulinStatisticModel(eventsByType[EventType.INSULIN]),
        bread = buildBreadStatisticModel(eventsByType[EventType.BREAD]),
        activity = buildActivityStatisticModel(eventsByType[EventType.ACTIVITY])
    )
}

fun buildDayStatisticModel(date: Date, events: List<Event>, settings: GlucoseLevelSettings): StatisticByDateModel {
    return StatisticByDateModel(
        date = date,
        glucose = buildGlucoseStatisticModel(events, settings),
        insulin = buildInsulinStatisticModel(events),
        bread = buildBreadStatisticModel(events),
        activity = buildActivityStatisticModel(events)
    )
}

fun buildGlucoseStatisticModel(events: List<Event>? = null, settings: GlucoseLevelSettings): GlucoseStatisticModel {
    val count = events?.size ?: 0
    var totalLevel = 0.0

    var maxLevel = 0.0
    var minLevel = 0.0

    var maxHighLevel = 0.0
    var minHighLevel = 0.0

    var maxNormalLevel = 0.0
    var minNormalLevel = 0.0

    var maxLowLevel = 0.0
    var minLowLevel = 0.0

    var eventsHighCount = 0
    var eventsNormalCount = 0
    var eventsLowCount = 0

    events?.forEach { event ->
        if (event.type == EventType.GLUCOSE) {
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
    }

    return GlucoseStatisticModel(
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

        eventsHighPercent = eventsHighCount.percent(count),
        eventsNormalPercent = eventsNormalCount.percent(count),
        eventsLowPercent = eventsLowCount.percent(count)
    )
}

fun buildInsulinStatisticModel(events: List<Event>?): InsulinStatisticModel {
    var totalBolusLevel = 0.0
    var totalBasalLevel = 0.0
    var totalLevel = 0.0

    var bolusCount = 0
    var basalCount = 0
    var count = 0

    events?.forEach { event ->
        if (event.type == EventType.INSULIN) {
            event.value?.let { value ->
                if (value != 0.0) {
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
        }
    }

    return InsulinStatisticModel(
        averageBolusLevel = totalBolusLevel.average(bolusCount),
        averageBasalLevel = totalBasalLevel.average(basalCount),
        averageLevel = totalLevel.average(count)
    )
}

fun buildBreadStatisticModel(events: List<Event>?): BreadStatisticModel {
    var totalLevel = 0.0
    var count = 0

    events?.forEach { event ->
        if (event.type == EventType.BREAD) {
            event.value?.let { value ->
                if (value != 0.0) {
                    totalLevel += value
                    count++
                }
            }
        }
    }

    return BreadStatisticModel(
        averageLevel = totalLevel.average(count)
    )
}

fun buildActivityStatisticModel(events: List<Event>?): ActivityStatisticModel {
    var totalDuration = 0L
    var count = 0

    events?.forEach { event ->
        if (event.type == EventType.ACTIVITY) {
            event.duration?.let { duration ->
                if (duration != 0L) {
                    totalDuration += duration
                    count++
                }
            }
        }
    }

    return ActivityStatisticModel(
        eventsCount = count,
        averageDuration = totalDuration.average(count)
    )
}

internal inline fun Double.average(total: Int): Double = this / total
internal inline fun Long.average(total: Int): Long = this / total
internal inline fun Int.percent(total: Int): Double = this * 100.0 / total
internal inline fun Double.checkMax(max: Double): Double = if (max < this) this else max
internal inline fun Double.checkMin(min: Double): Double = if (min > this) this else min

internal inline fun StatisticByDateModel.checkMax(max: StatisticByDateModel): StatisticByDateModel = if (max.glucose.maxLevel < this.glucose.maxLevel) this else max
internal inline fun StatisticByDateModel.checkMin(min: StatisticByDateModel): StatisticByDateModel = if (min.glucose.minLevel > this.glucose.minLevel) this else min

internal inline fun Event.isBolusInsulin(): Boolean = insulinType == InsulinType.ULTRASHORT || insulinType == InsulinType.SHORT
internal inline fun Event.isBasalInsulin(): Boolean = insulinType == InsulinType.INTERMIDIATE || insulinType == InsulinType.LONG || insulinType == InsulinType.ULTRALONG
internal inline fun Event.isNotMixedInsulin(): Boolean = insulinType != InsulinType.MIXED

internal fun List<Event>.splitByDate(): Map<Date, List<Event>> {
    val destinations = hashMapOf<Date, List<Event>>()
    for (element in this) {
        val day = element.additionTime.withoutTime()
        var destination = destinations[day]
        if (destination == null) {
            destination = arrayListOf()
            destinations[day] = destination
        }
        (destination as MutableList).add(element)
    }
    return destinations
}

internal fun List<Event>.splitByType(): Map<EventType, List<Event>> {
    val destinations = hashMapOf<EventType, List<Event>>()
    val predicates = EventType.values()
    for (element in this) {
        for (predicate in predicates) {
            var destination = destinations[predicate]
            if (destination == null) {
                destination = arrayListOf()
                destinations[predicate] = destination
            }
            if (element.type == predicate) (destination as MutableList).add(element)
        }
    }
    return destinations
}

internal fun List<Event>.toEventsContainer(): EventsContainer {
    val byDate = hashMapOf<Date, List<Event>>()
    val byType = hashMapOf<EventType, List<Event>>()

    for (element in this) {
        // split by date
        val day = element.additionTime.withoutTime()
        var destinationByDate = byDate[day]
        if (destinationByDate == null) {
            destinationByDate = arrayListOf()
            byDate[day] = destinationByDate
        }
        (destinationByDate as MutableList).add(element)

        // split by type
        val type = element.type
        var destinationByType = byType[type]
        if (destinationByType == null) {
            destinationByType = arrayListOf()
            byType[type] = destinationByType
        }
        (destinationByType as MutableList).add(element)
    }

    return EventsContainer(byDate = byDate, byType = byType)
}

data class EventsContainer(
    val byDate: Map<Date, List<Event>>,
    val byType: Map<EventType, List<Event>>
)