package com.elta.android.presentation.widgets.charts.statistics.models

import java.util.Date

data class DateModel(
    val date: Date?,
    val formattedDate: String?,
    val needDrawDateTile: Boolean,
    val isStub: Boolean = false
) : Comparable<DateModel> {

    override fun compareTo(other: DateModel) =
        when {
            this.date == null -> -1
            other.date == null -> 1
            else -> this.date.compareTo(other.date)
        }
}