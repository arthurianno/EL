package com.elta.android.presentation.utils

import android.support.annotation.DrawableRes
import com.elta.android.domain.features.diary.events.model.EventType
import com.elta.android.presentation.R

fun EventType.toName(): Int =
    when (this) {
        EventType.BREAD -> R.string.event_type_xe
        EventType.INSULIN -> R.string.event_type_insulin
        EventType.MEDICAMENTS -> R.string.event_type_medicines
        EventType.WEIGHT -> R.string.event_type_weight
        EventType.ACTIVITY -> R.string.event_type_activity
        EventType.GLUCOSE -> R.string.event_type_glucose
    }

@DrawableRes
fun EventType.toIcon(): Int =
    when (this) {
        EventType.BREAD -> R.drawable.ic_event_xe
        EventType.INSULIN -> R.drawable.ic_event_ins
        EventType.MEDICAMENTS -> R.drawable.ic_event_medicine
        EventType.WEIGHT -> R.drawable.ic_event_weight
        EventType.ACTIVITY -> R.drawable.ic_event_active
        EventType.GLUCOSE -> R.drawable.ic_event_ins
    }

@DrawableRes
fun EventType.toIconWithBg(): Int =
    when (this) {
        EventType.BREAD -> R.drawable.ic_event_xe_with_bg
        EventType.INSULIN -> R.drawable.ic_event_ins_with_bg
        EventType.MEDICAMENTS -> R.drawable.ic_event_medicine_with_bg
        EventType.WEIGHT -> R.drawable.ic_event_weight_with_bg
        EventType.ACTIVITY -> R.drawable.ic_event_active_with_bg
        EventType.GLUCOSE -> R.drawable.ic_event_ins_with_bg
    }