package com.elta.android.data.features.diary.insulin.mapper

import com.elta.android.common.mapper.Mapper
import com.elta.android.data.features.diary.insulin.cache.DrugCachedDto
import com.elta.android.data.features.diary.insulin.dto.DrugDto
import javax.inject.Inject

class DrugToCacheMapper @Inject constructor() : Mapper<DrugDto, DrugCachedDto> {

    override fun mapFromObject(source: DrugDto): DrugCachedDto =
        DrugCachedDto(
            id = source.id.toLong(),
            drug = source.name,
            insulinType = source.insulinType.code.lowercase()
        )
}
