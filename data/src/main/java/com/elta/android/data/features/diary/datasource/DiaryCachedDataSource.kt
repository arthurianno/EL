package com.elta.android.data.features.diary.datasource

import com.elta.android.common.mapper.Mapper
import com.elta.android.data.features.common.cache.CommonConditions
import com.elta.android.data.features.diary.cache.EventsCache
import com.elta.android.data.features.diary.cache.EventsConditions
import com.elta.android.data.features.diary.cache.TagsCache
import com.elta.android.data.features.diary.cache.dto.EventCachedDto
import com.elta.android.data.features.diary.cache.dto.TagCachedDto
import com.elta.android.data.features.diary.dto.event.EventDto
import com.elta.android.data.features.diary.dto.tag.TagDto
import io.reactivex.Observable
import java.util.Date
import javax.inject.Inject

class DiaryCachedDataSource @Inject constructor(
    private val tagsFromCacheMapper: Mapper<TagCachedDto, TagDto>,
    private val tagsCache: TagsCache,
    private val eventsFromCacheMapper: Mapper<EventCachedDto, EventDto>,
    private val eventsCache: EventsCache
) : DiaryDataSource {

    override fun getEvents(): Observable<List<EventDto>> =
        Observable.fromCallable {
            eventsCache.get(CommonConditions.All)
        }.map(eventsFromCacheMapper::mapFromObjects)

    override fun getEvents(start: Date, end: Date): Observable<List<EventDto>> =
        Observable.fromCallable {
            eventsCache.get(EventsConditions.ByPeriod(start, end))
        }.map(eventsFromCacheMapper::mapFromObjects)

    override fun getTags(): Observable<List<TagDto>> =
        Observable.fromCallable {
            tagsCache.get(CommonConditions.All)
        }.map(tagsFromCacheMapper::mapFromObjects)
}