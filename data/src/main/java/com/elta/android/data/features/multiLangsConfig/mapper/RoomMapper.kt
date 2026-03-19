package com.elta.android.data.features.multiLangsConfig.mapper

import com.elta.android.data.features.multiLangsConfig.dto.ScreenDto
import com.elta.android.data.features.multiLangsConfig.room.ScreenConfigEntity
import com.elta.android.domain.features.multiLangsConfig.model.ScreenEntity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Locale

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

    val localizedTitle = resolveLocalizedValue(titleMap, lang)
    val localizedDescription = resolveLocalizedValue(descriptionMap, lang)

    return ScreenEntity(
        slug = this.slug,
        title = localizedTitle?.takeIf { it.isNotBlank() },
        description = localizedDescription?.takeIf { it.isNotBlank() },
        backgroundImageUrl = this.backgroundImageUrl,
        lang = lang
    )
}

private fun resolveLocalizedValue(values: Map<String, String>, requestedLang: String): String? {
    if (values.isEmpty()) return null

    val normalizedRequested = normalizeLanguageTag(requestedLang)
    val requestedBase = normalizedRequested.substringBefore('-')

    return findByTag(values, normalizedRequested)
        ?: findByBase(values, requestedBase)
        ?: findByTag(values, "ru")
        ?: findByBase(values, "ru")
        ?: findByTag(values, "en")
        ?: findByBase(values, "en")
        ?: values.values.firstOrNull { it.isNotBlank() }
}

private fun findByTag(values: Map<String, String>, tag: String): String? =
    values.entries.firstOrNull {
        normalizeLanguageTag(it.key) == tag && it.value.isNotBlank()
    }?.value

private fun findByBase(values: Map<String, String>, base: String): String? =
    values.entries.firstOrNull {
        normalizeLanguageTag(it.key).substringBefore('-') == base && it.value.isNotBlank()
    }?.value

private fun normalizeLanguageTag(rawTag: String?): String =
    rawTag
        ?.trim()
        ?.replace('_', '-')
        ?.lowercase(Locale.ROOT)
        ?.takeIf { it.isNotEmpty() }
        ?: "ru"
