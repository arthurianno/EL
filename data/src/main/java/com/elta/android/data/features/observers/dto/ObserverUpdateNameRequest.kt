package com.elta.android.data.features.observers.dto

import com.google.gson.annotations.SerializedName

data class ObserverUpdateNameRequest(
    @SerializedName("customName") val name: String
)
