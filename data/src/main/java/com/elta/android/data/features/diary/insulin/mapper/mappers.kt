package com.elta.android.data.features.diary.insulin.mapper

import com.elta.android.data.features.diary.insulin.dto.DrugDto
import com.elta.android.domain.features.diary.events.model.Drug

fun List<DrugDto>.toDrug(): List<Drug> =
        map {
            Drug(
                    id = it.id,
                    insulinType = it.insulinType.toDomain(),
                    name = it.name
            )
        }

private fun DrugDto.InsulinTypeDto.toDomain(): Drug.InsulinType =
        Drug.InsulinType(
                code = code,
                id = id,
                name = name
        )