package com.elta.android.data.features.user.mapper

import com.elta.android.common.mapper.Mapper
import com.elta.android.data.features.user.dto.HealthAppDto
import com.elta.android.data.features.user.dto.HealthAppTypeDto
import com.elta.android.domain.features.user.model.HealthApp
import javax.inject.Inject

class HealthAppToDtoMapper @Inject constructor() : Mapper<HealthApp, HealthAppDto> {
    override fun mapFromObject(source: HealthApp): HealthAppDto =
        with(source) {
            HealthAppDto(
                type = HealthAppTypeDto.valueOf(type.name),
                isActive = isActive
            )
        }
}