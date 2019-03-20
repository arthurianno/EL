package com.elta.android.data.features.user.dto

import com.google.gson.annotations.SerializedName

//todo not need maybe
data class ProfileWithEmailDto(
    @SerializedName("diabet") val diabetType: DiabetTypeDto,
    @SerializedName("weight") val weight: Double,
    @SerializedName("gender") val gender: GenderTypeDto,
    @SerializedName("person") val person: PersonDto?,
    @SerializedName("glucoseLevel") val glucoseLevel: GlucoseLevelDto?,
    @SerializedName("email") val email: String,
    @SerializedName("timeStamp") val timeStamp: Int
)