package com.elta.android.presentation.widgets.picker.model

open class FormMeasurementConfig(
    val firstPickerMaxValue: Int,
    val firstPickerMinValue: Int,
    val secondPickerMaxValue: Int,
    val secondPickerMinValue: Int,
    val firstMeasureUnit: String?,
    val secondMeasureUnit: String?,
    val resultMappingFunction: (left: Int, right: Int) -> Double
)