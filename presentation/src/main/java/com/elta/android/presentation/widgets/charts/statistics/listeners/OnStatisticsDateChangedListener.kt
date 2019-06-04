package com.elta.android.presentation.widgets.charts.statistics.listeners

import org.threeten.bp.LocalDate

interface OnStatisticsDateChangedListener {

    fun onUnselectedAll()

    fun onDateChanged(date: LocalDate)
}