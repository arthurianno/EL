package com.elta.android.presentation.utils

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.util.Log
import java.util.*

object LocaleHelper {

    private const val PREF_LANGUAGE = "language_preference"
    private const val KEY_LANGUAGE = "selected_language"
    private const val KEY_PENDING_GREETING_AFTER_LANGUAGE = "pending_greeting_after_language"
    private const val LANGUAGE_RU = "ru"
    private const val LANGUAGE_EN = "en"
    private const val TAG = "LangFlow"

    fun setLocale(context: Context, languageCode: String): Context {
        val normalizedLanguageCode = normalizeLanguage(languageCode)
        Log.i(TAG, "LocaleHelper.setLocale(requested=$languageCode, normalized=$normalizedLanguageCode)")
        saveLanguage(context, normalizedLanguageCode)
        return updateResources(context, normalizedLanguageCode)
    }

    fun getLanguage(context: Context): String {
        val prefs = context.getSharedPreferences(PREF_LANGUAGE, Context.MODE_PRIVATE)
        val savedLanguage = prefs.getString(KEY_LANGUAGE, null)
        val systemLanguage = getSystemLanguage()
        val resolved = normalizeLanguage(savedLanguage ?: systemLanguage)
        Log.i(
            TAG,
            "LocaleHelper.getLanguage(saved=$savedLanguage, system=$systemLanguage, resolved=$resolved)"
        )
        return resolved
    }

    fun shouldShowLanguageSelection(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREF_LANGUAGE, Context.MODE_PRIVATE)
        val shouldShow = prefs.getString(KEY_LANGUAGE, null) == null
        Log.i(TAG, "LocaleHelper.shouldShowLanguageSelection=$shouldShow")
        return shouldShow
    }

    private fun getSystemLanguage(): String {
        val systemLocale = runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                android.content.res.Resources.getSystem().configuration.locales[0]
            } else {
                @Suppress("DEPRECATION")
                android.content.res.Resources.getSystem().configuration.locale
            }
        }.getOrNull()

        val normalized = when (systemLocale?.language?.lowercase(Locale.ROOT)) {
            LANGUAGE_EN -> LANGUAGE_EN
            LANGUAGE_RU -> LANGUAGE_RU
            else -> LANGUAGE_RU
        }
        Log.i(TAG, "LocaleHelper.getSystemLanguage(raw=${systemLocale?.language}, normalized=$normalized)")
        return normalized
    }

    private fun saveLanguage(context: Context, languageCode: String) {
        val prefs = context.getSharedPreferences(PREF_LANGUAGE, Context.MODE_PRIVATE)
        val editor = prefs.edit().putString(KEY_LANGUAGE, languageCode)
        val committed = editor.commit()
        Log.i(TAG, "LocaleHelper.saveLanguage(code=$languageCode, commitResult=$committed)")
        if (!committed) {
            // Fallback path if synchronous write unexpectedly fails.
            editor.apply()
            Log.w(TAG, "LocaleHelper.saveLanguage: commit failed, fallback apply()")
        }
    }

    fun markPendingGreetingAfterLanguageSelection(context: Context) {
        val prefs = context.getSharedPreferences(PREF_LANGUAGE, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_PENDING_GREETING_AFTER_LANGUAGE, true).apply()
        Log.i(TAG, "LocaleHelper.markPendingGreetingAfterLanguageSelection(true)")
    }

    fun consumePendingGreetingAfterLanguageSelection(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREF_LANGUAGE, Context.MODE_PRIVATE)
        val pending = prefs.getBoolean(KEY_PENDING_GREETING_AFTER_LANGUAGE, false)
        if (pending) {
            prefs.edit().putBoolean(KEY_PENDING_GREETING_AFTER_LANGUAGE, false).apply()
        }
        Log.i(TAG, "LocaleHelper.consumePendingGreetingAfterLanguageSelection=$pending")
        return pending
    }

    private fun updateResources(context: Context, languageCode: String): Context {
        Log.i(
            TAG,
            "LocaleHelper.updateResources(start, code=$languageCode, sdk=${Build.VERSION.SDK_INT}, currentDefault=${Locale.getDefault().language})"
        )
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(locale)
        @Suppress("DEPRECATION")
        context.resources.updateConfiguration(configuration, context.resources.displayMetrics)

        val appContext = context.applicationContext
        @Suppress("DEPRECATION")
        appContext.resources.updateConfiguration(configuration, appContext.resources.displayMetrics)

        val updatedContext = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.createConfigurationContext(configuration)
        } else {
            @Suppress("DEPRECATION")
            context.resources.updateConfiguration(configuration, context.resources.displayMetrics)
            context
        }
        Log.i(TAG, "LocaleHelper.updateResources(complete, default=${Locale.getDefault().language})")
        return updatedContext
    }

    fun onAttach(context: Context): Context {
        val lang = getLanguage(context)
        Log.i(TAG, "LocaleHelper.onAttach(lang=$lang)")
        return updateResources(context, lang)
    }

    private fun normalizeLanguage(languageCode: String): String {
        return when (languageCode.lowercase(Locale.ROOT)) {
            LANGUAGE_EN -> LANGUAGE_EN
            else -> LANGUAGE_RU
        }
    }
}
