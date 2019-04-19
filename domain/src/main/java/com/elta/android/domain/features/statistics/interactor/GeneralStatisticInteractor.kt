package com.elta.android.domain.features.statistics.interactor

import com.elta.android.domain.features.diary.events.model.Event
import com.elta.android.domain.features.diary.events.model.EventType
import com.elta.android.domain.features.diary.events.model.InsulinType
import com.elta.android.domain.features.statistics.model.daily.DailyStatisticModel
import com.nullgr.core.date.withoutTime
import java.util.Date

internal inline fun Double.average(total: Int): Double = this / total
internal inline fun Long.average(total: Int): Long = this / total
internal inline fun Int.percent(total: Int): Double = this * 100.0 / total
internal inline fun Double.checkMax(max: Double): Double = if (max < this) this else max
internal inline fun Double.checkMin(min: Double): Double = if (min > this) this else min
internal inline fun Event.isBolusInsulin(): Boolean = insulinType == InsulinType.ULTRASHORT || insulinType == InsulinType.SHORT
internal inline fun Event.isBasalInsulin(): Boolean = insulinType == InsulinType.INTERMIDIATE || insulinType == InsulinType.LONG || insulinType == InsulinType.ULTRALONG
internal inline fun Event.isNotMixedInsulin(): Boolean = insulinType != InsulinType.MIXED

internal inline fun DailyStatisticModel.checkMax(max: DailyStatisticModel): DailyStatisticModel = if (max.glucose.maxLevel < this.glucose.maxLevel) this else max
internal inline fun DailyStatisticModel.checkMin(min: DailyStatisticModel): DailyStatisticModel = if (min.glucose.minLevel > this.glucose.minLevel) this else min

internal fun List<Event>.toEventsContainer(): EventsContainer {
    val byType = hashMapOf<EventType, List<Event>>()
    val byTypePerDay = hashMapOf<Date, Map<EventType, List<Event>>>()

    for (element in this) {
        // split by type
        val type = element.type
        var destinationByType = byType[type]
        if (destinationByType == null) {
            destinationByType = arrayListOf()
            byType[type] = destinationByType
        }
        (destinationByType as MutableList).add(element)

        // split by type per day
        val day = element.additionTime.withoutTime()
        var destinationByDay1 = byTypePerDay[day]
        if (destinationByDay1 == null) {
            destinationByDay1 = hashMapOf()
            byTypePerDay[day] = destinationByDay1
        }

        var destinationByType1 = destinationByDay1[type]
        if (destinationByType1 == null) {
            destinationByType1 = arrayListOf()
            (destinationByDay1 as MutableMap)[type] = destinationByType1
        }
        (destinationByType1 as MutableList).add(element)
    }

    return EventsContainer(byType = byType, byTypePerDay = byTypePerDay)
}

data class EventsContainer(
    val byType: Map<EventType, List<Event>>,
    val byTypePerDay: Map<Date, Map<EventType, List<Event>>>
)