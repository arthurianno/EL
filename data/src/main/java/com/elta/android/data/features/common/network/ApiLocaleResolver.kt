package com.elta.android.data.features.common.network

import com.elta.android.data.features.user.dto.SupportedLanguageTag
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime
import java.util.Locale

object ApiLocaleResolver {

    fun languageTag(rawLanguageTag: String? = Locale.getDefault().toLanguageTag()): String =
        SupportedLanguageTag.fromRawValue(rawLanguageTag).value

    fun reportLocale(rawLanguageTag: String? = Locale.getDefault().toLanguageTag()): String =
        languageTag(rawLanguageTag)

    fun timezoneOffset(): String {
        val offset = ZonedDateTime.now(ZoneId.systemDefault()).offset.id
        return if (offset == "Z") "+00:00" else offset
    }
}
