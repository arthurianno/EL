package com.elta.android.presentation.utils

import android.content.Context

object SyncAttemptTimeStore {
    private const val PREFERENCES_NAME = "sync_attempt_time"
    private const val LAST_ATTEMPT_AT_KEY = "last_attempt_at"

    fun recordAttempt(context: Context): String {
        val now = System.currentTimeMillis()
        preferences(context)
            .edit()
            .putLong(LAST_ATTEMPT_AT_KEY, now)
            .apply()
        return formatElapsedTime(now)
    }

    fun getLastAttemptText(context: Context): String {
        val timestamp = preferences(context).getLong(LAST_ATTEMPT_AT_KEY, 0L)
        return if (timestamp > 0L) formatElapsedTime(timestamp) else "Нет синхронизаций"
    }

    private fun preferences(context: Context) =
        context.applicationContext.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    private fun formatElapsedTime(timestamp: Long): String {
        val minutesAgo = ((System.currentTimeMillis() - timestamp).coerceAtLeast(0L) / 60_000L)
        return when {
            minutesAgo == 0L -> "Только что"
            minutesAgo < 60L -> "${minutesAgo}м назад"
            minutesAgo < 24L * 60L -> "${minutesAgo / 60L}ч назад"
            else -> "${minutesAgo / (24L * 60L)}д назад"
        }
    }
}
