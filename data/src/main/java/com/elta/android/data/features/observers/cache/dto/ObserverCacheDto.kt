package com.elta.android.data.features.observers.cache.dto

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import java.util.Date

@Entity
data class ObserverCacheDto(
    @Id(assignable = true) var id: Long,
    val secondaryId: String,
    val email: String,
    val name: String?,
    val status: String,
    val modificationTime: Date?,
    val state: String
)