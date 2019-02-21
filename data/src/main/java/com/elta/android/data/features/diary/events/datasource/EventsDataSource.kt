package com.elta.android.data.features.diary.events.datasource

import com.elta.android.data.features.diary.events.dto.EventDto
import io.reactivex.Observable
import java.util.Date

interface EventsDataSource {

    fun getEvents(): Observable<List<EventDto>>

    fun getEvents(start: Date, end: Date): Observable<List<EventDto>>
}