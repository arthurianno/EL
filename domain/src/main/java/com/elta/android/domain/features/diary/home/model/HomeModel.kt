package com.elta.android.domain.features.diary.home.model

import com.elta.android.domain.features.diary.events.model.Event
import com.elta.android.domain.features.user.model.GlucoseFormat

data class HomeModel(
    val isFirstEntrance: Boolean,
    val dayPeriod: DayPeriod,
    val lastBreadEvent: Event?,
    val lastInsulinEvent: Event?,
    val lastGlucoseEvent: Event?,
    val glucoseLevel: GlucoseLevel?,
    val glucoseLevelDirection: GlucoseLevelDirection?,
    val glucoseLevelDifference: Double?,
    val eventsBlocks: List<EventsBlock>,
    val dailyGlucoseModel: DailyGlucoseModel,
    val glucoseFormat: GlucoseFormat
) {
    val hasEvents: Boolean
        get() = eventsBlocks.isNotEmpty()
}
