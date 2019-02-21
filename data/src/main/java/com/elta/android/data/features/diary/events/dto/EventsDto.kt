package com.elta.android.data.features.diary.events.dto

import com.elta.android.data.features.common.dto.MetaDto
import com.google.gson.annotations.SerializedName

data class EventsDto(
    @SerializedName("events") val events: List<EventDto>,
    @SerializedName("meta") val meta: MetaDto
)