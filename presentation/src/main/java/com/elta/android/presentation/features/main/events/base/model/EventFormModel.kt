package com.elta.android.presentation.features.main.events.base.model

import com.elta.android.domain.features.diary.events.model.EventType
import com.elta.android.domain.features.diary.tags.model.Tag
import java.util.Date

data class EventFormModel(
    var eventType: EventType? = null,
    var pickerValue: Double? = 0.0,
    var inputValue: String? = null,
    var tag: Tag? = null,
    var isDateChanged: Boolean = false,
    var date: Date? = null,
    var note: String? = null,
    var meta: Any? = null
)