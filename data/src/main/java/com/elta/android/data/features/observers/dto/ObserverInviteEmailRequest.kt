package com.elta.android.data.features.observers.dto

import com.google.gson.annotations.SerializedName

data class ObserverInviteEmailRequest(
    @SerializedName("email") val email: String
)
