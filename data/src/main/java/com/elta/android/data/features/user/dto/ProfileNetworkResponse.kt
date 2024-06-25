package com.elta.android.data.features.user.dto

import com.google.gson.annotations.SerializedName

data class ProfileNetworkResponse(
    @SerializedName("diabet") val diabetes: DiabetesTypeNetworkEntity?,
    @SerializedName("weight") val weight: Double?,
    @SerializedName("gender") val gender: GenderTypeNetworkEntity?,
    @SerializedName("person") val person: PersonNetworkEntity?,
    @SerializedName("birthDate") val birthDate: String?,
    @SerializedName("glucoseLevelsBeforeEating") val glucoseLevelsBeforeEating: GlucoseLevelNetworkEntity?,
    @SerializedName("glucoseLevelsAfterEating") val glucoseLevelsAfterEating: GlucoseLevelNetworkEntity?,
    @SerializedName("glucoseLevelsAverage") val glucoseLevelsAverage: GlucoseLevelNetworkEntity?,
    @SerializedName("email") val email: String?,
    @SerializedName("socialNetworks") var socialNetworks: List<SocialNetworkDto>?,
    @SerializedName("healthApps") var healthApps: List<HealthAppNetworkEntity>?,
    @SerializedName("timestamp") val timeStamp: Long
)
