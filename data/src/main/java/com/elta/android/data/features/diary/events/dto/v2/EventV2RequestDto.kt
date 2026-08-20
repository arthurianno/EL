package com.elta.android.data.features.diary.events.dto.v2

import com.elta.android.data.features.calculator.model.ProductResponse
import com.elta.android.data.features.common.dto.StateDto
import com.elta.android.data.features.diary.events.dto.ActivityTypeDto
import com.elta.android.data.features.diary.events.dto.EventTypeDto
import com.elta.android.data.features.diary.events.dto.GlucoseInputTypeDto
import com.elta.android.data.features.diary.events.dto.MealTagDto
import com.google.gson.annotations.SerializedName
import java.util.Locale

data class EventV2RequestDto(
    @SerializedName("data") val data: EventDataV2RequestDto,
    @SerializedName("additionalTime") val additionTime: String,
    @SerializedName("tag") val tagId: String?,
    @SerializedName("note") val note: String?,
    @SerializedName("timeStamp") val modificationTime: Long?,
    @SerializedName("id") val id: String,
    @SerializedName("modifiedState") val state: StateDto
)

data class EventDataV2RequestDto(
    @SerializedName("temperature") val temperature: Double?,
    @SerializedName("duration") val duration: Long?,
    @SerializedName("value") val value: Double?,
    @SerializedName("kind") val kind: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("activityType") val activityType: ActivityTypeDto?,
    @SerializedName("mealTagging") val mealTag: MealTagDto?,
    @SerializedName("inputType") val inputType: GlucoseInputTypeDto?,
    @SerializedName("insulinMedicament") val insulinMedicament: InsulinMedicamentRequestDto?,
    @SerializedName("medicament") val medicament: MedicamentDto?,
    @SerializedName("tabletsNumber") val tabletsNumber: Double?,
    @SerializedName("eventType") val type: EventTypeDto,
    @SerializedName("glucometerSerialNumber") val glucometerSerialNumber: String?,
    @SerializedName("products") val products: List<ProductResponse>?,
    @SerializedName("productsCount") val productsCount: Long?,
    @SerializedName("isTimeInvalid") val isTimeInvalid: Boolean = false,
    @SerializedName("isTemperatureInvalid") val isTemperatureInvalid: Boolean = false
)

interface InsulinMedicamentRequestDto

data class ReferenceInsulinMedicamentRequestDto(
    @SerializedName("id") val id: Int
) : InsulinMedicamentRequestDto

data class CustomInsulinMedicamentRequestDto(
    @SerializedName("name") val name: String,
    @SerializedName("insulinType") val insulinType: String
) : InsulinMedicamentRequestDto

fun EventV2Dto.toRequestDto(): EventV2RequestDto =
    EventV2RequestDto(
        id = id,
        state = state,
        additionTime = additionTime,
        tagId = tagId,
        note = note,
        modificationTime = modificationTime,
        data = data.toRequestDto()
    )

private fun EventDataV2Dto.toRequestDto(): EventDataV2RequestDto =
    EventDataV2RequestDto(
        type = type,
        value = value,
        kind = kind,
        name = name,
        temperature = temperature,
        duration = duration,
        activityType = activityType,
        mealTag = mealTag,
        inputType = inputType,
        insulinMedicament = insulinMedicament?.toRequestDto(customName = name),
        medicament = medicament,
        tabletsNumber = tabletsNumber,
        glucometerSerialNumber = glucometerSerialNumber,
        products = products,
        productsCount = productsCount,
        isTimeInvalid = isTimeInvalid,
        isTemperatureInvalid = isTemperatureInvalid
    )

private fun InsulinMedicamentDto.toRequestDto(customName: String?): InsulinMedicamentRequestDto =
    if (isOther) {
        CustomInsulinMedicamentRequestDto(
            name = customName?.takeIf(String::isNotBlank) ?: name,
            insulinType = insulinType.code.toNetworkInsulinTypeCode()
        )
    } else {
        ReferenceInsulinMedicamentRequestDto(id = id)
    }

fun String.toNetworkInsulinTypeCode(): String =
    when (trim().uppercase(Locale.ROOT)) {
        LEGACY_ULTRASHORT,
        LEGACY_ULTRA_SHORT -> NETWORK_SHORT
        else -> trim().uppercase(Locale.ROOT)
    }

private const val NETWORK_SHORT = "SHORT"
private const val LEGACY_ULTRASHORT = "ULTRASHORT"
private const val LEGACY_ULTRA_SHORT = "ULTRA_SHORT"
