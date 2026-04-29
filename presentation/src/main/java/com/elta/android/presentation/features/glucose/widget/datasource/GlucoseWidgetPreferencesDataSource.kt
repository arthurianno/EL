package com.elta.android.presentation.features.glucose.widget.datasource

import android.content.Context
import android.content.SharedPreferences
import com.elta.android.domain.features.glucose.widget.model.GlucoseWidgetData
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

/**
 * SharedPreferences storage for widget data.
 * Used as a lightweight bridge between worker updates and Glance rendering.
 */
class GlucoseWidgetPreferencesDataSource(
    context: Context
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun saveWidgetData(data: GlucoseWidgetData) {
        prefs.edit().apply {
            putInt(KEY_GLUCOSE_VALUE, data.glucoseValue ?: -1)
            putString(KEY_UNIT, data.unit.name)
            data.trend?.let { putString(KEY_TREND, it.name) } ?: remove(KEY_TREND)
            data.breadUnits?.let { putFloat(KEY_BREAD_UNITS, it) } ?: remove(KEY_BREAD_UNITS)
            data.glucoseDiff?.let { putFloat(KEY_GLUCOSE_DIFF, it) } ?: remove(KEY_GLUCOSE_DIFF)
            data.breadDiff?.let { putFloat(KEY_BREAD_DIFF, it) } ?: remove(KEY_BREAD_DIFF)
            data.insulinUnits?.let { putFloat(KEY_INSULIN_UNITS, it) } ?: remove(KEY_INSULIN_UNITS)
            if (data.chartPoints.isNotEmpty()) {
                putString(KEY_CHART_POINTS, data.chartPoints.joinToString(","))
            } else {
                remove(KEY_CHART_POINTS)
            }
            data.lastMeasurementTime?.let {
                putLong(KEY_LAST_MEASUREMENT, it.toEpochSecond(ZoneOffset.UTC))
            } ?: remove(KEY_LAST_MEASUREMENT)
            putString(KEY_SYNC_STATUS, data.syncStatus.name)
            putBoolean(KEY_REMINDER_ACTIVE, data.reminderActive)
            data.reminderMessage?.let { putString(KEY_REMINDER_MESSAGE, it) } ?: remove(KEY_REMINDER_MESSAGE)
            data.reminderTimeText?.let { putString(KEY_REMINDER_TIME, it) } ?: remove(KEY_REMINDER_TIME)
            data.reminderDateText?.let { putString(KEY_REMINDER_DATE, it) } ?: remove(KEY_REMINDER_DATE)
            putBoolean(KEY_IS_AUTHENTICATED, data.isAuthenticated)
            putBoolean(KEY_IS_ONLINE, data.isOnline)
            putLong(KEY_UPDATED_AT, System.currentTimeMillis())
            apply()
        }
    }

    fun loadWidgetData(): GlucoseWidgetData {
        val glucoseValue = prefs.getInt(KEY_GLUCOSE_VALUE, -1).takeIf { it >= 0 }
        val unit = prefs.getString(KEY_UNIT, null)
            ?.let { GlucoseWidgetData.GlucoseUnit.valueOf(it) }
            ?: GlucoseWidgetData.GlucoseUnit.MG_DL
        val trend = prefs.getString(KEY_TREND, null)
            ?.let { GlucoseWidgetData.GlucoseTrend.valueOf(it) }
        val breadUnits = if (prefs.contains(KEY_BREAD_UNITS)) prefs.getFloat(KEY_BREAD_UNITS, 0f) else null
        val lastMeasurement = prefs.getLong(KEY_LAST_MEASUREMENT, -1L)
            .takeIf { it >= 0 }
            ?.let { LocalDateTime.ofInstant(Instant.ofEpochSecond(it), ZoneOffset.UTC) }
        val syncStatus = prefs.getString(KEY_SYNC_STATUS, null)
            ?.let { raw ->
                runCatching { GlucoseWidgetData.SyncStatus.valueOf(raw) }.getOrNull()
            }
            ?: GlucoseWidgetData.SyncStatus.ERROR
        val chartPoints = prefs.getString(KEY_CHART_POINTS, null)
            ?.split(",")
            ?.mapNotNull { it.trim().toFloatOrNull() }
            .orEmpty()

        return GlucoseWidgetData(
            glucoseValue = glucoseValue,
            unit = unit,
            trend = trend,
            breadUnits = breadUnits,
            lastMeasurementTime = lastMeasurement,
            syncStatus = syncStatus,
            reminderActive = prefs.getBoolean(KEY_REMINDER_ACTIVE, false),
            reminderMessage = prefs.getString(KEY_REMINDER_MESSAGE, null),
            isAuthenticated = prefs.getBoolean(KEY_IS_AUTHENTICATED, false),
            isOnline = prefs.getBoolean(KEY_IS_ONLINE, false),
            glucoseDiff = prefs.getFloatOrNull(KEY_GLUCOSE_DIFF),
            breadDiff = prefs.getFloatOrNull(KEY_BREAD_DIFF),
            insulinUnits = prefs.getFloatOrNull(KEY_INSULIN_UNITS),
            chartPoints = chartPoints,
            reminderTimeText = prefs.getString(KEY_REMINDER_TIME, null),
            reminderDateText = prefs.getString(KEY_REMINDER_DATE, null)
        )
    }

    private fun SharedPreferences.getFloatOrNull(key: String): Float? =
        if (contains(key)) getFloat(key, 0f) else null

    companion object {
        private const val PREFS_NAME = "glucose_widget_prefs"
        private const val KEY_GLUCOSE_VALUE = "glucose_value"
        private const val KEY_UNIT = "unit"
        private const val KEY_TREND = "trend"
        private const val KEY_BREAD_UNITS = "bread_units"
        private const val KEY_GLUCOSE_DIFF = "glucose_diff"
        private const val KEY_BREAD_DIFF = "bread_diff"
        private const val KEY_INSULIN_UNITS = "insulin_units"
        private const val KEY_CHART_POINTS = "chart_points"
        private const val KEY_LAST_MEASUREMENT = "last_measurement"
        private const val KEY_SYNC_STATUS = "sync_status"
        private const val KEY_REMINDER_ACTIVE = "reminder_active"
        private const val KEY_REMINDER_MESSAGE = "reminder_message"
        private const val KEY_REMINDER_TIME = "reminder_time"
        private const val KEY_REMINDER_DATE = "reminder_date"
        private const val KEY_IS_AUTHENTICATED = "is_authenticated"
        private const val KEY_IS_ONLINE = "is_online"
        private const val KEY_UPDATED_AT = "updated_at"
    }
}
