package com.elta.android.data.features.diary.events.cache

import com.elta.android.data.features.common.cache.BoxCache
import com.elta.android.data.features.common.cache.BoxStoreFactory
import com.elta.android.data.features.common.cache.CommonConditions
import com.elta.android.data.features.common.cache.Condition
import com.elta.android.data.features.diary.events.cache.dto.EventCachedDto
import com.elta.android.data.features.diary.events.cache.dto.EventCachedDto_
import com.elta.android.data.features.diary.events.dto.EventTypeDto
import com.elta.android.data.features.diary.events.extensions.toQueryMillis
import io.objectbox.kotlin.query
import io.objectbox.query.QueryBuilder
import io.objectbox.query.QueryBuilder.StringOrder.CASE_INSENSITIVE
import javax.inject.Inject
import org.threeten.bp.LocalDateTime

class DbEventsCache @Inject constructor(
    factory: BoxStoreFactory
) : BoxCache<EventCachedDto>(factory) {

    override val classToken: Class<EventCachedDto> = EventCachedDto::class.java

    override fun getAll(condition: Condition): List<EventCachedDto> =
        when (condition) {
            is EventsConditions.ByPeriod -> getAllForPeriod(condition.start, condition.end)
            is EventsConditions.ByTypeAndIds -> getAllByTypeAndIds(condition.type, condition.ids)
            else -> super.getAll(condition)
        }

    override fun get(condition: Condition): EventCachedDto? =
        when (condition) {
            is EventsConditions.LastByType -> getLastByType(condition.type)
            else -> super.get(condition)
        }

    override fun contains(condition: Condition): Boolean =
        when (condition) {
            is CommonConditions.ById -> containsById(condition.id)
            else -> super.contains(condition)
        }

    private fun getAllForPeriod(start: LocalDateTime, end: LocalDateTime): List<EventCachedDto> =
        box.query {
            between(EventCachedDto_.additionTime, start.toQueryMillis(), end.toQueryMillis())
        }.find()

    private fun getAllByTypeAndIds(type: EventTypeDto, ids: LongArray): List<EventCachedDto> =
        box.query {
            equal(EventCachedDto_.type, type.name, CASE_INSENSITIVE)
            and()
            `in`(EventCachedDto_.id, ids)
        }.find()

    private fun getLastByType(type: EventTypeDto): EventCachedDto? =
        box.query {
            equal(EventCachedDto_.type, type.name, CASE_INSENSITIVE)
            order(EventCachedDto_.additionTimeString, QueryBuilder.DESCENDING)
        }.findFirst()

    private fun containsById(id: Long): Boolean =
        box.query {
            equal(EventCachedDto_.id, id)
        }.count() > 0
}
