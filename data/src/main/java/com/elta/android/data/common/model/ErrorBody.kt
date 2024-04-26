package com.elta.android.data.common.model

import com.google.gson.annotations.SerializedName

data class ErrorBody(
    @SerializedName("status") val status: Int,
    @SerializedName("errorCode") val errorCode: String,
    @SerializedName("message") val message: String,
)