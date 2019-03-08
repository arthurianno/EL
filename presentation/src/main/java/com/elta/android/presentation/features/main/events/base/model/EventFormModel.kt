package com.elta.android.presentation.features.main.events.base.model

import com.elta.android.domain.features.diary.events.model.EventType
import java.util.Date

data class EventFormModel(
    var eventType: EventType? = null,
    var pickerValue: Double? = 0.0,
    var inputValue: String? = null,
    var variantId: String? = null,
    var tagId: String? = null,
    var isDateChanged: Boolean = false,
    var date: Date? = null,
    var note: String? = null
)