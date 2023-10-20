package com.elta.android.domain.features.diary.home.model

import com.elta.android.domain.features.diary.events.model.EventV2
import com.elta.android.domain.features.user.model.GlucoseFormat

data class DailyGlucoseModel(
    val glucoseEvents: List<EventV2>,
    val glucoseLevelSettings: GlucoseLevelSettings,
    val maxEvent: EventV2?,
    val minEvent: EventV2?,
    val lastEvent: EventV2?,
    val glucoseFormat: GlucoseFormat
) {
    val hasEvents: Boolean
        get() = glucoseEvents.isNotEmpty()
}
