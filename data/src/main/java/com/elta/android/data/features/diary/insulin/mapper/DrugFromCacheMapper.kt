package com.elta.android.data.features.diary.insulin.mapper

import com.elta.android.common.mapper.Mapper
import com.elta.android.data.features.diary.insulin.cache.DrugCachedDto
import com.elta.android.data.features.diary.insulin.dto.DrugDto
import com.elta.android.domain.features.diary.events.model.InsulinType
import javax.inject.Inject

class DrugFromCacheMapper @Inject constructor() : Mapper<DrugCachedDto, DrugDto> {
    override fun mapFromObject(source: DrugCachedDto): DrugDto =
        DrugDto(
            id = source.id.toInt(),
            name = source.drug,
            insulinType = InsulinType.valueOf(source.insulinType.uppercase()).toDto()
        )

    private fun InsulinType.toDto() = DrugDto.InsulinTypeDto(
        id = ordinal,
        code = name,
        name = name
    )
}
