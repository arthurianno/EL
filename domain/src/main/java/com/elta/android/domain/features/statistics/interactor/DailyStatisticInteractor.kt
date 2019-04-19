package com.elta.android.domain.features.statistics.interactor

import com.elta.android.domain.features.diary.events.model.Event
import com.elta.android.domain.features.diary.home.model.GlucoseLevelSettings
import com.elta.android.domain.features.statistics.model.daily.DailyBreadStatisticModel
import com.elta.android.domain.features.statistics.model.daily.DailyInsulinStatisticModel
import com.elta.android.domain.features.statistics.model.daily.DailyStatisticModel
import java.util.Date

fun buildDailyStatisticModel(date: Date, eventsPerDay: List<Event>, settings: GlucoseLevelSettings): DailyStatisticModel {
    return DailyStatisticModel(
        date = date,
        glucose = buildGlucoseStatisticModel(eventsPerDay, settings),
        insulin = buildDailyInsulinStatisticModel(eventsPerDay),
        bread = buildDailyBreadStatisticModel(eventsPerDay),
        activity = buildActivityStatisticModel(eventsPerDay)
    )
}

fun buildDailyInsulinStatisticModel(insulinEventsPerDay: List<Event>?): DailyInsulinStatisticModel {
    var totalBolusLevel = 0.0
    var totalBasalLevel = 0.0
    var totalLevel = 0.0

    insulinEventsPerDay?.forEach { event ->
        event.value?.let { value ->
            if (value != 0.0) {
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
        event.value?.let { value ->
            if (value != 0.0) {
                totalLevel += value
            }
        }
    }

    return DailyBreadStatisticModel(
        totalLevel = totalLevel
    )
}