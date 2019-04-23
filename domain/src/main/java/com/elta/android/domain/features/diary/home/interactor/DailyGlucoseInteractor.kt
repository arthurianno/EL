package com.elta.android.domain.features.diary.home.interactor

import com.elta.android.domain.features.diary.events.model.Event
import com.elta.android.domain.features.diary.events.model.EventType
import com.elta.android.domain.features.diary.home.model.DailyGlucoseModel
import com.elta.android.domain.features.diary.home.model.DoubleRange
import com.elta.android.domain.features.diary.home.model.GlucoseLevelSettings

fun buildDailyGlucoseModel(list: List<Event>, glucoseLevelSettings: GlucoseLevelSettings): DailyGlucoseModel {
    val sortedEvents = list.sortGlucoseOnly()
    return DailyGlucoseModel(
        glucoseEvents = sortedEvents,
        glucoseLevelSettings = glucoseLevelSettings,
        maxEvent = sortedEvents.max(glucoseLevelSettings.high),
        minEvent = sortedEvents.min(glucoseLevelSettings.low),
        lastEvent = sortedEvents.lastOrNull()
    )
}

fun List<Event>.sortGlucoseOnly(): List<Event> = sortedBy { it.additionTime }
    .filter { it.type == EventType.GLUCOSE && it.value != null }

private fun List<Event>.max(highRange: DoubleRange): Event? =
    maxBy { it.nonNullValue() }?.takeIf { it.nonNullValue() in highRange }

private fun List<Event>.min(lowRange: DoubleRange): Event? =
    minBy { it.nonNullValue() }?.takeIf { it.nonNullValue() in lowRange }

private fun Event.nonNullValue() = value ?: -1.0