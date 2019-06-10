package com.elta.android.data.features.user.mapper

import com.elta.android.common.mapper.Mapper
import com.elta.android.data.features.user.cache.dto.HealthAppCacheDto
import com.elta.android.data.features.user.dto.HealthAppDto
import com.elta.android.data.features.user.dto.HealthAppTypeDto
import javax.inject.Inject

class HealthAppFromCacheMapper @Inject constructor() : Mapper<HealthAppCacheDto, HealthAppDto> {
    override fun mapFromObject(source: HealthAppCacheDto): HealthAppDto =
        with(source) {
            HealthAppDto(
                type = HealthAppTypeDto.valueOf(type),
                isActive = isActive
            )
        }
}