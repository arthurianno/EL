package com.elta.android.domain.features.diary.home.interactor

import com.elta.android.domain.features.diary.events.model.EventType
import com.elta.android.domain.features.diary.events.model.EventV2
import com.elta.android.domain.features.diary.home.model.DailyGlucoseModel
import com.elta.android.domain.features.diary.home.model.DoubleRange
import com.elta.android.domain.features.diary.home.model.GlucoseLevelSettings
import com.elta.android.domain.features.user.model.GlucoseFormat

fun buildDailyGlucoseModel(
    list: List<EventV2>,
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

fun List<EventV2>.sortGlucoseOnly(): List<EventV2> = sortedBy { it.additionTime }
    .filter { it.type == EventType.GLUCOSE && it.value != null }

private fun List<EventV2>.max(highRange: DoubleRange): EventV2? =
    maxByOrNull { it.nonNullValue() }?.takeIf { it.nonNullValue() in highRange }

private fun List<EventV2>.min(lowRange: DoubleRange): EventV2? =
    minByOrNull { it.nonNullValue() }?.takeIf { it.nonNullValue() in lowRange }

private fun EventV2.nonNullValue() = value ?: -1.0
