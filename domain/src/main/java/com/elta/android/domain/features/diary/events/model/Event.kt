package com.elta.android.domain.features.diary.events.model

import java.util.Date

data class Event(
    val id: String,
    val additionTime: Date,
    val additionTimeString: String,
    val tagId: String?,
    val note: String?,
    val modificationTime: Date?,
    val value: Double?,
    val name: String?,
    val kind: String?,
    val duration: String?,
    val activityType: ActivityType?,
    val mealTag: MealTag?,
    val insulinType: InsulinType?,
    val type: EventType
)