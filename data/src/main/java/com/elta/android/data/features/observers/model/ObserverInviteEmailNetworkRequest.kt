package com.elta.android.data.features.observers.model

import com.google.gson.annotations.SerializedName

data class ObserverInviteEmailNetworkRequest(
    @SerializedName("email") val email: String,
    @SerializedName("languageTag") val languageTag: String? = null,
    @SerializedName("countryCode") val countryCode: String? = null
)
