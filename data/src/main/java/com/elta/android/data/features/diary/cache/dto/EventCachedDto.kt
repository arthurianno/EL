package com.elta.android.data.features.diary.cache.dto

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import java.util.Date

@Entity
data class EventCachedDto(
    @Id(assignable = true) var id: Long,
    val secondaryId: String,
    val type: String,
    val additionTime: Date,
    val tagId: String?,
    val note: String?,
    val modificationTime: Date?,

    // represents EventDataDto
    val value: Double?,
    val name: String?,
    val duration: String?,
    val activityType: String?,
    val mealTag: String?,
    val insulinType: String?
)