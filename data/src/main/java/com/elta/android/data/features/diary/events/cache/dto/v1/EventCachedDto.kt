package com.elta.android.data.features.diary.events.cache.dto.v1

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id

@Deprecated("use v2")
@Entity
data class EventCachedDto(
    @Id(assignable = true) var id: Long,
    val secondaryId: String,
    val type: String,
    val additionTime: Long,
    val additionTimeString: String,
    val tagId: String?,
    val note: String?,
    val modificationTime: Long?,
    val products: String?,

    val temperature: Double?,
    val value: Double?,
    val name: String?,
    val kind: String?,
    val duration: Long?,
    val activityType: String?,
    val mealTag: String?,
    val insulinType: String?,
    val medicament: String?,
    val state: String,
    val glucometerSerialNumber: String?
)
