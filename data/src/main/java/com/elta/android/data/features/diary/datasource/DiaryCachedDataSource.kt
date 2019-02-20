package com.elta.android.data.features.diary.datasource

import com.elta.android.common.mapper.Mapper
import com.elta.android.data.features.diary.cache.EventsCache
import com.elta.android.data.features.diary.cache.EventsConditions
import com.elta.android.data.features.diary.cache.dto.EventCachedDto
import com.elta.android.data.features.diary.dto.event.EventDto
import io.reactivex.Observable
import javax.inject.Inject

class DiaryCachedDataSource @Inject constructor(
    private val eventsFromCacheMapper: Mapper<EventCachedDto, EventDto>,
    private val eventsCache: EventsCache
) : DiaryDataSource {

    override fun getEvents(): Observable<List<EventDto>> =
        Observable.fromCallable {
            eventsCache.get(EventsConditions.All)
        }.map(eventsFromCacheMapper::mapFromObjects)
}