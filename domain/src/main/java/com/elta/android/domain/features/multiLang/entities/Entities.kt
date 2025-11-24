package com.elta.android.domain.features.multiLang.entities

data class MultiLangString(
    val translations: Map<String, String> = emptyMap()
) {
    /**
     * Get translation for a specific language, fallback to default (e.g., "ru") if not found.
     */
    fun getTranslation(lang: String, defaultLang: String = "ru"): String? {
        return translations[lang] ?: translations[defaultLang]
    }
}

data class ScreenConfig(
    val slug: String,
    val description: MultiLangString,
    val backgroundImageUrl: String? = null // Optional, as per API
)