package com.elta.android.presentation.widgets.picker.model

import android.content.res.Resources
import androidx.annotation.StringRes

open class FormMeasurementConfig(
    val firstPickerMaxValue: Int,
    val firstPickerMinValue: Int,
    val secondPickerMaxValue: Int,
    val secondPickerMinValue: Int,
    @StringRes val firstMeasureUnit: Int? = null,
    @StringRes val secondMeasureUnit: Int?,
    val resultMappingFunction: (left: Int, right: Int) -> Double,
    val formatter: (Resources.(left: Int, right: Int) -> String?)?
)
