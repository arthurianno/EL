package com.elta.android.data.features.observers.model

import com.google.gson.annotations.SerializedName

data class ObserverUpdateNameNetworkRequest(
    @SerializedName("customName") val name: String
)
