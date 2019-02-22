package com.elta.android.data.features.diary.events.repository

import com.elta.android.common.di.qualifires.Cache
import com.elta.android.common.di.qualifires.Remote
import com.elta.android.common.mapper.Mapper
import com.elta.android.data.features.diary.events.datasource.EventsDataSource
import com.elta.android.data.features.diary.events.dto.EventDto
import com.elta.android.domain.features.diary.events.model.Event
import com.elta.android.domain.features.diary.events.repository.EventsRepository
import io.reactivex.Observable
import java.util.Date
import javax.inject.Inject

class EventsDataRepository @Inject constructor(
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
}