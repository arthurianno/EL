package com.elta.android.data.features.multiLangsConfig.mapper

import com.elta.android.data.features.multiLangsConfig.dto.ScreenDto
import com.elta.android.data.features.multiLangsConfig.room.ScreenConfigEntity
import com.elta.android.domain.features.multiLangsConfig.model.ScreenEntity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

// ScreenDto → ScreenConfigEntity (для сохранения данных с API в Room)
fun ScreenDto.toRoomEntity(): ScreenConfigEntity {
    return ScreenConfigEntity(
        slug = this.slug,
        titleJson = Gson().toJson(this.title),  // Map → JSON String
        descriptionJson = Gson().toJson(this.description),  // Map → JSON String
        backgroundImageUrl = this.backgroundImageUrl,
    )
}

fun ScreenConfigEntity.toLocalizedScreenEntity(lang: String): ScreenEntity {
    val gson = Gson()
    val type = object : TypeToken<Map<String, String>>() {}.type

    // ✅ ДОБАВЛЕНА ЗАЩИТА: если JSON невалидный, используем пустую карту
    val titleMap: Map<String, String> = try {
        gson.fromJson(this.titleJson, type) ?: emptyMap()
    } catch (e: Exception) {
        emptyMap()
    }

    val descriptionMap: Map<String, String> = try {
        gson.fromJson(this.descriptionJson, type) ?: emptyMap()
    } catch (e: Exception) {
        emptyMap()
    }

    // Извлекаем только нужный язык с fallback на ru
    val localizedTitle = titleMap[lang] ?: titleMap["ru"]
    val localizedDescription = descriptionMap[lang] ?: descriptionMap["ru"]

    return ScreenEntity(
        slug = this.slug,
        title = localizedTitle?.takeIf { it.isNotBlank() },
        description = localizedDescription?.takeIf { it.isNotBlank() },
        backgroundImageUrl = this.backgroundImageUrl,
        lang = lang
    )
}
