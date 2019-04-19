package com.elta.android.domain.features.statistics.interactor

import com.elta.android.domain.features.diary.events.model.Event
import com.elta.android.domain.features.diary.home.model.GlucoseLevelSettings
import com.elta.android.domain.features.statistics.model.daily.DailyBreadStatisticModel
import com.elta.android.domain.features.statistics.model.daily.DailyInsulinStatisticModel
import com.elta.android.domain.features.statistics.model.daily.DailyStatisticModel
import java.util.Date

fun buildDailyStatisticModel(date: Date, events: List<Event>, settings: GlucoseLevelSettings): DailyStatisticModel {
    return DailyStatisticModel(
        date = date,
        glucose = buildGlucoseStatisticModel(events, settings),
        insulin = buildDailyInsulinStatisticModel(events),
        bread = buildDailyBreadStatisticModel(events),
        activity = buildActivityStatisticModel(events)
    )
}

fun buildDailyInsulinStatisticModel(events: List<Event>?): DailyInsulinStatisticModel {

    return DailyInsulinStatisticModel(
        totalBolusLevel = 0.0,
        totalBasalLevel = 0.0,
        totalLevel = 0.0
    )
}

fun buildDailyBreadStatisticModel(events: List<Event>?): DailyBreadStatisticModel {
    return DailyBreadStatisticModel(
        totalLevel = 0.0
    )
}