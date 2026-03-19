package com.elta.android.data.features.multiLangsConfig.mapper

import com.elta.android.data.features.multiLangsConfig.dto.ScreenDto
import com.elta.android.domain.features.multiLangsConfig.model.ScreenEntity
import java.util.Locale

object ScreenMapper {
    fun ScreenDto.toEntity(lang: String = java.util.Locale.getDefault().language): ScreenEntity {
        return try {
            val titleValue = resolveLocalizedValue(title, lang)
            val descriptionValue = resolveLocalizedValue(description, lang)

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

    private fun resolveLocalizedValue(values: Map<String, String>?, requestedLang: String): String? {
        if (values.isNullOrEmpty()) return null

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
}
