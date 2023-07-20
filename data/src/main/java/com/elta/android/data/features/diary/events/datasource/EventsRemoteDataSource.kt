package com.elta.android.data.features.diary.events.datasource

import com.elta.android.common.mapper.Mapper
import com.elta.android.common.utils.timestamp
import com.elta.android.data.features.common.cache.Cache
import com.elta.android.data.features.common.cache.updateCache
import com.elta.android.data.features.common.isTheLastPage
import com.elta.android.data.features.common.storage.SyncStorage
import com.elta.android.data.features.diary.events.api.EventsApi
import com.elta.android.data.features.diary.events.cache.dto.EventCachedDto
import com.elta.android.data.features.diary.events.dto.EventDto
import com.elta.android.data.features.diary.events.dto.EventTypeDto
import com.elta.android.data.features.diary.events.dto.EventsDto
import com.elta.android.data.features.diary.events.dto.SimpleEventDto
import com.elta.android.domain.features.FeatureToggles
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import org.threeten.bp.LocalDateTime
import javax.inject.Inject

class EventsRemoteDataSource @Inject constructor(
    private val toCacheMapper: Mapper<EventDto, EventCachedDto>,
    private val eventsCache: Cache<EventCachedDto>,
    private val syncStorage: SyncStorage,
    private val api: EventsApi
) : EventsDataSource {

    override fun getEvents(): Observable<List<EventDto>> =
        getDataByPage(PAGE, PAGE_SIZE)
            .doOnNext { syncStorage.lastEventsSync = timestamp() }
            .map(EventsDto::events)
            .doOnNext { events -> updateCache(events, eventsCache, toCacheMapper) }

    override fun getEvents(start: LocalDateTime, end: LocalDateTime): Observable<List<EventDto>> =
        getEvents()

    override fun getEventById(id: String): Single<EventDto> =
        throw UnsupportedOperationException("${this::class.java.simpleName} doesn't support getting event by id.")

    override fun getEventsById(ids: List<Long>): Observable<List<EventDto>> {
        throw UnsupportedOperationException("${this::class.java.simpleName} doesn't support getting events by id.")
    }

    override fun getLastEvent(eventType: EventTypeDto): Single<EventDto> {
        throw UnsupportedOperationException("${this::class.java.simpleName} doesn't support getting event by type.")
    }

    override fun countEvents(): Single<Long> {
        throw UnsupportedOperationException("${this::class.java.simpleName} doesn't support countEvents.")
    }

    override fun addEvents(events: List<EventDto>): Completable =
        api.addEvents(FeatureToggles.isEnableIiotSdkFeature, events)
            .flatMapCompletable { Completable.complete() }

    override fun updateEvents(events: List<EventDto>): Completable =
        api.updateEvents(events)
            .flatMapCompletable { Completable.complete() }

    override fun deleteEvents(events: List<SimpleEventDto>): Completable =
        api.deleteEvents(events)
            .flatMapCompletable { Completable.complete() }

    private fun getDataByPage(page: Int, size: Int): Observable<EventsDto> =
        api.getEvents(syncStorage.lastEventsSync, page, size)
            .concatMap { data ->
                val meta = data.meta
                val nextPage = meta.currentPage + 1
                when (meta.isTheLastPage()) {
                    true -> Observable.just(data)
                    else -> Observable.just(data).concatWith(getDataByPage(nextPage, meta.pageSize))
                }
            }
            .collectInto(mutableListOf<EventsDto>()) { list, data -> list.add(data) }
            .map { list ->
                val allData = list.map { it.events }.flatten()
                val lastMeta = list.last().meta
                EventsDto(allData, lastMeta)
            }
            .toObservable()

    private companion object {
        const val PAGE = 1
        const val PAGE_SIZE = 150
    }
}
