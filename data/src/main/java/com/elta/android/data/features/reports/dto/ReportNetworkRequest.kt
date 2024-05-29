package com.elta.android.data.features.reports.dto

import com.google.gson.annotations.SerializedName

data class ReportNetworkRequest(
    @SerializedName("startPeriod") val startDate: String,
    @SerializedName("endPeriod") val endDate: String,
    @SerializedName("glucoseFormat") val glucoseFormat: String
)
