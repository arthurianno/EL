package com.elta.android.data.features.user.dto

import com.google.gson.annotations.SerializedName

data class PersonDto(
    @SerializedName("firstName") val firstName: String?,
    @SerializedName("lastName") val lastName: String?
)