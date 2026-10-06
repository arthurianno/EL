package com.elta.android.data.features.diary.events.datasource.cache

import com.elta.android.data.features.diary.events.datasource.EventsDataSource
import com.elta.android.data.features.diary.events.dto.EventTypeDto
import com.elta.android.data.features.diary.events.dto.v2.EventV2Dto
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

interface EventsCacheDataSource: EventsDataSource {

    fun getEventById(id: String): Single<EventV2Dto>

    fun getEventsById(ids: List<Long>): Observable<List<EventV2Dto>>

    fun getLastEvent(eventType: EventTypeDto): Single<EventV2Dto>

    fun countEvents(): Single<Long>

    fun updateEventsFromLocalEdit(events: List<EventV2Dto>): Completable
}
