package com.elta.android.data.features.diary.events.cache

import com.elta.android.data.features.common.cache.BoxStoreFactory
import com.elta.android.data.features.common.cache.CommonConditions
import com.elta.android.data.features.common.cache.Condition
import com.elta.android.data.features.common.cache.IllegalDeleteConditionError
import com.elta.android.data.features.common.cache.IllegalGetConditionError
import com.elta.android.data.features.diary.events.cache.dto.EventCachedDto
import com.elta.android.data.features.diary.events.cache.dto.EventCachedDto_
import io.objectbox.Box
import io.objectbox.kotlin.boxFor
import io.objectbox.kotlin.query
import java.util.Date
import javax.inject.Inject

class DbEventsCache @Inject constructor(
    private val factory: BoxStoreFactory
) : EventsCache {

    private val box: Box<EventCachedDto>
        get() = factory.getBoxStore().boxFor()

    override fun add(objects: List<EventCachedDto>) {
        box.put(objects)
    }

    override fun update(objects: List<EventCachedDto>) {
        box.put(objects)
    }

    override fun delete(condition: Condition) {
        when (condition) {
            is CommonConditions.All -> removeAll()
            is CommonConditions.ByIds -> removeByIds(condition.ids)
            else -> throw IllegalDeleteConditionError(condition)
        }
    }

    override fun get(condition: Condition): List<EventCachedDto> =
        when (condition) {
            is CommonConditions.All -> getAll()
            is EventsConditions.ByPeriod -> getAllForPeriod(condition.start, condition.end)
            is CommonConditions.ByIds -> getAllByIds(condition.ids)
            else -> throw IllegalGetConditionError(condition)
        }

    private fun removeAll() {
        box.removeAll()
    }

    private fun removeByIds(ids: List<Long>) {
        box.removeByKeys(ids)
    }

    private fun getAll(): List<EventCachedDto> = box.all

    private fun getAllForPeriod(start: Date, end: Date): List<EventCachedDto> =
            box.query {
                between(EventCachedDto_.additionTime, start, end)
            }.find()

    private fun getAllByIds(ids: List<Long>): List<EventCachedDto> = box[ids]
}