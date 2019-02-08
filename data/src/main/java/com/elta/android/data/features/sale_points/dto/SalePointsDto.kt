package com.elta.android.data.features.sale_points.dto

import com.google.gson.annotations.SerializedName

data class SalePointsDto(
    @SerializedName("data") val points: List<SalePointDto>,
    @SerializedName("meta") val meta: MetaDto
)