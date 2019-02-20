package com.elta.android.data.features.diary.dto.event

import com.elta.android.data.features.diary.dto.ActivityTypeDto
import com.elta.android.data.features.diary.dto.EventDataDto
import com.elta.android.data.features.diary.dto.EventTypeDto
import com.google.gson.annotations.SerializedName

data class ActivityDataDto(
    @SerializedName("duration") val duration: String,
    @SerializedName("activityType") val activityType: ActivityTypeDto,
    @SerializedName("eventType") override val type: EventTypeDto
) : EventDataDto