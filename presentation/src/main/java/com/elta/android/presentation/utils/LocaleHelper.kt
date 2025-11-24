package com.elta.android.presentation.utils

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import java.util.*

object LocaleHelper {

    private const val PREF_LANGUAGE = "language_preference"
    private const val KEY_LANGUAGE = "selected_language"

    fun setLocale(context: Context, languageCode: String): Context {
        saveLanguage(context, languageCode)
        return updateResources(context, languageCode)
    }

    fun getLanguage(context: Context): String {
        val prefs = context.getSharedPreferences(PREF_LANGUAGE, Context.MODE_PRIVATE)
        val savedLanguage = prefs.getString(KEY_LANGUAGE, null)

        // Если язык еще не был сохранен, определяем язык системы
        if (savedLanguage == null) {
            val systemLanguage = getSystemLanguage()
            saveLanguage(context, systemLanguage)
            return systemLanguage
        }

        return savedLanguage
    }

    private fun getSystemLanguage(): String {
        val systemLocale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            android.content.res.Resources.getSystem().configuration.locales[0]
        } else {
            @Suppress("DEPRECATION")
            android.content.res.Resources.getSystem().configuration.locale
        }

        // Проверяем, поддерживается ли язык системы, иначе используем русский по умолчанию
        return when (systemLocale.language) {
            "en" -> "en"
            "ru" -> "ru"
            else -> "en" // По умолчанию русский для неподдерживаемых языков
        }
    }

    private fun saveLanguage(context: Context, languageCode: String) {
        val prefs = context.getSharedPreferences(PREF_LANGUAGE, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LANGUAGE, languageCode).apply()
    }

    private fun updateResources(context: Context, languageCode: String): Context {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(locale)

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.createConfigurationContext(configuration)
        } else {
            @Suppress("DEPRECATION")
            context.resources.updateConfiguration(configuration, context.resources.displayMetrics)
            context
        }
    }

    fun onAttach(context: Context): Context {
        val lang = getLanguage(context)
        return setLocale(context, lang)
    }
}

