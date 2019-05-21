package com.elta.android.presentation.utils

import com.elta.android.presentation.R
import com.nullgr.core.date.CommonFormats
import com.nullgr.core.date.isToday
import com.nullgr.core.date.isYesterday
import com.nullgr.core.date.toStringWithFormat
import com.nullgr.core.resources.ResourceProvider
import java.util.Calendar
import java.util.Date
import java.util.concurrent.TimeUnit

const val DATE_FORMAT_WITHOUT_ZERO = "d LLL yyyy"

fun Date.toEventTime(resourceProvider: ResourceProvider) =
    resourceProvider.getString(
        R.string.event_time_mask,
        this.toStringWithFormat(CommonFormats.FORMAT_TIME)
    )

fun Date.toEventDate(resourceProvider: ResourceProvider) =
    when {
        isToday() -> resourceProvider.getString(R.string.event_date_today)
        isYesterday() -> resourceProvider.getString(R.string.event_date_yesterday)
        else -> toStringWithFormat(DATE_FORMAT_WITHOUT_ZERO)
    }

fun Date.toSyncDate(resources: ResourceProvider) =
    when {
        isToday() -> "${resources.getString(R.string.event_date_today)} ${this.toEventTime(resources)}"
        isYesterday() -> "${resources.getString(R.string.event_date_yesterday)} ${this.toEventTime(resources)}"
        else -> toStringWithFormat(DATE_FORMAT_WITHOUT_ZERO)
    }

fun Date.toCalendar(): Calendar = Calendar.getInstance().apply { time = this@toCalendar }

val Calendar.year
    get() = this.get(Calendar.YEAR)

val Calendar.month
    get() = this.get(Calendar.MONTH)

val Calendar.dayOfMonth
    get() = this.get(Calendar.DAY_OF_MONTH)

val Calendar.hourOfDay
    get() = this.get(Calendar.HOUR_OF_DAY)

val Calendar.minute
    get() = this.get(Calendar.MINUTE)

infix fun Date.daysTo(other: Date): Long {
    val millisDiff = Math.abs(other.time - this.time)
    return TimeUnit.MILLISECONDS.toDays(millisDiff)
}