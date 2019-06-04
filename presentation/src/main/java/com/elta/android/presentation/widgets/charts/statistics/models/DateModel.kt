package com.elta.android.presentation.widgets.charts.statistics.models

import org.threeten.bp.LocalDateTime

data class DateModel(
    val date: LocalDateTime?,
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