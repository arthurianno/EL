package com.elta.android.data.features.reminder.cache.dto

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import java.util.Date

@Entity
data class ReminderCacheDto(
    @Id(assignable = true) var id: Long,
    val secondaryId: String,
    val title: String,
    val time: Date?,
    val schedule: String
)