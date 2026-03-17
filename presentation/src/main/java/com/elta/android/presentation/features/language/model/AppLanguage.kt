package com.elta.android.presentation.features.language.model

import java.util.Locale

enum class AppLanguage(val code: String) {
    RU("ru"),
    EN("en");

    companion object {
        fun fromCode(code: String?): AppLanguage {
            return when (code?.lowercase(Locale.ROOT)) {
                EN.code -> EN
                else -> RU
            }
        }
    }
}
