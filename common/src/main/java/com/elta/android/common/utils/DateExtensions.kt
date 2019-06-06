package com.elta.android.common.utils

import org.threeten.bp.ZonedDateTime

inline fun timestamp(): Long = currentMillisUtc() / 1000

inline fun currentMillisUtc(): Long = ZonedDateTime.now().toMillisUtc()

inline fun currentMillis(): Long = ZonedDateTime.now().toMillis()

inline fun millisAtStartOfDay(): Long = ZonedDateTime.now().atStartOfDay().toMillis()

inline fun ZonedDateTime?.isDateChanged(other: ZonedDateTime): Boolean {
    return when {
        this == null -> false
        else -> this != other
    }
}