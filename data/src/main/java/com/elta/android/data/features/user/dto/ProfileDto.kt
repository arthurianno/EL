package com.elta.android.data.features.user.dto

import com.google.gson.annotations.SerializedName

data class ProfileDto(
    @SerializedName("diabet") val diabetes: DiabetTypeDto?,
    @SerializedName("weight") val weight: Double?,
    @SerializedName("gender") val gender: GenderTypeDto?,
    @SerializedName("person") val person: PersonDto?,
    @SerializedName("glucoseLevels") val glucoseLevel: GlucoseLevelDto?,
    @SerializedName("email") val email: String?,
    @SerializedName("socialNetworks") var socialNetworks: List<SocialNetworkDto>?,
    @SerializedName("timestamp") val timeStamp: Long
)