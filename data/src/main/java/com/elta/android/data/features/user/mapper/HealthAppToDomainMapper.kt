package com.elta.android.data.features.user.mapper

import com.elta.android.common.mapper.Mapper
import com.elta.android.data.features.user.dto.HealthAppDto
import com.elta.android.domain.features.user.model.HealthApp
import com.elta.android.domain.features.user.model.HealthAppType
import javax.inject.Inject

class HealthAppToDomainMapper @Inject constructor() : Mapper<HealthAppDto, HealthApp> {
    override fun mapFromObject(source: HealthAppDto): HealthApp =
        with(source) {
            HealthApp(
                type = HealthAppType.valueOf(type.name),
                isActive = isActive
            )
        }
}