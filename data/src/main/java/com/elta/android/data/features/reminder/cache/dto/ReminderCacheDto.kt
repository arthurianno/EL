package com.elta.android.data.features.reminder.cache.dto

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id

@Entity
data class ReminderCacheDto(
    @Id(assignable = true) var id: Long,
    val secondaryId: String,
    val title: String,
    val time: String,
    val schedule: String
)
