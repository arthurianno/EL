package com.elta.android.data.features.observers.mapper

import com.elta.android.common.mapper.Mapper
import com.elta.android.data.features.observers.cache.dto.ObserverCacheDto
import com.elta.android.data.features.observers.dto.ObserverDto
import javax.inject.Inject

class ObserverToCacheMapper @Inject constructor() : Mapper<ObserverDto, ObserverCacheDto> {
    override fun mapFromObject(source: ObserverDto): ObserverCacheDto =
        with(source) {
            ObserverCacheDto(
                id = id.hashCode().toLong(),
                secondaryId = id,
                email = email,
                name = name,
                status = status.name,
                modificationTime = modificationTime,
                state = state.name
            )
        }
}