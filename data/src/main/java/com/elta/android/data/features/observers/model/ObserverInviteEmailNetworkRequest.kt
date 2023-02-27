package com.elta.android.data.features.observers.model

import com.google.gson.annotations.SerializedName

data class ObserverInviteEmailNetworkRequest(
    @SerializedName("email") val email: String
)
