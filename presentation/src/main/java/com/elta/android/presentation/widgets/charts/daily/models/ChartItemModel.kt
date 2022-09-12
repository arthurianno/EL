package com.elta.android.presentation.widgets.charts.daily.models

import org.threeten.bp.ZonedDateTime

data class ChartItemModel(
    val value: Double,
    val dateTime: ZonedDateTime,
    val formattedTime: String,
    var hourOfEvent: Int = 0,
    var minutesOfEvent: Int = 0,
    val valueType: ChartItemValueType,
    val isMaxValue: Boolean = false,
    val isMinValue: Boolean = false,
    val isLastValue: Boolean = false,
    var isSelected: Boolean = false
)
