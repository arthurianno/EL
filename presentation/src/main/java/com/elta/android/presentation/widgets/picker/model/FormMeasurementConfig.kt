package com.elta.android.presentation.widgets.picker.model

import android.support.annotation.StringRes

open class FormMeasurementConfig(
    val firstPickerMaxValue: Int,
    val firstPickerMinValue: Int,
    val secondPickerMaxValue: Int,
    val secondPickerMinValue: Int,
    @StringRes val firstMeasureUnit: Int? = null,
    @StringRes val secondMeasureUnit: Int?,
    val resultMappingFunction: (left: Int, right: Int) -> Double
)