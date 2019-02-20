package com.elta.android.data.features.diary.dto.event

import com.elta.android.data.features.diary.dto.ActivityTypeDto
import com.elta.android.data.features.diary.dto.EventTypeDto
import com.elta.android.data.features.diary.dto.InsulinTypeDto
import com.elta.android.data.features.diary.dto.MealTagDto
import com.google.gson.annotations.SerializedName

data class EventDataDto(
    @SerializedName("duration") val duration: String?,
    @SerializedName("value") val value: Double?,
    @SerializedName("kind") val kind: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("activityType") val activityType: ActivityTypeDto?,
    @SerializedName("mealTagging") val mealTag: MealTagDto?,
    @SerializedName("insulinType") val insulinType: InsulinTypeDto?,
    @SerializedName("eventType") val type: EventTypeDto
)