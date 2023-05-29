package com.elta.android.data.features.user.mapper

import com.elta.android.common.mapper.Mapper
import com.elta.android.data.features.user.cache.dto.HealthAppCacheDto
import com.elta.android.data.features.user.dto.HealthAppNetworkEntity
import com.elta.android.data.features.user.dto.HealthAppTypeNetworkEntity
import javax.inject.Inject

class HealthAppFromCacheMapper @Inject constructor() : Mapper<HealthAppCacheDto, HealthAppNetworkEntity> {
    override fun mapFromObject(source: HealthAppCacheDto): HealthAppNetworkEntity =
        with(source) {
            HealthAppNetworkEntity(
                type = HealthAppTypeNetworkEntity.valueOf(type),
                isActive = isActive
            )
        }
}
