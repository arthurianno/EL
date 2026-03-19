package com.elta.android.data.features.user.dto

import java.util.Locale

const val LanguageTagQueryParam = "languageTag"

enum class SupportedLanguageTag(val value: String) {
    RU("ru"),
    EN("en");

    companion object {
        private fun normalize(rawValue: String?): String? =
            rawValue
                ?.trim()
                ?.replace('_', '-')
                ?.lowercase(Locale.ROOT)
                ?.takeIf { it.isNotEmpty() }
                ?.substringBefore('-')

        fun fromRawValue(rawValue: String?): SupportedLanguageTag =
            when (normalize(rawValue)) {
                EN.value -> EN
                RU.value -> RU
                else -> RU
            }
    }
}
