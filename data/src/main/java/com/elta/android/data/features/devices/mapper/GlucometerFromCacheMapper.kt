package com.elta.android.data.features.devices.mapper

import com.elta.android.common.mapper.Mapper
import com.elta.android.data.features.devices.cache.dto.GlucometerCachedDto
import com.elta.android.data.features.devices.dto.GlucometerDto
import javax.inject.Inject

class GlucometerFromCacheMapper @Inject constructor() : Mapper<GlucometerCachedDto, GlucometerDto> {

    override fun mapFromObject(source: GlucometerCachedDto): GlucometerDto =
        with(source) {
            GlucometerDto(
                id = secondaryId,
                address = address,
                name = name,
                isPrimary = isPrimary
            )
        }
}
