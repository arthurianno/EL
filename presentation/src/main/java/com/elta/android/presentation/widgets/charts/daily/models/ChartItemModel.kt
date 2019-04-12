package com.elta.android.presentation.widgets.charts.daily.models

import java.util.Date

data class ChartItemModel(
    val value: Double,
    val dateTime: Date,
    val isMaxValue: Boolean = false,
    val isMinValue: Boolean = false,
    val isLastValue: Boolean = false,
    val isSelected: Boolean = false
)