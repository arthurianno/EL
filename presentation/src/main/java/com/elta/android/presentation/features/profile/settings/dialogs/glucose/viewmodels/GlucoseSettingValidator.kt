package com.elta.android.presentation.features.profile.settings.dialogs.glucose.viewmodels

import com.elta.android.domain.features.diary.home.model.GlucoseLevelSettings
import com.elta.android.presentation.features.profile.settings.dialogs.glucose.model.GlucoseLevel
import com.elta.android.presentation.features.profile.settings.dialogs.glucose.model.GlucoseRangeError
import com.elta.android.presentation.features.profile.settings.dialogs.glucose.model.isNotError
import com.elta.android.presentation.features.profile.settings.dialogs.glucose.model.toDoubleFormat
import com.elta.android.presentation.features.profile.settings.dialogs.glucose.model.toDoubleFormatOrDefault
import kotlin.math.max
import kotlin.math.min

private const val MAX_GLUCOSE_LEVEL = 20.0
private const val MIN_GLUCOSE_LEVEL = 1.0
private const val DIFFERENT_BETWEEN_MIN_MAX = 2.0

fun getErrorTypeByValues(minValue: String, maxValue: String): GlucoseRangeError =
    validateRange(
        minValue = minValue.toDoubleFormatOrDefault(MIN_GLUCOSE_LEVEL),
        maxValue = maxValue.toDoubleFormatOrDefault(MAX_GLUCOSE_LEVEL)
    )

private fun validateRange(minValue: Double, maxValue: Double): GlucoseRangeError =
    when {
        !minValue.toString().inRange() || !maxValue.toString()
            .inRange() -> GlucoseRangeError.OUT_OF_RANGE

        minValue > maxValue -> GlucoseRangeError.MAX_MUST_BE_HIGHER_THAN_MIN
        (maxValue - minValue) < DIFFERENT_BETWEEN_MIN_MAX -> GlucoseRangeError.DIFFERENT_BETWEEN_VALUES

        else -> GlucoseRangeError.NONE
    }

fun isRangeValid(currentGlucoseLevel: GlucoseLevel, startGlucoseLevel: GlucoseLevel): Boolean =
    isRangeChanged(currentGlucoseLevel, startGlucoseLevel) && isRangeCorrect(currentGlucoseLevel)

fun isRangeChanged(currentGlucoseLevel: GlucoseLevel, startGlucoseLevel: GlucoseLevel) =
    currentGlucoseLevel.beforeMeal.minLevel != startGlucoseLevel.beforeMeal.minLevel ||
            currentGlucoseLevel.beforeMeal.maxLevel != startGlucoseLevel.beforeMeal.maxLevel ||
            currentGlucoseLevel.afterMeal.minLevel != startGlucoseLevel.afterMeal.minLevel ||
            currentGlucoseLevel.afterMeal.maxLevel != startGlucoseLevel.afterMeal.maxLevel

fun isRangeCorrect(currentGlucoseLevel: GlucoseLevel): Boolean {
    val isBeforeMealCorrect = getErrorTypeByValues(
        currentGlucoseLevel.beforeMeal.minLevel,
        currentGlucoseLevel.beforeMeal.maxLevel
    ).isNotError()

    val isAfterMealCorrect = getErrorTypeByValues(
        currentGlucoseLevel.afterMeal.minLevel,
        currentGlucoseLevel.afterMeal.maxLevel
    ).isNotError()

    return isAfterMealCorrect && isBeforeMealCorrect
}

fun getMinLevel(beforeMeal: String, afterMeal: String): String =
    min(
        beforeMeal.toDoubleFormatOrDefault(GlucoseLevelSettings.NORMAL_START),
        afterMeal.toDoubleFormatOrDefault(GlucoseLevelSettings.NORMAL_START)
    ).toString()

fun getMaxLevel(beforeMeal: String, afterMeal: String): String =
    max(
        beforeMeal.toDoubleFormatOrDefault(GlucoseLevelSettings.NORMAL_END),
        afterMeal.toDoubleFormatOrDefault(GlucoseLevelSettings.NORMAL_END)
    ).toString()

fun String.inRange(): Boolean {
    val value = this.toDoubleFormat() ?: MIN_GLUCOSE_LEVEL
    return !(value > MAX_GLUCOSE_LEVEL || value < MIN_GLUCOSE_LEVEL)
}
