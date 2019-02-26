package com.elta.android.data.features.diary.events.cache

import com.elta.android.data.features.common.cache.CommonConditions
import com.elta.android.data.features.common.cache.Condition
import com.elta.android.data.features.common.cache.IllegalDeleteConditionError
import com.elta.android.data.features.common.cache.IllegalGetConditionError
import com.elta.android.data.features.common.cache.doInUserExists
import com.elta.android.data.features.common.storage.UserHolder
import com.elta.android.data.features.diary.events.cache.dto.EventCachedDto
import com.elta.android.data.features.diary.events.cache.dto.EventCachedDto_
import io.objectbox.BoxStore
import io.objectbox.kotlin.boxFor
import io.objectbox.kotlin.inValues
import io.objectbox.kotlin.query
import java.util.Date
import javax.inject.Inject

class DbEventsCache @Inject constructor(
    private val userHolder: UserHolder,
    boxStore: BoxStore
) : EventsCache {

    private val box = boxStore.boxFor<EventCachedDto>()

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
            else -> throw IllegalGetConditionError(condition)
        }

    private fun removeAll() {
        userHolder.doInUserExists {
            val result = box.query {
                equal(EventCachedDto_.userId, it)
            }.findIds()
            box.removeByKeys(result.asList())
        }
    }

    private fun removeByIds(ids: List<Long>) {
        userHolder.doInUserExists {
            val result = box.query {
                equal(EventCachedDto_.userId, it)
                and()
                inValues(EventCachedDto_.id, ids.toLongArray())
            }.findIds()
            box.removeByKeys(result.asList())
        }
    }

    private fun getAll(): List<EventCachedDto> =
        userHolder.doInUserExists {
            box.query {
                equal(EventCachedDto_.userId, it)
            }.find()
        }

    private fun getAllForPeriod(start: Date, end: Date): List<EventCachedDto> =
        userHolder.doInUserExists {
            box.query {
                equal(EventCachedDto_.userId, it)
                and()
                between(EventCachedDto_.additionTime, start, end)
            }.find()
        }
}