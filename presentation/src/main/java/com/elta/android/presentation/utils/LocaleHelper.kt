package com.elta.android.presentation.utils

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.util.Log
import com.elta.android.presentation.BuildConfig
import java.util.*

object LocaleHelper {

    private const val PREF_LANGUAGE = "language_preference"
    private const val KEY_LANGUAGE = "selected_language"
    private const val KEY_REGION = "selected_region"
    private const val KEY_PENDING_GREETING_AFTER_LANGUAGE = "pending_greeting_after_language"
    private const val KEY_PENDING_HOME_AFTER_LANGUAGE = "pending_home_after_language"
    private const val LANGUAGE_RU = "ru"
    private const val LANGUAGE_EN = "en"
    private const val DEFAULT_REGION = "RU"
    private const val TAG = "≠"

    fun setLocale(context: Context, languageCode: String): Context {
        val normalizedLanguageCode = resolveLanguageForBuild(languageCode)
        Log.i(TAG, "LocaleHelper.setLocale(requested=$languageCode, normalized=$normalizedLanguageCode)")
        saveLanguage(context, normalizedLanguageCode)
        // Fix 9: Android 13+ — persist locale via LocaleManager so the system is aware.
        // LocaleManager.setApplicationLocales() triggers its own activity recreation on API 33+;
        // callers should check needsManualRecreate() to avoid a double-recreate.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            runCatching {
                context.applicationContext
                    ?.getSystemService(android.app.LocaleManager::class.java)
                    ?.applicationLocales = android.os.LocaleList(Locale(normalizedLanguageCode))
                Log.i(TAG, "LocaleHelper.setLocale: LocaleManager updated (API 33+)")
            }.onFailure { e ->
                Log.w(TAG, "LocaleHelper.setLocale: LocaleManager failed: ${e.message}")
            }
        }
        return updateResources(context, normalizedLanguageCode)
    }

    /**
     * Fix 9: Returns true when the caller must call activity.recreate() manually.
     * On API 33+, LocaleManager.setApplicationLocales() already triggers recreation.
     */
    fun needsManualRecreate(): Boolean = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU

    // Fix 3: getLanguage() is now a pure getter — no more silent saveLanguage() side-effect inside.
    fun getLanguage(context: Context): String {
        val prefs = context.getSharedPreferences(PREF_LANGUAGE, Context.MODE_PRIVATE)
        val savedLanguage = prefs.getString(KEY_LANGUAGE, null)
        val systemLanguage = getSystemLanguage()
        val resolved = resolveLanguageForBuild(savedLanguage ?: systemLanguage)
        Log.i(
            TAG,
            "LocaleHelper.getLanguage(saved=$savedLanguage, system=$systemLanguage, resolved=$resolved, languageSelectionEnabled=${BuildConfig.SHOW_LANGUAGE_SELECTION})"
        )
        return resolved
    }

    fun shouldShowLanguageSelection(context: Context): Boolean {
        if (!BuildConfig.SHOW_LANGUAGE_SELECTION) {
            Log.i(TAG, "LocaleHelper.shouldShowLanguageSelection=false (feature disabled)")
            return false
        }
        val prefs = context.getSharedPreferences(PREF_LANGUAGE, Context.MODE_PRIVATE)
        val shouldShow = prefs.getString(KEY_LANGUAGE, null) == null
        Log.i(TAG, "LocaleHelper.shouldShowLanguageSelection=$shouldShow")
        return shouldShow
    }

    // Fix 1: use commit() (synchronous) so the flag is guaranteed to be persisted
    // before activity.recreate() fires. Falls back to apply() only if commit() fails.
    fun markPendingGreetingAfterLanguageSelection(context: Context) {
        val prefs = context.getSharedPreferences(PREF_LANGUAGE, Context.MODE_PRIVATE)
        val committed = prefs.edit().putBoolean(KEY_PENDING_GREETING_AFTER_LANGUAGE, true).commit()
        Log.i(TAG, "LocaleHelper.markPendingGreetingAfterLanguageSelection(committed=$committed)")
        if (!committed) {
            prefs.edit().putBoolean(KEY_PENDING_GREETING_AFTER_LANGUAGE, true).apply()
            Log.w(TAG, "LocaleHelper.markPendingGreetingAfterLanguageSelection: commit failed, fallback apply()")
        }
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

    // Флаг «после смены языка из настроек — перейти на главный экран (HomeFlow)».
    // Используется вместо простого recreate(), чтобы не оставлять пользователя на экране настроек.
    fun markPendingHomeAfterLanguageChange(context: Context) {
        val prefs = context.getSharedPreferences(PREF_LANGUAGE, Context.MODE_PRIVATE)
        val committed = prefs.edit().putBoolean(KEY_PENDING_HOME_AFTER_LANGUAGE, true).commit()
        Log.i(TAG, "LocaleHelper.markPendingHomeAfterLanguageChange(committed=$committed)")
        if (!committed) {
            prefs.edit().putBoolean(KEY_PENDING_HOME_AFTER_LANGUAGE, true).apply()
            Log.w(TAG, "LocaleHelper.markPendingHomeAfterLanguageChange: commit failed, fallback apply()")
        }
    }

    fun consumePendingHomeAfterLanguageChange(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREF_LANGUAGE, Context.MODE_PRIVATE)
        val pending = prefs.getBoolean(KEY_PENDING_HOME_AFTER_LANGUAGE, false)
        if (pending) {
            prefs.edit().putBoolean(KEY_PENDING_HOME_AFTER_LANGUAGE, false).apply()
        }
        Log.i(TAG, "LocaleHelper.consumePendingHomeAfterLanguageChange=$pending")
        return pending
    }

    fun saveRegion(context: Context, regionCode: String) {
        val prefs = context.getSharedPreferences(PREF_LANGUAGE, Context.MODE_PRIVATE)
        val committed = prefs.edit().putString(KEY_REGION, regionCode).commit()
        Log.i(TAG, "LocaleHelper.saveRegion(code=$regionCode, commitResult=$committed)")
        if (!committed) {
            prefs.edit().putString(KEY_REGION, regionCode).apply()
            Log.w(TAG, "LocaleHelper.saveRegion: commit failed, fallback apply()")
        }
    }

    fun getRegion(context: Context): String {
        val prefs = context.getSharedPreferences(PREF_LANGUAGE, Context.MODE_PRIVATE)
        val saved = prefs.getString(KEY_REGION, null)
        if (saved != null) {
            Log.i(TAG, "LocaleHelper.getRegion(saved=$saved)")
            return saved
        }

        // Берем код региона из оригинальной конфигурации системы Android
        val systemCountry = getSystemCountry().takeIf { it.isNotEmpty() } ?: DEFAULT_REGION
        Log.i(TAG, "LocaleHelper.getRegion(systemCountry=$systemCountry)")
        return systemCountry
    }

    private fun getSystemCountry(): String {
        val systemLocale = runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                android.content.res.Resources.getSystem().configuration.locales[0]
            } else {
                @Suppress("DEPRECATION")
                android.content.res.Resources.getSystem().configuration.locale
            }
        }.getOrNull()

        val country = systemLocale?.country?.uppercase(Locale.ROOT) ?: ""
        Log.i(TAG, "LocaleHelper.getSystemCountry(raw=${systemLocale?.country}, resolved=$country)")
        return country
    }

    // Fix 4: updateAppResourcesIfAvailable is called ONCE after configuration is fully set up,
    // instead of being duplicated inside every branch of the `when` block.
    private fun updateResources(context: Context, languageCode: String): Context {
        Log.i(
            TAG,
            "LocaleHelper.updateResources(start, code=$languageCode, sdk=${Build.VERSION.SDK_INT}, currentDefault=${Locale.getDefault().language})"
        )
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val configuration = Configuration(context.resources.configuration)
        val appContext: Context? = context.applicationContext

        val updatedContext: Context = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.N -> {
                val localeList = android.os.LocaleList(locale)
                android.os.LocaleList.setDefault(localeList)
                configuration.setLocales(localeList)
                context.createConfigurationContext(configuration)
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 -> {
                configuration.setLocale(locale)
                context.createConfigurationContext(configuration)
            }
            else -> {
                @Suppress("DEPRECATION")
                configuration.locale = locale
                @Suppress("DEPRECATION")
                context.resources.updateConfiguration(configuration, context.resources.displayMetrics)
                context
            }
        }

        // Keep app-context resources in sync so Application.getString() etc. return the right locale.
        // Called once here instead of in each branch above (Fix 4).
        updateAppResourcesIfAvailable(appContext, configuration)

        Log.i(TAG, "LocaleHelper.updateResources(complete, default=${Locale.getDefault().language})")
        return updatedContext
    }

    @Suppress("DEPRECATION")
    private fun updateAppResourcesIfAvailable(appContext: Context?, configuration: Configuration) {
        val resources = appContext?.resources ?: return
        resources.updateConfiguration(configuration, resources.displayMetrics)
    }

    // Fix 3: side effect extracted from getLanguage() into this method, called explicitly in onAttach().
    private fun ensureLanguageConsistency(context: Context) {
        if (!BuildConfig.SHOW_LANGUAGE_SELECTION) {
            val prefs = context.getSharedPreferences(PREF_LANGUAGE, Context.MODE_PRIVATE)
            val savedLanguage = prefs.getString(KEY_LANGUAGE, null)
            if (savedLanguage == null || normalizeLanguage(savedLanguage) != LANGUAGE_RU) {
                Log.i(TAG, "LocaleHelper.ensureLanguageConsistency: forcing RU (feature disabled)")
                saveLanguage(context, LANGUAGE_RU)
            }
        }
    }

    fun onAttach(context: Context): Context {
        ensureLanguageConsistency(context) // Fix 3: side effect here, not inside getLanguage()
        val lang = getLanguage(context)
        Log.i(TAG, "LocaleHelper.onAttach(lang=$lang)")
        return updateResources(context, lang)
    }

    private fun resolveLanguageForBuild(languageCode: String?): String {
        return if (BuildConfig.SHOW_LANGUAGE_SELECTION) {
            normalizeLanguage(languageCode)
        } else {
            LANGUAGE_RU
        }
    }

    private fun saveLanguage(context: Context, languageCode: String) {
        val prefs = context.getSharedPreferences(PREF_LANGUAGE, Context.MODE_PRIVATE)
        val editor = prefs.edit().putString(KEY_LANGUAGE, languageCode)
        val committed = editor.commit()
        Log.i(TAG, "LocaleHelper.saveLanguage(code=$languageCode, commitResult=$committed)")
        if (!committed) {
            editor.apply()
            Log.w(TAG, "LocaleHelper.saveLanguage: commit failed, fallback apply()")
        }
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

    private fun normalizeLanguage(languageCode: String?): String {
        val normalized = languageCode
            ?.trim()
            ?.replace('_', '-')
            ?.lowercase(Locale.ROOT)
            ?.takeIf { it.isNotEmpty() }
            ?: return LANGUAGE_RU

        return when (normalized.substringBefore('-')) {
            LANGUAGE_EN -> LANGUAGE_EN
            LANGUAGE_RU -> LANGUAGE_RU
            else -> LANGUAGE_RU
        }
    }
}
