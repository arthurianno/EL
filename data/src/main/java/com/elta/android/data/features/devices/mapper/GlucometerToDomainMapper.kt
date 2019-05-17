package com.elta.android.data.features.devices.mapper

import com.elta.android.common.mapper.Mapper
import com.elta.android.data.features.devices.dto.GlucometerDto
import com.elta.android.domain.features.devices.model.Glucometer
import javax.inject.Inject

class GlucometerToDomainMapper @Inject constructor() : Mapper<GlucometerDto, Glucometer> {

    override fun mapFromObject(source: GlucometerDto): Glucometer =
        with(source) {
            Glucometer(
                id = id,
                address = address,
                name = name,
                isPrimary = isPrimary
            )
        }
}