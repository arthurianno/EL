package com.elta.android.data.features.emias.dto


import com.google.gson.annotations.SerializedName

data class EmiasStatusResponse(
    @SerializedName("credentials")
    val credentials: Credentials?,
    @SerializedName("linked")
    val linked: Boolean
) {
    data class Credentials(
        @SerializedName("birthDate")
        val birthDate: String,
        @SerializedName("oms")
        val oms: String
    )
}
