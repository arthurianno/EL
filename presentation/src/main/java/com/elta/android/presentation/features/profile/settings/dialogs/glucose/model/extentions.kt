package com.elta.android.presentation.features.profile.settings.dialogs.glucose.model

import com.elta.android.domain.features.diary.home.model.DoubleRange

internal const val DOT_CHAR = '.'
internal const val COMMA_CHAR = ','

fun Double.toStringFormat() = this.toString().replace(DOT_CHAR, COMMA_CHAR)

fun String.toDoubleFormat() = this.replace(COMMA_CHAR, DOT_CHAR).toDoubleOrNull()

fun String.toDoubleFormatOrDefault(defaultValue: Double) = this.toDoubleFormat() ?: defaultValue

fun DoubleRange.toGlucoseRange() = GlucoseRange(
    minLevel = this.start.toStringFormat(),
    maxLevel = this.end.toStringFormat()
)
