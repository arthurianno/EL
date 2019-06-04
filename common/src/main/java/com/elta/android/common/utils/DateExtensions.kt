package com.elta.android.common.utils

import org.threeten.bp.ZonedDateTime

inline fun timestamp(): Long = ZonedDateTime.now().toMillis()

inline fun ZonedDateTime?.isDateChanged(other: ZonedDateTime): Boolean {
    return when {
        this == null -> false
        else -> this != other
    }
}