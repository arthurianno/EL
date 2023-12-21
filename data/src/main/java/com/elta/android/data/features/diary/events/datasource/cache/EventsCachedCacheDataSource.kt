package com.elta.android.data.features.diary.events.datasource.cache

import com.elta.android.common.mapper.Mapper
import com.elta.android.data.features.common.cache.Cache
import com.elta.android.data.features.common.cache.CommonConditions
import com.elta.android.data.features.diary.events.cache.EventsConditions
import com.elta.android.data.features.diary.events.cache.dto.v2.EventV2CachedDto
import com.elta.android.data.features.diary.events.dto.EventTypeDto
import com.elta.android.data.features.diary.events.dto.SimpleEventDto
import com.elta.android.data.features.diary.events.dto.v2.EventV2Dto
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import org.threeten.bp.LocalDateTime
import javax.inject.Inject

class EventsCachedCacheDataSource @Inject constructor(
    private val toCacheMapper: Mapper<EventV2Dto, EventV2CachedDto>,
    private val fromCacheMapper: Mapper<EventV2CachedDto, EventV2Dto>,
    private val cache: Cache<EventV2CachedDto>
) : EventsCacheDataSource {

    override fun getEvents(): Observable<List<EventV2Dto>> =
        Observable.fromCallable {
            cache.getAll(CommonConditions.All)
        }.map(fromCacheMapper::mapFromObjects)

    override fun getEvents(start: LocalDateTime, end: LocalDateTime): Observable<List<EventV2Dto>> =
        Observable.fromCallable {
            cache.getAll(EventsConditions.ByPeriod(start, end))
        }.map(fromCacheMapper::mapFromObjects)

    override fun getEventById(id: String): Single<EventV2Dto> =
        Single.fromCallable {
            cache.getAll(CommonConditions.ByIds(listOf(id.hashCode().toLong())))
        }.map(fromCacheMapper::mapFromObjects).map {
            if (it.isNotEmpty()) {
                it[0]
            } else {
                throw IllegalArgumentException("Event with $id doesn't exist.")
            }
        }

    override fun getEventsById(ids: List<Long>): Observable<List<EventV2Dto>> =
        Observable.fromCallable {
            cache.getAll(CommonConditions.ByIds(ids))
        }.map(fromCacheMapper::mapFromObjects)

    override fun getLastEvent(eventType: EventTypeDto): Single<EventV2Dto> =
        Single.fromCallable {
            cache.get(EventsConditions.LastByType(eventType))
        }.map(fromCacheMapper::mapFromObject)

    override fun countEvents(): Single<Long> =
        Single.fromCallable { cache.count(CommonConditions.All) }

    override fun addEvents(events: List<EventV2Dto>): Completable =
        Completable.fromCallable {
            cache.add(toCacheMapper.mapFromObjects(events))
        }

    override fun updateEvents(events: List<EventV2Dto>): Completable =
        Completable.fromCallable {
            cache.update(toCacheMapper.mapFromObjects(events))
        }

    override fun deleteEvents(events: List<SimpleEventDto>): Completable =
        Completable.fromCallable {
            cache.delete(CommonConditions.ByIds(events.map { it.id.hashCode().toLong() }))
        }
}
