package com.elta.android.data.features.version.model


import com.google.gson.annotations.SerializedName

data class VersionResponse(
    @SerializedName("update")
    val update: String
)
