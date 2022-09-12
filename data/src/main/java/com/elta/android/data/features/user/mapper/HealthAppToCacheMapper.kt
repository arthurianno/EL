package com.elta.android.data.features.user.mapper

import com.elta.android.common.mapper.Mapper
import com.elta.android.data.features.user.cache.dto.HealthAppCacheDto
import com.elta.android.data.features.user.dto.HealthAppDto
import javax.inject.Inject

class HealthAppToCacheMapper @Inject constructor() : Mapper<HealthAppDto, HealthAppCacheDto> {
    override fun mapFromObject(source: HealthAppDto): HealthAppCacheDto =
        with(source) {
            HealthAppCacheDto(
                id = type.name.hashCode().toLong(),
                type = type.name,
                isActive = isActive
            )
        }
}
