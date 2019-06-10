package com.elta.android.data.features.sale_points.mapper

import com.elta.android.common.mapper.Mapper
import com.elta.android.data.features.sale_points.cache.dto.SalePointCacheDto
import com.elta.android.data.features.sale_points.dto.SalePointDto
import javax.inject.Inject

class SalePointToCacheMapper @Inject constructor() : Mapper<SalePointDto, SalePointCacheDto> {
    override fun mapFromObject(source: SalePointDto): SalePointCacheDto =
        with(source) {
            SalePointCacheDto(
                id = id.hashCode().toLong(),
                secondaryId = id,
                name = name,
                type = type.name,
                region = region,
                city = city,
                address = address,
                fullAddress = "$city $address $name".toLowerCase(),
                phone = phone,
                latitude = coordinates.latitude,
                longitude = coordinates.longitude,
                timeStamp = timeStamp,
                modifiedState = state.name
            )
        }
}