package com.elta.android.domain.features.diary.events.repository

import com.elta.android.domain.features.diary.events.model.Event
import io.reactivex.Observable
import java.util.Date

interface EventsRepository {

    fun getEvents(): Observable<List<Event>>

    fun getEvents(start: Date, end: Date): Observable<List<Event>>
}