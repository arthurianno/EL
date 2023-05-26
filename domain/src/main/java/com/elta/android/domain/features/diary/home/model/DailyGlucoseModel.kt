package com.elta.android.domain.features.diary.home.model

import com.elta.android.domain.features.diary.events.model.Event
import com.elta.android.domain.features.user.model.GlucoseFormat

data class DailyGlucoseModel(
    val glucoseEvents: List<Event>,
    val glucoseLevelSettings: GlucoseLevelSettings,
    val maxEvent: Event?,
    val minEvent: Event?,
    val lastEvent: Event?,
    val glucoseFormat: GlucoseFormat
) {
    val hasEvents: Boolean
        get() = glucoseEvents.isNotEmpty()
}
