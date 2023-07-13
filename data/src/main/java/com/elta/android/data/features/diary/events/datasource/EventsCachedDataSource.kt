package com.elta.android.data.features.diary.events.datasource

import com.elta.android.common.mapper.Mapper
import com.elta.android.data.features.common.cache.Cache
import com.elta.android.data.features.common.cache.CommonConditions
import com.elta.android.data.features.diary.events.cache.EventsConditions
import com.elta.android.data.features.diary.events.cache.dto.EventCachedDto
import com.elta.android.data.features.diary.events.dto.EventDto
import com.elta.android.data.features.diary.events.dto.EventTypeDto
import com.elta.android.data.features.diary.events.dto.SimpleEventDto
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import javax.inject.Inject
import org.threeten.bp.LocalDateTime

class EventsCachedDataSource @Inject constructor(
    private val toCacheMapper: Mapper<EventDto, EventCachedDto>,
    private val fromCacheMapper: Mapper<EventCachedDto, EventDto>,
    private val cache: Cache<EventCachedDto>
) : EventsDataSource {

    override fun getEvents(): Observable<List<EventDto>> =
        Observable.fromCallable {
            cache.getAll(CommonConditions.All)
        }.map(fromCacheMapper::mapFromObjects)

    override fun getEvents(start: LocalDateTime, end: LocalDateTime): Observable<List<EventDto>> =
        Observable.fromCallable {
            cache.getAll(EventsConditions.ByPeriod(start, end))
        }.map(fromCacheMapper::mapFromObjects)

    override fun getEventById(id: String): Single<EventDto> =
        Single.fromCallable {
            cache.getAll(CommonConditions.ByIds(listOf(id.hashCode().toLong())))
        }.map(fromCacheMapper::mapFromObjects).map {
            if (it.isNotEmpty()) {
                it[0]
            } else {
                throw IllegalArgumentException("Event with $id doesn't exist.")
            }
        }

    override fun getEventsById(ids: List<Long>): Observable<List<EventDto>> =
        Observable.fromCallable {
            cache.getAll(CommonConditions.ByIds(ids))
        }.map(fromCacheMapper::mapFromObjects)

    override fun getLastEvent(eventType: EventTypeDto): Single<EventDto> =
        Single.fromCallable {
            cache.get(EventsConditions.LastByType(eventType))
        }.map(fromCacheMapper::mapFromObject)

    override fun countEvents(): Single<Long> =
        Single.fromCallable { cache.count(CommonConditions.All) }

    override fun addEvents(events: List<EventDto>): Completable =
        Completable.fromCallable {
            cache.add(toCacheMapper.mapFromObjects(events))
        }

    override fun updateEvents(events: List<EventDto>): Completable =
        Completable.fromCallable {
            cache.update(toCacheMapper.mapFromObjects(events))
        }

    override fun deleteEvents(events: List<SimpleEventDto>): Completable =
        Completable.fromCallable {
            cache.delete(CommonConditions.ByIds(events.map { it.id.hashCode().toLong() }))
        }
}
