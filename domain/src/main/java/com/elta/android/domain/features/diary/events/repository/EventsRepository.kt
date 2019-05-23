package com.elta.android.domain.features.diary.events.repository

import android.graphics.Bitmap
import com.elta.android.domain.features.diary.events.model.Event
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import java.io.File
import java.util.Date

interface EventsRepository {

    fun getEvents(): Observable<List<Event>>

    fun getEvents(start: Date, end: Date): Observable<List<Event>>

    fun getEventById(id: String): Single<Event>

    fun addEvent(event: Event): Completable

    fun addEvents(events: List<Event>): Completable

    fun updateEvent(event: Event): Completable

    fun deleteEvent(event: Event): Completable

    fun sync(): Completable

    fun saveEventBitmap(eventHash: String, path: String, bitmap: Bitmap): Single<File>
}