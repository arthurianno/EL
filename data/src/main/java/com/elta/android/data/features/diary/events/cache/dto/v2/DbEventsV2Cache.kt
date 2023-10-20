package com.elta.android.data.features.diary.events.cache.dto.v2

import com.elta.android.data.features.common.cache.BoxCache
import com.elta.android.data.features.common.cache.BoxStoreFactory
import com.elta.android.data.features.common.cache.CommonConditions
import com.elta.android.data.features.common.cache.Condition
import com.elta.android.data.features.diary.events.cache.EventsConditions
import com.elta.android.data.features.diary.events.dto.EventTypeDto
import com.elta.android.data.features.diary.events.extensions.toQueryMillis
import io.objectbox.kotlin.query
import io.objectbox.query.QueryBuilder
import io.objectbox.query.QueryBuilder.StringOrder.CASE_INSENSITIVE
import javax.inject.Inject
import org.threeten.bp.LocalDateTime

class DbEventsV2Cache @Inject constructor(
    factory: BoxStoreFactory
) : BoxCache<EventV2CachedDto>(factory) {

    override val classToken: Class<EventV2CachedDto> = EventV2CachedDto::class.java

    override fun getAll(condition: Condition): List<EventV2CachedDto> =
        when (condition) {
            is EventsConditions.ByPeriod -> getAllForPeriod(condition.start, condition.end)
            is EventsConditions.ByTypeAndIds -> getAllByTypeAndIds(condition.type, condition.ids)
            else -> super.getAll(condition)
        }

    override fun get(condition: Condition): EventV2CachedDto? =
        when (condition) {
            is EventsConditions.LastByType -> getLastByType(condition.type)
            else -> super.get(condition)
        }

    override fun contains(condition: Condition): Boolean =
        when (condition) {
            is CommonConditions.ById -> containsById(condition.id)
            else -> super.contains(condition)
        }

    private fun getAllForPeriod(start: LocalDateTime, end: LocalDateTime): List<EventV2CachedDto> =
        box.query {
            between(EventV2CachedDto_.additionTime, start.toQueryMillis(), end.toQueryMillis())
        }.find()

    private fun getAllByTypeAndIds(type: EventTypeDto, ids: LongArray): List<EventV2CachedDto> =
        box.query {
            equal(EventV2CachedDto_.type, type.name, CASE_INSENSITIVE)
            and()
            `in`(EventV2CachedDto_.id, ids)
        }.find()

    private fun getLastByType(type: EventTypeDto): EventV2CachedDto? =
        box.query {
            equal(EventV2CachedDto_.type, type.name, CASE_INSENSITIVE)
            order(EventV2CachedDto_.additionTimeString, QueryBuilder.DESCENDING)
        }.findFirst()

    private fun containsById(id: Long): Boolean =
        box.query {
            equal(EventV2CachedDto_.id, id)
        }.count() > 0
}
