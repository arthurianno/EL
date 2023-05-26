package com.elta.android.domain.features.diary.home.interactor

import com.elta.android.domain.features.diary.events.model.Event
import com.elta.android.domain.features.diary.events.model.EventType
import com.elta.android.domain.features.diary.home.model.DailyGlucoseModel
import com.elta.android.domain.features.diary.home.model.DoubleRange
import com.elta.android.domain.features.diary.home.model.GlucoseLevelSettings
import com.elta.android.domain.features.user.model.GlucoseFormat

fun buildDailyGlucoseModel(
    list: List<Event>,
    glucoseLevelSettings: GlucoseLevelSettings,
    glucoseFormat: GlucoseFormat
): DailyGlucoseModel {
    val sortedEvents = list.sortGlucoseOnly()
    return DailyGlucoseModel(
        glucoseEvents = sortedEvents,
        glucoseLevelSettings = glucoseLevelSettings,
        maxEvent = sortedEvents.max(glucoseLevelSettings.high),
        minEvent = sortedEvents.min(glucoseLevelSettings.low),
        lastEvent = sortedEvents.lastOrNull(),
        glucoseFormat = glucoseFormat
    )
}

fun List<Event>.sortGlucoseOnly(): List<Event> = sortedBy { it.additionTime }
    .filter { it.type == EventType.GLUCOSE && it.value != null }

private fun List<Event>.max(highRange: DoubleRange): Event? =
    maxByOrNull { it.nonNullValue() }?.takeIf { it.nonNullValue() in highRange }

private fun List<Event>.min(lowRange: DoubleRange): Event? =
    minByOrNull { it.nonNullValue() }?.takeIf { it.nonNullValue() in lowRange }

private fun Event.nonNullValue() = value ?: -1.0
