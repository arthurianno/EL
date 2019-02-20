package com.elta.android.data.features.diary.dto.event

import com.elta.android.data.features.diary.dto.EventDataDto
import com.elta.android.data.features.diary.dto.EventTypeDto
import com.elta.android.data.features.diary.dto.MealTagDto
import com.google.gson.annotations.SerializedName

data class GlucoseDataDto(
    @SerializedName("value") val value: Double,
    @SerializedName("mealTagging") val mealTag: MealTagDto,
    @SerializedName("eventType") override val type: EventTypeDto
) : EventDataDto