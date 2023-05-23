package com.elta.android.data.features.user.dto

import com.google.gson.annotations.SerializedName

data class ProfileSettingsNetworkRequest(
    @SerializedName("onboarded") val isOnboarded: Boolean?,
    @SerializedName("glucoseFormat") val glucoseFormat: GlucoseFormatNetworkEntity?
)
