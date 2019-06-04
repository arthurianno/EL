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

inline fun ZonedDateTime.toEventTime(resources: ResourceProvider) =
    resources.getString(R.string.event_time_mask, this.toStringWithFormat(CommonFormats.FORMAT_TIME))

inline fun ZonedDateTime.toEventDate(resourceProvider: ResourceProvider) =
    when {
        this.toLocalDate().isToday() -> resourceProvider.getString(R.string.event_date_today)
        this.toLocalDate().isYesterday() -> resourceProvider.getString(R.string.event_date_yesterday)
        else -> toStringWithFormat(DATE_FORMAT_WITHOUT_ZERO)
    }

inline fun ZonedDateTime.toSyncDate(resources: ResourceProvider) =
    when {
        this.toLocalDate().isToday() -> "${resources.getString(R.string.event_date_today)} ${this.toEventTime(resources)}"
        this.toLocalDate().isYesterday() -> "${resources.getString(R.string.event_date_yesterday)} ${this.toEventTime(resources)}"
        else -> toStringWithFormat(DATE_FORMAT_WITHOUT_ZERO)
    }

infix fun LocalDateTime.daysTo(other: LocalDateTime): Long = Duration.between(other, this).abs().toDays()