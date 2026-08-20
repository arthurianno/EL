package com.elta.android.data.features.diary.events.dto.v2

import com.elta.android.data.features.calculator.model.ProductResponse
import com.elta.android.data.features.diary.events.dto.ActivityTypeDto
import com.elta.android.data.features.diary.events.dto.EventTypeDto
import com.elta.android.data.features.diary.events.dto.GlucoseInputTypeDto
import com.elta.android.data.features.diary.events.dto.MealTagDto
import com.google.gson.annotations.SerializedName

data class EventDataV2Dto(
    @SerializedName("temperature") val temperature: Double?,
    @SerializedName("duration") val duration: Long?,
    @SerializedName("value") val value: Double?,
    @SerializedName("kind") val kind: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("activityType") val activityType: ActivityTypeDto?,
    @SerializedName("mealTagging") val mealTag: MealTagDto?,
    @SerializedName("inputType") val inputType: GlucoseInputTypeDto?,
    @SerializedName("insulinMedicament") val insulinMedicament: InsulinMedicamentDto?,
    @SerializedName("medicament") val medicament: MedicamentDto?,
    @SerializedName("tabletsNumber") val tabletsNumber: Double?,
    @SerializedName("eventType") val type: EventTypeDto,
    @SerializedName("glucometerSerialNumber") val glucometerSerialNumber: String?,
    @SerializedName("products") val products: List<ProductResponse>?,
    @SerializedName("productsCount") val productsCount: Long?,
    @SerializedName("isTimeInvalid") val isTimeInvalid: Boolean = false,
    @SerializedName("isTemperatureInvalid") val isTemperatureInvalid: Boolean = false
)
