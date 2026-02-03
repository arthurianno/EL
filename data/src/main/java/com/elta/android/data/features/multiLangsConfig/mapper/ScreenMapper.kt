package com.elta.android.data.features.multiLangsConfig.mapper

import android.util.Log
import com.elta.android.data.features.multiLangsConfig.dto.ScreenDto
import com.elta.android.domain.features.multiLangsConfig.model.ScreenEntity

object ScreenMapper {
    fun ScreenDto.toEntity(lang: String = java.util.Locale.getDefault().language): ScreenEntity {
        return try {
            ScreenEntity(
                slug = slug,
                title = title?.get(lang) ?: title?.get("ru") ?: "",
                description = description?.get(lang) ?: description?.get("ru") ?: "",
                backgroundImageUrl = backgroundImageUrl,
                lang = lang
            )
        } catch (e: Exception) {
            Log.e("ScreenMapper", "❌ Ошибка маппинга для slug='$slug': ${e.message}", e)
            ScreenEntity(
                slug = slug,
                title = "",
                description = "",
                backgroundImageUrl = backgroundImageUrl,
                lang = lang
            )
        }
    }

}