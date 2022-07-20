package com.elta.android.presentation.features.main.events.base.model

import com.elta.android.domain.features.diary.events.model.ActivityType
import com.elta.android.domain.features.diary.events.model.EventType
import com.elta.android.domain.features.diary.events.model.Insulin
import com.elta.android.domain.features.diary.tags.model.Tag
import org.threeten.bp.ZonedDateTime

data class EventFormModel(
    var eventType: EventType? = null,
    var pickerValue: Double? = 0.0,
    var inputValue: String? = null,
    var tag: Tag? = null,
    var isDateChanged: Boolean = false,
    var date: ZonedDateTime? = null,
    var noteValue: String? = null,
    var meta: Any? = null
) {

    val kind: String?
        get() = if (eventType != EventType.BREAD || inputValue.isNullOrEmpty()) null else inputValue

    val name: String?
        get() = if (eventType != EventType.MEDICAMENTS || inputValue.isNullOrEmpty()) null else inputValue

    val value: Double?
        get() = when {
            eventType == EventType.ACTIVITY || eventType == EventType.MEDICAMENTS -> null
            pickerValue == 0.0 -> null
            else -> pickerValue
        }

    val duration: Long?
        get() = when {
            eventType != EventType.ACTIVITY -> null
            pickerValue?.toLong() == 0L -> null
            else -> pickerValue?.toLong()
        }

    val note: String?
        get() = if (noteValue.isNullOrEmpty()) null else noteValue

    val activityType: ActivityType?
        get() = meta as? ActivityType

//    val insulinType: InsulinType?
//        get() = meta as? InsulinType

    val insulin: Insulin?
        get() = meta as? Insulin
}
