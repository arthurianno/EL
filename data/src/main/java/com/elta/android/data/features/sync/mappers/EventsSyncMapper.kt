package com.elta.android.data.features.sync.mappers

import com.elta.android.data.features.common.dto.StateDto
import com.elta.android.data.features.sync.cache.dto.LocalSyncCachedDto
import com.elta.android.data.features.sync.configuration.LocalSyncMapper
import com.elta.android.domain.features.diary.events.model.Event

class EventsSyncMapper : LocalSyncMapper<Event> {

    override fun mapToUpdate(entity: Event): LocalSyncCachedDto =
        LocalSyncCachedDto(
            id = entity.id.hashCode().toLong(),
            secondaryId = entity.id,
            className = Event::class.java.simpleName,
            state = StateDto.UPDATED
        )

    override fun mapToCreate(entity: Event): LocalSyncCachedDto =
        LocalSyncCachedDto(
            id = entity.id.hashCode().toLong(),
            secondaryId = entity.id,
            className = Event::class.java.simpleName,
            state = StateDto.CREATED
        )

    override fun mapToDelete(entity: Event): LocalSyncCachedDto =
        LocalSyncCachedDto(
            id = entity.id.hashCode().toLong(),
            secondaryId = entity.id,
            className = Event::class.java.simpleName,
            state = StateDto.DELETED,
            meta = entity.type.toString()
        )
}
