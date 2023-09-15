package com.elta.android.data.features.diary.insulin.dto

import com.google.gson.annotations.SerializedName

data class DrugDto(
    @SerializedName("id") val id: Int,
    @SerializedName("insulinType") val insulinType: InsulinTypeDto,
    @SerializedName("foodName") val name: String
) {
    data class InsulinTypeDto(
        @SerializedName("code") val code: String,
        @SerializedName("id") val id: Int,
        @SerializedName("foodName") val name: String
    )
}
