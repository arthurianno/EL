package com.elta.android.data.features.diary.events.dto.v1

import com.elta.android.data.features.common.dto.MetaDto
import com.google.gson.annotations.SerializedName

@Deprecated("use v2")
data class EventsDto(
    @SerializedName("items") val events: List<EventDto>,
    @SerializedName("meta") val meta: MetaDto
)
