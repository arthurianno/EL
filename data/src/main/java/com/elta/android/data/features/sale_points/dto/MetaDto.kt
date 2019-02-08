package com.elta.android.data.features.sale_points.dto

import com.google.gson.annotations.SerializedName

data class MetaDto(
    @SerializedName("totalItems") val totalItems: Int,
    @SerializedName("currentPage") val currentPage: Int,
    @SerializedName("pageSize") val pageSize: Int
)