package com.elta.android.presentation.widgets.charts.statistics.models

import java.util.Date

data class DateModel(
    val date: Date?,
    val formattedDate: String?,
    val needDrawDateTile: Boolean,
    val isStub: Boolean
)