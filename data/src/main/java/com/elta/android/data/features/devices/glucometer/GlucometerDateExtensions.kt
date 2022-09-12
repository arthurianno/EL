package com.elta.android.data.features.devices.glucometer

import com.elta.android.common.utils.toDateTimeUtc
import com.elta.android.common.utils.toLocalDateTime
import com.elta.android.common.utils.toStringWithFormat
import org.threeten.bp.ZoneId
import org.threeten.bp.ZoneOffset
import org.threeten.bp.ZonedDateTime

const val TO_PATTERN = "yyMMddHHmmss"
const val FROM_PATTERN = "yyyyMMddHHmmss"

/**
 * This file contains extension functions to convert date objects
 * for sync with glucometer use case.
 *
 * First sync step is to set time at glucometer,
 * according to logic glucometer time must be in UTC time with 0 offset.
 * Use #toGlucometerDateTime function to convert #ZonedDateTime to format acceptable by glucometer.
 *
 * Second is to create #ZonedDateTime from event response. As glucometer return events
 * with time in UTC without any information about offset add offset information and extra time manually.
 * Use #fromGlucometerDateTime function.
 *
 * Third is to convert date from second step to date that will be stored in database.
 * Use #toStorageDateTime function
 */

fun ZonedDateTime.toGlucometerDateTime() = toDateTimeUtc().toStringWithFormat(TO_PATTERN)

fun String.fromGlucometerDateTime() =
    ZonedDateTime.of("20$this".toLocalDateTime(FROM_PATTERN), ZoneOffset.UTC)

fun ZonedDateTime.toStorageDateTime() =
    this.toOffsetDateTime().atZoneSameInstant(ZoneId.systemDefault())
