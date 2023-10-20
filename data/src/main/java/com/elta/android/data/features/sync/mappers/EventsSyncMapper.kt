package com.elta.android.data.features.sync.mappers

import com.elta.android.data.features.common.dto.StateDto
import com.elta.android.data.features.sync.cache.dto.LocalSyncCachedDto
import com.elta.android.data.features.sync.configuration.LocalSyncMapper
import com.elta.android.domain.features.diary.events.model.EventV2

class EventsSyncMapper : LocalSyncMapper<EventV2> {

    override fun mapToUpdate(entity: EventV2): LocalSyncCachedDto =
        LocalSyncCachedDto(
            id = entity.id.hashCode().toLong(),
            secondaryId = entity.id,
            className = EventV2::class.java.simpleName,
            state = StateDto.UPDATED
        )

    override fun mapToCreate(entity: EventV2): LocalSyncCachedDto =
        LocalSyncCachedDto(
            id = entity.id.hashCode().toLong(),
            secondaryId = entity.id,
            className = EventV2::class.java.simpleName,
            state = StateDto.CREATED
        )

    override fun mapToDelete(entity: EventV2): LocalSyncCachedDto =
        LocalSyncCachedDto(
            id = entity.id.hashCode().toLong(),
            secondaryId = entity.id,
            className = EventV2::class.java.simpleName,
            state = StateDto.DELETED,
            meta = entity.type.toString()
        )
}
