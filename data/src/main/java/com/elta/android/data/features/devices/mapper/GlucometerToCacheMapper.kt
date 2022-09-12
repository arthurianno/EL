package com.elta.android.data.features.devices.mapper

import com.elta.android.common.mapper.Mapper
import com.elta.android.data.features.devices.cache.dto.GlucometerCachedDto
import com.elta.android.data.features.devices.dto.GlucometerDto
import javax.inject.Inject

class GlucometerToCacheMapper @Inject constructor() : Mapper<GlucometerDto, GlucometerCachedDto> {

    override fun mapFromObject(source: GlucometerDto): GlucometerCachedDto =
        with(source) {
            GlucometerCachedDto(
                id = id.hashCode().toLong(),
                secondaryId = id,
                address = address,
                name = name,
                isPrimary = isPrimary
            )
        }
}
