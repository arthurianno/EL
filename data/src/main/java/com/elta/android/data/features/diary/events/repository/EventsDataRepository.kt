package com.elta.android.data.features.diary.events.repository

import android.graphics.Bitmap
import android.net.Uri
import com.elta.android.common.di.qualifires.Cache
import com.elta.android.common.di.qualifires.Remote
import com.elta.android.common.errors.NotFoundItemError
import com.elta.android.common.mapper.Mapper
import com.elta.android.data.features.common.dto.StateDto
import com.elta.android.data.features.common.storage.FileStorage
import com.elta.android.data.features.devices.dto.GlucometerEventDto
import com.elta.android.data.features.diary.events.datasource.EventsDataSource
import com.elta.android.data.features.diary.events.datasource.cache.EventsCacheDataSource
import com.elta.android.data.features.diary.events.dto.EventTypeDto
import com.elta.android.data.features.diary.events.dto.EventTypeDto.Companion.toEventTypeDto
import com.elta.android.data.features.diary.events.dto.SimpleEventDto
import com.elta.android.data.features.diary.events.dto.v2.EventV2Dto
import com.elta.android.data.features.diary.events.extensions.EVENTS_DIR_NAME
import com.elta.android.data.features.diary.events.extensions.buildFileName
import com.elta.android.data.features.diary.events.mapper.toDomain
import com.elta.android.data.features.sync.manger.LocalSyncManager
import com.elta.android.domain.features.devices.model.GlucometerEvent
import com.elta.android.domain.features.diary.events.model.EventType
import com.elta.android.domain.features.diary.events.model.EventV2
import com.elta.android.domain.features.diary.medicines.model.MedicamentInsulinType
import com.elta.android.domain.features.diary.events.repository.EventsRepository
import com.elta.android.domain.features.diary.home.model.GlucoseSharingInfo
import com.elta.android.domain.features.diary.medicines.repository.InsulinMedicamentRepository
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import org.threeten.bp.LocalDateTime
import timber.log.Timber
import javax.inject.Inject

@Suppress("MaxLineLength")
class EventsDataRepository @Inject constructor(
    private val toDtoMapper: Mapper<EventV2, EventV2Dto>,
    @Remote private val remoteSource: EventsDataSource,
    @Cache private val cacheSource: EventsCacheDataSource,
    private val insulinMedicamentRepository: InsulinMedicamentRepository,
    private val syncManager: LocalSyncManager,
    private val fileStorage: FileStorage,
    private val eventsFromGlucometerMapper: Mapper<GlucometerEvent, EventV2>
) : EventsRepository {

    override fun getEvents(): Observable<List<EventV2>> =
        cacheSource.getEvents()
            .map{ events ->
            events.map { it.toDomain() }
        }

    override fun getEvents(start: LocalDateTime, end: LocalDateTime): Observable<List<EventV2>> =
        cacheSource.getEvents(start, end)
            .map { events ->
            events.map { it.toDomain() }
        }

    override fun getEventById(id: String): Single<EventV2> =
        cacheSource.getEventById(id)
            .map { event ->
            event.toDomain()
        }

    override fun getLastEvent(eventType: EventType): Single<EventV2> {

        val eventObservable = cacheSource.getLastEvent(eventType.toEventTypeDto())
            .map { event ->
                event.toDomain()
            }

        return when(eventType) {
            EventType.Medicaments -> Single.zip(
                eventObservable,
                Single.fromObservable(
                    insulinMedicamentRepository.getInsulinMedicaments(MedicamentInsulinType.allMedicament()))
                ) { event, insulin ->
                    if (event.insulinMedicament == null || insulin.contains(event.insulinMedicament))
                        event
                    else throw NotFoundItemError

                }
            else -> eventObservable
        }
    }

    override fun countEvents(): Single<Long> =
        cacheSource.countEvents()

    override fun addEvent(event: EventV2): Completable =
        Single.fromCallable { listOf(toDtoMapper.mapFromObject(event)) }
            .flatMapCompletable {
                cacheSource.addEvents(it)
                    .andThen(
                        remoteSource.addEvents(it)
                            .onErrorResumeNext { th ->
                                Timber.e(th, "<<<<< AddEvent error >>>>>")
                                syncManager.saveAsCreated(event)
                            }
                    )
            }

    override fun addEvents(events: List<EventV2>): Completable =
        Single.fromCallable { toDtoMapper.mapFromObjects(events) }
            .flatMapCompletable {
                cacheSource.addEvents(it)
                    .andThen(
                        remoteSource.addEvents(it)
                            .onErrorResumeNext { th ->
                                Timber.e(th, "<<<<< AddEvents error >>>>>")
                                syncManager.saveAsCreated(events)
                            }
                    )
            }

    override fun addEventFromGlucometer(glucometerEvents: List<GlucometerEvent>) {
        val events = glucometerEvents.map { event ->
            eventsFromGlucometerMapper.mapFromObject(event)
        }
        addEvents(events)
    }

    override fun updateEvent(event: EventV2): Completable =
        Single.fromCallable { listOf(toDtoMapper.mapFromObject(event)) }
            .flatMapCompletable {
                cacheSource.updateEvents(it)
                    .andThen(
                        remoteSource.updateEvents(it)
                            .onErrorResumeNext { th ->
                                Timber.e(th, "<<<<< UpdateEvent error >>>>>")
                                syncManager.saveAsUpdated(event)
                            }
                    )
            }

    override fun deleteEvent(event: EventV2): Completable =
        Single.fromCallable {
            listOf(
                SimpleEventDto(
                    event.id,
                    event.type.toEventTypeDto()
                )
            )
        }
            .flatMapCompletable {
                cacheSource.deleteEvents(it)
                    .andThen(
                        remoteSource.deleteEvents(it)
                            .onErrorResumeNext { syncManager.saveAsDeleted(event) }
                    )
            }

    override fun sync(): Completable =
        remoteSource.getEvents()
            .flatMap {
                syncManager.needToSync<EventV2>()
                    .flatMapObservable { needToSync ->
                        if (needToSync) {
                            syncManager.getNotSynced<EventV2>()
                        } else {
                            Observable.empty()
                        }
                    }
            }
            .flatMap { toSync ->
                val toDelete = toSync.filter { it.state == StateDto.DELETED }
                    .map {
                        SimpleEventDto(
                            it.secondaryId,
                            EventTypeDto.valueOf(checkNotNull(it.meta))
                        )
                    }

                if (toDelete.isEmpty()) {
                    Observable.just(toSync)
                } else {
                    remoteSource.deleteEvents(toDelete)
                        .andThen(syncManager.setAllSynced<EventV2>(StateDto.DELETED))
                        .andThen(Observable.just(toSync))
                }
            }
            .flatMap { toSync ->
                val toCreate = toSync.filter { it.state == StateDto.CREATED }
                    .map { it.secondaryId.hashCode().toLong() }

                if (toCreate.isEmpty()) {
                    Observable.just(toSync)
                } else {
                    cacheSource.getEventsById(toCreate)
                        .flatMapCompletable { remoteSource.addEvents(it) }
                        .andThen(syncManager.setAllSynced<EventV2>(StateDto.CREATED))
                        .andThen(Observable.just(toSync))
                }
            }
            .flatMapCompletable { toSync ->
                val toUpdate = toSync.filter { it.state == StateDto.UPDATED }
                    .map { it.secondaryId.hashCode().toLong() }

                if (toUpdate.isEmpty()) {
                    Completable.complete()
                } else {
                    cacheSource.getEventsById(toUpdate)
                        .flatMapCompletable { remoteSource.updateEvents(it) }
                        .andThen(syncManager.setAllSynced<EventV2>(StateDto.UPDATED))
                }
            }

    override fun getShareEventUri(sharingInfo: GlucoseSharingInfo): Single<Uri> =
        Single.fromCallable {
            fileStorage.getFile(
                fileName = buildFileName(sharingInfo),
                directoryName = EVENTS_DIR_NAME
            )
        }
            .map {
                if (it.exists()) {
                    fileStorage.getFileUri(it)
                } else {
                    Uri.EMPTY
                }
            }

    override fun saveShareEventBitmap(
        sharingInfo: GlucoseSharingInfo,
        bitmap: Bitmap,
    ): Single<Uri> =
        Single.fromCallable {
            fileStorage.saveBitmap(
                fileName = buildFileName(sharingInfo),
                directoryName = EVENTS_DIR_NAME,
                bitmap = bitmap
            )
        }
            .map { fileStorage.getFileUri(it) }
}
