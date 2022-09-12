package com.elta.android.common.utils

import org.threeten.bp.ZonedDateTime

const val MILLIS_IN_SECOND = 1000L

fun timestamp(): Long = currentMillisUtc() / MILLIS_IN_SECOND

fun currentMillisUtc(): Long = ZonedDateTime.now().toMillisUtc()

fun currentMillis(): Long = ZonedDateTime.now().toMillis()

fun millisAtStartOfDay(): Long = ZonedDateTime.now().atStartOfDay().toMillis()

fun ZonedDateTime?.isDateChanged(other: ZonedDateTime): Boolean {
    return when {
        this == null -> false
        else -> this != other
    }
}
