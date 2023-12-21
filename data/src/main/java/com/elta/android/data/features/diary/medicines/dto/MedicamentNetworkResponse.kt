package com.elta.android.data.features.diary.medicines.dto

import com.google.gson.annotations.SerializedName

data class MedicamentNetworkResponse(
    @SerializedName("id")
    val id: Long,

    @SerializedName("name")
    val name: String,

    @SerializedName("deleted")
    val deleted: Boolean,

    @SerializedName("other")
    val other: Boolean,

    @SerializedName("touchedAt")
    val touchedAt: Long
)
