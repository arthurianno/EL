package com.elta.android.domain.features.diary.events

import com.elta.android.domain.features.diary.events.model.Event
import com.elta.android.domain.features.user.model.GlucoseFormat

const val GLUCOSE_DEFAULT_VALUE = 0.0
const val GLUCOSE_PLASMA_COEFFICIENT = 1.12

fun Event.glucoseValue(format: GlucoseFormat): Double = run {
    value?.let {
        when (format) {
            GlucoseFormat.CAPLILARY -> it
            GlucoseFormat.PLASMA -> it * GLUCOSE_PLASMA_COEFFICIENT
        }
    } ?: GLUCOSE_DEFAULT_VALUE
}
