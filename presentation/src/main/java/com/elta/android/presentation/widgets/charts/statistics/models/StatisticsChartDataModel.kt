package com.elta.android.presentation.widgets.charts.statistics.models

import com.elta.android.domain.features.statistics.model.GlucoseStatisticModel

data class StatisticsChartDataModel(
    val maxValue: Double?,
    val minValue: Double?,
    val values: List<Double>,
    val statisticsPerDate: Map<DateModel, GlucoseStatisticModel?>
)