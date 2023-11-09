package com.elta.android.presentation.features.profile.settings.dialogs.glucose.model

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.elta.android.presentation.R
import com.elta.android.presentation.features.profile.settings.dialogs.glucose.viewmodels.inRange

enum class GlucoseRangeError {
    OUT_OF_RANGE,
    DIFFERENT_BETWEEN_VALUES,
    MAX_MUST_BE_HIGHER_THAN_MIN,
    NONE
}

@Composable
fun GlucoseRangeError.getMessageByError() =
    when (this) {
        GlucoseRangeError.OUT_OF_RANGE -> stringResource(id = R.string.profile_settings_glucose_out_of_range_error)
        GlucoseRangeError.DIFFERENT_BETWEEN_VALUES -> stringResource(id = R.string.profile_settings_glucose_different_between_values_error)
        GlucoseRangeError.MAX_MUST_BE_HIGHER_THAN_MIN -> stringResource(id = R.string.profile_settings_glucose_max_must_be_higher_than_min_error)
        GlucoseRangeError.NONE -> ""
    }

fun GlucoseRangeError.isError(): Boolean = this != GlucoseRangeError.NONE

fun GlucoseRangeError.isNotError(): Boolean = this == GlucoseRangeError.NONE

fun GlucoseRangeError.isErrorWithoutOutOfRange(value: String): Boolean =
    when (this) {
        GlucoseRangeError.NONE -> false
        GlucoseRangeError.OUT_OF_RANGE -> !value.inRange()
        else -> true
    }
