package com.elta.android.data.features.diary.events.datasource

import com.elta.android.data.features.diary.events.dto.EventDto
import com.elta.android.data.features.diary.events.dto.SimpleEventDto
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import org.threeten.bp.LocalDateTime

interface EventsDataSource {

    fun getEvents(): Observable<List<EventDto>>

    fun getEvents(start: LocalDateTime, end: LocalDateTime): Observable<List<EventDto>>

    fun getEventById(id: String): Single<EventDto>

    fun getEventsById(ids: List<Long>): Observable<List<EventDto>>

    fun countEvents(): Single<Long>

    fun addEvents(events: List<EventDto>): Completable

    fun updateEvents(events: List<EventDto>): Completable

    fun deleteEvents(events: List<SimpleEventDto>): Completable
}
