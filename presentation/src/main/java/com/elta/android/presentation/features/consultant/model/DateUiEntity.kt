package com.elta.android.presentation.features.consultant.model

sealed class DateUiEntity(val timestamp: Long) {
    data class Today(private val timestampOfDate: Long) : DateUiEntity(timestampOfDate)
    data class Yesterday(private val timestampOfDate: Long) : DateUiEntity(timestampOfDate)
    data class ThisYear(private val timestampOfDate: Long, val date: String) :
        DateUiEntity(timestampOfDate)

    data class Other(private val timestampOfDate: Long, val date: String) :
        DateUiEntity(timestampOfDate)
}
