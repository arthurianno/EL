package com.elta.android.presentation.core.date

import com.elta.android.presentation.core.bus.Event
import org.threeten.bp.ZonedDateTime

data class DateChangedEvent(val prevDate: ZonedDateTime, val newDate: ZonedDateTime) : Event