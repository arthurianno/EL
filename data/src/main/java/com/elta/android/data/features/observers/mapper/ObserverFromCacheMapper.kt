package com.elta.android.data.features.observers.mapper

import com.elta.android.common.mapper.Mapper
import com.elta.android.data.features.common.dto.StateDto
import com.elta.android.data.features.observers.cache.dto.ObserverCacheDto
import com.elta.android.data.features.observers.dto.ObserverDto
import com.elta.android.data.features.observers.dto.ObserverStatusDto
import javax.inject.Inject

class ObserverFromCacheMapper @Inject constructor() : Mapper<ObserverCacheDto, ObserverDto> {
    override fun mapFromObject(source: ObserverCacheDto): ObserverDto =
        with(source) {
            ObserverDto(
                id = secondaryId,
                email = email,
                name = name,
                status = ObserverStatusDto.valueOf(status),
                modificationTime = modificationTime,
                state = StateDto.valueOf(state)
            )
        }
}