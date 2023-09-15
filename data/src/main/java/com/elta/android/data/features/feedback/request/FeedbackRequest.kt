package com.elta.android.data.features.feedback.request

import com.google.gson.annotations.SerializedName

data class FeedbackRequest(
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("text") val message: String
)
