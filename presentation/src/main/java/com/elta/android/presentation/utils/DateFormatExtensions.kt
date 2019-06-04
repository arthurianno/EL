package com.elta.android.presentation.utils

import com.elta.android.common.utils.CommonFormats
import com.elta.android.common.utils.isToday
import com.elta.android.common.utils.isYesterday
import com.elta.android.common.utils.toStringWithFormat
import com.elta.android.presentation.R
import com.nullgr.core.resources.ResourceProvider
import org.threeten.bp.Duration
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZonedDateTime

const val DATE_FORMAT_WITHOUT_ZERO = "d LLL yyyy"

inline fun ZonedDateTime.toEventTime(r: ResourceProvider) =
    r.getString(R.string.event_time_mask, toStringWithFormat(CommonFormats.FORMAT_TIME))

inline fun ZonedDateTime.toEventDate(r: ResourceProvider) =
    when {
        toLocalDate().isToday() -> r.getString(R.string.event_date_today)
        toLocalDate().isYesterday() -> r.getString(R.string.event_date_yesterday)
        else -> toStringWithFormat(DATE_FORMAT_WITHOUT_ZERO)
    }

inline fun ZonedDateTime.toSyncDate(r: ResourceProvider) =
    when {
        toLocalDate().isToday() -> "${r.getString(R.string.event_date_today)} ${toEventTime(r)}"
        toLocalDate().isYesterday() -> "${r.getString(R.string.event_date_yesterday)} ${toEventTime(r)}"
        else -> toStringWithFormat(DATE_FORMAT_WITHOUT_ZERO)
    }

infix fun LocalDateTime.daysTo(other: LocalDateTime): Long = Duration.between(other, this).abs().toDays()