package com.elta.android.presentation.features.language.model

import java.util.Locale

enum class AppLanguage(val code: String) {
    RU("ru"),
    EN("en");

    companion object {
        fun fromCode(code: String?): AppLanguage {
            val normalized = code
                ?.trim()
                ?.replace('_', '-')
                ?.lowercase(Locale.ROOT)
                ?.takeIf { it.isNotEmpty() }
                ?.substringBefore('-')

            return when (normalized) {
                EN.code -> EN
                else -> RU
            }
        }
    }
}
