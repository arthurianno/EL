package com.elta.android.data.features.diary.cache

import com.elta.android.data.features.common.cache.CommonConditions
import com.elta.android.data.features.common.cache.Condition
import com.elta.android.data.features.common.cache.IllegalDeleteConditionError
import com.elta.android.data.features.common.cache.IllegalGetConditionError
import com.elta.android.data.features.diary.cache.dto.EventCachedDto
import com.elta.android.data.features.diary.cache.dto.EventCachedDto_
import io.objectbox.BoxStore
import io.objectbox.kotlin.query
import java.util.Date
import javax.inject.Inject

class DbEventsCache @Inject constructor(
    boxStore: BoxStore
) : EventsCache {

    private val box = boxStore.boxFor(EventCachedDto::class.java)

    override fun add(objects: List<EventCachedDto>) {
        box.put(objects)
    }

    override fun update(objects: List<EventCachedDto>) {
        box.put(objects)
    }

    override fun delete(condition: Condition) {
        when (condition) {
            is CommonConditions.All -> box.removeAll()
            is CommonConditions.ByIds -> box.removeByKeys(condition.ids)
            else -> throw IllegalDeleteConditionError(condition)
        }
    }

    override fun get(condition: Condition): List<EventCachedDto> =
        when (condition) {
            is CommonConditions.All -> box.all
            is EventsConditions.ByPeriod -> getAllForPeriod(condition.start, condition.end)
            else -> throw IllegalGetConditionError(condition)
        }

    private fun getAllForPeriod(start: Date, end: Date): List<EventCachedDto> =
        box.query {
            between(EventCachedDto_.additionTime, start, end)
        }.find()
}