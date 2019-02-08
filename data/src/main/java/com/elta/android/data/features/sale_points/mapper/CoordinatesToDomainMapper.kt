package com.elta.android.data.features.sale_points.mapper

import com.elta.android.common.mapper.Mapper
import com.elta.android.data.features.sale_points.dto.CoordinatesDto
import com.elta.android.domain.features.sale_points.model.Coordinates
import javax.inject.Inject

class CoordinatesToDomainMapper @Inject constructor() : Mapper<CoordinatesDto, Coordinates> {
    override fun mapFromObject(source: CoordinatesDto): Coordinates =
        with(source) {
            Coordinates(
                latitude = latitude,
                longitude = longitude
            )
        }
}