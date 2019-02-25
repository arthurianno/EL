package com.elta.android.data.common

import com.nullgr.core.date.toStringWithFormat
import org.joda.time.Instant
import java.util.Date
import java.util.TimeZone

fun String.getDate(): Date = Instant.parse(this).toDate()

fun Date.toStringIso(): String = toStringWithFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", TimeZone.getDefault())