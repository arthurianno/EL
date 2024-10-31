package com.elta.android.data.features.diary.events.dto

import com.elta.android.data.features.diary.events.mapper.toDomain
import com.elta.android.domain.features.diary.events.model.EventType
import com.elta.android.domain.features.diary.events.model.GlucoseInputType
import com.elta.android.domain.features.diary.home.model.CalculatorFlow

enum class EventTypeDto {
    GLYCATEDHEMOGLOBIN,
    BREAD,
    INSULIN,
    MEDICAMENTS,
    ACTIVITY,
    WEIGHT,
    GLUCOSE;

    companion object {
        fun EventType.toEventTypeDto(): EventTypeDto {
            return when (this) {
                EventType.Activity -> ACTIVITY
                is EventType.Bread -> BREAD
                is EventType.Glucose -> GLUCOSE
                EventType.Glycatedhemoglobin -> GLYCATEDHEMOGLOBIN
                EventType.Insulin -> INSULIN
                EventType.Medicaments -> MEDICAMENTS
                EventType.Weight -> WEIGHT
            }
        }

        fun EventTypeDto.toEventType(
            value: Double?,
            inputTypeDto: GlucoseInputTypeDto?
        ): EventType {
            return when (this) {
                GLYCATEDHEMOGLOBIN -> EventType.Glycatedhemoglobin
                BREAD -> {
                    val calculatorFlow = if (value != null)
                        CalculatorFlow.BREAD_UNITS
                    else
                        CalculatorFlow.PRODUCT_ONLY
                    EventType.Bread(calculatorFlow)
                }
                INSULIN -> EventType.Insulin
                MEDICAMENTS -> EventType.Medicaments
                ACTIVITY -> EventType.Activity
                WEIGHT -> EventType.Weight
                GLUCOSE -> {
                    inputTypeDto?.let {
                        EventType.Glucose(inputTypeDto.toDomain())
                    } ?: EventType.Glucose(GlucoseInputType.AUTO)
                }
            }
        }
    }
}
