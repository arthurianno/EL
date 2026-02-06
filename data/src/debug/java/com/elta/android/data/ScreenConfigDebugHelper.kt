package com.elta.android.data.features.multiLangsConfig.debug

import android.content.Context
import android.util.Log
import androidx.core.content.edit

/**
 * 🧪 Простой helper для тестирования обновления конфигов
 */
class ScreenConfigDebugHelper(context: Context) {

    private val prefs = context.getSharedPreferences("screen_config_prefs", Context.MODE_PRIVATE)

    fun printCurrentState() {
        val lastRefresh = prefs.getLong("last_refresh_timestamp", 0L)
        val randomOffset = prefs.getLong("random_offset_minutes", Long.MAX_VALUE)
        val currentTime = System.currentTimeMillis()

        Log.i("DEBUG", "═══════════════════════════════════════")
        Log.i("DEBUG", "СОСТОЯНИЕ КОНФИГОВ:")

        if (lastRefresh == 0L) {
            Log.i("DEBUG", "❌ Первый запуск - обновление не было")
        } else {
            val hoursPassed = (currentTime - lastRefresh) / (60 * 60 * 1000)
            val minutesPassed = ((currentTime - lastRefresh) % (60 * 60 * 1000)) / (60 * 1000)
            Log.i("DEBUG", "⏱️ Прошло: ${hoursPassed}ч ${minutesPassed}мин")
        }

        if (randomOffset != Long.MAX_VALUE) {
            val offsetMinutes = randomOffset / (60 * 1000)
            Log.i("DEBUG", "🎲 Смещение: ${offsetMinutes} минут")

            val interval = (24 * 60 * 60 * 1000L) + randomOffset
            val shouldRefresh = (currentTime - lastRefresh) >= interval
            Log.i("DEBUG", "✅ Нужно обновление: $shouldRefresh")
        }
        Log.i("DEBUG", "═══════════════════════════════════════")
    }

    fun simulateTimePassed(hours: Int) {
        val lastRefresh = prefs.getLong("last_refresh_timestamp", 0L)
        if (lastRefresh == 0L) return

        val newTime = lastRefresh - (hours * 60 * 60 * 1000L)
        prefs.edit { putLong("last_refresh_timestamp", newTime) }
        Log.i("DEBUG", "⏰ Симулировано: прошло ${hours} часов")
        printCurrentState()
    }

    fun resetAll() {
        prefs.edit {
            remove("last_refresh_timestamp")
            remove("random_offset_minutes")
        }
        Log.i("DEBUG", "🔄 Сброшено (первый запуск)")
    }

    fun setNow() {
        prefs.edit { putLong("last_refresh_timestamp", System.currentTimeMillis()) }
        Log.i("DEBUG", "✅ Время установлено = сейчас")
    }
}
