package com.elta.android.data.features.devices.mapper

import com.elta.android.common.mapper.Mapper
import com.elta.android.data.features.devices.dto.GlucometerDto
import com.elta.android.domain.features.devices.model.Glucometer
import javax.inject.Inject

class GlucometerToDtoMapper @Inject constructor() : Mapper<Glucometer, GlucometerDto> {

    override fun mapFromObject(source: Glucometer): GlucometerDto =
        with(source) {
            GlucometerDto(
                id = id,
                address = address,
                name = name,
                isPrimary = isPrimary
            )
        }
}
