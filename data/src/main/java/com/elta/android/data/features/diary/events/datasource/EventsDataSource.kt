package com.elta.android.data.features.diary.events.datasource

import com.elta.android.data.features.diary.events.dto.SimpleEventDto
import com.elta.android.data.features.diary.events.dto.v2.EventV2Dto
import io.reactivex.Completable
import io.reactivex.Observable
import org.threeten.bp.LocalDateTime

interface EventsDataSource {

    fun getEvents(): Observable<List<EventV2Dto>>

    fun getEvents(start: LocalDateTime, end: LocalDateTime): Observable<List<EventV2Dto>>

    fun addEvents(events: List<EventV2Dto>): Completable

    fun updateEvents(events: List<EventV2Dto>): Completable

    fun deleteEvents(events: List<SimpleEventDto>): Completable
}
