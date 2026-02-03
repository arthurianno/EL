package com.elta.android.data.features.multiLangsConfig.dto

import com.google.gson.annotations.SerializedName

data class ScreenDto(
    @SerializedName("slug")
    val slug: String,

    @SerializedName("title")
    val title: Map<String, String>?,

    @SerializedName("description")
    val description: Map<String, String>?,

    @SerializedName("backgroundImageUrl")
    val backgroundImageUrl: String?,

)

data class ScreenResponseDto(
    @SerializedName("content")
    val content: List<ScreenDto>
)