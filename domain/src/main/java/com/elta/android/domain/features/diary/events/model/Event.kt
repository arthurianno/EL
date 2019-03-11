package com.elta.android.domain.features.diary.events.model

import com.elta.android.domain.features.diary.tags.model.Tag
import java.util.Date

data class Event(
    val id: String,
    val additionTime: Date,
    val additionTimeString: String,
    val tagId: String?,
    var tag: Tag?,
    val note: String?,
    val modificationTime: Date?,
    val value: Double?,
    val name: String?,
    val kind: String?,
    val duration: Long?,
    val activityType: ActivityType?,
    val mealTag: MealTag?,
    val insulinType: InsulinType?,
    val type: EventType,
    val state: State
)