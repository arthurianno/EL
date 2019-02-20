package com.elta.android.data.features.diary.dto.event

import com.elta.android.data.features.diary.dto.EventDataDto
import com.elta.android.data.features.diary.dto.EventTypeDto
import com.google.gson.annotations.SerializedName

data class MedicamentsDataDto(
    @SerializedName("name") val name: String,
    @SerializedName("eventType") override val type: EventTypeDto
) : EventDataDto