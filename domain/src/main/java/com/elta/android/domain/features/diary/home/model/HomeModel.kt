package com.elta.android.domain.features.diary.home.model

import com.elta.android.domain.features.diary.events.model.EventV2
import com.elta.android.domain.features.user.model.GlucoseFormat

data class HomeModel(
    val isFirstEntrance: Boolean,
    val dayPeriod: DayPeriod,
    val lastFoodEvent: EventV2?,
    val lastInsulinEvent: EventV2?,
    val lastGlucoseEvent: EventV2?,
    val glucoseLevel: GlucoseLevel?,
    val glucoseLevelDirection: GlucoseLevelDirection?,
    val glucoseLevelDifference: Double?,
    val eventsBlocks: List<EventsBlock>,
    val dailyGlucoseModel: DailyGlucoseModel,
    val glucoseFormat: GlucoseFormat,
    val calculatorFlow: CalculatorFlow
) {
    val hasEvents: Boolean
        get() = eventsBlocks.isNotEmpty()
}
