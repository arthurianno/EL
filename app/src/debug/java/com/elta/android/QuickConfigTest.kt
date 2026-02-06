package com.elta.android

import android.content.Context
import android.util.Log
import androidx.core.content.edit

/**
 * 🧪 БЫСТРЫЙ ТЕСТ для проверки логики обновления конфигов
 *
 * Добавь в AppActivity.onCreate():
 * ```
 * if (BuildConfig.DEBUG) {
 *     QuickConfigTest.runTests(this)
 * }
 * ```
 */
object QuickConfigTest {

    private const val PREFS_NAME = "screen_config_prefs"
    private const val KEY_LAST_REFRESH = "last_refresh_timestamp"
    private const val KEY_RANDOM_OFFSET = "random_offset_minutes"

    /**
     * Запускает быстрые тесты
     */
    fun runTests(context: Context) {
        Log.i("🧪 QuickTest", "═══════════════════════════════════════")
        Log.i("🧪 QuickTest", "БЫСТРЫЕ ТЕСТЫ КОНФИГУРАЦИИ")
        Log.i("🧪 QuickTest", "═══════════════════════════════════════")

        printCurrentState(context)

        // Раскомментируй нужный тест:

        // testFirstLaunch(context)
        // testAfter12Hours(context)
        // testAfter25Hours(context)
    }

    /**
     * Показывает текущее состояние
     */
    fun printCurrentState(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lastRefresh = prefs.getLong(KEY_LAST_REFRESH, 0L)
        val randomOffset = prefs.getLong(KEY_RANDOM_OFFSET, Long.MAX_VALUE)
        val currentTime = System.currentTimeMillis()

        Log.i("🧪 QuickTest", "📊 ТЕКУЩЕЕ СОСТОЯНИЕ:")

        if (lastRefresh == 0L) {
            Log.i("🧪 QuickTest", "   ❌ Обновление еще не было (первый запуск)")
        } else {
            val hoursPassed = (currentTime - lastRefresh) / (60 * 60 * 1000)
            val minutesPassed = ((currentTime - lastRefresh) % (60 * 60 * 1000)) / (60 * 1000)
            Log.i("🧪 QuickTest", "   ⏱️ Прошло: ${hoursPassed}ч ${minutesPassed}мин")
        }

        if (randomOffset != Long.MAX_VALUE) {
            val offsetMinutes = randomOffset / (60 * 1000)
            Log.i("🧪 QuickTest", "   🎲 Смещение: ${offsetMinutes} минут")

            val interval = (24 * 60 * 60 * 1000L) + randomOffset
            val shouldRefresh = (currentTime - lastRefresh) >= interval
            Log.i("🧪 QuickTest", "   ✅ Нужно обновление: $shouldRefresh")

            if (!shouldRefresh && lastRefresh != 0L) {
                val timeLeft = interval - (currentTime - lastRefresh)
                val hoursLeft = timeLeft / (60 * 60 * 1000)
                val minutesLeft = (timeLeft % (60 * 60 * 1000)) / (60 * 1000)
                Log.i("🧪 QuickTest", "   ⏳ До обновления: ${hoursLeft}ч ${minutesLeft}мин")
            }
        } else {
            Log.i("🧪 QuickTest", "   🎲 Смещение: НЕ сгенерировано")
        }

        Log.i("🧪 QuickTest", "═══════════════════════════════════════")
    }

    /**
     * ТЕСТ 1: Первый запуск
     */
    fun testFirstLaunch(context: Context) {
        Log.i("🧪 QuickTest", "")
        Log.i("🧪 QuickTest", "🧪 ТЕСТ: Симуляция первого запуска")

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit {
            remove(KEY_LAST_REFRESH)
            remove(KEY_RANDOM_OFFSET)
        }

        Log.i("🧪 QuickTest", "✅ Сброшено. Перезапусти приложение!")
        Log.i("🧪 QuickTest", "Ожидается: загрузка с сервера + генерация смещения")
        printCurrentState(context)
    }

    /**
     * ТЕСТ 2: Прошло 12 часов
     */
    fun testAfter12Hours(context: Context) {
        Log.i("🧪 QuickTest", "")
        Log.i("🧪 QuickTest", "🧪 ТЕСТ: Симуляция 12 часов назад")

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val newTime = System.currentTimeMillis() - (12 * 60 * 60 * 1000L)

        prefs.edit {
            putLong(KEY_LAST_REFRESH, newTime)
            putLong(KEY_RANDOM_OFFSET, 10 * 60 * 1000L) // +10 минут
        }

        Log.i("🧪 QuickTest", "✅ Установлено: последнее обновление 12 часов назад")
        Log.i("🧪 QuickTest", "Ожидается: использование КЭША (без загрузки)")
        printCurrentState(context)
        Log.i("🧪 QuickTest", "Перезапусти приложение и смотри логи!")
    }

    /**
     * ТЕСТ 3: Прошло 25 часов
     */
    fun testAfter25Hours(context: Context) {
        Log.i("🧪 QuickTest", "")
        Log.i("🧪 QuickTest", "🧪 ТЕСТ: Симуляция 25 часов назад")

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val newTime = System.currentTimeMillis() - (25 * 60 * 60 * 1000L)

        prefs.edit {
            putLong(KEY_LAST_REFRESH, newTime)
            putLong(KEY_RANDOM_OFFSET, 10 * 60 * 1000L) // +10 минут
        }

        Log.i("🧪 QuickTest", "✅ Установлено: последнее обновление 25 часов назад")
        Log.i("🧪 QuickTest", "Ожидается: ЗАГРУЗКА с сервера")
        printCurrentState(context)
        Log.i("🧪 QuickTest", "Перезапусти приложение и смотри логи!")
    }
}
