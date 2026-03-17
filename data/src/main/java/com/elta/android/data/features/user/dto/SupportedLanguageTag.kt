package com.elta.android.data.features.user.dto

const val LanguageTagQueryParam = "languageTag"

enum class SupportedLanguageTag(val value: String) {
    RU("ru"),
    EN("en");

    companion object {
        fun fromRawValue(rawValue: String?): SupportedLanguageTag =
            when (rawValue?.lowercase()) {
                EN.value -> EN
                else -> RU
            }
    }
}
