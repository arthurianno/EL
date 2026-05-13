package com.elta.android.data.features.reports.dto

import com.google.gson.annotations.SerializedName

data class ReportNetworkRequest(
    @SerializedName("startPeriod") val startDate: String,
    @SerializedName("endPeriod") val endDate: String,
    @SerializedName("glucose_format") val glucoseFormat: String,
    @SerializedName("languageTag") val languageTag: String
)
