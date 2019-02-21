package com.elta.android.data.features.diary.events.datasource

import com.elta.android.common.mapper.Mapper
import com.elta.android.data.features.common.cache.CommonConditions
import com.elta.android.data.features.diary.events.dto.EventDto
import com.elta.android.data.features.diary.events.cache.EventsCache
import com.elta.android.data.features.diary.events.cache.EventsConditions
import com.elta.android.data.features.diary.events.cache.dto.EventCachedDto
import io.reactivex.Observable
import java.util.Date
import javax.inject.Inject

class EventsCachedDataSource @Inject constructor(
    private val fromCacheMapper: Mapper<EventCachedDto, EventDto>,
    private val cache: EventsCache
) : EventsDataSource {

    override fun getEvents(): Observable<List<EventDto>> =
        Observable.fromCallable {
            cache.get(CommonConditions.All)
        }.map(fromCacheMapper::mapFromObjects)

    override fun getEvents(start: Date, end: Date): Observable<List<EventDto>> =
        Observable.fromCallable {
            cache.get(EventsConditions.ByPeriod(start, end))
        }.map(fromCacheMapper::mapFromObjects)
}