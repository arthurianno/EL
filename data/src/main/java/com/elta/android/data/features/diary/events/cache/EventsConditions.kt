package com.elta.android.data.features.diary.events.cache

import com.elta.android.data.features.common.cache.Condition
import java.util.Date

sealed class EventsConditions : Condition {

    data class ByPeriod(val start: Date, val end: Date) : EventsConditions()
}