package com.elta.android.data.features.diary.events.cache

import com.elta.android.data.features.common.cache.BoxCache
import com.elta.android.data.features.common.cache.BoxStoreFactory
import com.elta.android.data.features.common.cache.Condition
import com.elta.android.data.features.diary.events.cache.dto.EventCachedDto
import com.elta.android.data.features.diary.events.cache.dto.EventCachedDto_
import io.objectbox.kotlin.query
import java.util.Date
import javax.inject.Inject

class DbEventsCache @Inject constructor(
    factory: BoxStoreFactory
) : BoxCache<EventCachedDto>(factory), EventsCache {

    override val classToken: Class<EventCachedDto> = EventCachedDto::class.java

    override fun get(condition: Condition): List<EventCachedDto> =
        when (condition) {
            is EventsConditions.ByPeriod -> getAllForPeriod(condition.start, condition.end)
            else -> super.get(condition)
        }

    private fun getAllForPeriod(start: Date, end: Date): List<EventCachedDto> =
        box.query {
            between(EventCachedDto_.additionTime, start, end)
        }.find()

}