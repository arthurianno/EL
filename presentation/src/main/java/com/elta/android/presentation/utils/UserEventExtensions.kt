package com.elta.android.presentation.utils

import android.support.annotation.DrawableRes
import com.elta.android.domain.features.events.model.UserEvent
import com.elta.android.presentation.R

fun UserEvent.toName(): Int =
    when (this) {
        UserEvent.XE -> R.string.event_type_xe
        UserEvent.INSULIN -> R.string.event_type_insulin
        UserEvent.MEDICINE -> R.string.event_type_medicines
        UserEvent.WEIGHT -> R.string.event_type_weight
        UserEvent.ACTIVITY -> R.string.event_type_activity
    }

@DrawableRes
fun UserEvent.toIcon(): Int =
    when (this) {
        UserEvent.XE -> R.drawable.ic_xe
        UserEvent.INSULIN -> R.drawable.ic_ins
        UserEvent.MEDICINE -> R.drawable.ic_medicine
        UserEvent.WEIGHT -> R.drawable.ic_weight
        UserEvent.ACTIVITY -> R.drawable.ic_active
    }