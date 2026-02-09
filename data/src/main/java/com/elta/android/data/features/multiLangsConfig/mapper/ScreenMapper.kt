package com.elta.android.data.features.multiLangsConfig.mapper

import android.util.Log
import com.elta.android.data.features.multiLangsConfig.dto.ScreenDto
import com.elta.android.domain.features.multiLangsConfig.model.ScreenEntity

object ScreenMapper {
    fun ScreenDto.toEntity(lang: String = java.util.Locale.getDefault().language): ScreenEntity {
        return try {
            val titleValue = title?.get(lang) ?: title?.get("ru")
            val descriptionValue = description?.get(lang) ?: description?.get("ru")

            ScreenEntity(
                slug = slug,
                title = titleValue?.takeIf { it.isNotBlank() },
                description = descriptionValue?.takeIf { it.isNotBlank() },
                backgroundImageUrl = backgroundImageUrl,
                lang = lang
            )
        } catch (e: Exception) {
            ScreenEntity(
                slug = slug,
                // ✅ ИЗМЕНЕНО: возвращаем null вместо пустой строки
                title = null,
                description = null,
                backgroundImageUrl = backgroundImageUrl,
                lang = lang
            )
        }
    }

}