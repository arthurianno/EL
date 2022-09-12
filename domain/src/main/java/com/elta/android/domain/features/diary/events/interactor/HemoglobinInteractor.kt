package com.elta.android.domain.features.diary.events.interactor

import com.elta.android.common.utils.takeFirst
import com.elta.android.domain.features.diary.events.model.Event
import com.elta.android.domain.features.diary.events.model.EventType

private const val GEMOGLOBIN_EVENTS_COUNT = 10

fun buildHemoglobinEvents(allEvents: List<Event>) =
    allEvents.filter { it.type == EventType.GLYCATEDHEMOGLOBIN }
        .sortedByDescending { it.additionTime }
        .takeFirst(GEMOGLOBIN_EVENTS_COUNT)
