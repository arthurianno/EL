package com.elta.android.data.features.diary.events.repository

import com.elta.android.common.di.qualifires.Cache
import com.elta.android.common.di.qualifires.Remote
import com.elta.android.common.errors.NetworkConnectionError
import com.elta.android.common.mapper.Mapper
import com.elta.android.data.features.diary.events.datasource.EventsDataSource
import com.elta.android.data.features.diary.events.dto.EventDto
import com.elta.android.domain.features.diary.events.model.Event
import com.elta.android.domain.features.diary.events.model.EventType
import com.elta.android.domain.features.diary.events.repository.EventsRepository
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import java.util.Date
import javax.inject.Inject

// TODO: CRUD logic should be improved
class EventsDataRepository @Inject constructor(
    private val toDtoMapper: Mapper<Event, EventDto>,
    private val toDomainMapper: Mapper<EventDto, Event>,
    @Remote private val remoteSource: EventsDataSource,
    @Cache private val cacheSource: EventsDataSource
) : EventsRepository {

    override fun getEvents(): Observable<List<Event>> =
        remoteSource.getEvents()
            .flatMap { cacheSource.getEvents() }
            .map(toDomainMapper::mapFromObjects)

    override fun getEvents(start: Date, end: Date): Observable<List<Event>> =
        remoteSource.getEvents(start, end)
            .flatMap { cacheSource.getEvents(start, end) }
            .map(toDomainMapper::mapFromObjects)

    override fun addEvent(event: Event): Completable =
        Single.fromCallable { listOf(toDtoMapper.mapFromObject(event)) }
            .flatMapCompletable {
                cacheSource.addEvents(it)
                    .andThen(remoteSource.addEvents(it)
                        .onErrorComplete { error -> error is NetworkConnectionError }
                    )
            }


    override fun updateEvent(event: Event): Completable =
        Single.fromCallable { listOf(toDtoMapper.mapFromObject(event)) }
            .flatMapCompletable {
                cacheSource.updateEvents(it)
                    .andThen(remoteSource.updateEvents(it)
                        .onErrorComplete { error -> error is NetworkConnectionError }
                    )
            }

    override fun deleteEvent(eventId: String, type: EventType): Completable =
        Single.fromCallable { listOf(toDtoMapper.mapFromObject(event)) }
            .flatMapCompletable {
                cacheSource.deleteEvents(it)
                    .andThen(remoteSource.deleteEvents(it)
                        .onErrorComplete { error -> error is NetworkConnectionError }
                    )
            }
}