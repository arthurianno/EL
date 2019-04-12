package com.elta.android.presentation.widgets.charts.daily.models

data class ChartRangesModel(
    val start: Double,
    val end: Double,
    val normalMax: Double,
    val lowMax: Double?,
    val highMax: Double?
) {
    val needDrawLow: Boolean
        get() = lowMax != null

    val needDrawHigh: Boolean
        get() = highMax != null
}