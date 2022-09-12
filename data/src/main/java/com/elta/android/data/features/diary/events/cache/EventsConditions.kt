package com.elta.android.data.features.diary.events.cache

import com.elta.android.data.features.common.cache.Condition
import com.elta.android.data.features.diary.events.dto.EventTypeDto
import org.threeten.bp.LocalDateTime

sealed class EventsConditions : Condition {

    data class ByPeriod(val start: LocalDateTime, val end: LocalDateTime) : EventsConditions()
    data class ByTypeAndIds(val type: EventTypeDto, val ids: LongArray) : EventsConditions()
}
