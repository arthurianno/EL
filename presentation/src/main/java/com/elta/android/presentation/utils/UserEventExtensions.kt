package com.elta.android.presentation.utils

import android.support.annotation.DrawableRes
import com.elta.android.domain.features.diary.events.model.EventType
import com.elta.android.presentation.R

fun EventType.toName(): Int =
    when (this) {
        EventType.BREAD -> R.string.event_type_bread
        EventType.INSULIN -> R.string.event_type_insulin
        EventType.MEDICAMENTS -> R.string.event_type_medicaments
        EventType.WEIGHT -> R.string.event_type_weight
        EventType.ACTIVITY -> R.string.event_type_activity
        EventType.GLUCOSE -> R.string.event_type_glucose
    }

@DrawableRes
fun EventType.toIcon(): Int =
    when (this) {
        EventType.BREAD -> R.drawable.ic_event_bread
        EventType.INSULIN -> R.drawable.ic_event_insulin
        EventType.MEDICAMENTS -> R.drawable.ic_event_medicaments
        EventType.WEIGHT -> R.drawable.ic_event_weight
        EventType.ACTIVITY -> R.drawable.ic_event_activity
        EventType.GLUCOSE -> R.drawable.ic_event_glucose
    }

@DrawableRes
fun EventType.toIconWithBg(): Int =
    when (this) {
        EventType.BREAD -> R.drawable.ic_event_bread_with_bg
        EventType.INSULIN -> R.drawable.ic_event_insulin_with_bg
        EventType.MEDICAMENTS -> R.drawable.ic_event_medicaments_with_bg
        EventType.WEIGHT -> R.drawable.ic_event_weight_with_bg
        EventType.ACTIVITY -> R.drawable.ic_event_activity_with_bg
        EventType.GLUCOSE -> R.drawable.ic_event_insulin_with_bg
    }