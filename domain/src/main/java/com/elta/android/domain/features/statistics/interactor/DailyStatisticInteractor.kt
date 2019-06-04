package com.elta.android.domain.features.statistics.interactor

import com.elta.android.domain.features.diary.events.model.Event
import com.elta.android.domain.features.diary.events.model.EventType
import com.elta.android.domain.features.diary.home.model.GlucoseLevelSettings
import com.elta.android.domain.features.statistics.model.daily.DailyBreadStatisticModel
import com.elta.android.domain.features.statistics.model.daily.DailyInsulinStatisticModel
import com.elta.android.domain.features.statistics.model.daily.DailyStatisticModel
import org.threeten.bp.LocalDate

fun buildDailyStatisticModel(
    date: LocalDate,
    eventsPerDay: Map<EventType, List<Event>>,
    settings: GlucoseLevelSettings
): DailyStatisticModel {
    return DailyStatisticModel(
        date = date,
        glucose = buildGlucoseStatisticModel(eventsPerDay[EventType.GLUCOSE], settings, false),
        insulin = buildDailyInsulinStatisticModel(eventsPerDay[EventType.INSULIN]),
        bread = buildDailyBreadStatisticModel(eventsPerDay[EventType.BREAD]),
        activity = buildActivityStatisticModel(eventsPerDay[EventType.ACTIVITY])
    )
}

fun buildDailyInsulinStatisticModel(insulinEventsPerDay: List<Event>?): DailyInsulinStatisticModel {
    var totalBolusLevel = 0.0
    var totalBasalLevel = 0.0
    var totalLevel = 0.0

    insulinEventsPerDay?.forEach { event ->
        val value = event.value
        if (value != null && value != 0.0) {
            if (event.isBolusInsulin()) {
                totalBolusLevel += value
            }

            if (event.isBasalInsulin()) {
                totalBasalLevel += value
            }

            if (event.isNotMixedInsulin()) {
                totalLevel += value
            }
        }
    }

    return DailyInsulinStatisticModel(
        totalBolusLevel = totalBolusLevel,
        totalBasalLevel = totalBasalLevel,
        totalLevel = totalLevel
    )
}

fun buildDailyBreadStatisticModel(breadEventsPerDay: List<Event>?): DailyBreadStatisticModel {
    var totalLevel = 0.0

    breadEventsPerDay?.forEach { event ->
        val value = event.value
        if (value != null && value != 0.0) {
            totalLevel += value
        }
    }

    return DailyBreadStatisticModel(
        totalLevel = totalLevel
    )
}