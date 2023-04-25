package com.elta.android.data.features.observers.model

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id

@Entity
data class ObserverDbEntity(
    @Id(assignable = true) var id: Long,
    val secondaryId: String,
    val email: String,
    val name: String?,
    val customName: String?,
    val status: String,
    val modificationTime: Long?,
    val state: String
)
