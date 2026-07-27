package com.elta.android.domain.features.devices.model

import com.elta.android.domain.features.diary.events.model.MealTag
import org.threeten.bp.ZonedDateTime

data class GlucometerEvent(
    val id: String,
    val date: ZonedDateTime?,
    val temperature: Double?,
    val value: Double?,
    val glucometerSerialNumber: String?,
    val originalResponse: String,
    val mealTag: MealTag? = null,
    val isTimeInvalid: Boolean = false
)