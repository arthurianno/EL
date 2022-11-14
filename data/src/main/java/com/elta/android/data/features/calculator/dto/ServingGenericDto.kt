package com.elta.android.data.features.calculator.dto

import com.google.gson.annotations.SerializedName

data class ServingGenericDto(
    @SerializedName("serving") val servings: List<ServingDto>
)
