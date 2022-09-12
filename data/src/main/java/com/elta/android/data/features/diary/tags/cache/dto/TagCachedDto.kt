package com.elta.android.data.features.diary.tags.cache.dto

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id

@Entity
data class TagCachedDto(
    @Id(assignable = true) var id: Long,
    val secondaryId: String,
    val name: String,
    val image: String,
    val isReadOnly: Boolean,
    val modificationTime: Long?,
    val state: String
)
