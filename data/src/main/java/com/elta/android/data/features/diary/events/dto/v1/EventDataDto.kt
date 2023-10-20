package com.elta.android.data.features.diary.events.dto.v1

import com.elta.android.data.features.calculator.model.ProductResponse
import com.elta.android.data.features.diary.events.dto.ActivityTypeDto
import com.elta.android.data.features.diary.events.dto.EventTypeDto
import com.elta.android.data.features.diary.events.dto.MealTagDto
import com.google.gson.annotations.SerializedName

@Deprecated("use v2")
data class EventDataDto(
    @SerializedName("temperature") val temperature: Double?,
    @SerializedName("duration") val duration: Long?,
    @SerializedName("value") val value: Double?,
    @SerializedName("kind") val kind: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("activityType") val activityType: ActivityTypeDto?,
    @SerializedName("mealTagging") val mealTag: MealTagDto?,
    @SerializedName("insulinType") val insulinType: InsulinTypeDto?,
    @SerializedName("insulinMedicament") val insulinMedicament: InsulinMedicamentDataDto?,
    @SerializedName("eventType") val type: EventTypeDto,
    @SerializedName("glucometerSerialNumber") val glucometerSerialNumber: String?,
    @SerializedName("products") val products: List<ProductResponse>?,
    @SerializedName("productsCount") val productsCount: Long?
)
