package com.elta.android.data.features.diary.events.dto

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