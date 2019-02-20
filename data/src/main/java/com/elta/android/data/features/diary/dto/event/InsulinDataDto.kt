package com.elta.android.data.features.diary.dto.event

import com.elta.android.data.features.diary.dto.EventDataDto
import com.elta.android.data.features.diary.dto.EventTypeDto
import com.elta.android.data.features.diary.dto.InsulinTypeDto
import com.google.gson.annotations.SerializedName

data class InsulinDataDto(
    @SerializedName("value") val value: Double,
    @SerializedName("insulinType") val insulinType: InsulinTypeDto,
    @SerializedName("eventType") override val type: EventTypeDto
) : EventDataDto