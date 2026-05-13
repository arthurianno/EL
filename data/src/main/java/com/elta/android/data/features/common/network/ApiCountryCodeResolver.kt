package com.elta.android.data.features.common.network

import android.content.Context
import java.util.Locale
import javax.inject.Inject

interface CountryCodeProvider {
    fun countryCode(): String
}

class ApiCountryCodeResolver @Inject constructor(
    private val context: Context
) : CountryCodeProvider {

    override fun countryCode(): String {
        val savedRegion = context
            .getSharedPreferences(LANGUAGE_PREFS_NAME, Context.MODE_PRIVATE)
            .getString(REGION_KEY_SELECTED, null)

        return normalize(savedRegion)
    }

    companion object {
        private const val LANGUAGE_PREFS_NAME = "language_preference"
        private const val REGION_KEY_SELECTED = "selected_region"
        const val DEFAULT_COUNTRY_CODE = "RU"

        fun normalize(rawCountryCode: String?): String =
            rawCountryCode
                ?.trim()
                ?.uppercase(Locale.ROOT)
                ?.takeIf { it.length == ISO_ALPHA_2_LENGTH && it.all(Char::isLetter) }
                ?: DEFAULT_COUNTRY_CODE

        private const val ISO_ALPHA_2_LENGTH = 2
    }
}
