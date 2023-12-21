package com.elta.android.domain.features.user.interactor

import com.elta.android.domain.features.diary.events.model.EventType
import com.elta.android.domain.features.diary.events.model.EventV2
import com.elta.android.domain.features.user.isNameValid
import com.elta.android.domain.features.user.model.HealthApp
import com.elta.android.domain.features.user.model.HealthAppType
import com.elta.android.domain.features.user.model.Profile

fun buildProfile(original: Profile, events: List<EventV2>): Profile {
    if (events.isEmpty()) {
        return original
    }

    val sortedEvents = events.sortedByDescending { it.additionTime }

    var lastWeightEvent: EventV2? = null
    var lastHbA1cEvent: EventV2? = null

    sortedEvents.forEach { event ->
        if (lastWeightEvent == null && event.type == EventType.Weight) {
            lastWeightEvent = event
        } else if (lastHbA1cEvent == null && event.type == EventType.Glycatedhemoglobin) {
            lastHbA1cEvent = event
        }
    }

    val weight = lastWeightEvent?.value ?: original.weight
    val hba1cLevel = lastHbA1cEvent?.value ?: original.hba1cLevel

    return original.copy(weight = weight, hba1cLevel = hba1cLevel)
}

fun isNameValid(name: String): Boolean = name.isNameValid()

fun Profile.googleFitApp(): HealthApp? = healthApps?.find { it.type == HealthAppType.GOOGLE_FIT }
