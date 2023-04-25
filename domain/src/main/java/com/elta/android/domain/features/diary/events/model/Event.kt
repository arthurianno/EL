package com.elta.android.domain.features.diary.events.model

import com.elta.android.domain.features.calculator.model.Dish
import com.elta.android.domain.features.diary.tags.model.Tag
import org.threeten.bp.ZonedDateTime

data class Event(
    val id: String,
    val additionTime: ZonedDateTime,
    val tagId: String?,
    val tag: Tag?,
    val note: String?,
    val modificationTime: Long?,
    val value: Double?,
    val name: String?,
    val kind: String?,
    val temperature: Double?,
    val duration: Long?,
    val activityType: ActivityType?,
    val mealTag: MealTag?,
    val insulinType: InsulinType?,
    val medicament: String?,
    val type: EventType,
    val state: State,
    val glucometerSerialNumber: String?,
    val dishes: List<Dish>
)
