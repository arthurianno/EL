package com.elta.android.presentation.features.statistic.period.ui.adapter.items

import com.nullgr.core.adapter.items.ListItem

data class GlucoseStatisticChartItem(
    val datesTitle: String,
    val chartModel: Any // TODO provide real chart model
) : ListItem