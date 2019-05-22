package com.elta.android.data.features.diary.events.repository

import com.elta.android.common.di.qualifires.Cache
import com.elta.android.common.di.qualifires.Remote
import com.elta.android.common.mapper.Mapper
import com.elta.android.data.features.common.dto.StateDto
import com.elta.android.data.features.diary.events.datasource.EventsDataSource
import com.elta.android.data.features.diary.events.dto.EventDto
import com.elta.android.data.features.diary.events.dto.EventTypeDto
import com.elta.android.data.features.diary.events.dto.SimpleEventDto
import com.elta.android.data.features.sync.manger.LocalSyncManager
import com.elta.android.domain.features.diary.events.model.Event
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
    @Cache private val cacheSource: EventsDataSource,
    private val syncManager: LocalSyncManager
) : EventsRepository {

    override fun getEvents(): Observable<List<Event>> =
        cacheSource.getEvents()
            .map(toDomainMapper::mapFromObjects)

    override fun getEvents(start: Date, end: Date): Observable<List<Event>> =
        cacheSource.getEvents(start, end)
            .map(toDomainMapper::mapFromObjects)

    override fun getEventById(id: String): Single<Event> =
        cacheSource.getEventById(id)
            .map(toDomainMapper::mapFromObject)

    override fun addEvent(event: Event): Completable =
        Single.fromCallable { listOf(toDtoMapper.mapFromObject(event)) }
            .flatMapCompletable {
                cacheSource.addEvents(it)
                    .andThen(
                        remoteSource.addEvents(it)
                            .onErrorResumeNext { syncManager.saveAsCreated(event) }
                    )
            }

    override fun addEvents(events: List<Event>): Completable =
        Single.fromCallable { toDtoMapper.mapFromObjects(events) }
            .flatMapCompletable {
                cacheSource.addEvents(it)
                    .andThen(
                        remoteSource.addEvents(it)
                            .onErrorResumeNext { syncManager.saveAsCreated(events) }
                    )
            }

    override fun updateEvent(event: Event): Completable =
        Single.fromCallable { listOf(toDtoMapper.mapFromObject(event)) }
            .flatMapCompletable {
                cacheSource.updateEvents(it)
                    .andThen(remoteSource.updateEvents(it)
                        .onErrorResumeNext { syncManager.saveAsUpdated(event) }
                    )
            }

    override fun deleteEvent(event: Event): Completable =
        Single.fromCallable { listOf(SimpleEventDto(event.id, EventTypeDto.valueOf(event.type.name))) }
            .flatMapCompletable {
                cacheSource.deleteEvents(it)
                    .andThen(remoteSource.deleteEvents(it)
                        .onErrorResumeNext { syncManager.saveAsDeleted(event) }
                    )
            }

    override fun sync(): Completable =
        remoteSource.getEvents()
            .flatMap {
                syncManager.needToSync<Event>()
                    .flatMapObservable { needToSync ->
                        when (needToSync) {
                            true -> syncManager.getNotSynced<Event>()
                            else -> Observable.empty()
                        }
                    }
            }
            .flatMap { toSync ->
                remoteSource.deleteEvents(
                    toSync.filter { it.state == StateDto.DELETED }
                        .map { SimpleEventDto(it.secondaryId, EventTypeDto.valueOf(checkNotNull(it.meta))) }
                )
                    .andThen(syncManager.setAllSynced<Event>(StateDto.DELETED))
                    .andThen(Observable.just(toSync))
            }
            .flatMap { toSync ->
                val toCreate = toSync.filter { it.state == StateDto.CREATED }
                    .map { it.secondaryId.hashCode().toLong() }
                cacheSource.getEventsById(toCreate)
                    .flatMapCompletable { remoteSource.addEvents(it) }
                    .andThen(syncManager.setAllSynced<Event>(StateDto.CREATED))
                    .andThen(Observable.just(toSync))
            }
            .flatMapCompletable { toSync ->
                val toCreate = toSync.filter { it.state == StateDto.UPDATED }
                    .map { it.secondaryId.hashCode().toLong() }
                cacheSource.getEventsById(toCreate)
                    .flatMapCompletable { remoteSource.updateEvents(it) }
                    .andThen(syncManager.setAllSynced<Event>(StateDto.UPDATED))
            }
}