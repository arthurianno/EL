package com.elta.android.data.features.sale_points.mapper

import com.elta.android.common.mapper.Mapper
import com.elta.android.data.features.sale_points.cache.dto.SalePointCacheDto
import com.elta.android.data.features.sale_points.dto.CoordinatesDto
import com.elta.android.data.features.sale_points.dto.SalePointDto
import com.elta.android.data.features.common.dto.StateDto
import com.elta.android.data.features.sale_points.dto.TypeDto
import javax.inject.Inject

class SalePointFromCacheMapper @Inject constructor() : Mapper<SalePointCacheDto, SalePointDto> {
    override fun mapFromObject(source: SalePointCacheDto): SalePointDto =
        with(source) {
            SalePointDto(
                id = secondaryId,
                name = name,
                type = TypeDto.valueOf(type),
                region = region,
                city = city,
                address = address,
                phone = phone,
                coordinates = CoordinatesDto(latitude, longitude),
                timeStamp = timeStamp,
                state = StateDto.valueOf(modifiedState)
            )
        }
}