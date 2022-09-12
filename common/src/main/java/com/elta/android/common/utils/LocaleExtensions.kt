package com.elta.android.common.utils

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.LocaleList
import java.util.Locale

@Suppress("LongMethod")
@SuppressLint("ObsoleteSdkInt")
fun updateResources(context: Context, lang: String): Context {
    val locale = Locale(lang)

    val res = context.resources
    val configuration = res.configuration

    return when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.N -> {
            configuration.setLocale(locale)

            val localeList = LocaleList(locale)
            LocaleList.setDefault(localeList)
            configuration.setLocales(localeList)

            context.createConfigurationContext(configuration)
        }
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 -> {
            configuration.setLocale(locale)
            context.createConfigurationContext(configuration)
        }
        else -> {
            configuration.locale = locale
            res.updateConfiguration(configuration, res.displayMetrics)
            context
        }
    }
}
