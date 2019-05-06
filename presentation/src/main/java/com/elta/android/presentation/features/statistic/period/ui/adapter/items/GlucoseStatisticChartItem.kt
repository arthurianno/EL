package com.elta.android.presentation.features.statistic.period.ui.adapter.items

import com.elta.android.presentation.widgets.charts.statistics.models.StatisticsChartDataModel
import com.nullgr.core.adapter.items.ListItem

data class GlucoseStatisticChartItem(
    val datesTitle: String,
    val chartModel: StatisticsChartDataModel
) : ListItem