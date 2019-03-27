package com.elta.android.data.features.diary.tags.cache.dto

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import java.util.Date

@Entity
data class TagCachedDto(
    @Id(assignable = true) var id: Long,
    val secondaryId: String,
    val name: String,
    val image: String,
    val isReadOnly: Boolean,
    val modificationTime: Date?,
    val state: String
)