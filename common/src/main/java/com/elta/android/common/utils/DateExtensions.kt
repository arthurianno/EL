package com.elta.android.common.utils

import org.threeten.bp.ZonedDateTime

const val MILLIS_IN_SECOND = 1000L

inline fun timestamp(): Long = currentMillisUtc() / MILLIS_IN_SECOND

inline fun currentMillisUtc(): Long = ZonedDateTime.now().toMillisUtc()

inline fun currentMillis(): Long = ZonedDateTime.now().toMillis()

inline fun millisAtStartOfDay(): Long = ZonedDateTime.now().atStartOfDay().toMillis()

inline fun ZonedDateTime?.isDateChanged(other: ZonedDateTime): Boolean {
    return when {
        this == null -> false
        else -> this != other
    }
}
