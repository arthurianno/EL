package com.elta.android.data.features.diary.events.dto

import com.google.gson.annotations.SerializedName

data class SimpleEventDto(
    @SerializedName("id") val id: String,
    @SerializedName("eventType") val type: EventTypeDto
)