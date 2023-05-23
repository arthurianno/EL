package com.elta.android.data.features.user.dto

import com.google.gson.annotations.SerializedName

data class ProfileSettingsNetworkResponse(
    @SerializedName("onboarded") val isOnboarded: Boolean,
    @SerializedName("glucoseFormat") val glucoseFormat: GlucoseFormatNetworkEntity
)
