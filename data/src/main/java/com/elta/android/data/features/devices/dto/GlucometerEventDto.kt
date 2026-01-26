package com.elta.android.data.features.devices.dto

import com.elta.android.domain.features.diary.events.model.MealTag
import org.threeten.bp.ZonedDateTime

data class GlucometerEventDto(
    val id: String,
    val date: ZonedDateTime?,
    val temperature: Double?,
    val value: Double?,
    val glucometerSerialNumber: String?,
    val originalResponse: String?,
    val mealTag: MealTag? = null
)
