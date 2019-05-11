package com.elta.android.presentation.widgets.charts.statistics.listeners

import java.util.Date

interface OnStatisticsDateChangedListener {

    fun onUnselectedAll()

    fun onDateChanged(date: Date)
}