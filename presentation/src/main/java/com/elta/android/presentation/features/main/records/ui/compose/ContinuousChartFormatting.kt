package com.elta.android.presentation.features.main.records.ui.compose

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import org.threeten.bp.LocalDate
import org.threeten.bp.temporal.ChronoUnit
import java.util.Locale

private val RUSSIAN_MONTHS = listOf(
    "января", "февраля", "марта", "апреля", "мая", "июня",
    "июля", "августа", "сентября", "октября", "ноября", "декабря"
)
internal fun percent(value: Int, total: Int): String =
    if (total == 0) "-" else "${value * 100 / total}%"

internal fun continuousFormatDateRange(start: LocalDate, end: LocalDate): String {
    val startMonth = RUSSIAN_MONTHS.getOrElse(start.monthValue - 1) { "" }
    val endMonth = RUSSIAN_MONTHS.getOrElse(end.monthValue - 1) { "" }
    return if (start == end) {
        "${start.dayOfMonth} $startMonth ${start.year}"
    } else {
        "${start.dayOfMonth} $startMonth ${start.year} -\n${end.dayOfMonth} $endMonth ${end.year}"
    }
}

internal fun continuousFormat(value: Float?): String =
    value?.let { String.format(Locale.US, "%.1f", it).replace('.', ',') } ?: "-"

internal fun continuousPointColor(value: Float) = when {
    value <= 3.9f -> ContinuousLow
    value >= 10f -> ContinuousHigh
    else -> ContinuousNormal
}

internal fun DetailedGlucosePoint.continuousMinute(origin: LocalDate): Long =
    ChronoUnit.DAYS.between(origin, date ?: origin) * DAY_MINUTES +
        continuousTimeMinutes(timeLabel)

internal fun DetailedInsulinEntry.continuousMinute(origin: LocalDate): Long =
    ChronoUnit.DAYS.between(origin, date ?: origin) * DAY_MINUTES +
        continuousTimeMinutes(timeLabel)

internal fun DetailedFoodEntry.continuousMinute(origin: LocalDate): Long =
    ChronoUnit.DAYS.between(origin, date ?: origin) * DAY_MINUTES +
        continuousTimeMinutes(timeLabel)

internal fun DetailedActivityEntry.continuousStartMinute(origin: LocalDate): Long =
    ChronoUnit.DAYS.between(origin, startDate ?: origin) * DAY_MINUTES +
        continuousTimeMinutes(startTimeLabel)

internal fun DetailedActivityEntry.continuousEndMinute(origin: LocalDate): Long =
    ChronoUnit.DAYS.between(origin, endDate ?: startDate ?: origin) * DAY_MINUTES +
        continuousTimeMinutes(endTimeLabel)

internal fun DetailedInsulinEntry.continuousValue() = value ?: units.continuousEventValue()
internal fun DetailedFoodEntry.continuousValue() = value ?: breadUnits.continuousEventValue()

private fun String.continuousEventValue(): Float =
    trim().substringBefore(' ').replace(',', '.').toFloatOrNull() ?: 0f

internal fun continuousTimeMinutes(label: String): Long {
    val parts = label.split(":")
    val hours = parts.getOrNull(0)?.toLongOrNull() ?: 0L
    val minutes = parts.getOrNull(1)?.toLongOrNull() ?: 0L
    return (hours * 60L + minutes).coerceIn(0L, DAY_MINUTES)
}

internal fun String.toContinuousLocalDate(): LocalDate =
    runCatching { LocalDate.parse(this) }.getOrElse { LocalDate.now() }

internal fun Context.continuousFindActivity(): Activity? {
    var current: Context = this
    while (current is ContextWrapper) {
        if (current is Activity) return current
        current = current.baseContext
    }
    return null
}
