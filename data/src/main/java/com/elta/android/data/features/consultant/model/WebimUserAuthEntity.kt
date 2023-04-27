package com.elta.android.data.features.consultant.model

import com.google.gson.annotations.SerializedName

internal data class WebimUserAuthEntity(
    @SerializedName("fields") val fields: Fields,
    @SerializedName("hash") val hash: String
) {
    internal data class Fields(
        @SerializedName("display_name") val displayName: String,
        @SerializedName("id") val id: String
    )
}
