package com.elta.android.domain.features.user.interactor

import com.elta.android.domain.features.diary.events.model.Event
import com.elta.android.domain.features.diary.events.model.EventType
import com.elta.android.domain.features.user.model.Profile

private const val MAX_NAME_LENGTH = 50

fun buildProfile(original: Profile, events: List<Event>): Profile {
    if (events.isEmpty()) {
        return original
    }

    val sortedEvents = events.sortedByDescending { it.additionTime }

    var lastWeightEvent: Event? = null
    var lastHbA1cEvent: Event? = null

    sortedEvents.forEach { event ->
        if (lastWeightEvent == null && event.type == EventType.WEIGHT) {
            lastWeightEvent = event
        } else if (lastHbA1cEvent == null && event.type == EventType.GLYCATEDHEMOGLOBIN) {
            lastHbA1cEvent = event
        }
    }

    val weight = lastWeightEvent?.value ?: original.weight
    val hba1cLevel = lastHbA1cEvent?.value ?: original.hba1cLevel

    return original.copy(weight = weight, hba1cLevel = hba1cLevel)
}

fun isNameValid(firstName: String?, secondName: String?): Boolean {
    if (firstName == null || secondName == null) return false
    return firstName.isNotEmpty() && firstName.length < MAX_NAME_LENGTH ||
        secondName.isNotEmpty() && secondName.length < MAX_NAME_LENGTH
}
