package com.elta.android.domain.features.diary.events.repository

import android.graphics.Bitmap
import android.net.Uri
import com.elta.android.domain.features.devices.model.GlucometerEvent
import com.elta.android.domain.features.diary.events.model.EventType
import com.elta.android.domain.features.diary.events.model.EventV2
import com.elta.android.domain.features.diary.home.model.GlucoseSharingInfo
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import org.threeten.bp.LocalDateTime

@Suppress("ComplexInterface", "TooManyFunctions")
interface EventsRepository {

    fun getEvents(): Observable<List<EventV2>>

    fun getEvents(start: LocalDateTime, end: LocalDateTime): Observable<List<EventV2>>

    fun getEventById(id: String): Single<EventV2>

    fun getLastEvent(eventType: EventType): Single<EventV2>

    fun countEvents(): Single<Long>

    fun addEvent(event: EventV2): Completable

    fun addEvents(events: List<EventV2>): Completable

    suspend fun addEventFromGlucometer(glucometerEvents: List<GlucometerEvent>)

    fun updateEvent(event: EventV2): Completable

    fun deleteEvent(event: EventV2): Completable

    fun sync(): Completable

    fun getShareEventUri(sharingInfo: GlucoseSharingInfo): Single<Uri>

    fun saveShareEventBitmap(sharingInfo: GlucoseSharingInfo, bitmap: Bitmap): Single<Uri>
    suspend fun addEventsSuspend(events: List<EventV2>)
}
