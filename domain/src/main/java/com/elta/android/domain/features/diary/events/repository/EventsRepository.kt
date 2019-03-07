package com.elta.android.domain.features.diary.events.repository

import com.elta.android.domain.features.diary.events.model.Event
import com.elta.android.domain.features.diary.events.model.EventType
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import java.util.Date

interface EventsRepository {

    fun getEvents(): Observable<List<Event>>

    fun getEvents(start: Date, end: Date): Observable<List<Event>>

    fun getEventById(id: String): Single<Event>

    fun addEvent(event: Event): Completable

    fun updateEvent(event: Event): Completable

    fun deleteEvent(eventId: String, type: EventType): Completable
}