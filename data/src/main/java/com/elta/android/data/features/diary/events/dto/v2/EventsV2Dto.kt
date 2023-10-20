package com.elta.android.data.features.diary.events.dto.v2

import com.elta.android.data.features.common.dto.MetaDto
import com.google.gson.annotations.SerializedName

data class EventsV2Dto(
    @SerializedName("items") val events: List<EventV2Dto>,
    @SerializedName("meta") val meta: MetaDto
)
