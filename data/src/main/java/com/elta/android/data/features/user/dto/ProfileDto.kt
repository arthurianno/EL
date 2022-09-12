package com.elta.android.data.features.user.dto

import com.google.gson.annotations.SerializedName

data class ProfileDto(
    @SerializedName("diabet") val diabetes: DiabetTypeDto?,
    @SerializedName("weight") val weight: Double?,
    @SerializedName("gender") val gender: GenderTypeDto?,
    @SerializedName("person") val person: PersonDto?,
    @SerializedName("glucoseLevelsBeforeEating") val glucoseLevelsBeforeEating: GlucoseLevelDto?,
    @SerializedName("glucoseLevelsAfterEating") val glucoseLevelsAfterEating: GlucoseLevelDto?,
    @SerializedName("glucoseLevelsAverage") val glucoseLevelsAverage: GlucoseLevelDto?,
    @SerializedName("email") val email: String?,
    @SerializedName("socialNetworks") var socialNetworks: List<SocialNetworkDto>?,
    @SerializedName("healthApps") var healthApps: List<HealthAppDto>?,
    @SerializedName("timestamp") val timeStamp: Long
)
