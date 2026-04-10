package com.elta.android.presentation.utils

import android.content.Context
import android.util.Log
import com.elta.android.presentation.BuildConfig
import com.onesignal.OneSignal

object OneSignalTags {

    fun login(userId: String, context: Context) {
        OneSignal.login(userId)
        apply(context)
    }

    fun logout() {
        OneSignal.logout()
    }

    fun apply(context: Context) {
        val languageTag = when (LocaleHelper.getLanguage(context).lowercase()) {
            "ru" -> "ru"
            else -> "en"
        }
        val platform = "android"
        val version = BuildConfig.CLEAN_VERSION
        val environment = if (BuildConfig.DEBUG) "dev" else "prod"
        val newsSegment = "$languageTag|$platform|$version"
        val countryCode = LocaleHelper.getRegion(context)
        Log.d(
            "OneSignalTags",
            "Applying tags: env=$environment, lang=$languageTag, platform=$platform, ver=$version, seg=$newsSegment, country=$countryCode"
        )
        OneSignal.User.addTags(
            mapOf(
                "environment" to environment,
                "language_tag" to languageTag,
                "platform" to platform,
                "version" to version,
                "news_segment" to newsSegment,
                "country_code" to countryCode
            )
        )
    }
}
