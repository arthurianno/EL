package com.elta.android.data.features.diary.events.datasource

import com.elta.android.common.mapper.Mapper
import com.elta.android.data.common.checkNetwork
import com.elta.android.data.features.common.cache.updateCache
import com.elta.android.data.features.common.isTheLastPage
import com.elta.android.data.features.common.storage.SyncStorage
import com.elta.android.data.features.diary.events.api.EventsApi
import com.elta.android.data.features.diary.events.cache.EventsCache
import com.elta.android.data.features.diary.events.cache.dto.EventCachedDto
import com.elta.android.data.features.diary.events.dto.EventDto
import com.elta.android.data.features.diary.events.dto.EventsDto
import com.nullgr.core.date.toTimestamp
import com.nullgr.core.hardware.NetworkChecker
import io.reactivex.Observable
import java.util.Date
import javax.inject.Inject

class EventsRemoteDataSource @Inject constructor(
    private val toCacheMapper: Mapper<EventDto, EventCachedDto>,
    private val eventsCache: EventsCache,
    private val syncStorage: SyncStorage,
    private val checker: NetworkChecker,
    private val api: EventsApi
) : EventsDataSource {

    override fun getEvents(): Observable<List<EventDto>> =
        getDataByPage(PAGE, PAGE_SIZE).checkNetwork(checker)
            .doOnNext { syncStorage.lastEventsSync = Date().toTimestamp() }
            .map(EventsDto::events)
            .doOnNext { events -> updateCache(events, eventsCache, toCacheMapper) }

    override fun getEvents(start: Date, end: Date): Observable<List<EventDto>> =
        getEvents()

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