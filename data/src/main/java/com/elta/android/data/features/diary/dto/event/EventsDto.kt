package com.elta.android.data.features.diary.dto.event

import com.elta.android.data.features.common.dto.MetaDto
import com.google.gson.annotations.SerializedName

data class EventsDto(
    @SerializedName("events.json") val events: List<EventDto>,
    @SerializedName("meta") val meta: MetaDto
)