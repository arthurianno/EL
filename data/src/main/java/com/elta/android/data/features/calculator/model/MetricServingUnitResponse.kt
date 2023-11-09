package com.elta.android.data.features.calculator.model

import com.google.gson.annotations.SerializedName

data class MetricServingUnitResponse(
    @SerializedName("id")
    val id: Int,

    @SerializedName("name")
    val name: String
)