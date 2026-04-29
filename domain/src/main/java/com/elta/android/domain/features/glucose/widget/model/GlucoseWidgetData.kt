package com.elta.android.domain.features.glucose.widget.model

import java.time.LocalDateTime

/**
 * Модель данных для Home Screen Widget
 * Простой data class БЕЗ зависимостей - может использоваться в Widget Process
 *
 * @property glucoseValue Значение глюкозы (мг/дл или ммоль/л)
 * @property unit Единица измерения
 * @property trend Направление изменения глюкозы
 * @property breadUnits Хлебные единицы за день
 * @property lastMeasurementTime Время последнего измерения
 * @property syncStatus Статус синхронизации
 * @property reminderActive Активно ли напоминание
 * @property reminderMessage Текст напоминания
 * @property isAuthenticated Авторизован ли пользователь
 * @property isOnline Есть ли интернет-соединение
 */
data class GlucoseWidgetData(
    val glucoseValue: Int?,
    val unit: GlucoseUnit,
    val trend: GlucoseTrend?,
    val breadUnits: Float?,
    val lastMeasurementTime: LocalDateTime?,
    val syncStatus: SyncStatus,
    val reminderActive: Boolean,
    val reminderMessage: String?,
    val isAuthenticated: Boolean,
    val isOnline: Boolean,
    val glucoseDiff: Float? = null,
    val breadDiff: Float? = null,
    val insulinUnits: Float? = null,
    val chartPoints: List<Float> = emptyList(),
    val reminderTimeText: String? = null,
    val reminderDateText: String? = null
) {
    enum class GlucoseUnit {
        MG_DL,
        MMOL_L;

        fun symbol(): String = when (this) {
            MG_DL -> "мг/дл"
            MMOL_L -> "ммоль/л"
        }
    }

    enum class GlucoseTrend {
        UP,
        DOWN,
        STABLE,
        RISING_FAST,
        FALLING_FAST;

        fun arrow(): String = when (this) {
            UP -> "↑"
            DOWN -> "↓"
            STABLE -> "→"
            RISING_FAST -> "↑↑"
            FALLING_FAST -> "↓↓"
        }

        fun colorHex(): Long = when (this) {
            UP, RISING_FAST -> 0xFFE53935 // Red
            DOWN, FALLING_FAST -> 0xFF1976D2 // Blue
            STABLE -> 0xFF43A047 // Green
        }
    }

    enum class SyncStatus {
        SUCCESS,
        IDLE,
        REQUESTED,
        EMPTY,
        TIMEOUT,
        FAILED,
        NO_PRIMARY_DEVICE,
        // legacy values kept for backward compatibility
        SYNCED,
        SYNCING,
        ERROR,
        OFFLINE,
        NOT_AUTHENTICATED;

        fun icon(): String = when (this) {
            SUCCESS, SYNCED -> "✓"
            IDLE -> "↻"
            REQUESTED, SYNCING -> "⟳"
            EMPTY -> "▣"
            TIMEOUT, FAILED, ERROR -> "⚠"
            NO_PRIMARY_DEVICE -> "⌁"
            OFFLINE -> "○"
            NOT_AUTHENTICATED -> "🔒"
        }

        fun label(isCompact: Boolean = false): String = when (this) {
            SUCCESS, SYNCED -> "Все хорошо"
            IDLE -> if (isCompact) "Обновить" else "Требуется обновление"
            REQUESTED, SYNCING -> "Идет синхронизация"
            EMPTY -> "Новых данных нет"
            TIMEOUT -> "Ошибка: таймаут"
            FAILED, ERROR -> "Ошибка синхронизации"
            NO_PRIMARY_DEVICE -> "Нет подключенного прибора"
            OFFLINE -> "Нет связи"
            NOT_AUTHENTICATED -> "Требуется вход"
        }

        fun smallLabel(): String = when (this) {
            SUCCESS, SYNCED -> "Готово к работе"
            IDLE -> "Обновить"
            REQUESTED, SYNCING -> "Синхронизация"
            EMPTY -> "Нет данных"
            TIMEOUT, OFFLINE -> "Нет связи"
            FAILED, ERROR -> "Ошибка"
            NO_PRIMARY_DEVICE -> "Нет устройства"
            NOT_AUTHENTICATED -> "Требуется вход"
        }

        fun colorHex(): Long = when (this) {
            SUCCESS, SYNCED -> 0xFF43E695
            REQUESTED, SYNCING -> 0xFF38B7E1
            IDLE, EMPTY, OFFLINE -> 0xFFFF8058
            TIMEOUT, FAILED, ERROR, NO_PRIMARY_DEVICE -> 0xFFD91717
            NOT_AUTHENTICATED -> 0xFFD91717
        }
    }

    companion object {
        fun empty() = GlucoseWidgetData(
            glucoseValue = null,
            unit = GlucoseUnit.MG_DL,
            trend = null,
            breadUnits = null,
            lastMeasurementTime = null,
            syncStatus = SyncStatus.ERROR,
            reminderActive = false,
            reminderMessage = null,
            isAuthenticated = false,
            isOnline = false
        )

        fun noData() = GlucoseWidgetData(
            glucoseValue = null,
            unit = GlucoseUnit.MG_DL,
            trend = null,
            breadUnits = null,
            lastMeasurementTime = null,
            syncStatus = SyncStatus.ERROR,
            reminderActive = false,
            reminderMessage = null,
            isAuthenticated = true,
            isOnline = true
        )

        fun unauthorized() = GlucoseWidgetData(
            glucoseValue = null,
            unit = GlucoseUnit.MG_DL,
            trend = null,
            breadUnits = null,
            lastMeasurementTime = null,
            syncStatus= SyncStatus.NOT_AUTHENTICATED,
            reminderActive = false,
            reminderMessage = null,
            isAuthenticated = false,
            isOnline = true
        )

        fun offline() = GlucoseWidgetData(
            glucoseValue = null,
            unit = GlucoseUnit.MG_DL,
            trend = null,
            breadUnits = null,
            lastMeasurementTime = null,
            syncStatus = SyncStatus.OFFLINE,
            reminderActive = false,
            reminderMessage = null,
            isAuthenticated = true,
            isOnline = false
        )
    }
}
