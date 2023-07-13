package com.elta.android.domain.features.diary.events.repository

import android.graphics.Bitmap
import android.net.Uri
import com.elta.android.domain.features.diary.events.model.Event
import com.elta.android.domain.features.diary.events.model.EventType
import com.elta.android.domain.features.diary.home.model.GlucoseSharingInfo
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import org.threeten.bp.LocalDateTime

@Suppress("ComplexInterface", "TooManyFunctions")
interface EventsRepository {

    fun getEvents(): Observable<List<Event>>

    fun getEvents(start: LocalDateTime, end: LocalDateTime): Observable<List<Event>>

    fun getEventById(id: String): Single<Event>

    fun getLastEvent(eventType: EventType): Single<Event>

    fun countEvents(): Single<Long>

    fun addEvent(event: Event): Completable

    fun addEvents(events: List<Event>): Completable

    fun updateEvent(event: Event): Completable

    fun deleteEvent(event: Event): Completable

    fun sync(): Completable

    fun getShareEventUri(sharingInfo: GlucoseSharingInfo): Single<Uri>

    fun saveShareEventBitmap(sharingInfo: GlucoseSharingInfo, bitmap: Bitmap): Single<Uri>
}
