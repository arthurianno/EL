package com.elta.android.presentation.features.main.events.base.initializer

import android.view.View
import com.elta.android.domain.features.diary.events.model.EventType
import com.elta.android.presentation.widgets.picker.model.FormMeasurementConfig

interface FormInitializer {

    val pickerConfiguration: FormMeasurementConfig?

    fun init(view: View)
}

fun formInitializer(eventType: EventType) =
    when (eventType) {
        EventType.BREAD -> BreadFormInitializer
        EventType.INSULIN -> InsulinFormInitializer
        EventType.MEDICAMENTS -> MedicamentsFormInitializer
        EventType.ACTIVITY -> ActivityFormInitializer
        EventType.WEIGHT -> WeightFormInitializer
        else -> throw IllegalArgumentException("No form initializer for GLUCOSE type")
    }