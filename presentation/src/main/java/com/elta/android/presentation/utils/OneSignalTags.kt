package com.elta.android.presentation.utils

import android.content.Context
import android.util.Log
import com.elta.android.presentation.BuildConfig
import com.onesignal.OneSignal
import java.util.Locale

object OneSignalTags {

    fun login(userId: String, context: Context) {
        OneSignal.login(userId)
        apply(context)
    }

    fun logout() {
        OneSignal.User.removeTags(ONE_SIGNAL_TAG_KEYS)
        OneSignal.logout()
    }

    fun apply(context: Context) {
        val tags = buildTags(
            languageCode = LocaleHelper.getLanguage(context),
            countryCode = LocaleHelper.getRegion(context),
            version = BuildConfig.CLEAN_VERSION,
            isDebug = BuildConfig.DEBUG
        )
        Log.d(
            "OneSignalTags",
            "Applying tags: env=${tags.getValue(TAG_ENV)}, " +
                "lang=${tags.getValue(TAG_LANGUAGE)}, " +
                "platform=${tags.getValue(TAG_PLATFORM)}, " +
                "ver=${tags.getValue(TAG_VERSION)}, " +
                "seg=${tags.getValue(TAG_NEWS_SEGMENT)}, " +
                "country=${tags.getValue(TAG_COUNTRY)}"
        )
        OneSignal.User.addTags(tags)
    }

    internal fun buildTags(
        languageCode: String,
        countryCode: String,
        version: String,
        isDebug: Boolean
    ): Map<String, String> {
        val languageTag = when (languageCode.lowercase(Locale.ROOT)) {
            SUPPORTED_RU -> SUPPORTED_RU
            else -> SUPPORTED_EN
        }
        val platform = PLATFORM_ANDROID
        val environment = if (isDebug) ENV_DEV else ENV_PROD
        val normalizedCountry = countryCode
            .trim()
            .uppercase(Locale.ROOT)
            .takeIf { it.length == COUNTRY_CODE_LENGTH && it.all(Char::isLetter) }
            ?: DEFAULT_COUNTRY_CODE

        return mapOf(
            TAG_ENV to environment,
            TAG_ENVIRONMENT_LEGACY to environment,
            TAG_LANGUAGE to languageTag,
            TAG_PLATFORM to platform,
            TAG_VERSION to version,
            TAG_NEWS_SEGMENT to "$languageTag|$platform|$version",
            TAG_COUNTRY to normalizedCountry
        )
    }

    private const val TAG_ENV = "env"
    private const val TAG_ENVIRONMENT_LEGACY = "environment"
    private const val TAG_LANGUAGE = "language_tag"
    private const val TAG_PLATFORM = "platform"
    private const val TAG_VERSION = "version"
    private const val TAG_NEWS_SEGMENT = "news_segment"
    private const val TAG_COUNTRY = "country_code"
    private const val PLATFORM_ANDROID = "android"
    private const val ENV_DEV = "dev"
    private const val ENV_PROD = "prod"
    private const val SUPPORTED_RU = "ru"
    private const val SUPPORTED_EN = "en"
    private const val DEFAULT_COUNTRY_CODE = "RU"
    private const val COUNTRY_CODE_LENGTH = 2
    private val ONE_SIGNAL_TAG_KEYS = listOf(
        TAG_ENV,
        TAG_ENVIRONMENT_LEGACY,
        TAG_LANGUAGE,
        TAG_PLATFORM,
        TAG_VERSION,
        TAG_NEWS_SEGMENT,
        TAG_COUNTRY
    )
}
