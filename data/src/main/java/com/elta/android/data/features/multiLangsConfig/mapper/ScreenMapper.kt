package com.elta.android.data.features.multiLangsConfig.mapper

import com.elta.android.data.features.multiLangsConfig.dto.ScreenDto
import com.elta.android.domain.features.multiLangsConfig.model.ScreenEntity

object ScreenMapper {
    fun ScreenDto.toEntity(lang: String = java.util.Locale.getDefault().language): ScreenEntity {
        return ScreenEntity(
            slug = slug,
            title = title[lang] ?: title["ru"] ?: "",
            description = description[lang] ?: description["ru"] ?: "",
            backgroundImageUrl = backgroundImageUrl,
            lang = lang
        )
    }

}