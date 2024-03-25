package com.elta.android.data.features.diary.events.datasource.remote

import com.elta.android.common.mapper.Mapper
import com.elta.android.common.utils.timestamp
import com.elta.android.data.features.common.cache.Cache
import com.elta.android.data.features.common.cache.updateCache
import com.elta.android.data.features.common.isTheLastPage
import com.elta.android.data.features.common.storage.SyncStorage
import com.elta.android.data.features.diary.events.api.EventsV2Api
import com.elta.android.data.features.diary.events.cache.dto.v2.EventV2CachedDto
import com.elta.android.data.features.diary.events.datasource.EventsDataSource
import com.elta.android.data.features.diary.events.dto.SimpleEventDto
import com.elta.android.data.features.diary.events.dto.v2.EventV2Dto
import com.elta.android.data.features.diary.events.dto.v2.EventsV2Dto
import com.elta.android.domain.features.FeatureToggles
import io.reactivex.Completable
import io.reactivex.Observable
import org.threeten.bp.LocalDateTime
import javax.inject.Inject

class EventsRemoteDataSource @Inject constructor(
    private val toCacheMapper: Mapper<EventV2Dto, EventV2CachedDto>,
    private val eventsCache: Cache<EventV2CachedDto>,
    private val syncStorage: SyncStorage,
    private val api: EventsV2Api
) : EventsDataSource {

    override fun getEvents(): Observable<List<EventV2Dto>> =
        getDataByPage(PAGE, PAGE_SIZE)
            .doOnNext { syncStorage.lastEventsSync = timestamp() }
            .map(EventsV2Dto::events)
            .doOnNext { events -> updateCache(events, eventsCache, toCacheMapper) }

    override fun getEvents(start: LocalDateTime, end: LocalDateTime): Observable<List<EventV2Dto>> =
        getEvents()

    override fun addEvents(events: List<EventV2Dto>): Completable =
        api.addEvents(!FeatureToggles.isEnableIiotSdkFeature, events)
            .flatMapCompletable { Completable.complete() }

    override suspend fun addEventsSuspend(events: List<EventV2Dto>) {
        api.addEventsSuspend(!FeatureToggles.isEnableIiotSdkFeature, events)
    }


    override fun updateEvents(events: List<EventV2Dto>): Completable =
        api.updateEvents(events)
            .flatMapCompletable { Completable.complete() }

    override fun deleteEvents(events: List<SimpleEventDto>): Completable =
        api.deleteEvents(events)
            .flatMapCompletable { Completable.complete() }

    private fun getDataByPage(page: Int, size: Int): Observable<EventsV2Dto> =
        api.getEvents(syncStorage.lastEventsSync, true, page, size)
            .concatMap { data ->
                val meta = data.meta
                val nextPage = meta.currentPage + 1
                when (meta.isTheLastPage()) {
                    true -> Observable.just(data)
                    else -> Observable.just(data).concatWith(getDataByPage(nextPage, meta.pageSize))
                }
            }
            .collectInto(mutableListOf<EventsV2Dto>()) { list, data -> list.add(data) }
            .map { list ->
                val allData = list.map { it.events }.flatten()
                val lastMeta = list.last().meta
                EventsV2Dto(allData, lastMeta)
            }
            .toObservable()

    private companion object {
        const val PAGE = 1
        const val PAGE_SIZE = 150
    }
}
