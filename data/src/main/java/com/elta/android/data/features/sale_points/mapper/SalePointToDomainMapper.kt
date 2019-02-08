package com.elta.android.data.features.sale_points.mapper

import com.elta.android.common.mapper.Mapper
import com.elta.android.data.features.sale_points.dto.CoordinatesDto
import com.elta.android.data.features.sale_points.dto.SalePointDto
import com.elta.android.domain.features.sale_points.model.Coordinates
import com.elta.android.domain.features.sale_points.model.SalePoint
import com.elta.android.domain.features.sale_points.model.Type
import javax.inject.Inject

class SalePointToDomainMapper @Inject constructor(
    private val coordinatesMapper: Mapper<CoordinatesDto, Coordinates>
) : Mapper<SalePointDto, SalePoint> {
    override fun mapFromObject(source: SalePointDto): SalePoint =
        with(source) {
            SalePoint(
                id = id,
                name = name,
                type = Type.valueOf(type.name),
                region = region,
                city = city,
                address = address,
                fullAddress = fullAddress,
                phone = phone,
                coordinates = coordinatesMapper.mapFromObject(coordinates),
                timeStamp = timeStamp
            )
        }
}