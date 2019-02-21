package com.elta.android.data.features.diary.cache.dto

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import java.util.Date

@Entity
data class TagCachedDto(
    @Id(assignable = true) var id: Long,
    val secondaryId: String,
    val userId: Long?,
    val name: String,
    val image: String,
    val isReadOnly: Boolean,
    val modificationTime: Date?,
    val state: String
)