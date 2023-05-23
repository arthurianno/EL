package com.elta.android.data.features.user.mapper

import com.elta.android.common.mapper.Mapper
import com.elta.android.data.features.user.cache.dto.HealthAppCacheDto
import com.elta.android.data.features.user.dto.HealthAppNetworkEntity
import javax.inject.Inject

class HealthAppToCacheMapper @Inject constructor() : Mapper<HealthAppNetworkEntity, HealthAppCacheDto> {
    override fun mapFromObject(source: HealthAppNetworkEntity): HealthAppCacheDto =
        with(source) {
            HealthAppCacheDto(
                id = type.name.hashCode().toLong(),
                type = type.name,
                isActive = isActive
            )
        }
}
